package com.gmobi.poponews.service;

import com.gmobi.poponews.util.DataCollect;
import com.momock.service.IService;
import com.momock.util.JsonDatabase.Collection;

public interface IReportService extends IService {
    String KEY_EMO_HAPPY = "happy";
    String KEY_EMO_MOVING = "moving";
    String KEY_EMO_ANGRY = "angry";
    String KEY_EMO_AMAZE = "amazing";
    String KEY_EMO_WORRY = "worry";
    String KEY_EMO_SAD = "sad";

    boolean recordTrackData(Object jsonData);
    boolean recordEmo(String aid, String emo);
	boolean recordFav(String aid, int fav);
    boolean recordFeedback(String msg);
    
	
	boolean recordPv(String aid);
	boolean recordList(String cid);
	boolean recordCrash(String thread, String msg, String deviceInfo);
    boolean recordPushRecv(String aid);
	boolean recordPushClick(String aid);
	boolean recordUninterest(String aid, String cid);


}
