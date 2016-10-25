package com.gmobi.poponews.event;

import com.momock.event.EventArgs;

public class UpgradeEventsArgs extends EventArgs {
	private int percent;
	private int downloadLength;
	private int contentLength;
	private int status;
	
	
	public UpgradeEventsArgs(int status, int percent,int dl,int cl)
	{
		this.percent = percent;
		this.downloadLength = dl;
		this.contentLength = cl;
		this.status = status;
	}

	public int getPercent() {
		return percent;
	}

	public int getDownloadLength() {
		return downloadLength;
	}

	public int getContentLength() {
		return contentLength;
	}
	
	
	public int getStatus() {
		return status;
	}
	
}
