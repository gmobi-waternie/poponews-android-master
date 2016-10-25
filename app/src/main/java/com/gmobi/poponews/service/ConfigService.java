package com.gmobi.poponews.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.ConfigNames;
import com.gmobi.poponews.app.PopoApplication;

import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.util.MD5;
import com.gmobi.poponews.util.PreferenceHelper;
import com.momock.app.App;
import com.momock.data.Settings;
import com.momock.service.IJsonService;
import com.momock.service.IRService;
import com.momock.service.ISystemService;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.inject.Inject;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

public class ConfigService implements IConfigService {


	@Inject
	ISystemService systemService;
	@Inject
	Resources resources;
	@Inject
	IJsonService jsonService;


	//public static final String BASE_URL="http://test.poponews.net";
	//public static final String BASE_URL="http://54.254.236.57:6060";
	public static final String SHARE_URL = "{this}/api/news/to/:{nid}";
//	public static final String STORE_URL_SUFFIX = "index.html?Channel=TrendMicro.Acc.new&locale=tw&country=tw#games";


	public static final int UPDATE_NONE = 0;
	public static final int UPDATE_NEED = 1;
	public static final int UPDATE_FORCE = 2;

	public static final int AD_FILTER_IMAGE = 0;
	public static final int AD_FILTER_VIDEO = 1;


	private int updateflag;
	private String updateUrl = null;
	private String updateVersion = null;
	private String did;

