package com.gmobi.poponews.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;


import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.activity.WidgetEditionsActivity;
import com.gmobi.poponews.model.EditionList;
import com.gmobi.poponews.model.WidgetNewsCategory;
import com.gmobi.poponews.model.WidgetNewsItems;
import com.gmobi.poponews.provider.PoponewsProvider;
import com.gmobi.poponews.service.CacheService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 10/28 0028.
 */
public class WidgetDataHelper {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
/*    private static String testStr =  "{\"categories\":" +
            "[{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c83\",\"layout\":\"grid\",\"name\":\"国内\"}," +
            "{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c84\",\"layout\":\"list\",\"name\":\"国际\"}," +
            "{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c85\",\"layout\":\"list\",\"name\":\"军事\"}," +
            "{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c8a\",\"layout\":\"pin\",\"name\":\"图片\"}," +
            "{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c86\",\"layout\":\"list\",\"name\":\"财经\"}," +
            "{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c87\",\"layout\":\"list\",\"name\":\"科技\"}," +
            "{\"type\":\"news\",\"id\":\"54ffecefa6cbab2f96285c89\",\"layout\":\"grid\",\"name\":\"时尚\"}," +
            //"{\"id\":\"54ffecefa6cbab2f96285c88\",\"type\":\"news\",\"extra\":\"\",\"layout\":\"pin2\",\"name\":\"娱乐\"}," +
            //"{\"id\":\"55b9845446c3f58420f7f14a\",\"type\":\"social\",\"extra\":{\"sources\":[{\"config\":{\"channels\":[{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=civilnews&tn=rss\",\"name\":\"国内\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=internews&tn=rss\",\"name\":\"国际\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=mil&tn=rss\",\"name\":\"军事\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=finannews&tn=rss\",\"name\":\"财经\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=internet&tn=rss\",\"name\":\"互联网\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=housenews&tn=rss\",\"name\":\"房产\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=autonews&tn=rss\",\"name\":\"汽车\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=sportnews&tn=rss\",\"name\":\"体育\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=enternews&tn=rss\",\"name\":\"娱乐\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=gamenews&tn=rss\",\"name\":\"游戏\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=edunews&tn=rss\",\"name\":\"教育\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=healthnews&tn=rss\",\"name\":\"女人\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=technnews&tn=rss\",\"name\":\"科技\"},{\"rss_url\":\"http:\\/\\/news.baidu.com\\/n?cmd=1&class=socianews&tn=rss\",\"name\":\"社会\"}]},\"title\":\"百度新闻\",\"name\":\"baidu\"}]},\"layout\":\"list\",\"name\":\"Social\"}" +
            "]}";*/

    public static String getCategory(Context ctx) throws JSONException {
        HttpHelper hh = new HttpHelper(ctx);
        String ctgCacheData = PreferenceHelper.readCategory(ctx);
        WidgetNewsCategory ctgList;
        int idx = 0;
        if (ctgCacheData.equals("")) {
            String ctgRsp = hh.getCategory();
            JSONObject connectJo = new JSONObject(ctgRsp);
            JSONObject ctgjo = new JSONObject();

            ctgjo.put(WidgetNewsCategory.CATEOGRY_PREFIX, connectJo.getJSONArray(HttpHelper.TAG_CATEGORIES));

            //TODO:
            ctgList = GsonImpl.get().toObject(ctgjo.toString(), WidgetNewsCategory.class);

            PreferenceHelper.saveCategory(ctx, (ctgList == null || ctgList.getCategories().size() == 0) ? "" : connectJo.getJSONArray(HttpHelper.TAG_CATEGORIES).toString());

            idx = 0;
        } else {
            JSONObject ctgjo = new JSONObject();
            ctgjo.put(WidgetNewsCategory.CATEOGRY_PREFIX, new JSONArray(ctgCacheData));

            //TODO
            ctgList = GsonImpl.get().toObject(ctgjo.toString(), WidgetNewsCategory.class);

            idx = PreferenceHelper.readCurCategoryIdx(ctx);
            if (idx >= ctgList.getCategories().size())
                idx = 0;
            else if(idx < 0)
                idx = ctgList.getCategories().size() - 1;



            if(ctgList.getCategories().get(idx).getType().equals("social"))
            {
                int direct = PreferenceHelper.readFetchDirect(ctx);

                if(direct > 0)
                {
                    idx ++;
                    if (idx >= ctgList.getCategories().size())
                        idx = 0;
                }

                else
                {
                    idx -- ;
                    if(idx < 0)
                        idx = ctgList.getCategories().size() - 1;
                }
            }
        }

        PreferenceHelper.saveCurCategoryIdx(ctx, idx);
        PreferenceHelper.saveCurCategoryId(ctx, ctgList.getCategories().get(idx).getId());
        PreferenceHelper.saveCategoryCount(ctx, ctgList.getCategories().size());
        PreferenceHelper.saveCurCategoryName(ctx, ctgList.getCategories().get(idx).getName());


        PreferenceHelper.saveFetchDirect(ctx, 1);


        Logger.error("ctgList.getCategory().get(" + idx + ").getId()=" + ctgList.getCategories().get(idx).getId());
        Logger.error("ctgList.getCategory().get(" + idx + ").getName()=" + ctgList.getCategories().get(idx).getName());
        return ctgList.getCategories().get(idx).getId();
    }



