package com.gmobi.poponews.model;

import android.util.Log;

import com.momock.data.DataMap;
import com.momock.util.Convert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 6/18 0018.
 * 统一社交数据的账户结构
 */
public class SocialAccount extends DataMap<String, Object> {

	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String EMAIL = "email";
	public final static String AGE = "age";


	public final static String NEXT= "next";
	public final static String ROLE= "role";
	public final static String SELECT= "select";
	public final static String RSSURL= "rss_url";
	public final static String RSSURLS= "rss_urls";
	public static final String EXTRA = "extra";
	public static final String Socialtype = "socailtype";


	public final static String ROLE_FRIEND = "friend";
	public final static String ROLE_FOLLOWING = "following";
	public final static String ROLE_PUBLIC_LIKE   = "pplike";
	public final static String ROLE_GOOGLE_CHANNEL   = "googlenews";
	public final static String ROLE_BAIDU_CHANNEL   = "baidunews";
	public static final String VISIBLE = "visible";
	public final static String FETCH_STATUS   = "fetchStatus";

	public final static int STATUS_UPDATING  = 1;
	public final static int STATUS_IDLE   = 0;

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


	public String getRole() {
		return (String)this.getProperty(ROLE);
	}

	public void setRole(String r) {
		this.setProperty(ROLE, r);
	}

	public String getNext() {
		return (String)this.getProperty(NEXT);
	}

	public void setNext(String next) {
		this.setProperty(NEXT, next);
	}

	public boolean isSelected() {
		return Convert.toBoolean(this.getProperty(SELECT));
	}

	public void setSelect(boolean s) {
		this.setProperty(SELECT, s);
	}

	public List<String> getRssurls() {
		return (List<String>)this.getProperty(RSSURLS);
	}

	public void setRssurls(List<String> urls) {
		this.setProperty(RSSURLS, urls);
	}


	public String getRssurl() {
		return (String)this.getProperty(RSSURL);
	}

	public void setRssurl(String url) {
		this.setProperty(RSSURL, url);
	}


	public int getFetchStatus() {
		if(hasProperty(FETCH_STATUS))
			return Convert.toInteger(this.getProperty(FETCH_STATUS));
		else
			return STATUS_IDLE;
	}

	public void setFetchStatus(int s) {
		this.setProperty(FETCH_STATUS, s);
	}

	public String getExtra() {
		return (String)this.getProperty(EXTRA);
	}

	public void setExtra(String e) {
		this.setProperty(EXTRA, e);
	}


	public String getSocialtype() {
		return (String)this.getProperty(Socialtype);
	}

	public void setSocialtype(String type) {
		this.setProperty(Socialtype, type);
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
			jo.put(RSSURL, checkStringProperty(getRssurl()));
			JSONArray jsonArray = new JSONArray();
			List<String> urls = getRssurls();
			if(urls != null)
			{
				for(int i=0; i<urls.size(); i++){
					jsonArray.put(urls.get(i));
				}
			}

			jo.put(RSSURLS, jsonArray);

			jo.put(NEXT, checkStringProperty(getNext()));
			jo.put(Socialtype, checkStringProperty(getSocialtype()));
			jo.put(EXTRA, checkStringProperty(getExtra()));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	public static SocialAccount parser(JSONObject jo)
	{
		SocialAccount sa = new SocialAccount();
		if(jo == null)
			return null;
		Log.e("Social", "Social post : " + jo.toString());
		try {
			sa.setId(jo.getString(ID));
			sa.setName(jo.getString(NAME));
			sa.setRssurl(jo.getString(RSSURL));
			sa.setSocialtype(jo.getString(Socialtype));
			sa.setNext(jo.getString(NEXT));
			sa.setExtra(jo.getString(EXTRA));

			JSONArray ja = jo.getJSONArray(RSSURLS);
			if(ja.length() == 0)
				sa.setRssurls(null);
			else
			{
				List<String> urls = new ArrayList<>();
				for(int i=0; i<ja.length(); i++) {
					urls.add((String)ja.get(i));
				}
				sa.setRssurls(urls);
			}


		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return sa;
	}
	private String checkStringProperty(String s)
	{
		return s==null ? "" : s;
	}

}

