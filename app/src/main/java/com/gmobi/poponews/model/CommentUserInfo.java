package com.gmobi.poponews.model;

import com.momock.data.DataMap;


/**
 * Created by nage on 2016/5/31.
 */
public class CommentUserInfo extends DataMap<String, Object> {
    public static final String UserName = "UserName";
    public static final String UId = "UId";
    public static final String TId = "TId";
    public static final String Email = "Email";
    public static final String Pwd = "Pwd";
    public static final String Content = "Content";
    public static final String Path = "Path";
    public static final String IID = "IID";
    public static final String Avatar = "Avatar";
    public boolean update;
    public boolean login;

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public void setEmail(String email) {
        this.setProperty(Email, email);
    }

    public String getEmail() {
        return (String) this.getProperty(Email);
    }

    public String getUserName() {
        return (String) this.getProperty(UserName);
    }

    public void setUserName(String userName) {
        this.setProperty(UserName, userName);
    }

    public String getUId() {
        return (String) this.getProperty(UId);
    }

    public void setUId(String uId) {
        this.setProperty(UId, uId);
    }
    public String getTId() {
        return (String) this.getProperty(TId);
    }

    public void setTId(String tId) {
        this.setProperty(TId, tId);
    }

    public void setPwd(String pWd){
        this.setProperty(Pwd,pWd);
    }

    public String getPwd(){
        return (String) this.getProperty(Pwd);
    }

    public void setContent(String content){
        this.setProperty(Content,content);
    }

    public String getContent(){
        return (String) this.getProperty(Content);
    }

    public void setPath(String path){
        this.setProperty(Path,path);
    }

    public String getPath(){
        return (String) this.getProperty(Path);
    }

    public void setIid(String iId){
        this.setProperty(IID,iId);
    }

    public String getIid(){
        return (String) this.getProperty(IID);
    }
    public void setAvatar(String avatar){
        this.setProperty(Avatar,avatar);
    }

    public String getAvatar(){
        return (String) this.getProperty(Avatar);
    }
}
