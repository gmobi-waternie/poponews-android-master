package com.gmobi.poponews.service;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.event.OfflineEventArgs;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.OfflineAlarmHelper;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.event.Event;
import com.momock.event.IEventHandler;
import com.momock.service.ICacheService;
import com.momock.service.IHttpService;
import com.momock.service.IMessageService;
//import com.momock.service.ReliableDownloadManager;
//import com.momock.service.IReliableDownloadManager.IDownloadItem;
//import com.momock.util.FileHelper;
//import com.momock.util.JsonDatabase;
import com.momock.util.JsonDatabase.Collection;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

/*TODO:错误处理逻辑待加*/

public class OfflineService implements IOfflineService {
	@Inject
	IConfigService configService;
	
	@Inject
	IHttpService httpService;
	
	@Inject
	ICacheService cacheService;
	
	@Inject
	IMessageService messageService;
	@Inject
	IRemoteService remoteService;
	@Inject
	IDataService newsdataService;
	
	public static final int STATUS_ERROR = -1; 
	public static final int STATUS_LIST = 1;  //列表下载完毕
	public static final int STATUS_CONTENT_FINISHED = 2;     //某个列表下的内容下载完成
	public static final int STATUS_ITEM_FINISHED = 3;      //某个Item下载完成
	public static final int STATUS_IMG_FINISHED = 4;      //某个Item的Image内容下载完成
	public static final int STATUS_ARTICLE_FINISHED = 5;  //某个item的article内容下载完成
	public static final int STATUS_ALL_FINISHED = 6;  
	
	
	private boolean isDownloadPic;
	private int hour;
	private int minute;
	ArrayList<String> offlineCategories = null;
	
	//ReliableDownloadManager offlineRdm;
	DataList<String> downloadList = null; //一个item需要下载的url,下载完成一个就删除一个url，当没有url则表明为下载完成 
	Collection offlineCol;
	Event<OfflineEventArgs> event = null;
	String curDownloadCategory;
	DataList<NewsItem> offlineItemsList; //需要下载的itemList
	private boolean restartFlag = false;
	
