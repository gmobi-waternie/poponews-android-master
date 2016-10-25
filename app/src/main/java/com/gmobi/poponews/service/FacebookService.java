package com.gmobi.poponews.service;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.TimeUtil;
import com.momock.app.CaseActivity;
import com.momock.data.DataList;
import com.momock.data.IDataView;
import com.momock.service.IMessageService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Created by Administrator on 6/19 0019.
 */
public class FacebookService implements IFacebookService {
	@Inject
	IMessageService messageService;
	@Inject
	ISocialDataService socialDataService;
	@Inject
	IConfigService configService;

	private final String TAG = "Facebook";


	private final int ACCOUNT_LIMIT_COUNT = 256;
	private final String SERVICE_TYPE = SocialExtra.SOCIAL_TYPE_FACEBOOK;

	private final static String AVATAR_URL = "http://graph.facebook.com/{id}/picture?type=large";

	private DataList<SocialPost> mergeList = new DataList<>();
	private DataList<SocialPost> postsList = new DataList<>();
	private int ACCOUNT_PER_FETCH = 20;
	private int curCountInProc = 0;

	private int SHOW_COUNT = 3;

	private int totalFetchCount = 0;
	private int totalFetchedCount = 0;
	private static ExecutorService executorService = Executors.newFixedThreadPool(10);

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

