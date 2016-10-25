package com.gmobi.poponews.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.article.ArticleActivity;
import com.gmobi.poponews.cases.browser.BrowserActivity;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IMessageService;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

public class UiHelper {
	
	public final static int FROM_LAUCHER = 0;//main
	public final static int FROM_PUSH = 1;//notification
	public final static int FROM_WIDGET = 2;//widget




	private static boolean fetchedData = false;

	public static boolean isFetchedData() {
		return fetchedData;
	}

	public static void setFetchedData(boolean f) {
		UiHelper.fetchedData = f;
	}



	private static boolean favEditMode = false;

	public static boolean isFavEditMode() {
		return favEditMode;
	}

	public static void setFavEditMode(boolean favEditMode) {
		UiHelper.favEditMode = favEditMode;
	}
	
	
	
	/*正常的离线下载*/
	public static final int NOT_OFFLINE_DOWNLOAD  = 0;
	public static final int OFFLINE_DOWNLOADING  = 1;
	public static final int OFFLINE_ALARM_DOWNLOADING  = 2;


	private static int offlineDownloadMode = NOT_OFFLINE_DOWNLOAD;

	public static int isOfflineDownloadMode() {
		return offlineDownloadMode;
	}

	public static void setOfflineDownloadMode(int offlineDownloadMode) {
		UiHelper.offlineDownloadMode = offlineDownloadMode;
	}
	


	/*定时的离线下载，当正常开始时，需要停止定时的离线下载*/
	/*
	private static boolean offlineAlarmDownloadMode = false;

	public static boolean isOfflineAlarmDownloadMode() {
		return offlineAlarmDownloadMode;
	}

	public static void setOfflineAlarmDownloadMode(boolean offlineAlarmDownloadMode) {
		UiHelper.offlineAlarmDownloadMode = offlineAlarmDownloadMode;
	}
	*/
	
	
	/*
	private static HashMap<String, Boolean> offlineCheckMap =  new HashMap<String, Boolean>(); //用于保存本次用户选择的offline

	
	public static HashMap<String, Boolean> getOfflineChecks()
	{
		return offlineCheckMap;
	}
	
	public static boolean containCategoryCheck(String cid)
	{
		return offlineCheckMap.containsKey(cid);
	}
	
	public static boolean isOfflineCheck(String cid)
	{
		return offlineCheckMap.get(cid);
	}
	
	public static void setOfflineCheck(String cid,boolean checked)
	{
		offlineCheckMap.put(cid, checked);
	}
	
	public static void clearOfflineChecks()
	{
		offlineCheckMap.clear();
	}
	*/
	
	
	private static IMessageHandler  pushContentHandler = null;
	public static void addGlobalMessageHandler()
	{
		final IMessageService ms = App.get().getService(IMessageService.class);
		/*if(pushContentHandler != null)
		{
			pushContentHandler  = new IMessageHandler() {

				@Override
				public void process(Object sender, Message msg) {
					HashMap<String, String> map = (HashMap<String, String>) msg.getData();
					new NotificationHelper(App.get().getCurrentContext()).sendNotification(map.get("nid"), map.get("title"));

				}
			};
		}*/
		//ms.removeHandler(MessageTopics.PUSH_CONTENT_READY,pushContentHandler);
		ms.addHandler(
				MessageTopics.PUSH_CONTENT_READY, new IMessageHandler() {

					@Override
					public void process(Object sender, Message msg) {
						Logger.error("PUSH_CONTENT_READY recv");
						HashMap<String, String> map = (HashMap<String, String>) msg.getData();
						new NotificationHelper(App.get().getCurrentContext()).sendNotification(map.get("nid"), map.get("title"));

			}
		});
	}
	
	private static boolean mainIsAlive = false;
	public static boolean isMainAlive() {
		return mainIsAlive;
	}

	public static void setMainAlive(boolean alive) {
		UiHelper.mainIsAlive = alive;
	}

