package com.gmobi.poponews.service;

import android.util.Log;

import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.share.FbShare;
import com.gmobi.poponews.share.IShare;
import com.gmobi.poponews.share.LineShare;
import com.gmobi.poponews.share.MoreShare;
import com.gmobi.poponews.share.QqShare;
import com.gmobi.poponews.share.QzShare;
import com.gmobi.poponews.share.TwShare;
import com.gmobi.poponews.share.WbShare;
import com.gmobi.poponews.share.WeiboConstants;
import com.gmobi.poponews.share.WxCircleShare;
import com.gmobi.poponews.share.WxSessionShare;
import com.momock.app.App;
import com.momock.util.Logger;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ShareService implements IShareService {
	private static ArrayList<IShare> curShares = new ArrayList<>();
	private static Map<String, IShare> AllShares = new HashMap<>();

	@Override
	public boolean canStop() {
		return false;
	}

	@Override
	public Class<?>[] getDependencyServices() {
		return null;
	}


	@Override
	public void start() {
		//if weiboAvail
		PopoApplication.mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(App.get(), WeiboConstants.APP_KEY);
		boolean r = PopoApplication.mWeiboShareAPI.registerApp();
		Log.e("Share", "registerApp weibo " + r);


		setup();

	}

	@Override
	public void stop() {

	}

	@Override
	public void setup() {
		Logger.error("[SHARE]:Setup");
		AllShares.clear();

		FbShare fs = new FbShare();
		AllShares.put(fs.getControlName(), fs);

		LineShare ls = new LineShare();
		AllShares.put(ls.getControlName(), ls);

		QqShare qqs = new QqShare();
		AllShares.put(qqs.getControlName(), qqs);

		QzShare qzs = new QzShare();
		AllShares.put(qzs.getControlName(), qzs);

		TwShare tws = new TwShare();
		AllShares.put(tws.getControlName(), tws);

		WbShare wbs = new WbShare();
		AllShares.put(wbs.getControlName(), wbs);

		WxSessionShare wss = new WxSessionShare();
		AllShares.put(wss.getControlName(), wss);

		WxCircleShare wcs = new WxCircleShare();
		AllShares.put(wcs.getControlName(), wcs);

		MoreShare ms = new MoreShare();
		AllShares.put(ms.getControlName(), ms);

		curShares.clear();
	}

	@Override
	public ArrayList<IShare> getCurShares() {
		return curShares;
	}

	@Override
	public void clearShares() {
		curShares.clear();
	}

	@Override
	public IShare getShareByName(String name) {
		Iterator<Map.Entry<String, IShare>> entries = AllShares.entrySet().iterator();

		while (entries.hasNext()) {

			Map.Entry<String, IShare> entry = entries.next();

			if (entry.getKey().equals(name))
				return entry.getValue();

		}
		return null;
	}

	@Override
	public void addShare(String name, String title) {

		IShare find = getShareByName(name);
		if (find != null) {
			find.setTitle(title);
			curShares.add(find);
		}


	}


	@Override
	public void share(String name, String title, String webUrl, String imageUri) {
		IShare shareControl = getShareByName(name);
		if (shareControl != null)
			shareControl.share(title, webUrl, imageUri);
	}

}

