package com.gmobi.poponews.cases.socialsetting;

import android.content.Intent;
import android.util.Log;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.cases.main.MainCase;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class SocialSettingActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
        setContentView(R.layout.activity_social_setting);
    }

	@Override
	protected String getSelfName() {
			return CaseNames.USER_SOCIAL_SETTING;
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(MainActivity.callbackManager!=null)
			MainActivity.callbackManager.onActivityResult(requestCode, resultCode, data);

		if(MainActivity.getTwitterAuthClient()!=null)
			MainActivity.getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);

		/*if(MainCase.twitterLoginButton != null)
			MainCase.twitterLoginButton.onActivityResult(requestCode, resultCode, data);*/
		Log.e("facebook", "requestCode = " + requestCode + ",resultCode=" + resultCode);
	}


	
}
