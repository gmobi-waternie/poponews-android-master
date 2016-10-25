package com.gmobi.poponews.share;

import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.PopoApplication;
import com.momock.app.App;
import com.momock.service.IMessageService;
import com.momock.util.Logger;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class QQBaseUiListener implements IUiListener {
	@Override
	public void onComplete(Object response) {

		App.get().getService(IMessageService.class).send(this, MessageTopics.SHARE_SUCCESS);
		Toast.makeText(App.get(), App.get().getResources().getString(R.string.share_success), Toast.LENGTH_SHORT).show();

		Logger.info("[Share]:QQ share complete");
	}
	
	@Override
	public void onError(UiError e) {
		Toast.makeText(App.get(), App.get().getResources().getString(R.string.share_fail), Toast.LENGTH_SHORT).show();
		Logger.info("[Share]:QQ share fail "+ (e!=null ? e.errorMessage : ""));
	}
	@Override
	public void onCancel() {
		Toast.makeText(App.get(), App.get().getResources().getString(R.string.share_fail), Toast.LENGTH_SHORT).show();
		Logger.info("[Share]:QQ share cancel");
	}
}
