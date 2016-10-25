package com.gmobi.poponews.util;

import android.content.Context;

import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.ConfigService;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.service.IMessageService;
import com.momock.util.Logger;
import com.reach.AdServiceManager;
import com.reach.IAd;
import com.reach.IAdItem;
import com.reach.IAdService;
import com.reach.ICallback;
import com.reach.INativeAd;
import com.reach.IServiceCallback;

import java.util.ArrayList;

/**
 * Created by Administrator on 3/2 0002.
 * GMOBI Native Ad
 */
public class GmobiAdPool implements IAdPool {
	private static final String NATIVE_AD_PLACEMENT = "news-list";
	private static INativeAd ads;
	private static String[] IMAGE_FILTER = {IAdItem.IMAGE};
	private static String[] VIDEO_FILTER = {IAdItem.VIDEO};
	private static int status = AdHelper.AD_STATUS_NOT_READY;

	private ArrayList<NewsItem> poolList = new ArrayList<>();

	//需要设置当前的cid
	@Override
	public NewsItem getNextAd(String cid) {
		if (isPoolEmpty() || getPoolStatus() < AdHelper.AD_STATUS_READY)
			return null;

		NewsItem news = poolList.get(0);
		news.set_cid(cid);
		poolList.remove(0);
		return news;
	}

	@Override
	public int getPoolSize() {
		return poolList.size();
	}

	@Override
	public void initPool(final Context ctx, final int count) {
		status = AdHelper.AD_STATUS_NOT_READY;
		poolList.clear();


		AnalysisUtil.recordNewsListAd("", AnalysisUtil.PROVIDER_GMOBI, count + "");

		final IConfigService cs = App.get().getService(IConfigService.class);

		AdServiceManager.get(ctx, new IServiceCallback<IAdService>() {

			@Override
			public void call(IAdService adService) {

				int type = cs.getAdFilterType(ctx);
				Logger.debug("getAdFilterType : " + type);


				ads = adService.getNativeAd(NATIVE_AD_PLACEMENT, 200, 160, count, type == ConfigService.AD_FILTER_IMAGE ? IMAGE_FILTER : VIDEO_FILTER);
				ads.setAutoplayMode(cs.isVideoPlayAuto());
				ads.setOnLoadLisenter(new ICallback() {

					@Override
					public void call(int resultCode) {
						if (resultCode == IAd.OK) {
							status = AdHelper.AD_STATUS_READY;

							for (int i = 0; i < ads.getCount(); i++) {

								IAdItem aditem = ads.getAdItem(i);

								Object title = aditem.execute("get", new Object[]{IAdItem.TITLE});
								Logger.debug("[AD] title=" + title);
								NewsItem newsitem = new NewsItem();
								newsitem.set_id("ad" + i);
								newsitem.setTitle("ad" + i);
								newsitem.setType(NewsItem.NEWS_TYPE_AD);
								newsitem.setLayoutType(NewsItem.NEWS_LAYOUT_AD);
								newsitem.setFav(NewsItem.NEWS_FAV_NONE);
								newsitem.setHaveRead(NewsItem.NEWS_NOT_READ);
								newsitem.setAdObj(aditem);
								poolList.add(newsitem);
							}
							App.get().getService(IMessageService.class).send(this, MessageTopics.AD_READY);

						} else {
							status = AdHelper.AD_STATUS_ERR;
						}
						AnalysisUtil.endRecordNewsListAd("", AnalysisUtil.PROVIDER_GMOBI, count + "");

					}

				});
				ads.load();
			}
		});


	}

	@Override
	public void addAdIntoPool(Object AdItem) {

	}

	@Override
	public int getPoolStatus() {
		return status;
	}

	@Override
	public void setPoolStatus(int s) {
		status = s;
	}

	@Override
	public boolean isPoolEmpty() {
		return (poolList.size() == 0);
	}

	public void setAutoPlayFlag(boolean flag) {
		ads.setAutoplayMode(flag);
	}

	public boolean closeFullScreen() {
		if (!ads.isFullscreen())
			return false;
		ads.closeFullscreen();
		return true;
	}
}