	/**
	 * 设置一体化状态栏
	 * @param activity
	 * @param statusBar
	 * @param color
	 */
	public static void setStatusBarColor(Activity activity, View statusBar,int color){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window w = activity.getWindow();
			w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//status bar height
			int actionBarHeight = getActionBarHeight(activity);
			int statusBarHeight = getStatusBarHeight(activity);
			//action bar height
			statusBar.getLayoutParams().height = statusBarHeight;
			statusBar.getLayoutParams().width = SystemHelper.getScreenWidth(activity);
			statusBar.setBackgroundColor(color);
		}
		else
		{
			statusBar.setVisibility(View.GONE);
		}
	}
	public static int getActionBarHeight(Activity activity) {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,activity.getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}

	public static int getStatusBarHeight(Activity activity) {
		int result = 0;
		int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = activity.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}


	private static boolean socialFetching = false;

	public static boolean isSocialFetching() {
		return socialFetching;
	}

	public static void setSocialFetching(boolean socialFetching) {
		UiHelper.socialFetching = socialFetching;
	}


	private static final int FACEBOOKAD_TIMEOUT =120000; //Facebook 广告获取超时时间
	private static final int TOP_CLEAR_TIMEOUT =5000; //置顶按钮自动消失时间


	private static Timer socialFetchTimer = null;
	private static  TimerTask mTimerTask = null;
	public static void startFetchTimer()
	{

		if(socialFetchTimer == null)
		{
			socialFetchTimer = new Timer();
		}

		if (mTimerTask != null){
			mTimerTask.cancel();  //将原任务从队列中移除
		}


		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				App.get().getService(IMessageService.class).send(this,MessageTopics.CLEAR_REFRESH_UI);
			}
		};

		socialFetchTimer.schedule(mTimerTask, 30000);
	}

	public static void stopFetchTimer()
	{
		if (mTimerTask != null){
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}



	private static Timer btnTopControlTimer = null;
	private static  TimerTask mBtnTopTask = null;
	public static void startBtnTopTimer()
	{

		if(btnTopControlTimer == null)
		{
			btnTopControlTimer = new Timer();
		}

		if (mBtnTopTask != null){
			mBtnTopTask.cancel();
		}


		mBtnTopTask = new TimerTask() {
			@Override
			public void run() {
				App.get().getService(IMessageService.class).send(this,MessageTopics.CLEAR_TOP_BTN);
			}
		};

		btnTopControlTimer.schedule(mBtnTopTask, TOP_CLEAR_TIMEOUT);
	}

	public static void stopBtnTopTimer()
	{
		if (mBtnTopTask != null){
			mBtnTopTask.cancel();
			mBtnTopTask = null;
		}
	}



	private static Timer closeAdTimer = null;
	private static  TimerTask mCloseAdTask = null;
	public static void startCloseAdTimer(int time)
	{

		if(closeAdTimer == null)
		{
			closeAdTimer = new Timer();
		}

		if (mCloseAdTask != null){
			mCloseAdTask.cancel();
		}


		mCloseAdTask = new TimerTask() {
			@Override
			public void run() {
				App.get().getService(IMessageService.class).send(this,MessageTopics.INTERSTITIAL_AD_CLOSE);
			}
		};

		closeAdTimer.schedule(mCloseAdTask, time*1000);
	}

	public static void stopCloseAdTimer()
	{
		if (mCloseAdTask != null){
			mCloseAdTask.cancel();
			mCloseAdTask = null;
		}
	}



	private static Timer btnFbAdTimer = null;
	private static  TimerTask mBtnFbAdTask = null;

	public static void startFbAdTimer()
	{

		if(btnFbAdTimer == null)
		{
			btnFbAdTimer = new Timer();
		}

		if (mBtnFbAdTask != null){
			mBtnFbAdTask.cancel();
		}


		mBtnFbAdTask = new TimerTask() {
			@Override
			public void run() {
				AdHelper.getInstance(App.get()).setFacebookAdInvalid();
			}
		};

		btnFbAdTimer.schedule(mBtnFbAdTask, FACEBOOKAD_TIMEOUT);
	}

	public static void stopFbAdTimer()
	{
		if (mBtnFbAdTask != null){
			mBtnFbAdTask.cancel();
			mBtnFbAdTask = null;
		}

	}



	public static void stopAllTimer()
	{
		stopBtnTopTimer();
		stopCloseAdTimer();
		stopFetchTimer();
		stopFbAdTimer();
	}



	public static boolean needShow(int per)
	{
		int ran = (int)(Math.random()*100);
		Logger.info("[AD] percent calc = " + ran + ",percent=" + per);
		return (ran <= per);
	}


	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isConnectedOrConnecting();
			}
		}
		return false;
	}


	public static  boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isConnectedOrConnecting();
			}
		}
		return false;
	}

	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isConnectedOrConnecting();
			}
		}
		return false;
	}

	public static void openBrowserActivity(Context ctx,String id,String type,String source,String title,String pdomain,String sns)
	{
		Intent intent = new Intent(ctx,BrowserActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("type", type);
		intent.putExtra("url", source);
		intent.putExtra("title",title);
		intent.putExtra("domain", pdomain);
		intent.putExtra("sns", sns);
		ctx.startActivity(intent);
	}

	public static void openArticleFromApp(Context ctx,String nid)
	{
		if(ctx==null)
			ctx = App.get();

		Intent in = new Intent(ctx, ArticleActivity.class);
		in.putExtra("nid", nid);
		in.putExtra("from", UiHelper.FROM_LAUCHER);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(in);
	}
	public static void openArticleFromApp(Context ctx,String nid, int count)
	{
		if(ctx==null)
			ctx = App.get();

		Intent in = new Intent(ctx, ArticleActivity.class);
		in.putExtra("nid", nid);
		in.putExtra("commentCount",count);
		in.putExtra("from", UiHelper.FROM_LAUCHER);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(in);
	}




	public  static boolean isCherryVersion()
	{
		return (BuildConfig.DISTRIBUTION_CHANNEL.equals("cherry"));
	}

}
