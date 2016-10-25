package com.gmobi.poponews.service;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.HMACSHA1;
import com.gmobi.poponews.util.TimeUtil;
import com.momock.app.CaseActivity;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.data.IDataView;
import com.momock.outlet.card.ICardOutlet;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.internal.oauth.OAuth1aHeaders;
import com.twitter.sdk.android.core.models.Tweet;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by Administrator on 6/19 0019.
 */
public class TwitterService implements ITwitterService {
	@Inject
	IMessageService messageService;
	@Inject
	ISocialDataService socialDataService;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IConfigService configService;

	private final String TAG = "Twitter";


	private DataList<SocialPost> mergeList = new DataList<>();
	private DataList<SocialPost> postsList = new DataList<>();
	private int ACCOUNT_PER_FETCH = 20;
	private final int ACCOUNT_LIMIT_COUNT = 256;
	private final String SERVICE_TYPE = SocialExtra.SOCIAL_TYPE_TWITTER;
	private TwitterSession session;

	private static boolean uiShow = false;
	//private DataList<SocialAccount> accountList = new DataList<SocialAccount>();

	String meId;

	private IDataView<SocialAccount> accountView;


	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{IMessageService.class};
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public boolean canStop() {
		return false;
	}



	@Override
	public void remoteGetAccountPosts(final SocialAccount sa, String nextUrl) {
		final String id = sa.getId();
		sa.setFetchStatus(SocialAccount.STATUS_UPDATING);

		if (session == null)
			session = Twitter.getSessionManager().getActiveSession();

		TwitterCore.getInstance().getApiClient(session).getStatusesService().userTimeline(
				Long.parseLong(sa.getId()), null,  25,
				null, null, null, null, null, null, new Callback<List<Tweet>>() {
					@Override
					public void success(Result<List<Tweet>> result) {
						final List<Tweet> rsp = result.data;

						uiTaskService.run(new Runnable() {

							@Override
							public void run() {
								procFriendTweet(sa.getId(), rsp);
							}
						});



						sa.setFetchStatus(SocialAccount.STATUS_IDLE);
						messageService.send(this, MessageTopics.GET_TWITTER_POST);

					}

					@Override
					public void failure(TwitterException e) {

						Logger.error("Twitter Error:" + e.getMessage());
						sa.setFetchStatus(SocialAccount.STATUS_IDLE);
						messageService.send(this, MessageTopics.GET_TWITTER_POST);
					}
				});


	}


	@Override
	public void remoteGetAllAccountPosts(boolean next) {
		DataList<SocialAccount> accountList = (DataList<SocialAccount>) socialDataService.getAccList(SERVICE_TYPE);

		for (int i = 0; i < accountList.getItemCount(); i++) {
			SocialAccount sa = accountList.getItem(i);
			if (!sa.isSelected())
				continue;
			remoteGetAccountPosts(sa, next ? sa.getNext() : null);
		}
	}

	@Override
	public void remoteGetNextPage() {

		if(providerData() <= 0)
			remoteGetAllAccountPosts(true);

	}


