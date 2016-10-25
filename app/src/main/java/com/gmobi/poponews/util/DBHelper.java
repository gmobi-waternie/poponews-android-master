package com.gmobi.poponews.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.gmobi.poponews.app.DbNames;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsImage;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.service.ConfigService;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.DataNode;
import com.momock.data.DataNodeView;
import com.momock.data.IDataList;
import com.momock.data.IDataMap;
import com.momock.data.IDataNode;
import com.momock.util.DataHelper;
import com.momock.util.JsonDatabase;
import com.momock.util.JsonDatabase.Collection;
import com.momock.util.JsonDatabase.Document;
import com.momock.util.JsonDatabase.IFilter;
import com.momock.util.Logger;

public class DBHelper {

	private static DBHelper ins;
	private static JsonDatabase db ;
	private static ConfigService configService;
	
	private Collection col;
	private Collection col_social;
	private Collection col_social_acc;
	private Collection col_ctgs;//categories:{edtion1:category1 json;edtion2:category2 json }
	private Collection col_connnect;//categories:{edtion1:category1 json;edtion2:category2 json }
	private Collection col_ctg; //cid:{offlineSelect:true}

	private Collection col_like; //aid:{useriddef: true  userid1:true  userid2:false  userid3:true}}
	private Collection col_uninterest;
	
	public  static final String TAG_FAV = "fav";
	public  static final String TAG_DATA = "data";
	public  static final String TAG_READ = "read";
	public  static final String TAG_PUSH = "push";
	public  static final String TAG_MOOD = "mood";
	public  static final String TAG_OFFLINE = "offline";
	public  static final String TAG_CACHE = "cache";
	public  static final String TAG_CHANNEL = "channel";
	public  static final String TAG_POST = "post";
	public  static final String TAG_ACC = "account";
	public  static final String TAG_COMMENT_COUNT = "count";
	public  static final String TAG_UNITERESTING = "uniteresting";

	public  static final String TAG_CTG_OFFLINE = "ctgoffline";
	public  static final String TAG_CTG_VISIBLE = "ctgvisible";
	public  static final String TAG_CTG_ORDER = "ctgorder";
	
	public  static final String DB_KEY_CTG = "categories";
	
	
	
	public static final int MAX_FAV_COUNT_PER_TIME = 256;
	public static final int MAX_READ_COUNT_PER_TIME = 50;
	public static final int MAX_OFFLINE_COUNT_PER_TIME = 1000;
	public static final int MAX_CACHE_COUNT_PER_TIME = 2000;
	public static final int MAX_PUSH_COUNT_PER_TIME = 50;

	public static final int MAX_SOCIAL_POST_COUNT_PER_TIME = 1000;
	public static final int MAX_UNI_COUNT_PER_TIME = 500;
	
	private DBHelper()
	{
		db = JsonDatabase.get(App.get().getCurrentContext(), DbNames.DATABASE_NEWS);

		col = db.getCollection(DbNames.COL_NEWS);
		col.setCachable(true);
		
		col_ctg= db.getCollection(DbNames.COL_CTG);
		col_ctg.setCachable(true);
		
		col_ctgs= db.getCollection(DbNames.COL_CTGS);
		col_ctgs.setCachable(true);

		col_social= db.getCollection(DbNames.COL_SOCIAL);
		col_social.setCachable(true);

		col_social_acc= db.getCollection(DbNames.COL_SOCIAL_ACCOUNT);
		col_social_acc.setCachable(true);

		col_connnect = db.getCollection(DbNames.COL_CONNECTDATA);
		col_connnect.setCachable(true);

		col_like = db.getCollection(DbNames.COL_LIKE);
		col_like.setCachable(true);

		col_uninterest = db.getCollection(DbNames.COL_UNINTEREST);
		col_uninterest.setCachable(true);

		configService = (ConfigService) App.get().getService(IConfigService.class);
	}
	
	synchronized public static DBHelper getInstance()
	{
		if(ins == null)
		{
			ins = new DBHelper();
		}
		return ins;
	}
	

