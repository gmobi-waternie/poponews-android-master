package com.gmobi.poponews.service;

import org.json.JSONArray;

import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.momock.data.IDataList;
import com.momock.service.IService;

public interface INewsCacheService extends IService {
	void setCategoryCache(JSONArray ja);
	IDataList<NewsCategory> getCategoryCache(String edition);
	IDataList<NewsItem> getNewsCache();
	String getConnectCache();

	void setNewsListCache(String cid,String data);
	String getNewsListCache(String cid);
	
	void setNewsContentCache(String nid,String data);
	String getNewsContentCache(String nid);

	boolean checkCategoryCache();
	
	


	
}
