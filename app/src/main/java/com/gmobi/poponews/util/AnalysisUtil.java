package com.gmobi.poponews.util;

import android.app.Activity;
import android.os.Bundle;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.gmobi.poponews.app.PopoApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.momock.app.App;
import com.momock.util.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class AnalysisUtil {
	//----------------------------------------Screen---------------------------------------
	public static final String SCR_LAUNCH = "launch";
	public static final String SCR_NEWS = "news";
	public static final String SCR_ARTICLE = "article";
	public static final String SCR_DISCOVER = "discover";
	public static final String SCR_ME = "me";

	//----------------------------------------事件---------------------------------------
	//splash里面触发（channel已经确定）
	public static final String EVENT_LAUNCH = "launch";

	//切换到某个news category的时候触发
	public static final String EVENT_LIST = "news.list";

	//向下划到底，向服务器要更多内容的时候触发，记录获取到内容的时间
	public static final String EVENT_LIST_NEXT = "news.list.next";

	//向上划到底，向服务器要更多内容的时候触发，记录获取到内容的时间
	public static final String EVENT_LIST_PREV = "news.list.prev";

	public static final String EVENT_LIST_LAYOUT = "news.list.layout";

	public static final String EVENT_LIST_TOP = "news.list.top";

	public static final String EVENT_LIST_CLOSE = "news.list.close";

	public static final String EVENT_LIST_AD = "news.list.ad";


	public static final String EVENT_CATEGORY_SETTING = "news.category.setting";

	public static final String EVENT_CATEGORY_ORDER = "news.category.order";

	public static final String EVENT_CATEGORY_VISIBLE = "news.category.visible";


	//打开文章，记录时间
	public static final String EVENT_ARTICLE_READ = "article.read";

	//打开文章时显示interstitial广告
	public static final String EVENT_ARTICLE_ADSI = "article.ads.i";

	//打开文章时显示banner广告
	public static final String EVENT_ARTICLE_ADSB = "article.ads.b";


	public static final String EVENT_ARTICLE_SHARE = "article.share";

	public static final String EVENT_ARTICLE_MOOD = "article.mood";

	public static final String EVENT_ARTICLE_FAV = "article.fav";

	public static final String EVENT_ARTICLE_FONT = "article.font";

	public static final String EVENT_ARTICLE_COMMENT = "article.comment";



	//切换到discover
	public static final String EVENT_SNS = "discover";

	//点击discover上面的社交网络
	public static final String EVENT_SNS_CLICK = "discover.sns.click";

	//登录社交网络
	public static final String EVENT_SNS_LOGIN = "discover.sns.login";

	public static final String EVENT_SNS_FETCH = "discover.sns.fetch";

	public static final String EVENT_SNS_READ = "discover.article.read";

	public static final String EVENT_SNS_ADSI = "discover.article.ads.i";

	//切换到Me
	public static final String EVENT_ME = "me";
	//点击进入Me的My Favorite
	public static final String EVENT_ME_FAV = "me.fav";

	public static final String EVENT_ME_FEED = "me.feed";

	//点击进入Me的Recently Read
	public static final String EVENT_ME_RECENT = "me.recent";

	//点击进入Me的Offline Download
	public static final String EVENT_ME_OFFLINE = "me.offline";

	//点击进入Me的Offers
	public static final String EVENT_ME_OFFERS = "me.offers";

	//点击进入Me的Settings
	public static final String EVENT_ME_SETTINGS = "me.settings";

	public static final String EVENT_ME_SETTINGS_EDITION = "me.settings.edition";
	public static final String EVENT_ME_SETTINGS_AUTOPLAY = "me.settings.autoplay";
	public static final String EVENT_ME_SETTINGS_CLEAR = "me.settings.clear";
	public static final String EVENT_ME_SETTINGS_FEEDBACK = "me.settings.feedback";
	public static final String EVENT_ME_SETTINGS_FEEDBACK_SEND = "me.settings.feedback.send";
	public static final String EVENT_ME_SETTINGS_ABOUT = "me.settings.about";

	public static final String EVENT_PUSH_RECV = "push.recv";


//----------------------------------------参数---------------------------------------

	public static final String EVENT_ARG_CHANNEL = "channel";
	public static final String EVENT_ARG_DCH = "dch";
	public static final String EVENT_ARG_ID = "id";
	public static final String EVENT_ARG_NAME = "name";
	public static final String EVENT_ARG_ORDER = "order";
	public static final String EVENT_ARG_VISIBLE = "visible";
	public static final String EVENT_ARG_PROVIDER = "provider";
	public static final String EVENT_ARG_COUNT = "count";
	public static final String EVENT_ARG_TITLE = "title";
	public static final String EVENT_ARG_TYPE = "type";
	public static final String EVENT_ARG_AD_NAME = "ad_name";
	public static final String EVENT_ARG_VIA = "via";
	public static final String EVENT_ARG_MOOD = "mood";
	public static final String EVENT_ARG_FONT = "font";
	public static final String EVENT_ARG_SNS = "sns";
	public static final String EVENT_ARG_LOGIN = "login";
	public static final String EVENT_ARG_URL = "url";
	public static final String EVENT_ARG_FAV = "fav";
	public static final String EVENT_ARG_AUTOPLAY = "autoplay";

	//----------------------------常量----------------------------------------
	public static final String PROVIDER_FACEBOOOK = "facebook";
	public static final String PROVIDER_GMOBI = "gmobi";

	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_CANCEL = "cancel";
	public static final String RESULT_FAIL = "fail";
	public static final String RESULT_LOGOUT = "logout";

	public static final String FONT_LARGE = "large";
	public static final String FONT_MEDIUM = "medium";
	public static final String FONT_SMALL = "small";


	public static final String DEFAULT_GA_CATEGORY = "POPONEWS_ANDROID";

	private static Tracker mTracker;

	private static Bundle mapToBundle(Map<String, String> map) {
		Bundle b = new Bundle();
		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();


		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			b.putString(entry.getKey(), entry.getValue());
		}

		return b;
	}

	private static String mapToLabel(Map<String, String> map) {
		String s = "{";
		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();


		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			s = s + entry.getKey() + "=" + entry.getValue() + ",";
		}

		s = s.substring(0, s.length() - 1);
		s += "}";
		return s;
	}


	private static Map<String, Object> mapToMap(Map<String, String> map) {
		Map<String, Object> af = new HashMap<>();

		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();


		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			af.put(entry.getKey(), entry.getValue());
		}

		return af;
	}

	private static String makeFAEventName(String eventName) {
		return eventName.replace(".", "_");
	}



	public static void onActivityResume(Activity activity,String screenName) {
		AppEventsLogger.activateApp(activity);
		if (mTracker == null)
			mTracker = ((PopoApplication) App.get()).getDefaultTracker();
		Logger.info("[GA]:Setting screen name: " + activity.getLocalClassName());
		mTracker.setScreenName(screenName);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}


	public static void onActivityPause(Activity activity) {
		AppEventsLogger.deactivateApp(activity);
	}

	private static void checkEnvironment() {
		if (mTracker == null)
			mTracker = ((PopoApplication) App.get()).getDefaultTracker();

		if (PopoApplication.faLogger == null)
			PopoApplication.faLogger = AppEventsLogger.newLogger(App.get());
	}


	public static void recordLaunch(String channel, String dch) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();


		p.put(EVENT_ARG_CHANNEL, channel);
		p.put(EVENT_ARG_DCH, dch);

		FlurryAgent.logEvent(EVENT_LAUNCH, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_LAUNCH, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LAUNCH);
		PopoApplication.faLogger.logEvent(e, b);


		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LAUNCH)
				.setAction(channel)
				.setLabel(l)
				.build());
	}

	public static void recordNewsList(String id, String name) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();
		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);
		FlurryAgent.logEvent(EVENT_LIST, p);

		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST, mapToMap(p));


		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LIST)
				.setAction(name)
				.setLabel(l)
				.build());
	}


	public static void recordCategorySetting() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_CATEGORY_SETTING);
		AppsFlyerLib.trackEvent(App.get(), EVENT_CATEGORY_SETTING, null);

		String e = makeFAEventName(EVENT_CATEGORY_SETTING);
		PopoApplication.faLogger.logEvent(e);


		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_CATEGORY_SETTING)
				.build());
	}


	public static void recordCategoryOrder(String id, String name, String order) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();
		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);
		p.put(EVENT_ARG_ORDER, order);
		FlurryAgent.logEvent(EVENT_CATEGORY_ORDER, p);

		AppsFlyerLib.trackEvent(App.get(), EVENT_CATEGORY_ORDER, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_CATEGORY_ORDER);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_CATEGORY_ORDER)
				.setAction(name)
				.setLabel(l)
				.build());
	}

	public static void recordCategoryVisible(String id, String name, String visible) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();
		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);
		p.put(EVENT_ARG_VISIBLE, visible);
		FlurryAgent.logEvent(EVENT_CATEGORY_VISIBLE, p);


		AppsFlyerLib.trackEvent(App.get(), EVENT_CATEGORY_VISIBLE, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_CATEGORY_VISIBLE);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_CATEGORY_VISIBLE)
				.setAction(name)
				.setLabel(l)
				.build());
	}


	public static void endRecordNewsListNext(String id, String name) {

		Map<String, String> p = new HashMap<>();

		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);

		FlurryAgent.endTimedEvent(EVENT_LIST_NEXT, p);
		Logger.debug("[Flurry]:endTimedEvent(EVENT_LIST_NEXT)");
	}

	public static void recordNewsListNext(String id, String name) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();
		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);
		FlurryAgent.logEvent(EVENT_LIST_NEXT, p, true);


		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST_NEXT, mapToMap(p));


		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST_NEXT);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LIST_NEXT)
				.setAction(name)
				.setLabel(l)
				.build());
	}

	public static void endRecordNewsListPrev(String id, String name) {
		Map<String, String> p = new HashMap<>();

		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);

		FlurryAgent.endTimedEvent(EVENT_LIST_PREV, p);
		Logger.debug("[Flurry]:endTimedEvent(EVENT_LIST_PREV)");
	}

	public static void recordNewsListPrev(String id, String name) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);

		FlurryAgent.logEvent(EVENT_LIST_PREV, p, true);


		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST_PREV, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST_PREV);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LIST_PREV)
				.setAction(name)
				.setLabel(l)
				.build());
	}

	public static void recordNewsListLayout(String id, String name, String type) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);
		p.put(EVENT_ARG_TYPE, type);

		FlurryAgent.logEvent(EVENT_LIST_LAYOUT, p);

		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST_LAYOUT, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST_LAYOUT);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(DEFAULT_GA_CATEGORY)
				.setAction(EVENT_LIST_LAYOUT)
				.setLabel(l)
				.build());
	}


	public static void recordNewsClose(String id, String title) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);

		FlurryAgent.logEvent(EVENT_LIST_CLOSE, p);

		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST_CLOSE, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST_CLOSE);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LIST_CLOSE)
				.setAction(title)
				.setLabel(l)
				.build());
	}

	public static void recordNewsListTop(String id, String name) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_NAME, name);

		FlurryAgent.logEvent(EVENT_LIST_TOP, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST_TOP, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST_TOP);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LIST_TOP)
				.setAction(name)
				.setLabel(l)
				.build());
	}

	public static void endRecordNewsListAd(String id, String provider, String count) {
		Map<String, String> p = new HashMap<>();

		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_PROVIDER, provider);
		p.put(EVENT_ARG_COUNT, count);

		FlurryAgent.endTimedEvent(EVENT_LIST_AD, p);

		Logger.debug("[Flurry]:endTimedEvent(EVENT_LIST_AD)");
	}


	public static void recordNewsListAd(String id, String provider, String count) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();



		p.put(EVENT_ARG_PROVIDER, provider);
		p.put(EVENT_ARG_COUNT, count);

		FlurryAgent.logEvent(EVENT_LIST_AD, p, true);
		AppsFlyerLib.trackEvent(App.get(), EVENT_LIST_AD, mapToMap(p));


		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_LIST_AD);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_LIST_AD)
				.setAction(provider)
				.setLabel(l)
				.build());
	}


	public static void endRecordArticleRead(String id, String title, String type) {
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);

		FlurryAgent.endTimedEvent(EVENT_ARTICLE_READ, p);
		Logger.debug("[Flurry]:endTimedEvent(EVENT_ARTICLE_READ)");
	}


	public static void recordArticleRead(String id, String title, String type) {
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);

		FlurryAgent.logEvent(EVENT_ARTICLE_READ, p, true);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_READ, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_READ);
		PopoApplication.faLogger.logEvent(e, b);


		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_READ)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordArticleComment(String id, String title, String type) {
		Logger.error("User add comment in news:"+title);
		checkEnvironment();
		Map<String, String> p = new LinkedHashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);

		FlurryAgent.logEvent(EVENT_ARTICLE_COMMENT, p, true);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_COMMENT, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_COMMENT);
		PopoApplication.faLogger.logEvent(e, b);


		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_COMMENT)
				.setAction(title)
				.setLabel(l)
				.build());
	}



	public static void recordArticleAdsI(String id, String title, String type, String provider, String name) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();

		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);
		p.put(EVENT_ARG_PROVIDER, provider);
		p.put(EVENT_ARG_AD_NAME, name);

		FlurryAgent.logEvent(EVENT_ARTICLE_ADSI, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_ADSI, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_ADSI);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_ADSI)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordArticleAdsB(String id, String title, String type, String provider, String name) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);
		p.put(EVENT_ARG_PROVIDER, provider);


		FlurryAgent.logEvent(EVENT_ARTICLE_ADSB, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_ADSB, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_ADSB);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_ADSB)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordArticleShare(String id, String title, String type, String via) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);
		p.put(EVENT_ARG_VIA, via);

		FlurryAgent.logEvent(EVENT_ARTICLE_SHARE, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_SHARE, mapToMap(p));


		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_SHARE);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_SHARE)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordArticleMood(String id, String title, String type, String mood) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);
		p.put(EVENT_ARG_MOOD, mood);


		FlurryAgent.logEvent(EVENT_ARTICLE_MOOD, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_MOOD, mapToMap(p));


		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_MOOD);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_MOOD)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordArticleFav(String id, String title, String type, String fav) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);
		p.put(EVENT_ARG_FAV, fav);

		FlurryAgent.logEvent(EVENT_ARTICLE_FAV, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_FAV, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_FAV);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_FAV)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordArticleFont(String id, String title, String type, String font) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_ID, id);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_TYPE, type);
		p.put(EVENT_ARG_FONT, font);

		FlurryAgent.logEvent(EVENT_ARTICLE_FONT, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ARTICLE_FONT, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ARTICLE_FONT);
		PopoApplication.faLogger.logEvent(e, b);


		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ARTICLE_FONT)
				.setAction(title)
				.setLabel(l)
				.build());
	}


	public static void recordSns() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_SNS);
		AppsFlyerLib.trackEvent(App.get(), EVENT_SNS, null);

		String e = makeFAEventName(EVENT_SNS);
		PopoApplication.faLogger.logEvent(e);

		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_SNS)
				.build());

	}


	public static void recordSnsClick(String sns) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_SNS, sns);


		FlurryAgent.logEvent(EVENT_SNS_CLICK, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_SNS_CLICK, mapToMap(p));

		String e = makeFAEventName(EVENT_SNS_CLICK);
		PopoApplication.faLogger.logEvent(e);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_SNS_CLICK)
				.setAction(sns)
				.setLabel(l)
				.build());
	}

	public static void recordSnsLogin(String sns, String login) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();
		p.put(EVENT_ARG_LOGIN, login);

		p.put(EVENT_ARG_SNS, sns);


		FlurryAgent.logEvent(EVENT_SNS_LOGIN, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_SNS_LOGIN, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_SNS_LOGIN);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_SNS_LOGIN)
				.setAction(sns)
				.setLabel(l)
				.build());
	}

	public static void recordSnsFetch(String sns, String count) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_SNS, sns);
		p.put(EVENT_ARG_COUNT, count);


		FlurryAgent.logEvent(EVENT_SNS_FETCH, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_SNS_FETCH, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_SNS_FETCH);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_SNS_FETCH)
				.setAction(sns)
				.setLabel(l)
				.build());
	}

	public static void recordSnsRead(String sns, String url, String title) {
		checkEnvironment();

		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_SNS, sns);
		p.put(EVENT_ARG_URL, url);
		p.put(EVENT_ARG_TITLE, title);


		FlurryAgent.logEvent(EVENT_SNS_READ, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_SNS_READ, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_SNS_READ);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_SNS_READ)
				.setAction(sns)
				.setLabel(l)
				.build());
	}

	public static void recordSnsAdsI(String sns, String url, String title, String provider, String name) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();


		p.put(EVENT_ARG_SNS, sns);
		p.put(EVENT_ARG_URL, url);
		p.put(EVENT_ARG_TITLE, title);
		p.put(EVENT_ARG_PROVIDER, provider);
		p.put(EVENT_ARG_AD_NAME, name);

		FlurryAgent.logEvent(EVENT_SNS_ADSI, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_SNS_ADSI, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_SNS_ADSI);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_SNS_ADSI)
				.setAction(sns)
				.setLabel(l)
				.build());
	}


	public static void recordMe() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME, null);
		String e = makeFAEventName(EVENT_ME);
		PopoApplication.faLogger.logEvent(e);

		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME)
				.build());
	}

	public static void recordMeFav() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_FAV);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_FAV, null);
		String e = makeFAEventName(EVENT_ME_FAV);
		PopoApplication.faLogger.logEvent(e);

		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_FAV)
				.build());
	}

	public static void recordMeFeed() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_FEED);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_FEED, null);
		String e = makeFAEventName(EVENT_ME_FEED);
		PopoApplication.faLogger.logEvent(e);

		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_FEED)
				.build());
	}

	public static void recordMeRecent() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_RECENT);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_RECENT, null);
		String e = makeFAEventName(EVENT_ME_RECENT);
		PopoApplication.faLogger.logEvent(e);

		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_RECENT)
				.build());
	}

	public static void recordMeOffline() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_OFFLINE);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_OFFLINE, null);
		String e = makeFAEventName(EVENT_ME_OFFLINE);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_OFFLINE)
				.build());
	}

	public static void recordMeOffers() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_OFFERS);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_OFFERS, null);
		String e = makeFAEventName(EVENT_ME_OFFERS);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_OFFERS)
				.build());
	}

	public static void recordMeSetting() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_SETTINGS);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS, null);
		String e = makeFAEventName(EVENT_ME_SETTINGS);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS)
				.build());
	}

	public static void recordMeEditon() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_SETTINGS_EDITION);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS_EDITION, null);
		String e = makeFAEventName(EVENT_ME_SETTINGS_EDITION);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS_EDITION)
				.build());
	}

	public static void recordMeAutoplay(String autoplay) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();
		p.put(EVENT_ARG_AUTOPLAY, autoplay);
		FlurryAgent.logEvent(EVENT_ME_SETTINGS_AUTOPLAY, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS_AUTOPLAY, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_ME_SETTINGS_AUTOPLAY);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS_AUTOPLAY)
				.setAction(autoplay)
				.setLabel(l)
				.build());
	}

	public static void recordMeClear() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_SETTINGS_CLEAR);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS_CLEAR, null);
		String e = makeFAEventName(EVENT_ME_SETTINGS_CLEAR);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS_CLEAR)
				.build());
	}

	public static void recordMeFeedBack() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_SETTINGS_FEEDBACK);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS_FEEDBACK, null);
		String e = makeFAEventName(EVENT_ME_SETTINGS_FEEDBACK);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS_FEEDBACK)
				.build());
	}

	public static void recordMeFeedbackSend() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_SETTINGS_FEEDBACK_SEND);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS_FEEDBACK_SEND, null);
		String e = makeFAEventName(EVENT_ME_SETTINGS_FEEDBACK_SEND);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS_FEEDBACK_SEND)
				.build());
	}

	public static void recordMeAbout() {
		checkEnvironment();
		FlurryAgent.logEvent(EVENT_ME_SETTINGS_ABOUT);
		AppsFlyerLib.trackEvent(App.get(), EVENT_ME_SETTINGS_ABOUT, null);
		String e = makeFAEventName(EVENT_ME_SETTINGS_ABOUT);
		PopoApplication.faLogger.logEvent(e);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_ME_SETTINGS_ABOUT)
				.build());
	}


	public static void recordPushRecv(String id) {
		checkEnvironment();
		Map<String, String> p = new HashMap<>();
		p.put(EVENT_ARG_ID, id);
		FlurryAgent.logEvent(EVENT_PUSH_RECV, p);
		AppsFlyerLib.trackEvent(App.get(), EVENT_PUSH_RECV, mapToMap(p));

		Bundle b = mapToBundle(p);
		String e = makeFAEventName(EVENT_PUSH_RECV);
		PopoApplication.faLogger.logEvent(e, b);

		String l = mapToLabel(p);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(EVENT_PUSH_RECV)
				.setAction(id)
				.setLabel(l)
				.build());
	}

	public static void recordLTV() {
		Map<String, Object> eventValue = new HashMap();
		eventValue.put(AFInAppEventParameterName.REVENUE, 0.01);
		eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "facebook"); // facebook, gmobi
		eventValue.put(AFInAppEventParameterName.CONTENT_ID, "placement name");
		eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
		AppsFlyerLib.trackEvent(App.get(), AFInAppEventType.PURCHASE, eventValue);
	}

}






