package com.gmobi.poponews.cases.register;

import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.CommentUserInfo;
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
import com.momock.service.IUITaskService;

import javax.inject.Inject;

/**
 * User: vivian .
 * Date: 2016-07-01
 * Time: 15:23
 */
public class RegisterCase extends Case<CaseActivity> {

    @Inject
    IUserService userService;
    @Inject
    IMessageService messageService;
    @Inject
    IUITaskService uiTaskService;

    private CaseActivity activity;

    public RegisterCase(String name) {
        super(name);
    }

    @Override
    public void onCreate() {


        messageService.addHandler("register", new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                CommentUserInfo info = (CommentUserInfo) message.getData();
                userService.doLocalLogin(info.getEmail(),info.getUserName(),info.getPwd(),false);
            }
        });

        messageService.addHandler(MessageTopics.USER_LOGIN_SUCCESS, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (getAttachedObject() != null){
                    getAttachedObject().finish();
                }
            }
        });


        messageService.addHandler(MessageTopics.REGISTER_ERROR, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                userService.setUserInfo(null);
                userService.setLogin(false);
                final String data = (String) message.getData();
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
                        if (activity != null){
                            ToastUtils.showShortToast(data);
                        }

                    }
                });
            }
        });
    }

    private EditText et_register_email,et_register_pwd,et_register_pwd_again;
    private CheckBox checkbox_register;
    private TextView tv_register_agree;
    private Button btn_register;
    private String email,pwd,pwd_again;
    @Override
    public void onAttach(CaseActivity target) {
        activity = target;
        findViews(target);
        registerListener(target);
        initData();
        checkbox_register.setChecked(true);

    }

    private void initData() {
        btn_register.setFocusable(false);
        btn_register.setClickable(false);
        btn_register.setBackgroundResource(R.color.bg_grey);
        tv_register_agree.setText(App.get().getResources().getString(R.string.user_register_agree));
        tv_register_agree.append(Html.fromHtml("<font color=\'#587CB2'>" + "《"+App.get().getResources().getString(R.string.user_register_privacy)+"》" + "</font>"));


    }


    /**
     * 初始化控件
     */
    private void findViews(final CaseActivity target) {
        ViewHolder.get(target, R.id.register_back).getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                target.finish();
            }
        });
        et_register_email = ViewHolder.get(target, R.id.et_register_email).getView();
        et_register_pwd = ViewHolder.get(target, R.id.et_register_pwd).getView();
        et_register_pwd_again = ViewHolder.get(target, R.id.et_register_pwd_again).getView();
        checkbox_register = ViewHolder.get(target, R.id.checkbox_register).getView();
        tv_register_agree = ViewHolder.get(target, R.id.tv_register_agree).getView();
        btn_register = ViewHolder.get(target, R.id.btn_register).getView();
    }
    private void registerListener(final CaseActivity target) {
        checkbox_register.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_register.setFocusable(true);
                    btn_register.setClickable(true);
                    btn_register.setBackgroundResource(R.drawable.btn_login_selector);
                } else {
                    btn_register.setFocusable(false);
                    btn_register.setClickable(false);
                    btn_register.setBackgroundResource(R.color.bg_grey);

                }
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_register_email.getText().toString())) {
                    ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_email_no));
                    return;
                }
                if (TextUtils.isEmpty(et_register_pwd.getText().toString())) {
                    ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_pwd_no));
                    return;
                }
                if (et_register_pwd.getText().toString().trim().length() < 6) {
                    ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_pwd_qualified));
                    return;
                }
                if (TextUtils.isEmpty(et_register_pwd_again.getText().toString())) {
                    ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_pwd_again_no));
                    return;
                }
                if (!StringUtil.isEmail(et_register_email.getText().toString())) {
                    ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_email_qualified));
                    return;
                }
                email = et_register_email.getText().toString();
                pwd = et_register_pwd.getText().toString();
                pwd_again = et_register_pwd_again.getText().toString();
                if (pwd.equals(pwd_again)) {
                    final CommentUserInfo info = new CommentUserInfo();
                    info.setEmail(email);
                    info.setPwd(pwd);
                    Log.i("oye", "regitser -----  email" + email + "---pwd--" + pwd);
                    userService.doRegister(info, new UserInfoCallBack() {
                        @Override
                        public void onSuccess(CommentUserInfo var1) {
                            if (null == var1 || "".equals(var1)) {
                                Log.i("oye", "register error");
                            } else {
                                CommentUserInfo info1 = new CommentUserInfo();
                                info1.setEmail(var1.getEmail());
                                info1.setUserName(var1.getUserName());
                                info1.setPwd(pwd);
                                messageService.send(null, new Message("register", info1));

                            }
                        }
                    });
                } else {
                    ToastUtils.showShortToast(App.get().getResources().getString(R.string.user_register_pwd_different));

                }
            }

        });

    }

    @Override
    public void onShow() {
        if (getAttachedObject() != null){
            NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_register_action_bar);
            boolean isNight = NightModeUtil.isNightMode();
            UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
                    isNight ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
            ViewHolder.get(getAttachedObject(), R.id.register_scroll).getView().setBackgroundColor(isNight ? App.get().getResources().getColor(R.color.bg_black) :
                    App.get().getResources().getColor(R.color.bg_white));

            ViewHolder.get(getAttachedObject(), R.id.register_back).getView().setBackgroundDrawable(isNight ?
                    App.get().getResources().getDrawable(R.drawable.btn_login_selector_night) : App.get().getResources().getDrawable(R.drawable.btn_login_selector));
            ViewHolder.get(getAttachedObject(), R.id.btn_register).getView().setBackgroundDrawable(isNight ?
                    App.get().getResources().getDrawable(R.drawable.btn_login_selector_night) : App.get().getResources().getDrawable(R.drawable.btn_login_selector));
            et_register_email.setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            et_register_pwd.setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            et_register_pwd_again.setTextColor(isNight ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
        }
        super.onShow();
    }
}
