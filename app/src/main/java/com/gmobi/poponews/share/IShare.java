package com.gmobi.poponews.share;

public interface IShare {
	boolean isAvailable();
	void setAvailable(boolean avail);
	void share(String title,  String webUrl, String imageUri);
	String getControlName();
	int getControlDrawable();
	String getTitle();
	String getName();

	void setTitle(String title);

}

