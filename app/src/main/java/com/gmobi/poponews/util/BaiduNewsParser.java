package com.gmobi.poponews.util;

import android.util.Base64;
import android.util.Log;

import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.momock.data.DataList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Administrator on 8/10 0010.
 * 序列化暂时不做
 */
public class BaiduNewsParser implements PostXmlParser{

	@Override
	public DataList<SocialPost> parse(File f, String ctg) throws Exception {
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
			fp.setPicture("", SocialExtra.SOCIAL_TYPE_BAIDU);
			fp.setFromid(ctg);
			fp.setFromname("");
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

					fp.setId(Base64.encodeToString(title.getBytes(),Base64.DEFAULT));
					fp.setName(title);



				}
				else if (nodeName.equals("link")) {
					fp.setLink(property.getFirstChild().getNodeValue());
				}
				else if (nodeName.equals("source")) {
					fp.setFromname(property.getFirstChild().getNodeValue());
				}
				else if (nodeName.equals("description")) {
					String des = property.getFirstChild().getNodeValue();
					String html = HtmlStringUtil.unescapeHTML(des, 0);

					String url  =getImgSrcFromHtml(html);
					if(url.equals(""))
						hasPic = false;
					fp.setPicture(url, SocialExtra.SOCIAL_TYPE_BAIDU);


				}
			}
			fp.setReleasetime(TimeUtil.getInstance().getCurUtcTime());
			fp.setSocialtype(SocialExtra.SOCIAL_TYPE_BAIDU);
			fp.setVisible(1);

			if(hasPic)
				posts.addItem(fp);

		}
		return posts;
	}

	@Override
	public DataList<SocialPost> parse(File f) throws Exception {
		return null;
	}

	@Override
	public String serialize(DataList<SocialPost> Posts) throws Exception {
		return null;
	}

	private final static String IMG_TAG = "src=\"";
	private final static String U_TAG = "/u=";
	private String getImgSrcFromHtml(String html)
	{
		int startpos = html.indexOf(IMG_TAG);
		if(startpos < 0)
			return "";

		String pref = html.substring(startpos+IMG_TAG.length());
		int endpos = pref.indexOf("\"");

		String baiduUrl = pref.substring(0, endpos);
		int upos = baiduUrl.indexOf(U_TAG);
		endpos = baiduUrl.indexOf("&",upos);

		String orgUrl = "";
		if(endpos < 0)
			orgUrl = baiduUrl.substring(upos + U_TAG.length());
		else
			orgUrl = baiduUrl.substring(upos + U_TAG.length(), endpos);

		try {
			orgUrl = URLDecoder.decode(orgUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


		Log.e("baidu","img src = "+orgUrl);
		return orgUrl;


	}


}
