package com.gmobi.poponews.javascript;

import java.io.File;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.cases.browser.BrowserActivity;
import com.gmobi.poponews.model.NewsImage;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.CacheService;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.TimeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.Settings;
import com.momock.service.ICacheService;
import com.momock.util.JsonDatabase.Collection;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;
import com.reach.IAdItem;

public class ExecJsApi {
	WebView appWView;
	Activity webActivity;
	IDataService ds;
	
	String webAppMonitorHdl = null;
	
	String webBackOnPressHdl = null;
	
	public void callWebInstallAppMonitor(Bundle params){
		if (params == null || webAppMonitorHdl == null)
			return;
		try {
			JSONObject obj = new JSONObject();
			obj.put("id", params.getString("id"));
			obj.put("ver", params.getString("ver"));
			callbackJsApi(webAppMonitorHdl, API_CALLBACK_SUCCESS, obj.toString());
		} catch (Exception e) {
			Logger.error(e);
		}
	}
	
	public ExecJsApi(WebView webView, Activity webAct) {
        this.appWView = webView;
        this.webActivity = webAct;
        this.ds = App.get().getService(IDataService.class);
    }
	
	static final String API_GET_DEVICE_INFO = "getDeviceInfo";
	static final String API_WEB_ON_BACK_PRESSED = "webOnBackPressedHdl";
	static final String API_WEB_OPEN_BROWSER = "webOpenBrowser";
	static final String API_DOWNLOAD_APP = "downloadApp";
	static final String API_WEB_OPEN_SRC = "webOpenSrc";
	
	
	static final String API_GET_DATA = "getNewsData";
	static final String API_GET_RELEASETIME = "getNewsTime";
	static final String API_GET_PNAME = "getNewsPname";
	static final String API_SDK_LOG = "loggerDebug";

	static final String API_GET_AD = "getAd";
	static final String API_CLICK_AD = "clickAd";

	

	
	
	
	static final String API_CALLBACK_SUCCESS = "success";
	static final String API_CALLBACK_ERROR = "error";
	
