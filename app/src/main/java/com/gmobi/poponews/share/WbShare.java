package com.gmobi.poponews.share;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.PopoApplication;

import com.momock.app.App;
import com.momock.service.ICacheService;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;

public class WbShare implements IShare {
	private boolean avail;
	private final static String NAME = "WEIBOSHARE"; 
	@Inject
	Resources resources;
	
	@Inject
	ICacheService cacheService;


	private String name;
	private String title;



	@Override
	public int getControlDrawable() {
		return R.drawable.share_weibo;
	}

	@Override
	public String getControlName() {
		return NAME;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public boolean isAvailable() {
		return avail;
	}



	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}
	
	
	private WebpageObject getWebpageObj(String title,String url ) {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description = "我是摘要";

        mediaObject.setThumbImage(BitmapFactory.decodeResource(App.get().getCurrentActivity().getResources(), R.drawable.ic_launcher));
        mediaObject.actionUrl = url;
        mediaObject.defaultText = "Webpage 默认文案";
        return mediaObject;
    }
	

    private String getSharedText(String title, String url) {

        String format = resources.getString(R.string.weibosdk_demo_share_webpage_template);
        String text = String.format(format, resources.getString(R.string.app_name)+":"+title, url);
        return text;
    }


    private TextObject getTextObj(String title,String url) {
        TextObject textObject = new TextObject();
        textObject.text = getSharedText(title,url);
        return textObject;
    }
    
    private ImageObject getImageObj(String uri) {
    	
    	File file = cacheService.getCacheOf(CacheNames.IMAGE_CACHEDIR, uri);
    	String pathName = file.getAbsolutePath();
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(BitmapFactory.decodeFile(pathName));
        return imageObject;
    }
    
	
	@Override
	public void share(String title, String webUrl, String imageUri) {
    
	    // 1. 初始化微博的分享消息
	    WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
	
	    weiboMessage.textObject = getTextObj(title,webUrl);
	    weiboMessage.imageObject = getImageObj(imageUri);
	    weiboMessage.mediaObject = getWebpageObj(title,webUrl);
	  
	
	        /*
	    // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
	    if (hasWebpage) {
	        weiboMessage.mediaObject = getWebpageObj();
	    }
	    if (hasMusic) {
	        weiboMessage.mediaObject = getMusicObj();
	    }
	    if (hasVideo) {
	        weiboMessage.mediaObject = getVideoObj();
	    }
	    if (hasVoice) {
	        weiboMessage.mediaObject = getVoiceObj();
	    }*/
	    
	    // 2. 初始化从第三方到微博的消息请求
	    SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
	    // 用transaction唯一标识一个请求
	    request.transaction = String.valueOf(System.currentTimeMillis());
	    request.multiMessage = weiboMessage;
	    
	    
	    AuthInfo authInfo = new AuthInfo(App.get().getCurrentActivity(), WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
	    Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(App.get().getApplicationContext());
	    String token = "";
	    if (accessToken != null) {
	        token = accessToken.getToken();
	    }
	    PopoApplication.mWeiboShareAPI.sendRequest(App.get().getCurrentActivity(), request, authInfo, token, new WeiboAuthListener() {
	        
	        @Override
	        public void onWeiboException( WeiboException arg0 ) {
	        	Log.e("Share","Weibo Share :"+arg0.getMessage());
	        }
	        
	        @Override
	        public void onComplete( Bundle bundle ) {
	            // TODO Auto-generated method stub
	            Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
	            AccessTokenKeeper.writeAccessToken(App.get().getApplicationContext(), newToken);
	            Toast.makeText(App.get().getApplicationContext(), "onAuthorizeComplete token = " + newToken.getToken(), Toast.LENGTH_SHORT).show();
	        }
	        
	        @Override
	        public void onCancel() {
	        }
	    });
    
	}

}
