package com.gmobi.poponews.event;

import com.momock.event.EventArgs;

public class OfflineEventArgs extends EventArgs {
	private int status;
	private String cid;
	private String url;
	
	public OfflineEventArgs(int status,String cid,String url)
	{
		this.status = status;
		this.cid = cid;
		this.url = url;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public String getCid()
	{
		return cid;
	}
}
