package com.gmobi.poponews.app;

public interface MessageTopics {
	String NEWS_PAGE_CHANGE = "msg://NewsPageChanged";
	String CONNECT_ERROR= "msg://ConnectError";
	String DATA_ERROR= "msg://DataError";
	String CONTENT_ERROR= "msg://ContentError";
	String CATEGORY_LOADED= "msg://CategoryLoaded";
	String NEWS_LOADED= "msg://NewsLoaded";
	String REFRESH_FINISH= "msg://RefreshFinish";
	String NEWS_CONTENT_LOADED= "msg://NewsContentLoaded";
	String NEWS_CONTENT_LOADED_COMMENT= "msg://NewsContentLoadedComment";
	String SHOW_CURRENT_NEWS= "msg://ShowCurNews";
	String NEWS_HTML_READY= "msg://NewsHtmlReady";
	
	String LOAD_MORE_CONTENT= "msg://LoadMoreContent";
	String REFRESH_MORE_CONTENT= "msg://FreshMoreContent";
	String CATEGORY_CHANGED= "msg://CategoryChanged";
	
	String FORCE_UPDATE= "msg://ForceUpdate";
	String NEED_UPDATE= "msg://NeedUpdate";
	String UPDATE_PROCESS= "msg://UpdateProgress";

	String COMMENT_NEWS = "msg//CommentNews";
	String COMMENT_HOTS = "msg//CommentHots";
	String COMMENT_CHANNEL = "msg//CommentChannel";
	String REGISTER_ERROR = "msg//RegisterError";
	String LOGIN_ERROR = "msg//LoginError";

	int SYSTEM_UPDATE_PROCESS = 1;
	int SYSTEM_UPDATE_CHECK = 2;
    int SYSTEM_CONNECT_SERVER = 3;
    int SYSTEM_ENTRY_MAIN = 4;
	int SYSTEM_EDITION_READY = 5;
	int SYSTEM_MSG_EDITION_READY = 6;
	int SYSTEM_MSG_EDITION_FAIL = 7;


	int SYSTEM_MSG_EDITION_IMG_READY = 8;
	int SYSTEM_MSG_REQUEST_PERMISSION = 9;
	int SYSTEM_MSG_REQUEST_PERMISSION_OK = 10;
	int SYSTEM_MSG_REQUEST_PERMISSION_FAIL = 11;


	String EDITION_LIST_READY = "msg://editionListReady";
	String EDITION_LIST_READY_SPLASH = "msg://editionListReadySplash";

    String PUSH_CONTENT_READY = "msg://PushContentReady";
    
    String UPDATE_OFFLINE_PROGRESS= "msg://UpdateOfflineProgress";
    String UPDATE_OFFLINE_FINISH= "msg://UpdateOfflineFinish";

	String UPDATE_OFFLINEALARM_START= "msg://UpdateOfflineAlarmStart";

	String GET_FACEBOOK_FRIENDS= "msg://GetFacebookFriends";
	String GET_FACEBOOK_ME= "msg://GetFacebookMe";
	String GET_FACEBOOK_ME_LOGIN= "msg://GetFacebookMeLogin";
	String GET_FACEBOOK_ME_BIND= "msg://GetFacebookMeBind";

	String GET_FACEBOOK_LIKES_MAIN= "msg://GetFacebookLikesMain";
	String GET_FACEBOOK_LIKES_SETTING= "msg://GetFacebookLikesSetting";

	String GET_FACEBOOK_POST= "msg://GetFacebookPost";
	String BIND_FACEBOOK= "msg://bindFacebook";

	String LOGIN_FACEBOOK_FAIL= "msg://LoginFacebookFail";
	String LOGIN_TWITTER_FAIL= "msg://LoginTwitterFail";
	String LOGIN_WEIBO_FAIL= "msg://LoginWeiboFail";

	String GET_GOOGLE_CHANNEL_MAIN= "msg://GetGoogleChannelMain";
	String GET_GOOGLE_CHANNEL_SETTING= "msg://GetGoogleChannelSetting";
	String GET_GOOGLE_ME_LOGIN= "msg://GetGoogleMeLogin";
	String GET_GOOGLE_ME_BIND= "msg://GetGoogleMeBind";
	String GET_GOOGLE_POST= "msg://GetGooglePost";
	String BIND_GOOGLE= "msg://bindGoogle";


	String GET_TWITTER_CHANNEL_MAIN= "msg://GetTwitterChannelMain";
	String GET_TWITTER_CHANNEL_SETTING= "msg://GetTwitterChannelSetting";
	String GET_TWITTER_ME_LOGIN= "msg://GetTwitterMeLogin";
	String GET_TWITTER_ME_BIND= "msg://GetTwitterMeBind";
	String GET_TWITTER_CHANNEL_FAIL= "msg://GetTwitterChannelFail";
	String GET_TWITTER_POST= "msg://GetTwitterPost";
	String BIND_TWITTER= "msg://bindTwitter";

	String GET_WEIBO_CHANNEL_MAIN= "msg://GetWeiboChannelMain";
	String GET_WEIBO_CHANNEL_SETTING= "msg://GetWeiboChannelSetting";
	String GET_WEIBO_CHANNEL_FAIL= "msg://GetWeiboChannelFail";
	String GET_WEIBO_POST= "msg://GetWeiboPost";

	String GET_BAIDU_CHANNEL_MAIN= "msg://GetBaiduChannelMain";
	String GET_BAIDU_CHANNEL_SETTING= "msg://GetBaiduChannelSetting";
	String GET_BAIDU_POST= "msg://GetBaiduPost";

	String GET_PART_OF_POST= "msg://GetPartOfPost";


	String CLEAR_REFRESH_UI= "msg://ClearRefreshUI";
	String CLEAR_TOP_BTN= "msg://ClearTopBtn";
	String CLOSE_ARC_MENU= "msg://CloseArcMenu";


	String AD_READY= "msg://adReady";
	String INTERSTITIAL_AD_CLOSE= "msg://adClose";



	String SHARE_SUCCESS= "msg://shareSuccess";
	String SHARE_FAIL= "msg://shareFail";
	String SHARE_MORE= "msg://shareMore";


	String CACHE_INIT_FINISH= "msg://CacheInitFinish";

	String USER_LOGIN_FAIL= "msg://UserLoginFail";
	String USER_LOGIN_CANCEL= "msg://UserLoginCancel";
	String USER_LOGIN_SUCCESS= "msg://UserLoginSuccess";

	String USER_BIND_FAIL= "msg://UserBindFail";
	String USER_BIND_SUCCESS= "msg://UserBindSuccess";

	String USER_UNBIND_SUCCESS = "msg://UserUnBindSuccess";
	String USER_UNBIND_FAIL = "msg://UserUnBindFail";

	String INIT_POPUP_WRITE_COMMENT = "msg://InitPopupWriteComment";



}

