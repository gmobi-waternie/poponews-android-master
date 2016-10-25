package com.gmobi.poponews.model;

import android.util.Log;

import com.gmobi.poponews.R;
import com.momock.app.App;
import com.momock.data.DataMap;
import com.momock.util.Convert;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Administrator on 6/18 0018.
 * 统一社交数据的post结构
 */
public class SocialPost extends DataMap<String, Object> {
	public final static String TYPE_LINK = "link";

	public final static String ID = "id";
	public final static String FROMID= "fromid";
	public final static String FROMNAME= "fromname";
	public final static String FROMAVATAR= "fromavatar";

	public final static String APPID= "appid";
	public final static String APPNAME= "appname";

	public final static String PICTURE= "picture";
	public final static String FULLPICTURE= "full_picture";

	public final static String LINK= "link";
	public final static String NAME= "name";
	public final static String CAPTION= "caption";
	public final static String TYPE= "type";

	public static final String Socialtype = "socailtype";
	public static final String RELEASETIME = "releasetime";
	public static final String VISIBLE = "visible";




	private final static String IMAGE_LIMIT = "#200x120";
	private final static String AVATAR_URL = "http://graph.facebook.com/{id}/picture?type=small";

	public String getId() {
		return (String)this.getProperty(ID);
	}

	public void setId(String id) {
		this.setProperty(ID, id);
	}

	public String getName() {
		return (String)this.getProperty(NAME);
	}

	public void setName(String name) {
		this.setProperty(NAME, name);
	}


	public String getFromid() {
		return (String)this.getProperty(FROMID);
	}

	public void setFromid(String id) {
		this.setProperty(FROMID, id);
	}

	public String getFromname() {
		return (String)this.getProperty(FROMNAME);
	}

	public void setFromname(String name) {
		this.setProperty(FROMNAME, name);
	}



	public String getAppid() {
		return (String)this.getProperty(APPID);
	}

	public void setAppid(String id) {
		this.setProperty(APPID, id);
	}

	public String getAppname() {
		return (String)this.getProperty(APPNAME);
	}

	public void setAppname(String name) {
		this.setProperty(APPNAME, name);
	}


	public String getPicture() {
		return (String)this.getProperty(PICTURE);
	}
	private String getPictureOriginalUrl(String orgUrl)
	{
		String newUrl;
		int pos = orgUrl.indexOf("&url=");
		if( pos < 0)
			return orgUrl;

		newUrl = orgUrl.substring(pos + 5);
		try {
			newUrl = URLDecoder.decode(newUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return orgUrl;
		}
		return newUrl;
	}

	public void setPicture(String pic,String socialtype) {
		if(pic.equals("") || pic == null)
			this.setProperty(PICTURE, "");
		else
		{
			String newUrl = pic;
			if(socialtype.equals(SocialExtra.SOCIAL_TYPE_FACEBOOK))
			{
				newUrl = getPictureOriginalUrl(pic);
				Logger.debug("Facebook:newUrl = " + newUrl);
			}
			this.setProperty(PICTURE, newUrl+IMAGE_LIMIT);
		}
	}

	public String getFromAvatar() {
		return (String)this.getProperty(FROMAVATAR);
	}

	public void setFromAvatar(String id,String socialtype) {
		String url = "";
		if (socialtype.equals(SocialExtra.SOCIAL_TYPE_FACEBOOK))
		{
			url = AVATAR_URL.replace("{id}", id);
			//Log.e("facebook","Facebook avatarUrl = "+url);
		}
		else if (socialtype.equals(SocialExtra.SOCIAL_TYPE_TWITTER))
		{

			url = id;
		}

		this.setProperty(FROMAVATAR, url);
	}



	public String getCaption() {
		return (String)this.getProperty(CAPTION);
	}

	public void setCaption(String caption) {
		this.setProperty(CAPTION, caption);
	}


	public String getLink() {
		return (String)this.getProperty(LINK);
	}

	public void setLink(String link) {
		this.setProperty(LINK, link);
	}


	public String getType() {
		return (String)this.getProperty(TYPE);
	}

	public void setType(String type) {
		this.setProperty(TYPE, type);
	}

	public static boolean isPoponewsLink(String type, String appname)
	{
		return type.equals(TYPE_LINK);
	}


	public String getSocialtype() {
		return (String)this.getProperty(Socialtype);
	}

	public void setSocialtype(String type) {
		this.setProperty(Socialtype, type);
	}

	public long getReleasetime() {
		return Convert.toLong(this.getProperty(RELEASETIME));
	}

	public void setReleasetime(long time) {
		this.setProperty(RELEASETIME, time);
	}

	public int getVisible() {
		return Convert.toInteger(this.getProperty(VISIBLE));
	}

	public void setVisible(int v) {
		this.setProperty(VISIBLE, v);
	}


	public JSONObject serialize()
	{
		JSONObject jo = new JSONObject();
		try {
			jo.put(ID, checkStringProperty(getId()));
			jo.put(NAME, checkStringProperty(getName()));
			jo.put(FROMID, checkStringProperty(getFromid()));
			jo.put(FROMNAME, checkStringProperty(getFromname()));
			jo.put(LINK, checkStringProperty(getLink()));
			jo.put(PICTURE, checkStringProperty(getPicture()));
			jo.put(Socialtype, checkStringProperty(getSocialtype()));
			jo.put(RELEASETIME, getReleasetime());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	public static SocialPost parser(JSONObject jo)
	{
		SocialPost sp = new SocialPost();
		if(jo == null)
			return null;
		Log.e("Social","Social post : "+ jo.toString());
		try {
			sp.setId(jo.getString(ID));
			sp.setName(jo.getString(NAME));
			sp.setLink(jo.getString(LINK));
			sp.setSocialtype(jo.getString(Socialtype));
			sp.setReleasetime(jo.getLong(RELEASETIME));

			sp.setPicture(jo.getString(PICTURE),jo.getString(Socialtype));
			sp.setFromname(jo.getString(FROMNAME));
			sp.setFromid(jo.getString(FROMID));

			if(jo.getString(Socialtype).equals(SocialExtra.SOCIAL_TYPE_FACEBOOK) || jo.getString(Socialtype).equals(SocialExtra.SOCIAL_TYPE_TWITTER))
				sp.setFromAvatar(sp.getFromid(),jo.getString(Socialtype));

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return sp;
	}

	private String checkStringProperty(String s)
	{
		return s==null ? "" : s;
	}



}


