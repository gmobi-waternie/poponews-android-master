package com.gmobi.poponews.share;

import android.content.Intent;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.momock.app.App;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.service.UITaskService;

import java.util.HashMap;
import java.util.Map;

public class MoreShare implements IShare {
	private boolean avail;
	private final static String NAME = "more";


	private String name;
	private String title;




	@Override
	public int getControlDrawable() {
		return R.drawable.share_more;
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
	public void share(final String title, final String webUrl, final String imageUri) {
//		Map<String,String> msgMap = new HashMap<>();
//		msgMap.put("title",title);
//		msgMap.put("weburl",webUrl);
//		msgMap.put("imageuri",imageUri);
//
//
//		App.get().getService(IMessageService.class).send(this, MessageTopics.SHARE_MORE,msgMap
//				);


		App.get().getService(IUITaskService.class).run(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, App.get().getResources().getText(R.string.share_subject));
				intent.putExtra(Intent.EXTRA_TEXT, App.get().getResources().getText(R.string.share_subject) + ":" + title + "\n" + webUrl); // 分享的内容
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				Intent newIntent = Intent.createChooser(intent, App.get().getResources().getText(R.string.share_title));
				newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				App.get().startActivity(newIntent);
			}
		});

	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}




}
