package com.gmobi.poponews.service;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gmobi.poponews.app.ConfigNames;
import com.gmobi.poponews.app.PopoApplication;
import com.momock.data.Settings;
import com.momock.service.IService;

public interface IConfigService extends IService{
	String getEntryBaseUrl();
	String getBaseUrl();
	String getBaseImageUrl();
	void setBaseUrl(String baseUrl);

	
	void setUpdateFlag(int flag);
	void setUpdateFile(String file);
	int getUpdateFlag();
	String getUpdateFile();
	
	void setDid(String did);
	String getDid();
	String getCurChannel();
    String getCurStoreChannel();
    String getCurCountry();
    String getCurLang();
    boolean isEditionListExist();
    void updateCurEdition(String ch, String lang, String country, String asCh);

	void updateNativeAdConfigure(boolean enable, int count, int fbPer);
	void updateInterstitialAdConfigure(boolean enable, int time,int per,int fbPer);
	void updateBannerAdConfigure(boolean enable,int fbPer);
	void updateSocialAdConfigure(boolean enable, int time,int per,int fbPer);
	void updateArticleNativeAdConfigure(boolean enable, int per);

	int getNativeAdCount();
	int getNativeFacebookAdPercent();
	boolean isNativeAdEnable();


	int getIntersAdTime();
	int getIntersAdPercent();
	boolean isIntersAdEnable();
	int getIntersFacebookAdPercent();

	boolean isBannerAdEnable();
	int getBannerFacebookAdPercent();


	int getSocialAdTime();
	int getSocialAdPercent();
	boolean isSocialAdEnable();
	int getSocialFacebookAdPercent();

	boolean isArticleNativeAdEnable();
	int getArticleNativeAdPercent();



	void setEditionListData(String data);
	JSONObject getEditionList();
	String getShareUrl(String nid);
	void setUpdateVersion(String version);
	String getUpdateVersion();
	
	void setFontSize(int size);
	int getFontSize();
	
	String getBuiltinStoreUrl();
	
	
	boolean getOfflinePicFlag();
	void setOfflinePicFlag(boolean flag);
	boolean getOfflineTimeFlag();
	void setOfflineTimeFlag(boolean flag);
	int getOfflineTimeHour();
	int getOfflineTimeMinute();
	void setOfflineTime(int hour,int minute);
	
	void setNewsFeedFlag(boolean flag);
	boolean getNewsFeedFlag();
	void setNewsFeedCurId(int id);
	int getNewsFeedCurId();
	float getImageScale();

	int getCategoryLayoutType(String cid);
	void setCategoryLayoutType(String cid, int type);

	boolean isFirstEntry();
	void setFirstEntry(boolean fe);

	String getReferrer();
	void setReferrer(String referrer);

	void setDch(String dch);
	String getDch();

	boolean isVideoPlayAuto();
	void setVideoPlayAuto(boolean a);


	void updateShareSetting(String jsonStr);
	String getShareSetting();


	void setFacebookUserInfo(String info);
	void setTwitterUserInfo(String info);
	void setSimilarWebUserInfo(String info);

	JSONObject getUserInfo();

	void saveDeviceInfo(String info);
	boolean checkDeviceInfoData(String info);

	boolean setPushId(String token);
	String getPushId();
	String getPrePushId();



	long getOfflineDownloadTime();
	void setOfflineDownloadTime(long time);

	int getAdFilterType(Context context);

	//设置从服务器取得的user信息,作为全局唯一的userinfo原始数据
	void setLoginUserInfo(String jo);

	//获取从服务器取得的user信息,作为全局唯一的userinfo原始数据
	JSONObject getLoginUserInfo();


	//设置从服务器取得的primary信息
	void setAllBindStatus(JSONArray jo);

	//获取从服务器取得的primary信息
	JSONArray getAllBindStatus();

	//从服务器取得的primary信息中取得指定service的状态
	JSONObject getServiceBindStatus(String type);

	//保存第三方SDK返回的信息
	void insertThirdUserInfo(String type , String info);

	//移除第三方SDK返回的信息
	void removeThirdUserInfo(String type);

	//获取第三方SDK返回的信息
	JSONObject getThirdUserInfo(String type);






}