	@Override
	public void remoteGetAccountList(final int from) {
		if (hasFetchedList() && from != ISocialService.FROM_LOGIN)
			return;

		if (session == null)
			session = Twitter.getSessionManager().getActiveSession();

		TwitterAuthConfig config = TwitterCore.getInstance().getAuthConfig();

		Map<String, String> param = new HashMap<>();
		param.put("cursor", "-1");
		param.put("screen_name", session.getUserName());
		param.put("skip_status", "true");
		param.put("include_user_entities", "false");

		String authHeader = (new OAuth1aHeaders()).getAuthorizationHeader(config, session.getAuthToken(), null, "GET", "https://api.twitter.com/1.1/friends/list.json", param);
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Authorization", authHeader);
		Log.e("Twitter", "authHeader = " + authHeader);

		SocialHttpService.connectWithHeader(headers, "https://api.twitter.com/1.1/friends/list.json?cursor=-1&screen_name=" + session.getUserName() + "&skip_status=true&include_user_entities=false", new SocialHttpService.ICallback() {
			@Override
			public void onResult(final JSONObject dn) {
				Log.e("Twitter", dn.toString());
				if (!dn.has("users")) {
					messageService.send(this, MessageTopics.GET_TWITTER_CHANNEL_FAIL, -1);
					return;
				}

				uiTaskService.run(new Runnable() {

					@Override
					public void run() {
						int ret = 0;
						try {
							ret = procFriendsList(dn.getJSONArray("users"));
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (from == ISocialService.FROM_MAIN)
							messageService.send(this, MessageTopics.GET_TWITTER_CHANNEL_MAIN, ret);
						else if(from == ISocialService.FROM_SETTING)
							messageService.send(this, MessageTopics.GET_TWITTER_CHANNEL_SETTING, ret);
						else if(from == ISocialService.FROM_LOGIN || from == ISocialService.FROM_BIND) {
							IDataList<SocialAccount> accList = socialDataService.getAccList(SocialExtra.SOCIAL_TYPE_TWITTER);
							if (accList != null) {
								TwitterSession session = Twitter.getSessionManager().getActiveSession();
								JSONObject jo  = new JSONObject();
								try {
									jo.put("id", session.getUserId());

									if(accList.getItemCount() > 0)
										jo.put("avatar", accList.getItem(0).getRssurl());
									else
										jo.put("avatar","");

									jo.put("name",session.getUserName());
									configService.insertThirdUserInfo(SocialExtra.SOCIAL_TYPE_TWITTER, jo.toString());
									if(from == ISocialService.FROM_LOGIN)
										messageService.send(this,MessageTopics.GET_TWITTER_ME_LOGIN);
									else if (from == ISocialService.FROM_BIND)
										messageService.send(this,MessageTopics.GET_TWITTER_ME_BIND,SocialExtra.SOCIAL_TYPE_TWITTER);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

						}
					}
				});


			}
		}, true);
	}


	private String processPageNext(JSONObject pageJo) {
		try {
			String nextUrl = pageJo.getString("next");
			String nextPageUrl = nextUrl.substring(nextUrl.indexOf("likes?"));

			return nextPageUrl;
		} catch (JSONException e) {

			e.printStackTrace();
			return null;
		}

	}


	public final static int RET_NO_FRIENDS = -1;
	public final static int RET_NO_FRIENDS_POST = -2;
	public final static int RET_HAS_FRIENDS = 1;
	public final static int RET_HAS_FRIENDS_POST = 2;


	private int procFriendsList(JSONArray dataJa) {
		if (dataJa.length() == 0)
			return RET_NO_FRIENDS;

		DataList<SocialAccount> accountList = new DataList<>();

		for (int i = 0; i < dataJa.length(); i++) {

			try {
				JSONObject friendJo = dataJa.getJSONObject(i);
				SocialAccount sa = new SocialAccount();
				String id = friendJo.getLong(SocialAccount.ID) + "";
				String name = friendJo.getString(SocialAccount.NAME);
				sa.setId(id);
				sa.setName(name);
				sa.setNext("");
				sa.setRole(SocialAccount.ROLE_FOLLOWING);
				sa.setRssurl(friendJo.getString("profile_image_url"));
				sa.setRssurls(null);

				sa.setVisible(1);
				sa.setExtra(friendJo.getInt("followers_count") + "");
				sa.setSocialtype(SERVICE_TYPE);
				sa.setFetchStatus(SocialAccount.STATUS_IDLE);

				boolean s = SocialSetting.getCategorySelect(SERVICE_TYPE, id);
				sa.setSelect(s);

				accountList.addItem(sa);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		socialDataService.addIntoAccList(accountList);
		return RET_HAS_FRIENDS;
	}


	private int procPageNext(String id, JSONObject paging) {
		if (paging == null)
			return RET_NO_FRIENDS_POST;

		if (!paging.has("next"))
			return RET_NO_FRIENDS_POST;

		try {
			String nextUrl = (String) paging.get("next");
			String nextPageUrl = nextUrl.substring(nextUrl.indexOf("posts?") + 6);
			Log.e(TAG, "nextPageUrl=" + nextPageUrl);

			SocialAccount fa = socialDataService.findAccount(SERVICE_TYPE, id);
			if (fa != null)
				fa.setNext(nextPageUrl);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return RET_HAS_FRIENDS_POST;

	}

	private int procFriendTweet(String aid, List<Tweet> data) {
		if (data.size() == 0)
			return RET_NO_FRIENDS_POST;

		SocialAccount fa = socialDataService.findAccount(SERVICE_TYPE, aid);
		if (fa != null && !fa.isSelected())
			return RET_NO_FRIENDS_POST;




		boolean status = SocialSetting.getStatus(SERVICE_TYPE);
		int addPostCount= 0;
		for (int i = 0; i < data.size(); i++) {

			Tweet tweet = data.get(i);
			SocialPost sp = new SocialPost();
			sp.setType("");

			sp.setId(tweet.idStr);
			sp.setPicture("", SocialExtra.SOCIAL_TYPE_TWITTER);
			sp.setFromid(tweet.user.profileImageUrl);
			sp.setFromname(fa.getName());
			sp.setName(tweet.text);
			sp.setSocialtype(SERVICE_TYPE);
			if (tweet.entities.media == null || tweet.entities.urls == null || tweet.entities.media.size() <= 0 || tweet.entities.urls.size() <= 0)
				continue;

			sp.setPicture(tweet.entities.media.get(0).mediaUrl, SERVICE_TYPE);
			sp.setLink(tweet.entities.urls.get(0).url);

			sp.setFromAvatar(tweet.user.profileImageUrl, SocialExtra.SOCIAL_TYPE_TWITTER);
			sp.setReleasetime(getTwitterDate(tweet.createdAt));

			sp.setVisible(status ? 1 : 0);

			postsList.addItem(sp);
			addPostCount++;

		}



		if(postsList.getItemCount() > ACCOUNT_PER_FETCH && !uiShow)
		{
			providerData();
			uiShow = true;
		}


		AnalysisUtil.recordSnsFetch(SocialExtra.SOCIAL_TYPE_TWITTER, addPostCount + "");
		return RET_HAS_FRIENDS_POST;
	}


	@Override
	public boolean isLogged() {
		return (Twitter.getSessionManager().getActiveSession() != null);
	}

	@Override
	public boolean isBinded() {
		return (configService.getThirdUserInfo(SocialExtra.SOCIAL_TYPE_TWITTER)!= null);
	}

	@Override
	public String getCacheData() {
		return null;
	}

	@Override
	public void setCacheData(String data) {

	}

	@Override
	public void doLogin(Object extra, final int from) {
		CaseActivity activity = (CaseActivity) extra;

		session = Twitter.getSessionManager().getActiveSession();
		final TwitterAuthClient twitterAuthClient = MainActivity.getTwitterAuthClient();
		if (session != null) {
			TwitterAuthToken authToken = session.getAuthToken();
			Log.e("Twitter", session.getUserName() + " is already login");
			socialDataService.ShowTypeInAccList(SERVICE_TYPE);
			socialDataService.ShowTypeInPostList(SERVICE_TYPE);

			requestEmail(twitterAuthClient, session);

			remoteGetAccountList(from);


		} else {


			twitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
				@Override
				public void success(final Result<TwitterSession> result) {
					session = result.data;
					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_TWITTER, AnalysisUtil.RESULT_SUCCESS);
					Log.e("Twitter", result.data.getUserName() + "login");

					requestEmail(twitterAuthClient, session);

					remoteGetAccountList(from);
				}

				@Override
				public void failure(final TwitterException e) {
					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_TWITTER, AnalysisUtil.RESULT_FAIL);
					messageService.send(this, MessageTopics.LOGIN_TWITTER_FAIL);
				}

			});
		}


	}

	@Override
	public boolean doBind(Object extra) {
		if(isBinded())
			return true;

		doLogin(extra,ISocialService.FROM_LOGIN);
		return false;
	}

	@Override
	public void doUnbind(Object extra) {
		configService.removeThirdUserInfo(SocialExtra.SOCIAL_TYPE_TWITTER);
	}

	@Override
	public void doLogout(Object extra) {
		socialDataService.HideTypeInAccList(SERVICE_TYPE);
		socialDataService.HideTypeInPostList(SERVICE_TYPE);
		AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_TWITTER, AnalysisUtil.RESULT_LOGOUT);
		setFetchedData(false);
		setFetchedList(false);
	}


	private static boolean hasFetchedData = false;
	private static boolean hasFetchedList = false;

	@Override
	public boolean hasFetchedData() {
		return hasFetchedData;
	}

	@Override
	public void setFetchedData(boolean f) {
		hasFetchedData = f;
	}

	@Override
	public boolean hasFetchedList() {
		return hasFetchedList;
	}

	@Override
	public void setFetchedList(boolean f) {
		hasFetchedList = f;
	}

	@Override
	public void syncData() {
		socialDataService.syncPostData(SERVICE_TYPE);
	}

	private static boolean enabled = false;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean e) {
		enabled = e;
	}


