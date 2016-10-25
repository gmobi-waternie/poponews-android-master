package com.gmobi.poponews.service;

import org.json.JSONObject;

import com.gmobi.poponews.model.CommentUserInfo;
import com.momock.service.IService;

public interface IRemoteService extends IService{

	void doService();
	void connect(boolean update,boolean needParse);

	void getConfig();
	void getContent(String nid);

    void getEditionList(int from);
    void getSingleNews(String nid,String extra,int type);
	void getBodyContent(String nid, String body);
	
	void getList(String cid, long time, int TimeFlag);
    boolean startDefaultReport(String requestBody);
    
    
	void getOfflineList(String cid);
	void stopDownloadOffline();
	void startDownloadOfflineImage(String uri);
	void startDownloadOfflineArticle(String uri);
	void startDownloadUpdateFile(String uri);
	void stopDownloadUpdateFile();
	
	
	
	JSONObject getDeviceInfo();
	
    void startDownloadHotNews(String i_id, int hotCount);
	void startDownloadNews(String i_id, int page, int newsCount);
	void addComment(CommentUserInfo info); // 评论
	void addReply(CommentUserInfo info, boolean isReply); // 回复
//	void addReplyTo(CommentUserInfo info); //回复的回复
	void doApproval(String i_id, String path);
	void startDownloadChannelComment(String u_id, String channel);
}
