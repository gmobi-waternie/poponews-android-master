package com.gmobi.poponews.service;

import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.momock.data.IDataList;
import com.momock.service.IService;

/**
 * Created by Administrator on 8/5 0005.
 *
 *
 * Social服务可能有3种状态
 * 1.是否可用，isEnabled，setEnabled，是由服务器下发的数据决定的，social section中如果有该项，则enable
 * 2.是否打开，这是由用户决定的，在SocialSetting（用户社交设置）类中设定
 * 3.是否已经登录，这是由用户之前的操作决定，
 * Facebook:通过AccessToken.getCurrentAccessToken()是否为空来判断。
 * Google：默认登录，不可修改
 */
public interface ISocialService extends IService {
	int FROM_LOGIN = 0;
	int FROM_MAIN = 1;
	int FROM_SETTING = 2;
	int FROM_BIND = 3;


	String getTitle();
	void setTitle(String title);

	boolean isLogged();
	boolean isBinded();


	boolean isEnabled();
	void setEnabled(boolean e);



	String getCacheData();

	void setCacheData(String data);

	void doLogin(Object extra, int from);
	boolean doBind(Object extra);
	void doUnbind(Object extra);

	void doLogout(Object extra);


	boolean hasFetchedData();

	void setFetchedData(boolean f);

	boolean hasFetchedList();

	void setFetchedList(boolean f);

	void syncData();



	int NOT_UPDATE = 0;
	int IS_REFRESHING = 1;
	int IS_LOADINGMORE= 2;

	int getUpdateStatus();
	void setUpdateStatus(int f);

}