	/*
	* 判断下拉刷新、上拉刷新、未处于更新状态
	* */
	private static int updateStatus = NOT_UPDATE;

	@Override
	public int getUpdateStatus() {
		return updateStatus;
	}

	@Override
	public void setUpdateStatus(int u) {
		updateStatus = u;
	}


	private void oauthRequest(String url) {
		TwitterAuthConfig config = TwitterCore.getInstance().getAuthConfig();
		String oauth_consumer_key = config.getConsumerKey();
		String oauth_nonce = Base64.encodeToString(Long.toString(TimeUtil.getInstance().getCurUtcTime()).getBytes(), Base64.DEFAULT);
		String oauth_version = "1.0";
		String oauth_timestamp = Long.toString(TimeUtil.getInstance().getCurUtcTime());

		String BASE_SIGNATURE_STRING = " GET&https%3A%2F%2Fapi.twitter.com%2F1.1%2Ffriends%2Flist.json&cursor%3D-1%26include_user_entities%3Dfalse%26";
		BASE_SIGNATURE_STRING += "oauth_consumer_key%3D" + oauth_consumer_key + "%26";
		BASE_SIGNATURE_STRING += "oauth_nonce%3D4031dd8bca80e67c717ad681600450d0%26";
		BASE_SIGNATURE_STRING += "oauth_signature_method%3DHMAC-SHA1%26";
		BASE_SIGNATURE_STRING += "oauth_timestamp%3D" + oauth_timestamp + "%26";
		BASE_SIGNATURE_STRING += "oauth_token%3D" + session.getAuthToken().toString() + "%26";
		BASE_SIGNATURE_STRING += "oauth_version%3D1.0%26screen_name%3D" + session.getUserName() + "%26skip_status%3Dtrue";

		try {
			byte[] oauth_signature = HMACSHA1.getSignature(BASE_SIGNATURE_STRING.getBytes(), config.getConsumerSecret().getBytes());
			String oauth_signature_base_64 = Base64.encodeToString(oauth_signature, Base64.DEFAULT);
			Log.e("Twitter", "oauth_signature_base_64 = " + oauth_signature_base_64);
			Log.e("Twitter", "oauth_consumer_key = " + config.getConsumerKey());
			Log.e("Twitter", "oauth_consumer_sec = " + config.getConsumerSecret());


			Map<String, String> param = new HashMap<>();
			//"cursor=-1&screen_name=Waternie1&skip_status=true&include_user_entities=false"
			param.put("cursor", "-1");
			param.put("screen_name", session.getUserName());
			param.put("skip_status", "true");
			param.put("include_user_entities", "false");

			String authHeader = (new OAuth1aHeaders()).getAuthorizationHeader(config, session.getAuthToken(), null, "GET", "https://api.twitter.com/1.1/friends/list.json", param);
			Header[] headers = new Header[1];
			headers[0] = new BasicHeader("Authorization", authHeader);
			Log.e("Twitter", "authHeader = " + authHeader);

			SocialHttpService.connectWithHeader(headers, "https://api.twitter.com/1.1/friends/list.json?cursor=-1&screen_name=" + session.getUserName() + "&skip_status=true&include_user_entities=false", new SocialHttpService.ICallback() {
				@Override
				public void onResult(JSONObject dn) {
					Log.e("Twitter", dn.toString());
				}
			}, true);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public static long getTwitterDate(String date)
	{
		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
		sf.setLenient(true);
		try {
			Date d =  sf.parse(date);
			return d.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}

	private  String title;
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}



	private synchronized int providerData()
	{
		int count = postsList.getItemCount();
		int getCount = ACCOUNT_PER_FETCH;

		if(count <= 0)
			return 0;

		if(count < getCount)
		{
			getCount = count;
		}


		mergeList.removeAllItems();
		for(int i = 0; i< getCount; i++)
		{
			mergeList.addItem(postsList.getItem(i));

		}
		for(int i = 0; i< getCount; i++)
		{
			postsList.removeItem(mergeList.getItem(i));
		}



		messageService.send(this,MessageTopics.GET_PART_OF_POST);
		socialDataService.addIntoPostList(mergeList);
		return 1;
	}


	private void requestEmail(TwitterAuthClient twitterAuthClient, final TwitterSession session)
	{
		JSONObject userInfoJo = configService.getUserInfo();
		boolean hasChanged = true;

		if(userInfoJo.has("twitter"))
		{
			try {
				JSONObject twitterInfo = userInfoJo.getJSONObject("twitter");
				if(twitterInfo.getString("id").equals(session.getUserId()))
					hasChanged = false;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if(hasChanged) {
			twitterAuthClient.requestEmail(session, new Callback<String>() {
				@Override
				public void success(Result<String> result) {
					String email = result.data;
					String id = session.getUserId() + "";
					String name = session.getUserName();

					JSONObject userInfo = new JSONObject();
					try {
						userInfo.put("id", id);
						userInfo.put("name", name);
						userInfo.put("email", email);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					configService.setTwitterUserInfo(userInfo.toString());
				}

				@Override
				public void failure(TwitterException exception) {
					Logger.error("[Twitter]:requestEmail Error:"+exception.getMessage());
				}
			});
		}
	}

}


