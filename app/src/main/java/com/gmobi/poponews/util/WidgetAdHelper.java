package com.gmobi.poponews.util;

import android.content.Context;

import com.gmobi.poponews.model.WidgetNewsItems;
import com.gmobi.poponews.model.WidgetNewsItems.NewsEntity;
import com.momock.util.Logger;
import com.reach.AdServiceManager;
import com.reach.IAd;
import com.reach.IAdItem;
import com.reach.IAdService;
import com.reach.ICallback;
import com.reach.INativeAd;
import com.reach.IServiceCallback;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 11/10 0010.
 */
public class WidgetAdHelper {


	private static WidgetAdHelper mIns = null;
	private static Context mContext;
	private static IAdService adService;
	private static final String NATIVE_AD_PLACEMENT_FORMAT = "adCh:{adCh}/adDch:{adDch}/list:{cid}";
	private static String[] IMAGE_FILTER = {IAdItem.IMAGE};

	//private static INativeAd ads;
	private static HashMap<String, ArrayList<WidgetNewsItems.NewsEntity>> adMaps;
	private static HashMap<String, Object> nativeAdMap;

	private static final String FACEBOOK_NATIVE_AD_PLACEMENT = "1608179009437197_1666024683652629";


	public synchronized static WidgetAdHelper getInstance(Context ctx) {
		if (mIns == null) {
			mIns = new WidgetAdHelper(ctx);
		}
		return mIns;

	}

	private WidgetAdHelper(Context ctx) {
		WidgetAdHelper.mContext = ctx;
		adMaps = new HashMap<>();
		nativeAdMap = new HashMap<>();
	}


	public boolean isAdReady(String cid) {

		if (adMaps.size() == 0 || adMaps.get(cid) == null) {

			initAd(cid);
			return false;
		} else
			return (adMaps.get(cid) != null && adMaps.get(cid).size() > 0);
	}

	public ArrayList<WidgetNewsItems.NewsEntity> getAds(String cid) {
		if (isAdReady(cid))
			return adMaps.get(cid);
		else
			return null;
	}


	public static void clearAd() {
		if (nativeAdMap != null)
			nativeAdMap.clear();
		if (adMaps != null)
			adMaps.clear();
	}


	public Object getNativeAd(String cid) {
		if (nativeAdMap.containsKey(cid))
			return nativeAdMap.get(cid);
		return null;

	}

	private String adCh;
	private String adDch;
	private String adPlacement;
	private int adCount;
	public void initAd(final String cid) {
		Logger.debug("[AD]:" + cid + " initAd");
		boolean enable = PreferenceHelper.getAdEnable(mContext);
		adCount = PreferenceHelper.getAdCount(mContext);

		if (!enable || adCount <= 0) {
			Logger.debug("[AD]:AD is disabled on server.");
			return;
		}


		if (nativeAdMap.containsKey(cid) && adMaps.size() > 0) {
			Logger.debug("[AD]:" + cid + " has ad already");
			return;
		}


		adCh = PreferenceHelper.getCurChannel(mContext);
		adDch = PreferenceHelper.getDch(mContext);
		adPlacement = NATIVE_AD_PLACEMENT_FORMAT
				.replace("{adCh}", adCh)
				.replace("{adDch}", adDch)
				.replace("{cid}", cid);


		AdServiceManager.get(mContext, new IServiceCallback<IAdService>() {

					@Override
					public void call(IAdService service) {
						adService = service;
						if (adService == null) {
							System.out.println("[AD]:adService create error2");
						} else {
							final INativeAd ads = adService.getNativeAd(adPlacement, 200, 160, adCount, IMAGE_FILTER);

							ArrayList<NewsEntity> adItemList = new ArrayList<>();
							adMaps.put(cid, adItemList);
							nativeAdMap.put(cid, ads);

							ads.setOnLoadLisenter(new ICallback() {

								@Override
								public void call(int resultCode) {
									if (resultCode == IAd.OK) {

										ArrayList<NewsEntity> adItemList = adMaps.get(cid);
										if (adItemList == null)
											adItemList = new ArrayList<NewsEntity>();


										INativeAd nativeAd = (INativeAd) nativeAdMap.get(cid);
										if (nativeAd == null)
											nativeAd = ads;
										Logger.error("[AD]:native ad is got in " + cid + ",count=" + nativeAd.getCount());

										for (int i = 0; i < nativeAd.getCount(); i++) {

											IAdItem aditem = nativeAd.getAdItem(i);

											Object t = aditem.execute("get", new Object[]{IAdItem.TITLE});

											NewsEntity newsitem = new NewsEntity();
											newsitem.set_id("ad" + (String) t + cid);
											newsitem.set_cid(cid);
											newsitem.setTitle("ad" + i);
											newsitem.setType(WidgetNewsItems.NEWS_TYPE_AD);
											newsitem.setAdObj(aditem);
											adItemList.add(newsitem);
										}

										WidgetDataHelper.NotifyListRemoteViewService(mContext);

									}

								}

							});
							ads.load();
						}
					}
				});


	}


}