	public void InsertItem(String nid,JSONObject jo)
	{
		if(col == null)
			return;
		
		col.set(nid, jo);
	}
	
	public void InsertCategoryItem(String cid,JSONObject jo)
	{
		if(col_ctg == null)
			return;
		
		col_ctg.set(cid, jo);
	}

	public void saveConnectData(JSONObject jo)
	{
		if(col_connnect == null)
			return;

		col_connnect.set("connect", jo);
	}


	public JSONObject getConnectData()
	{
		if(col_connnect == null)
			return null;

		return col_connnect.get("connect");
	}

	public void setApproval(String path, JSONObject jo){
		if (col_like == null){
			return;
		}
		col_like.set(path,jo);
	}

	public JSONObject getApproval(String path){
		if (col_like == null){
			return null;
		}
		return col_like.get(path);
	}

	public JSONObject getUserAction(String uid)
	{
		if(col_uninterest == null)
			return null;

		return col_uninterest.get(uid);
	}

	public void setUserAction(String uid,JSONObject jo)
	{
		if(col_uninterest == null)
			return;

		col_uninterest.set(uid, jo);
	}
	
	public JSONObject getItem(String nid)
	{
		if(col == null)
			return null;
		
		return col.get(nid);
	}
	
	public JSONObject getCategoryItem(String cid)
	{
		if(col_ctg == null)
			return null;
		
		return col_ctg.get(cid);
	}
	
	
	
	
	public void DeleteItem(String nid)
	{
		if(col == null)
			return;
		
		col.set(nid, null);
	}
	
	public void DeleteCategoryItem(String cid)
	{
		if(col_ctg == null)
			return;
		
		col_ctg.set(cid, null);
	}
	public JSONObject getData(String nid)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return null;
			
			JSONObject data = jo.getJSONObject(TAG_DATA);
			return data;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	
	public void setData(String nid, JSONObject data)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return;
			jo.put(TAG_DATA,data);
			