	@JavascriptInterface
	public String exec(String callbackId, String action, String arguments) {
		String ret = "";
		try {
			JSONArray args = null;
			if (arguments != null)
				args = new JSONArray(arguments);
			if(API_SDK_LOG.equals(action))
			{
				String msg = args.isNull(0) ? null : args.getString(0);
				Log.e("JSLOG", msg);
			}
			else if (API_DOWNLOAD_APP.equals(action)) {
				String appId = args.getString(0);
				String url = args.isNull(1) ? null : args.getString(1);
				String title = args.isNull(2) ? null : args.getString(2);
				String eventMsg = args.isNull(3) ? null : args.getString(3);
				if (url != null && url.equals(""))
					url = null;
				if (title != null && title.equals(""))
					title = null;

				Uri u = Uri.parse("market://details?id="+appId);  
				Intent i = new Intent(Intent.ACTION_VIEW,u);  
				webActivity.startActivity(i);
				
				callbackJsApi(callbackId, API_CALLBACK_SUCCESS, "");
			}
			else if (API_WEB_OPEN_BROWSER.equals(action)){
				String url = args.isNull(0) ? null : args.getString(0);
				if (url != null){
					Intent intent = new Intent();

					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					webActivity.startActivity(intent);

				}
			}
			else if (API_GET_DATA.equals(action)){
				String nid = args.isNull(0) ? null : args.getString(0);
				
				if (nid != null){
					NewsItem ni = ds.getNewsById(nid);
					String pDate = TimeUtil.getInstance().getLastTime(ni.getReleaseTime());
					ICacheService cas = App.get().getService(ICacheService.class);
					IConfigService cos = App.get().getService(IConfigService.class);
					
					JSONObject json = new JSONObject();
					
					json.put("title", ni.getTitle());
					json.put("time", pDate);
					json.put("pname", ni.getPname());
					json.put("picon", ni.getPicon());
					json.put("source", ni.getSource());
					json.put("readsource", webActivity.getResources().getString(R.string.webview_readsource));
					json.put("fontsize", cos.getFontSize());
					json.put("nightmode", NightModeUtil.getDayNightMode());
					json.put("entryurl", cos.getEntryBaseUrl());
					json.put("pakname", App.get().getPackageName());
					
					JSONArray jaMm = new JSONArray();
					DataList<NewsImage> mms = ni.getImgs();

					if(mms.getItemCount() > 0) {
						for (int i = 0; i < mms.getItemCount(); i++) {
							JSONObject joMM = new JSONObject();
							NewsImage mm = mms.getItem(i);
							joMM.put("id", mm.getFile());
							joMM.put("w", mm.getWidth());
							joMM.put("h", mm.getHeight());
							joMM.put("desc", mm.getDesc());

							jaMm.put(joMM);
						}


						json.put("mms", jaMm);


						File f = cas.getCacheOf(CacheNames.MY_IMAGE_CACHEDIR, cos.getBaseImageUrl() + ni.getImgs().getItem(0).getFile());
						if (f.exists())
							json.put("local", "1");
						else
							json.put("local", "0");
					}
					else
						json.put("local", "0");

					callbackJsApi(callbackId, API_CALLBACK_SUCCESS, json.toString());
				}
			} else if (API_GET_RELEASETIME.equals(action)){
				String nid = args.isNull(0) ? null : args.getString(0);
				
				if (nid != null){
					NewsItem ni = ds.getNewsById(nid);
					JSONObject json = new JSONObject();
					
					json.put("data", ni.getReleaseTime());
					callbackJsApi(callbackId, API_CALLBACK_SUCCESS, ni.getReleaseTime()+"");
				}
			}
			else if (API_WEB_OPEN_SRC.equals(action)){
				String nid = args.isNull(0) ? null : args.getString(0);
				if (nid != null){
					NewsItem ni = ds.getNewsById(nid);
					UiHelper.openBrowserActivity(webActivity, ni.get_id(), ni.getType(), ni.getSource(), ni.getTitle(), ni.getPdomain(),"");
				}
			}
			else if (API_WEB_ON_BACK_PRESSED.equals(action)){
				String apiStr = args.isNull(0) ? null : args.getString(0);
				
				if (apiStr.equals(""))
					webBackOnPressHdl = null;
				else
					webBackOnPressHdl = apiStr;
				Logger.debug("set web back hdl: " + webAppMonitorHdl);
			}
			else if (API_GET_AD.equals(action)){
				NewsItem  adItem = AdHelper.getInstance(webActivity).getNextAd();
				if(adItem!=null) {
					IAdItem ad = (IAdItem) adItem.getAdObj();
					if (adItem != null) {
						JSONObject json = new JSONObject();


						json.put("title", adItem.getTitle());
						json.put("img", "poponewsad" + CacheService.getInstance(webActivity).getFilenameOf(adItem.getPreview()));
						json.put("pakname", App.get().getPackageName());


						callbackJsApi(callbackId, API_CALLBACK_SUCCESS, json.toString());
					} else {
						JSONObject json = new JSONObject();
						json.put("title", "");
						json.put("img", "");
						json.put("pakname", App.get().getPackageName());

						callbackJsApi(callbackId, API_CALLBACK_ERROR, json.toString());

					}
					ad.execute("report", new Object[]{1});
				}

			}
			else if (API_CLICK_AD.equals(action)){
				NewsItem  adItem = AdHelper.getInstance(webActivity).getCurAdInArticleAdPool();
				IAdItem  ad = (IAdItem) adItem.getAdObj();
				if(ad !=null)
				{
					ad.execute("go", null);
					ad.execute("report", new Object[]{2});
				}

			}

        } catch(Exception e){
        	Logger.error(e);
        }
        return ret;
    }
	
	public boolean onBackPressed(){
		if (webBackOnPressHdl == null || webBackOnPressHdl.equals(""))
			return false;
		
		final Runnable runnable = new Runnable() {
            public void run() {
            	String js = null;
            	
        		js = "try {";
        		js +=  webBackOnPressHdl + "();";
        		js += "}catch(e){console.log('android callback error!');}";
            	
                if (js != null) {
                	appWView.loadUrl("javascript:" + js);
                	Logger.debug("[web][callback] = " + js);
                }
            }
        };
        
        try {
        	if (webActivity != null)
            	webActivity.runOnUiThread(runnable);
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
		
		return true;
	}

	private void callbackJsApi(final String cbId, final String status,final String params){
		final Runnable runnable = new Runnable() {
            public void run() {
            	String js = null;
            	if (params != null && !params.equals("")){
            		js = "try {";
            		js += "callJsCallback(\"" + cbId + "\", \"" + status + "\"," + params +");";
            		js += "}catch(e){console.log('android callback error!');}";
            	}
            	else{
            		js = "try {";
            		js += "callJsCallback(\"" + cbId + "\", \"" + status + "\", null);";
            		js += "}catch(e){console.log('android callback error!');}";
            	}
            	
                if (js != null) {
                	appWView.loadUrl("javascript:" + js);
                }
            }
        };
        
        try {
        	if (webActivity != null)
            	webActivity.runOnUiThread(runnable);
		} catch (Exception e) {
			Logger.error(e);
		}
        
	}
}
