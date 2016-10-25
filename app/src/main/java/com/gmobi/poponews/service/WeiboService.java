package com.gmobi.poponews.service;

import android.os.Bundle;
import android.util.Log;

import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.share.AccessTokenKeeper;
import com.gmobi.poponews.share.WeiboConstants;
import com.momock.app.App;
import com.momock.app.CaseActivity;
import com.momock.data.DataList;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Administrator on 6/19 0019.
 */
public class WeiboService implements IWeiboService {
	@Inject
	IMessageService messageService;
	@Inject
	ISocialDataService socialDataService;
	@Inject
	IUITaskService uiTaskService;

	private final String TAG = "Weibo";


	private final int ACCOUNT_LIMIT_COUNT = 200;
	private final String SERVICE_TYPE = SocialExtra.SOCIAL_TYPE_WEIBO;
	private Oauth2AccessToken mAccessToken;
	private JSONObject selfJo;


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



/*
		TwitterCore.getInstance().getApiClient(session).getStatusesService().userTimeline(
				Long.parseLong(sa.getId()), null, 25,
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
				});*/


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
		remoteGetAllAccountPosts(true);
	}


	@Override
	public void remoteGetAccountList(final int From) {
		if (hasFetchedList())
			return;


		FriendshipsAPI friendshipsAPI = new FriendshipsAPI(App.get(), WeiboConstants.APP_KEY, mAccessToken);
		friendshipsAPI.friends(Long.parseLong(mAccessToken.getUid()), 200, 0, false, new RequestListener() {
			@Override
			public void onComplete(String s) {
				Log.e("Weibo", s);

				try {
					final JSONObject jo = new JSONObject(s);


					uiTaskService.run(new Runnable() {

						@Override
						public void run() {
							int ret = 0;
							try {

								ret = procFriendsList(jo.getJSONArray("users"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if (From == ISocialService.FROM_MAIN)
								messageService.send(this, MessageTopics.GET_WEIBO_CHANNEL_MAIN, ret);
							else
								messageService.send(this, MessageTopics.GET_WEIBO_CHANNEL_SETTING, ret);

						}
					});


				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onWeiboException(WeiboException e) {
				Log.e("Weibo", e.getMessage());
			}
		});


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

		DataList<SocialPost> mergeList = new DataList<>();


		boolean status = SocialSetting.getStatus(SERVICE_TYPE);

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
			sp.setReleasetime(0L);

			sp.setVisible(status ? 1 : 0);

			mergeList.addItem(sp);


		}


		Logger.debug("2.tweetList count=" + mergeList.getItemCount());

		socialDataService.addIntoPostList(mergeList);

		return RET_HAS_FRIENDS_POST;
	}


	@Override
	public boolean isLogged() {
		return true;
	}

	@Override
	public boolean isBinded() {
		return false;
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

		MainActivity.mSsoHandler.authorize(new WeiboAuthListener() {
			@Override
			public void onComplete(Bundle values) {
				mAccessToken = Oauth2AccessToken.parseAccessToken(values); // 从 Bundle 中解析 Token

				if (mAccessToken.isSessionValid()) {
					AccessTokenKeeper.writeAccessToken(App.get(), mAccessToken); //保存Token

					remoteGetAccountList(from);


				} else {
					String code = values.getString("code", "");
					Logger.error(code);
				}
			}

			@Override
			public void onWeiboException(WeiboException e) {

			}

			@Override
			public void onCancel() {

			}
		});


	}

	@Override
	public boolean doBind(Object extra) {
		return false;
	}

	@Override
	public void doUnbind(Object extra) {

	}

	@Override
	public void doLogout(Object extra) {
		socialDataService.HideTypeInAccList(SERVICE_TYPE);
		socialDataService.HideTypeInPostList(SERVICE_TYPE);

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


	private String title;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

}


