/*******************************************************************************
 * Copyright 2012 momock.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gmobi.poponews.service;

import android.util.Log;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsImage;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.NewsListItem;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.GsonImpl;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.DataListView;
import com.momock.data.DataNode;
import com.momock.data.DataNodeView;
import com.momock.data.IDataList;
import com.momock.data.IDataMap;
import com.momock.data.IDataMutableList;
import com.momock.data.IDataMutableMap;
import com.momock.data.IDataNode;
import com.momock.data.IDataView;
import com.momock.data.IDataView.IFilter;
import com.momock.data.IDataView.IOrder;
import com.momock.service.IAsyncTaskService;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.BeanHelper;
import com.momock.util.DataHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import javax.inject.Inject;

public class NewsDataService implements IDataService {

	@Inject
	IMessageService messageService;

	@Inject
	INewsCacheService newsCacheService;

	@Inject
	IConfigService configService;
	@Inject
	IAsyncTaskService asyncTaskService;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IUserService userService;

	private static int AD_POS = 5;//广告在10条新闻内的位置

	long curUtcTime;


	private int FEATURED_IMAGE_WIDTH = 720;
	private int FEATURED_IMAGE_HEIGHT = 400;

	String curNid = null;
	NewsItem curItem = null;

	DataList<NewsItem> items = new DataList<NewsItem>();
	DataList<NewsItem> cacheItems = new DataList<NewsItem>();
	DataList<NewsItem> additems = new DataList<NewsItem>();
	DataList<NewsListItem> listitems = new DataList<NewsListItem>();

	DataList<NewsItem> offlineitems = new DataList<NewsItem>();
	DataList<NewsItem> offlineitemsForMerge = new DataList<NewsItem>();
	DataList<NewsItem> favitems = new DataList<NewsItem>();
	DataList<NewsItem> pushitems = new DataList<NewsItem>();
	DataList<NewsItem> readitems = new DataList<NewsItem>();
	DataList<NewsItem> commentitems = new DataList<NewsItem>();

	DataList<NewsItem> allCacheItems = new DataList<NewsItem>();


	DataList<NewsCategory> categories = new DataList<NewsCategory>();
	DataList<NewsCategory> categoriesRestore = new DataList<NewsCategory>();
	DataList<Integer> categoriesVisibleRestore = new DataList<Integer>();


	IDataView<NewsCategory> visibleCategoriesView;
	IDataView<NewsCategory> offlineCategoriesView;
	IDataView<NewsCategory> allCategoriesView;
	IDataView<NewsItem> favItemsView;
	IDataView<NewsItem> readItemsView;
	IDataView<NewsItem> pushItemsView;
	IDataView<NewsItem> commentItemsView;


	HashMap<String, IDataView<NewsItem>> itemViews = new HashMap<>();
	HashMap<String, IDataView<NewsItem>> cacheItemViews = new HashMap<>();
	HashMap<String, IDataView<NewsListItem>> listitemViews = new HashMap<>();


	DataList<String> itemIds = new DataList<String>();
	DataList<String> cacheItemIds = new DataList<String>();
	DataList<String> uninterestedList = new DataList<String>();

	DBHelper dh;


	private final static int NOT_READ_CACHE = 0;
	private final static int READING_CACHE = 1;
	private final static int READED_CACHE = 2;
	int cacheReading = NOT_READ_CACHE;


	public NewsDataService() {

	}

	@Override
	public NewsItem getNewsById(String id) {
		for (int i = 0; i < items.getItemCount(); i++) {
			NewsItem p = items.getItem(i);
			if (p.get_id().equals(id))
				return p;
		}
		for (int i = 0; i < favitems.getItemCount(); i++) {
			NewsItem p = favitems.getItem(i);
			if (p.get_id().equals(id))
				return p;
		}
		for (int i = 0; i < readitems.getItemCount(); i++) {
			NewsItem p = readitems.getItem(i);
			if (p.get_id().equals(id))
				return p;
		}
		for (int i = 0; i < pushitems.getItemCount(); i++) {
			NewsItem p = pushitems.getItem(i);
			if (p.get_id().equals(id))
				return p;
		}
		for (int i = 0; i < commentitems.getItemCount(); i++) {
			NewsItem p = commentitems.getItem(i);
			if (p.get_id().equals(id))
				return p;
		}


		return null;
	}


	@Override
	public NewsCategory getCategoryById(String id) {
		for (int i = 0; i < categories.getItemCount(); i++) {
			NewsCategory c = categories.getItem(i);
			if (c.getid().equals(id))
				return c;
		}
		return null;
	}


	@Override
	public void restoreCategories() {
		categories.removeAllItems();
		for (int i = 0; i < categoriesRestore.getItemCount(); i++) {
			NewsCategory c = categoriesRestore.getItem(i);
			c.setVisible(categoriesVisibleRestore.getItem(i));
			categories.addItem(c);

		}

	}

	@Override
	public void saveCategories() {
		categoriesRestore.removeAllItems();
		categoriesVisibleRestore.removeAllItems();

		DataList<NewsCategory> list = (DataList<NewsCategory>) getAllCategories();
		for (int i = 0; i < list.getItemCount(); i++) {
			NewsCategory c = list.getItem(i);
			c.setOrder(i + 1);
			dh.setCtgOrder(c.getid(), i + 1);
			categoriesRestore.addItem(c);
			categoriesVisibleRestore.addItem(c.isVisible());

		}
	}


	@Override
	public IDataList<NewsCategory> getAllCategories() {


		if (allCategoriesView == null) {
			allCategoriesView = new DataListView<NewsCategory>(categories);
			allCategoriesView.setOrder(new IOrder<NewsCategory>() {
				@Override
				public int compare(NewsCategory lhs, NewsCategory rhs) {
					if (lhs.getOrder() > rhs.getOrder())
						return 1;
					else if (lhs.getOrder() < rhs.getOrder())
						return -1;
					else
						return 0;
				}
			});
		}
		return allCategoriesView.getData();
	}

	@Override
	public IDataList<NewsItem> initFavList() {
		favitems.beginBatchChange();
		favitems.removeAllItems();


		DataList<NewsItem> list = DBHelper.getInstance().getFavListFromDB();
		for (int i = 0; i < list.getItemCount(); i++)
			favitems.addItem(list.getItem(i));
		favitems.endBatchChange();
		return favitems;
	}

	@Override
	public IDataList<String> initUninterestList(String uid) {
		uninterestedList.removeAllItems();
		DataList<String> list = DBHelper.getInstance().getUninterestListFromDB(uid);
		for (int i = 0; i < list.getItemCount(); i++) {
			uninterestedList.addItem(list.getItem(i));
			Log.i("oye", "add item---" + i + "---list.getItem(i)---" + list.getItem(i));
		}
		return uninterestedList;
	}

	@Override
	public void removeUninterestItem(NewsItem n) {
		items.beginBatchChange();
		items.removeItem(n);
		items.endBatchChange();

		if (favitems.hasItem(n))
			removeFromFavList(n);
		if (pushitems.hasItem(n))
			removeFromPushList(n);

		if (readitems.hasItem(n))
			removeFromReadList(n);
	}

	@Override
	public void removeFromFavList(NewsItem n) {
		favitems.beginBatchChange();
		favitems.removeItem(n);
		favitems.endBatchChange();
	}

	@Override
	public void addIntoFavList(NewsItem n) {
		boolean exist = false;
		for (int i = 0; i < favitems.getItemCount(); i++) {
			if (favitems.getItem(i).get_id().equals(n.get_id())) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			favitems.beginBatchChange();
			favitems.addItem(n);
			favitems.endBatchChange();
		}
	}

	@Override
	public IDataList<NewsItem> initReadList() {
		readitems.beginBatchChange();
		readitems.removeAllItems();

		DataList<NewsItem> list = DBHelper.getInstance().getReadListFromDB();
		for (int i = 0; i < list.getItemCount(); i++)
			readitems.addItem(list.getItem(i));
		readitems.endBatchChange();
		return readitems;
	}

	@Override
	public void addIntoReadList(NewsItem n) {
		readitems.beginBatchChange();
		if (readitems.getItemCount() >= DBHelper.MAX_READ_COUNT_PER_TIME) {
			readitems.removeItemAt(readitems.getItemCount() - 1);
		}

		boolean has = false;
		for (int i = 0; i < readitems.getItemCount(); i++) {
			if (readitems.getItem(i).get_id().equals(n.get_id())) {
				has = true;
				break;
			}
		}
		if (!has)
			readitems.insertItem(0, n);

		readitems.endBatchChange();

	}


	@Override
	public IDataList<NewsItem> initPushList() {
		pushitems.beginBatchChange();
		pushitems.removeAllItems();

		DataList<NewsItem> list = DBHelper.getInstance().getPushListFromDB();
		for (int i = 0; i < list.getItemCount(); i++)
			pushitems.addItem(list.getItem(i));
		pushitems.endBatchChange();
		return pushitems;
	}

	@Override
	public void removeFromPushList(NewsItem n) {
		pushitems.beginBatchChange();
		pushitems.removeItem(n);
		pushitems.endBatchChange();
	}

	@Override
	public void addIntoPushList(NewsItem n) {
		pushitems.beginBatchChange();

		boolean has = false;
		for (int i = 0; i < pushitems.getItemCount(); i++) {
			if (pushitems.getItem(i).get_id().equals(n.get_id())) {
				has = true;
				break;
			}
		}

		if (!has)
			pushitems.addItem(n);

		pushitems.endBatchChange();
	}

	@Override
	public void addIntoCommentList(NewsItem n) {
		commentitems.beginBatchChange();

		boolean has = false;
		for (int i = 0; i < commentitems.getItemCount(); i++) {
			if (commentitems.getItem(i).get_id().equals(n.get_id())) {
				has = true;
				break;
			}
		}

		if (!has)
			commentitems.addItem(n);

		commentitems.endBatchChange();
	}

	@Override
	public IDataList<NewsItem> getCommentList() {
		if (commentItemsView == null) {
			commentItemsView = new DataListView<NewsItem>(commentitems);
		}
		return commentItemsView.getData();
	}


	@Override
	public long getEarlyReleaseTime(String cid, boolean cache) {
		IDataList<NewsItem> items = getNewsInCategory(cid, cache);
		if (items == null || items.getItemCount() == 0)
			return 0;

		NewsItem i = items.getItem(items.getItemCount() - 1);
		return i.getReleaseTime();
	}

	@Override
	public long getLatestReleaseTime(String cid, boolean cache) {
		IDataList<NewsItem> items = getNewsInCategory(cid, false);
		if (items == null || items.getItemCount() == 0)
			return 0;

		NewsItem i = items.getItem(0);
		return i.getReleaseTime();
	}

	/*
	@Override
	public IDataMutableList<NewsItem> getNewsInCategory(String cid) {
		DataList<NewsItem> ps = new DataList<NewsItem>();
		for(int i = 0; i < items.getItemCount(); i++)
		{
			NewsItem p = items.getItem(i);
			if (p.get_cid().equals(cid))
				ps.addItem(p);
		}
		return ps;
	}
	*/

	private NewsItem getFeaturedNews(String cid) {
		for (int i = 0; i < items.getItemCount(); i++) {
			NewsItem n = items.getItem(i);
			if (n.get_cid().equals(cid)) {
				if (n.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
					return n;
				}
			}

		}
		return null;
	}


	@Override
	public IDataList<NewsItem> getNewsInCategory(final String cid, boolean fromCache) {
		DataListView<NewsItem> itemView = (DataListView<NewsItem>) itemViews.get(cid);

		if (itemView == null) {
			itemView = new DataListView<NewsItem>(items);

			itemView.setFilter(new IFilter<NewsItem>() {

				@Override
				public boolean check(NewsItem item) {
					return item.get_cid().equals(cid);
				}
			});
			itemView.setOrder(new IOrder<NewsItem>() {

				@Override
				public int compare(NewsItem lhs, NewsItem rhs) {
					if (lhs.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
						return -1;
					}
					if (rhs.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
						return 1;
					}

					if (lhs.getReleaseTime() > rhs.getReleaseTime())
						return -1;
					else if (lhs.getReleaseTime() < rhs.getReleaseTime())
						return 1;
					else
						return 0;
				}
			});
			itemViews.put(cid, itemView);

		}
		return itemView.getData();
	}


	public IDataList<NewsItem> getCacheNews(final String cid) {
		DataListView<NewsItem> itemView = (DataListView<NewsItem>) cacheItemViews.get(cid);
		if (itemView == null) {
			itemView = new DataListView<NewsItem>(allCacheItems);

			itemView.setFilter(new IFilter<NewsItem>() {

				@Override
				public boolean check(NewsItem item) {
					return item.get_cid().equals(cid);
				}
			});
			itemView.setOrder(new IOrder<NewsItem>() {

				@Override
				public int compare(NewsItem lhs, NewsItem rhs) {
					if (lhs.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
						return -1;
					}


					if (lhs.getReleaseTime() > rhs.getReleaseTime())
						return -1;
					else if (lhs.getReleaseTime() < rhs.getReleaseTime())
						return 1;
					else
						return 0;
				}
			});
			cacheItemViews.put(cid, itemView);
		}
		return itemView.getData();
	}

	@Override
	public IDataList<NewsListItem> getNewsListInCategory(final String cid) {
		DataListView<NewsListItem> listitemView = (DataListView<NewsListItem>) listitemViews.get(cid);
		if (listitemView == null) {
			listitemView = new DataListView<NewsListItem>(listitems);

			listitemView.setFilter(new IFilter<NewsListItem>() {

				@Override
				public boolean check(NewsListItem item) {
					return item.getCid().equals(cid);
				}
			});

			listitemViews.put(cid, listitemView);
		}
		return listitemView.getData();
	}

	@Override
	public IDataList<NewsCategory> getAllVisibleCategories() {
		if (visibleCategoriesView == null) {
			visibleCategoriesView = new DataListView<NewsCategory>(categories);
			visibleCategoriesView.setFilter(new IFilter<NewsCategory>() {

				@Override
				public boolean check(NewsCategory c) {
					return (c.isVisible() > 0 && (!c.getCategoryType().equals(NewsCategory.TYPE_SOCIAL)));
				}
			});

		}
		visibleCategoriesView.setOrder(new IOrder<NewsCategory>() {
			@Override
			public int compare(NewsCategory lhs, NewsCategory rhs) {
				if (lhs.getOrder() > rhs.getOrder())
					return 1;
				else if (lhs.getOrder() < rhs.getOrder())
					return -1;
				else
					return 0;
			}
		});
		return visibleCategoriesView.getData();
	}

	@Override
	public IDataList<NewsCategory> getAllOfflineCategories() {
		if (offlineCategoriesView == null) {
			offlineCategoriesView = new DataListView<NewsCategory>(categories);
			offlineCategoriesView.setFilter(new IFilter<NewsCategory>() {

				@Override
				public boolean check(NewsCategory c) {
					if(UiHelper.isCherryVersion())
					{
						if(!c.getCategoryType().equals(NewsCategory.TYPE_NEWS))
							return false;
						if(c.getname().equals("Philippines"))
							return false;
					}

					return (c.isVisible() > 0 && (!c.getCategoryType().equals(NewsCategory.TYPE_SOCIAL)));
				}
			});

		}
		offlineCategoriesView.setOrder(new IOrder<NewsCategory>() {
			@Override
			public int compare(NewsCategory lhs, NewsCategory rhs) {
				if (lhs.getOrder() > rhs.getOrder())
					return 1;
				else if (lhs.getOrder() < rhs.getOrder())
					return -1;
				else
					return 0;
			}
		});
		return offlineCategoriesView.getData();
	}


	@Override
	public IDataList<NewsItem> getFavList() {
		if (favItemsView == null) {
			favItemsView = new DataListView<NewsItem>(favitems);
		}
		return favItemsView.getData();
	}

	@Override
	public IDataList<NewsItem> getReadList() {
		if (readItemsView == null) {
			readItemsView = new DataListView<NewsItem>(readitems);
		}
		return readItemsView.getData();
	}

	@Override
	public void removeFromReadList(NewsItem n) {
		readitems.beginBatchChange();
		readitems.removeItem(n);
		readitems.endBatchChange();
	}

	@Override
	public IDataList<NewsItem> getPushList() {
		if (pushItemsView == null) {
			pushItemsView = new DataListView<NewsItem>(pushitems);
		}
		return pushItemsView.getData();
	}

	@Override
	public void refreshFavList() {
		if (favItemsView == null) {
			favItemsView = new DataListView<NewsItem>(favitems);
		}
		favItemsView.setLimit(0);
		favItemsView.refresh();
	}

	@Override
	public void refreshReadList() {
		if (readItemsView == null) {
			readItemsView = new DataListView<NewsItem>(readitems);
		}
		readItemsView.setLimit(0);
		readItemsView.refresh();
	}

	@Override
	public void refreshPushList() {
		if (pushItemsView == null) {
			pushItemsView = new DataListView<NewsItem>(pushitems);
		}
		pushItemsView.setLimit(0);
		pushItemsView.refresh();
	}


	@Override
	public void refreshItems(final String cid) {
		DataListView<NewsItem> itemView = (DataListView<NewsItem>) itemViews.get(cid);

		if (itemView == null)
			return;

		itemView.setFilter(new IFilter<NewsItem>() {

			@Override
			public boolean check(NewsItem item) {
				return item.get_cid().equals(cid);
			}
		});
		itemView.setOrder(new IOrder<NewsItem>() {

			@Override
			public int compare(NewsItem lhs, NewsItem rhs) {
				if (rhs.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
					return 1;
				}
				if (lhs.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
					return -1;
				}


				if (lhs.getReleaseTime() > rhs.getReleaseTime())
					return -1;
				else if (lhs.getReleaseTime() < rhs.getReleaseTime())
					return 1;
				else
					return 0;
			}
		});
		itemView.refresh();

	}

	@Override
	public void refreshVisibleCategory() {

		visibleCategoriesView.setFilter(new IFilter<NewsCategory>() {

			@Override
			public boolean check(NewsCategory c) {
				return (c.isVisible() > 0);
			}
		});
		visibleCategoriesView.setOrder(new IOrder<NewsCategory>() {
			@Override
			public int compare(NewsCategory lhs, NewsCategory rhs) {
				if (lhs.getOrder() > rhs.getOrder())
					return 1;
				else if (lhs.getOrder() < rhs.getOrder())
					return -1;
				else
					return 0;
			}
		});
		visibleCategoriesView.refresh();
	}

	@Override
	public IDataMutableList<NewsItem> getAllNews() {
		return items;
	}

	@Override
	public void start() {
		dh = DBHelper.getInstance();
	}

	@Override
	public void stop() {

	}

	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{IAsyncTaskService.class, IConfigService.class};
	}

	@Override
	public boolean canStop() {
		return true;
	}

	boolean ctgReady = false;
	HashMap<String, Boolean> listReady = new HashMap<String, Boolean>();


	@Override
	public boolean isListReady(String cid) {
		return listReady.get(cid);
	}