    public static String getNews(Context ctx, String cid) throws JSONException {
        String productData = PreferenceHelper.readProducts(ctx, cid);

        JSONObject jo = new JSONObject();
        if (productData == null || productData.equals("")) {
            String productRsp = new HttpHelper(ctx).getList(cid, 0, HttpHelper.EARLY_TIME);
            Logger.error(productRsp);

            jo.put(WidgetNewsItems.NEWS_PREFIX, new JSONArray(productRsp));
            PreferenceHelper.saveProducts(ctx, cid, productRsp);
        } else {
            jo.put(WidgetNewsItems.NEWS_PREFIX, new JSONArray(productData));
        }
        return jo.toString();
    }

    public static void getNewsRemote(final Context context, final String cid) throws JSONException {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Logger.error("Fecth remote product data " + cid);
                String productRsp = new HttpHelper(context).getList(cid, 0, HttpHelper.EARLY_TIME);
                if(productRsp!=null)
                    PreferenceHelper.saveProducts(context, cid,productRsp);
            }
        });

    }

    public interface ImageCallback {
        void OnDownloaded();
    }

    public static void downloadImageWithCallback(final Context mContext, final String fullUri, final ImageCallback cb) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                HttpHelper.download(fullUri, CacheService.getInstance(mContext).getCachePathOf("PoponewsIcon", fullUri));

                if (cb != null)
                    cb.OnDownloaded();

            }
        });
    }

    public static void downloadImage(final Context mContext, final String fullUri) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                HttpHelper.download(fullUri, CacheService.getInstance(mContext).getCachePathOf("PoponewsIcon", fullUri));
                Logger.error("download Grid" + fullUri + "!");
                NotifyListRemoteViewService(mContext);
            }
        });
    }

	public static void downloadFlag(final Context mContext, final String fullUri) {
		new Thread(){
			@Override
			public void run() {
				HttpHelper.download(fullUri, CacheService.getInstance(mContext).getCachePathOf("PoponewsFlag", fullUri));
				WidgetEditionsActivity.handler.sendEmptyMessage(MessageTopics.SYSTEM_MSG_EDITION_IMG_READY);
			}
		}.start();
	}
    public static void NotifyListRemoteViewService(Context mContext) {
        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(mContext);
        int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                mContext, PoponewsProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appIds, R.id.lv_apps);
    }



    public static void getNewsInExecutor(final Context context) {
        new Thread() {
            public void run() {
				WidgetAdHelper.clearAd();
                String ctgRsp = new HttpHelper(context).getCategory();
                JSONObject ctgjo = new JSONObject();
                try {
                    ctgjo.put(WidgetNewsCategory.CATEOGRY_PREFIX,
                            new JSONObject(ctgRsp).getJSONArray(HttpHelper.TAG_CATEGORIES));

                    //TODO
                    WidgetNewsCategory ctgList = GsonImpl.get().toObject(ctgjo.toString(), WidgetNewsCategory.class);

                    for (int i = 0; i < ctgList.getCategories().size(); i++) {
                        final WidgetNewsCategory.CategoriesEntity ctg = ctgList.getCategories().get(i);
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                String rsp = new HttpHelper(context).getList(ctg.getId(),0,HttpHelper.EARLY_TIME);
                                //Logger.error("Get News ("+ctg.getName()+") :"  +rsp);
                                PreferenceHelper.saveProducts(context, ctg.getId(), rsp);
                            }
                        });
						WidgetAdHelper.getInstance(context).initAd(ctg.getId());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();


	}


	private static EditionList el = null;


	public static void remoteGetEditionList(final Context ctx, final boolean needUpdate) {
		final String orgEdition = PreferenceHelper.readEdition(ctx);
		if (orgEdition != null &&  !orgEdition.equals("")) {
			el = GsonImpl.get().toObject(orgEdition, EditionList.class);
			if(WidgetEditionsActivity.handler != null && needUpdate)
                WidgetEditionsActivity.handler.sendEmptyMessage(MessageTopics.SYSTEM_MSG_EDITION_READY);
		}

		new Thread() {
			public void run() {
				String rsp = new HttpHelper(ctx).getEdition();

				if (rsp == null || rsp.equals("")) {
					if(needUpdate) {
						if (WidgetEditionsActivity.handler != null)
                            WidgetEditionsActivity.handler.sendEmptyMessage(MessageTopics.SYSTEM_MSG_EDITION_FAIL);
					}
				} else {
					if(!rsp.equals(orgEdition)) {
						PreferenceHelper.saveEdition(ctx, rsp);
						el = GsonImpl.get().toObject(rsp, EditionList.class);
						if (WidgetEditionsActivity.handler != null && needUpdate)
                            WidgetEditionsActivity.handler.sendEmptyMessage(MessageTopics.SYSTEM_MSG_EDITION_READY);
					}
				}
			}
		}.start();
	}

	public static EditionList getEditionList(final Context ctx) {
		return el;
	}

	public static String getEditionBaseUrl(final Context ctx) {
			return (el == null)? null : el.getBaseUrl();
	}



}
