package com.gmobi.poponews.service;

import com.momock.util.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 9/9 0009.
 */
public class SocialHttpService {


	public static class Response {
		int statusCode;
		String body;
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
	}
	public interface ICallback{
		void onResult(JSONObject dn);
	}


	public static void connectWithHeader(final Header[] headers, final String url, final ICallback callback, boolean jsonformat){
		new Thread(){
			public void run() {
				Response resp = doGet(url,headers);

				Logger.debug("Connect Resp4 : " + resp.getBody() + "(" + resp.getStatusCode() + ")");

				if (resp.getStatusCode() == 200){

					JSONObject jo = null;
					try {
						jo = new JSONObject(resp.getBody());
					} catch (JSONException e) {
						callback.onResult(null);
						e.printStackTrace();
						return;
					}
					callback.onResult(jo);
				} else {
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						Logger.debug(e.toString());
					}
				}
			}

		}.start();
	}

	public static  Response doGet(String url,Header[] headers){

		final DefaultHttpClient client = new DefaultHttpClient();

		//String encodeUrl = java.net.URLEncoder.encode(url);
		HttpGet httpget = new HttpGet(url);
		if(headers != null)
			httpget.setHeaders(headers);
		//Logger.debug("encode url = " + encodeUrl+", url = "+url);


		Response resp = new Response();
		try {
			Logger.debug("doget 1,url="+url);
			HttpResponse response = client.execute(httpget);
			Logger.debug("doget 2");
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				resp.statusCode = response.getStatusLine().getStatusCode();
				if(resp.statusCode == 200)
					resp.body = EntityUtils.toString(entity);
				else if (resp.statusCode == 302)
				{
					Header[] rspheaders = response.getHeaders("Location");
					resp.body = rspheaders[0].getValue();
					Logger.debug("Redirect Addr = "+resp.body);
				}



			}
		} catch (Exception e) {
			Logger.debug(e.toString());
		}
		return resp;
	}
}
