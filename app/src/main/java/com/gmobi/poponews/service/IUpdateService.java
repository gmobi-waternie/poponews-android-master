package com.gmobi.poponews.service;

import com.gmobi.poponews.event.UpgradeEventsArgs;
import com.momock.event.Event;
import com.momock.service.IService;

public interface IUpdateService extends IService {
	void updateStart();
	void updateCancel();
	Event<UpgradeEventsArgs> getNotificationEvent();

}
