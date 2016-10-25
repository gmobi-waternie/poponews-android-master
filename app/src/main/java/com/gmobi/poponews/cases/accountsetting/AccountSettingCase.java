package com.gmobi.poponews.cases.accountsetting;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.login.LoginActivity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.IGoogleService;
import com.gmobi.poponews.service.ISocialService;
import com.gmobi.poponews.service.ITwitterService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IMessageService;
import com.momock.util.Logger;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

/**
 * Created by vivian on 2016/6/30.
 */
public class AccountSettingCase extends Case<CaseActivity> implements View.OnClickListener{
    @Inject
    IUserService userService;
    @Inject
    IFacebookService facebookService;
    @Inject
    ITwitterService twitterService;
    @Inject
    IGoogleService googleService;
    @Inject
    IConfigService configService;
    @Inject
    IMessageService messageService;

    IMessageHandler bindMshHandler;

    public AccountSettingCase(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        bindMshHandler = new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                String type = (String) message.getData();
                JSONObject jo = configService.getThirdUserInfo(type);
                CommentUserInfo user = userService.getUserInfo();
                if(user == null || jo == null)
                {
                    messageService.send(this,MessageTopics.USER_BIND_FAIL);
                    return;
                }

                try {
                    JSONObject infoJo = jo.getJSONObject("data");
                    userService.doThirdLogin(infoJo.getString("id"),infoJo.getString("name"),infoJo.getString("avatar"),null,user.getUId(),type,true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    messageService.send(this, MessageTopics.USER_BIND_FAIL);

                }
            }
        };

