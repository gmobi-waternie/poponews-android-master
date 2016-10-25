package com.gmobi.poponews.cases.login;


import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.register.RegisterActivity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.IGoogleService;
import com.gmobi.poponews.service.ISocialDataService;
import com.gmobi.poponews.service.ISocialService;
import com.gmobi.poponews.service.ITwitterService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.StringUtil;
import com.gmobi.poponews.util.ToastUtils;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.util.UserInfoCallBack;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IMessageService;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

/**
 * Created by nage on 2016/6/6.
 */
public class LoginCase extends Case<CaseActivity> implements View.OnClickListener {
	private ImageButton ss_back;
	private EditText et_name, et_pwd;
	private Button btn_login;
	private TextView login_register, login_facebook, login_twitter, login_google;
	private CaseActivity activity;

	@Inject
	IFacebookService facebookService;
	@Inject
	ITwitterService twitterService;
	@Inject
	IGoogleService googleService;
	@Inject
	IMessageService messageService;
	@Inject
	ISocialDataService socialDataService;
	@Inject
	IConfigService configService;

	@Inject
	IUserService userService;
	@Inject
	Resources resources;

	public LoginCase(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		messageService.addHandler(MessageTopics.USER_LOGIN_FAIL, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (getAttachedObject() != null) {
					ToastUtils.showShortToast(resources.getString(R.string.user_login_error));
				}
			}
		});

		messageService.addHandler(MessageTopics.USER_LOGIN_SUCCESS, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				finishBack();
			}
		});


		messageService.addHandler(MessageTopics.GET_FACEBOOK_ME_LOGIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (!isAttached())
					return;


				JSONObject jo = configService.getThirdUserInfo(SocialExtra.SOCIAL_TYPE_FACEBOOK);
				if (jo != null) {
					try {
						JSONObject infoJo = jo.getJSONObject("data");
						userService.doThirdLogin(infoJo.getString("id"),infoJo.getString("name"),infoJo.getString("avatar"),null,null,SocialExtra.SOCIAL_TYPE_FACEBOOK,false);
					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		});

		messageService.addHandler(MessageTopics.GET_TWITTER_ME_LOGIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (!isAttached())
					return;

				JSONObject jo = configService.getThirdUserInfo(SocialExtra.SOCIAL_TYPE_TWITTER);
				if (jo != null) {
					try {
						JSONObject infoJo = jo.getJSONObject("data");
						userService.doThirdLogin(infoJo.getString("id"), infoJo.getString("name"), infoJo.getString("avatar"), null, null, SocialExtra.SOCIAL_TYPE_TWITTER, false);
					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		});
	}

	@Override
	public void onAttach(CaseActivity target) {
		Toast.makeText(target, App.get().getResources().getString(R.string.user_login_please), Toast.LENGTH_SHORT).show();
		activity = target;
		ss_back = (ImageButton) target.findViewById(R.id.comment_back);
		btn_login = (Button) target.findViewById(R.id.btn_login);
		login_facebook = (TextView) target.findViewById(R.id.login_facebook);
		login_twitter = (TextView) target.findViewById(R.id.login_twitter);
		login_google = (TextView) target.findViewById(R.id.login_google);
		login_register = (TextView) target.findViewById(R.id.login_register);
		et_name = (EditText) target.findViewById(R.id.et_local_name);
		et_pwd = (EditText) target.findViewById(R.id.et_pwd);
//        et_name.setText("799652260@qq.com");
//        et_pwd.setText("123456");
		bindListener();
	}

	private void bindListener() {
		ss_back.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		login_facebook.setOnClickListener(this);
		login_twitter.setOnClickListener(this);
		login_google.setOnClickListener(this);
		login_register.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.comment_back:
				finishBack();
				break;
			case R.id.btn_login:
				localLogin();
				break;
			case R.id.login_register:
				App.get().startActivity(RegisterActivity.class);
				finishBack();
				break;
			case R.id.login_facebook:
				facebookService.doLogin(activity, ISocialService.FROM_LOGIN);
				break;
			case R.id.login_twitter:
				twitterService.doLogin(activity, ISocialService.FROM_LOGIN);
				break;
			case R.id.login_google:
                googleService.doLogin(activity, ISocialService.FROM_LOGIN);
				break;
		}
	}

	private void localLogin() {
		if (TextUtils.isEmpty(et_name.getText().toString())) {
			ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_email_no));
			return;
		}
		if (TextUtils.isEmpty(et_pwd.getText().toString())) {
			ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_pwd_no));
			return;
		}
		if (!StringUtil.isEmail(et_name.getText().toString())) {
			ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_email_qualified));
			return;
		}
		userService.doLocalLogin(et_name.getText().toString(),null,et_pwd.getText().toString(),false);
	}

	@Override
	public void onShow() {
		if (getAttachedObject() != null){
			NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_login_action_bar);
			boolean isNight = NightModeUtil.isNightMode();
			UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
					isNight ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
			ViewHolder.get(getAttachedObject(), R.id.linear_login).getView().setBackgroundColor(isNight ? App.get().getResources().getColor(R.color.bg_black) :
					App.get().getResources().getColor(R.color.bg_white));

			ViewHolder.get(getAttachedObject(), R.id.comment_back).getView().setBackgroundDrawable(isNight ?
					App.get().getResources().getDrawable(R.drawable.btn_login_selector_night) : App.get().getResources().getDrawable(R.drawable.btn_login_selector));
			ViewHolder.get(getAttachedObject(), R.id.btn_login).getView().setBackgroundDrawable(isNight ?
					App.get().getResources().getDrawable(R.drawable.btn_login_selector_night) : App.get().getResources().getDrawable(R.drawable.btn_login_selector));
			et_name.setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
					App.get().getResources().getColor(R.color.bg_black));
			et_pwd.setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
					App.get().getResources().getColor(R.color.bg_black));

		}
		super.onShow();
	}

	private void finishBack() {
		if (getAttachedObject() != null) {
			getAttachedObject().finish();
			getAttachedObject().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}
}
