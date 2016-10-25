package com.gmobi.poponews.model;

import com.momock.data.DataList;
import com.momock.data.DataMap;
import com.momock.data.IDataList;

public class NewsListItem  extends DataMap<String, Object>{
	public static final String Preview1 = "preview1";
	public static final String Preview2 = "preview2";
	public static final String Preview3 = "preview3";
	
	public static final String Title1 = "title1";
	public static final String Title2 = "title2";
	public static final String Title3 = "title3";
	
	public static final String Type = "type";
	public static final String ListType = "listtype";
	public static final String Cid = "cid";
	
	private DataList<NewsItem> newsList = new DataList<NewsItem>();
	
	
	public static final int LIST_TYPE_1L = 1;
	public static final int LIST_TYPE_1R = 2;
	public static final int LIST_TYPE_2 = 3;
	public static final int LIST_TYPE_3 = 4;
	
	
	
	public int getListType() {
		return (Integer)this.getProperty(ListType);
	}
	
	public void setListType(int lType) {
		this.setProperty(ListType, lType);
	}	
	
	public String getCid() {
		return (String) this.getProperty(Cid);
	}	
	
	
	public void setCid(String cid2) {
		this.setProperty(Cid, cid2);
	}	
	
	
	
	public void setPreview1(String preview) {
		this.setProperty(Preview1, preview);
	}	
	public void setPreview2(String preview) {
		this.setProperty(Preview2, preview);
	}
	public void setPreview3(String preview) {
		this.setProperty(Preview3, preview);
	}
	public void setTitle1(String title) {
		this.setProperty(Title1, title);
	}
	public void setTitle2(String title) {
		this.setProperty(Title2, title);
	}
	public void setTitle3(String title) {
		this.setProperty(Title3, title);
	}
	
	public String getPreview1() {
		return (String)this.getProperty(Preview1);
	}	
	public String getPreview2() {
		return (String)this.getProperty(Preview2);
	}	
	public String getPreview3() {
		return (String)this.getProperty(Preview3);
	}	
	public String getTitle1() {
		return (String)this.getProperty(Title1);
	}
	public String getTitle2() {
		return (String)this.getProperty(Title2);
	}	
	public String getTitle3() {
		return (String)this.getProperty(Title3);
	}	
	
	
	public void setPreviewAndTitle()
	{
		if(newsList.getItemCount() == 0)
			return;
		
		if(newsList.getItemCount() >= 1)
		{
			setPreview1(newsList.getItem(0).getGridPreview());
			setTitle1(newsList.getItem(0).getTitle());
		}
			
		if(newsList.getItemCount() >= 2)
		{
			setPreview2(newsList.getItem(1).getGridPreview());
			setTitle2(newsList.getItem(1).getTitle());
		}
		
		if(newsList.getItemCount() >= 3)
		{
			setPreview3(newsList.getItem(2).getGridPreview());
			setTitle3(newsList.getItem(2).getTitle());
		}
		
			
	}
	
	
	public void addNewsItem(NewsItem n)
	{
		if(newsList == null)
			newsList = new DataList<NewsItem>();
		newsList.addItem(n);
	}
	
	
	public NewsItem getItem(int idx)
	{
		return newsList.getItem(idx);
	}
	
}