	/**
	 * 解析出url参数中的键值对
	 * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
	 *
	 * @param URL url地址
	 * @return url请求参数部分
	 */
	public static Map<String, String> getURLParam(String URL) {
		Map<String, String> mapRequest = new HashMap<String, String>();

		String[] arrSplit = null;

		String strUrlParam = URL;
		if (strUrlParam == null) {
			return mapRequest;
		}
		//每个键值为一组
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = null;
			arrSplitEqual = strSplit.split("[=]");

			//解析出键值
			if (arrSplitEqual.length > 1) {
				//正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

			} else {
				if (arrSplitEqual[0] != "") {
					//只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}


	private Bundle createPageParam(String nextUrl) {
		Bundle para = new Bundle();
		para.putString("fields", "id,link,name,type,caption,from,picture,created_time,full_picture");
		if (nextUrl != null) {
			Map<String, String> urlParams = getURLParam(nextUrl);
			if (urlParams.containsKey("until"))
				para.putLong("until", Long.valueOf(urlParams.get("until")));

			if (urlParams.containsKey("__paging_token"))
				para.putString("__paging_token", urlParams.get("__paging_token"));
		}
		return para;

	}


	@Override
	public void remoteGetAccountPosts(final SocialAccount sa, final String nextUrl) {

		final String id = sa.getId();
		sa.setFetchStatus(SocialAccount.STATUS_UPDATING);
		String request = "/" + id + "/posts";
		Bundle para = createPageParam(nextUrl);

		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				request, para, HttpMethod.GET,
				new GraphRequest.Callback() {
					@Override
					public void onCompleted(GraphResponse graphResponse) {
						if (graphResponse.getError() != null) {
							Logger.error("id:" + id + " Error:" + graphResponse.getError().getErrorMessage());
							sa.setFetchStatus(SocialAccount.STATUS_IDLE);
							messageService.send(this, MessageTopics.GET_FACEBOOK_POST);
							return;
						} else {
							Logger.error(graphResponse.getJSONObject().toString());

						}


						try {
							procFriendPosts(id, graphResponse.getJSONObject().getJSONArray("data"));
							procPageNext(id, graphResponse.getJSONObject().getJSONObject("paging"));

						} catch (JSONException e) {
							e.printStackTrace();
						}
						Log.e("Facebook", "Facebook sa=" + sa.getName());
						sa.setFetchStatus(SocialAccount.STATUS_IDLE);
						messageService.send(this, MessageTopics.GET_FACEBOOK_POST);
					}
				}
		).executeAsync();


	}


	@Override
	public void remoteGetAllAccountPosts(boolean next) {
		Collection<GraphRequest> requests = new ArrayList<>();
		DataList<SocialAccount> accountList = (DataList<SocialAccount>) socialDataService.getAccList(SERVICE_TYPE);


		for (int i = 0; i < accountList.getItemCount(); i++) {
			SocialAccount sa = accountList.getItem(i);
			if (sa.isSelected())
				totalFetchCount++;
		}


		for (int i = 0; i < accountList.getItemCount(); i++) {
			SocialAccount sa = accountList.getItem(i);
			if (!sa.isSelected())
				continue;
			remoteGetAccountPosts(sa, next ? sa.getNext() : null);
		}
	}

	@Override
	public void remoteGetNextPage() {
		if (providerData() <= 0)
			remoteGetAllAccountPosts(true);
	}


	@Override
	public void remoteGetAccountList(final int From) {
		Logger.debug("remoteGetLikeList:" + AccessToken.getCurrentAccessToken().getToken());
		String request = "/" + AccessToken.getCurrentAccessToken().getUserId() +
				"/likes";
		if (hasFetchedList())
			return;

		Bundle para = new Bundle();
		para.putInt("limit", ACCOUNT_LIMIT_COUNT);

		new GraphRequest(
				AccessToken.getCurrentAccessToken(), request,
				para, HttpMethod.GET,
				new GraphRequest.Callback() {
					@Override
					public void onCompleted(GraphResponse graphResponse) {
						int ret = 0;
						String next = null;
						if (graphResponse.getError() != null) {
							Logger.error(graphResponse.getError().getErrorMessage());
							return;
						} else {
							Logger.error(graphResponse.getJSONObject().toString());


							try {
								ret = procLikeList(graphResponse.getJSONObject().getJSONArray("data"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						setFetchedList(true);

						if (From == ISocialService.FROM_MAIN)
							messageService.send(this, MessageTopics.GET_FACEBOOK_LIKES_MAIN, ret);
						else
							messageService.send(this, MessageTopics.GET_FACEBOOK_LIKES_SETTING, ret);

					}
				}
		).executeAsync();


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

	@Override
	public void remoteGetFriendsList() {
		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/me/friends",
				null,
				null,
				new GraphRequest.Callback() {
					@Override
					public void onCompleted(GraphResponse graphResponse) {
						int ret = 0;
						Logger.error(graphResponse.getJSONObject().toString());
						try {
							ret = procFriendsList(graphResponse.getJSONObject().getJSONArray("data"));
						} catch (JSONException e) {
							e.printStackTrace();
						}

						messageService.send(this, MessageTopics.GET_FACEBOOK_FRIENDS, ret);
					}
				}
		).executeAsync();
	}


	@Override
	public void remoteGetMe(String uid,final int from) {
		Logger.error(AccessToken.getCurrentAccessToken().getToken());

		Bundle para = new Bundle();
		para.putString("fields", "id,name,gender,age_range,email");

		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				"/me",
				para,
				null,
				new GraphRequest.Callback() {
					@Override
					public void onCompleted(GraphResponse graphResponse) {
						int ret = 0;

						Logger.error("[Facebook]:" + graphResponse.getRawResponse());
						if (graphResponse.getError() != null) {
							Logger.error("[Facebook]:" + graphResponse.getError().getErrorMessage());
							return;
						}

						try {
							procMe(new JSONObject(graphResponse.getRawResponse()),from);
							if(from == ISocialService.FROM_LOGIN)
								messageService.send(this,MessageTopics.GET_FACEBOOK_ME_LOGIN);
							else if (from == ISocialService.FROM_BIND)
								messageService.send(this,MessageTopics.GET_FACEBOOK_ME_BIND,SocialExtra.SOCIAL_TYPE_FACEBOOK);
						} catch (JSONException e) {

							e.printStackTrace();
						}

						//if(ret > 0)
						//	remoteGetAccountPosts(meId,null);


					}
				}
		).executeAsync();
	}


	public final static int RET_NO_FRIENDS = -1;
	public final static int RET_NO_FRIENDS_POST = -2;
	public final static int RET_HAS_FRIENDS = 1;
	public final static int RET_HAS_FRIENDS_POST = 2;

	public final static int RET_NO_LIKES = -1;
	public final static int RET_HAS_LIKES = 1;

//		{
//			"id":"174621222901914",
//			"name":"James Faust","gender":"male","age_range":{"min":21},
//			"email":"jamesfaust.56@rediffmail.com"
//		}

	private void procMe(JSONObject dataJo,int from) {
		try {
			JSONObject userinfo = new JSONObject();

			if (dataJo.has("id")) {
				String id = dataJo.getString(SocialAccount.ID);
				userinfo.put("id", id);

				String avatar = AVATAR_URL.replace("{id}", id);
				userinfo.put("avatar", avatar);
			}

			if (dataJo.has("name")) {
				String name = dataJo.getString(SocialAccount.NAME);
				userinfo.put("name", name);
			}

			if (dataJo.has("gender")) {
				String gender = dataJo.getString("gender");
				userinfo.put("sex", gender);
			}

			if (dataJo.has("age_range")) {
				JSONObject ageJo = dataJo.getJSONObject("age_range");
				userinfo.put("age", ageJo);
			}

			if (dataJo.has("email")) {
				String email = dataJo.getString(SocialAccount.EMAIL);
				userinfo.put("email", email);
			}

			configService.insertThirdUserInfo(SocialExtra.SOCIAL_TYPE_FACEBOOK,userinfo.toString());
			configService.setFacebookUserInfo(userinfo.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	private int procFriendsList(JSONArray dataJa) {
		if (dataJa.length() == 0)
			return RET_NO_FRIENDS;

		DataList<SocialAccount> accountList = new DataList<>();

		for (int i = 0; i < dataJa.length(); i++) {

			try {
				JSONObject friendJo = dataJa.getJSONObject(i);
				SocialAccount sa = new SocialAccount();
				String id = friendJo.getString(SocialAccount.ID);
				String name = friendJo.getString(SocialAccount.NAME);
				sa.setId(id);
				sa.setName(name);
				sa.setRole(SocialAccount.ROLE_FRIEND);
				sa.setRssurl("");
				sa.setRssurls(null);
				sa.setSelect(false);
				sa.setVisible(1);
				sa.setExtra("");

				accountList.addItem(sa);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		socialDataService.addIntoAccList(accountList);
		return RET_HAS_FRIENDS;
	}


	private int procLikeList(JSONArray dataJa) {
		if (dataJa.length() == 0)
			return RET_NO_LIKES;

		DataList<SocialAccount> accountList = new DataList<>();
		for (int i = 0; i < dataJa.length(); i++) {

			try {
				JSONObject friendJo = dataJa.getJSONObject(i);
				SocialAccount sa = new SocialAccount();
				String id = friendJo.getString(SocialAccount.ID);
				String name = friendJo.getString(SocialAccount.NAME);
				sa.setId(id);
				sa.setName(name);
				sa.setNext("");
				sa.setRssurl("");
				sa.setRssurls(null);
				sa.setSocialtype(SERVICE_TYPE);

				sa.setVisible(1);
				sa.setRole(SocialAccount.ROLE_PUBLIC_LIKE);
				sa.setExtra("");
				boolean s = SocialSetting.getCategorySelect(SERVICE_TYPE, id);
				sa.setSelect(s);

				accountList.addItem(sa);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		socialDataService.addIntoAccList(accountList);

		return RET_HAS_LIKES;
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

	private synchronized int procFriendPosts(String aid, JSONArray dataJa) {
		if (dataJa.length() == 0)
			return RET_NO_FRIENDS_POST;

		SocialAccount fa = socialDataService.findAccount(SERVICE_TYPE, aid);
		if (fa != null && !fa.isSelected())
			return RET_NO_FRIENDS_POST;


		boolean status = SocialSetting.getStatus(SERVICE_TYPE);


		int addPostCount = 0;
		for (int i = 0; i < dataJa.length(); i++) {
			try {
				JSONObject friendJo = dataJa.getJSONObject(i);
				SocialPost fp = new SocialPost();
				String type = friendJo.getString(SocialPost.TYPE);
				String appname = "";
				String appid = "";
				if (friendJo.has("application")) {
					appname = friendJo.getJSONObject("application").getString(SocialPost.NAME);
					appid = friendJo.getJSONObject("application").getString(SocialPost.ID);
				}

				Logger.debug("post appname=" + appname + "  type=" + type);
				if (!SocialPost.isPoponewsLink(type, appname))
					continue;


				String id = friendJo.getString(SocialPost.ID);
				String fromid = friendJo.getJSONObject("from").getString(SocialPost.ID);
				String fromname = friendJo.getJSONObject("from").getString(SocialPost.NAME);

				String link = "";
				if (friendJo.has(SocialPost.LINK))
					link = friendJo.getString(SocialPost.LINK);


				String picture = friendJo.getString(SocialPost.FULLPICTURE);

				String caption = "";
				if (friendJo.has(SocialPost.CAPTION))
					caption = friendJo.getString(SocialPost.CAPTION);

				String name = friendJo.getString(SocialPost.NAME);
				String time = friendJo.getString("created_time");

				Logger.debug("post caption=" + caption + "  link=" + link);

				fp.setId(id);
				fp.setName(name);
				fp.setLink(link);
				fp.setPicture(picture, SERVICE_TYPE);
				fp.setCaption(caption);
				fp.setFromid(fromid);
				fp.setFromname(fromname);
				fp.setAppname(appname);
				fp.setAppid(appid);
				fp.setFromAvatar(fromid, SERVICE_TYPE);
				fp.setSocialtype(SERVICE_TYPE);
				fp.setReleasetime(TimeUtil.getInstance().getUtcFromFacebookTime(time));


				fp.setVisible(status ? 1 : 0);

				postsList.addItem(fp);
				addPostCount++;
				Logger.debug("1.polist count=" + postsList.getItemCount());
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}


		Logger.debug("2.polist count=" + postsList.getItemCount());
		mergeList.removeAllItems();
		if (postsList.getItemCount() > ACCOUNT_PER_FETCH && !uiShow) {
			providerData();

			uiShow = true;
		}

		AnalysisUtil.recordSnsFetch(SocialExtra.SOCIAL_TYPE_FACEBOOK, addPostCount + "");

		/*

		totalFetchedCount++;
		if (totalFetchCount - totalFetchedCount < ACCOUNT_PER_FETCH && curCountInProc == 0) {
			ACCOUNT_PER_FETCH = totalFetchCount - totalFetchedCount;
			Logger.error("1 = ACCOUNT_PER_FETCH = " + ACCOUNT_PER_FETCH);
		}


		if (totalFetchCount - totalFetchedCount < ACCOUNT_PER_FETCH && curCountInProc == 0) {
			socialDataService.addIntoPostList(mergeList);
			mergeList.removeAllItems();

			Logger.error("3 = curCountInProc = " + curCountInProc);
		} else {
			curCountInProc++;
			Logger.error("4= curCountInProc = " + curCountInProc);
			//如果
			if (curCountInProc >= ACCOUNT_PER_FETCH) {
				curCountInProc = 0;
				socialDataService.addIntoPostList(mergeList);
				mergeList.removeAllItems();
			}

		}*/


		return RET_HAS_FRIENDS_POST;
	}


	@Override
	public boolean isLogged() {
		return (AccessToken.getCurrentAccessToken() != null);
	}

	@Override
	public boolean isBinded() {
		return (configService.getThirdUserInfo(SocialExtra.SOCIAL_TYPE_GOOGLE) != null);
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
		if (AccessToken.getCurrentAccessToken() != null) {
			Logger.debug("AccessToken.getCurrentAccessToken() is not null = " + AccessToken.getCurrentAccessToken().getToken());
			socialDataService.ShowTypeInAccList(SERVICE_TYPE);
			socialDataService.ShowTypeInPostList(SERVICE_TYPE);

			remoteGetMe("",from);

			remoteGetAccountList(from);
		} else {
			Logger.debug("AccessToken.getCurrentAccessToken() is null");
			LoginManager.getInstance().registerCallback(MainActivity.callbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					Set<String> pm = AccessToken.getCurrentAccessToken().getPermissions();
					Logger.error(pm.toString());

					remoteGetMe("",from);

					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK, AnalysisUtil.RESULT_SUCCESS);
					remoteGetAccountList(from);

				}

				@Override
				public void onCancel() {
					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK, AnalysisUtil.RESULT_CANCEL);
					Log.e("facebook", "onCancel");
					messageService.send(this, MessageTopics.LOGIN_FACEBOOK_FAIL);
				}

				@Override
				public void onError(FacebookException exception) {
					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK, AnalysisUtil.RESULT_FAIL);
					Log.e("facebook", "onError:" + exception.getMessage());
					messageService.send(this, MessageTopics.LOGIN_FACEBOOK_FAIL);
				}
			});


			LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("user_friends", "user_likes"));
		}
	}

	@Override
	public boolean doBind(Object extra) {

		if (isBinded())
			return true;

		doLogin(extra, ISocialService.FROM_LOGIN);
		return false;
	}

	@Override
	public void doUnbind(Object extra) {
		configService.removeThirdUserInfo(SocialExtra.SOCIAL_TYPE_FACEBOOK);
	}

	@Override
	public void doLogout(Object extra) {
		socialDataService.HideTypeInAccList(SERVICE_TYPE);
		socialDataService.HideTypeInPostList(SERVICE_TYPE);
		AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK, AnalysisUtil.RESULT_LOGOUT);
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


	private synchronized int providerData() {
		int count = postsList.getItemCount();
		int getCount = ACCOUNT_PER_FETCH;

		if (count <= 0)
			return 0;

		if (count < getCount) {
			getCount = count;
		}


		mergeList.removeAllItems();
		for (int i = 0; i < getCount; i++) {
			mergeList.addItem(postsList.getItem(i));

		}
		for (int i = 0; i < getCount; i++) {
			postsList.removeItem(mergeList.getItem(i));
		}


		messageService.send(this, MessageTopics.GET_PART_OF_POST);
		socialDataService.addIntoPostList(mergeList);
		return 1;
	}

}

