package com.gmobi.poponews.model;

import com.momock.data.DataMap;

/**
 * User: vivian .
 * Date: 2016-07-04
 * Time: 16:48
 */
public class CommentChannelEntity extends DataMap<Object, Object> {
    public static final String IId = "IId"; // 唯一标识
    public static final String UserName = "UserName"; // 评论的用户
    public static final String Content = "Content"; // 评论内容
    public static final String Path = "Path"; // 回复评论的标识
    public static final String ItemId = "ItemId"; // item项的id
    public static final String UserId = "UserId"; // 用户的id
    public static final String UserAvatar = "UserAvatar"; // 用户的头像
    public static final String ToAvatar = "ToAvatar"; // 回复用户的头像
    public static final String ToId = "ToId"; // 评论回复的id
    public static final String ToName = "ToName"; // 评论回复的昵称
    private CommentReplyEntity reply; // 评论回复集合
    private CommentChannelItem items;
    private long UpdateTime = 1l; // 评论时间

    public long getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        UpdateTime = updateTime;
    }

    public CommentChannelItem getItems() {
        return items;
    }

    public void setItems(CommentChannelItem items) {
        this.items = items;
    }

    public CommentReplyEntity getReply() {
        return reply;
    }

    public void setReply(CommentReplyEntity reply) {
        this.reply = reply;
    }

    public String getIId(){
        return (String) this.getProperty(IId);
    }

    public void setIId(String iId){
        this.setProperty(IId,iId);
    }

    public String getUserName() {
        return (String) this.getProperty(UserName);
    }

    public void setUserName(String title) {
        this.setProperty(UserName, title);
    }

    public String getContent() {
        return (String) this.getProperty(Content);
    }

    public void setContent(String content) {
        this.setProperty(Content, content);
    }

    public String getPath(){
        return (String) this.getProperty(Path);
    }

    public void setPath(String path){
        this.setProperty(Path,path);
    }

    public String getItemId(){
        return (String) this.getProperty(ItemId);
    }
    public void setItemId(String itemId){
        this.setProperty(ItemId,itemId);
    }

    public String getUserId(){
        return (String) this.getProperty(UserId);
    }

    public void setUserId(String userId){
        this.setProperty(UserId,userId);
    }

    public String getToId(){
        return (String) this.getProperty(ToId);
    }

    public void setToId(String toId){
        this.setProperty(ToId,toId);
    }

    public String getToName(){
        return (String) this.getProperty(ToName);
    }

    public void setToName(String toName){
        this.setProperty(ToName,toName);
    }

    public String getUserAvatar(){
        return (String) this.getProperty(UserAvatar);
    }

    public void setUserAvatar(String userAcatar){
        this.setProperty(UserAvatar,userAcatar);
    }

    public String getToAvatar(){
        return (String) this.getProperty(ToAvatar);
    }

    public void setToAvatar(String toAvatar){
        this.setProperty(ToAvatar,toAvatar);
    }

}