        messageService.addHandler(MessageTopics.USER_BIND_FAIL, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                Toast.makeText(getAttachedObject(),App.get().getResources().getString(R.string.account_setting_bind_error),Toast.LENGTH_SHORT).show();
            }
        });
        messageService.addHandler(MessageTopics.USER_UNBIND_FAIL, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                Toast.makeText(getAttachedObject(),App.get().getResources().getString(R.string.account_setting_unbind_error),Toast.LENGTH_SHORT).show();
            }
        });
        messageService.addHandler(MessageTopics.USER_BIND_SUCCESS, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached()) {
                    Logger.error("USER_BIND_SUCCESS isAttached="+isAttached());
                    return;
                }
                String unBindString = "<a href=\"#\">"+App.get().getResources().getString(R.string.account_setting_unbind)+"</a>";
                String type = (String) message.getData();
                switch (type)
                {
                    case SocialExtra.SOCIAL_TYPE_FACEBOOK:
                        tv_account_facebook.setText(Html.fromHtml(unBindString));
                        break;
                    case SocialExtra.SOCIAL_TYPE_TWITTER:
                        tv_account_twitter.setText(Html.fromHtml(unBindString));
                        break;
                    case SocialExtra.SOCIAL_TYPE_GOOGLE:
                        tv_account_google.setText(Html.fromHtml(unBindString));
                        break;
                    default:
                        break;
                }

            }
        });
        messageService.addHandler(MessageTopics.USER_UNBIND_SUCCESS, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                String bindString = "<a href=\"#\">"+App.get().getResources().getString(R.string.account_setting_bind)+"</a>";
                String type = (String) message.getData();
                switch (type)
                {
                    case SocialExtra.SOCIAL_TYPE_FACEBOOK:
                        tv_account_facebook.setText(Html.fromHtml(bindString));
                        break;
                    case SocialExtra.SOCIAL_TYPE_TWITTER:
                        tv_account_twitter.setText(Html.fromHtml(bindString));
                        break;
                    case SocialExtra.SOCIAL_TYPE_GOOGLE:
                        tv_account_google.setText(Html.fromHtml(bindString));
                        break;
                    default:
                        break;
                }

            }
        });


        messageService.addHandler(MessageTopics.GET_FACEBOOK_ME_BIND, bindMshHandler);
        messageService.addHandler(MessageTopics.GET_TWITTER_ME_BIND, bindMshHandler);
        messageService.addHandler(MessageTopics.GET_GOOGLE_ME_BIND, bindMshHandler);

    }

    private CaseActivity context;
    private Button btn_login_out;
    private TextView tv_account_facebook,tv_account_twitter,tv_account_google;
    @Override
    public void onAttach(final CaseActivity target) {
        context = target;
        ViewHolder.get(target, R.id.account_setting_back).getView().setOnClickListener(this);
        btn_login_out = ViewHolder.get(target, R.id.btn_login_out).getView();
        tv_account_facebook = ViewHolder.get(target, R.id.tv_account_facebook).getView();
        tv_account_twitter = ViewHolder.get(target, R.id.tv_account_twitter).getView();
        tv_account_google = ViewHolder.get(target, R.id.tv_account_google).getView();
        btn_login_out.setOnClickListener(this);
        tv_account_facebook.setOnClickListener(this);
        tv_account_twitter.setOnClickListener(this);
        tv_account_google.setOnClickListener(this);
//        String text;
//        if (userService.getUserInfo() != null){
//            text = "<a href=\"#\">"+App.get().getResources().getString(R.string.account_setting_bind)+"</a>";
//            tv_account_facebook.setText(Html.fromHtml(text));
//            tv_account_twitter.setText(Html.fromHtml(text));
//            tv_account_google.setText(Html.fromHtml(text));
//        } else {
//            text = "<font color='#999999'>"+App.get().getResources().getString(R.string.account_setting_unlogin)+"</font>";
//            tv_account_facebook.setText(Html.fromHtml(text));
//            tv_account_twitter.setText(Html.fromHtml(text));
//            tv_account_google.setText(Html.fromHtml(text));
//        }





    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.account_setting_back:
                context.finish();
                break;
            case R.id.btn_login_out:
                if (userService.getUserInfo() != null){
                    configService.setLoginUserInfo(null);
                    userService.setUserInfo(null);
                    userService.setLogin(false);
                    context.finish();
                } else {
                    App.get().startActivity(LoginActivity.class);
                }
                break;
            case R.id.tv_account_facebook:
                if (userService.getUserInfo() == null){
                    App.get().startActivity(LoginActivity.class);
                } else {
                    if (configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK) != null){
                        JSONObject faceJson = configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK);
                        userService.doUnBind(userService.getUserInfo().getUId(), faceJson.optString("type"), faceJson.optString("open_id"));
                    } else {
                        facebookService.doLogin(context, ISocialService.FROM_BIND);
                    }
                }
                break;
            case R.id.tv_account_twitter:
                if (userService.getUserInfo() == null) {
                    App.get().startActivity(LoginActivity.class);
                } else {
                    if (configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_TWITTER) != null) {
                        JSONObject twitterJson = configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_TWITTER);
                        userService.doUnBind(userService.getUserInfo().getUId(), twitterJson.optString("type"), twitterJson.optString("open_id"));
                    } else {
                        twitterService.doLogin(context, ISocialService.FROM_BIND);
                    }
                }
                break;
            case R.id.tv_account_google:
                if (userService.getUserInfo() == null) {
                    App.get().startActivity(LoginActivity.class);
                } else {
                    if (configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_GOOGLE) != null) {
                        JSONObject googleJson = configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_GOOGLE);
                        userService.doUnBind(userService.getUserInfo().getUId(), googleJson.optString("type"), googleJson.optString("open_id"));
                    } else {
                        googleService.doLogin(context, ISocialService.FROM_BIND);
                    }
                }
                break;
        }
    }


    @Override
    public void onShow() {
        if (getAttachedObject() != null){
            NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_account_setting_action_bar);
            boolean isNight = NightModeUtil.isNightMode();
            UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
                    isNight ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
            ViewHolder.get(getAttachedObject(),R.id.linear_account_setting).getView().setBackgroundColor(isNight ? App.get().getResources().getColor(R.color.bg_black) :
                    App.get().getResources().getColor(R.color.bg_white));

            ((TextView)ViewHolder.get(getAttachedObject(),R.id.account_facebook).getView()).setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            ((TextView)ViewHolder.get(getAttachedObject(),R.id.account_twitter).getView()).setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            ((TextView)ViewHolder.get(getAttachedObject(),R.id.account_google).getView()).setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            ViewHolder.get(getAttachedObject(), R.id.btn_login_out).getView().setBackgroundDrawable(isNight ?
                    App.get().getResources().getDrawable(R.drawable.btn_login_selector_night) : App.get().getResources().getDrawable(R.drawable.btn_login_selector));


            String unBindString = "<a href=\"#\">"+App.get().getResources().getString(R.string.account_setting_unbind)+"</a>";
            String bingString = "<a href=\"#\">"+App.get().getResources().getString(R.string.account_setting_bind)+"</a>";
            String unLoginString = "<font color='#999999'>" + App.get().getResources().getString(R.string.account_setting_unlogin) + "</font>";
            if (userService.getUserInfo() != null){
                btn_login_out.setText(App.get().getResources().getString(R.string.user_login_out));

                if (configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK) != null){
                    tv_account_facebook.setText(Html.fromHtml(unBindString));
                } else {
                    tv_account_facebook.setText(Html.fromHtml(bingString));
                }

                if (configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_TWITTER) != null){
                    tv_account_twitter.setText(Html.fromHtml(unBindString));
                } else {
                    tv_account_twitter.setText(Html.fromHtml(bingString));
                }

                if (configService.getServiceBindStatus(SocialExtra.SOCIAL_TYPE_GOOGLE) != null) {
                    tv_account_google.setText(Html.fromHtml(unBindString));
                } else {
                    tv_account_google.setText(Html.fromHtml(bingString));
                }
            } else {
                btn_login_out.setText(App.get().getResources().getString(R.string.user_login));
                tv_account_facebook.setText(Html.fromHtml(unLoginString));
                tv_account_twitter.setText(Html.fromHtml(unLoginString));
                tv_account_google.setText(Html.fromHtml(unLoginString));
            }
        }

        super.onShow();
    }
}
