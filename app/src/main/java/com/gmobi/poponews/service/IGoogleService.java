package com.gmobi.poponews.service;

import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialPost;
import com.momock.data.IDataList;

/**
 * Created by Administrator on 6/19 0019.
 */
public interface IGoogleService extends  ISocialService{
	void remoteGetAccountList(int fromActivity);
	void remoteGetAccountPosts(SocialAccount sa, String nextUrl);
	void setCategory(NewsCategory ctg);

}
