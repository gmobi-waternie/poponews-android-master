package com.gmobi.poponews.util;

import com.gmobi.poponews.model.SocialPost;
import com.momock.data.DataList;

import java.io.File;


/**
 * Created by Administrator on 8/10 0010.
 */
public interface PostXmlParser {
	/**
	 * 解析输入流 得到Posts对象集合
	 * @param f
	 * @return
	 * @throws Exception
	 */
	DataList<SocialPost> parse(File f) throws Exception;
	DataList<SocialPost> parse(File f, String ctg) throws Exception;

	/**
	 * 序列化Posts对象集合 得到XML形式的字符串
	 * @param Posts
	 * @return
	 * @throws Exception
	 */
	String serialize(DataList<SocialPost> Posts) throws Exception;
}
