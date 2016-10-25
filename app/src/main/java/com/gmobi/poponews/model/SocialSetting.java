package com.gmobi.poponews.model;

import com.momock.app.App;

/**
 * Created by Administrator on 8/3 0004.
 * 用于存放社交的用户配置
 * CategoryName = type + ctg ,如“Google财经 ，Facebook12vj3io68fuyd”
 * 默认为关
 */
public class SocialSetting {
	public static boolean getStatus(String type)
	{
		return App.get().getSettings().getBooleanProperty(type,false);
	}

	public static void setStatus(String type,boolean open)
	{
		App.get().getSettings().setProperty(type, open);
	}

	public static void setCategorySelect(String type , String ctg,boolean select)
	{
		App.get().getSettings().setProperty(type + ctg, select);
	}

	public static boolean getCategorySelect(String type, String ctg)
	{
		return App.get().getSettings().getBooleanProperty(type + ctg,false);
	}




}