	@Override
	public Event<OfflineEventArgs> getNotificationEvent(){
		return event;
	}
	
	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{IRemoteService.class, IHttpService.class,IMessageService.class,ICacheService.class,IConfigService.class};
	}

	@Override
	public void start() {
		isDownloadPic = configService.getOfflinePicFlag();
		
		hour =  configService.getOfflineTimeHour();
		minute  =  configService.getOfflineTimeMinute();
		
		if(offlineCategories == null)
			offlineCategories = new ArrayList<String>();
		
		if(event ==null)
		{
			event = new Event<OfflineEventArgs>();
			event.addEventHandler(new IEventHandler<OfflineEventArgs>() {
				
				@Override
				public void process(Object sender, OfflineEventArgs args) {
					if(UiHelper.isOfflineDownloadMode() == UiHelper.NOT_OFFLINE_DOWNLOAD)
						return;
					
					if(args.getStatus() == STATUS_LIST)
					{
						offlineItemsList =(DataList<NewsItem>) newsdataService.getOfflineNewsList();
						startRestartTimer();
						downloadItem(offlineItemsList.getItem(0));	
					}
					else if(args.getStatus() == STATUS_ITEM_FINISHED)
					{
						if(offlineItemsList!=null && offlineItemsList.getItemCount() > 0)
							offlineItemsList.removeItemAt(0);

						
						if(offlineItemsList.getItemCount() == 0)
							event.fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_CONTENT_FINISHED, null,null));
						else
						{
							notifyUiThread(curDownloadCategory, 100 - offlineItemsList.getItemCount(),true);
							startRestartTimer();
							downloadItem(offlineItemsList.getItem(0));
						}

					}
					else if(args.getStatus() == STATUS_IMG_FINISHED || args.getStatus() == STATUS_ARTICLE_FINISHED)
					{
						downloadList.removeItem(args.getUrl());
						stopRestartTimer();
						Logger.error("downloadList left"+downloadList.getItemCount());
						if(downloadList.getItemCount() == 1 && downloadList.getItem(0).equals(""))
						{
							downloadList.removeItem("");
							event.fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_ITEM_FINISHED, null,null));
						}
						else if(downloadList.getItemCount() == 0)
							event.fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_ITEM_FINISHED, null,null));
						else {
							startRestartTimer();
							Logger.error("downloadList download"+downloadList.getItem(0));
							remoteService.startDownloadOfflineImage(downloadList.getItem(0));



						}


					}
									
					else if(args.getStatus() == STATUS_CONTENT_FINISHED)
					{
						if(UiHelper.isOfflineDownloadMode() == UiHelper.OFFLINE_ALARM_DOWNLOADING || UiHelper.isOfflineDownloadMode() == UiHelper.OFFLINE_DOWNLOADING)
							newsdataService.mergeDataOfflineToOnline(offlineCategories.get(0));
						offlineCategories.remove(0);
						notifyUiThread(curDownloadCategory, 100,true);
						startDownloadCategory(UiHelper.isOfflineDownloadMode());
					}
					
					else if(args.getStatus() == STATUS_ALL_FINISHED)
					{
						stopDownloadCategoryInt();
						notifyUiThread(null,0,true);
					}
					
					
				}
			});
		}
		
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean canStop() {
		return false;
	}

	@Override
	public void startDownloadCategory(int mode) {
		configService.setOfflineDownloadTime(System.currentTimeMillis());

		if(offlineCategories.size() > 0)
		{
			UiHelper.setOfflineDownloadMode(mode);
			curDownloadCategory = offlineCategories.get(0);
			remoteService.getOfflineList(offlineCategories.get(0));
		}
		else
		{
			event.fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_ALL_FINISHED, null,null));
			restartFlag = false;
			stopRestartTimer();
		}
	}

	

	public void stopDownloadCategoryInt() {
		remoteService.stopDownloadOffline();
		restartFlag = false;
		stopRestartTimer();
		if(offlineItemsList != null)
			offlineItemsList.removeAllItems();
		
		if(downloadList != null)
			downloadList.removeAllItems();
		
		curDownloadCategory = null;
		clearOfflineList();
		UiHelper.setOfflineDownloadMode(UiHelper.NOT_OFFLINE_DOWNLOAD);

	}

	@Override
	public void stopDownloadCategory(boolean show) {

		stopDownloadCategoryInt();
		
		notifyUiThread(null, 0, show);
		
	}
	
	

	public void notifyUiThread(String cid,int per,boolean show) {
		IMessageService ms = App.get().getService(IMessageService.class);
		if(cid == null && per == 0)
		{
			ms.send(this, MessageTopics.UPDATE_OFFLINE_FINISH,show);
		}
		else{
			
			Bundle b = new Bundle();
			b.putInt("per",per);
			b.putString("cid",cid);
			
			ms.send(this, MessageTopics.UPDATE_OFFLINE_PROGRESS, b);
		}
	}
	
	@Override
	public void addCategoryToOfflineList(String cid)
	{
		if(offlineCategories != null)
			offlineCategories.add(cid);
	}
	
	@Override
	public void removeCategoryFromOfflineList(String cid)
	{
		if(offlineCategories != null)
			offlineCategories.remove(cid);
	}
	
	@Override
	public void clearOfflineList()
	{
		if(offlineCategories != null)
			offlineCategories.clear();
	}


	private void safeAddItem(DataList<String> list, String item)
	{
		if(item!= null && !item.equals("") && !list.hasItem(item))
			list.addItem(item);
	}


	private void downloadItem(NewsItem n)
	{
		int screenWidth = SystemHelper.getScreenWidth(App.get());

		isDownloadPic = configService.getOfflinePicFlag();
		if(downloadList == null)
			downloadList = new DataList<String>();
		else
			downloadList.removeAllItems();
		
		/*article总是第一个下载*/

		if(!n.getGo2Src())
			downloadList.addItem(configService.getBaseUrl()+n.getBody());


		if(isDownloadPic)
		{

			safeAddItem(downloadList, n.getListPreview());
//			safeAddItem(downloadList, n.getPin2Preview());

			safeAddItem(downloadList, n.getPinPreview());

//			safeAddItem(downloadList, n.getGrid1Preview());
//			safeAddItem(downloadList, n.getGrid2Preview());
//			safeAddItem(downloadList, n.getGrid3Preview());



			for(int i=0; i <n.getImgs().getItemCount();i++)
			{
				String imgurl = configService.getBaseImageUrl() + n.getImgs().getItem(i).getFile();
				safeAddItem(downloadList, imgurl);

				if(n.getType().equals(NewsItem.NEWS_TYPE_IMAGE))
				{
					String scaleImgUrl= configService.getBaseImageUrl() + n.getImgs().getItem(i).getFile()+"."+screenWidth+"x"+"t5";
					safeAddItem(downloadList, scaleImgUrl);
				}
			}


				safeAddItem(downloadList, n.getPreview1());
				safeAddItem(downloadList, n.getPreview2());
				safeAddItem(downloadList, n.getPreview3());

		}
		
		if((downloadList.getItemCount() == 1 && downloadList.getItem(0).equals("")) || downloadList.getItemCount() == 0)
		{
			OfflineEventArgs oea = new OfflineEventArgs(OfflineService.STATUS_ITEM_FINISHED, null, "");
			event.fireEvent(null, oea);
		}
		else {

			if (!n.getGo2Src())
				remoteService.startDownloadOfflineArticle(downloadList.getItem(0));
			else
				remoteService.startDownloadOfflineImage(downloadList.getItem(0));
//		else
//		{
//			OfflineEventArgs oea = new OfflineEventArgs(OfflineService.STATUS_ARTICLE_FINISHED, null, "");
//			event.fireEvent(null, oea);
//		}


//			if (isDownloadPic) {
//				for (int i = 1; i < downloadList.getItemCount(); i++)
//					remoteService.startDownloadOfflineImage(downloadList.getItem(i));
//			}
		}


	}


	private Timer restartTimer = null;
	private TimerTask mRestartTask = null;

	public void startRestartTimer()
	{

		if(restartTimer == null)
		{
			restartTimer = new Timer();
		}

		if (mRestartTask != null){
			mRestartTask.cancel();
		}


		mRestartTask = new TimerTask() {
			@Override
			public void run() {

					event.fireEvent(null, new OfflineEventArgs(OfflineService.STATUS_ITEM_FINISHED, null,null));


			}
		};

		restartTimer.schedule(mRestartTask, 120000);
	}

	public void stopRestartTimer()
	{
		if (mRestartTask != null){
			mRestartTask.cancel();
			mRestartTask = null;
		}

	}

 
}
