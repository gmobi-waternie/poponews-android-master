package com.gmobi.poponews.app;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.cases.accountsetting.AccountSettingCase;
import com.gmobi.poponews.cases.article.ArticleCase;
import com.gmobi.poponews.cases.browser.BrowserCase;
import com.gmobi.poponews.cases.categorysetting.CategorySettingCase;
import com.gmobi.poponews.cases.comment.CommentCase;
import com.gmobi.poponews.cases.facebook.FacebookCase;
import com.gmobi.poponews.cases.favorite.FavoriteCase;
import com.gmobi.poponews.cases.guide.GuideEditonCase;
import com.gmobi.poponews.cases.login.LoginCase;
import com.gmobi.poponews.cases.main.MainCase;
import com.gmobi.poponews.cases.mycomment.MyCommentCase;
import com.gmobi.poponews.cases.newsfeed.FeedCase;
import com.gmobi.poponews.cases.offline.OfflineCase;
import com.gmobi.poponews.cases.read.ReadCase;
import com.gmobi.poponews.cases.register.RegisterCase;
import com.gmobi.poponews.cases.setting.EditionCase;
import com.gmobi.poponews.cases.socialsetting.SocialSettingCase;
import com.gmobi.poponews.provider.CommentProvider;
import com.gmobi.poponews.provider.IDataProvider;
import com.gmobi.poponews.service.BaiduService;
import com.gmobi.poponews.service.CherryUserService;
import com.gmobi.poponews.service.CommentService;
import com.gmobi.poponews.service.ConfigService;
import com.gmobi.poponews.service.FacebookService;
import com.gmobi.poponews.service.GoogleService;
import com.gmobi.poponews.service.IBaiduService;
import com.gmobi.poponews.service.ICommentService;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.IGoogleService;
import com.gmobi.poponews.service.INewsCacheService;
import com.gmobi.poponews.service.IOfflineService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.service.IShareService;
import com.gmobi.poponews.service.ISocialDataService;
import com.gmobi.poponews.service.ITwitterService;
import com.gmobi.poponews.service.IUpdateService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.service.IWeiboService;
import com.gmobi.poponews.service.MyImageService;
import com.gmobi.poponews.service.NewsCacheService;
import com.gmobi.poponews.service.NewsDataService;
import com.gmobi.poponews.service.OfflineService;
import com.gmobi.poponews.service.RemoteService;
import com.gmobi.poponews.service.ReportService;
import com.gmobi.poponews.service.ShareService;
import com.gmobi.poponews.service.SocialDataService;
import com.gmobi.poponews.service.TwitterService;
import com.gmobi.poponews.service.UpdateService;
import com.gmobi.poponews.service.MeService;
import com.gmobi.poponews.service.WeiboService;
import com.gmobi.poponews.util.AssetsHelper;
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.UiHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.momock.app.App;
import com.momock.data.Settings;
import com.momock.service.CacheService;
import com.momock.service.HttpService;
import com.momock.service.ICacheService;
import com.momock.service.IHttpService;
import com.momock.service.IImageService;
import com.momock.service.IJsonService;
import com.momock.service.ISystemService;
import com.momock.service.JsonService;
import com.momock.service.SystemService;
import com.momock.util.FileHelper;
import com.momock.util.Logger;
//import com.similarweb.portraitlib.GetPortraitListener;
//import com.similarweb.portraitlib.Portrait;
//import com.similarweb.portraitlib.SWPortrait;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.tencent.tauth.Tencent;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.io.File;
import java.io.IOException;

import io.fabric.sdk.android.Fabric;

public class PopoApplication extends App {
	private static Settings appSetting = null;
	public static Tencent mTencent;

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY = "Y2gJAm4RwEid7VcKCtze5iSvx";
	private static final String TWITTER_SECRET = "GUHJGCcOnf9iqDlx63JRl3dqmVqEmfpNYD6NdIbRXIh6n9PgTl";
	public static final String QQ_APPID = "1104962133";
	public static AuthInfo mAuthInfo;

	public static String FLURRY_APIKEY = BuildConfig.FLURRY_APIKEY;
	public static String APPFLYER_DEVKEY = BuildConfig.APPFLYER_DEVKEY;
	public static AppEventsLogger faLogger = null;

	/**
	 * 微博分享的接口实例
	 */
	public static IWeiboShareAPI mWeiboShareAPI;



	@Override
	public void onCreate() {
		super.onCreate();
		doPrepare();

	}

	public static Settings getSetting() {
		return appSetting;
	}

	protected void onAddCases() {

		addCase(new MainCase(CaseNames.MAIN));
		addCase(new EditionCase(CaseNames.SETTING_EDITION));

		addCase(new FavoriteCase(CaseNames.USER_FAVORITE));
		addCase(new FacebookCase(CaseNames.USER_FACEBOOK));
		addCase(new ReadCase(CaseNames.USER_READ));
		addCase(new FeedCase(CaseNames.USER_FEED));
		addCase(new OfflineCase(CaseNames.USER_OFFLINE));
		addCase(new BrowserCase(CaseNames.USER_BROWSER));
		addCase(new ArticleCase(CaseNames.ARTICLE));
		addCase(new SocialSettingCase(CaseNames.USER_SOCIAL_SETTING));
		addCase(new GuideEditonCase(CaseNames.GUIDE_EDITION));
		addCase(new CategorySettingCase(CaseNames.CATEGORY_SETTING));
		addCase(new CommentCase(CaseNames.COMMENT));
		addCase(new LoginCase(CaseNames.LOGIN));
		addCase(new RegisterCase(CaseNames.REGISTER));
		addCase(new MyCommentCase(CaseNames.MY_COMMENT));
		addCase(new AccountSettingCase(CaseNames.ACCOUNT_SETTING));
	}


