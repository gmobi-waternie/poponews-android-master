package com.gmobi.poponews.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.WidgetNewsItems;
import com.gmobi.poponews.service.CacheService;
import com.gmobi.poponews.service.ConfigService;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.event.Event;
import com.momock.service.IMessageService;
import com.momock.util.*;
import com.reach.AdServiceManager;
import com.reach.IAd;
import com.reach.IAdItem;
import com.reach.IAdService;
import com.reach.ICallback;
import com.reach.INativeAd;
import com.reach.IServiceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 3/2 0002.
 * GMOBI Native Ad
 */
public class ArticleInsideAdPool implements IAdPool {
	private static final String NATIVE_AD_PLACEMENT = "article_inside";
	private static INativeAd ads;
	private static String[] IMAGE_FILTER = {IAdItem.IMAGE};
	private static String[] VIDEO_FILTER = {IAdItem.VIDEO};
	private static int status=AdHelper.AD_STATUS_NOT_READY;

	private ArrayList<NewsItem> poolList = new ArrayList<>();
	private Context mContext;
	private int idx= 0;

	//需要设置当前的cid
	@Override
	public NewsItem getNextAd(String cid) {
		if(isPoolEmpty() || getPoolStatus() < AdHelper.AD_STATUS_READY)
			return null;

		IConfigService cs = App.get().getService(IConfigService.class);

		if(!cs.isArticleNativeAdEnable())
			return null;

		if(!UiHelper.needShow(cs.getArticleNativeAdPercent()))
			return null;

		for(int i= 0; i<poolList.size(); i++) {
			idx++;
			Logger.debug("getNextAd curIdx="+idx);
			if(idx == poolList.size())
				idx= 0;

			NewsItem news = poolList.get(idx);
			if(!news.getPreview().equals("")) {
				news.set_cid(cid);
				return news;
			}
		}
		return null;
	}

	public NewsItem getCurAd()
	{
		if(idx <0|| idx>poolList.size())
			return null;
		return poolList.get(idx);
	}

	@Override
	public int getPoolSize() {
		return poolList.size();
	}

	@Override
	public void initPool(final Context ctx, final int count) {
		status = AdHelper.AD_STATUS_NOT_READY;
		poolList.clear();

		mContext = ctx;

		AnalysisUtil.recordNewsListAd("", AnalysisUtil.PROVIDER_GMOBI, count + "");

		final IConfigService cs = App.get().getService(IConfigService.class);

		AdServiceManager.get(ctx, new IServiceCallback<IAdService>(){

			@Override
			public void call(IAdService adService) {
				int type = cs.getAdFilterType(ctx);
				Logger.debug("getAdFilterType : "+type);


				ads = adService.getNativeAd(NATIVE_AD_PLACEMENT, 200, 160, count, type == ConfigService.AD_FILTER_IMAGE  ? IMAGE_FILTER :VIDEO_FILTER);
				ads.setAutoplayMode(cs.isVideoPlayAuto());
				ads.setOnLoadLisenter(new ICallback() {

					@Override
					public void call(int resultCode) {
						if (resultCode == IAd.OK) {
							status = AdHelper.AD_STATUS_READY;

							for (int i = 0; i < ads.getCount(); i++) {

								IAdItem aditem = ads.getAdItem(i);

								NewsItem newsitem = new NewsItem();
								newsitem.set_id("ad" + i);
								Object title = aditem.execute("get", new Object[]{IAdItem.TITLE});
								newsitem.setTitle((String)title);
								newsitem.setType(NewsItem.NEWS_TYPE_AD);
								newsitem.setFav(NewsItem.NEWS_FAV_NONE);
								newsitem.setHaveRead(NewsItem.NEWS_NOT_READ);
								newsitem.setAdObj(aditem);
								newsitem.setPreview("");//用来判断是否下完广告图片。空为没下完，否则为下完。
								Object img = aditem.execute("get", new Object[]{IAdItem.IMAGE});
								if (img instanceof JSONObject) {
									JSONObject jo = (JSONObject) img;

									try {
										downloadImage(mContext,jo.getString("url"), newsitem);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								poolList.add(newsitem);
							}


						}
						else
						{
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
		status =  s;
	}
	@Override
	public boolean isPoolEmpty() {
		return (poolList.size() == 0);
	}

	public void setAutoPlayFlag(boolean flag) {
		ads.setAutoplayMode(flag);
	}

	public boolean closeFullScreen() {
		if(!ads.isFullscreen())
			return false;
		ads.closeFullscreen();
		return true;
	}

	private ExecutorService executorService = Executors.newFixedThreadPool(10);


	public void downloadImage(final Context mContext, final String fullUri,  final NewsItem item) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				HttpHelper.download(fullUri, CacheService.getInstance(mContext).getCachePathOf(CacheNames.AD_IMAGE_CACHEDIR, fullUri));
				Logger.error("download Ad Image" + fullUri + "! save as"+CacheService.getInstance(mContext).getCachePathOf("AdImage", fullUri));
				item.setPreview(fullUri);
			}
		});
	}
}
