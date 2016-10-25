package com.gmobi.poponews.share;

import android.content.Context;
import android.os.Bundle;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.PopoApplication;
import com.momock.app.App;

import com.tencent.connect.share.QQShare;

public class QqShare implements IShare {
	private boolean avail;

	private String name;
	private String title;

	private final static String NAME = "qq";



	@Override
	public int getControlDrawable() {
		return R.drawable.share_qq_friend;
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
	public void share(String title, String webUrl, String imageUri) {
		
	    final Bundle params = new Bundle();
	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
	    params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  webUrl);
	    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  webUrl);
	    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,imageUri);
	    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  App.get().getResources().getString(R.string.app_name));
	    params.putString(QQShare.SHARE_TO_QQ_EXT_INT,  "其他附加功能");		
	    PopoApplication.mTencent.shareToQQ(App.get().getCurrentActivity(), params, new QQBaseUiListener());
	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}




}
