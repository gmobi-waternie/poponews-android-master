package com.gmobi.poponews.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.StringUtil;
import com.momock.app.App;
import com.momock.util.Logger;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Administrator on 11/23 0023.
 */

public class ReferrerReceiver extends BroadcastReceiver {
	private static final String LOGTAG = "ReferrerReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOGTAG, "onReceive intent=" + intent);
		//处理referrer参数
		String referrer = intent.getStringExtra("referrer");
		try {
			String decodeReferrer = URLDecoder.decode(referrer,"UTF-8");

			if(decodeReferrer!=null && !decodeReferrer.equals(""))
			{
				Logger.error("Receive Referrer(Plain):" + decodeReferrer);

				try {
					String jsonString = StringUtil.UrlparamToJson(decodeReferrer);
					App.get().getService(IConfigService.class).setReferrer(jsonString);
					App.get().getService(IRemoteService.class).connect(false,false);
					Logger.error("Receive Referrer(JSON):" + jsonString);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


	}
}