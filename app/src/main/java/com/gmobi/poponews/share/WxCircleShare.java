package com.gmobi.poponews.share;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.cases.main.MainActivity;
import com.momock.app.App;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;

import javax.inject.Inject;

public class WxCircleShare implements IShare {
	@Inject
	Resources resource;


	private boolean avail;
	private final static String NAME = "wechat.moments";



	private String name;
	private String title;



	@Override
	public int getControlDrawable() {
		return R.drawable.share_moments;
	}

	@Override
	public String getControlName() {
		return NAME;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public boolean isAvailable() {
		return avail;
	}

	@Override
	public void share(String title, String webUrl, String imageUri) {

		if(!SystemHelper.isInstalled(App.get(), "com.tencent.mm"))
		{
			Toast.makeText(App.get(), App.get().getResources().getString(R.string.share_not_install), Toast.LENGTH_SHORT).show();
			return;
		}



		WXWebpageObject webpageObject = new WXWebpageObject();
		webpageObject.webpageUrl = webUrl;

		WXMediaMessage msg = new WXMediaMessage(webpageObject);
		msg.title = title;
		msg.description = webUrl;

		Logger.debug("[SHARE]image uri=" + imageUri);

/*
		if(f.exists()) {
			Logger.debug("[SHARE]image exist");
			thumb = BitmapFactory.decodeFile(f.getAbsolutePath());
		}
		else
		{
			Logger.debug("[SHARE]image need download");
			try {
				URL url = new URL(imageUri);

				URLConnection conn = url.openConnection();
				conn.connect();
				InputStream is=conn.getInputStream();


				thumb =BitmapFactory.decodeStream(is);


			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}

*/
		Bitmap thumb = BitmapFactory.decodeResource(resource, R.drawable.ic_launcher);
		if (thumb != null)
			msg.thumbData = WxShareUtil.bmpToByteArray(thumb, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;


		req.scene = SendMessageToWX.Req.WXSceneTimeline;

		MainActivity.wxApi.sendReq(req);

	}

	@Override
	public void setAvailable(boolean avail) {
		this.avail = avail;
	}


	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}