	private String basefileUrl = "";
	private String baseShareUrl = "";

	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{ISystemService.class, IRService.class};
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}


	@Override
	public boolean canStop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEntryBaseUrl() {
		return BuildConfig.BASE_URL;
	}


	@Override
	public String getBaseUrl() {
		if (basefileUrl.equals(""))
			basefileUrl = getEntryBaseUrl() + "/files/";
		return basefileUrl;
	}

	@Override
	public void setBaseUrl(String baseUrl) {
		basefileUrl = baseUrl.replace("{this}", getEntryBaseUrl());
	}

	@Override
	public String getShareUrl(String nid) {
		baseShareUrl = SHARE_URL.replace("{this}", getEntryBaseUrl()).replace("{nid}", nid);
		return baseShareUrl;
	}


	@Override
	public void setUpdateFlag(int flag) {
		updateflag = flag;
	}

	@Override
	public int getUpdateFlag() {
		return updateflag;
	}

	@Override
	public void setDid(String did) {
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTINGS_KEY_DID, did);
	}

	@Override
	public String getDid() {
		Settings st = PopoApplication.getSetting();
		String did = null;
		if (st != null)
			did = st.getStringProperty(ConfigNames.SETTINGS_KEY_DID, null);
		return did;
	}

	@Override
	public String getCurChannel() {
		Settings st = PopoApplication.getSetting();
		return st.getStringProperty(ConfigNames.SETTINGS_KEY_CHANNEL, "");

	}


	@Override
	public String getCurStoreChannel() {
		Settings st = PopoApplication.getSetting();
		return st.getStringProperty(ConfigNames.SETTING_KEY_STORE_CHANNEL, "gmobi");
	}

	@Override
	public String getCurLang() {
		Settings st = PopoApplication.getSetting();
		return st.getStringProperty(ConfigNames.SETTING_KEY_LANG, "");
	}

	public String getCurCountry() {
		Settings st = PopoApplication.getSetting();
		return st.getStringProperty(ConfigNames.SETTING_KEY_COUNTRY, "");
	}

	public void updateCurEdition(String ch, String lang, String country, String asCh) {
		Settings st = PopoApplication.getSetting();
		String val;
		if (null != ch) {
			val = getCurChannel();
			if (val.isEmpty() || !val.equals(ch)) {
				st.setProperty(ConfigNames.SETTINGS_KEY_CHANNEL, ch);
			}
		}
		if (null != lang) {
			val = getCurLang();
			if (val.isEmpty() || !val.equals(lang)) {
				st.setProperty(ConfigNames.SETTING_KEY_LANG, lang);
			}
		}
		if (null != country) {
			val = getCurCountry();
			if (val.isEmpty() || !val.equals(country)) {
				st.setProperty(ConfigNames.SETTING_KEY_COUNTRY, country);
			}
		}
		if (null != asCh) {
			val = getCurStoreChannel();
			if (null == val || !val.equals(asCh)) {
				st.setProperty(ConfigNames.SETTING_KEY_STORE_CHANNEL, asCh);
			}
		}
	}

	@Override
	public void updateNativeAdConfigure(boolean enable, int count, int fbper) {
		Settings st = PopoApplication.getSetting();

		st.setProperty(ConfigNames.SETTINGS_KEY_NATIVE_AD_ENABLE, enable);
		st.setProperty(ConfigNames.SETTINGS_KEY_NATIVE_AD_COUNT, count);
		st.setProperty(ConfigNames.SETTINGS_KEY_NATIVE_FACEBOOK_AD_PERCENT, fbper);
	}

	@Override
	public void updateInterstitialAdConfigure(boolean enable, int time, int per, int fbper) {
		Settings st = PopoApplication.getSetting();

		st.setProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_AD_ENABLE, enable);
		st.setProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_AD_TIME, time);
		st.setProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_AD_PERCENT, per);
		st.setProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_FACEBOOK_AD_PERCENT, fbper);
	}

	@Override
	public void updateBannerAdConfigure(boolean enable, int fbper) {
		Settings st = PopoApplication.getSetting();

		st.setProperty(ConfigNames.SETTINGS_KEY_BANNER_AD_ENABLE, enable);
		st.setProperty(ConfigNames.SETTINGS_KEY_BANNER_FACEBOOK_AD_PERCENT, fbper);
	}


	@Override
	public void updateArticleNativeAdConfigure(boolean enable, int per) {
		Settings st = PopoApplication.getSetting();

		st.setProperty(ConfigNames.SETTINGS_KEY_AR_NATIVE_AD_ENABLE, enable);
		st.setProperty(ConfigNames.SETTINGS_KEY_AR_NATIVE_PERCENT, per);
	}

	@Override
	public void updateSocialAdConfigure(boolean enable, int time, int per, int fbper) {
		Settings st = PopoApplication.getSetting();

		st.setProperty(ConfigNames.SETTINGS_KEY_SOCIAL_AD_ENABLE, enable);
		st.setProperty(ConfigNames.SETTINGS_KEY_SOCIAL_AD_TIME, time);
		st.setProperty(ConfigNames.SETTINGS_KEY_SOCIAL_AD_PERCENT, per);
		st.setProperty(ConfigNames.SETTINGS_KEY_SOCIAL_FACEBOOK_AD_PERCENT, fbper);
	}


	@Override
	public int getNativeAdCount() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_NATIVE_AD_COUNT, 0);
	}

	@Override
	public int getNativeFacebookAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_NATIVE_FACEBOOK_AD_PERCENT, 0);
	}

	@Override
	public boolean isNativeAdEnable() {
		Settings st = PopoApplication.getSetting();
		return st.getBooleanProperty(ConfigNames.SETTINGS_KEY_NATIVE_AD_ENABLE, false);
	}


	@Override
	public int getIntersAdTime() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_AD_TIME, -1);
	}

	@Override
	public int getIntersAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_AD_PERCENT, 0);
	}

	@Override
	public int getIntersFacebookAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_FACEBOOK_AD_PERCENT, 0);
	}

	@Override
	public boolean isIntersAdEnable() {
		Settings st = PopoApplication.getSetting();
		return st.getBooleanProperty(ConfigNames.SETTINGS_KEY_INTERSTITIAL_AD_ENABLE, false);
	}


	@Override
	public boolean isArticleNativeAdEnable() {
		Settings st = PopoApplication.getSetting();
		return st.getBooleanProperty(ConfigNames.SETTINGS_KEY_AR_NATIVE_AD_ENABLE, false);
	}

	@Override
	public int getArticleNativeAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_AR_NATIVE_PERCENT, 0);
	}


	@Override
	public boolean isBannerAdEnable() {
		Settings st = PopoApplication.getSetting();
		return st.getBooleanProperty(ConfigNames.SETTINGS_KEY_BANNER_AD_ENABLE, false);
	}

	@Override
	public int getBannerFacebookAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_BANNER_FACEBOOK_AD_PERCENT, 0);
	}


	@Override
	public int getSocialAdTime() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_SOCIAL_AD_TIME, -1);
	}

	@Override
	public int getSocialAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_SOCIAL_AD_PERCENT, 0);
	}

	@Override
	public boolean isSocialAdEnable() {
		Settings st = PopoApplication.getSetting();
		return st.getBooleanProperty(ConfigNames.SETTINGS_KEY_SOCIAL_AD_ENABLE, false);
	}

	@Override
	public int getSocialFacebookAdPercent() {
		Settings st = PopoApplication.getSetting();
		return st.getIntProperty(ConfigNames.SETTINGS_KEY_SOCIAL_FACEBOOK_AD_PERCENT, 0);
	}


	@Override
	public void setEditionListData(String data) {
		if (null == data) {
			return;
		}
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTINGS_KEY_CH_LIST, data);

	}

	@Override
	public boolean isEditionListExist() {
		Settings st = PopoApplication.getSetting();
		return st.hasProperty(ConfigNames.SETTINGS_KEY_CH_LIST);
	}

	@Override
	public JSONObject getEditionList() {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			String data = st.getStringProperty(ConfigNames.SETTINGS_KEY_CH_LIST, null);
			if (null != data) {
				return JsonHelper.parse(data);
			}
		}
		return null;
	}

	@Override
	public String getBaseImageUrl() {
		if (basefileUrl.equals(""))
			basefileUrl = getEntryBaseUrl() + "/files/";
		return basefileUrl;
	}

	@Override
	public void setUpdateFile(String file) {
		updateUrl = file.replace("{this}", getEntryBaseUrl());
	}


	@Override
	public String getUpdateFile() {
		return updateUrl;
	}


	@Override
	public void setUpdateVersion(String version) {
		updateVersion = version;
	}


	@Override
	public String getUpdateVersion() {
		return updateVersion;
	}

	@Override
	public void setFontSize(int size) {
		if (size < 0) {
			return;
		}
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTINGS_KEY_FONTSIZE, size);

	}

	@Override
	public int getFontSize() {
		Settings st = PopoApplication.getSetting();
		int size = 1;
		if (st != null)
			size = st.getIntProperty(ConfigNames.SETTINGS_KEY_FONTSIZE, 1);
		return size;
	}

	@Override
	public String getBuiltinStoreUrl() {
		File f = getFileDir(App.get().getCurrentContext(), CacheNames.STORE_CACHEDIR);
		StringBuilder sb = new StringBuilder(256);
		//sb.append("file://");
		String val = f.getAbsolutePath();
		//sb.append(val);
		sb.append("http://offers.fotapro.com/poponews/?Channel=");
		//sb.append("/index.html?Channel=");
		Settings st = PopoApplication.getSetting();

		sb.append(getCurStoreChannel());
		val = getCurLang();
		if (!val.isEmpty()) {
			sb.append("&locale=");
			sb.append(val);
		}
		val = getCurCountry();
		if (!val.isEmpty()) {
			sb.append("&country=");
			sb.append(val);
		}
		val = sb.toString();
		Logger.info("webview = " + val);
		return val;
	}


	@Override
	public int getNewsFeedCurId() {
		Settings st = PopoApplication.getSetting();
		int id = 1;
		if (st != null)
			id = st.getIntProperty(ConfigNames.SETTINGS_KEY_PUSH_ID, 1);
		return id;

	}

	@Override
	public void setNewsFeedCurId(int id) {
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTINGS_KEY_PUSH_ID, id);

	}


	@Override
	public boolean getNewsFeedFlag() {
		Settings st = PopoApplication.getSetting();
		boolean flag = false;
		if (st != null)
			flag = st.getBooleanProperty(ConfigNames.SETTING_KEY_NEWS_FEED, true);
		return flag;

	}

	@Override
	public void setNewsFeedFlag(boolean flag) {
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTING_KEY_NEWS_FEED, flag);

	}


	@Override
	public boolean getOfflineTimeFlag() {
		Settings st = PopoApplication.getSetting();
		boolean flag = false;
		if (st != null)
			flag = st.getBooleanProperty(ConfigNames.SETTING_KEY_OFFLINE_TIME, false);
		return flag;

	}

	@Override
	public void setOfflineTimeFlag(boolean flag) {
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTING_KEY_OFFLINE_TIME, flag);

	}

	@Override
	public boolean getOfflinePicFlag() {
		Settings st = PopoApplication.getSetting();
		boolean flag = false;
		if (st != null)
			flag = st.getBooleanProperty(ConfigNames.SETTING_KEY_OFFLINE_PIC, false);
		return flag;

	}

	@Override
	public void setOfflinePicFlag(boolean flag) {
		Settings st = PopoApplication.getSetting();
		if (st != null)
			st.setProperty(ConfigNames.SETTING_KEY_OFFLINE_PIC, flag);

	}

	@Override
	public int getOfflineTimeHour() {
		Settings st = PopoApplication.getSetting();
		int hour = 0;
		if (st != null)
			hour = st.getIntProperty(ConfigNames.SETTING_KEY_OFFLINE_HOUR, 0);
		return hour;
	}

	@Override
	public int getOfflineTimeMinute() {
		Settings st = PopoApplication.getSetting();
		int min = 0;
		if (st != null)
			min = st.getIntProperty(ConfigNames.SETTING_KEY_OFFLINE_MINUTE, 0);
		return min;
	}

	@Override
	public void setOfflineTime(int hour, int minute) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTING_KEY_OFFLINE_HOUR, hour);
			st.setProperty(ConfigNames.SETTING_KEY_OFFLINE_MINUTE, minute);
		}
	}


	public static File getFileDir(Context ctx, String category) {
		File fileDir = null;

		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			fileDir = new File(getExternalStorageDirectory().getPath() + "/Android/data/" + ctx.getPackageName() + "/files/");
		} else {
			fileDir = App.get().getExternalFilesDir(null);
		}
		if (fileDir != null && !fileDir.exists()) {
			fileDir.mkdirs();
		}

		File fc = category == null ? fileDir : new File(fileDir, category);
		if (!fc.exists())
			fc.mkdir();
		return fc;
	}


	public float getImageScale() {
		return (float) BuildConfig.IMAGE_SCALE;
	}

	@Override
	public int getCategoryLayoutType(String cid) {
		Settings st = PopoApplication.getSetting();
		int type = 0;
		if (st != null)
			type = st.getIntProperty("layouttype" + cid, 0);
		return type;
	}

	@Override
	public void setCategoryLayoutType(String cid, int type) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty("layouttype" + cid, type);
		}

	}

	@Override
	public boolean isFirstEntry() {
		Settings st = PopoApplication.getSetting();
		boolean type = true;
		if (st != null)
			type = st.getBooleanProperty(ConfigNames.SETTINGS_KEY_FIRST_ENTRY, true);

		if (!type)
			return type;

		String installCh = PreferenceHelper.getInstallerChannel(App.get());
		if (installCh.equals(""))
			return true;
		else {
			setFirstEntry(false);
			return false;
		}
	}

	@Override
	public void setFirstEntry(boolean fe) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_FIRST_ENTRY, fe);
		}
	}


	@Override
	public String getReferrer() {
		Settings st = PopoApplication.getSetting();
		String ref = "";
		if (st != null)
			ref = st.getStringProperty(ConfigNames.SETTINGS_KEY_REFERRER, "{}");
		return ref;
	}

	@Override
	public void setReferrer(String referrer) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_REFERRER, referrer);
		}
	}

	@Override
	public void setDch(String dch) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_DCH, dch);
		}
	}


	public final String SP_KEY_COMMON_DCH = "commondch";

	@Override
	public String getDch() {


		Settings st = PopoApplication.getSetting();
		String dch = "";
		if (st != null) {
			dch = st.getStringProperty(ConfigNames.SETTINGS_KEY_DCH, "");
			Logger.debug("dch from google = " + dch);
		}
		if (dch.equals("")) {
			SharedPreferences sp = App.get().getSharedPreferences("poponewsCommon", Context.MODE_WORLD_READABLE);
			dch = sp.getString(SP_KEY_COMMON_DCH, "");
			Logger.debug("dch from installer = " + dch);

			if (dch.equals("")) {
				Logger.debug("dch return default = " + dch);
				return BuildConfig.DISTRIBUTION_CHANNEL;
			}
		}

		Logger.debug("dch finally return = " + dch);
		return dch;
	}


	@Override
	public boolean isVideoPlayAuto() {

		Settings st = PopoApplication.getSetting();
		boolean auto = false;
		if (st != null)
			auto = st.getBooleanProperty(ConfigNames.SETTINGS_KEY_VIDEO_PLAY_AUTO, false);
		return auto;
	}

	@Override
	public void setVideoPlayAuto(boolean a) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_VIDEO_PLAY_AUTO, a);
		}
	}

	@Override
	public void updateShareSetting(String jsonStr) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_SHARE, jsonStr);
		}
	}

	@Override
	public String getShareSetting() {
		Settings st = PopoApplication.getSetting();
		String share = "";
		if (st != null)
			share = st.getStringProperty(ConfigNames.SETTINGS_KEY_SHARE, "");
		return share;
	}

	@Override
	public void setFacebookUserInfo(String info) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_FACEBOOK_USERINFO, info);
		}
	}

	@Override
	public void setTwitterUserInfo(String info) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_TWITTER_USERINFO, info);
		}
	}

	@Override
	public void setSimilarWebUserInfo(String info) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_SIMILARWEB_USERINFO, info);
		}
	}

	@Override
	public JSONObject getUserInfo() {
		Settings st = PopoApplication.getSetting();

		String facebookInfo;
		String twitterInfo;
		String similarWebInfo;
		JSONObject userInfo = new JSONObject();

		if (st != null) {
			facebookInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_FACEBOOK_USERINFO, "");
			twitterInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_TWITTER_USERINFO, "");
			similarWebInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_SIMILARWEB_USERINFO, "");

			try {
				if (!facebookInfo.equals(""))
					userInfo.put("fb", new JSONObject(facebookInfo));
//				if (!twitterInfo.equals(""))
//					userInfo.put("tw", new JSONObject(twitterInfo));
				if (!similarWebInfo.equals(""))
					userInfo.put("sw", new JSONObject(similarWebInfo));

				return userInfo;
			} catch (JSONException e) {
				e.printStackTrace();
				return new JSONObject();
			}

		}


		return userInfo;
	}

	@Override
	public void saveDeviceInfo(String info) {
		Settings st = PopoApplication.getSetting();
		try {
			String encodeInfo = MD5.encode(info.getBytes("UTF-8"));
			if (st != null) {
				st.setProperty(ConfigNames.SETTINGS_KEY_CONNECTDEVICE, encodeInfo);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	//true:need update;
	@Override
	public boolean checkDeviceInfoData(String info) {
		Settings st = PopoApplication.getSetting();
		if(st != null)
		{

			try {
				String encodeInfo = MD5.encode(info.getBytes("UTF-8"));
				String oldInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_CONNECTDEVICE, "");

				if(encodeInfo.equals(oldInfo))
					return false;

				saveDeviceInfo(info);
				return true;

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}



	@Override
	public boolean setPushId(String token) {
		Settings st = PopoApplication.getSetting();

		if (st != null) {
			String oldToken =  st.getStringProperty(ConfigNames.SETTINGS_KEY_GCMTOKEN, "");
			if(!oldToken.equals(token)) {
				st.setProperty(ConfigNames.SETTINGS_KEY_PREGCMTOKEN, oldToken);
				st.setProperty(ConfigNames.SETTINGS_KEY_GCMTOKEN, token);
				return false;
			}
		}
		return true;

	}

	@Override
	public String getPushId() {
		Settings st = PopoApplication.getSetting();
		return	st.getStringProperty(ConfigNames.SETTINGS_KEY_GCMTOKEN, "");

	}

	@Override
	public String getPrePushId() {
		Settings st = PopoApplication.getSetting();
		return	st.getStringProperty(ConfigNames.SETTINGS_KEY_PREGCMTOKEN, "");

	}


	@Override
	public long getOfflineDownloadTime() {

		Settings st = PopoApplication.getSetting();
		long time= 0L;
		if (st != null)
			time = st.getLongProperty(ConfigNames.SETTINGS_KEY_OFFLINEDOWNLOADTIME, 0);
		return time;
	}

	@Override
	public void setOfflineDownloadTime(long time) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_OFFLINEDOWNLOADTIME, time);
		}
	}


	private static final String CONFIG_FILENAME = "poponewsad.cfg";
	public static File getConfigFile(Context context) {
		File rootdir;
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			rootdir = new File(getExternalStorageDirectory().getPath()+"/"+CONFIG_FILENAME);
		} else {
			rootdir = new File(context.getFilesDir()+"/"+CONFIG_FILENAME);
		}
		return rootdir;
	}

	@Override
	public int getAdFilterType(Context context)
	{
		File cfgFile = getConfigFile(context);
		if(cfgFile.exists())
		{
			try {
				String cfgStr = FileHelper.readText(cfgFile);
				if(cfgStr!=null && !cfgStr.equals(""))
				{
					JSONObject jo = new JSONObject(cfgStr);
					if(jo.has("adtype"))
					{
						String typeInCfg = jo.getString("adtype").toLowerCase();
						if(typeInCfg.equals("video"))
							return AD_FILTER_VIDEO;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return AD_FILTER_IMAGE;
			} catch (JSONException e) {
				e.printStackTrace();
				return AD_FILTER_IMAGE;
			}
		}

		return AD_FILTER_IMAGE;
	}

	@Override
	public void setLoginUserInfo(String jo){
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			if (jo!=null && !jo.equals(""))
				st.setProperty(ConfigNames.SETTINGS_KEY_LOGIN_USERINFO, jo);
			else
			{
				st.setProperty(ConfigNames.SETTINGS_KEY_LOGIN_USERINFO, "");
			}

		}
	}


	@Override
	public JSONObject getLoginUserInfo(){
		Settings st = PopoApplication.getSetting();
		if(st != null)
		{
			String joStr = st.getStringProperty(ConfigNames.SETTINGS_KEY_LOGIN_USERINFO,"");
			try {
				if(joStr.equals(""))
					return null;

				JSONObject jo = new JSONObject(joStr);
				return jo;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

		}

		return null;

	}

	@Override
	public void setAllBindStatus(JSONArray ja) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			st.setProperty(ConfigNames.SETTINGS_KEY_BIND_STATUS, ja.toString());
		}
	}

	@Override
	public JSONArray getAllBindStatus() {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			String bindstr = st.getStringProperty(ConfigNames.SETTINGS_KEY_BIND_STATUS, "");
			if(bindstr.equals(""))
				return null;
			try {
				return new JSONArray(bindstr);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	//获取单个service的绑定状态
	@Override
	public JSONObject getServiceBindStatus(String type) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {
			String bindstr = st.getStringProperty(ConfigNames.SETTINGS_KEY_BIND_STATUS, "");
			if(bindstr.equals(""))
				return null;
			try {

				JSONArray ja =  new JSONArray(bindstr);
				for(int i=0;i<ja.length();i++)
				{
					JSONObject servicebind = (JSONObject) ja.get(i);
					if(servicebind.has("type"))
					{
						if(servicebind.getString("type").equals(type))
							return servicebind;
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	//[
	// {"type":"google","data","{.....}"}
	// ]
	//登录第三方时，保存从nativeSDK API获取到的原始信息
	@Override
	public void insertThirdUserInfo(String type, String data) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {

			try {
				JSONArray allJa;
				JSONObject singleJo = new JSONObject();
				JSONArray copyJa = new JSONArray();

				singleJo.put("type",type);
				singleJo.put("data",new JSONObject(data));

				String allThirdInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_THIRD_USERINFO, "");
				if(allThirdInfo.equals(""))
				{
					allJa= new JSONArray();
					allJa.put(singleJo);

				}
				else
				{
					allJa = new JSONArray(allThirdInfo);


					for(int i=0; i<allJa.length(); i++)
					{
						JSONObject jo = (JSONObject) allJa.get(i);
						if(jo != null && jo.get("type").equals(type))
							continue;

						copyJa.put(jo);
					}

					copyJa.put(singleJo);
				}

				st.setProperty(ConfigNames.SETTINGS_KEY_THIRD_USERINFO, copyJa.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}



		}
	}


	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public synchronized void removeThirdUserInfo(String type) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {

			try {
				String allThirdInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_THIRD_USERINFO, "");
				if(allThirdInfo.equals(""))
					return;

				JSONArray allJa = new JSONArray(allThirdInfo);
				for(int i=0; i<allJa.length(); i++)
				{
					JSONObject singleJo = (JSONObject) allJa.get(i);
					if(singleJo.get("type").equals(type))
					{
						allJa.remove(i);
						break;
					}
				}

				st.setProperty(ConfigNames.SETTINGS_KEY_THIRD_USERINFO, allJa.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public JSONObject getThirdUserInfo(String type) {
		Settings st = PopoApplication.getSetting();
		if (st != null) {

			try {
				String allThirdInfo = st.getStringProperty(ConfigNames.SETTINGS_KEY_THIRD_USERINFO, "");
				if(allThirdInfo.equals(""))
					return null;

				JSONArray allJa = new JSONArray(allThirdInfo);
				for(int i=0; i<allJa.length(); i++)
				{
					JSONObject singleJo = (JSONObject) allJa.get(i);
					if(singleJo.get("type").equals(type))
					{
						return singleJo;
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}




}
