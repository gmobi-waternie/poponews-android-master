package com.gmobi.poponews.service;

import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.util.UserInfoCallBack;
import com.momock.service.IService;

/**
 * Created by Administrator on 6/19 0019.
 */
public interface IUserService extends IService{
	String USER_TYPE_LOCAL = "poponews";
	String USER_TYPE_FACEBOOK = "facebook";
	String USER_TYPE_TWITTER = "twitter";
	String USER_TYPE_GOOGLE = "google";



	void doLogin(Object extra);
	boolean isLogged();

	void doThirdLogin(String openid,String name,String avatar,String email,String uid, String type, boolean bind);
	void doLocalLogin(String email,String name,String pwd,boolean bind);

	void doRegister(CommentUserInfo info, UserInfoCallBack callBack);

	void doUnBind(String uid, String type, String openId);

	void setLogin(boolean isLogin);
	boolean isLogin();

	void setUserInfo(CommentUserInfo info);
	CommentUserInfo getUserInfo();






}
