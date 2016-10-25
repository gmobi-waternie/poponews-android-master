package com.gmobi.poponews.util;

import com.gmobi.poponews.model.CommentEntity;
import com.momock.data.DataList;

/**
 * User: vivian .
 * Date: 2016-07-29
 * Time: 10:43
 */
public class PathUtils {

    public static String getFirstPath(){
        return "1*" + System.currentTimeMillis();
    }

    public static String addCommentPath(String oldPath){
        return oldPath + "/{n}*" + System.currentTimeMillis();
    }

    public static String getPath(DataList<CommentEntity> localList,int position,int vId,String path,String addPath, boolean isReply){
        if (!isReply){
            if (path == null || "".equals(path)){
                if (localList.getItemCount() > 0){
                    path = PathUtils.addCommentPath(localList.getItem(position).getPath());
                } else {
                    path = PathUtils.addCommentPath(addPath);
                }
            } else {
                path = PathUtils.addCommentPath(path);
            }
            return path;
        } else {
            if (path == null || "".equals(path)){
                if (localList.getItemCount() > 0){
                    path = PathUtils.addCommentPath(localList.getItem(position).getReplyList().getItem(vId).getPath());
                } else {
                    path = PathUtils.addCommentPath(addPath);
                }
            } else {
                path = PathUtils.addCommentPath(path);
            }
            return path;
        }
    }
}
