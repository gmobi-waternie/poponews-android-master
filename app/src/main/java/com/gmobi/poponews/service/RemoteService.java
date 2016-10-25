package com.gmobi.poponews.service;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.activity.SplashActivity;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.event.OfflineEventArgs;
import com.gmobi.poponews.event.UpgradeEventsArgs;
import com.gmobi.poponews.model.Comment;
import com.gmobi.poponews.model.CommentChannelEntity;
import com.gmobi.poponews.model.CommentChannelItem;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentReplyEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.PreferenceHelper;
import com.gmobi.poponews.util.TimeUtil;
import com.gmobi.poponews.util.UserInfoCallBack;
import com.momock.app.App;
import com.momock.app.ICase;
import com.momock.data.DataList;
import com.momock.event.IEventHandler;
import com.momock.holder.DialogHolder;
import com.momock.holder.TextHolder;
import com.momock.http.HttpSession;
import com.momock.http.HttpSession.StateChangedEventArgs;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.HttpService;
import com.momock.service.IAsyncTaskService;
import com.momock.service.ICacheService;
import com.momock.service.IHttpService;
import com.momock.service.IJsonService;
import com.momock.service.IJsonService.JsonEventArgs;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;


public class RemoteService implements IRemoteService {

	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{IConfigService.class,IJsonService.class, IDataService.class, IUITaskService.class, IAsyncTaskService.class, IUpdateService.class, INewsCacheService.class};
	}

	@Inject
	IDataService dataService;
	@Inject
	ICommentService commentService;

	@Inject
	IReportService reportService;

	@Inject
	IConfigService configService;
	@Inject
	IJsonService jsonService;
	@Inject
	IMessageService messageService;
	@Inject
	IAsyncTaskService asyncTaskService;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	ICacheService cacheService;
	@Inject
	IUpdateService updateService;
	@Inject
	Resources resources;
	@Inject
	Context context;
	@Inject
	IHttpService httpService;
	@Inject
	IOfflineService offlineService;
	@Inject
	INewsCacheService newsCacheService;
	@Inject
	IUserService userService;

	private boolean editionRequestIsRunning = false;

	private static final String CACHE_CATEGORY = "cache.category";
	private static final String CACHE_NEWS_LIST_PREFIX = "cache.newslist.";
	private static final String CACHE_NEWS_DATA_PREFIX = "cache.newsdata.";


	//时间标志位
	public static final int EARLY_TIME = 0;//比某时间早
	public static final int LATER_TIME = 1;//比某时间晚


	//Connect API RSP JSON数据的TAG定义
	public static final String TAG_DID = "did";
	public static final String TAG_BASEURL = "baseUrl";
	public static final String TAG_UPDATE = "update";
	public static final String TAG_VERSION = "lastVersion";
	public static final String TAG_UPDATE_FILE = "update_file";
	public static final String TAG_UPDATE_RN = "rn";
	public static final String TAG_EDITION_CHANNEL = "channel";
	public static final String TAG_EDITION_INFO = "channelInfo";

	public static final String TAG_EDITION_LANG = "lang";
	public static final String TAG_EDITION_COUNTRY = "country";
	public static final String TAG_EDITION_AS_CHANNEL = "minikit";
	public static final String TAG_AD1 = "ad1";
	public static final String TAG_AD2 = "ad2";
	public static final String TAG_AD3 = "ad3";
	public static final String TAG_AD4 = "ad4";
	public static final String TAG_AD5 = "ad5";
	public static final String TAG_AD_ENABLED = "enabled";
	public static final String TAG_AD_COUNT = "count";
	public static final String TAG_AD_TIME = "time";
	public static final String TAG_AD_PERCENT = "percent";
	public static final String TAG_AD_FB_PERCENT = "fb";
	public static final String TAG_DCH = "dch";

	public static final String TAG_EXTRA = "extra";
	public static final String TAG_EXTRA_SHARE = "share";
	public static final String TAG_EXTRA_SHARE_NAME = "name";
	public static final String TAG_EXTRA_SHARE_TITLE = "title";


	private final static int FETCH_COUNT = 10;
	private final static int OFFLINE_FETCH_COUNT = 100;
	private static final String BEFORE_TEMPLATE_URL = "/api/news/list?cid={cid}&before={time}&count={count}&did={did}";
	private static final String AFTER_TEMPLATE_URL = "/api/news/list?cid={cid}&after={time}&count={count}&did={did}";

	public static final String COMMENT_HOT = "/api/comment/condition?i_id={i_id}&hottest_list=true&per_page={per_page}";
	public static final String COMMENT_NEWS = "/api/comment?i_id={i_id}&page={page}&per_page={per_page}";

	Map<String, HttpSession> sessions = new HashMap<String, HttpSession>();
	HttpSession upgradeSession;

	//TODO 后期会被替换成CacheService中的API
	File getCategoryCacheFile() {
		return new File(cacheService.getCacheDir(null), CACHE_CATEGORY);
	}

	File getNewsListCacheFile(String cid) {
		return new File(cacheService.getCacheDir(null), CACHE_NEWS_LIST_PREFIX + cid);
	}

	File getNewsDataCacheFile(String nid) {
		return new File(cacheService.getCacheDir(null), CACHE_NEWS_DATA_PREFIX + nid);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canStop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getConfig() {
		// TODO Auto-generated method stub

	}


	private void showErrorDialog() {
		Runnable showDialog = new Runnable() {

			@Override
			public void run() {
				if (App.get().getCurrentActivity() == null) {
					uiTaskService.runDelayed(this, 1000);
					return;
				}


				ICase currCase = App.get().getActiveCase();
				if (currCase != null) {
					String className = currCase.getFullName();
					if (className.equalsIgnoreCase("/GuideCase"))
						return;
				}

				DialogHolder dh = DialogHolder.create(
						TextHolder.get(R.string.title_error),
						TextHolder.get(R.string.error_to_load_data),
						TextHolder.get(R.string.button_ok), null);
				dh.show(App.get().getCurrentActivity());
			}

		};
		if (App.get().getCurrentActivity() == null)
			uiTaskService.runDelayed(showDialog, 1000);
		else
			showDialog.run();
	}

	private IMessageHandler dataErrorhandler = null;

	@Override
	public void doService() {

		if (dataErrorhandler == null) {
			dataErrorhandler = new IMessageHandler() {

				@Override
				public void process(Object sender, Message msg) {
					if (!dataService.isCtgReady()) {
						String connectData = newsCacheService.getConnectCache();
						if (connectData != null) {
							ProcessConnect(connectData);
							dataService.setCategory(connectData, false,false);

							SplashActivity.splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_ENTRY_MAIN);
						} else {
							/*
							*兼容1.2.4之前的版本
							*/
							if (dataService.getCategoryFromCache())
								SplashActivity.splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_ENTRY_MAIN);
						}
					}
				}
			};
			App.get().getService(IMessageService.class).addHandler(MessageTopics.DATA_ERROR, dataErrorhandler);
		}


