package com.gmobi.poponews.service;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.gmobi.poponews.activity.SplashActivity;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.event.UpgradeEventsArgs;
import com.momock.app.App;
import com.momock.event.Event;
import com.momock.event.IEventHandler;
import com.momock.service.ICacheService;
import com.momock.service.IHttpService;
import com.momock.service.IMessageService;
import com.momock.util.InstallHelper;

import java.io.File;



public class UpdateService implements IUpdateService {


	Event<UpgradeEventsArgs> event = null;
	
	public static final int UPDATE_DOWNLOADING = 1;
	public static final int UPDATE_DOWNLOADED = 2;
	

	
	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{IHttpService.class,IMessageService.class,ICacheService.class,IConfigService.class};
	}
	@Override
	public Event<UpgradeEventsArgs> getNotificationEvent()
	{
		return event;
	}

	@Override
	public void start() {
		if(event == null)
		{
			event = new Event<>();
			event.addEventHandler(new IEventHandler<UpgradeEventsArgs>() {
				
				@Override
				public void process(Object sender, UpgradeEventsArgs args) {
					if(args.getStatus() == UPDATE_DOWNLOADING)
					{
						Message msg = new Message();
						msg.what = MessageTopics.SYSTEM_UPDATE_PROCESS;
						Bundle b = new Bundle();
						b.putInt("per",args.getPercent());
						b.putLong("dl", args.getDownloadLength());
						b.putLong("cl", args.getContentLength());
						
						msg.setData(b);
						SplashActivity.splashHandler.sendMessage(msg);
					}
					else if(args.getStatus() == UPDATE_DOWNLOADED)
					{
						int dl = args.getDownloadLength();
						int cl = args.getContentLength();
						int per = args.getPercent();
						Log.e("Poponews","upgrade dl = "+dl+",cl="+cl+",per="+per);
						if(dl!=cl || per != 100)	
						{
							return;
						}
						ICacheService cacheService = App.get().getService(ICacheService.class);
						String uri = App.get().getService(IConfigService.class).getUpdateFile();
						File f = cacheService.getCacheOf(CacheNames.UPDATE_CACHEDIR, uri);
						InstallHelper.install(App.get(), f);
					}
				}
			});
		}
	}

	@Override
	public void stop() {
		updateCancel();

	}

	@Override
	public boolean canStop() {

		return false;
	}

	@Override
	public void updateStart() {
		IRemoteService rs = App.get().getService(IRemoteService.class);
		ICacheService cacheService = App.get().getService(ICacheService.class);
		String uri = App.get().getService(IConfigService.class).getUpdateFile();	
		File f = cacheService.getCacheOf(CacheNames.UPDATE_CACHEDIR, uri);
		if(f.exists())
			f.delete();
		rs.startDownloadUpdateFile(uri);
	}
	

	@Override
	public void updateCancel() {
		App.get().getService(IRemoteService.class).stopDownloadUpdateFile();
	}





}
