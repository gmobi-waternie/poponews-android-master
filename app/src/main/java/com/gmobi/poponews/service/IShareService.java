package com.gmobi.poponews.service;

import java.util.ArrayList;

import com.gmobi.poponews.share.IShare;
import com.momock.service.IService;

public interface IShareService extends IService {
	void setup();

	ArrayList<IShare> getCurShares();

	IShare getShareByName(String name);

	void share(String name, String title, String webUrl, String imageUri);

	void addShare(String name,String title);

	void clearShares();
}
