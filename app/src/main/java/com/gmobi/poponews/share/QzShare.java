package com.gmobi.poponews.share;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.PopoApplication;

import com.momock.app.App;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;

public class QzShare implements IShare {
	private boolean avail;
	private final static String NAME = "QZONESHARE";

	private String name;
	private String title;


	@Override
	public int getControlDrawable() {
		return R.drawable.share_qq_zone;
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
		ArrayList<String> imgArray = new ArrayList<String>();
		imgArray.add(imageUri);
		int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);
		
	    params.putString(QzoneShare.SHARE_TO_QQ_TITLE,title);//必填
	    params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "摘要");//选填
	    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, webUrl);//必填
	    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgArray);
	    PopoApplication.mTencent.shareToQzone(App.get().getCurrentActivity(), params, new QQBaseUiListener());
	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}

}
