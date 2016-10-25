package com.gmobi.poponews.provider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.activity.WidgetEditionsActivity;
import com.gmobi.poponews.cases.article.ArticleActivity;
import com.gmobi.poponews.service.RefreshListService;
import com.gmobi.poponews.service.RemoteListviewService;
import com.gmobi.poponews.util.PreferenceHelper;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.util.WidgetAdHelper;
import com.gmobi.poponews.util.WidgetDataHelper;
import com.momock.app.App;
import com.momock.util.Logger;
import com.reach.IAdItem;

import org.json.JSONException;


public class PoponewsProvider extends AppWidgetProvider {


	private RemoteViews mRv;
	private static String imei = "";
	private static final String Tag = "[PoponewsWidget]";


	@Override
	public IBinder peekService(Context myContext, Intent service) {
		System.out.println("peekService in provider");
		return super.peekService(myContext, service);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		RefreshListService.cancelAlarm(context);
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		context.startService(new Intent(context, RefreshListService.class));
		WidgetDataHelper.getNewsInExecutor(context);
		WidgetDataHelper.remoteGetEditionList(context, false);
	}

	@SuppressLint("NewApi")
	@Override
	public void onAppWidgetOptionsChanged(Context context,
										  AppWidgetManager appWidgetManager, int appWidgetId,
										  Bundle newOptions) {

		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);

