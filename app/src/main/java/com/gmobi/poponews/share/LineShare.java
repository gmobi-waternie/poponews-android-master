package com.gmobi.poponews.share;

import android.content.Context;
import android.os.Bundle;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.PopoApplication;
import com.momock.app.App;

import com.tencent.connect.share.QQShare;

public class LineShare implements IShare {
	private boolean avail;
	private final static String NAME = "TWSHARE";


	private String name;
	private String title;




	@Override
	public int getControlDrawable() {
		return R.drawable.share_line;
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
		
	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}




}
