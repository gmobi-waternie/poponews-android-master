package com.gmobi.poponews.model;
/**
 * 评论实体bean
 */
import com.gmobi.poponews.util.MyTimeUtils;
import com.gmobi.poponews.util.TimeUtil;
import com.momock.data.DataList;
import com.momock.data.DataMap;

public class CommentEntity extends DataMap<Object, Object> {
	public static final String IId = "IId"; // 唯一标识
	public static final String UserName = "UserName"; // 评论的用户
	public static final String Content = "Content"; // 评论内容
	public static final String Path = "Path"; // 回复评论的标识
	public static final String ItemId = "ItemId"; // item项的id
	public static final String UserId = "UserId"; // 用户的id
	public static final String UserAvatar = "UserAvatar"; // 用户的头像
	public static final String ToAvatar = "ToAvatar"; // 回复用户的头像
	public static final String ReplyId = "ReplyId"; // 评论回复的id
	public static final String ReplyName = "ReplyName"; // 评论回复的昵称

	private long UpdateTime = 1l; // 评论时间
	private boolean IsClick; // 是否显示展开更多按钮
	private boolean IsDing; // 是否点过赞
	private DataList<CommentReplyEntity> ReplyList = null; // 评论回复集合
	private int Update = 0; // 标识评论数据是否改变
	private int DingNumber = 0; // 点赞数量

	public CommentEntity() {
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

	public String getReplyId(){
		return (String) this.getProperty(ReplyId);
	}

	public void setReplyId(String replyId){
		this.setProperty(ReplyId,replyId);
	}

	public String getReplyName(){
		return (String) this.getProperty(ReplyName);
	}

	public void setReplyName(String replyName){
		this.setProperty(ReplyName,replyName);
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

	public long getUpdateTime() {
		return UpdateTime;
	}

	public void setUpdateTime(long updateTime) {
		UpdateTime = updateTime;
	}

	public DataList<CommentReplyEntity> getReplyList() {
		return ReplyList;
	}

	public void setReplyList(DataList<CommentReplyEntity> replyList) {
		ReplyList = replyList;
	}

	public int getUpdate() {
		return Update;
	}

	public void setUpdate(int update) {
		Update = update;
	}

	public int getDingNumber() {
		return DingNumber;
	}

	public void setDingNumber(int dingNumber) {
		DingNumber = dingNumber;
	}

	public String getLastTime() {
		return TimeUtil.getInstance().getLastTime(getUpdateTime());
	}

	public boolean isIsClick() {
		return IsClick;
	}

	public void setIsClick(boolean isClick) {
		IsClick = isClick;
	}

	public boolean isIsDing() {
		return IsDing;
	}

	public void setIsDing(boolean isDing) {
		IsDing = isDing;
	}

}
