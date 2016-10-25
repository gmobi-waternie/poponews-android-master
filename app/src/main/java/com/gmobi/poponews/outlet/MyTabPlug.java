package com.gmobi.poponews.outlet;

import com.momock.holder.FragmentHolder;
import com.momock.holder.IComponentHolder;
import com.momock.holder.ImageHolder;
import com.momock.holder.TextHolder;
import com.momock.holder.ViewHolder;
import com.momock.outlet.Plug;
import com.momock.outlet.tab.ITabPlug;

public class MyTabPlug extends Plug implements ITabPlug {

	int imageId;
	int imageSelectedId;
	int stringId;
	String customText;
	String tag;
	FragmentHolder holder;
	
	boolean group;
	
	public MyTabPlug(int imageId, int imageSelectedId, FragmentHolder holder){
		this.imageId = imageId;
		this.imageSelectedId = imageSelectedId;
		this.holder = holder;
		
		group = false;
	}
	
	public MyTabPlug(int imageId, int imageSelectedId, int stringId, FragmentHolder holder,String tag){
		this(imageId, imageSelectedId, holder);
		this.stringId = stringId;
		this.customText="";
		this.tag = tag;
		group = false;
	}
	
	public MyTabPlug(int imageId, int imageSelectedId, String text, FragmentHolder holder,String tag){
		this(imageId, imageSelectedId, holder);
		this.stringId = 0;
		this.customText = text;
		this.tag = tag;
		group = false;
	}
	
	public MyTabPlug(int imageId, int imageSelectedId, int stringId, FragmentHolder holder, String tag, boolean group){
		this(imageId, imageSelectedId, stringId, holder,tag);
		this.group = group;
		
	}
	
	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public TextHolder getText() {
		//return null;
		if(customText.equals(""))
			return TextHolder.get(stringId); 
		else
			return TextHolder.get(customText);
	}

	@Override
	public ImageHolder getIcon() {
		return null;
	}

	@Override
	public IComponentHolder getContent() {
		return holder;
	}

	public int getImageId() {
		return imageId;
	}
	
	public boolean getGroup() {
		return group;
	}

	
	public String getTag() {
		return tag;
	}

	
	public int getImageSelectedId() {
		return imageSelectedId;
	}
	
	
	

}
