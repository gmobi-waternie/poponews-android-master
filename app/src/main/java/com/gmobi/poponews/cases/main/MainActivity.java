package com.gmobi.poponews.cases.main;


import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.FacebookService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IFacebookService;

import com.gmobi.poponews.service.ISocialService;
import com.gmobi.poponews.service.IWeiboService;
import com.gmobi.poponews.share.AccessTokenKeeper;
import com.gmobi.poponews.share.WeiboConstants;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.momock.holder.ViewHolder;
import com.reach.IAdItem;
import com.reach.INativeAd;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.*;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseAppActivity {


    
    private boolean isExit = false;
    private Handler handler;
    private TextView tv_title;
	public static String curCid = "";

	public static CallbackManager callbackManager;
	public static AccessTokenTracker accessTokenTracker;
	public static TwitterAuthClient twitterAuthClient;
	public static SsoHandler mSsoHandler = null;

	private static String WX_APP_ID = "wx6c162ed458fe4b42";
	public static IWXAPI wxApi;




    
    private static final int WAIT_TIME = 2000;//2s内再次按下back退出
    private static final int SHOW_TIME = 1000;//提示显示1s


 	
	@Override
	protected void onCaseCreate() {

		twitterAuthClient = new TwitterAuthClient();
		if(NightModeUtil.isNightMode())
			setTheme(R.style.NightTheme);
		else
			setTheme(R.style.DayTheme);

		setContentView(R.layout.activity_main);



		callbackManager = CallbackManager.Factory.create();

		accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(
					AccessToken oldAccessToken,
					AccessToken currentAccessToken) {
				if(oldAccessToken!=null && oldAccessToken.getToken().indexOf("ACCESS_TOKEN_REMOVED") != 0)
				{
					FacebookService fbService = (FacebookService) App.get().getService(IFacebookService.class);
					fbService.doLogout(null);

				}
			}
		};

		PopoApplication.mAuthInfo = new AuthInfo(this, WeiboConstants.APP_KEY,
				WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
		mSsoHandler = new SsoHandler(MainActivity.this, PopoApplication.mAuthInfo);


		wxApi = WXAPIFactory.createWXAPI(this,WX_APP_ID,true);
		wxApi.registerApp(WX_APP_ID);

		//App.get().getService(IWeiboService.class).doLogin(null,ISocialService.FROM_MAIN);


		//mDrawerToggle = new ActionBarDrawerToggle(
		//this,                  /* host Activity */
        //        mDrawerLayout,         /* DrawerLayout object */
        //        R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
        //        R.string.left_drawer_hint_open,  /* "open drawer" description for accessibility */
        //        R.string.left_drawer_hint_close/* "close drawer" description for accessibility */
        //       );
        //mDrawerLayout.setDrawerListener(mDrawerToggle);
		
        /*
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
        */
        

	    handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                isExit = false;
            }
        };
        
        /*tv_title = (TextView) findViewById(R.id.main_action_bar_title);
        tv_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickTitleBtn(v);
			}
		});*/
	}
	
	@Override
	protected String getSelfName() {
		return CaseNames.MAIN;
	}


	@Override
	protected void onResume() {
		super.onResume();

		View tab = findViewById(android.R.id.tabs);
		if(tab!=null)
			tab.setBackgroundColor(
					NightModeUtil.isNightMode() ?
							getResources().getColor(R.color.tab_bg_color_night) :
							getResources().getColor(R.color.tab_bg_color)
			);
		View div = findViewById(R.id.main_tab_top_divider);
		if(div!=null)
			div.setBackgroundColor(
					NightModeUtil.isNightMode() ?
							getResources().getColor(R.color.tab_bg_color_night) :
							getResources().getColor(R.color.main_tab_top_divider)
			);

	}

	long touchTime = 0;
	long waitTime = 2000;


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){

			NewsCategory ctg = App.get().getService(IDataService.class).getCategoryById(curCid);
			if(ctg!=null &&  ctg.getAdSrc() == AdHelper.NATIVE_AD_SRC_GMOBI)
			{
				if(AdHelper.getInstance(this).closeAdFullScreen())
					return true;
			}
			if(ctg!=null &&  ctg.getCategoryType().equals(NewsCategory.TYPE_VK))
			{
				WebView wv = (WebView) findViewById(R.id.wv_list);
				if(wv!=null&&wv.canGoBack()) {
					wv.goBack();
					return true;
				}

			}


			{
				if(!isExit){
					isExit = true;
					handler.sendEmptyMessageDelayed(0, WAIT_TIME);
					Toast.makeText(this, R.string.press_back_again,Toast.LENGTH_SHORT).show();
				}else{
					UiHelper.setMainAlive(false);
					UiHelper.stopAllTimer();
					finish();
					System.exit(0);
				}
				return true;
			}


		}

		return super.onKeyDown(keyCode, event);
		
	}

	


	@Override
	protected void onDestroy() {
		UiHelper.setMainAlive(false);

		super.onDestroy();
		accessTokenTracker.stopTracking();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
		if(MainCase.twitterLoginButton != null)
			MainCase.twitterLoginButton.onActivityResult(requestCode, resultCode, data);

		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
		Log.e("facebook", "requestCode = " + requestCode + ",resultCode=" + resultCode);
	}

	public static TwitterAuthClient getTwitterAuthClient()
	{
		if(twitterAuthClient == null)
			twitterAuthClient = new TwitterAuthClient();

		return twitterAuthClient;
	}



}
