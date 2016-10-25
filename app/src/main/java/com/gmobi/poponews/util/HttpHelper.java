/*******************************************************************************
 * Copyright 2012 momock.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gmobi.poponews.util;

import android.content.Context;
import android.telephony.TelephonyManager;


import com.gmobi.poponews.BuildConfig;
import com.momock.app.App;
import com.momock.util.FileHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpHelper {
	public static class Response {
		private int statusCode;
		private String body;
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
	}

	public static final int HTTP_GET = 1;
	public static final int HTTP_POST = 2;

	public static String getParamString(Map<String, String> params){
		List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
		for (String key : params.keySet()) {
			lparams.add(new BasicNameValuePair(key, params.get(key)));			
		}
		return URLEncodedUtils.format(lparams, "UTF-8");
	}
	public static String getFullUrl(String url, Map<String, String> params) {
		if (url == null) return null;
		if (params == null)
			return url;
		return url + (url.lastIndexOf('?') == -1 ? "?" : "&") + getParamString(params);
	}

	public static Response doGet(String url, Map<String, String> params) {
		return doRequest(getFullUrl(url, params), null, HTTP_GET);
	}

	public static Response doPost(String url, Map<String, String> params) {
		return doRequest(getFullUrl(url, params), null, HTTP_POST);
	}
	public static Response doPost(String url, Map<String, String> params, String body) {
		return doRequest(getFullUrl(url, params), body, HTTP_POST);
	}
	public static Response doPost(String url, Map<String, String> params, JSONObject body) {
		return doRequest(getFullUrl(url, params), body, HTTP_POST);
	}
	public static int download(String url, String file){
		return download(url, new File(file));
	}
	public static int download(String url, File file){
		HttpURLConnection connection = null;
		int length = 0;
		try {
			URL httpURL = new URL(url);
			connection = (HttpURLConnection) httpURL.openConnection();
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(30000);
			length = connection.getContentLength();
			FileHelper.copy(connection.getInputStream(), file);
			connection = null;
			
		} catch (Exception e) {			
			Logger.error(e);
		}
		return length;
	}
	public static Response upload(String url, InputStream is){
		Response response = new Response();
		String boundary = Long.toHexString(System.currentTimeMillis()); 
		HttpURLConnection connection = null;
		try{
			URL httpURL = new URL(url);
			connection = (HttpURLConnection) httpURL.openConnection();
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(30000);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			byte[] st = ("--" + boundary + "\r\n" + 
					"Content-Disposition: form-data; name=\"file\"; filename=\"data\"\r\n" + 
					"Content-Type: application/octet-stream; charset=UTF-8\r\n" +
					"Content-Transfer-Encoding: binary\r\n\r\n").getBytes();
			byte[] en = ("\r\n--" + boundary + "--\r\n").getBytes();			
            connection.setRequestProperty("Content-Length", String.valueOf(st.length + en.length + is.available()));
			OutputStream os = connection.getOutputStream();
			os.write(st);
            FileHelper.copy(is, os);
            os.write(en);
            os.flush();
            os.close();  
            response.setStatusCode(connection.getResponseCode());
			connection = null;  
		}catch(Exception e){
			Logger.error(e);
		}

		return response;
	}
	static boolean initialized = false;
	static void disableSslCheck(){
		if (initialized) return;
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };

		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			initialized = true;
		} catch (Exception e) {
			Logger.error(e);
		}

	}
	private static Response doRequest(String url, Object raw, int method) {
		disableSslCheck();
		boolean isJson = raw instanceof JSONObject;
		String body = raw == null ? null : raw.toString();
		Response response = new Response();
		HttpURLConnection connection = null;
		try {
			URL httpURL = new URL(url);
			connection = (HttpURLConnection) httpURL.openConnection();
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(30000);
			connection.setUseCaches(false); 
			if (method == HTTP_POST)
				connection.setRequestMethod("POST");
			if (body != null){
				if (isJson){
					connection.setRequestProperty("Accept", "application/json");
					connection.setRequestProperty("Content-Type", "application/json");
				}
				OutputStream os = connection.getOutputStream();
	            OutputStreamWriter osw = new OutputStreamWriter(os);
	            osw.write(body);
	            osw.flush();
	            osw.close();
			}
			InputStream in = connection.getInputStream();
			response.setBody(FileHelper.readText(in, "UTF-8"));
			response.setStatusCode(connection.getResponseCode());
			in.close();
			connection.disconnect();
			connection = null;
		} catch (Exception e) {
			Logger.error(e);
			try {
				if ((connection != null) && (response.getBody() == null) && (connection.getErrorStream() != null)) {
					response.setBody(FileHelper.readText(connection.getErrorStream(), "UTF-8"));
				} 
			} catch (Exception ex) {
				Logger.error(ex);
			}
		}
		return response;
	}



	private Context mContext;

	public HttpHelper(Context ctx)
	{
		mContext =ctx;
	}

	private final static int FETCH_COUNT = 10;
	private final static int OFFLINE_FETCH_COUNT = 100;
	private static final String BEFORE_TEMPLATE_URL="/api/news/list?cid={cid}&before={time}&count={count}&did={did}";
	private static final String AFTER_TEMPLATE_URL="/api/news/list?cid={cid}&after={time}&count={count}&did={did}";

	//时间标志位
	public static final int EARLY_TIME = 0;//比某时间早
	public static final int LATER_TIME = 1;//比某时间晚


	public static String getEntryBaseUrl() {
		return BuildConfig.BASE_URL;
	}


	private String getEditionListUrl() {
		return BuildConfig.BASE_URL + "/api/news/group/" + BuildConfig.GROUP;
	}


	public static String getImageBaseUrl() {
		return	getEntryBaseUrl()+"/files/";

	}

	private String getConnectUrl()
	{
		StringBuilder url = new StringBuilder(128);
		url.append(getEntryBaseUrl());
		url.append("/api/news/connect?group=");
		url.append(BuildConfig.GROUP);
		String ch = PreferenceHelper.getCurChannel(mContext);
		Logger.debug("use ch:" + ch);
		if(!ch.equals("")){
			url.append("&channel=");
			url.append(ch);
		}
		else
		{

			String installerCh = PreferenceHelper.getInstallerChannel(App.get());
			if (!installerCh.equals(""))
			{
				url.append("&channel=");
				url.append(installerCh);
			}
			Logger.debug("use installerCh:" + installerCh);
		}
		return url.toString();
	}

	private String getDefaultReportUrl(){
		return getEntryBaseUrl()+"/api/news/data";
	}

	private String getLatestNewsListUrl(String cid,long utc_time)
	{
		String url = getEntryBaseUrl()+AFTER_TEMPLATE_URL;
		String did = PreferenceHelper.getDid(mContext);
		return url.replace("{cid}", URLEncoder.encode(cid)).replace("{time}", utc_time+"").replace("{count}", FETCH_COUNT+"").replace("{did}", did+"");
	}

	private String getEarlyNewsListUrl(String cid,long utc_time)
	{
		String url = getEntryBaseUrl()+BEFORE_TEMPLATE_URL;
		String did = PreferenceHelper.getDid(mContext);
		return url.replace("{cid}", URLEncoder.encode(cid)).replace("{time}", utc_time+"").replace("{count}", FETCH_COUNT+"").replace("{did}", did + "");
	}



	public JSONObject getDeviceInfo() {
		;
		TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		String imei = mTelephonyMgr.getDeviceId();
		JSONObject json = new JSONObject();
		try {
			json.put("app", mContext.getPackageName());
			json.put("ch", PreferenceHelper.getCurChannel(mContext));
			json.put("group", BuildConfig.GROUP);
			json.put("app_v", SystemHelper.getAppVersion(mContext));
			json.put("imsi", imsi);
			json.put("imei", imei);
			json.put("sd", SystemHelper.hasSdcard(mContext));
			json.put("ua", SystemHelper.getUA(false));
			json.put("os", "android");
			json.put("os_v", SystemHelper.getOsVersion());
			json.put("lang", Locale.getDefault().getLanguage());
			json.put("country", SystemHelper.getCountry(mContext));
			json.put("wmac", SystemHelper.getWifiMac(mContext));
			json.put("bmac", "");
			json.put("sn", SystemHelper.getAndroidId(mContext));
			json.put("sa", SystemHelper.isSystemApp(mContext));
			json.put("sw", SystemHelper.getScreenWidth(mContext));
			json.put("sh", SystemHelper.getScreenHeight(mContext));

			json.put("dch", BuildConfig.DISTRIBUTION_CHANNEL);
			json.put("gref", new JSONObject("{}"));

			Logger.debug("Send Device Info: " + json.toString(4));
		} catch (JSONException e) {
			Logger.error(e);
		}
		return json;
	}

	public String getEdition(){

		String connectUrl = getEditionListUrl();
		Logger.debug(connectUrl);

		HttpHelper.Response rsp = HttpHelper.doGet(connectUrl, null);
		return rsp.getBody();
	}


	public String getCategory() {

		String connectUrl = getConnectUrl();
		Logger.debug(connectUrl);



		JSONObject deviceInfo = getDeviceInfo();
		JSONObject jo = new JSONObject();


		try {
			jo.put("device", deviceInfo);
			if(PreferenceHelper.getDid(mContext) != null)
				jo.put("did", PreferenceHelper.getDid(mContext));
			Logger.debug("POPONews device = " + deviceInfo.toString());

			HttpHelper.Response rsp = HttpHelper.doGet(connectUrl, null);
			ProcessConnect(rsp.getBody());
			return rsp.getBody();


		} catch (JSONException e1) {

			e1.printStackTrace();
			return null;
		}



	}



	public String getList(final String cid,long time, int TimeFlag) {
		long curTime = TimeUtil.getInstance().getCurUtcTime();
		if(time == 0)
			time = curTime;

		String connectUrl = "";

		if(TimeFlag == EARLY_TIME)
			connectUrl = getEarlyNewsListUrl(cid,time);
		else
			connectUrl = getLatestNewsListUrl(cid,time);

		Logger.debug(connectUrl);
		HttpHelper.Response rsp = HttpHelper.doGet(connectUrl,null);
		return rsp.getBody();

	}


	//Connect API RSP JSON数据的TAG定义
	public static final String TAG_DID = "did";
	public static final String TAG_BASEURL = "baseUrl";
	public static final String TAG_UPDATE = "update";
	public static final String TAG_VERSION = "lastVersion";
	public static final String TAG_UPDATE_FILE = "update_file";
	public static final String TAG_UPDATE_RN = "rn";

	public static final String TAG_EDITION_CHANNEL = "channel";
	public static final String TAG_CATEGORIES = "categories";

	public static final String TAG_EDITION_INFO = "channelInfo";
	public static final String TAG_EDITION_LANG = "lang";
	public static final String TAG_EDITION_COUNTRY = "country";
	public static final String TAG_EDITION_AS_CHANNEL = "minikit";
	public static final String TAG_AD1 = "ad1";
	public static final String TAG_AD2 = "ad2";
	public static final String TAG_AD3 = "ad3";
	public static final String TAG_AD4 = "ad4";
	public static final String TAG_AD_ENABLED = "enabled";
	public static final String TAG_AD_COUNT = "count";
	public static final String TAG_AD_TIME = "time";
	public static final String TAG_AD_PERCENT = "percent";
	public static final String TAG_DCH = "dch";


	private void ProcessConnect(String data)
	{
		JSONObject jn;
		try {
			jn = new JSONObject(data);

			String base_url = null;
			if((base_url = jn.getString(TAG_BASEURL)) != null)
				PreferenceHelper.setFileBaseUrl(mContext, base_url);

			String did = null;
			if((did = jn.getString(TAG_DID)) != null)
				PreferenceHelper.setDid(mContext,did);

			JSONObject editionInfo = jn.getJSONObject(TAG_EDITION_INFO);
			PreferenceHelper.updateEditonConfigure(mContext,jn.getString(TAG_EDITION_CHANNEL),
					editionInfo.getString(TAG_EDITION_COUNTRY),
					editionInfo.getString(TAG_EDITION_LANG));


			if (editionInfo.has(TAG_AD1)) {
				JSONObject adInfo = editionInfo.getJSONObject(TAG_AD1);
				if (adInfo.has(TAG_AD_COUNT))
					PreferenceHelper.updateNativeAdConfigure(mContext,adInfo.getBoolean(TAG_AD_ENABLED), adInfo.getInt(TAG_AD_COUNT));

			}
			String dch = null;
			if(jn.has(TAG_DCH)) {
				if ((dch = jn.getString(TAG_DCH)) != null)
					PreferenceHelper.setDch(mContext,dch);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


}
