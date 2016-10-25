package com.gmobi.poponews.util;

import android.content.Context;

import com.gmobi.poponews.model.NewsItem;
import com.reach.ICallback;

import java.util.ArrayList;

/**
 * Created by Administrator on 3/2 0002.
 */
public interface IAdPool {


	NewsItem getNextAd(String cid);
	int getPoolSize();

	void initPool(Context ctx, int count);
	void addAdIntoPool(Object AdItem);
	int getPoolStatus();
	void setPoolStatus(int s);
	boolean isPoolEmpty();


}
