package com.gmobi.poponews.model;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.gmobi.poponews.R;
import com.gmobi.poponews.util.MyTimeUtils;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.StringUtil;
import com.momock.app.App;
import com.momock.data.DataMap;

public class CommentReplyEntity extends DataMap<String, Object> {
	public static final String Content = "Content"; // 回复内容
	public static final String IId = "IId"; // 唯一标识
	public static final String ItemId = "ItemId"; // item项的id
	public static final String Path = "Path";
	public static final String UserId = "UserId";
	public static final String ToId = "ToId";
	public static final String UserName = "UserName"; // 评论回复用户昵称
	public static final String ToName = "ToName"; // 评论被回复用户昵称
	public static final String UserAvatar = "UserAvatar";
	public static final String ToAvatar = "ToAvatar";

	private long ReplyTime = 1l; // 回复的时间

	public CommentReplyEntity() {
	}

	public Spanned getFullText(String lastName,String thisName,String path) {
		Spanned fullText;
		if (NightModeUtil.isNightMode()){
			if ( "".equals(lastName) && "".equals(thisName)){
				if (StringUtil.isReply(path) == 1){
					fullText = Html.fromHtml("<font color=\'#587CB2'>" + getToName()
							+ "</font>" + "<font color=\'#eeeeee'>"+":"+getContent() +"</font>");
				} else {
					if (getUserName() == getToName() || getUserName().equals(getToName())){
						fullText = Html.fromHtml("<font color=\'#587CB2'>" + getToName()
								+ "</font>" + "<font color=\'#eeeeee'>"+ ":" + getContent()+"</font>");
					} else {
						fullText = Html.fromHtml("<font color=\'#587CB2'>" + getUserName()
								+ "</font>" + "<font color=\'#f2f3f7'>"+App.get().getResources().getString(R.string.comment_reply)+"</font>"
								+ "<font color=\'#587CB2'>" + getToName() + "</font>" + "<font color=\'#eeeeee'>"+ ":" + getContent()+"</font>");
					}

				}
			} else {
				if (StringUtil.isReply(path) == 1) {
					fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
							+ "</font>" + "<font color=\'#eeeeee'>"+ ":" + getContent()+"</font>");
				} else {
					if (StringUtil.isReply(path) == 0){
						fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
								+ "</font>" + "<font color=\'#eeeeee'>"+ ":" + getContent()+"</font>");
					} else {
						if (thisName == lastName || thisName.equals(lastName)){
							fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
									+ "</font>" + "<font color=\'#eeeeee'>"+ ":" + getContent()+"</font>");
						} else {
							fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
									+ "</font>" + "<font color=\'#f2f3f7'>"+App.get().getResources().getString(R.string.comment_reply)+"</font>"
									+ "<font color=\'#587CB2'>" + thisName + "</font>" + "<font color=\'#eeeeee'>"+ ":" + getContent()+"</font>");
						}
					}

				}

			}
		} else {
			if ( "".equals(lastName) && "".equals(thisName)){
				if (StringUtil.isReply(path) == 1){
					fullText = Html.fromHtml("<font color=\'#587CB2'>" + getToName()
							+ "</font>" + ":" + getContent());
				} else {
					if (getUserName() == getToName() || getUserName().equals(getToName())){
						fullText = Html.fromHtml("<font color=\'#587CB2'>" + getToName()
								+ "</font>" + ":" + getContent());
					} else {
						fullText = Html.fromHtml("<font color=\'#587CB2'>" + getUserName()
								+ "</font>" + App.get().getResources().getString(R.string.comment_reply) + "<font color=\'#587CB2'>" + getToName()
								+ "</font>" + ":" + getContent());
					}

				}
			} else {
				if (StringUtil.isReply(path) == 1) {
					fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
							+ "</font>" + ":" + getContent());
				} else {
					if (StringUtil.isReply(path) == 0){
						fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
								+ "</font>" + ":" + getContent());
					} else {
						if (thisName == lastName || thisName.equals(lastName)){
							fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
									+ "</font>" + ":" + getContent());
						} else {
							fullText = Html.fromHtml("<font color=\'#587CB2'>" + lastName
									+ "</font>" + App.get().getResources().getString(R.string.comment_reply) + "<font color=\'#587CB2'>" + thisName
									+ "</font>" + ":" + getContent());
						}
					}

				}

			}
		}



		return fullText;

	}

	public String getIId(){
		return (String) this.getProperty(IId);
	}

	public void setIId(String iId){
		this.setProperty(IId,iId);
	}

	public String getItemId(){
		return (String) this.getProperty(ItemId);
	}
	public void setItemId(String itemId){
		this.setProperty(ItemId,itemId);
	}

	public String getPath(){
		return (String) this.getProperty(Path);
	}

	public void setPath(String path){
		this.setProperty(Path,path);
	}

	public long getReplyTime() {
		return ReplyTime;
	}

	public void setReplyTime(long replyTime) {
		ReplyTime = replyTime;
	}

	public String getLastReplyTime(){
		return MyTimeUtils.getLastTime(getReplyTime());
	}

	public String getContent() {
		return (String) this.getProperty(Content);
	}

	public void setContent(String content) {
		this.setProperty(Content, content);
	}

	public String getUserId() {
		return (String) this.getProperty(UserId);
	}

	public void setUserId(String userId) {
		this.setProperty(UserId, userId);
	}
	public String getToId() {
		return (String) this.getProperty(ToId);
	}

	public void setToId(String toId) {
		this.setProperty(ToId, toId);
	}

	public String getUserName(){
		return (String) this.getProperty(UserName);
	}
	public void setUserName(String userName){
		this.setProperty(UserName,userName);
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
