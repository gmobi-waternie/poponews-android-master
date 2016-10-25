package com.gmobi.poponews.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.PopoApplication;
import com.momock.app.App;

import com.tencent.connect.share.QQShare;

public class TwShare implements IShare {
	private boolean avail;
	private final static String NAME = "twitter";


	private String name;
	private String title;



	@Override
	public int getControlDrawable() {
		return R.drawable.share_twitter;
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
		String tweetUrl = "https://twitter.com/intent/tweet?text="+title+"&url="
                + webUrl;
		Uri uri = Uri.parse(tweetUrl);
		App.get().getCurrentActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}



}
