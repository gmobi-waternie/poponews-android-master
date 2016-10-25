package com.gmobi.poponews.util;

import android.support.v4.util.TimeUtils;
import android.text.Html;
import android.util.Base64;
import android.util.Log;

import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialExtra;
import com.momock.data.DataList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Administrator on 8/10 0010.
 * 序列化暂时不做
 */
public class GoogleNewsParser implements PostXmlParser{

	@Override
	public DataList<SocialPost> parse(File f) throws Exception {
		DataList<SocialPost> posts = new DataList<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  //取得DocumentBuilderFactory实例
		DocumentBuilder builder = factory.newDocumentBuilder(); //从factory获取DocumentBuilder实例
		Document doc = builder.parse(f);   //解析输入流 得到Document实例
		Element rootElement = doc.getDocumentElement();
		NodeList channel = rootElement.getElementsByTagName("channel");
		NodeList items = channel.item(0).getChildNodes();

		boolean hasPic;
		for (int i = 0; i < items.getLength(); i++) {
			SocialPost fp = new SocialPost();
			fp.setType("");
			fp.setPicture("",SocialExtra.SOCIAL_TYPE_GOOGLE);
			fp.setFromid("");
			Node item = items.item(i);
			hasPic = true;

			if (!item.getNodeName().equals("item"))
				continue;

			NodeList properties = item.getChildNodes();
			for (int j = 0; j < properties.getLength(); j++) {
				Node property = properties.item(j);
				String nodeName = property.getNodeName();
				if (nodeName.equals("title")) {

					String title = property.getFirstChild().getNodeValue();
					int pos = title.lastIndexOf("-");

					if(pos >= 0) {
						String subTitle  = title.substring(0, pos);
						String fromname  = title.substring(pos+1);
						fp.setId(Base64.encodeToString(subTitle.getBytes(),Base64.DEFAULT));
						fp.setName(subTitle);
						fp.setFromname(fromname);
					}else{
						fp.setId(Base64.encodeToString(title.getBytes(),Base64.DEFAULT));
						fp.setName(title);
						fp.setFromname("");
					}

				}
				else if (nodeName.equals("link")) {
					fp.setLink(property.getFirstChild().getNodeValue());
				}
				else if (nodeName.equals("category")) {
					fp.setFromid(property.getFirstChild().getNodeValue());
				}
				else if (nodeName.equals("description")) {
					String des = property.getFirstChild().getNodeValue();
					String html = HtmlStringUtil.unescapeHTML(des, 0);
					//Log.e("google", "des = " + html);
					String url  =getImgSrcFromHtml(html);
					if(url.equals(""))
						hasPic = false;
					fp.setPicture(url,SocialExtra.SOCIAL_TYPE_GOOGLE);


				}
			}
			fp.setReleasetime(TimeUtil.getInstance().getCurUtcTime());
			fp.setSocialtype(SocialExtra.SOCIAL_TYPE_GOOGLE);
			fp.setVisible(1);

			if(hasPic)
				posts.addItem(fp);

		}
		return posts;
	}

	@Override
	public DataList<SocialPost> parse(File f, String ctg) throws Exception {
		return null;
	}

	@Override
	public String serialize(DataList<SocialPost> Posts) throws Exception {
		return null;
	}

	private final static String IMG_TAG = "<img src=\"";
	private String getImgSrcFromHtml(String html)
	{
		int startpos = html.indexOf(IMG_TAG);
		if(startpos < 0)
			return "";

		String pref = html.substring(startpos+IMG_TAG.length());
		int endpos = pref.indexOf("\"");

		String url = "http:" + pref.substring(0,endpos);
		Log.e("google","img src = "+url);
		return url;


	}


}