		Log.e(Tag, "onAppWidgetOptionsChanged");
	}

	@Override
	public void onReceive(Context context, Intent intent) {


		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		ComponentName cmpName = new ComponentName(context, PoponewsProvider.class);
		String action = intent.getAction();
		Logger.error("Recv----" + action);

		if (mRv == null) {
			mRv = new RemoteViews(context.getPackageName(),
					R.layout.main_widget);
		}


		if (IntentNames.DATE_CHANGED.equals(action) || IntentNames.TIME_CHANGED.equals(action) || IntentNames.TIME_TICK.equals(action)) {

			Logger.error("=====Recv TIME_TICK=========");
			//updateDate(context, mRv);

		}


		if (action.equals(IntentNames.ITEM)) {

			Bundle b = intent.getExtras();

			String id = b.getString("nid");
			String pname = b.getString("name");
			String cid = b.getString("cid");
			String pdomain = b.getString("domain");
			String title = b.getString("title");
			String body = b.getString("body");
			String source = b.getString("source");
			String type = b.getString("type");
			long releasetime = b.getLong("releasetime", 0);
			String mms = b.getString("mms");


//
//            String url = PopoApplication.SHARE_TMEPLATE.replace("{nid}",id);
//
//            Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(in);
			if (cid.equals("")) {
				Intent in = new Intent(App.get(), ArticleActivity.class);
				in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				Bundle data = new Bundle();
				data.putString(IntentNames.INTENT_EXTRA_NID, id);
				data.putString(IntentNames.INTENT_EXTRA_PNAME, pname);
				data.putString(IntentNames.INTENT_EXTRA_DOMAIN, pdomain);
				data.putString(IntentNames.INTENT_EXTRA_TITLE, title);
				data.putString(IntentNames.INTENT_EXTRA_BODY, body);
				data.putString(IntentNames.INTENT_EXTRA_SOURCE, source);
				data.putString(IntentNames.INTENT_EXTRA_TYPE, type);
				data.putString(IntentNames.INTENT_EXTRA_MMS, mms);
				data.putLong(IntentNames.INTENT_EXTRA_RELEASETIME, releasetime);
				data.putInt("from", UiHelper.FROM_WIDGET);


				in.putExtras(data);

				context.startActivity(in);
			} else {
				IAdItem ad = (IAdItem) WidgetAdHelper.getInstance(context).getAds(cid).get(0).getAdObj();
				ad.execute("go", null);
				ad.execute("report", new Object[]{2});
			}






		}else if (action.equals(IntentNames.REFRESH_ICON)) {
			System.out.println("REFRESH_ICON begin");
			int[] appWidgetIds = mgr.getAppWidgetIds(cmpName);
			mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_apps);
			System.out.println("REFRESH_ICON end");

		} else if (intent.getAction().equals(IntentNames.REFRESH)) {
			System.out.println("refresh button begin");
			int[] appWidgetIds = mgr.getAppWidgetIds(cmpName);
			mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_apps);
			System.out.println("refresh button end");
		} else if (intent.getAction().equals(IntentNames.FRESH_CLICK_ACTION)) {
			System.out.println("FRESH_CLICK_ACTION button begin");

			RefreshListService.cancelAlarm(context);
			refreshNews(context);
			restartRefreshAlarm(context);

			System.out.println("FRESH_CLICK_ACTION button end");
		} else if (intent.getAction().equals(IntentNames.PREV_CATEGORY)) {
			System.out.println("PREV_CATEGORY button begin");


			PreferenceHelper.saveFetchDirect(context, -1);
			RefreshListService.cancelAlarm(context);
			int idx = PreferenceHelper.readCurCategoryIdx(context);
			idx = idx - 1;
			PreferenceHelper.saveCurCategoryIdx(context, idx);


			refreshNews(context);

			updateTitle(context, mRv);
			restartRefreshAlarm(context);

			System.out.println("PREV_CATEGORY button end");
		} else if (intent.getAction().equals(IntentNames.NEXT_CATEGORY)) {
			System.out.println("NEXT_CATEGORY button begin");

			PreferenceHelper.saveFetchDirect(context, 1);
			RefreshListService.cancelAlarm(context);
			int idx = PreferenceHelper.readCurCategoryIdx(context);
			idx = idx + 1;
			PreferenceHelper.saveCurCategoryIdx(context, idx);

			refreshNews(context);
			updateTitle(context, mRv);
			restartRefreshAlarm(context);


			System.out.println("NEXT_CATEGORY button end");
		} else if (intent.getAction().equals(IntentNames.UPDATE_TITLE)) {
			System.out.println("UPDATE_TITLE button begin");
			updateTitle(context, mRv);
			System.out.println("UPDATE_TITLE button end");
		} else if (action.equals(IntentNames.SET_EDITION)) {
			Intent in = new Intent(context, WidgetEditionsActivity.class);
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(in);
		}


		int appWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		updateButton(appWidgetId, context, mRv);

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(
				context, PoponewsProvider.class));

		appWidgetManager.updateAppWidget(appIds, mRv);


		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = mTelephonyMgr.getDeviceId();

		super.onReceive(context, intent);

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
						 int[] appWidgetIds) {

		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int i = 0; i < appWidgetIds.length; i++) {

			updateButton(appWidgetIds[i], context, mRv);


			Intent intent = new Intent(context, RemoteListviewService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			// When intents are compared, the extras are ignored, so we need to embed the extras
			// into the data so that the extras will not be ignored.
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.main_widget);
			rv.setRemoteAdapter(R.id.lv_apps, intent);
			rv.setEmptyView(R.id.lv_apps, R.id.rl_loading);

			Intent refreshIntent = new Intent(context, PoponewsProvider.class);
			refreshIntent.setAction(IntentNames.REFRESH);
			PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0,
					refreshIntent, 0);
			rv.setOnClickPendingIntent(R.id.btn_fresh, refreshPendingIntent);

			Intent toastIntent = new Intent(context, PoponewsProvider.class);
			toastIntent.setAction(IntentNames.ITEM);
			toastIntent.putExtra("page", 0); // main page
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setPendingIntentTemplate(R.id.lv_apps, toastPendingIntent);

			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);


		}

	}

	public void updateTitle(Context context, RemoteViews rv) {
		rv.setTextViewText(R.id.tv_title, PreferenceHelper.readCurCategoryName(context));
	}

	public void updateButton(int appWidgetId, Context context, RemoteViews rv) {
		{
			Intent in = new Intent();
			in.setAction(IntentNames.FRESH_CLICK_ACTION);
			in.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
					in, 0);
			rv.setOnClickPendingIntent(R.id.btn_fresh, pendingIntent);
		}

		{
			Intent in_prev = new Intent();
			in_prev.setAction(IntentNames.PREV_CATEGORY);
			in_prev.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0,
					in_prev, 0);
			rv.setOnClickPendingIntent(R.id.btn_prev, pendingIntent2);
		}

		{
			Intent in_next = new Intent();
			in_next.setAction(IntentNames.NEXT_CATEGORY);
			in_next.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 0,
					in_next, 0);
			rv.setOnClickPendingIntent(R.id.btn_next, pendingIntent3);
		}

		{
			Intent in_edition = new Intent();
			in_edition.setAction(IntentNames.SET_EDITION);
			in_edition.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingIntent4 = PendingIntent.getBroadcast(context, 0,
					in_edition, 0);
			rv.setOnClickPendingIntent(R.id.btn_edition, pendingIntent4);
		}


	}

	public static String getImei() {
		return imei;
	}


	public void refreshNews(Context ctx) {
		String cid = PreferenceHelper.readCurCategoryId(ctx);
		if (!cid.equals("")) {
			try {
				WidgetDataHelper.getNewsRemote(ctx, cid);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(ctx);
		int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(
				ctx, PoponewsProvider.class));
		appWidgetManager.notifyAppWidgetViewDataChanged(appIds, R.id.lv_apps);
	}

	public void restartRefreshAlarm(Context ctx) {
		Logger.debug("startAlarmFlash start");
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ctx, RefreshListService.class);
		intent.setAction(IntentNames.LIST_REFRESH);
		PendingIntent pi = PendingIntent.getService(ctx, 1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pi);
		Logger.debug("startAlarmFlash end");
	}

}
