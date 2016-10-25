package com.gmobi.poponews.service;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.UserInfoCallBack;
import com.momock.app.App;
import com.momock.app.CaseActivity;
import com.momock.message.Message;
import com.momock.service.IMessageService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by Administrator on 5/4 0004.
 */
public class CherryUserService implements IUserService{
	@Inject
	IMessageService messageService;
	@Inject
	IConfigService configService;

	private final static String AVATAR_URL = "http://graph.facebook.com/{id}/picture?type=large";
	public static final String BASE_LOGIN_URL = "http://api.poponews.net/api/user/";
	private final static String BASE_OTHER_LOGIN = "http://api.poponews.net/api/user/otherlogin";

	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{IMessageService.class,IConfigService.class};
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


	private void procMe(JSONObject dataJo) {
		try {
			JSONObject userinfo = new JSONObject();

			if(dataJo.has("id")) {
				String id = dataJo.getString(SocialAccount.ID);
				userinfo.put("id", id);

				String avatar = AVATAR_URL.replace("{id}",id);
				userinfo.put("avatar", avatar);
			}

			if(dataJo.has("name")) {
				String name = dataJo.getString(SocialAccount.NAME);
				userinfo.put("name", name);

			}

			if(dataJo.has("gender")) {
				String gender = dataJo.getString("gender");
				userinfo.put("sex", gender);

			}

			if(dataJo.has("age_range")) {
				JSONObject ageJo = dataJo.getJSONObject("age_range");
				userinfo.put("age", ageJo);

			}

			if(dataJo.has("email")) {
				String email = dataJo.getString(SocialAccount.EMAIL);
				userinfo.put("email", email);

			}




			configService.setFacebookUserInfo(userinfo.toString());

			messageService.send(this, MessageTopics.USER_LOGIN_SUCCESS);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void remoteGetMe() {
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
						if (graphResponse.getError() != null)
							Logger.error("[Facebook]:" + graphResponse.getError().getErrorMessage());

						try {
							procMe(new JSONObject(graphResponse.getRawResponse()));
						} catch (JSONException e) {

							e.printStackTrace();
						}

					}
				}
		).executeAsync();
	}
	@Override
	public void doLogin(Object extra) {
		CaseActivity activity = (CaseActivity) extra;
		if (AccessToken.getCurrentAccessToken() != null) {
			Logger.debug("AccessToken.getCurrentAccessToken() is not null = " + AccessToken.getCurrentAccessToken().getToken());
			remoteGetMe();
		} else {
			Logger.debug("AccessToken.getCurrentAccessToken() is null");
			LoginManager.getInstance().registerCallback(MainActivity.callbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					Set<String> pm = AccessToken.getCurrentAccessToken().getPermissions();
					Logger.error(pm.toString());

					remoteGetMe();

					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK,AnalysisUtil.RESULT_SUCCESS);
				}

				@Override
				public void onCancel() {
					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK,AnalysisUtil.RESULT_CANCEL);
					Log.e("facebook", "onCancel");
					messageService.send(this, MessageTopics.USER_LOGIN_CANCEL);
				}

				@Override
				public void onError(FacebookException exception) {
					AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_FACEBOOK,AnalysisUtil.RESULT_FAIL);
					Log.e("facebook", "onError:" + exception.getMessage());
					messageService.send(this, MessageTopics.USER_LOGIN_FAIL);
				}
			});


			LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("user_friends",  "user_likes"));
		}
	}

	@Override
	public boolean isLogged() {
		return (AccessToken.getCurrentAccessToken() != null);
	}

	@Override
	public void doThirdLogin(String openid, String name, String avatar, String email, String uid, String type, boolean bind) {

	}

	@Override
	public void doLocalLogin(String email, String name, String pwd, boolean bind) {

	}


	CommentUserInfo info;




	@Override
	public void doRegister(CommentUserInfo info, UserInfoCallBack callBack) {

	}

	@Override
	public void doUnBind(String uid, String type, String openId) {

	}


	@Override
	public void setLogin(boolean isLogin) {
		if (info != null){
			info.setLogin(isLogin);
		}
	}

	@Override
	public boolean isLogin() {
		if (info == null){
			return false;
		}
		return info.isLogin();
	}

	@Override
	public void setUserInfo(CommentUserInfo userInfo) {
		if (userInfo != null){
			info = userInfo;
		}
	}

	@Override
	public CommentUserInfo getUserInfo() {
		if (info != null){
			return info;
		}
		return null;
	}


	private CommentUserInfo login(CommentUserInfo userInfo,String type){
		CommentUserInfo info = null;
		HttpURLConnection conn = null;
		URL url = null;
		try {
			url = new URL(BASE_OTHER_LOGIN);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.connect();

			// POST请求
			DataOutputStream out = new DataOutputStream(
					conn.getOutputStream());
			JSONObject obj = new JSONObject();
			if (userInfo.getEmail() != null){
				obj.put("email", userInfo.getEmail());
			}
			if (userInfo.getUserName() != null){
				obj.put("name",userInfo.getUserName());
			}
			obj.put("type",type);
			obj.put("openid",userInfo.getUId());
//			obj.put("avatar", GlobalConfig.TEST_IMG_URL);
			obj.put("update",true);
			out.flush();
			out.writeBytes(obj.toString());
			out.close();
			Log.i("oye", "提交 ---" + obj.toString());
			if (conn.getResponseCode() == 200) {
				// 读取响应
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				String lines;
				StringBuffer sb = new StringBuffer("");
				while ((lines = reader.readLine()) != null) {
					lines = new String(lines.getBytes(), "utf-8");
					sb.append(lines);
				}
				reader.close();
				Log.i("oye","back--"+sb.toString());
				info = new CommentUserInfo();
				JSONObject jsonObject = new JSONObject(sb.toString());
				JSONObject data = jsonObject.getJSONObject("data");
				JSONArray primary = data.getJSONArray("primary");
				JSONObject object = primary.getJSONObject(0);
				JSONObject user = data.getJSONObject("user");
				info.setUId(user.optString("_id"));
				info.setEmail(object.optString("email"));
				info.setUserName(user.optString("name"));
				info.setLogin(true);
				return info;
			} else {
				Log.i("oye", "响应错误------" + conn.getResponseCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 断开连接
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return null;
	}


}

