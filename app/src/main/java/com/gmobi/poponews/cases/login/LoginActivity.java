package com.gmobi.poponews.cases.login;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.UserInfoCallBack;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.momock.app.App;
import com.momock.holder.ViewHolder;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;

import javax.inject.Inject;

/**
 * Created by nage on 2016/6/20.
 */
public class LoginActivity extends BaseAppActivity {
    IUserService userService;
    IMessageService messageService;
    @Override
    protected void onCaseCreate() {
        setContentView(R.layout.activity_login);
        userService = App.get().getService(IUserService.class);
        messageService = App.get().getService(IMessageService.class);
    }

    @Override
    protected String getSelfName() {
        return CaseNames.LOGIN;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != PopoApplication.mTencent){
            PopoApplication.mTencent.onActivityResult(requestCode, resultCode, data);
        }
        if(MainActivity.callbackManager!=null)
            MainActivity.callbackManager.onActivityResult(requestCode, resultCode, data);

        if(MainActivity.getTwitterAuthClient()!=null)
            MainActivity.getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
        Log.i("oye", "login--requestCode = " + requestCode + ",resultCode=" + resultCode);
        if (requestCode == GlobalConfig.GOOGLE_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
            Log.i("oye","boolean---"+result.isSuccess());

        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            Log.i("oye","google login---s"+account.getId());
            userService.doThirdLogin(account.getId(), account.getDisplayName(), account.getPhotoUrl() + "",
                    account.getEmail(), null, SocialExtra.SOCIAL_TYPE_GOOGLE, false);
//            messageService.send(this, MessageTopics.GET_GOOGLE_ME_LOGIN);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalysisUtil.onActivityResume(this, AnalysisUtil.SCR_ME);
    }

    @Override
    public void onPause() {
        super.onPause();
        AnalysisUtil.onActivityPause(this);
    }

    /**
     * 重写目的：点击EditText所在ViewGroup外部，隐藏软键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    v.clearFocus();
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }


    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            // 获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
