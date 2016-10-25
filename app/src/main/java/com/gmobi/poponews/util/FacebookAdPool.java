package com.gmobi.poponews.util;

import android.content.Context;

import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.util.Logger;

import java.util.ArrayList;

/**
 * Created by Administrator on 3/2 0002.
 * Facebook Native AD
 */
public class FacebookAdPool implements IAdPool {

	private ArrayList<NewsItem> poolList = new ArrayList<>();
	private NativeAdsManager nativeFacebookAd = null;
	private static int status = AdHelper.AD_STATUS_NOT_READY;
	//private static final String FACEBOOK_NATIVE_AD_PLACEMENT = "1608179009437197_1666024683652629";
	private static final String FACEBOOK_NATIVE_AD_PLACEMENT = BuildConfig.FB_NATIVE_LIST;
	//需要设置当前的cid
	@Override
	public NewsItem getNextAd(String cid) {
		if (isPoolEmpty() || getPoolStatus()<AdHelper.AD_STATUS_READY)
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
	public void initPool(Context ctx, final int count) {
		//AdSettings.addTestDevice("fd4da392a2028e36d973d04c2bc057a7");
		status = AdHelper.AD_STATUS_NOT_READY;
		poolList.clear();
		AnalysisUtil.recordNewsListAd("", AnalysisUtil.PROVIDER_FACEBOOOK, count + "");

		Logger.debug("[AD]:showFacebookNativeAd");

		final IConfigService cs = App.get().getService(IConfigService.class);

		nativeFacebookAd = new NativeAdsManager(ctx, FACEBOOK_NATIVE_AD_PLACEMENT, count);
		nativeFacebookAd.setListener(
				new NativeAdsManager.Listener() {
					@Override
					public void onAdsLoaded() {
						status = AdHelper.AD_STATUS_READY;
						Logger.error("[AD]:Facebook getUniqueNativeAdCount() ="+nativeFacebookAd.getUniqueNativeAdCount());

						for (int i = 0; i < nativeFacebookAd.getUniqueNativeAdCount(); i++) {
							NativeAd ad = nativeFacebookAd.nextNativeAd();

							if (ad != null) {

								ad.setMediaViewAutoplay(cs.isVideoPlayAuto());
								NewsItem newsitem = new NewsItem();
								newsitem.set_id("facebookad" + i);
								newsitem.setTitle(ad.getAdTitle());
								newsitem.setListPreview(ad.getAdCoverImage().getUrl());
								newsitem.setGrid1Preview(ad.getAdCoverImage().getUrl());
								newsitem.setGrid2Preview(ad.getAdCoverImage().getUrl());
								newsitem.setGrid3Preview(ad.getAdCoverImage().getUrl());
								newsitem.setPinPreview(ad.getAdCoverImage().getUrl());
								newsitem.setPin2Preview(ad.getAdCoverImage().getUrl());
								newsitem.setType(NewsItem.NEWS_TYPE_FACEBOOKAD);
								newsitem.setLayoutType(NewsItem.NEWS_LAYOUT_FACEBOOKAD);
								newsitem.setFav(NewsItem.NEWS_FAV_NONE);
								newsitem.setHaveRead(NewsItem.NEWS_NOT_READ);
								newsitem.setAdObj(ad);
								poolList.add(newsitem);

							}
						}


						AnalysisUtil.endRecordNewsListAd("", AnalysisUtil.PROVIDER_FACEBOOOK, count + "");
					}

					@Override
					public void onAdError(AdError adError) {
						status = AdHelper.AD_STATUS_ERR;
						AnalysisUtil.endRecordNewsListAd("", AnalysisUtil.PROVIDER_FACEBOOOK,  count + "");
					}
				}

		);

		nativeFacebookAd.loadAds(NativeAd.MediaCacheFlag.ALL);
		UiHelper.startFbAdTimer();

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
		for (int i = 0; i < poolList.size(); i++) {
			NewsItem item = poolList.get(i);
			NativeAd ad = (NativeAd) item.getAdObj();
			ad.setMediaViewAutoplay(flag);
		}

	}
}
