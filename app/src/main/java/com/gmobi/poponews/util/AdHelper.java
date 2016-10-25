package com.gmobi.poponews.util;

import android.content.Context;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.NewsDataService;
import com.momock.app.App;
import com.momock.service.IMessageService;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;
import com.reach.AdServiceManager;
import com.reach.IAd;
import com.reach.IAdItem;
import com.reach.IAdService;
import com.reach.IBannerAd;
import com.reach.ICallback;
import com.reach.IInterstitialAd;
import com.reach.INativeAd;
import com.reach.IServiceCallback;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 11/10 0010.
 */
public class AdHelper {


	private static AdHelper mIns = null;
	private static Context ctx;

	private static IAdService bannerAdService;
	private static IAdService interstitialAdService;

	private static AdView bannerFacebookAd = null;
	private static InterstitialAd articleInterstitialFacebookAd = null;
	private static InterstitialAd discoverInterstitialFacebookAd = null;
	private static IInterstitialAd interstitialAd = null;
	private static IBannerAd bannerAd = null;

	//private static INativeAd ads;
	private static HashMap<String, ArrayList<NewsItem>> adMaps;
	private static HashMap<String, Object> nativeAdMap;

	private static final int BANNER_AD_HEIGHT_DP = 50;



	private static final String ARTICLE_INTERSTITIAL_AD_PLACEMENT = "article-interstitial";
	private static final String DISCOVER_INTERSTITIAL_AD_PLACEMENT = "discover-interstitial";
	private static final String BANNER_AD_PLACEMENT = "article-banner";


	private static final String FACEBOOK_BANNER_AD_PLACEMENT = "1608179009437197_1669512926637138";
	private static final String FACEBOOK_ARTICLE_INTERSTITIAL_AD_PLACEMENT = "1608179009437197_1669513056637125";
	private static final String FACEBOOK_DISCOVER_INTERSTITIAL_AD_PLACEMENT = "1608179009437197_1669513209970443";

	private static GmobiAdPool gmobiAdPool= null;
	private static FacebookAdPool facebookAdPool= null;
	private static ArticleInsideAdPool articleAdPool= null;



	public static int NATIVE_AD_SRC_NONE =0;
	public static int NATIVE_AD_SRC_GMOBI =1;
	public static int NATIVE_AD_SRC_FACEBOOK =2;

	public static int AD_STATUS_ERR =-1;
	public static int AD_STATUS_NOT_READY =0;
	public static int AD_STATUS_READY =1;


	public synchronized static AdHelper getInstance(Context ctx) {
		if (mIns == null) {
			mIns = new AdHelper(ctx);
		}
		return mIns;

	}

	private AdHelper(Context ctx) {
		AdHelper.ctx = ctx;
		adMaps = new HashMap<>();
		nativeAdMap = new HashMap<>();
	}


	public NewsItem getNextAd()
	{
		if(articleAdPool== null)
			return null;
		return articleAdPool.getNextAd("");
	}

	public NewsItem getCurAdInArticleAdPool()
	{
		if(articleAdPool== null)
			return null;
		return articleAdPool.getCurAd();
	}


	public NewsItem getNextAd(String cid)
	{
		NewsDataService ds = (NewsDataService) App.get().getService(IDataService.class);
		NewsCategory ctg = ds.getCategoryById(cid);
		if(ctg.getAdSrc() == 0)
			return null;
		else if(ctg.getAdSrc() == NATIVE_AD_SRC_FACEBOOK)
		{
			if(facebookAdPool== null)
				return null;
			if(!isFacebookAdInvalid())
				return facebookAdPool.getNextAd(cid);
			else {
				ctg.setAdSrc(NATIVE_AD_SRC_GMOBI);
				return null;
			}
		}
		else if(ctg.getAdSrc() == NATIVE_AD_SRC_GMOBI)
		{
			if(gmobiAdPool== null)return null;

			return gmobiAdPool.getNextAd(cid);
		}

		return null;
	}


	public synchronized void initGmobiNativeAd(int count) {
		if(gmobiAdPool == null) {
			gmobiAdPool = new GmobiAdPool();
			gmobiAdPool.initPool(ctx,count);
		}
	}