//		uiTaskService.run(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					//TODO
//				} catch (Exception e) {
//					Logger.error(e);
//				}
//			}
//		});
		asyncTaskService.run(new Runnable() {

			@Override
			public void run() {
			if(App.isEnvironmentCreated())
				connect(true, true);
			else
			{
				App.get();

				final Timer readyTimer = new Timer();

				readyTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						if(App.isEnvironmentCreated()) {
							readyTimer.cancel();
							connect(true, true);
						}
					}
				}, 1000, 1000);
			}

			}
		});

	}

	private String getConnectUrl() {
		StringBuilder url = new StringBuilder(128);
		url.append(configService.getEntryBaseUrl());
		url.append("/api/news/connect?group=");
		url.append(BuildConfig.GROUP);
		String ch = configService.getCurChannel();
		if (!ch.isEmpty()) {
			url.append("&channel=");
			url.append(ch);
		} else {

			String installerCh = PreferenceHelper.getInstallerChannel(App.get());
			if (!installerCh.equals("")) {
				url.append("&channel=");
				url.append(installerCh);
			}
			Logger.debug("use installerCh:" + installerCh);
		}
		return url.toString();
	}

	private String getDefaultReportUrl() {
		return configService.getEntryBaseUrl() + "/api/news/data";
	}

	private String getLatestNewsListUrl(String cid, long utc_time) {
		String url = configService.getEntryBaseUrl() + AFTER_TEMPLATE_URL;
		String did = configService.getDid();
		return url.replace("{cid}", URLEncoder.encode(cid)).replace("{time}", utc_time + "").replace("{count}", FETCH_COUNT + "").replace("{did}", did + "");
	}

	private String getEarlyNewsListUrl(String cid, long utc_time) {
		String url = configService.getEntryBaseUrl() + BEFORE_TEMPLATE_URL;
		String did = configService.getDid();
		return url.replace("{cid}", URLEncoder.encode(cid)).replace("{time}", utc_time + "").replace("{count}", FETCH_COUNT + "").replace("{did}", did + "");
	}

	private String getOfflineNewsListUrl(String cid, long utc_time) {
		String url = configService.getEntryBaseUrl() + BEFORE_TEMPLATE_URL;
		String did = configService.getDid();
		return url.replace("{cid}", URLEncoder.encode(cid)).replace("{time}", utc_time + "").replace("{count}", OFFLINE_FETCH_COUNT + "").replace("{did}", did + "");
	}

	private String getEditionListUrl(String gp) {
		return configService.getEntryBaseUrl() + "/api/news/group/" + gp;
	}

	private String getContentUrl(String nid) {
		return configService.getEntryBaseUrl() + "/api/news/article/" + nid;
	}

	private String getBodyUrl(String body) {
		return configService.getBaseUrl() + body;
	}

	/** 热门评论接口 **/
	private String getCommentHotNews(String i_id, int hotCount){
		String url = configService.getEntryBaseUrl() + COMMENT_HOT;
		return url.replace("{i_id}", i_id).replace("{per_page}", hotCount + "");
	}

	/** 最新评论接口 **/
	private String getCommentNews(String i_id, int page, int newsCount){
		String url = configService.getEntryBaseUrl() + COMMENT_NEWS;
		return url.replace("{i_id}", i_id).replace("{page}", page + "").replace("{per_page}", newsCount + "");
	}

	/** 评论点赞接口 **/
	private String getCommentApproval(String i_id, String path){
		Log.i("oye","----i_id---"+i_id+"--path--"+path);
		if (path == null || "".equals(path)){
			return configService.getEntryBaseUrl() + "/api/comment/approval?id=" + i_id + "&path=null";
		} else {
			return configService.getEntryBaseUrl() + "/api/comment/approval?path=" + path;
		}
	}

	/** 增加评论接口 **/
	private String getAddComment(){
		return configService.getEntryBaseUrl() + "/api/comment";
	}

	/** 获取特定用户的所有评论接口 **/
	private String getChannelComment(String u_id, String channel){
		return configService.getEntryBaseUrl() + "/api/comment/user/" + u_id + "?channel=" + channel;
	}

	@Override
	public void connect(final boolean update, final boolean needParse) {




		String connectUrl = getConnectUrl();
		Logger.debug(connectUrl);


		JSONObject deviceInfo = getDeviceInfo();
		JSONObject jo = new JSONObject();


		try {

			jo.put("device", deviceInfo);
			jo.put("update", configService.checkDeviceInfoData(deviceInfo.toString()));

			if (configService.getDid() != null)
				jo.put("did", configService.getDid());
			Logger.debug("POPONews connect data = " + jo.toString());



			jsonService.post(connectUrl, jo, null, new IEventHandler<JsonEventArgs>() {
				@Override
				public void process(Object sender, JsonEventArgs args) {
					if (!needParse)
						return;

					if (args == null) {
						Logger.debug("Connect RSP NULL!!!");
						return;
					}


					try {
						Logger.debug("Connect RSP" + args.getResponse());
						if (args.getJson() instanceof JSONObject) {


							ProcessConnect(args.getResponse());

							dataService.setCategory(args.getResponse(), update, true);

							initAdPools();

							DBHelper.getInstance().saveConnectData(new JSONObject(args.getResponse()));

							if (update)
								SplashActivity.splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_UPDATE_CHECK);


							ISocialDataService sds = App.get().getService(ISocialDataService.class);

							IFacebookService fbs = App.get().getService(IFacebookService.class);
							IGoogleService ggs = App.get().getService(IGoogleService.class);
							ITwitterService tws = App.get().getService(ITwitterService.class);
							IBaiduService bds = App.get().getService(IBaiduService.class);
							IWeiboService wbs = App.get().getService(IWeiboService.class);

							if (fbs.isEnabled() && fbs.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
								sds.restoreAccountsFromCache(SocialExtra.SOCIAL_TYPE_FACEBOOK);
								sds.restoreDataFromCache(SocialExtra.SOCIAL_TYPE_FACEBOOK);

							}
							if (ggs.isEnabled() && ggs.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
								sds.restoreAccountsFromCache(SocialExtra.SOCIAL_TYPE_GOOGLE);
								sds.restoreDataFromCache(SocialExtra.SOCIAL_TYPE_GOOGLE);
							}
							if (tws.isEnabled() && tws.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_TWITTER)) {
								sds.restoreAccountsFromCache(SocialExtra.SOCIAL_TYPE_TWITTER);
								sds.restoreDataFromCache(SocialExtra.SOCIAL_TYPE_TWITTER);
							}

							if (wbs.isEnabled() && wbs.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_WEIBO)) {
								sds.restoreAccountsFromCache(SocialExtra.SOCIAL_TYPE_WEIBO);
								sds.restoreDataFromCache(SocialExtra.SOCIAL_TYPE_WEIBO);
							}

							if (bds.isEnabled() && bds.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_BAIDU)) {
								sds.restoreAccountsFromCache(SocialExtra.SOCIAL_TYPE_BAIDU);
								sds.restoreDataFromCache(SocialExtra.SOCIAL_TYPE_BAIDU);
							}

						} else {
							Logger.error(args.getResponse());
							messageService.send(null, MessageTopics.DATA_ERROR);
						}
					} catch (Exception e) {
						messageService.send(null, MessageTopics.DATA_ERROR);
						Logger.debug("Connect RSP Error: " + e.toString());
					}
				}

			});
		} catch (JSONException e1) {

			e1.printStackTrace();
		}


	}

	private void ProcessConnect(String data) {
		JSONObject jn;
		try {
			jn = new JSONObject(data);

			String base_url = null;
			if ((base_url = jn.getString(TAG_BASEURL)) != null)
				configService.setBaseUrl(base_url);

			String did = null;
			if ((did = jn.getString(TAG_DID)) != null)
				configService.setDid(did);


			JSONObject editionInfo = jn.getJSONObject(TAG_EDITION_INFO);


			if (editionInfo.has(TAG_EXTRA)) {

				JSONObject extraInfo = editionInfo.getJSONObject(TAG_EXTRA);
				if (extraInfo.has(TAG_EXTRA_SHARE)) {
					JSONArray shareArray = extraInfo.getJSONArray(TAG_EXTRA_SHARE);
					App.get().getService(IShareService.class).clearShares();
					for (int i = 0; i < shareArray.length(); i++) {
						JSONObject jo = (JSONObject) shareArray.get(i);

						App.get().getService(IShareService.class).addShare(jo.getString(TAG_EXTRA_SHARE_NAME)
								, jo.getString(TAG_EXTRA_SHARE_TITLE));
					}
				}


			}


			configService.updateCurEdition(jn.getString(TAG_EDITION_CHANNEL),
					editionInfo.getString(TAG_EDITION_LANG),
					editionInfo.getString(TAG_EDITION_COUNTRY),
					editionInfo.getString(TAG_EDITION_AS_CHANNEL));


			if (editionInfo.has(TAG_AD1)) {
				JSONObject adInfo = editionInfo.getJSONObject(TAG_AD1);
				if (adInfo.has(TAG_AD_COUNT))
					configService.updateNativeAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), adInfo.getInt(TAG_AD_COUNT), adInfo.getInt(TAG_AD_FB_PERCENT));
				else {
					if (adInfo.has(TAG_AD_FB_PERCENT))
						configService.updateNativeAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), 0, adInfo.getInt(TAG_AD_FB_PERCENT));
					else
						configService.updateNativeAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), 0, 0);
				}
			}
			if (editionInfo.has(TAG_AD2)) {
				JSONObject adInfo = editionInfo.getJSONObject(TAG_AD2);
				if (adInfo.has(TAG_AD_TIME))
					configService.updateInterstitialAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), adInfo.getInt(TAG_AD_TIME), adInfo.getInt(TAG_AD_PERCENT),
							adInfo.has(TAG_AD_FB_PERCENT) ? adInfo.getInt(TAG_AD_FB_PERCENT) : 0);
				else {
					configService.updateInterstitialAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), -1, 0,
							adInfo.has(TAG_AD_FB_PERCENT) ? adInfo.getInt(TAG_AD_FB_PERCENT) : 0);
				}

			}
			if (editionInfo.has(TAG_AD3)) {
				JSONObject adInfo = editionInfo.getJSONObject(TAG_AD3);
				configService.updateBannerAdConfigure(
						adInfo.getBoolean(TAG_AD_ENABLED),
						adInfo.has(TAG_AD_FB_PERCENT) ? adInfo.getInt(TAG_AD_FB_PERCENT) : 0);
			}

			if (editionInfo.has(TAG_AD4)) {
				JSONObject adInfo = editionInfo.getJSONObject(TAG_AD4);
				if (adInfo.has(TAG_AD_TIME))
					configService.updateSocialAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), adInfo.getInt(TAG_AD_TIME), adInfo.getInt(TAG_AD_PERCENT),
							adInfo.has(TAG_AD_FB_PERCENT) ? adInfo.getInt(TAG_AD_FB_PERCENT) : 0);
				else {
					configService.updateSocialAdConfigure(adInfo.getBoolean(TAG_AD_ENABLED), -1, 0, adInfo.has(TAG_AD_FB_PERCENT) ? adInfo.getInt(TAG_AD_FB_PERCENT) : 0);
				}

			}
			if (editionInfo.has(TAG_AD5)) {
				JSONObject adInfo = editionInfo.getJSONObject(TAG_AD5);
				configService.updateArticleNativeAdConfigure(
						adInfo.getBoolean(TAG_AD_ENABLED),
						adInfo.has(TAG_AD_PERCENT) ? adInfo.getInt(TAG_AD_PERCENT) : 0);
			}


			Integer update = 0;
			if ((update = jn.getInt(TAG_UPDATE)) != null)
				configService.setUpdateFlag(update);

			if (update != ConfigService.UPDATE_NONE) {
				String updatefile;
				if ((updatefile = jn.getString(TAG_UPDATE_FILE)) != null)
					configService.setUpdateFile(updatefile);

				String updateVersion;
				if ((updateVersion = jn.getString(TAG_VERSION)) != null)
					configService.setUpdateVersion(updateVersion);
			}

			String dch = null;
			if (jn.has(TAG_DCH)) {
				if ((dch = jn.getString(TAG_DCH)) != null)
					configService.setDch(dch);
			}

		} catch (JSONException e) {
			messageService.send(null, MessageTopics.CONNECT_ERROR);
			e.printStackTrace();
		}new Thread(){
			@Override
			public void run() {
				IDataService ds = App.get().getService(IDataService.class);

				ds.initReadList();
				ds.initPushList();
				JSONObject userInfo = configService.getLoginUserInfo();
				parseUserInfo(userInfo);

				if (userService.getUserInfo() == null){
					ds.initUninterestList(GlobalConfig.USER_ID);
				} else {
					ds.initUninterestList(userService.getUserInfo().getUId());
				}
				//if(!configService.isEditionListExist()) {
				getEditionList(GlobalConfig.FROM_SPLASH);
			}
		}


		.start();


		//App.get().getService(IDataService.class).initCacheItemsList();


	}

	private void parseUserInfo(JSONObject user) {
		CommentUserInfo info = new CommentUserInfo();
		if (user != null){
			info.setUId(user.optString("_id"));
			if (user.optString("avatar") == null){
				info.setAvatar(null);
			} else {
				info.setAvatar(user.optString("avatar"));
			}
			info.setEmail(user.optString("email"));
			info.setUserName(user.optString("name"));
			info.setLogin(true);
			userService.setUserInfo(info);
			userService.setLogin(true);
		} else {
			userService.setUserInfo(null);
			userService.setLogin(false);
		}
	}

	@Override
	public void getList(final String cid, long time, final int TimeFlag) {
		long curTime = TimeUtil.getInstance().getCurUtcTime();
		if (time == 0)
			time = curTime;

		String connectUrl = "";

		final NewsCategory ctg = dataService.getCategoryById(cid);

		if (TimeFlag == EARLY_TIME) {
			AnalysisUtil.recordNewsListPrev(cid, ctg.getname());
			connectUrl = getEarlyNewsListUrl(cid, time);

			if (ctg.isCache()) {
				uiTaskService.run(new Runnable() {
					@Override
					public void run() {
						dataService.setNewsList(cid, null, true, false);
					}
				});
			}

		} else {
			AnalysisUtil.recordNewsListNext(cid, ctg.getname());

			if (ctg.isCache()) {
				connectUrl = getEarlyNewsListUrl(cid, time);
			} else
				connectUrl = getLatestNewsListUrl(cid, time);
		}


		Logger.debug(connectUrl);
		final String url  =  connectUrl;

		new Thread(new Runnable() {
			@Override
			public void run() {
				jsonService.get(url, null, new IEventHandler<JsonEventArgs>() {
							@Override
							public void process(Object sender, final JsonEventArgs args) {
								if (args == null) {
									Logger.debug("Connect RSP NULL!!!");
									if (TimeFlag == EARLY_TIME) {
										AnalysisUtil.endRecordNewsListPrev(cid, ctg.getname());
									} else {
										AnalysisUtil.endRecordNewsListNext(cid, ctg.getname());
									}
									return;
								}

								try {
									Logger.debug("Connect RSP" + args.getResponse());
									if (TimeFlag == EARLY_TIME) {
										AnalysisUtil.endRecordNewsListPrev(cid, ctg.getname());
									} else {
										AnalysisUtil.endRecordNewsListNext(cid, ctg.getname());
									}

									if (args.getJson() instanceof JSONArray) {
//										if (ctg.isCache()) {
//											dataService.clearAllCacheContent(cid);
//											ctg.setCacheFlag(false);
//										}

										uiTaskService.run(new Runnable() {
											@Override
											public void run() {
												dataService.setNewsList(cid, args.getResponse(), false, (TimeFlag == EARLY_TIME));
											}
										});



									} else {
										Logger.error(args.getResponse());
										messageService.send(null, MessageTopics.DATA_ERROR);
									}
								} catch (Exception e) {
									Logger.debug("Connect RSP Error: " + e.toString());
								}
							}

						}

				);
			}
		}).start();


	}


	@Override
	public void getSingleNews(final String nid,final String extra,final int type) {
		String connectUrl = getContentUrl(nid);
		Logger.debug(connectUrl);
		jsonService.get(connectUrl, null, new IEventHandler<JsonEventArgs>() {
			@Override
			public void process(Object sender, JsonEventArgs args) {
				if (args == null) {
					Logger.debug("[PUSH]:Connect RSP NULL!!!");
					return;
				}

				try {
					Logger.debug("[PUSH]:Connect RSP" + args.getResponse());
					if (args.getJson() instanceof JSONObject) {
						NewsItem n = dataService.getPushItem(args.getResponse());

						if(type == GlobalConfig.SINGLE_NEWS){
							if (configService.getNewsFeedFlag()) {
								HashMap<String, String> msgData = new HashMap<String, String>();
								msgData.put("title", (extra == null || extra.equals("")) ? n.getTitle():extra);
								msgData.put("nid", nid);
								messageService.send(this, MessageTopics.PUSH_CONTENT_READY, msgData);
							}
						} else if (type == GlobalConfig.SINGLE_COMMENT){
//							dataService.addIntoNewsList(n);
							dataService.addIntoCommentList(n);
							messageService.send(this, MessageTopics.NEWS_CONTENT_LOADED_COMMENT, n);
						}

					} else {


					}
				} catch (Exception e) {
					Logger.debug("Connect RSP Error: " + e.toString());
				}
			}

		});

	}

	@Override
	public void getOfflineList(final String cid) {
		String connectUrl = getOfflineNewsListUrl(cid, TimeUtil.getInstance().getCurUtcTime());

		Logger.debug(connectUrl);
		jsonService.get(connectUrl, null, new IEventHandler<JsonEventArgs>() {
			@Override
			public void process(Object sender, JsonEventArgs args) {
				if (args == null) {
					offlineService.getNotificationEvent().fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_CONTENT_FINISHED, cid, null));
					return;
				}

				try {
					//Logger.debug("Offline Connect RSP" + args.getResponse());
					if (args.getJson() instanceof JSONArray) {

						dataService.setOfflineNewsList(cid, args.getResponse());
						offlineService.getNotificationEvent().fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_LIST, cid, null));

					} else {
						Logger.error(args.getResponse());
						offlineService.getNotificationEvent().fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_CONTENT_FINISHED, cid, null));
					}
				} catch (Exception e) {
					offlineService.getNotificationEvent().fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_CONTENT_FINISHED, cid, null));
					Logger.debug("Connect RSP Error: " + e.toString());
					e.printStackTrace();
				}
			}

		});

	}

	@Override
	public void getContent(final String body) {
		String connectUrl = getBodyUrl(body);
		Logger.debug(connectUrl);

		jsonService.get(connectUrl, null, new IEventHandler<JsonEventArgs>() {
			@Override
			public void process(Object sender, JsonEventArgs args) {
				if (args == null) {
					Logger.debug("Connect RSP NULL!!!");
					return;
				}

				try {
					Logger.debug("Connect RSP" + args.getResponse());
					if (args.getJson() instanceof JSONObject) {

						dataService.setCurNewsData(args.getResponse());

					} else {
						Logger.error(args.getResponse());
						messageService.send(null, MessageTopics.DATA_ERROR);
					}
				} catch (Exception e) {
					// TODO: handle exception
					Logger.debug("Connect RSP Error: " + e.toString());
				}
			}

		});
	}

	@Override
	public void getBodyContent(final String nid, String body) {
		final String connectUrl = getBodyUrl(body);
		final File f = cacheService.getCacheOf(CacheNames.NEWS_CONTENT_CACHEDIR, connectUrl);

		Logger.debug(connectUrl);
		if (f.exists()) {
			messageService.send(this, MessageTopics.NEWS_CONTENT_LOADED, nid);
			new Thread() {
				public void run() {
					try {
						DefaultHttpClient httpClient = new DefaultHttpClient();
						HttpGet httpGet = new HttpGet(connectUrl);
						HttpResponse httpResponse = httpClient.execute(httpGet);
						HttpEntity entity = httpResponse.getEntity();
						InputStream in = entity.getContent();
						long length = entity.getContentLength();
						if (length <= 0) {
							messageService.send(null, MessageTopics.CONTENT_ERROR);
							return;
						}
						OutputStream out = new FileOutputStream(f);
						byte[] data = new byte[8 * 1024];
						int index = 0;
						while ((index = in.read(data)) != -1) {
							out.write(data, 0, index);
						}
						in.close();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						messageService.send(null, MessageTopics.CONTENT_ERROR);
					} catch (Exception e) {
						e.printStackTrace();
						messageService.send(null, MessageTopics.CONTENT_ERROR);
					}

				}
			}.start();
		} else {
			new Thread() {
				public void run() {
					try {
						DefaultHttpClient httpClient = new DefaultHttpClient();
						HttpGet httpGet = new HttpGet(connectUrl);
						HttpResponse httpResponse = httpClient.execute(httpGet);
						HttpEntity entity = httpResponse.getEntity();
						InputStream in = entity.getContent();
						long length = entity.getContentLength();
						if (length <= 0) {
							messageService.send(null, MessageTopics.CONTENT_ERROR);
							return;
						}
						OutputStream out = new FileOutputStream(f);
						byte[] data = new byte[8 * 1024];
						int index = 0;
						while ((index = in.read(data)) != -1) {
							out.write(data, 0, index);
						}
						in.close();
						out.close();
						messageService.send(this, MessageTopics.NEWS_CONTENT_LOADED, nid);
					} catch (IOException e) {
						e.printStackTrace();
						messageService.send(null, MessageTopics.CONTENT_ERROR);
					} catch (Exception e) {
						e.printStackTrace();
						messageService.send(null, MessageTopics.CONTENT_ERROR);
					}

				}
			}.start();
		}
	}

	public void getEditionList(final int from) {
		if (editionRequestIsRunning) {
			Logger.info("edition request is running");
			return;
		}
		String group = BuildConfig.GROUP;
		jsonService.get(getEditionListUrl(group), null, new IEventHandler<JsonEventArgs>() {

			@Override
			public void process(Object sender, JsonEventArgs args) {
				editionRequestIsRunning = false;
				if (args == null) {
					Logger.debug("channel list RSP NULL!!!");
					return;
				}

				try {
					String rsp = args.getResponse();
					Logger.debug("channel list RSP = " + rsp);
					if (args.getJson() instanceof JSONObject) {
						configService.setEditionListData(rsp);
						if (from == GlobalConfig.FROM_SETTING)
							messageService.send(null, MessageTopics.EDITION_LIST_READY);
						else
							SplashActivity.splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_EDITION_READY);
					} else {
						Logger.debug("channel list is invalid");
					}
				} catch (Exception e) {
					Logger.debug("channel list RSP Error: " + e.toString());
				}

			}
		});
	}


	@Override
	public JSONObject getDeviceInfo() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) App.get().getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		String imei = mTelephonyMgr.getDeviceId();
		JSONObject json = new JSONObject();
		try {
			json.put("app", context.getPackageName());
			json.put("ch", configService.getCurChannel());
			json.put("group", BuildConfig.GROUP);
			json.put("app_v", SystemHelper.getAppVersion(context));
			json.put("imsi", imsi);
			json.put("imei", imei);
			json.put("sd", SystemHelper.hasSdcard(context));
			json.put("ua", SystemHelper.getUA(false));
			json.put("os", "android");
			json.put("os_v", SystemHelper.getOsVersion());
			json.put("lang", Locale.getDefault().getLanguage());
			json.put("country", SystemHelper.getCountry(context));
			json.put("wmac", SystemHelper.getWifiMac(context));
			json.put("bmac", "");
			json.put("sn", SystemHelper.getAndroidId(context));
			json.put("sa", SystemHelper.isSystemApp(context));
			json.put("sw", SystemHelper.getScreenWidth(context));
			json.put("sh", SystemHelper.getScreenHeight(context));

			json.put("dch", configService.getDch());
			json.put("gref", new JSONObject(configService.getReferrer()));

			json.put("user", configService.getUserInfo());
			json.put("pid", configService.getPushId());
			json.put("ppid", configService.getPrePushId());
			Logger.debug("Send Device Info: " + json.toString(4));

		} catch (JSONException e) {
			Logger.error(e);
		}
		return json;
	}

    HttpService service = (HttpService) App.get().getService(IHttpService.class);
    HttpSession commSession;

	private void parseHotJson(String json) {
		try {
			commentService.removeAllHotComment();
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0;i < jsonArray.length(); i++){
				CommentEntity entity = new CommentEntity();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				entity.setUpdateTime(jsonObject.optLong("_at")); // 评论时间
				entity.setIId(jsonObject.optString("_id")); // 唯一标识
				entity.setContent(jsonObject.optString("content")); // 评论内容
				entity.setItemId(jsonObject.optString("i_id")); // item的id
				entity.setPath(jsonObject.optString("path")); // path标识
				entity.setUserId(jsonObject.optString("u_id")); // 用户的id
				entity.setUserName(jsonObject.optString("u_name")); // 用户昵称
				entity.setDingNumber(jsonObject.optInt("approval")); // 点赞数量
				entity.setUserAvatar(jsonObject.optString("u_avatar"));
				JSONArray reply = jsonObject.getJSONArray("reply"); // 评论回复集合
				if (reply != null && reply.length() != 0){
					DataList<CommentReplyEntity> replyList = new DataList<>();
					for (int j = 0; j < reply.length(); j++){
						CommentReplyEntity replyEntity = new CommentReplyEntity();
						JSONObject object = reply.getJSONObject(j);
						replyEntity.setIId(object.optString("_id")); // 唯一标识
						replyEntity.setPath(object.optString("path"));
						replyEntity.setUserId(object.optString("u_id")); // 回复用户的id
						replyEntity.setItemId(object.optString("i_id")); // item项的id
						replyEntity.setContent(object.optString("content")); // 回复的内容
						replyEntity.setReplyTime(object.optLong("_at")); // 回复的时间
						replyEntity.setUserName(object.optString("u_name"));
						replyEntity.setToName(object.optString("t_name"));
						replyList.addItem(replyEntity);
					}
					if (reply.length() < 5){
						entity.setIsClick(true);
					}
					entity.setReplyList(replyList);
				} else {
					DataList<CommentReplyEntity> replyListNull = new DataList<>();
					entity.setIsClick(true);
					entity.setReplyList(replyListNull);
				}
				commentService.addItemHotComment(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private int parseNewsJson(String json) {
		try {
			JSONObject jsonObj = new JSONObject(json);
			Comment comment = new Comment();
			comment.setCount(jsonObj.optInt("count"));
			JSONArray jsonArray = jsonObj.getJSONArray("data");
			for (int i = 0;i < jsonArray.length(); i++){
				CommentEntity entity = new CommentEntity();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				entity.setUpdateTime(jsonObject.optLong("_at")); // 评论时间
				entity.setIId(jsonObject.optString("_id")); // 唯一标识
				entity.setContent(jsonObject.optString("content")); // 评论内容
				entity.setItemId(jsonObject.optString("i_id")); // item的id
				entity.setPath(jsonObject.optString("path")); // path标识
				entity.setUserId(jsonObject.optString("u_id")); // 用户的id
				entity.setUserName(jsonObject.optString("u_name")); // 用户昵称
				entity.setDingNumber(jsonObject.optInt("approval")); // 点赞数量
				entity.setUserAvatar(jsonObject.optString("u_avatar"));
				JSONArray reply = jsonObject.getJSONArray("reply"); // 评论回复集合
				if (reply != null && reply.length() != 0){
					DataList<CommentReplyEntity> replyList = new DataList<>();
					for (int j = 0; j < reply.length(); j++){
						CommentReplyEntity replyEntity = new CommentReplyEntity();
						JSONObject object = reply.getJSONObject(j);
						replyEntity.setIId(object.optString("_id")); // 唯一标识
						replyEntity.setPath(object.optString("path"));
						replyEntity.setUserId(object.optString("u_id")); // 回复用户的id
						replyEntity.setItemId(object.optString("i_id")); // item项的id
						replyEntity.setContent(object.optString("content")); // 回复的内容
						replyEntity.setReplyTime(object.optLong("_at")); // 回复的时间
						replyEntity.setUserName(object.optString("u_name"));
						replyEntity.setToName(object.optString("t_name"));
						replyList.addItem(replyEntity);
					}
					if (reply.length() < 5){
						entity.setIsClick(true);
					}
					entity.setReplyList(replyList);
				} else {
					DataList<CommentReplyEntity> replyListNull = new DataList<>();
					entity.setIsClick(true);
					entity.setReplyList(replyListNull);
				}
				comment.setEntity(entity);

				String nid = jsonObject.optString("i_id");
				commentService.addItemNewsComment(nid,entity);
			}
			return commentService.getCommentCount(comment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

    @Override
    public void startDownloadHotNews(String i_id, int hotCount) {
//        commSession = service.get(getCommentHotNews("570df86e03043016480f5560", hotCount));
        /*commSession = service.get(getCommentHotNews(i_id, hotCount));
        commSession.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {
            @Override
            public void process(Object o, StateChangedEventArgs stateChangedEventArgs) {
                if (stateChangedEventArgs.getState() == HttpSession.STATE_FINISHED) {
                    String json = commSession.getResultAsString("utf-8");
					messageService.send(this, new Message(MessageTopics.COMMENT_HOTS,json));
                }
            }
        });
        commSession.start();*/
        /*commSession = service.get(getCommentHotNews(i_id, hotCount));
        commSession.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {
            @Override
            public void process(Object o, StateChangedEventArgs stateChangedEventArgs) {
                if (stateChangedEventArgs.getState() == HttpSession.STATE_FINISHED) {
                    String json = commSession.getResultAsString("utf-8");
					messageService.send(this, new Message(MessageTopics.COMMENT_HOTS,json));
                }
            }
        });
        commSession.start();*/

		jsonService.get(getCommentHotNews(i_id, hotCount), null, new IEventHandler<JsonEventArgs>() {
			@Override
			public void process(Object o, JsonEventArgs args) {
				if (args == null) {
					Logger.debug("channel list RSP NULL!!!");
					return;
				}
				try {
					String rsp = args.getResponse();
					Logger.debug("channel list RSP = " + rsp);
					parseHotJson(rsp);
					uiTaskService.runDelayed(new Runnable() {
						@Override
						public void run() {
							messageService.send(null, new Message(MessageTopics.COMMENT_HOTS));
						}
					}, 1000);

					/*if (args.getJson() instanceof JSONObject) {
						parseJson(rsp);
						messageService.send(null, new Message(MessageTopics.COMMENT_HOTS));
					} else {
						Logger.debug("channel list is invalid");
					}*/
				} catch (Exception e) {
					Logger.debug("channel list RSP Error: " + e.toString());
				}

			}
		});
    }

    @Override
    public void startDownloadNews(String i_id, int page, int newsCount) {
//        commSession = service.get(getCommentNews("570df86e03043016480f5560", page));
        /*commSession = service.get(getCommentNews(i_id, page, newsCount));
        commSession.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {
            @Override
            public void process(Object o, StateChangedEventArgs stateChangedEventArgs) {
                if (stateChangedEventArgs.getState() == HttpSession.STATE_FINISHED) {
                    String json = commSession.getResultAsString("utf-8");
//					Log.i("oye","newscomment  -url----"+commSession.getUrl());
//					Log.i("oye","json  -----"+json);
					messageService.send(this, new Message(MessageTopics.COMMENT_NEWS,json));
                }
            }
        });
        commSession.start();*/
		Log.i("oye","new comment url---"+getCommentNews(i_id, page, newsCount));
		jsonService.get(getCommentNews(i_id, page, newsCount), null, new IEventHandler<JsonEventArgs>() {
			@Override
			public void process(Object o, JsonEventArgs args) {
				if (args == null) {
					Logger.debug("NewsComment list RSP NULL!!!");
					return;
				}
				try {
					String rsp = args.getResponse();
					Logger.debug("NewsComment list RSP = " + rsp);
					int count = parseNewsJson(rsp);
					messageService.send(null, new Message(MessageTopics.COMMENT_NEWS, count));
					/*if (args.getJson() instanceof JSONObject) {
						parseJson(rsp);
						messageService.send(null, new Message(MessageTopics.COMMENT_HOTS));
					} else {
						Logger.debug("channel list is invalid");
					}*/
				} catch (Exception e) {
					Logger.debug("channel list RSP Error: " + e.toString());
				}

			}
		});
    }

	String lastPath = "";
	/** 增加评论 **/
	@Override
	public void addComment(final CommentUserInfo info) {
		new Thread(){
			@Override
			public void run() {
				doAddComment(info);
			}
		}.start();
	}
	/** 评论回复 **/
	@Override
	public void addReply(final CommentUserInfo info,final boolean isReply) {
		new Thread(){
			@Override
			public void run() {
				doReply(info, isReply);
			}
		}.start();
	}
//	/** 评论回复的回复 **/
//	@Override
//	public void addReplyTo(final CommentUserInfo info) {
//		new Thread(){
//			@Override
//			public void run() {
//				doReply(info);
//			}
//		}.start();
//	}



	private void doAddComment(CommentUserInfo userInfo) {
		try {
			// 创建连接
			URL url = new URL(getAddComment());
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type",
					"application/json");
			connection.connect();
			// POST请求
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			try {
				JSONObject obj = new JSONObject();
				String i_id = userInfo.getIid();
				obj.put("i_id", i_id); // 新闻的ID
				obj.put("u_id", userInfo.getUId());
//				String t_id = userInfo.getTId();
//				if (t_id == null || "".equals(t_id)){
//					t_id = userInfo.getUId();
//				}
//				obj.put("t_id", t_id);
//				String path = userInfo.getPath();
//				if (isNew){
//					if (path == null || "".equals(path))
//						path = lastPath;
//				} else {
//					path += "/{n}*" +System.currentTimeMillis();
//				}
//				if (isNew){
//					lastPath = path+ "/{n}*" +System.currentTimeMillis();
//				}
				obj.put("path",userInfo.getPath());
				obj.put("content", userInfo.getContent());
				obj.put("channel", configService.getCurChannel());
				out.flush();
				out.write(obj.toString().getBytes());
				Log.i("oye","request ---" + obj.toString());
				out.close();
				if (connection.getResponseCode() == 200){
					// 读取响应
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					StringBuffer sb = new StringBuffer("");
					while ((lines = reader.readLine()) != null) {
						lines = new String(lines.getBytes(), "utf-8");
						sb.append(lines);
					}
					Log.i("oye","response--" + sb.toString());
					reader.close();
				} else {
					Log.i("oye","error----"+connection.getResponseCode());
				}
				// 断开连接
				connection.disconnect();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doReply(CommentUserInfo userInfo, boolean isReply){
		try {
			// 创建连接
			URL url = new URL(getAddComment());
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type",
					"application/json");
			connection.connect();
			// POST请求
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			try {
				JSONObject obj = new JSONObject();
				String i_id = userInfo.getIid();
				obj.put("i_id", i_id);
				obj.put("u_id", userInfo.getUId());
//				String t_id = userInfo.getTId();
//				if (t_id == null || "".equals(t_id)){
//					t_id = userInfo.getUId();
//				}
				obj.put("t_id", userInfo.getTId());
//				String path = userInfo.getPath();
//				if (isNew){
//					if (path == null || "".equals(path))
//						path = lastPath;
//				} else {
//					path += "/{n}*" +System.currentTimeMillis();
//				}
//				if (isNew){
//					lastPath = path+ "/{n}*" +System.currentTimeMillis();
//				}
//				String path = null;
//				if (!isReply){
//					path = userInfo.getPath();
//					obj.put("path",path);
//				} else {
//					path = path + "/{n}*" + System.currentTimeMillis();
//					obj.put("path",path);
//				}
				obj.put("path",userInfo.getPath());
				obj.put("content", userInfo.getContent());
				obj.put("channel", configService.getCurChannel());
				out.flush();
				out.write(obj.toString().getBytes());
				Log.i("oye","request ---" + obj.toString());
				out.close();
				if (connection.getResponseCode() == 200){
					// 读取响应
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String lines;
					StringBuffer sb = new StringBuffer("");
					while ((lines = reader.readLine()) != null) {
						lines = new String(lines.getBytes(), "utf-8");
						sb.append(lines);
					}
					Log.i("oye","response--" + sb.toString());
					reader.close();
				} else {
					Log.i("oye","error----"+connection.getResponseCode());
				}
				// 断开连接
				connection.disconnect();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** 点赞 **/
	@Override
	public void doApproval(String i_id,String path) {
		commSession = service.get(getCommentApproval(i_id, path));
		commSession.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {
			@Override
			public void process(Object o, StateChangedEventArgs stateChangedEventArgs) {
				if (stateChangedEventArgs.getState() == HttpSession.STATE_FINISHED){
					Log.i("oye","点赞接口---"+commSession.getUrl());
					Log.i("oye","点赞返回---"+commSession.getResultAsString("utf-8"));
				}
			}
		});
		commSession.start();
	}

	/** 获取特定用户的所有评论 **/
	@Override
	public void startDownloadChannelComment(String u_id, String channel) {
		/*commSession = service.get(getChannelComment(u_id, channel));
		Log.i("oye","url----"+getChannelComment(u_id, channel));
		commSession.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {
			@Override
			public void process(Object o, StateChangedEventArgs stateChangedEventArgs) {
				if (stateChangedEventArgs.getState() == HttpSession.STATE_FINISHED){
					String json = commSession.getResultAsString("utf-8");
					messageService.send(null, new Message(MessageTopics.COMMENT_CHANNEL, json));
				}
			}
		});
		commSession.start();*/
		jsonService.get(getChannelComment(u_id, channel), null, new IEventHandler<JsonEventArgs>() {
			@Override
			public void process(Object o, JsonEventArgs args) {
				if (args == null) {
					Logger.debug("channel list RSP NULL!!!");
					return;
				}
				try {
					String rsp = args.getResponse();
					Logger.debug("channel list RSP = " + rsp);
					parseChannelJson(rsp);
					messageService.send(null, new Message(MessageTopics.COMMENT_CHANNEL));
				} catch (Exception e) {
					Logger.debug("channel list RSP Error: " + e.toString());
				}
			}
		});
	}

	/*
	 * 解析特定用户的所有评论
	 */
	private void parseChannelJson(String json) {
		try {
			JSONArray jsonArray = new JSONArray(json);
			for (int i= 0; i < jsonArray.length(); i++){
				CommentChannelEntity channelEntity = new CommentChannelEntity();
				JSONObject jsonObject = jsonArray.optJSONObject(i);
				channelEntity.setUpdateTime(jsonObject.optLong("_at")); // 评论时间

				channelEntity.setIId(jsonObject.optString("_id")); // 唯一标识
				channelEntity.setContent(jsonObject.optString("content")); // 评论内容

				JSONObject replyObjcet = jsonObject.optJSONObject("fore_reply");
				if (replyObjcet != null){
					CommentReplyEntity replyEntity = new CommentReplyEntity();
					replyEntity.setReplyTime(replyObjcet.optLong("_at"));
					replyEntity.setIId(replyObjcet.optString("_id"));
					replyEntity.setContent(replyObjcet.optString("content")); // 评论内容
					replyEntity.setItemId(replyObjcet.optString("i_id")); // item的id
					replyEntity.setPath(replyObjcet.optString("path")); // path标识
					replyEntity.setToAvatar(replyObjcet.optString("t_avatar")); // 回复用户的头像
					replyEntity.setToId(replyObjcet.optString("t_id")); // 回复用户的id
					replyEntity.setToName(replyObjcet.optString("t_name")); // 回复用户昵称
					replyEntity.setUserAvatar(replyObjcet.optString("u_avatar")); // 用户的头像
					replyEntity.setUserId(replyObjcet.optString("u_id")); // 用户的id
					replyEntity.setUserName(replyObjcet.optString("u_name")); // 用户昵称
					channelEntity.setReply(replyEntity);

					channelEntity.setItemId(jsonObject.optString("i_id")); // item的id
					JSONObject items = jsonObject.optJSONObject("items");
					CommentChannelItem itemEntity = new CommentChannelItem();
					itemEntity.setIId(items.optString("_id")); // 唯一标识
					itemEntity.setGo2Source(items.optBoolean("go2source"));
					itemEntity.setPDomain(items.optString("p_domain"));
					itemEntity.setPIcon(items.optString("p_icon"));
					itemEntity.setPName(items.optString("p_name"));
					itemEntity.setPreview(items.optString("preview"));
					itemEntity.setPSource(items.optString("source"));
					itemEntity.setTitle(items.optString("title"));
					channelEntity.setItems(itemEntity);
					channelEntity.setPath(jsonObject.optString("path")); // path标识
					channelEntity.setToAvatar(jsonObject.optString("t_avatar")); // 回复用户的头像
					channelEntity.setToId(jsonObject.optString("t_id")); // 用户的id
					channelEntity.setToName(jsonObject.optString("t_name")); // 用户昵称
					channelEntity.setUserAvatar(jsonObject.optString("u_avatar")); // 用户的头像
					channelEntity.setUserId(jsonObject.optString("u_id")); // 用户的id
					channelEntity.setUserName(jsonObject.optString("u_name")); // 用户昵称
					commentService.addChannelComment(channelEntity);
				}


			}
		} catch (JSONException e){
			Log.e("oye","parse-error---"+e.getMessage());
		}
	}

	JSONObject getResultAsJson(HttpSession session) {
		try {
			//String json = LZStringHelper.decompress(session.getResult());
			String json = session.getResultAsString();
			Logger.info(json);
			return JsonHelper.parse(json);
		} catch (Exception e) {
			Logger.error(e);
		}
		return null;
	}

	HttpSession postJson(String url, String json) {
		ByteArrayEntity entity = null;
		try {
			//entity = new ByteArrayEntity(LZStringHelper.compress(json));
			entity = new ByteArrayEntity(json.getBytes());
		} catch (Exception e) {
			Logger.error(e);
		}
		Logger.debug(url + "(" + json.getBytes().length + " ==> "
				+ entity.getContentLength() + ")");
//		return httpService.post(url, new Header[] { new BasicHeader("Content-Type",
//				"application/cj") }, entity);
		return httpService.post(url, new Header[]{new BasicHeader("Content-Type",
				"application/json")}, entity);
	}

	@Override
	public boolean startDefaultReport(String requestBody) {
		boolean ret = false;
		if (null != requestBody) {
			Logger.info("start report collected data");
			HttpSession s = postJson(getDefaultReportUrl(), requestBody);
			s.start(true);
			if (s.getStatusCode() == 200) {
				JSONObject joResult = getResultAsJson(s);
				if (joResult != null && joResult.has("status")) {
					Logger.debug("request successful");
					ret = true;
				}
			}
		}
		return ret;
	}


	@Override
	public void stopDownloadOffline() {
		Iterator<String> i = sessions.keySet().iterator();
		while (i.hasNext()) {
			HttpSession session = sessions.get(i.next());
			if (session != null)
				session.stop();
		}
		sessions.clear();
	}

	private final static int DOWNLOAD_ARTICLE = 1;
	private final static int DOWNLOAD_IMAGE = 2;

	private void startDownloadOfflineItem(final int type, final String uri) {
		File f = null;
		if (type == DOWNLOAD_IMAGE)
			f = cacheService.getCacheOf(CacheNames.MY_IMAGE_CACHEDIR, uri);
		else
			f = cacheService.getCacheOf(CacheNames.NEWS_CONTENT_CACHEDIR, uri);

		HttpSession session = sessions.get(uri);
		if (session == null) {
			session = httpService.download(uri, f);
			session.start();
			sessions.put(uri, session);
			session.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {

				@Override
				public void process(Object sender,
									StateChangedEventArgs args) {
					if (args.getState() == HttpSession.STATE_FINISHED) {
						HttpSession session = sessions.get(uri);
						if (session != null) {
							session.stop();
						}
						sessions.remove(uri);

						OfflineEventArgs oea;
						if (type == DOWNLOAD_IMAGE)
							oea = new OfflineEventArgs(OfflineService.STATUS_IMG_FINISHED, null, uri);
						else
							oea = new OfflineEventArgs(OfflineService.STATUS_ARTICLE_FINISHED, null, uri);

						offlineService.getNotificationEvent().fireEvent(null, oea);
					} else if (args.getState() == HttpSession.STATE_ERROR) {
						OfflineEventArgs oea;
						if (type == DOWNLOAD_IMAGE)
							oea = new OfflineEventArgs(OfflineService.STATUS_IMG_FINISHED, null, uri);
						else
							oea = new OfflineEventArgs(OfflineService.STATUS_ARTICLE_FINISHED, null, uri);

						offlineService.getNotificationEvent().fireEvent(null, oea);
					}
				}

			});

		}
	}

	@Override
	public void startDownloadOfflineImage(final String uri) {
		startDownloadOfflineItem(DOWNLOAD_IMAGE, uri);
	}


	@Override
	public void startDownloadOfflineArticle(final String uri) {
		startDownloadOfflineItem(DOWNLOAD_ARTICLE, uri);
	}

	@Override
	public void startDownloadUpdateFile(final String uri) {
		File f = cacheService.getCacheOf(CacheNames.UPDATE_CACHEDIR, uri);

		if (upgradeSession == null) {

			upgradeSession = httpService.download(uri, f);
			upgradeSession.start();

			upgradeSession.getStateChangedEvent().addEventHandler(new IEventHandler<StateChangedEventArgs>() {

				@Override
				public void process(Object sender,
									StateChangedEventArgs args) {
					if (args.getState() == HttpSession.STATE_FINISHED) {
						int dl = (int) upgradeSession.getDownloadedLength();
						int cl = (int) upgradeSession.getContentLength();
						int per = upgradeSession.getPercent();
						if (upgradeSession != null) {
							upgradeSession.stop();
						}
						UpgradeEventsArgs uea = new UpgradeEventsArgs(UpdateService.UPDATE_DOWNLOADED, per, dl, cl);
						updateService.getNotificationEvent().fireEvent(args.getSession(), uea);

					} else if (args.getState() == HttpSession.STATE_CONTENT_RECEIVING || args.getState() == HttpSession.STATE_HEADER_RECEIVED) {
						int per = upgradeSession.getPercent();
						int dl = (int) upgradeSession.getDownloadedLength();
						int cl = (int) upgradeSession.getContentLength();

						UpgradeEventsArgs uea = new UpgradeEventsArgs(UpdateService.UPDATE_DOWNLOADING, per, dl, cl);
						updateService.getNotificationEvent().fireEvent(args.getSession(), uea);

					}
				}

			});


		}
	}

	@Override
	public void stopDownloadUpdateFile() {
		if (upgradeSession != null) {
			upgradeSession.stop();
		}
	}


	private void initAdPools()
	{
		int adCount = dataService.getAllCategories().getItemCount() * configService.getNativeAdCount();
		AdHelper.getInstance(App.get()).initFacebookNativeAd(adCount);
		AdHelper.getInstance(App.get()).initGmobiNativeAd(adCount);
		AdHelper.getInstance(App.get()).initArticleNativeAd(adCount);
	}


}


