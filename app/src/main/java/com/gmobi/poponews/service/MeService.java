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
import com.momock.event.IEventHandler;
import com.momock.message.Message;
import com.momock.service.IJsonService;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
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
public class MeService implements IUserService{
	@Inject
	IMessageService messageService;
	@Inject
	IConfigService configService;
	@Inject
	IUITaskService uiTaskService;

	private final static String AVATAR_URL = "http://graph.facebook.com/{id}/picture?type=large";
	public  final static String BASE_LOGIN_URL = "http://api.poponews.net/api/user/";
//	public  final static String BASE_LOGIN_URL = "http://192.168.1.125:6069/api/user/";


	private final static String LOGIN_SUFFIX = "login";
	private final static String REGISTER_SUFFIX = "register";
	private final static String THIRD_SUFFIX = "otherlogin";
	private final static String USER_UNBIND = "unbundling";


	private CommentUserInfo info;


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





	/**
	 * 三方登录Login
	 */
	@Override
	public void doThirdLogin(String openid,String name,String avatar,String email,String uid, final String type, final boolean bind) {

		JSONObject body = new JSONObject();
		try {
			if (email != null){
				body.put("email", email);
			}
			if(uid != null)
			{
				body.put("userid",uid);
			}
			body.put("type",type);
			body.put("openid",openid);
			if (avatar != null) {
				body.put("avatar", avatar);
			}
			body.put("name",name);
			body.put("update",bind);

		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}

		App.get().getService(IJsonService.class).post(BASE_LOGIN_URL+THIRD_SUFFIX, body, null, new IEventHandler<IJsonService.JsonEventArgs>() {
			@Override
			public void process(Object o, IJsonService.JsonEventArgs args) {
				if(args!=null)
				{
					try {
						JSONObject rspJo = new JSONObject(args.getResponse());
						Log.i("oye","request--"+rspJo.toString());
						boolean ret = procLoginResponse(rspJo);
						if(bind)
						{
							messageService.send(this, configService.getServiceBindStatus(type) == null ? MessageTopics.USER_BIND_FAIL : MessageTopics.USER_BIND_SUCCESS ,type);
						}
						else{
							messageService.send(this, ret ? MessageTopics.USER_LOGIN_SUCCESS : MessageTopics.USER_LOGIN_FAIL);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if(bind)
						{
							messageService.send(this, MessageTopics.USER_BIND_FAIL);
						}
						else{
							messageService.send(this, MessageTopics.USER_LOGIN_FAIL);
						}
					}
				}
			}
		});



	}


	@Override
	public void doRegister(final CommentUserInfo userInfo, final UserInfoCallBack callBack) {
		if (userInfo.isLogin()) {
			callBack.onSuccess(userInfo);
			return;
		} else {
			new Thread() {
				@Override
				public void run() {
					CommentUserInfo commentUserInfo = localRegister(userInfo);
					if (commentUserInfo != null){
						callBack.onSuccess(commentUserInfo);
					} else {
						messageService.send(null, new Message(MessageTopics.REGISTER_ERROR,
								App.get().getResources().getString(R.string.user_register_exists)));
					}
				}
			}.start();
		}
	}

	@Override
	public void doUnBind(String uid, final String type, String openId) {
		JSONObject body = new JSONObject();
		try {
			body.put("userid", uid);
			body.put("type",type);
			body.put("openid", openId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		App.get().getService(IJsonService.class).post(BASE_LOGIN_URL + USER_UNBIND, body, null, new IEventHandler<IJsonService.JsonEventArgs>() {
			@Override
			public void process(Object o, IJsonService.JsonEventArgs args) {
				if(args!=null)
				{
					try {
						JSONObject rspJo = new JSONObject(args.getResponse());
						Log.i("oye","unbind back---"+rspJo);
						boolean ret = procLoginResponse(rspJo);
						messageService.send(this, ret ? MessageTopics.USER_UNBIND_SUCCESS : MessageTopics.USER_UNBIND_FAIL,type);
					} catch (JSONException e) {
						e.printStackTrace();
						messageService.send(this, MessageTopics.USER_UNBIND_FAIL);
					}
				}
			}
		});
	}

	/**
	 * 本地登录Login
	 */
	@Override
	public void doLocalLogin(String email,String name,String pwd,final boolean bind) {
		JSONObject body = new JSONObject();
		try {
			if (email != null){
				body.put("email", email);
			}else{
				body.put("name",name);
			}
			body.put("pwd", pwd);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}


		App.get().getService(IJsonService.class).post(BASE_LOGIN_URL + LOGIN_SUFFIX, body, null, new IEventHandler<IJsonService.JsonEventArgs>() {
			@Override
			public void process(Object o, IJsonService.JsonEventArgs args) {
				if(args!=null)
				{
					try {
						JSONObject rspJo = new JSONObject(args.getResponse());
						Log.i("oye","local---"+rspJo.toString());
						boolean ret = procLoginResponse(rspJo);
						messageService.send(this, ret ? MessageTopics.USER_LOGIN_SUCCESS : MessageTopics.USER_LOGIN_FAIL,SocialExtra.SOCIAL_TYPE_POPONEWS);
					} catch (JSONException e) {
						e.printStackTrace();
						messageService.send(this, MessageTopics.USER_LOGIN_FAIL);
					}
				}
			}
		});
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
		info = userInfo;
	}

	@Override
	public CommentUserInfo getUserInfo() {
		return info;
	}


	private CommentUserInfo localRegister(CommentUserInfo userInfo){
		CommentUserInfo info = null;
		HttpURLConnection conn = null;
		URL url = null;
		try {
			url = new URL(BASE_LOGIN_URL + REGISTER_SUFFIX);
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
			obj.put("pwd",userInfo.getPwd());
			out.flush();
			out.writeBytes(obj.toString());
			out.close();
			Log.i("oye", "提交 --register-" + obj.toString());
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
				Log.i("oye", "back-register-" + sb.toString());
				JSONObject jsonObject = new JSONObject(sb.toString());
				JSONObject data = jsonObject.getJSONObject("data");
				if (jsonObject.optBoolean("seccess")) {
					info = new CommentUserInfo();
					info.setUId(data.optString("_id"));
					info.setEmail(data.optString("email"));
					info.setUserName(data.optString("name"));
					return info;
				} else {
					return null;
				}
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


	private boolean procLoginResponse(JSONObject jo)
	{
		if (jo.optBoolean("seccess"))
		{
			try {
				JSONObject data = jo.getJSONObject("data");

				JSONObject user = data.getJSONObject("user");
				configService.setLoginUserInfo(user.toString());

				JSONArray bind = data.getJSONArray("primary");
				configService.setAllBindStatus(bind);

				updateGlobalUserInfo(user,true);

				return true;
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
		else
			return false;
	}


	private void updateGlobalUserInfo(JSONObject userServer,boolean loginStatus)
	{
		info = new CommentUserInfo();
		info.setUId(userServer.optString("_id"));
		info.setEmail(userServer.optString("email"));
		info.setUserName(userServer.optString("name"));
		if (userServer.optString("avatar") == null){
			info.setAvatar(null);
		} else {
			info.setAvatar(userServer.optString("avatar"));
		}
		info.setLogin(loginStatus);
	}

}