	public synchronized void initArticleNativeAd(int count) {
		if(articleAdPool == null) {
			articleAdPool = new ArticleInsideAdPool();
			articleAdPool.initPool(ctx,count);
		}
	}

	public boolean closeAdFullScreen() {
		return ((gmobiAdPool == null) ? false : gmobiAdPool.closeFullScreen());
	}

	/*
	public boolean isAdReady(String cid) {
		return (adMaps.get(cid) != null && adMaps.get(cid).size() > 0);
	}

	public ArrayList<NewsItem> getAds(String cid) {
		if (isAdReady(cid))
			return adMaps.get(cid);
		else
			return null;
	}


	public Object getNativeAd(String cid) {
		if (nativeAdMap.containsKey(cid))
			return nativeAdMap.get(cid);
		return null;

	}*/

	public static void initDiscoverInterstitialAd(Context ctx, final int time, String publisher,
												  final String sns, final String url, final String title) {

		if (interstitialAdService == null)
		{
			AdServiceManager.get(ctx, new IServiceCallback<IAdService>(){

				@Override
				public void call(IAdService service) {
					interstitialAdService = service;
					interstitialAd = interstitialAdService.getInterstitialAd(DISCOVER_INTERSTITIAL_AD_PLACEMENT);
					AnalysisUtil.recordSnsAdsI(sns, url, title, AnalysisUtil.PROVIDER_GMOBI, "");
					interstitialAd.popup(time);
				}

			});
		}
		else
		{
			interstitialAd = interstitialAdService.getInterstitialAd(DISCOVER_INTERSTITIAL_AD_PLACEMENT);
			AnalysisUtil.recordSnsAdsI(sns, url, title, AnalysisUtil.PROVIDER_GMOBI, "");
			interstitialAd.popup(time);
		}



	}


	public static void initArticleInterstitialAd(Context ctx, final int time, String publisher,
												 final String id, final String title, final String type) {
		if (interstitialAdService == null)
		{
			AdServiceManager.get(ctx, new IServiceCallback<IAdService>(){

				@Override
				public void call(IAdService service) {
					interstitialAdService = service;
					interstitialAd = interstitialAdService.getInterstitialAd(ARTICLE_INTERSTITIAL_AD_PLACEMENT);
					AnalysisUtil.recordArticleAdsI(id, title, type, AnalysisUtil.PROVIDER_GMOBI, "");
					interstitialAd.popup(time);
				}

			});
		}
		else
		{
			interstitialAd = interstitialAdService.getInterstitialAd(ARTICLE_INTERSTITIAL_AD_PLACEMENT);
			AnalysisUtil.recordArticleAdsI(id, title, type, AnalysisUtil.PROVIDER_GMOBI, "");
			interstitialAd.popup(time);
		}

	}



	public static void initBannerAd(Context ctx, final ViewGroup parent, String publisher, final NewsItem item) {

		int width = SystemHelper.getScreenWidth(ctx);
		int widthDp = DipHelper.dip2px(DipHelper.px2dip(width)-20);

		if (bannerAdService == null)
		{
			AdServiceManager.get(ctx, new IServiceCallback<IAdService>(){

				@Override
				public void call(IAdService service) {
					bannerAdService = service;
					bannerAd = bannerAdService.getBannerAd(BANNER_AD_PLACEMENT, 340, BANNER_AD_HEIGHT_DP, null);
					parent.addView(bannerAd.create());

					if (item != null)
						AnalysisUtil.recordArticleAdsB(item.get_id(), item.getTitle(), item.getType(), AnalysisUtil.PROVIDER_GMOBI, "");
					bannerAd.load();
				}

			});
		}
		else
		{
			bannerAd = bannerAdService.getBannerAd(BANNER_AD_PLACEMENT, 340, BANNER_AD_HEIGHT_DP, null);
			parent.addView(bannerAd.create());

			if (item != null)
				AnalysisUtil.recordArticleAdsB(item.get_id(), item.getTitle(), item.getType(), AnalysisUtil.PROVIDER_GMOBI, "");
			bannerAd.load();
		}




	}







	public synchronized void initFacebookNativeAd(int count) {
		if(facebookAdPool == null) {
			facebookAdPool = new FacebookAdPool();
			facebookAdPool.initPool(ctx,count);
		}
	}


