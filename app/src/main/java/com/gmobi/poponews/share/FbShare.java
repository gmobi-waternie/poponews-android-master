package com.gmobi.poponews.share;

import android.net.Uri;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.gmobi.poponews.R;
import com.momock.app.App;

public class FbShare implements IShare {
	private boolean avail;
	private final static String NAME = "facebook";


	private String name;
	private String title;


	@Override
	public int getControlDrawable() {
		return R.drawable.share_facebook;
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
		ShareLinkContent content = new ShareLinkContent.Builder()
				.setContentTitle(title)
				.setContentUrl(Uri.parse(webUrl))
				.setImageUrl(Uri.parse(imageUri))
				.build();

		ShareDialog.show(App.get().getCurrentActivity(), content);
	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}


}