			InsertItem(nid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean getRead(String nid)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return false;
			
			boolean fav = jo.getBoolean(TAG_READ);
			return fav;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
	public void setRead(String nid, boolean read)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return;
			jo.put(TAG_READ,read);
			
			InsertItem(nid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public  JSONArray getCtg(String edition)
	{
		try {
			JSONObject jo = col_ctgs.get(DB_KEY_CTG);
			if(jo == null || !jo.has(edition))
				return null;
			
			JSONArray jaCtg  = jo.getJSONArray(edition);
			return jaCtg;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	public void setCtg(String edition ,JSONArray jaCtg)
	{
		try {
			JSONObject jo = col_ctgs.get(DB_KEY_CTG);
			if(jo == null)
			{
				JSONObject newjo = new JSONObject();
				newjo.put(edition, jaCtg);
				col_ctgs.set(DB_KEY_CTG, newjo);
			}
			else
			{
				jo.put(edition, jaCtg);
				col_ctgs.set(DB_KEY_CTG, jo);
			}
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
	}
	
	public boolean getCtgOfflineSelect(String cid)
	{
		try {
			JSONObject jo = null;
			if((jo=getCategoryItem(cid)) == null)
				return false;

			boolean sel  =false;
			if(jo.has(TAG_CTG_OFFLINE))
				sel = jo.getBoolean(TAG_CTG_OFFLINE);

			return sel;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
	
	public void setCtgOfflineSelect(String cid,boolean sel)
	{
		try {
			JSONObject jo = null;
			if((jo=getCategoryItem(cid)) == null)
				jo = new JSONObject();

			jo.put(TAG_CTG_OFFLINE,sel);
			
			InsertCategoryItem(cid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



	public int getCtgVisible(String cid)
	{
		try {
			JSONObject jo = null;
			if((jo=getCategoryItem(cid)) == null)
				return NewsCategory.VISIBLE;

			int v = NewsCategory.VISIBLE;
			if(jo.has(TAG_CTG_VISIBLE))
				v = jo.getInt(TAG_CTG_VISIBLE);

			return v;

		} catch (JSONException e) {

			e.printStackTrace();
			return NewsCategory.VISIBLE;
		}
	}


	public void setCtgVisible(String cid,int v)
	{
		try {
			JSONObject jo = null;
			if((jo=getCategoryItem(cid)) == null)
				jo = new JSONObject();

			jo.put(TAG_CTG_VISIBLE,v);

			InsertCategoryItem(cid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	public int getCtgOrder(String cid)
	{
		try {
			JSONObject jo = getCategoryItem(cid);
			if(jo == null)
				return 0;

			int o = 0;
			if(jo.has(TAG_CTG_ORDER))
				o = jo.getInt(TAG_CTG_ORDER);

			return o;

		} catch (JSONException e) {

			e.printStackTrace();
			return 0;
		}
	}


	public void setCtgOrder(String cid,int o)
	{
		try {
			JSONObject jo = getCategoryItem(cid);
			if(jo == null)
				jo = new JSONObject();

			jo.put(TAG_CTG_ORDER,o);

			InsertCategoryItem(cid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}






	public boolean getPush(String nid)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return false;
			
			boolean push = jo.getBoolean(TAG_PUSH);
			return push;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
	
	public boolean getFav(String nid)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return false;
			
			boolean fav = jo.getBoolean(TAG_FAV);
			return fav;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return false;
		}
	}
	
	public void setFav(String nid, boolean fav)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return;
			jo.put(TAG_FAV,fav);
			
			InsertItem(nid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getUninterest(String uid){
		try {
			JSONObject jo = null;
			if((jo=getUserAction(uid)) == null)
				return null;
			String uniterestId = jo.getString(TAG_UNITERESTING);
			return uniterestId;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setUninterest(String uid, String uninterestId){
		try {
			JSONObject jo = null;
			JSONArray unArray = null;
			if ((jo=getUserAction(uid)) == null) {
				jo = new JSONObject();
			}
			if(jo.has(TAG_UNITERESTING))
				unArray = jo.getJSONArray(TAG_UNITERESTING);

			if (unArray == null)
				unArray = new JSONArray();

			for(int i=0; i<unArray.length(); i++) {
				if (unArray.getString(i).equals(uninterestId))
					return;
			}

			unArray.put(uninterestId);
			jo.put(TAG_UNITERESTING,unArray);
			setUserAction(uid, jo);

			setFav(uninterestId, false);
			setRead(uninterestId,false);

		} catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	
	public String getCustomVote(String nid)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return null;
			if(!jo.has(TAG_MOOD))
				return null;

			String mood = jo.getString(TAG_MOOD);
			return mood;
			
		} catch (JSONException e) {
			
			e.printStackTrace();
			return null;
		}
	}

	public void setCustomVote(String nid, String mood)
	{
		try {
			JSONObject jo = null;
			if((jo=getItem(nid)) == null)
				return;
			jo.put(TAG_MOOD,mood);
			
			InsertItem(nid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public DataList<NewsItem> getFavListFromDB()
	{
		return getListFromDB(TAG_FAV);
	}
	
	public DataList<NewsItem> getReadListFromDB()
	{
		return getListFromDB(TAG_READ);
	}
	

	public DataList<NewsItem> getPushListFromDB()
	{
		return getListFromDB(TAG_PUSH);
	}
	
	
	
	
	public DataList<NewsItem> getListFromDB(String tag)
	{
		List<Document> l = null;

		Logger.error("getListFromDB tag="+tag);
		if(tag.equals(TAG_FAV))
		{
			l = col.list(new IFilter() {
				
				@Override
				public boolean check(String id, JSONObject doc) {
					try {
						return doc.getBoolean(TAG_FAV);
					} catch (JSONException e) {
						e.printStackTrace();
						return false;
					}
				}
			}, false, MAX_FAV_COUNT_PER_TIME);
		}
		else if (tag.equals(TAG_READ))
		{
			l = col.list(new IFilter() {
				
				@Override
				public boolean check(String id, JSONObject doc) {
					try {
						return doc.getBoolean(TAG_READ);
					} catch (JSONException e) {
						e.printStackTrace();
						return false;
					}
				}
			}, false, MAX_READ_COUNT_PER_TIME);
		}
		
		else if (tag.equals(TAG_OFFLINE))
		{
			l = col.list(new IFilter() {
				
				@Override
				public boolean check(String id, JSONObject doc) {
					try {
						return doc.getBoolean(TAG_OFFLINE);
					} catch (JSONException e) {
						e.printStackTrace();
						return false;
					}
				}
			}, false, MAX_OFFLINE_COUNT_PER_TIME);
		}
		else if (tag.equals(TAG_PUSH))
		{
			l = col.list(new IFilter() {
				
				@Override
				public boolean check(String id, JSONObject doc) {
					try {
						return doc.getBoolean(TAG_PUSH);
					} catch (JSONException e) {
						e.printStackTrace();
						return false;
					}
				}
			}, false, MAX_PUSH_COUNT_PER_TIME);
		}
		else
		{
			l = col.list(null, false, MAX_CACHE_COUNT_PER_TIME);
		}

		
		if(l == null)
			return null;
		
		DataList<NewsItem> items = new DataList<NewsItem>();
		try {
			for(int i=0; i<l.size(); i++)
			{
				//Log.e("fav",l.get(i).getData().getJSONObject(TAG_DATA).toString());
				DataNode node;
				JSONArray array = new JSONArray();
				array.put(0, l.get(i).getData().getJSONObject(TAG_DATA));

				if(l.get(i).getData().getJSONObject(TAG_DATA).toString().equals("{}"))
					continue;
				 
				node = (DataNode) DataHelper.parseJson(array.toString());
	
				IDataList<NewsItem>  SingleItemList = DataHelper.getBeanList(new DataNodeView(node,
						"*").getData(), NewsItem.class);
				NewsItem n = SingleItemList.getItem(0);
				
                EmoVote emoToMerge = new EmoVote();
                emoToMerge.copyPropertiesFrom((IDataMap)((IDataNode) node.getItem(0)).getProperty("mood"));
                n.setEmo(emoToMerge);
				
				IDataList<NewsImage>  imgsToMerge = DataHelper.getBeanList(new DataNodeView((IDataNode) node.getItem(0),"mm/*").getData(), NewsImage.class);
				n.setImgs((DataList<NewsImage>)imgsToMerge);

				setPreview(n,(DataList<NewsImage>)imgsToMerge);


				
				items.addItem(n);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return items;
	}
	
	public DataList<NewsCategory> getCtgFromDB(String edition)
	{
		List<Document> l = null;
		
		l = col_ctgs.list();
				
		if(l == null || l.size() == 0)
			return null;
		
		JSONObject allCtg = l.get(0).getData();
		if(!allCtg.has(edition))
			return null;
		
		DataList<NewsCategory> ctgs = null;
		JSONArray curCtg = null;
		try {
			curCtg = (JSONArray) allCtg.get(edition);
			DataNode node = (DataNode) DataHelper.parseJson(curCtg.toString());	
			ctgs = DataHelper.getBeanList(new DataNodeView(node,"*").getData(), NewsCategory.class);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ctgs;
	}



	public DataList<NewsCategory> getConnnectDataFromDB(String edition)
	{
		List<Document> l = null;

		l = col_ctgs.list();

		if(l == null || l.size() == 0)
			return null;

		JSONObject allCtg = l.get(0).getData();
		if(!allCtg.has(edition))
			return null;

		DataList<NewsCategory> ctgs = null;
		JSONArray curCtg = null;
		try {
			curCtg = (JSONArray) allCtg.get(edition);
			DataNode node = (DataNode) DataHelper.parseJson(curCtg.toString());
			ctgs = DataHelper.getBeanList(new DataNodeView(node,"*").getData(), NewsCategory.class);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ctgs;
	}

	public void savePostListToDB(SocialPost sp)
	{
		String jid = sp.getSocialtype() + sp.getId();
		JSONObject jo = getItem(jid);
		if(jo != null)
			return;

		jo = new JSONObject();

		try {
			jo.put(TAG_POST,sp.serialize());
			jo.put(TAG_CHANNEL,configService.getCurChannel());
			col_social.set(jid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}


	}



	public DataList<SocialPost> getPostListFromDB(final String social_type)
	{
		List<Document> l = col_social.list(new IFilter() {

				@Override
				public boolean check(String id, JSONObject doc) {
					try {
						if(doc.getJSONObject(TAG_POST).getString(SocialPost.Socialtype).equals(social_type)) {
							if(!social_type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE))
								return true;
							return (doc.getString(TAG_CHANNEL).equals(configService.getCurChannel()));
						}
						else
							return false;
					} catch (JSONException e) {
						e.printStackTrace();
						return false;
					}
				}
			}, false, MAX_SOCIAL_POST_COUNT_PER_TIME);

		DataList<SocialPost> postsList = new DataList<>();
		for(int i=0; i<l.size();i++)
		{
			JSONObject jo = null;
			try {
				jo = l.get(i).getData().getJSONObject(TAG_POST);
				SocialPost sp = SocialPost.parser(jo);
				postsList.addItem(sp);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return postsList;

	}

	public DataList<String> getUninterestListFromDB(final String uid){
		List<Document> list = col_uninterest.list(new IFilter() {
			@Override
			public boolean check(String s, JSONObject jsonObject) {
					Logger.debug("[DB] s="+s);
				Logger.debug("[DB] jsonObject="+jsonObject.toString());
				return (s.equals(uid));
			}
		},false,MAX_UNI_COUNT_PER_TIME);
		DataList<String> uninterestList = new DataList<>();
		if(list.size() > 0) {
			JSONArray ja = null;
			try {
				ja = list.get(0).getData().getJSONArray(TAG_UNITERESTING);
				for (int j = 0; j < ja.length(); j++) {
					uninterestList.addItem(ja.getString(j));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return uninterestList;
	}



	public void saveAccToDB(SocialAccount sa)
	{
		String jid = sa.getSocialtype() + sa.getId();
		JSONObject jo = getItem(jid);
		if(jo != null)
			return;


		jo = new JSONObject();

		try {
			jo.put(TAG_ACC,sa.serialize());
			jo.put(TAG_CHANNEL, configService.getCurChannel());
			col_social_acc.set(jid, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}



	public DataList<SocialAccount> getAccfromDB(final String social_type)
	{
		List<Document> l = col_social_acc.list(new IFilter() {

			@Override
			public boolean check(String id, JSONObject doc) {
				try {
					if(doc.getJSONObject(TAG_ACC).getString(SocialPost.Socialtype).equals(social_type))
					{
						if(!social_type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE))
							return true;
						String c = doc.getString(TAG_CHANNEL);
						String cc = configService.getCurChannel();
						return (c.equals(cc));
					}
					else
						return false;
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
		}, false, MAX_SOCIAL_POST_COUNT_PER_TIME);

		DataList<SocialAccount> accList = new DataList<>();
		for(int i=0; i<l.size();i++)
		{
			JSONObject jo = null;
			try {
				jo = l.get(i).getData().getJSONObject(TAG_ACC);
				SocialAccount sa = SocialAccount.parser(jo);
				accList.addItem(sa);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return accList;
	}


	private  void setPreview(NewsItem item,DataList<NewsImage> imgsToMerge)
	{
		int preW = 0, preH = 0;
		for (int i = 0; i < imgsToMerge.getItemCount(); i++) {
			if (imgsToMerge.getItem(i).getFile().equals(item.getPreview())) {
				preW = imgsToMerge.getItem(i).getWidth();
				preH = imgsToMerge.getItem(i).getHeight();
			}
		}
		//多图

		DataList<NewsImage> showList = new DataList<>();
		DataList<NewsImage> delList = new DataList<>();
		for (int i = 0; i < imgsToMerge.getItemCount(); i++) {
			showList.addItem(imgsToMerge.getItem(i));
		}
		if(showList.getItemCount()==0)
		{
			item.setLayoutType(NewsItem.NEWS_LAYOUT_NORMAL_RIGHT);
			String listPreview;
			listPreview = App.get().getService(IConfigService.class).getBaseImageUrl() +item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x";
			item.setListPreview(listPreview);
			return;
		}

		int avgW=0, avgH = 0;
		int minW = showList.getItem(0).getWidth(), maxW = showList.getItem(0).getWidth();
		int minH = showList.getItem(0).getHeight(), maxH = showList.getItem(0).getHeight();
		int minWIdx = 0,maxWIdx = 0,minHIdx = 0,maxHIdx = 0;
		for (int i = 0; i < showList.getItemCount(); i++) {
			if (i>0) {
				if (showList.getItem(i).getWidth() > maxW) {
					maxW = showList.getItem(i).getWidth();
					maxWIdx = i;
				}
				else if (showList.getItem(i).getWidth() < minW) {
					minW = showList.getItem(i).getWidth();
					minWIdx = i;
				}

				if (showList.getItem(i).getHeight() > maxH) {
					maxH = showList.getItem(i).getHeight();
					maxHIdx = i;
				}
				else if (showList.getItem(i).getHeight() < minH) {
					minH = showList.getItem(i).getHeight();
					minHIdx = i;
				}

			}

			avgW = avgW + showList.getItem(i).getWidth();
			avgH = avgH + showList.getItem(i).getHeight();
		}

		if(showList.getItemCount() > 2) {
			avgW = (avgW - maxW - minW) / (showList.getItemCount() - 2);
			avgH = (avgH - minH - maxH) / (showList.getItemCount() - 2);
		}
		else
		{
			avgW = avgW / showList.getItemCount();
			avgH = avgH / showList.getItemCount();
		}


		for (int i = 0; i < showList.getItemCount(); i++) {
			NewsImage nim = showList.getItem(i);
			if (nim.getWidth() > avgW * 1.25 || nim.getWidth() < avgW * 0.75)
			{
				delList.addItem(nim);
			}
			else if (nim.getHeight() > avgH * 1.25 || nim.getHeight() < avgH * 0.75)
			{
				delList.addItem(nim);
			}
		}

		for(int i=0; i <delList.getItemCount(); i++)
		{
			showList.removeItem(delList.getItem(i));
		}


		if(showList.getItemCount() >= 3)
		{
			item.setLayoutType(NewsItem.NEWS_LAYOUT_MULTIPICS);

			item.updateImgs(
					showList.getItem(0).getFile(),
					showList.getItem(1).getFile(),
					showList.getItem(2).getFile()
			);
			return;
		}



		//大图
		if (maxW > GlobalConfig.IMAGE_LARGE_WIDTH || maxH > GlobalConfig.IMAGE_LARGE_HEIGHT) {
			item.setLayoutType(NewsItem.NEWS_LAYOUT_LARGEPIC);

			String pinPreview;
			if (avgW >= avgH) {//横图
				pinPreview = App.get().getService(IConfigService.class).getBaseImageUrl() + imgsToMerge.getItem(maxWIdx).getFile() + "." + GlobalConfig.IMAGE_LARGE_WIDTH + "x" + GlobalConfig.IMAGE_LARGE_HEIGHT;
			} else//直图
			{
				pinPreview = App.get().getService(IConfigService.class).getBaseImageUrl() + imgsToMerge.getItem(maxHIdx).getFile() + "." + GlobalConfig.IMAGE_LARGE_WIDTH + "x";
			}
			item.setPinPreview(pinPreview);
		} else {
			item.setLayoutType(NewsItem.NEWS_LAYOUT_NORMAL_RIGHT);
			String listPreview;
			if (avgW >= avgH) {//横图
				listPreview = App.get().getService(IConfigService.class).getBaseImageUrl() + imgsToMerge.getItem(maxWIdx).getFile() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x" + GlobalConfig.IMAGE_NORMAL_HEIGHT;
			} else//直图
			{
				listPreview = App.get().getService(IConfigService.class).getBaseImageUrl() + imgsToMerge.getItem(maxHIdx).getFile() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x";
			}
			item.setListPreview(listPreview);
		}
	}

}

