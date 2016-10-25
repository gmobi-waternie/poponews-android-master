package com.gmobi.poponews.cases.facebook;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.OutletNames;
import com.gmobi.poponews.service.FacebookService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.CaseActivity;
import com.momock.outlet.action.MenuActionOutlet;


public class FacebookActivity extends CaseActivity {
//	public static CallbackManager callbackManager;
//	public static AccessTokenTracker accessTokenTracker;
    @Override
    public void onCreate() {
        setContentView(R.layout.activity_facebook);
        

		//callbackManager = CallbackManager.Factory.create();


		
    }

	@Override
	protected String getCaseName() {
			return CaseNames.USER_FACEBOOK;
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//super.onActivityResult(requestCode, resultCode, data);
		//callbackManager.onActivityResult(requestCode, resultCode, data);
		//Log.e("facebook", "requestCode = " + requestCode + ",resultCode=" + resultCode);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//accessTokenTracker.stopTracking();
	}


	
}
