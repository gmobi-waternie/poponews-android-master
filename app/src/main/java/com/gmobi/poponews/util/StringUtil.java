package com.gmobi.poponews.util;

import com.momock.util.Logger;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 8/11 0011.
 */
public class StringUtil {
	public static String upperFirstLetter(String oldStr)
	{
		String first = oldStr.substring(0, 1).toUpperCase();
		String rest = oldStr.substring(1, oldStr.length());
		String newStr = new StringBuffer(first).append(rest).toString();
		return newStr;
	}


	public static String JsonToUrlParam(String jsonString) throws JSONException {

		JSONObject jsonObject = new JSONObject(jsonString);

		Iterator iterator = jsonObject.keys();
		String key = null;
		String value = null;
		String ret = "";
		while (iterator.hasNext()) {

			key = (String) iterator.next();
			value = jsonObject.getString(key);
			ret = ret + key + "=" + value+"&";
		}
		ret = ret.substring(0,ret.length()-2);
		return ret;

	}

	public static String UrlparamToJson(String urlParam) throws JSONException
	{

		JSONObject jo = new JSONObject();
		if (urlParam == null || urlParam.equals("")) {
			return "";
		}
		String[] params = urlParam.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) {
				jo.put(p[0], p[1]);
			}
		}

		return jo.toString();

	}
	public static boolean isEmail(String email) {

		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";

		Pattern p = Pattern.compile(str);

		Matcher m = p.matcher(email);

		return m.matches();

	}

	public static int isReply(String path){
		int leng = 0;
		for (int i=0; i < path.length(); i++){
			String getStr = path.substring(i, i+1);
			if (getStr.equals("/")){
				leng++;
			}
		}
		return leng;
	}



}