//	@Override
//	public boolean isAllListReady() {
//		for (int i = 0; i < categories.getItemCount(); i++) {
//			if (listReady.containsKey(categories.getItem(i).getid()) == false || listReady.get(categories.getItem(i).getid()) == false)
//				return false;
//		}
//		return true;
//
//	}

	@Override
	public boolean isCtgReady() {
		return ctgReady;
	}


	private void resetListReady() {
		listReady.clear();
		for (int i = 0; i < categories.getItemCount(); i++)
			listReady.put(categories.getItem(i).getid(), false);
	}

	private void setListReady(String cid, boolean ready) {
		listReady.put(cid, ready);
	}


	@Override
	public boolean getCategoryFromCache() {
		int i;


		categories.beginBatchChange();
		categories.removeAllItems();
		categoriesRestore.removeAllItems();


		IDataList<NewsCategory> ctgToMerge = newsCacheService.getCategoryCache(configService.getCurChannel());
		if (ctgToMerge == null) {
			categories.endBatchChange();
			return false;
		}

		for (i = 0; i < ctgToMerge.getItemCount(); i++) {
			NewsCategory nc = ctgToMerge.getItem(i);
			String cid = nc.getid();


			nc.setOfflineFlag(dh.getCtgOfflineSelect(cid));
			nc.setCacheFlag(true);
			nc.setVisible(dh.getCtgVisible(cid));
			nc.setOrder(dh.getCtgOrder(cid));
			nc.setRefresh(NewsCategory.NOT_REFRESHING);
			nc.setLastIdx(0);

			int type = configService.getCategoryLayoutType(cid);
			if (type != 0)
				nc.setLayoutType(NewsCategory.LAYOUT_TYPE_STR[type - 1]);

			categories.addItem(nc);
			categoriesRestore.addItem(nc);
			categoriesVisibleRestore.addItem(NewsCategory.VISIBLE);
		}

		Logger.debug("Categories count = " + categories.getItemCount());

		categories.endBatchChange();


		if (categories.getItemCount() == 0) {
			return false;
		}

		ctgReady = true;
		resetListReady();


		return true;

	}

	@Override
	public void clearAllCacheContent(String cid, boolean all) {
		int i, len;

		allCacheItems.beginBatchChange();

		for (i = 0, len = allCacheItems.getItemCount(); i < len; ++i) {
			NewsItem n = allCacheItems.getItem(i);
			if (n.get_cid().equals(cid)) {
				allCacheItems.removeItem(n);
				--len;
				--i;
			}
		}
		allCacheItems.endBatchChange();


		if (all) {

			items.beginBatchChange();
			itemIds.beginBatchChange();

			for (i = 0, len = items.getItemCount(); i < len; ++i) {
				NewsItem n = items.getItem(i);
				if (n.get_cid().equals(cid)) {
					items.removeItem(n);
					itemIds.removeItem(n.get_id());
					--len;
					--i;
				}
			}

			items.endBatchChange();
			itemIds.endBatchChange();

			listitems.beginBatchChange();
			for (i = 0, len = listitems.getItemCount(); i < len; ++i) {
				NewsListItem n = listitems.getItem(i);
				if (n.getCid().equals(cid)) {
					listitems.removeItem(n);
					--len;
					--i;
				}
			}

			listitems.endBatchChange();
		}
	}


	@Override
	public void setCategory(String data, boolean update, boolean errorSend) {
		int i;
		IDataNode node;
		node = DataHelper.parseJson(data);
		categories.beginBatchChange();
		categories.removeAllItems();


		try {
			JSONArray ja = new JSONObject(data).getJSONArray("categories");


			IDataList<NewsCategory> ctgToMerge = DataHelper.getBeanList(new DataNodeView(node,
					"categories/*").getData(), NewsCategory.class);

			//clearOfflineCategoryList();

			for (i = 0; i < ctgToMerge.getItemCount(); i++) {
				NewsCategory nc = ctgToMerge.getItem(i);
				String cid = nc.getid();
				JSONObject catJo = (JSONObject) ja.get(i);

				nc.setOfflineFlag(dh.getCtgOfflineSelect(cid));
				nc.setCacheFlag(true);
				nc.setVisible(dh.getCtgVisible(cid));
				nc.setOrder(dh.getCtgOrder(cid));
				nc.setAdInsert(false);
				nc.setRefresh(NewsCategory.NOT_REFRESHING);
				nc.setLastIdx(0);
				nc.setLastLayoutIdx(-1);
				if (i == 0)
					nc.setHotFlag(true);
				else
					nc.setHotFlag(false);

				int type = configService.getCategoryLayoutType(cid);
				if (type != 0)
					nc.setLayoutType(NewsCategory.LAYOUT_TYPE_STR[type - 1]);


				if (nc.getCategoryType().equals(NewsCategory.TYPE_SOCIAL)) {
					if (catJo.has(NewsCategory.Extra)) {
						if ((catJo.get(NewsCategory.Extra) instanceof String) && catJo.getString(NewsCategory.Extra).equals("")) {
							Log.e("Social", "Extra is empty");
						} else {
							String extra = catJo.get(NewsCategory.Extra).toString();
							SocialExtra ss = GsonImpl.get().toObject(extra, SocialExtra.class);
							nc.setExtra(ss);

							App.get().getService(IFacebookService.class).setEnabled(false);
							App.get().getService(IGoogleService.class).setEnabled(false);
							App.get().getService(ITwitterService.class).setEnabled(false);
							App.get().getService(IBaiduService.class).setEnabled(false);
							App.get().getService(IWeiboService.class).setEnabled(false);

							if (ss.getSources() != null) {
								for (int j = 0; j < ss.getSources().size(); j++) {
									if (ss.getSources().get(j).getName().equals(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
										App.get().getService(IFacebookService.class).setEnabled(true);
										App.get().getService(IFacebookService.class).setTitle(ss.getSources().get(j).getTitle());
									}
									if (ss.getSources().get(j).getName().equals(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
										App.get().getService(IGoogleService.class).setEnabled(true);
										App.get().getService(IGoogleService.class).setTitle(ss.getSources().get(j).getTitle());
									}
									if (ss.getSources().get(j).getName().equals(SocialExtra.SOCIAL_TYPE_TWITTER)) {
										App.get().getService(ITwitterService.class).setEnabled(true);
										App.get().getService(ITwitterService.class).setTitle(ss.getSources().get(j).getTitle());
									}

									if (ss.getSources().get(j).getName().equals(SocialExtra.SOCIAL_TYPE_BAIDU)) {
										App.get().getService(IBaiduService.class).setEnabled(true);
										App.get().getService(IBaiduService.class).setTitle(ss.getSources().get(j).getTitle());
									}

									if (ss.getSources().get(j).getName().equals(SocialExtra.SOCIAL_TYPE_WEIBO)) {
										App.get().getService(IWeiboService.class).setEnabled(true);
										App.get().getService(IWeiboService.class).setTitle(ss.getSources().get(j).getTitle());
									}
								}

							}


							if (!App.get().getService(IFacebookService.class).isEnabled())
								SocialSetting.setStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK, false);
							if (!App.get().getService(IGoogleService.class).isEnabled())
								SocialSetting.setStatus(SocialExtra.SOCIAL_TYPE_GOOGLE, false);
							if (!App.get().getService(ITwitterService.class).isEnabled())
								SocialSetting.setStatus(SocialExtra.SOCIAL_TYPE_TWITTER, false);
							if (!App.get().getService(IBaiduService.class).isEnabled())
								SocialSetting.setStatus(SocialExtra.SOCIAL_TYPE_BAIDU, false);
							if (!App.get().getService(IWeiboService.class).isEnabled())
								SocialSetting.setStatus(SocialExtra.SOCIAL_TYPE_WEIBO, false);

							//social
							App.get().getService(IGoogleService.class).setCategory(nc);
							App.get().getService(IBaiduService.class).setCategory(nc);
						}
					}
				} else {
					categories.addItem(nc);
					categoriesRestore.addItem(nc);
					categoriesVisibleRestore.addItem(NewsCategory.VISIBLE);
				}


			}

			Logger.debug("Categories count = " + categories.getItemCount());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		categories.endBatchChange();


		if (categories.getItemCount() == 0 && errorSend) {
			messageService.send(this, MessageTopics.DATA_ERROR);
			return;
		}


		if (!newsCacheService.checkCategoryCache())//需要更新
		{
			try {

				JSONObject jo = new JSONObject(data);
				JSONArray ja = jo.getJSONArray("categories");
				if (ja != null)
					newsCacheService.setCategoryCache(ja);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		ctgReady = true;
		resetListReady();


		if (!update)
			messageService.send(this, MessageTopics.CATEGORY_LOADED);

	}


	public static <T, I extends IDataMap<String, Object>> DataList<T> getBeanList(IDataList<I> nodes, Class<T> beanClass) {
		DataList<T> dl = new DataList<T>();
		for (int i = 0; i < nodes.getItemCount(); i++) {
			try {
				T obj = beanClass.newInstance();
				if (obj instanceof IDataMutableMap) {
					IDataMutableMap<String, Object> target = (IDataMutableMap<String, Object>) obj;
					target.copyPropertiesFrom(nodes.getItem(i));
				} else {
					BeanHelper.copyPropertiesFromDataMap(obj, nodes.getItem(i));
				}
				dl.addItem(obj);
			} catch (Exception e) {
				Logger.error(e);
			}
		}
		return dl;
	}

	private boolean isItemExist(String nid) {
		for (int i = 0; i < itemIds.getItemCount(); i++) {
			if (itemIds.getItem(i).equals(nid))
				return true;
		}

		for (int i = 0; i < uninterestedList.getItemCount(); i++) {
			if (uninterestedList.getItem(i).equals(nid))
				return true;
		}
		return false;
	}


	@Override
	public synchronized void setNewsList(final String cid, final String data, final boolean fromCache, final boolean addAd) {
		if (fromCache)
			initCacheItemsList();


		if (!fromCache) {
			IDataNode node;
			JSONArray jsonDataArray = null;
			node = DataHelper.parseJson(data);


			try {
				jsonDataArray = new JSONArray(data);
				NewsCategory c = getCategoryById(cid);
				//c.setRefresh(NewsCategory.NOT_REFRESHING);

				//c.setCacheFlag(false);

				items.beginBatchChange();
				listitems.beginBatchChange();
				additems.removeAllItems();


				int preCount = items.getItemCount();


				IDataList<NewsItem> itemsToMerge = getBeanList(new DataNodeView(node, "*").getData(), NewsItem.class);

				boolean addAdThisTime = false;
				int adTotalCount = configService.getNativeAdCount();
				int interval = 0; // 广告间隔


				if (adTotalCount > 0)
					interval = 10 / adTotalCount;

				Logger.debug("[AD] interval =" + interval);
				int newsAddThisTime = 0;//判断此次被添加进列表的新闻个数，防止只有广告被加入。

				//如果处于cache状态下，需要判一下是否把缓存数据清除
				if (c.isCache()) {
					boolean needRefresh = false;
					for (int i = 0; i < itemsToMerge.getItemCount(); i++) {
						NewsItem ni = itemsToMerge.getItem(i);
						if (!isItemExist(ni.get_id())) {
							needRefresh = true;
							break;
						}
					}
					clearAllCacheContent(cid, needRefresh);
					newsAddThisTime = 1;
				}
				c.setCacheFlag(false);


				for (int i = 0; i < itemsToMerge.getItemCount(); i++) {


					//NewsItem Featured = getFeaturedNews(cid);
					NewsItem n = itemsToMerge.getItem(i);
					JSONObject itemjo = (JSONObject) jsonDataArray.get(i);
					IDataNode itemNode = (IDataNode) node.getItem(i);

					processItemDataExt(n, itemjo, itemNode, c);
//					if (c.getNewsLayoutType() == NewsCategory.LAYOUT_LIST)
//						setFeaturedItem(n, Featured);


					if (i == AD_POS && adTotalCount > 0 && addAd && newsAddThisTime > 2) {
						Logger.error("[AD]Entry ad  process");

						NewsItem ni = AdHelper.getInstance(App.get()).getNextAd(cid);

						if (ni != null) {
							Logger.error("add ad item");
							ni.set_cid(cid);
							ni.setReleaseTime(n.getReleaseTime());

							items.addItem(ni);
							additems.addItem(ni);
							itemIds.addItem(ni.get_id());

							AnalysisUtil.recordLTV();

							addAdThisTime = true;
						}
					}

					if (!isItemExist(n.get_id())) {
						newsAddThisTime++;
						Logger.error("item not exist");
						items.addItem(n);
						itemIds.addItem(n.get_id());
						additems.addItem(n);
					} else {
						Logger.error("item exist");
					}
				}

				if (addAdThisTime)
					c.setAdInsert(true);




			/*当ListType为Grid时，建立随机个数列表项，同时修改Preview宽高

				//if(c.getNewsLayoutType() == NewsCategory.LAYOUT_GRID)
				{
					int curLeftCount = additems.getItemCount();
					int curIdxInAddItemList = 0;

					int preLayoutIdx = c.getLastLayoutIdx();
					float scale = configService.getImageScale();
					int screenWidth = SystemHelper.getScreenWidth(App.get());

					for (int i = 0; i < additems.getItemCount(); i++) {
						NewsListItem li = new NewsListItem();
						li.setCid(cid);
						RandomLayoutFactory rlf = new RandomLayoutFactory();

					//每个Cat需要记住上次最后一个layout的type，防止重复
						int layoutIdx = rlf.setLeftItemCount(curLeftCount).setPreIdx(preLayoutIdx).getRandomIndex();
						preLayoutIdx = layoutIdx;
						c.setLastLayoutIdx(preLayoutIdx);

						int layoutType = rlf.generateLayoutType(layoutIdx);
						li.setListType(layoutType);


						int selectCount = 0;
						if (layoutType == NewsListItem.LIST_TYPE_1L || layoutType == NewsListItem.LIST_TYPE_1R)
							selectCount = 1;
						else if (layoutType == NewsListItem.LIST_TYPE_2)
							selectCount = 2;
						else if (layoutType == NewsListItem.LIST_TYPE_3)
							selectCount = 3;

						curLeftCount -= selectCount;
						//抓取符合数量的新闻项
						for (int j = 0; j < selectCount; j++, curIdxInAddItemList++) {
							NewsItem n = additems.getItem(curIdxInAddItemList);
							int w = IMAGE_WIDTH, h = IMAGE_HEIGHT;
							if (selectCount == 2 || selectCount == 3) {
								w = screenWidth / selectCount;
								h = (int) (w * scale);

							} else if (selectCount == 1) {
								w = DipHelper.dip2px(100);
								h = (int) (w * scale);

							}
							String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + n.getPreview() + "." + w + "x" + h + "t5";
							n.setGridPreview(preview);

							li.addNewsItem(n);
						}
						li.setPreviewAndTitle();
						listitems.addItem(li);

						if (curLeftCount == 0)
							break;


					}
				}
*/

				items.endBatchChange();
				listitems.endBatchChange();

				setListReady(cid, true);

				final HashMap<String, Object> msgData = new HashMap<String, Object>();
				msgData.put("cid", cid);
				msgData.put("precount", preCount);

				uiTaskService.run(new Runnable() {
					@Override
					public void run() {
						messageService.send(this, MessageTopics.NEWS_LOADED, msgData);
					}
				});


			} catch (JSONException e) {
				Logger.error("===PARSE DATA ERROR=============");
				e.printStackTrace();
			}
		} else {
			int i;

			if (cacheReading != READED_CACHE)
				return;

			NewsCategory c = getCategoryById(cid);

			items.beginBatchChange();
			listitems.beginBatchChange();
			additems.removeAllItems();


			int preCount = items.getItemCount();
			DataList<NewsItem> cacheSource = (DataList<NewsItem>) getCacheNews(cid);
			DataList<NewsItem> itemsToMerge = new DataList<>();
			int totalCount = cacheSource.getItemCount();

			for (i = 0; i < totalCount; i++) {
				if (i == 10)
					break;
				itemsToMerge.addItem(cacheSource.getItem(i));
			}


			for (i = 0; i < itemsToMerge.getItemCount(); i++) {

				allCacheItems.removeItem(itemsToMerge.getItem(i));


				NewsItem n = itemsToMerge.getItem(i);

//				NewsItem Featured = getFeaturedNews(cid);
//				if (c.getNewsLayoutType() == NewsCategory.LAYOUT_LIST)
//					setFeaturedItem(n, Featured);


				if (!isItemExist(n.get_id())) {
					items.addItem(n);
					itemIds.addItem(n.get_id());
					additems.addItem(n);
				}


			}


			/*当ListType为Grid时，建立随机个数列表项，同时修改Preview宽高

			//if(c.getNewsLayoutType() == NewsCategory.LAYOUT_GRID)
			{
				int curLeftCount = additems.getItemCount();
				int curIdxInAddItemList = 0;

				int preLayoutIdx = c.getLastLayoutIdx();
				float scale = configService.getImageScale();
				int screenWidth = SystemHelper.getScreenWidth(App.get());

				for (i = 0; i < additems.getItemCount(); i++) {
					NewsListItem li = new NewsListItem();
					li.setCid(cid);
					RandomLayoutFactory rlf = new RandomLayoutFactory();

					//每个Cat需要记住上次最后一个layout的type，防止重复
					int layoutIdx = rlf.setLeftItemCount(curLeftCount).setPreIdx(preLayoutIdx).getRandomIndex();
					preLayoutIdx = layoutIdx;
					c.setLastLayoutIdx(preLayoutIdx);

					int layoutType = rlf.generateLayoutType(layoutIdx);
					li.setListType(layoutType);


					int selectCount = 0;
					if (layoutType == NewsListItem.LIST_TYPE_1L || layoutType == NewsListItem.LIST_TYPE_1R)
						selectCount = 1;
					else if (layoutType == NewsListItem.LIST_TYPE_2)
						selectCount = 2;
					else if (layoutType == NewsListItem.LIST_TYPE_3)
						selectCount = 3;

					curLeftCount -= selectCount;
					//抓取符合数量的新闻项
					for (int j = 0; j < selectCount; j++, curIdxInAddItemList++){
						NewsItem n = additems.getItem(curIdxInAddItemList);
						int w = IMAGE_WIDTH, h = IMAGE_HEIGHT;
						if (selectCount == 2 || selectCount == 3) {
							w = screenWidth / selectCount;
							h = (int) (w * scale);

						} else if (selectCount == 1) {
							w = DipHelper.dip2px(100);
							h = (int) (w * scale);

						}
						String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + n.getPreview() + "." + w + "x" + h + "t5";
						n.setGridPreview(preview);

						li.addNewsItem(n);
					}
					li.setPreviewAndTitle();
					listitems.addItem(li);

					if (curLeftCount == 0)
						break;


				}
			}
*/

			items.endBatchChange();
			listitems.endBatchChange();

			setListReady(cid, true);

			final HashMap<String, Object> msgData = new HashMap<String, Object>();
			msgData.put("cid", cid);
			msgData.put("precount", preCount);
			messageService.send(this, MessageTopics.NEWS_LOADED, msgData);

		}


	}


	@Override
	public void setOfflineNewsList(String cid, String data) {
		IDataNode node;
		JSONArray jsonDataArray = null;
		node = DataHelper.parseJson(data);


		try {
			jsonDataArray = new JSONArray(data);


			offlineitems.beginBatchChange();

			offlineitems.removeAllItems();
			offlineitemsForMerge.removeAllItems();

			NewsCategory c = getCategoryById(cid);

			IDataList<NewsItem> itemsToMerge = getBeanList(new DataNodeView(node, "*").getData(), NewsItem.class);

			for (int i = 0; i < itemsToMerge.getItemCount(); i++) {

				NewsItem n = itemsToMerge.getItem(i);
				processItemDataExt(n, (JSONObject) jsonDataArray.get(i), (IDataNode) node.getItem(i), c);

				JSONObject jo = dh.getItem(n.get_id());
				if (jo != null) {
					jo.put(DBHelper.TAG_OFFLINE, true);
					dh.InsertItem(n.get_id(), jo);
				}

				offlineitems.addItem(n);
				offlineitemsForMerge.addItem(n);

			}
			offlineitems.endBatchChange();


		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IDataList<NewsItem> getOfflineNewsList() {
		return offlineitems;
	}

	@Override
	public void setCurNewsData(String data) {
		IDataNode node;
		node = DataHelper.parseJson(data);

		curItem = new NewsItem();
		curItem.set_id((String) node.getProperty("_id"));
		curItem.setTitle((String) node.getProperty("title"));
		curItem.setPname((String) node.getProperty("p_name"));
		curItem.setPdomain((String) node.getProperty("p_domain"));
		curItem.setSource((String) node.getProperty("source"));
		curItem.setBody((String) node.getProperty("body"));


		messageService.send(this, MessageTopics.NEWS_CONTENT_LOADED, curItem);
	}

	@Override
	public String getCurNid() {
		return curNid;
	}

	@Override
	public void setCurNid(String curNid) {
		this.curNid = curNid;
	}

	public int getCategoryIndex(String cid) {
		for (int i = 0; i < categories.getItemCount(); i++) {
			NewsCategory c = categories.getItem(i);
			if (c.getid().equals(cid))
				return i;
		}
		return -1;
	}

	public int getVoteNum(String nId, IVoteNum vn) {
		Logger.error("getVoteNum nid=" + nId);
		NewsItem newItem = getNewsById(nId);

		EmoVote ev = newItem.getEmo();
//		if (ev == null) {
//			ev = new EmoVote();
//			newItem.setEmo(ev);
//		}

		int val = 0;
		int total = 0;
		for (String emoName : ev.getPropertyNames()) {
			val = ev.getProperty(emoName);
			total += vn.onGetVote(emoName, val);
		}
		return total;
	}

	public void voteArticle(String nId, String vote) {
		NewsItem newItem = getNewsById(nId);
		EmoVote ev = newItem.getEmo();
		ev.setProperty(vote, ev.getProperty(vote) + 1);
	}


	private void processItemData(NewsItem item, JSONObject itemjo, IDataNode itemNode, NewsCategory parent) throws JSONException {
		JSONObject jo = dh.getItem(item.get_id());
		if (jo == null) {
			JSONObject newItemDb = new JSONObject();
			newItemDb.put("data", itemjo);
			newItemDb.put(DBHelper.TAG_FAV, false);
			newItemDb.put(DBHelper.TAG_READ, false);
			newItemDb.put(DBHelper.TAG_PUSH, false);
			dh.InsertItem(item.get_id(), newItemDb);
		} else {
			jo.put("data", itemjo);
			dh.InsertItem(item.get_id(), jo);
		}

		EmoVote emoToMerge = new EmoVote();
		emoToMerge.copyPropertiesFrom((IDataMap) itemNode.getProperty("mood"));
		item.setEmo(emoToMerge);

		IDataList<NewsImage> imgsToMerge = getBeanList(new DataNodeView(itemNode, "mm/*").getData(), NewsImage.class);
		item.setImgs((DataList<NewsImage>) imgsToMerge);


		int w1, w2, w3, h1, h2, h3;
		float scale = configService.getImageScale();
		int widthLimit = SystemHelper.getScreenWidth(App.get());
		w1 = DipHelper.dip2px(100);
		h1 = (int) (w1 * scale);

		w2 = widthLimit / 2;
		h2 = (int) (w2 * scale);

		w3 = widthLimit / 3;
		h3 = (int) (w3 * scale);


		String preview1 = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + w1 + "x" + h1 + "t5";
		String preview2 = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + w2 + "x" + h2 + "t5";
		String preview3 = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + w3 + "x" + h3 + "t5";
		item.setGrid1Preview(preview1);
		item.setGrid2Preview(preview2);
		item.setGrid3Preview(preview3);


		if (!item.getPreview().equals("")) {
			String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x";
			item.setPin2Preview(preview);

			preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x" + "t5";
			item.setPinPreview(preview);

			preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x" + GlobalConfig.IMAGE_NORMAL_HEIGHT + "t5";
			item.setListPreview(preview);
		}

		if (item.getImgs().getItemCount() > 0) {
			String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.calcPinPreview() + "t5";
			item.setPinPreview(preview);
		}


	}


	private void setFeaturedItem(NewsItem n, NewsItem Featured) {
		if (n.getType().equals(NewsItem.NEWS_TYPE_IMAGE)) {
			if (Featured != null) {
				if (n.getReleaseTime() > Featured.getReleaseTime()) {
					n.setType(NewsItem.NEWS_TYPE_FEATURED);
					String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + n.getPreview() + "." + FEATURED_IMAGE_WIDTH + "x" + FEATURED_IMAGE_HEIGHT + "t5";
					n.setListPreview(preview);


					Featured.setType(NewsItem.NEWS_TYPE_IMAGE);
					preview = App.get().getService(IConfigService.class).getBaseImageUrl() + n.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x" + GlobalConfig.IMAGE_NORMAL_HEIGHT + "t5";
					Featured.setListPreview(preview);

				}
			} else {
				n.setType(NewsItem.NEWS_TYPE_FEATURED);
				String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + n.getPreview() + "." + FEATURED_IMAGE_WIDTH + "x" + FEATURED_IMAGE_HEIGHT + "t5";
				n.setListPreview(preview);
			}

		}
	}

	//在这里设置
	private void processItemDataExt(NewsItem item, JSONObject itemjo, IDataNode itemNode, NewsCategory parent) throws JSONException {
		JSONObject jo = dh.getItem(item.get_id());
		if (jo == null) {
			JSONObject newItemDb = new JSONObject();
			newItemDb.put("data", itemjo);
			newItemDb.put(DBHelper.TAG_FAV, false);
			newItemDb.put(DBHelper.TAG_READ, false);
			newItemDb.put(DBHelper.TAG_PUSH, false);
			dh.InsertItem(item.get_id(), newItemDb);
		} else {
			jo.put("data", itemjo);
			dh.InsertItem(item.get_id(), jo);
		}

		EmoVote emoToMerge = new EmoVote();
		emoToMerge.copyPropertiesFrom((IDataMap) itemNode.getProperty("mood"));
		item.setEmo(emoToMerge);

		IDataList<NewsImage> imgsToMerge = getBeanList(new DataNodeView(itemNode, "mm/*").getData(), NewsImage.class);
		item.setImgs((DataList<NewsImage>) imgsToMerge);

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

		if (showList.getItemCount() == 0) {
			item.setLayoutType(NewsItem.NEWS_LAYOUT_NORMAL_RIGHT);
			String listPreview;
			listPreview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x";
			item.setListPreview(listPreview);
			return;
		}

		int avgW = 0, avgH = 0;
		int minW = showList.getItem(0).getWidth(), maxW = showList.getItem(0).getWidth();
		int minH = showList.getItem(0).getHeight(), maxH = showList.getItem(0).getHeight();
		int minWIdx = 0, maxWIdx = 0, minHIdx = 0, maxHIdx = 0;
		for (int i = 0; i < showList.getItemCount(); i++) {
			if (i > 0) {
				if (showList.getItem(i).getWidth() > maxW) {
					maxW = showList.getItem(i).getWidth();
					maxWIdx = i;
				} else if (showList.getItem(i).getWidth() < minW) {
					minW = showList.getItem(i).getWidth();
					minWIdx = i;
				}

				if (showList.getItem(i).getHeight() > maxH) {
					maxH = showList.getItem(i).getHeight();
					maxHIdx = i;
				} else if (showList.getItem(i).getHeight() < minH) {
					minH = showList.getItem(i).getHeight();
					minHIdx = i;
				}

			}

			avgW = avgW + showList.getItem(i).getWidth();
			avgH = avgH + showList.getItem(i).getHeight();
		}

		if (showList.getItemCount() > 2) {
			avgW = (avgW - maxW - minW) / (showList.getItemCount() - 2);
			avgH = (avgH - minH - maxH) / (showList.getItemCount() - 2);
		} else {
			avgW = avgW / showList.getItemCount();
			avgH = avgH / showList.getItemCount();
		}


		for (int i = 0; i < showList.getItemCount(); i++) {
			NewsImage nim = showList.getItem(i);
			if (nim.getWidth() > avgW * 1.25 || nim.getWidth() < avgW * 0.75) {
				delList.addItem(nim);
			} else if (nim.getHeight() > avgH * 1.25 || nim.getHeight() < avgH * 0.75) {
				delList.addItem(nim);
			}
		}

		for (int i = 0; i < delList.getItemCount(); i++) {
			showList.removeItem(delList.getItem(i));
		}


		if (showList.getItemCount() >= 3) {
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


	/*
	@Override
	public void addCategoryToOfflineList(OfflineCategoryItem item) {
		offlineList.add(item);

	}

	@Override
	public void removeCategoryFromOfflineList(OfflineCategoryItem item) {
		offlineList.remove(item);

	}

	@Override
	public void clearOfflineCategoryList() {
		offlineList.clear();
	}

	@Override
	public OfflineCategoryItem getOfflineCategoryItem(int idx) {
		if (idx > offlineList.size() - 1)
			return null;
		return offlineList.get(idx);
	}

	@Override
	public ArrayList<OfflineCategoryItem> getOfflineCategoryList() {

		return offlineList;
	}
	*/

	@Override
	public void mergeDataOfflineToOnline(String cid) {
		try {
			items.beginBatchChange();
			int preCount = items.getItemCount();

			for (int i = 0; i < offlineitemsForMerge.getItemCount(); i++) {

				NewsItem n = offlineitemsForMerge.getItem(i);

				JSONObject jo = dh.getItem(n.get_id());
				jo.put(DBHelper.TAG_OFFLINE, true);
				dh.InsertItem(n.get_id(), jo);
				if (!isItemExist(n.get_id())) {
					items.addItem(n);
					itemIds.addItem(n.get_id());
				}


			}
			items.endBatchChange();

			setListReady(cid, true);

			HashMap<String, Object> msgData = new HashMap<String, Object>();
			msgData.put("cid", cid);
			msgData.put("precount", preCount);
			messageService.send(this, MessageTopics.NEWS_LOADED, msgData);


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public NewsItem getPushItem(String data) {
		NewsItem item = null;
		DataNode node;
		try {
			JSONObject itemJo = new JSONObject(data);
			JSONArray array = new JSONArray();
			array.put(0, itemJo);

			node = (DataNode) DataHelper.parseJson(array.toString());

			IDataList<NewsItem> items = DataHelper.getBeanList(new DataNodeView(node,
					"*").getData(), NewsItem.class);
			item = items.getItem(0);
			JSONObject jo = dh.getItem(item.get_id());
			if (jo == null) {
				JSONObject newItemDb = new JSONObject();
				newItemDb.put("data", itemJo);
				newItemDb.put(DBHelper.TAG_FAV, false);
				newItemDb.put(DBHelper.TAG_READ, false);
				newItemDb.put(DBHelper.TAG_PUSH, true);
				dh.InsertItem(item.get_id(), newItemDb);
			} else {
				jo.put(DBHelper.TAG_PUSH, true);
				dh.InsertItem(item.get_id(), jo);
			}

			IDataList<NewsImage> imgsToMerge = getBeanList(new DataNodeView((IDataNode) node.getItem(0), "mm/*").getData(), NewsImage.class);
			item.setImgs((DataList<NewsImage>) imgsToMerge);

			EmoVote emoToMerge = new EmoVote();
			emoToMerge.copyPropertiesFrom((IDataMap) ((IDataNode) node.getItem(0)).getProperty("mood"));
			item.setEmo(emoToMerge);

			int w1, w2, w3, h1, h2, h3;
			float scale = configService.getImageScale();
			int widthLimit = SystemHelper.getScreenWidth(App.get());
			w1 = DipHelper.dip2px(100);
			h1 = (int) (w1 * scale);

			w2 = widthLimit / 2;
			h2 = (int) (w2 * scale);

			w3 = widthLimit / 3;
			h3 = (int) (w3 * scale);


			String preview1 = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + w1 + "x" + h1 + "t5";
			String preview2 = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + w2 + "x" + h2 + "t5";
			String preview3 = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + w3 + "x" + h3 + "t5";
			item.setGrid1Preview(preview1);
			item.setGrid2Preview(preview2);
			item.setGrid3Preview(preview3);


			if (!item.getPreview().equals("")) {
				String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x";
				item.setPin2Preview(preview);

				preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x" + "t5";
				item.setPinPreview(preview);

				preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.getPreview() + "." + GlobalConfig.IMAGE_NORMAL_WIDTH + "x" + GlobalConfig.IMAGE_NORMAL_HEIGHT + "t5";
				item.setListPreview(preview);
			}

			if (item.getImgs().getItemCount() > 0) {
				String preview = App.get().getService(IConfigService.class).getBaseImageUrl() + item.calcPinPreview() + "t5";
				item.setPinPreview(preview);
			}

			addIntoPushList(item);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return item;
	}

	@Override
	public void addIntoNewsList(NewsItem n) {
		items.beginBatchChange();

		boolean has = false;
		for (int i = 0; i < items.getItemCount(); i++) {
			if (items.getItem(i).get_id().equals(n.get_id())) {
				has = true;
				break;
			}
		}

		if (!has)
			items.addItem(n);

		items.endBatchChange();
	}

	@Override
	public synchronized void initCacheItemsList() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (cacheReading != NOT_READ_CACHE)
					return;

				allCacheItems.removeAllItems();
				DataList<NewsItem> list = (DataList<NewsItem>) newsCacheService.getNewsCache();
				allCacheItems.beginBatchChange();
				for (int i = 0; i < list.getItemCount(); i++) {
					allCacheItems.addItem(list.getItem(i));
				}
				allCacheItems.endBatchChange();

				cacheReading = READED_CACHE;

				uiTaskService.run(new Runnable() {
					@Override
					public void run() {
						messageService.send(this, MessageTopics.CACHE_INIT_FINISH);
					}
				});
			}


		}).start();


	}


}
