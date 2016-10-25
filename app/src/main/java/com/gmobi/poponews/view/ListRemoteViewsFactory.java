package com.gmobi.poponews.view;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;


import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.model.WidgetNewsItems;
import com.gmobi.poponews.service.CacheService;
import com.gmobi.poponews.util.WidgetAdHelper;
import com.gmobi.poponews.util.WidgetDataHelper;
import com.gmobi.poponews.util.GsonImpl;
import com.gmobi.poponews.util.HttpHelper;
import com.gmobi.poponews.util.ImageHelper;
import com.gmobi.poponews.util.MemoryHelper;
import com.gmobi.poponews.util.PreferenceHelper;

import com.momock.util.Convert;
import com.momock.util.Logger;
import com.reach.IAdItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 10/9 0009.
 */
public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static int mCount = 0;
    private List<WidgetNewsItems.NewsEntity> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;



    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        System.out.println("onCreate  in factory");
    }

    @Override
    public int getCount() {
        return mWidgetItems.size();
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public RemoteViews getLoadingView() {

        return null;
    }



    @Override
    public RemoteViews getViewAt(int position) {
        System.out.println("getViewAt");

        if (mWidgetItems == null || mWidgetItems.size() == 0) {
            Logger.error("mWidgetItems is empty!!!");
            return null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

		WidgetNewsItems.NewsEntity item = mWidgetItems.get(position);
		if (item.getType().equals(WidgetNewsItems.NEWS_TYPE_AD)) {
			IAdItem ad = (IAdItem) item.getAdObj();

			Object img = ad.execute("get", new Object[]{IAdItem.IMAGE});

			if (img instanceof JSONObject) {
				JSONObject jo = (JSONObject) img;
				Bitmap preview = null;
				try {
					preview = getBitmap(position, jo.getString("url"), DOWNLOAD_PREVIEW);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (preview != null) {
					if (mWidgetItems == null || mWidgetItems.size() == 0 || position > (mWidgetItems.size() - 1)) {
						Logger.error("mWidgetItems is empty!!!");
						return null;
					}
					PreferenceHelper.saveNewsPreviewStatus(mContext, mWidgetItems.get(position).get_id(), WidgetNewsItems.DOWNLOADED);
					rv.setImageViewBitmap(R.id.news_img, preview);
				}
			}

			Object t = ad.execute("get", new Object[]{IAdItem.TITLE});
			rv.setTextViewText(R.id.news_title, (String) t);
			rv.setViewVisibility(R.id.news_source, View.GONE);
			rv.setViewVisibility(R.id.news_ad_icon, View.VISIBLE);

			ad.execute("report", new Object[]{1});

			Bundle extras = new Bundle();
			extras.putString("cid", item.get_cid());
			extras.putInt("pos", position);
			extras.putString("nid", mWidgetItems.get(position).get_id());
			extras.putString("name", mWidgetItems.get(position).getTitle());

			Intent fillInIntent = new Intent(IntentNames.ITEM);
			fillInIntent.putExtras(extras);
			rv.setOnClickFillInIntent(R.id.ll_list_item, fillInIntent);


		} else {
			rv.setTextViewText(R.id.news_title, mWidgetItems.get(position).getTitle());
			rv.setTextViewText(R.id.news_source, mWidgetItems.get(position).getP_name());
			rv.setViewVisibility(R.id.news_ad_icon, View.GONE);
			Logger.error(position + " preview=" + mWidgetItems.get(position).getPreview());
			String previewUrl = HttpHelper.getImageBaseUrl() + mWidgetItems.get(position).getPreview() + ".600x360";
			Bitmap preview = getBitmap(position, previewUrl, DOWNLOAD_PREVIEW);
			if (preview != null) {
				if (mWidgetItems == null || mWidgetItems.size() == 0 || position > (mWidgetItems.size() - 1)) {
					Logger.error("mWidgetItems is empty!!!");
					return null;
				}
				PreferenceHelper.saveNewsPreviewStatus(mContext, mWidgetItems.get(position).get_id(), WidgetNewsItems.DOWNLOADED);
				rv.setImageViewBitmap(R.id.news_img, preview);
			}


			Bundle extras = new Bundle();
			extras.putString("cid", "");
			extras.putInt("pos", position);
			extras.putString("nid", mWidgetItems.get(position).get_id());
			extras.putString("name", mWidgetItems.get(position).getTitle());

			Intent fillInIntent = new Intent(IntentNames.ITEM);
			fillInIntent.putExtras(extras);
			rv.setOnClickFillInIntent(R.id.ll_list_item, fillInIntent);
		}
		return rv;
	}


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public void onDataSetChanged() {
        //this func is get data
        mWidgetItems.clear();


        System.out.println("onDataSetChanged List==============================================");

        try {

            String ctgId = WidgetDataHelper.getCategory(mContext);
            String products = WidgetDataHelper.getNews(mContext, ctgId);


            Intent n =new Intent(IntentNames.UPDATE_TITLE);
            mContext.sendBroadcast(n);
            WidgetNewsItems appList = GsonImpl.get().toObject(products, WidgetNewsItems.class);
            for (int i = 0, len = appList.getNews().size(); i < len; i++) {
                PreferenceHelper.saveNewsPreviewStatus(mContext, appList.getNews().get(i).get_id(), WidgetNewsItems.NOT_DOWNLOAD);
                PreferenceHelper.saveNewsIconStatus(mContext, appList.getNews().get(i).get_id(), WidgetNewsItems.NOT_DOWNLOAD);

                if (appList.getNews().get(i).getPreview() != null)
                    mWidgetItems.add(appList.getNews().get(i));
            }
			if (WidgetAdHelper.getInstance(mContext).isAdReady(ctgId)) {
				ArrayList<WidgetNewsItems.NewsEntity> adList = WidgetAdHelper.getInstance(mContext).getAds(ctgId);
				int count = adList.size();
				if(count > 0 &&  count <= 10 )
				{
					int interval = 10 / count;
					int curPos = interval;
					//Logger.debug("[AD]Add ad count = "+count);
					for (int i = 0; i < count; i++) {
						mWidgetItems.add(curPos, adList.get(i));
						//Logger.debug("[AD]Add ad pos at" + curPos);
						curPos += interval + 1;
					}
				}


			}

            mCount = mWidgetItems.size();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }


        System.out.println("onDataSetChanged end");


    }

    @Override
    public void onDestroy() {

        mWidgetItems.clear();
    }


    private static int DOWNLOAD_PREVIEW = 1;
    private static int DOWNLOAD_ICON = 2;

    private Bitmap getBitmap(final int idx, final String fullUri, final int src) {
        Bitmap bitmap = null;
        final int expectedWidth;
        final int expectedHeight;
        if (fullUri == null) return null;
        String uri = fullUri;
        int pos = fullUri.lastIndexOf('#');
        if (pos > 0) {
            int pos2 = fullUri.lastIndexOf('x');
            Logger.check(pos2 > pos, "The image uri is not correct!");
            expectedWidth = Convert.toInteger(fullUri.substring(pos + 1, pos2));
            expectedHeight = Convert.toInteger(fullUri.substring(pos2 + 1));
            uri = fullUri.substring(0, pos);
        } else {
            expectedWidth = 0;
            expectedHeight = 0;
        }

        CacheService cs = CacheService.getInstance(mContext);

        bitmap = MemoryHelper.getBitmapFromMem(cs.getFilenameOf(fullUri));
        if(bitmap != null)
            return bitmap;

        File bmpFile = cs.getCacheOf("PoponewsIcon", fullUri);
        if (bmpFile.exists()) {
            bitmap = ImageHelper.fromFile(bmpFile, expectedWidth, expectedHeight);
        }
        if (bitmap == null) {
            if (src == DOWNLOAD_ICON) {
                int status = PreferenceHelper.readNewsIconStatus(mContext, mWidgetItems.get(idx).get_id());
                if (status > WidgetNewsItems.NOT_DOWNLOAD)
                    return null;
                PreferenceHelper.saveNewsIconStatus(mContext, mWidgetItems.get(idx).get_id(), WidgetNewsItems.DOWNLOADING);
                WidgetDataHelper.downloadImage(mContext, fullUri);

            } else if (src == DOWNLOAD_PREVIEW) {
                int status = PreferenceHelper.readNewsPreviewStatus(mContext, mWidgetItems.get(idx).get_id());
                if (status > WidgetNewsItems.NOT_DOWNLOAD)
                    return null;

                PreferenceHelper.saveNewsPreviewStatus(mContext, mWidgetItems.get(idx).get_id(), WidgetNewsItems.DOWNLOADING);
                WidgetDataHelper.downloadImage(mContext, fullUri);

            }
        }
        else
            MemoryHelper.addBitmap(cs.getFilenameOf(fullUri), bitmap);


        return bitmap;
    }


}
