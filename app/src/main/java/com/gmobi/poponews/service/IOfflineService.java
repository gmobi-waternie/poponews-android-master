package com.gmobi.poponews.service;

import org.json.JSONObject;

import com.gmobi.poponews.event.OfflineEventArgs;
import com.momock.event.Event;
import com.momock.event.EventArgs;
import com.momock.service.IService;

public interface IOfflineService extends IService{
	void startDownloadCategory(int mode);
	void stopDownloadCategory(boolean show);
	
	Event<OfflineEventArgs> getNotificationEvent();
	
	void addCategoryToOfflineList(String cid);
	void removeCategoryFromOfflineList(String cid);
	void clearOfflineList();
	
	
}
