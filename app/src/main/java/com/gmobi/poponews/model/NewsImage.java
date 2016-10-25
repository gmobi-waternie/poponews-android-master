package com.gmobi.poponews.model;

import com.momock.data.DataMap;
import com.momock.util.Convert;

public class NewsImage extends DataMap<String, Object>{
	//{"t":1,"f":"abac320d599a8e3cf970ccb2cc566c3f","w":1200,"h":800,"desc":"壮美的冰川。"},
	public static final String Type = "t";
	public static final String File = "f";
	public static final String Width = "w";
	public static final String Height = "h";
	public static final String Desc = "desc";
	
	
	public int getType() {
		return  (Integer)this.getProperty(Type);
	}
	
	public void setType(int type) {
		this.setProperty(Type, type);
	}
	
	
	public int getWidth() {
		return  Convert.toInteger(this.getProperty(Width));
	}
	
	public void setWidth(int w) {
		this.setProperty(Width, w);
	}
	
	public int getHeight() {
		return  Convert.toInteger(this.getProperty(Height));
	}
	
	public void setHeight(int h) {
		this.setProperty(Height, h);
	}
	
	public String getFile() {
		return  (String) this.getProperty(File);
	}
	
	public void setFile(String file) {
		this.setProperty(File, file);
	}
	
	public String getDesc() {
		return  (String) this.getProperty(Desc);
	}
	
	public void setDesc(String desc) {
		this.setProperty(Desc, desc);
	}

}
