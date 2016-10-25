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
package com.gmobi.poponews.service;

import com.gmobi.poponews.model.Comment;
import com.gmobi.poponews.model.CommentChannelEntity;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.NewsListItem;
import com.gmobi.poponews.model.CommentReplyEntity;
import com.momock.data.IDataList;
import com.momock.data.IDataMutableList;
import com.momock.service.IService;

public interface IDataService extends IService{

    interface IVoteNum{
        int onGetVote(String voteName, int num);
    }
	
	NewsItem getNewsById(String nid);
	void addIntoNewsList(NewsItem n);

	NewsCategory getCategoryById(String id);

	IDataList<NewsCategory> getAllVisibleCategories();
	IDataList<NewsCategory> getAllOfflineCategories();
	IDataList<NewsCategory> getAllCategories();

	IDataMutableList<NewsItem> getAllNews();

	void setNewsList(String cid, String data,boolean fromCache,boolean addAd);//addAd 本次是否加载新闻
	void setOfflineNewsList(String cid, String data);
	IDataList<NewsItem> getOfflineNewsList();

	void setCategory(String data,boolean update,boolean errorSend);
	
	
	boolean isListReady(String cid);
	//boolean isAllListReady();
	boolean isCtgReady();

	IDataList<NewsItem> getNewsInCategory(String cid,boolean cache);
	IDataList<NewsListItem> getNewsListInCategory(String cid);

	String getCurNid();

	void setCurNid(String curNid);

	void setCurNewsData(String data);

	

	int getCategoryIndex(String cid);

	long getEarlyReleaseTime(String cid,boolean cache);
	long getLatestReleaseTime(String cid,boolean cache);

	void refreshVisibleCategory();

	IDataList<NewsItem> initFavList();

	IDataList<NewsItem> getFavList();

	void removeFromFavList(NewsItem n);
	
	void addIntoFavList(NewsItem n);

	void refreshItems(String cid);

	void saveCategories();
	void restoreCategories();

    int getVoteNum(String nId, IVoteNum vn);
    void voteArticle(String nId, String vote);
	IDataList<NewsItem> initReadList();
	void addIntoReadList(NewsItem n);
	IDataList<NewsItem> getReadList();
	void removeFromReadList(NewsItem n);

	
	
	
	/*
	void addCategoryToOfflineList(OfflineCategoryItem item);
	void removeCategoryFromOfflineList(OfflineCategoryItem item);
	void clearOfflineCategoryList();
	ArrayList<OfflineCategoryItem> getOfflineCategoryList();
	OfflineCategoryItem getOfflineCategoryItem(int idx);
*/
	void mergeDataOfflineToOnline(String cid);

	boolean getCategoryFromCache();
	void clearAllCacheContent(String cid,boolean all);

	NewsItem getPushItem(String data);

	IDataList<NewsItem> getPushList();
	IDataList<NewsItem> initPushList();
	void removeFromPushList(NewsItem n);
	void addIntoPushList(NewsItem n);

	/** 与我相关的评论集合 **/
	void addIntoCommentList(NewsItem n);
	IDataList<NewsItem> getCommentList();

	void refreshFavList();
	void refreshReadList();
	void refreshPushList();

	void initCacheItemsList();



//	IDataList<CommentEntity> getAllNewsCommentList();
//	IDataList<CommentEntity> getAllHotCommentList();
//	IDataList<CommentEntity> getMoreHotCommentList();
//
//	IDataList<CommentEntity> getNewsCommentById(String nid);
//	IDataList<CommentEntity> getHotCommentById(String nid);
//	IDataList<CommentEntity> getEarlyHotComment(String nid);
//
//	IDataList<CommentReplyEntity> getAllReplyList(String nid, int position, int type);
//	void addItemEarlyHotComment(CommentEntity entity);
//	void addItemHotComment(CommentEntity entity);
//	void addItemNewsComment(String nid, CommentEntity entity);
//	void addItemComment(String nid, CommentEntity entity);
//	int getCommentCount(Comment comment);
//
//	IDataList<CommentChannelEntity> getAllChannelComment();
//	void addChannelComment(CommentChannelEntity entity);
//	void removeAllChannelComment();
//
//	void removeAllHotComment();
//	void removeAllNewsComment();
//
//	IDataList<CommentEntity> getHotCommentById(NewsItem n);

	IDataList<String> initUninterestList(String uid);
	void removeUninterestItem(NewsItem n);
	
}
