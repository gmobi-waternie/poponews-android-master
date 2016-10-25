package com.gmobi.poponews.util;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentReplyEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.momock.app.App;
import com.momock.data.DataList;

/**
 * User: vivian .
 * Date: 2016-07-29
 * Time: 11:43
 */
public class AddCommentUtils {

    public static CommentEntity addNewsComment(CommentUserInfo info,String nid,String path,String textString){
        CommentEntity entity = new CommentEntity();
        DataList<CommentReplyEntity> childList = new DataList<CommentReplyEntity>();
        entity.setReplyList(childList);
        entity.setUpdateTime(MyTimeUtils.getReplyTime());
        entity.setUpdate(GlobalConfig.DATA_UPDATE);// 数据改变，重新创建TextView设置
        entity.setIsClick(true);
        entity.setIId(nid);
        if (info != null){
            entity.setUserName(info.getUserName());
            if (info.getAvatar() != null){
                entity.setUserAvatar(info.getAvatar());
            } else {
                entity.setUserAvatar(null);
            }
        } else {
            entity.setUserName(App.get().getResources().getString(R.string.comment_write_news));
            entity.setUserAvatar(null);
        }
        entity.setContent(textString);
        entity.setPath(path);
        if (info != null && info.getUId() != null){
            entity.setReplyId(info.getUId());
        } else {
            entity.setReplyId(null);
        }
        return entity;
    }

    public static void replyComment(CommentEntity entity,String textString,String userId,String userName,String toName,String path){
        entity.setUpdate(GlobalConfig.DATA_UPDATE);// 数据改变，重新创建TextView设置
        entity.setIsClick(true);
        DataList<CommentReplyEntity> reply = entity.getReplyList();
        CommentReplyEntity replyEntity = new CommentReplyEntity();
        replyEntity.setContent(textString);
        replyEntity.setUserId(userId);
        replyEntity.setUserName(userName);
        replyEntity.setToName(toName);
        replyEntity.setPath(path);
        reply.addItem(replyEntity);
        entity.setReplyList(reply);
    }

    public static CommentUserInfo remoteUserInfo(String nid,String userId,String tId,String path,String textString){
        CommentUserInfo userInfo = new CommentUserInfo();
        userInfo.setIid(nid);
        userInfo.setUId(userId);
        userInfo.setTId(tId);
        userInfo.setPath(path);
        userInfo.setContent(textString);
        return userInfo;
    }


}