	public static void showFacebookBannerAd(Context ctx, final ViewGroup parent, NewsItem item) {
		parent.removeAllViews();

		Logger.error("[AD]:Get Facebook banner ad");
		bannerFacebookAd = new AdView(ctx, FACEBOOK_BANNER_AD_PLACEMENT, AdSize.BANNER_HEIGHT_50);
		parent.addView(bannerFacebookAd);

		if (item != null)
			AnalysisUtil.recordArticleAdsB(item.get_id(), item.getTitle(), item.getType(), AnalysisUtil.PROVIDER_FACEBOOOK, "");

		bannerFacebookAd.loadAd();

		bannerFacebookAd.setAdListener(new AdListener() {
			@Override
			public void onError(Ad ad, AdError adError) {
				Logger.error("[AD]:Get Facebook banner ad fail : " + adError.getErrorMessage());
			}

			@Override
			public void onAdLoaded(Ad ad) {
				Logger.error("[AD]:Facebook banner ad loaded");
			}

			@Override
			public void onAdClicked(Ad ad) {

			}
		});
	}

	public static void destroyFacebookBannerAd() {
		if (bannerFacebookAd != null)
			bannerFacebookAd.destroy();
	}

	public interface IInterstitialDismissCallback {
		void onInterstitialDismissed();
	}

	public static void showArticleFacebookInterstitialAd(Context ctx, final IInterstitialDismissCallback callback,
														 String id, String title, String type) {
		articleInterstitialFacebookAd = new InterstitialAd(ctx, FACEBOOK_ARTICLE_INTERSTITIAL_AD_PLACEMENT);

		AnalysisUtil.recordArticleAdsI(id, title, type, AnalysisUtil.PROVIDER_FACEBOOOK, "");

		articleInterstitialFacebookAd.loadAd();
		articleInterstitialFacebookAd.setAdListener(new InterstitialAdListener() {
			@Override
			public void onInterstitialDisplayed(Ad ad) {

			}

			@Override
			public void onInterstitialDismissed(Ad ad) {
				if (callback != null)
					callback.onInterstitialDismissed();
			}

			@Override
			public void onError(Ad ad, AdError adError) {
				Logger.error("[AD]:Get Facebook interstitial ad in article fail : " + adError.getErrorMessage());
			}

			@Override
			public void onAdLoaded(Ad ad) {
				articleInterstitialFacebookAd.show();
			}

			@Override
			public void onAdClicked(Ad ad) {

			}
		});
	}


	public static void showDiscoverFacebookInterstitialAd(Context ctx, final IInterstitialDismissCallback callback,
														  String sns, String url, String title) {
		discoverInterstitialFacebookAd = new InterstitialAd(ctx, FACEBOOK_DISCOVER_INTERSTITIAL_AD_PLACEMENT);
		AnalysisUtil.recordSnsAdsI(sns, url, title, AnalysisUtil.PROVIDER_GMOBI, "");
		discoverInterstitialFacebookAd.loadAd();
		discoverInterstitialFacebookAd.setAdListener(new InterstitialAdListener() {
			@Override
			public void onInterstitialDisplayed(Ad ad) {

			}

			@Override
			public void onInterstitialDismissed(Ad ad) {
				if (callback != null)
					callback.onInterstitialDismissed();
			}

			@Override
			public void onError(Ad ad, AdError adError) {
				Logger.error("[AD]:Get Facebook interstitial ad in discover fail : " + adError.getErrorMessage());
			}

			@Override
			public void onAdLoaded(Ad ad) {
				discoverInterstitialFacebookAd.show();
			}

			@Override
			public void onAdClicked(Ad ad) {

			}
		});
	}


	public void setAutoplayFlag(boolean flag)
	{
		if(gmobiAdPool != null)
			gmobiAdPool.setAutoPlayFlag(flag);
		if(facebookAdPool != null)
			facebookAdPool.setAutoPlayFlag(flag);
	}


	public boolean isFacebookAdInvalid()
	{
		return (facebookAdPool!=null && facebookAdPool.getPoolStatus()==AD_STATUS_ERR);
	}

	public void setFacebookAdInvalid()
	{
		if(facebookAdPool!=null&&facebookAdPool.getPoolStatus()!=AD_STATUS_READY)
			facebookAdPool.setPoolStatus(AD_STATUS_ERR);
	}







}
