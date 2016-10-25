package com.gmobi.poponews.service;

import java.util.Timer;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.OfflineAlarmHelper;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.data.IDataList;
import com.momock.service.IMessageService;
import com.momock.util.Logger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class OfflineSystemService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	IConfigService configService;
	IDataService dataService;
	IOfflineService offlineService;
	IMessageService messageService;
	Timer categoryReadyTimer;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.error("OfflineSystemService start :"+startId);


		if(UiHelper.isOfflineDownloadMode()  > UiHelper.NOT_OFFLINE_DOWNLOAD)
			return START_NOT_STICKY;

		configService = App.get().getService(IConfigService.class);
		dataService =  App.get().getService(IDataService.class);
		offlineService =  App.get().getService(IOfflineService.class);
		messageService =  App.get().getService(IMessageService.class);


		String ch = configService.getCurChannel();
		if(ch == null || ch.equals(""))
			return  START_NOT_STICKY;

		DBHelper dh = DBHelper.getInstance();
		IDataList<NewsCategory> ctgList =dh .getCtgFromDB(ch);
		if(ctgList == null || ctgList.getItemCount() == 0)
			return START_NOT_STICKY;

		for(int i =0; i<ctgList.getItemCount(); i++)
		{
			String cid = ctgList.getItem(i).getid();
			if(dh.getCtgOfflineSelect(cid))
				offlineService.addCategoryToOfflineList(cid);
		}
		//OfflineAlarmHelper.stopOfflineAlarm(App.get());

		Context ctx = App.get().getApplicationContext();
		if(!UiHelper.isNetworkConnected(ctx))
		{
			Toast.makeText(ctx, ctx.getString(R.string.offline_download_no_network), Toast.LENGTH_SHORT).show();
			return START_NOT_STICKY;
		}

		messageService.send(this, MessageTopics.UPDATE_OFFLINEALARM_START);
		offlineService.startDownloadCategory(UiHelper.OFFLINE_ALARM_DOWNLOADING);

		return START_NOT_STICKY;
	}


	
	
	
	

}
