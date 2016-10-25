/*******************************************************************************
 * Copyright 2012 momock.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gmobi.poponews.model;

import android.R.integer;


import com.gmobi.poponews.R;
import com.gmobi.poponews.service.IDataService;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.DataMap;
import com.momock.data.IDataList;
import com.momock.util.Convert;

public class NewsCategory extends DataMap<String, Object> {

	public static final String Id = "id";
	public static final String Name = "name";
	public static final String Visible = "visible";
	public static final String Refreshing = "refreshing";
	public static final String LastIdx = "lastIdx";
	public static final String LastLayoutIdx = "lastLayoutIdx";

	public static final String LayoutType = "layout";
	public static final String CategoryType = "type";
	public static final String Extra = "extra";
	public static final String Source = "source";
	public static final String Ad = "ad";
	public static final String AdInsert = "adinsert";
	public static final String Order = "order";
	public static final String AdSource = "adSrc";

	public static final String IsCache = "isCache";

	public static final String Offline = "offline";
	public static final String OfflineProgress = "offlineProgress";
	public static final String HotFlag = "HotFlag";




	
	public static final int INVISIBLE = 0;
	public static final int VISIBLE = 1;
	
	public static final int NOT_REFRESHING = 0;
	public static final int IS_REFRESHING = 1;
	public static final int IS_LOADINGMORE= 2;


	public static final String[] LAYOUT_TYPE_STR = {"list", "grid", "pin", "pin2"};
	public static final int LAYOUT_LIST = 1;
	public static final int LAYOUT_GRID = 2;
	public static final int LAYOUT_PIN = 3;
	public static final int LAYOUT_PIN2 = 4;
	public static final int LAYOUT_PIN3 = 5;


	public static final String TYPE_NEWS = "news";
	public static final String TYPE_FACEBOOK = "facebook";
	public static final String TYPE_TWITTER = "twitter";
	public static final String TYPE_VK = "vk";
	public static final String TYPE_SOCIAL = "social";


	public String getid() {
		return (String) this.getProperty(Id);
	}
	
	public void setid(String id) {
		this.setProperty(Id, id);
	}

	public String getname() {
		return (String) this.getProperty(Name);
	}

	public void setname(String name) {
		this.setProperty(Name, name);
	}
	
	public String getIconUri(){
		String uri = "https://www.google.com/images/srpr/logo3w.png";
		return uri;
	}
	
	public int isVisible() {
		return (Integer) this.getProperty(Visible);
	}
	
	public void setVisible(int v) {
		this.setProperty(Visible, v);
	}

	public int isRefresh() {
		return (Integer) this.getProperty(Refreshing);
	}
	
	public void setRefresh(int r) {
		this.setProperty(Refreshing, r);
	}
	
	public int getLastIdx() {
		return (Integer) this.getProperty(LastIdx);
	}
	
	public void setLastIdx(int i) {
		this.setProperty(LastIdx, i);
	}
	
	
	public int getLastLayoutIdx() {
		if(hasProperty(LastLayoutIdx))
			return (Integer) this.getProperty(LastLayoutIdx);
		return -1;
	}
	
	public void setLastLayoutIdx(int i) {
		this.setProperty(LastLayoutIdx, i);
	}


	public int getOrder() {
		return Convert.toInteger(this.getProperty(Order));
	}

	public void setOrder(int o) {
		this.setProperty(Order, o);
	}


	public int getAdSrc() {
		if(hasProperty(AdSource))
			return Convert.toInteger(this.getProperty(AdSource));
		return 0;
	}

	public void setAdSrc(int s) {
		this.setProperty(AdSource, s);
	}



	public void setLayoutType(String type) {
		this.setProperty(LayoutType, type);
	}

	public int getNewsLayoutType()
	{
		String t=(String) this.getProperty(LayoutType);

//		if(t.equals("grid"))
//			return LAYOUT_GRID;
//		else if (t.equals("pin"))
//			return LAYOUT_PIN;
//		else if (t.equals("pin2"))
//			return LAYOUT_PIN2;
//		else if (t.equals("pin3"))
//			return LAYOUT_PIN3;
//		else
			return LAYOUT_LIST;
	}


	public void setCategoryType(String type)
	{
		this.setProperty(CategoryType, type);
	}
	public String getCategoryType()
	{
		return (String) this.getProperty(CategoryType);
	}



	public void setExtra(Object e)
	{
		this.setProperty(Extra, e);
	}

	public Object getExtra()
	{
		return this.getProperty(Extra);
	}

	public void setAdInsert(boolean i)
	{
		this.setProperty(AdInsert, i);
	}

	public boolean getAdInsert()
	{
		if(this.hasProperty(AdInsert))
			return Convert.toBoolean(this.getProperty(AdInsert));
		else
			return false;
	}

	public void setCacheFlag(boolean i)
	{
		this.setProperty(IsCache, i);
	}

	public boolean isCache()
	{
		return Convert.toBoolean(this.getProperty(IsCache));
	}


	public void setHotFlag(boolean h)
	{
		this.setProperty(HotFlag, h);
	}

	public boolean isHot()
	{
		if(!hasProperty(HotFlag))
			return false;
		return Convert.toBoolean(this.getProperty(HotFlag));
	}




	public void setOfflineFlag(boolean o)
	{
		this.setProperty(Offline, o);
	}

	public boolean isOfflineFlag()
	{
		if(this.hasProperty(Offline))
			return Convert.toBoolean(this.getProperty(Offline));
		else
			return false;
	}

	public int getOfflineProgress() {
		if(hasProperty(OfflineProgress))
			return Convert.toInteger(this.getProperty(OfflineProgress));
		return 0;
	}

	public void setOfflineProgress(int p) {
		this.setProperty(OfflineProgress, p);
	}


}
