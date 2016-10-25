package com.gmobi.poponews.util;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.cases.article.ArticleActivity;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper {
	private Context mContext;
	private Resources mRes;
	public NotificationHelper(Context ctx)
	{
		
		mContext = ctx;
		mRes = mContext.getResources();
	}
	
	private int getCurId()
	{
		IConfigService cs = App.get().getService(IConfigService.class);
		
		int id = cs.getNewsFeedCurId();
		if(id == Integer.MAX_VALUE)
			id=0;
		else
			id ++;

		cs.setNewsFeedCurId(id);
		return id;
	}

	int notificationId=0;

	public void sendNotification(String id, String message) {
		Intent in = new Intent(App.get(), ArticleActivity.class);
		in.putExtra("nid", id);
		in.putExtra("from", UiHelper.FROM_PUSH);
		in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


		PendingIntent pendingIntent = PendingIntent.getActivity(App.get(), 0 , in,
				0);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.ic_launcher)
						//.setContentTitle(getString(R.string.app_title))
				.setContentTitle(message)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


		notificationManager.notify(getCurId(), notificationBuilder.build());



	}
}
