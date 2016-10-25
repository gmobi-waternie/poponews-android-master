package com.gmobi.poponews.model;


import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.util.DipHelper;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.DataMap;
import com.momock.util.Convert;
import com.momock.util.SystemHelper;


public class MeItem extends DataMap<String, Object>{
	public static final String ME_TYPE_NORMAL = "normal";
	public static final String ME_TYPE_URL = "url";
	public static final String ME_TYPE_SETTING = "setting";


	public static final String IconId = "iconid";
	public static final String Text = "text";
	public static final String Action = "action";
	public static final String Type = "type";

	public String getAction() {
		return   (String)this.getProperty(Action);
	}

	public void setAction(String action) {
		this.setProperty(Action,action);
	}

	public String getType() {
		return   (String)this.getProperty(Type);
	}

	public void setType(String type) {
		this.setProperty(Type, type);
	}

	public int getIconId() {
		return Convert.toInteger(this.getProperty(IconId));
	}
	
	public void setIconId(int id) {
		this.setProperty(IconId, id);
	}


	public String getText() {
		return   (String)this.getProperty(Text);
	}

	public void setText(String text) {
		this.setProperty(Text, text);
	}

	
	
}
