package com.gmobi.poponews.cases.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.javascript.ExecJsApi;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.INewsCacheService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.holder.ViewHolder;
import com.momock.service.ICacheService;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IRService;
import com.momock.service.ISystemService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class BrowserCase extends Case<CaseActivity> {

	public BrowserCase(String name) {
		super(name);
	}

	public BrowserCase(ICase<?> parent) {
		super(parent);
	}

	private static final int AD_NOT_SHOW = 0;
	private static final int AD_IS_SHOWING = 1;
	private static final int AD_IS_SHOWED = 2;
	private int adStatus = AD_NOT_SHOW;

	@Inject
	Resources resources;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IImageService imageService;
	@Inject
	ISystemService systemService;
	@Inject
	IMessageService messageService;
	@Inject
	IDataService dataService;
	@Inject
	IConfigService configService;
	@Inject
	IRService rService;
	@Inject
	NotificationManager notifier;
	@Inject
	Resources res;
	@Inject
	ICacheService cacheService;
	@Inject
	IRemoteService remoteService;
	@Inject
	INewsCacheService newsCacheService;


	WebView wv;
	ExecJsApi execJsApi;
	boolean callbackTriggered = false;

	private String title;
	private String id;
	private String domain;
	private String url;
	private String type;
	private String sns;

	public static final String RESULT_PREFIX = "result://";
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_ERROR = "error";

	public static final String RESULT_CLOSE = "result://close";
	public static final String ACTION_PREFIX = "action://";
	public static final String ACTION_LOGIN = "action://login";
	public static final String ACTION_MARKET = "market://";
	public static final String RESULT_LOGIN = "login://";

	public static final String ACTION_SETRESULT = "setresult://";
	public static final String ACTION_SETCOMMAND = "setcommand://";


	public void closeActivity() {
		if (wv != null) {
			ViewGroup root = (ViewGroup) wv.getParent();
			if (root != null) {
				root.removeView(wv);
				wv.destroy();
			}
		}

		getAttachedObject().finish();
	}

	@Override
	public void onCreate() {


	}

	@Override
	public void run(Object... args) {
		App.get().startActivity(BrowserActivity.class);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);

		Intent in = target.getIntent();

		if (in.hasExtra("id"))
			id = in.getStringExtra("id");
		else
			id = "";

		if (in.hasExtra("type"))
			type = in.getStringExtra("type");
		else
			type = "";


		if (in.hasExtra("sns"))
			sns = in.getStringExtra("sns");
		else
			sns = "";

		url = in.getStringExtra("url");
		title = in.getStringExtra("title");
		domain = in.getStringExtra("domain");


		doAdAction();


		View ivBackBtn = ViewHolder.get(target, R.id.wv_back).getView();

		ivBackBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (refTarget.get() != null) {
					if (wv != null) {
						ViewGroup root = (ViewGroup) wv.getParent();
						if (root != null) {
							root.removeView(wv);
							wv.destroy();
						}
					}

					Activity currActivity = App.get().getCurrentActivity();
					if (currActivity != null)
						currActivity.finish();
				}
			}
		});

		wv = ViewHolder.get(refTarget.get(), R.id.wv_store).getView();
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);

		// set webview UA
		String gUa = webSettings.getUserAgentString();
		gUa += " (CHANNEL/" + configService.getCurChannel();
		gUa += "; APPCHANNEL/" + configService.getCurStoreChannel();
		gUa += "; PACKAGEID/" + refTarget.get().getPackageName();
		gUa += "; APPVER/" + SystemHelper.getAppVersion(refTarget.get()) + ")";
		Logger.debug("webview ua : " + gUa);
		webSettings.setUserAgentString(gUa);

		// webview js api
		execJsApi = new ExecJsApi(wv, refTarget.get());
		wv.addJavascriptInterface(execJsApi, "_JsToNativePluginApi");
		initWebSettings(wv);
		/*
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
        	wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		*/
		final ProgressBar pb = ViewHolder.get(refTarget.get(), R.id.web_loading_pb).getView();
		pb.setVisibility(View.VISIBLE);
		pb.setProgress(0);

		wv.loadUrl(url);

		wv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (refTarget.get() != null) {
					pb.setProgress(progress);
					if (progress == 100)
						pb.setVisibility(View.GONE);
				}

			}
		});

		wv.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.startsWith(RESULT_CLOSE)) {
					closeActivity();
					return true;
				} else if (url.startsWith(ACTION_MARKET)) {
					Intent nIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					refTarget.get().startActivity(nIntent);

					return true;
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				ShowLoading(getAttachedObject(), false);
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
										String description, String failingUrl) {

				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onReceivedSslError(WebView view,
										   SslErrorHandler handler, SslError error) {

				//super.onReceivedSslError(view, handler, error);

				handler.proceed();
			}
		});


		TextView tv = ViewHolder.get(getAttachedObject(), R.id.wv_action_bar_title).getView();
		if (domain.equals("store"))
			tv.setText(title);
		else
			tv.setText(url);
	}


	@Override
	public void onShow() {
		NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_wv_action_bar);

		UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));


	}


	private synchronized void initWebSettings(WebView wv) {
		WebSettings webSettings = wv.getSettings();
		webSettings.setDomStorageEnabled(true);


		/*
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);


		webSettings.setAppCacheEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		webSettings.setDatabaseEnabled(true);



		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webSettings.setLoadsImagesAutomatically(true);
		} else {
			webSettings.setLoadsImagesAutomatically(false);
		}*/
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setBlockNetworkImage(false);

		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	}


	@Override
	public boolean onBack() {

		adStatus = AD_NOT_SHOW;

		if (execJsApi != null) {
			if (execJsApi.onBackPressed())
				return true;
		}


		if (wv != null) {
			if (wv.canGoBack()) {
				wv.goBack();
				return true;
			}

			ViewGroup root = (ViewGroup) wv.getParent();
			if (root != null) {
				root.removeView(wv);
				wv.destroy();
			}
		}

		return super.onBack();
	}

	private void ShowLoading(CaseActivity target, boolean visible) {


		ImageView iv = ViewHolder.get(target, R.id.iv_loading).getView();
		if (iv == null)
			return;

		iv.setBackgroundResource(R.drawable.loading_anim);
		AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
		loadAnim.setOneShot(false);

		if (visible) {
			RelativeLayout rl = ViewHolder.get(target, R.id.rl_loading).getView();
			rl.setVisibility(View.VISIBLE);
			loadAnim.start();
		} else {
			loadAnim.stop();
			RelativeLayout rl = ViewHolder.get(target, R.id.rl_loading).getView();
			rl.setVisibility(View.GONE);
		}
	}


	private void doAdAction() {
		Logger.debug("[AD]getIntersAdPercent() =" + configService.getIntersAdPercent() + ",getIntersFacebookAdPercent=" + configService.getIntersFacebookAdPercent());
		Logger.debug("[AD]getSocialAdPercent() =" + configService.getSocialAdPercent() + ",getSocialFacebookAdPercent=" + configService.getSocialFacebookAdPercent());
		if (domain == null || domain.equals("store"))
			return;


		if (domain.equals("social")) {
			if (configService.isSocialAdEnable()) {
				if (UiHelper.needShow(configService.getSocialAdPercent())) {
					if (UiHelper.needShow(configService.getSocialFacebookAdPercent())) {
						AdHelper.showDiscoverFacebookInterstitialAd(getAttachedObject(), new AdHelper.IInterstitialDismissCallback() {
							@Override
							public void onInterstitialDismissed() {
								wv.loadUrl(url);
							}
						}, sns, url, type);
					} else {
						if (configService.getSocialAdTime() >= 0) {
							AdHelper.initDiscoverInterstitialAd(getAttachedObject(), configService.getSocialAdTime(), domain,
									sns, url, type);
						}
					}
				}
			}
		} else {
			if (configService.isIntersAdEnable()) {
				if (UiHelper.needShow(configService.getIntersAdPercent())) {
					if (UiHelper.needShow(configService.getIntersFacebookAdPercent())) {
						AdHelper.showArticleFacebookInterstitialAd(getAttachedObject(), new AdHelper.IInterstitialDismissCallback() {
							@Override
							public void onInterstitialDismissed() {
								wv.loadUrl(url);
							}
						}, id, title, type);
					} else {
						if (configService.getIntersAdTime() >= 0) {
							NewsItem ni = dataService.getNewsById(dataService.getCurNid());
							if (ni != null)
								AdHelper.initArticleInterstitialAd(getAttachedObject(), configService.getIntersAdTime(), ni.getPdomain(), id, title, type);
						}
					}
				}
			}
		}

	}

	@Override
	public void onHide() {
//
//		if (wv != null)
//			wv.reload();
//

		super.onHide();
	}
}





