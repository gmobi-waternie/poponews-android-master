package com.gmobi.poponews.cases.accountsetting;

import android.content.Intent;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IUserService;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.momock.app.App;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: vivian .
 * Date: 2016-06-30
 * Time: 11:21
 */
public class AccountSettingActivity extends BaseAppActivity {
    IUserService userService;
    IConfigService configService;
    @Override
    protected void onCaseCreate() {
        setContentView(R.layout.activity_account_setting);
        userService = App.get().getService(IUserService.class);
        configService = App.get().getService(IConfigService.class);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != PopoApplication.mTencent){
            PopoApplication.mTencent.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == GlobalConfig.GOOGLE_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            JSONObject jo  = new JSONObject();
            try {
                jo.put("id", account.getId());
                if (account.getPhotoUrl() != null){
                    jo.put("avatar", account.getPhotoUrl());
                } else {
                    jo.put("avatar","");
                }
                jo.put("name", account.getDisplayName());
                configService.insertThirdUserInfo(SocialExtra.SOCIAL_TYPE_GOOGLE, jo.toString());
                userService.doThirdLogin(account.getId(), account.getDisplayName(), account.getPhotoUrl() + "",
                        account.getEmail(), userService.getUserInfo() != null ? userService.getUserInfo().getUId() : null, SocialExtra.SOCIAL_TYPE_GOOGLE, true);
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    @Override
    protected String getSelfName() {
        return CaseNames.ACCOUNT_SETTING;
    }
}
