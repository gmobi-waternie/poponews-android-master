package com.gmobi.poponews.service;

import com.gmobi.poponews.model.Comment;
import com.gmobi.poponews.model.CommentChannelEntity;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentReplyEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.service.IService;

/**
 * User: vivian .
 * Date: 2016-08-03
 * Time: 10:39
 */
public interface ICommentService extends IService {

    IDataList<CommentEntity> getAllNewsCommentList();
    IDataList<CommentEntity> getAllHotCommentList();
    IDataList<CommentEntity> getMoreHotCommentList();


    IDataList<CommentReplyEntity> getAllReplyList(String nid, int position, int type);
    void addItemEarlyHotComment(CommentEntity entity);
    void addItemHotComment(CommentEntity entity);
    void addItemNewsComment(String nid, CommentEntity entity);
    void addItemComment(String nid, CommentEntity entity);
    int getCommentCount(Comment comment);

    IDataList<CommentChannelEntity> getAllChannelComment();
    void addChannelComment(CommentChannelEntity entity);
    void removeAllChannelComment();
    void batchAddItems(DataList<CommentChannelEntity> ls);
    IDataList<CommentChannelEntity> getSizeChannelComment();

    void removeAllHotComment();
    void removeAllNewsComment();



    DataList<CommentEntity> getLocalList();
    void addLocalList(CommentEntity entity);
    void addNewsComment(String curNid,String edString,CommentUserInfo userInfo,boolean article);
    void replyComment(String nid,String edString, CommentUserInfo userInfo, int type, int position, int replyPos,
                      int vId, String userName, boolean isReply, String uId, String uPath,String addCommentPath);
    void replyReplies(CommentEntity entity);


}