	@Override
	public void onCreateLog(LogConfig config) {
	}


	@Override
	protected void onAddServices() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		Logger.error("MemoryClass = " + activityManager.getMemoryClass());

		long cacheSize;
		if (activityManager.getMemoryClass() <= 64) {
			Logger.error("Use small cache pool.");
			cacheSize = 1024L * 1024 * 8;
		} else {
			Logger.error("Use big cache pool.");
			cacheSize = 1024L * 1024 * 16;
		}

		addService(IHttpService.class, new HttpService());

		addService(ICacheService.class, new CacheService(this));
		addService(IImageService.class, new MyImageService(cacheSize));
		addService(ISystemService.class, new SystemService());
		addService(IDataService.class, new NewsDataService());

		addService(IJsonService.class, new JsonService());
		addService(IConfigService.class, new ConfigService());
		addService(IRemoteService.class, new RemoteService());

		addService(IUpdateService.class, new UpdateService());
		addService(INewsCacheService.class, new NewsCacheService());
		addService(IReportService.class, new ReportService());
		addService(IOfflineService.class, new OfflineService());
		addService(ISocialDataService.class, new SocialDataService());

		addService(IFacebookService.class, new FacebookService());
		addService(ITwitterService.class, new TwitterService());
		addService(IGoogleService.class, new GoogleService());
		addService(IWeiboService.class, new WeiboService());
		addService(IBaiduService.class, new BaiduService());

		addService(ICommentService.class, new CommentService());
		addService(IDataProvider.class, new CommentProvider());

		addService(IShareService.class, new ShareService());
		if(UiHelper.isCherryVersion())
			addService(IUserService.class, new CherryUserService());
		else
			addService(IUserService.class, new MeService());


	}

	@Override
	protected void onRegisterShortNames() {
		super.onRegisterShortNames();
		this.registerShortName("com.gmobi.poponews.widget",
				"CircleIndexIndicator", "PagerSlidingTabStrip", "PullToRefreshListView", "RefreshableView", "RefreshableRecycleView","TopCropImageView");
	}


	private void doPrepare() {
		String config = null;

		if (BuildConfig.DEBUG) {
			Logger.setEnabled(true);
			Logger.open(this, getResources().getString(R.string.app_name), 10, 0);
		} else {

			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File logDir = Environment.getExternalStorageDirectory();
				File f = new File(logDir, "poponews.ini");
				Log.d("Poponews", f + " : " + f.exists());
				if (f.exists()) {
					Logger.setEnabled(true);
					Logger.open(this, getResources().getString(R.string.app_name), 10, 0);
					try {
						config = FileHelper.readText(f);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}


				if (config != null) {
					String[] lines = config.split("\n");

					for (String l : lines) {
						if (l == null)
							continue;
						int pos = l.indexOf('=');
						if (pos != -1) {
							String k = l.substring(0, pos).trim();
							if (k.startsWith("//") || k.startsWith("#")) continue;
							String v = l.substring(pos + 1).trim();
							Logger.debug(k + "=" + v);
						}
					}

				}

			} else
				Logger.setEnabled(false);


		}


		if (appSetting == null)
			appSetting = new Settings(this, ConfigNames.SETTINGS_FILE_NAME);

		AssetsHelper ah = new AssetsHelper(this);
		ah.initHtmlRes();


		//ah.initStoreRes();

		DipHelper.init(this);

		/*
		TradeService.DEBUG = true;  // debug model
		
		Bundle params = new Bundle(); 
		params.putInt(TradeService.PARAM_NOTIF_RID_SMALL_ICON, R.drawable.ic_launcher);
		
		TradeService.start(this, params);	
		

		
		TradeService.setOnPushReceivedListener(new IPushCallback() {

			@Override
			public void onReceived(String arg0) {
				Log.e("Push", arg0);
				processPushData(arg0);

			}
		});

		*/

		UiHelper.addGlobalMessageHandler();
		FacebookSdk.sdkInitialize(getApplicationContext());
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Twitter(authConfig));

		mTencent = Tencent.createInstance(QQ_APPID, this);

		// configure Flurry
		FlurryAgent.setLogEnabled(true);
		FlurryAgent.setLogEvents(true);
		// init Flurry
		FlurryAgent.init(this, FLURRY_APIKEY);

		if (faLogger == null)
			faLogger = AppEventsLogger.newLogger(this);

		//getSimilarWebInfo();
		checkNetwork();

	}

	private void getSimilarWebInfo() {
//		GetPortraitListener portraitListener = new GetPortraitListener() {
//			@Override
//			public void onSuccess(Portrait response) {
//				Logger.info("[SimilarWeb]:" + response.toJSON().toString());
//				JSONObject info = response.toJSON();
//				App.get().getService(IConfigService.class).setSimilarWebUserInfo(info.toString());
//
//			}
//
//			@Override
//			public void onFail(final int errorCode, final String message) {
//				Logger.info("[SimilarWeb]:errorCode=" + errorCode + ",msg =" + message);
//			}
//		};
//		SWPortrait.getPortrait(getApplicationContext(), "a79697f1fand", portraitListener);
//		//SWPortrait.getPortrait(getApplicationContext(), "833f2e03ca4ec5a3c7c04bd018cdebdf", "d06736f41b9a825490591f24bdafecb1", portraitListener);
	}

	private void checkNetwork() {
		Logger.info("Network " + UiHelper.isNetworkConnected(App.get()));
		Logger.info("Wifi " + UiHelper.isWifiConnected(App.get()));
		Logger.info("Mobile net " + UiHelper.isMobileConnected(App.get()));
	}


	private Tracker mTracker;

	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}


}
