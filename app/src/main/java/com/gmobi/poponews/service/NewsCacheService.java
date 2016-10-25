package com.gmobi.poponews.service;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.util.DBHelper;
import com.momock.data.IDataList;
import com.momock.service.ICacheService;
import com.momock.service.IUITaskService;
import com.momock.util.FileHelper;

public class NewsCacheService implements INewsCacheService {
	@Inject
	ICacheService cs;
	
	@Inject
	IDataService ds;
	
	@Inject
	IConfigService configService;
	
	DBHelper dh;
	
	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{ ICacheService.class,IDataService.class };
	}

	@Override
	public void start() {
		dh = DBHelper.getInstance();

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canStop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCategoryCache(JSONArray ja) {
		dh.setCtg(configService.getCurChannel(),ja);
	}

	@Override
	public void setNewsListCache(String cid, String data) {
		File f = cs.getCacheOf(CacheNames.NEWS_CATEGORY_CACHEDIR, cid);
		
		try {
			FileHelper.writeText(f, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getNewsListCache(String cid) {
		File f = cs.getCacheOf(CacheNames.NEWS_CATEGORY_CACHEDIR, cid);
		
		String data = null;
		try {
			data = FileHelper.readText(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void setNewsContentCache(String nid, String data) {
		File f = cs.getCacheOf(CacheNames.NEWS_CONTENT_CACHEDIR, nid+".html");
		
		try {
			FileHelper.writeText(f, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getNewsContentCache(String nid) {
		File f = cs.getCacheOf(CacheNames.NEWS_CONTENT_CACHEDIR, nid);
		
		String data = null;
		try {
			data = FileHelper.readText(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	@Override
	public boolean checkCategoryCache()
	{
		JSONArray CtgDB = dh.getCtg(configService.getCurChannel());
		
		if(CtgDB == null || CtgDB.length() == 0)
			return false;
		
		try {
			IDataList<NewsCategory> CtgNew = ds.getAllCategories();
			boolean found = false;
			for(int i=0; i<CtgNew.getItemCount(); i++)
			{
				NewsCategory nc = CtgNew.getItem(i);
				found = false;
				for(int j=0; j<CtgDB.length(); j++)
				{
					JSONObject joDB = (JSONObject) CtgDB.get(j);
					if(joDB.get("id").equals(nc.getid()))
					{
						found =true;
						break;
					}
				}
				if(!found)
					return false;
			}
		
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IDataList<NewsCategory> getCategoryCache(String edition) {
		return dh.getCtgFromDB(edition);
	}

	@Override
	public String getConnectCache() {
		JSONObject jo = dh.getConnectData();
		return (jo == null) ? null : jo.toString();

	}

	
	@Override
	public IDataList<NewsItem> getNewsCache() {
		return dh.getListFromDB(DBHelper.TAG_CACHE);
	}
	
}
