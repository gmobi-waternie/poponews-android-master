package com.gmobi.poponews.activity;


import java.util.Timer;
import java.util.TimerTask;

import com.appsflyer.AppsFlyerLib;
import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.cases.guide.GuideEditionActivity;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.gcm.RegistrationIntentService;
import com.gmobi.poponews.model.EditionData;
import com.gmobi.poponews.service.ConfigService;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IUpdateService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.AnimUtils;
import com.gmobi.poponews.util.LocaleHelper;
import com.gmobi.poponews.util.PreferenceHelper;
import com.gmobi.poponews.util.SizeHelper;
import com.gmobi.poponews.util.UiHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class SplashActivity extends Activity {

	RelativeLayout rlUpdate;
	RelativeLayout rlUpdateButton;
	public static Handler splashHandler;
	ProgressBar updatePb;
	TextView updateLength;
	TextView updateTitle;
	ImageView loading;

	private boolean editionReady = false;

	private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
	private final static int READ_PHONE_STATE_REQUEST_CODE = 2;
	private final static int ACCESS_FINE_LOCATION_REQUEST_CODE = 3;

	void rsStart(){
		IRemoteService rs = App.get().getService(IRemoteService.class);
		rs.doService();
		rs.getEditionList(GlobalConfig.FROM_SPLASH);
	}

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		IConfigService cs = App.get().getService(IConfigService.class);
		String curLang = cs.getCurLang();
		if(!curLang.equals(""))
			LocaleHelper.setDefaultLocal(curLang);

		AppsFlyerLib.setAppsFlyerKey(PopoApplication.APPFLYER_DEVKEY);
		AppsFlyerLib.sendTracking(getApplicationContext());

		startRegGcm();
		App.get().getService(IDataService.class).initCacheItemsList();


		UiHelper.setStatusBarColor(this, findViewById(R.id.statusBarBackground),
				getResources().getColor(R.color.splash_top_bg));
		TextView tvVer = (TextView) findViewById(R.id.tv_splash_ver);
		updateLength =  (TextView) findViewById(R.id.update_message);
		updatePb =  (ProgressBar) findViewById(R.id.update_progress);
		updateTitle =  (TextView) findViewById(R.id.update_title);
		rlUpdate = (RelativeLayout) findViewById(R.id.rl_splash_update);
		rlUpdateButton =  (RelativeLayout) findViewById(R.id.rl_update_skip);
		loading = (ImageView)findViewById(R.id.iv_net_loading);
		String ver = SystemHelper.getAppVersion(this);
		tvVer.setText(getResources().getString(R.string.splash_ver, ver));

		
		startTopBgAnimation();
		
		splashHandler =new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
					case MessageTopics.SYSTEM_UPDATE_PROCESS:
						Bundle msgData =msg.getData();
						int per = msgData.getInt("per");
						updatePb.setProgress(per);

						long downloadLength = msgData.getLong("dl");
						long contentLength = msgData.getLong("cl");

						updateLength.setText(SizeHelper.getSize(downloadLength)+"/"+SizeHelper.getSize(contentLength));
						break;
					case MessageTopics.SYSTEM_EDITION_READY:
						editionReady = true;
						break;
					case MessageTopics.SYSTEM_UPDATE_CHECK:
						AnimUtils.ShowLoading(loading, R.anim.net_loading_anim, false, false);
						int updateFlag = App.get().getService(IConfigService.class).getUpdateFlag();


						if(updateFlag != ConfigService.UPDATE_NONE)
						{

							rlUpdate.setVisibility(View.VISIBLE);
							String upTitle = getResources().getString(R.string.dialog_update_text2);
							String updateVersion = App.get().getService(IConfigService.class).getUpdateVersion();
							upTitle = String.format(upTitle, updateVersion);
							updateTitle.setText(upTitle);
							if(updateFlag == ConfigService.UPDATE_FORCE)
								rlUpdateButton.setVisibility(View.GONE);

							App.get().getService(IUpdateService.class).updateStart();
						}else{
							if(editionReady)
							{
								checkEdition();
							}
							else
							{
								Timer readyTimer = new Timer();
								readyTimer.schedule(new TimerTask() {
									@Override
									public void run() {
										if(editionReady)
										{
											this.cancel();
											checkEdition();
											Log.e("POPONEWS", "edition is Ready");
										}
									}
								}, 0, 500);
							}

						}
						break;
                    case MessageTopics.SYSTEM_CONNECT_SERVER:
                        rsStart();
                        AnimUtils.ShowLoading(loading, R.anim.net_loading_anim, true, false);
                        break;

                    case MessageTopics.SYSTEM_ENTRY_MAIN:
                    	entryApp();
                        break;
					case MessageTopics.SYSTEM_MSG_REQUEST_PERMISSION:
						boolean allPermissiobAllowed = true;
						if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
								!= PackageManager.PERMISSION_GRANTED) {

							ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
									WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
							allPermissiobAllowed = false;
						}

						if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_PHONE_STATE)
								!= PackageManager.PERMISSION_GRANTED) {

							ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE},
									READ_PHONE_STATE_REQUEST_CODE);
							allPermissiobAllowed = false;
						}

						if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
								!= PackageManager.PERMISSION_GRANTED) {

							ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
									ACCESS_FINE_LOCATION_REQUEST_CODE);
							allPermissiobAllowed = false;
						}

						if(allPermissiobAllowed)
						{
							new Timer().schedule(new TimerTask() {
								@Override
								public void run() {
									splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_CONNECT_SERVER);
								}
							}, 100);
						}

						break;
				}

			}
		};
		
		rlUpdateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				App.get().getService(IUpdateService.class).updateCancel();
				IConfigService configService = App.get().getService(IConfigService.class);
				if(configService.isFirstEntry() && setEditionVisible())
				{
					entryEditonSetting();
					configService.setFirstEntry(false);
				}
				else
                	entryApp();
				
			}
		});
		
		/*
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mainIntent);
				finish();
			}}, 2500);*/
		
		
		
	}

	private void startTopBgAnimation() {

		startAnimation(R.id.fl_splash_top_bg, R.anim.splash_top_bg, new AnimationListener() {

			@Override
			public void onAnimationStart(Animation paramAnimation) {

			}

			@Override
			public void onAnimationEnd(Animation paramAnimation) {
                startTileAnimation();
				startAnimation(R.id.iv_splash_logo, R.anim.splash_logo, new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
						final Timer readyTimer = new Timer();

						readyTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_MSG_REQUEST_PERMISSION);
							}
						}, 1000);



						if(BuildConfig.GROUP.equals("cherry")) {
							ImageView iv = (ImageView) findViewById(R.id.iv_splash_logo);
							if (iv == null)
								return;
							iv.setImageBitmap(null);
							iv.setBackgroundResource(R.drawable.splash_icon_anim);
							AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
							loadAnim.setOneShot(true);
							loadAnim.start();
						}

					}

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
			}

			@Override
			public void onAnimationRepeat(Animation paramAnimation) {

			}

		});
	}

	private void startTileAnimation() {
		if (BuildConfig.GROUP.equals("vogue") || BuildConfig.GROUP.equals("gq"))
		{
			startAnimation(R.id.tv_splash_ver, R.anim.splash_title, null);
			startAnimation(R.id.tv_splash_foot, R.anim.splash_title, null);
		}
		else
		{
		
			startAnimation(R.id.tv_splash_name, R.anim.splash_title, new AnimationListener(){
	
				@Override
				public void onAnimationEnd(Animation animation) {
					startAnimation(R.id.tv_splash_ver, R.anim.splash_title, null);
					startAnimation(R.id.tv_splash_foot, R.anim.splash_title, null);
				}
	
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
	
				@Override
				public void onAnimationStart(Animation animation) {
	
					
				}
				
			});
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
        	finish();
            System.exit(0);
	        return true;
		}

		return super.onKeyDown(keyCode, event);
		
	}

	/**
	 * basic animation function
	 * @param viewId       view which want to do animation
	 * @param resAnim      animation resource
	 * @param al           animation action listener
	 */
	private void startAnimation(int viewId, int resAnim, AnimationListener al){
		Animation anim = AnimationUtils.loadAnimation(this, resAnim);
		anim.reset();
		if(null != al){
			anim.setAnimationListener(al);
		}
		View l = findViewById(viewId);
		l.setVisibility(View.VISIBLE);
		l.clearAnimation();
		l.startAnimation(anim);
	}

    private void entryApp(){
		ImageView iv = (ImageView)findViewById(R.id.iv_splash_logo);
		if(iv != null) {
			AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
			if(loadAnim!=null &&loadAnim.isRunning())
				loadAnim.stop();
		}


		IConfigService configService = App.get().getService(IConfigService.class);
		AnalysisUtil.recordLaunch(configService.getCurChannel(),configService.getDch());

        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }
	private void entryEditonSetting(){
		Intent editionIntent = new Intent(SplashActivity.this, GuideEditionActivity.class);
		editionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(editionIntent);
		finish();
	}

	private void checkEdition()
	{
		IConfigService configService = App.get().getService(IConfigService.class);
		if(configService.isFirstEntry() && setEditionVisible())
		{
			entryEditonSetting();
			configService.setFirstEntry(false);
		}
		else
			entryApp();
	}
   
    /*private void showMain()
	{
		Runnable showMain = new Runnable(){

			@Override
			public void run() {
				Intent intent;
				
				
				if (App.get().getCurrentActivity() == null)
				{
					intent = new Intent(App.get(),MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					App.get().startActivity(intent);
				}
				else
				{
					intent = new Intent(App.get().getCurrentActivity(),MainActivity.class);
					App.get().getCurrentActivity().startActivity(intent);
				}
					
			}
			
		};

		uiTaskService.runDelayed(showMain, 1000);
	}*/


	private boolean setEditionVisible(){
		IConfigService configSvc = App.get().getService(IConfigService.class);
		JSONObject dataSrc = configSvc.getEditionList();
		DataList<EditionData> out = new DataList<>();

		try{
			String baseUrl = dataSrc.getString("baseUrl");
			JSONArray ja = dataSrc.getJSONArray("data");
			JSONObject tmpJo;
			for(int i = 0; i < ja.length(); i++){
				tmpJo = ja.getJSONObject(i);
				EditionData ed = new EditionData(baseUrl + tmpJo.getString("icon"),
						tmpJo.getString("name"),
						tmpJo.getString("id"),tmpJo.getString("lang"));
				out.addItem(ed);
			}
		}catch(Exception e){
			return false;
		}

		return out.getItemCount() > 1;

	}

	@Override
	public void onResume() {
		super.onResume();
		AnalysisUtil.onActivityResume(this,AnalysisUtil.SCR_LAUNCH);
	}

	@Override
	public void onPause() {
		super.onPause();
		AnalysisUtil.onActivityPause(this);
	}


	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private BroadcastReceiver mRegistrationBroadcastReceiver;

	private boolean checkPlayServices() {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(resultCode)) {
				apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				Logger.error("This device is not supported GCM.");
			}
			return false;
		}
		return true;
	}

	private void startRegGcm()
	{

		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				SharedPreferences sharedPreferences =
						PreferenceManager.getDefaultSharedPreferences(context);
				boolean sentToken = sharedPreferences
						.getBoolean(PreferenceHelper.SENT_TOKEN_TO_SERVER, false);
				if (sentToken) {
					Toast.makeText(App.get(), getString(R.string.gcm_send_message), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(App.get(),getString(R.string.token_error_message),Toast.LENGTH_SHORT).show();
				}
			}
		};


		if (checkPlayServices()) {
			// Start IntentService to register this application with GCM.
			Intent intent = new Intent(this, RegistrationIntentService.class);
			startService(intent);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Timer readyTimer = new Timer();

		readyTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				splashHandler.sendEmptyMessage(MessageTopics.SYSTEM_CONNECT_SERVER);
			}
		}, 100);
	}
}
