package com.gmobi.poponews.outlet;


import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.javascript.ExecJsApi;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.service.RemoteService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.RefreshableView;
import com.gmobi.poponews.widget.RefreshableView.PullToRefreshListener;
import com.momock.app.App;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.IComponentHolder;
import com.momock.holder.ViewHolder;
import com.momock.holder.ViewHolder.OnViewCreatedHandler;
import com.momock.outlet.card.CardPlug;
import com.momock.service.IMessageService;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;


public class CategoryTabPlug extends CardPlug {
	private NewsCategory category;
	private ComposedItemBinder newsBinder = null;
	private ListViewBinder lvb;
	private RecyclerViewBinder rvb;
	private RecyclerView mRecyclerView;
	private ImageButton mBtnTop;
	private BGARefreshLayout mRefreshLayout;


	private final static String CHERRY_PRELOAD_URL1 = "http://noah.dost.gov.ph/#/";

	int imgSelectId;
	int index = 0;
	ViewHolder content;

	IDataService ds;
	IRemoteService rs;
	IConfigService cs;
	IMessageService ms;

	DBHelper dh;

	public CategoryTabPlug(final ICase<?> kase, NewsCategory cat, int imgSelectId, int id) {
		this(kase, cat, id);
		this.imgSelectId = imgSelectId;
		ds = kase.getService(IDataService.class);
		rs = kase.getService(IRemoteService.class);
		cs = kase.getService(IConfigService.class);
		ms = kase.getService(IMessageService.class);
		dh = DBHelper.getInstance();

	}

	public CategoryTabPlug(final ICase<?> kase, final NewsCategory cat, final int id) {
		category = cat;

		//final boolean nightmode = NightModeUtil.isNightMode();

		//content = ViewHolder.create(kase, R.layout.home_list, null);
		if (category.getCategoryType().equals(NewsCategory.TYPE_NEWS)) {
			int layoutType = category.getNewsLayoutType();
			int layoutId = R.layout.news_main_list;
			if (layoutType == NewsCategory.LAYOUT_PIN2)
				layoutId = R.layout.news_pin_list;

			content = ViewHolder.create(kase, layoutId, new OnViewCreatedHandler() {

				@Override

				public void onViewCreated(View view) {

					final CaseActivity ctx = (CaseActivity)kase.getAttachedObject();

					int layoutType = category.getNewsLayoutType();

					if (layoutType == NewsCategory.LAYOUT_LIST)

//						newsBinder = new NewsItemBinderFactory(ca, NewsItemBinderFactory.TYPE_LIST)
//								.setCategoryId(category.getid())
//								.setFeatureSupport(true)
//								.build();
						newsBinder = new NewsItemComposedBinderFactory(ctx,NewsItemBinderFactory.TYPE_LIST)
								.setCategoryId(category.getid())
								.build();
					else if (layoutType == NewsCategory.LAYOUT_GRID)
						newsBinder = new NewsListItemBinderFactory(ctx)
								.setCategoryId(category.getid())
								.setFeatureSupport(false)
								.build();
					else if (layoutType == NewsCategory.LAYOUT_PIN)
						newsBinder = new NewsPinBinderFactory(ctx)
								.setCategoryId(category.getid())
								.setFeatureSupport(true)
								.build();
					else if (layoutType == NewsCategory.LAYOUT_PIN2)
						newsBinder = new NewsPin2BinderFactory(ctx)
								.setCategoryId(category.getid())
								.setFeatureSupport(true)
								.build();


					if (layoutType == NewsCategory.LAYOUT_PIN2) {


						rvb = new RecyclerViewBinder(newsBinder);

						rvb.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
							@Override
							public void process(Object sender, ItemEventArgs args) {
								if (category.getNewsLayoutType() != NewsCategory.LAYOUT_GRID) {
									NewsItem i = (NewsItem) args.getItem();
									ds.setCurNid(i.get_id());

									dh.setRead(i.get_id(), true);
									i.setHaveRead(NewsItem.NEWS_HAVE_READ);
									ds.addIntoReadList(i);


									if(!i.getGo2Src())
										UiHelper.openArticleFromApp(ctx, i.get_id());
									else
										UiHelper.openBrowserActivity(ctx, i.get_id(), i.getType(), i.getSource(), i.getTitle(), i.getPdomain(), "");

									//kase.getCase(CaseNames.ARTICLE).run();
									if (!i.getType().equals(NewsItem.NEWS_TYPE_IMAGE))
										rs.getBodyContent(i.get_id(), i.getBody());
								}

							}

						});


						mRecyclerView = ViewHolder.get(view, R.id.plv_newslist).getView();
						mRefreshLayout = ViewHolder.get(view, R.id.refreshable_view).getView();
						mBtnTop = ViewHolder.get((CaseActivity)kase.getAttachedObject(), R.id.btn_top).getView();


						mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
							@Override
							public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
								super.onScrollStateChanged(recyclerView, newState);
								if (newState == RecyclerView.SCROLL_STATE_IDLE) {

									UiHelper.startBtnTopTimer();
								}

								if(newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING)
								{
									UiHelper.stopBtnTopTimer();
									ms.send(this,MessageTopics.CLOSE_ARC_MENU);
									mBtnTop.setVisibility(View.VISIBLE);
								}
							}

						});

						mRefreshLayout.setDelegate(new BGARefreshLayout.BGARefreshLayoutDelegate() {
							@Override
							public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
								new Thread() {
									public void run() {
										Log.e("mRefreshLayout", "拉到顶部，可以获取数据了");
										//AnalysisUtil.recordNewsListNext(category.getid(),category.getname());
										requestRefresh();
									}
								}.start();
							}

							@Override
							public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
								new Thread() {
									public void run() {
										Log.e("mRefreshLayout", "拉到底部，可以获取数据了");
										//AnalysisUtil.recordNewsListPrev(category.getid(), category.getname());
										requestLoadingMore();
									}
								}.start();
								return true;
							}
						});


						BGANormalRefreshViewHolder refreshViewHolder  = new BGANormalRefreshViewHolder((CaseActivity)kase.getAttachedObject(),true);
						refreshViewHolder.setLoadingMoreText(ctx.getResources().getString(R.string.main_loading));
						refreshViewHolder.setPullDownRefreshText(ctx.getResources().getString(R.string.pull_to_refresh));
						refreshViewHolder.setRefreshingText(ctx.getResources().getString(R.string.refreshing));
						refreshViewHolder.setReleaseRefreshText(ctx.getResources().getString(R.string.release_to_refresh));

						mRefreshLayout.setRefreshViewHolder(refreshViewHolder);

						//列数为两列
						int spanCount = 2;
						StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(
								spanCount,
								StaggeredGridLayoutManager.VERTICAL);
						mRecyclerView.setLayoutManager(mLayoutManager);


					} else {

						lvb = new ListViewBinder(newsBinder);

						lvb.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
							@Override
							public void process(Object sender, ItemEventArgs args) {
								if (category.getNewsLayoutType() != NewsCategory.LAYOUT_GRID) {
									NewsItem i = (NewsItem) args.getItem();
									ds.setCurNid(i.get_id());

									dh.setRead(i.get_id(), true);
									i.setHaveRead(NewsItem.NEWS_HAVE_READ);
									ds.addIntoReadList(i);


									if (!i.getGo2Src())
										UiHelper.openArticleFromApp(ctx, i.get_id());
									else
										UiHelper.openBrowserActivity(ctx, i.get_id(), i.getType(), i.getSource(), i.getPname(), i.getPdomain(), "");


									if (!i.getType().equals(NewsItem.NEWS_TYPE_IMAGE))
										rs.getBodyContent(i.get_id(), i.getBody());
								}

							}

						});

						ListView newsListview = ViewHolder.get(view, R.id.plv_newslist).getView();
						mBtnTop = ViewHolder.get((CaseActivity)kase.getAttachedObject(), R.id.btn_top).getView();

						newsListview.setSelector(R.color.bg_transparent);

						newsListview.setBackgroundResource(NightModeUtil.isNightMode() ? R.color.bg_black_night : R.color.bg_white);
						//newsListview.setLoadEnable(false);
						RefreshableView refreshableView = ViewHolder.get(view, R.id.refreshable_view).getView();
						Log.e("RefreshableView", "refreshableView = " + refreshableView.toString() + "cat=" + category.getname());

						refreshableView.setOnRefreshListener(new PullToRefreshListener() {

							@Override
							public void onRefresh() {
								//AnalysisUtil.recordNewsListNext(category.getid(), category.getname());
								requestRefresh();
							}
						}, id);

						final View moreView =
								((CaseActivity) kase.getAttachedObject()).getLayoutInflater().inflate(
										R.layout.footer_fresh_item,	null);
						AbsListView.LayoutParams lp = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, DipHelper.dip2px(81));
						moreView.setLayoutParams(lp);




						newsListview.addFooterView(moreView);

						ImageView iv = ViewHolder.get(moreView, R.id.iv_loading).getView();

						iv.setBackgroundResource(R.drawable.loading_anim);
						AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
						loadAnim.setOneShot(false);
						loadAnim.start();

						newsListview.setOnScrollListener(new OnScrollListener() {
							int lastItem;
							int count;
							boolean scrolling;

							@Override
							public void onScrollStateChanged(AbsListView view, int scrollState) {
								if (scrollState == SCROLL_STATE_IDLE) {
									if(lastItem == count)
									{
										Log.e("fresh", "拉到最底部");
										if(NightModeUtil.isNightMode())
											moreView.findViewById(R.id.rl_footer_fresh).setBackgroundColor(
													App.get().getResources().getColor( R.color.bg_black_night));
										else
											moreView.findViewById(R.id.rl_footer_fresh).setBackgroundColor(
													App.get().getResources().getColor(R.color.bg_white));

										moreView.setVisibility(View.VISIBLE);


										category.setLastIdx(count);
										//AnalysisUtil.recordNewsListPrev(category.getid(), category.getname());
										requestLoadingMore();

									}

									UiHelper.startBtnTopTimer();
								}

								if(scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING)
								{
									UiHelper.stopBtnTopTimer();
									ms.send(this, MessageTopics.CLOSE_ARC_MENU);
									mBtnTop.setVisibility(View.VISIBLE);

								}

							}

							@Override
							public void onScroll(AbsListView view, int firstVisibleItem,
												 int visibleItemCount, int totalItemCount) {
								lastItem = firstVisibleItem + visibleItemCount - 1;
								count = totalItemCount - 1;



							}
						});
					}


					if (category.getNewsLayoutType() == NewsCategory.LAYOUT_LIST || category.getNewsLayoutType() == NewsCategory.LAYOUT_PIN)
						lvb.bind(ViewHolder.get(view, R.id.plv_newslist), ds.getNewsInCategory(category.getid(),category.isCache()));
					else if (category.getNewsLayoutType() == NewsCategory.LAYOUT_PIN2)
						rvb.bind(ViewHolder.get(view, R.id.plv_newslist), ds.getNewsInCategory(category.getid(),category.isCache()));
					else
						lvb.bind(ViewHolder.get(view, R.id.plv_newslist), ds.getNewsListInCategory(category.getid()));

				}
			});
		}
		/*社交媒体分页显示*/
		else if (category.getCategoryType().equals(NewsCategory.TYPE_FACEBOOK)) {

			content = ViewHolder.create(kase, R.layout.news_facebook, new OnViewCreatedHandler() {

				@Override

				public void onViewCreated(View view) {

				}
			});
		}
		else if (category.getCategoryType().equals(NewsCategory.TYPE_VK)) {
			content = ViewHolder.create(kase, R.layout.news_webview, new OnViewCreatedHandler() {

				@Override
				public void onViewCreated(View view) {
					WebView wv = ViewHolder.get(view,R.id.wv_list).getView();
					WebSettings webSettings = wv.getSettings();
					webSettings.setJavaScriptEnabled(true);

					// set webview UA
					String gUa = webSettings.getUserAgentString();
					gUa += " (CHANNEL/" + cs.getCurChannel();
					gUa += "; APPCHANNEL/" + cs.getCurStoreChannel();
					gUa += "; PACKAGEID/" + App.get().getPackageName();
					gUa += "; APPVER/" + SystemHelper.getAppVersion(App.get()) + ")";
					Logger.debug("webview ua : " + gUa);
					webSettings.setUserAgentString(gUa);
					initWebSettings(wv);

					wv.setWebViewClient(new WebViewClient(){
						@Override
						public boolean shouldOverrideUrlLoading(WebView view, String url) {
							view.loadUrl(url);
							return super.shouldOverrideUrlLoading(view, url);
						}
					});
					wv.loadUrl(CHERRY_PRELOAD_URL1);

				}
			});
		}
		else if (category.getCategoryType().equals(NewsCategory.TYPE_SOCIAL)) {
			content = ViewHolder.create(kase, R.layout.news_social, new OnViewCreatedHandler() {

				@Override
				public void onViewCreated(View view) {


				}
			});
		}


	}


	public ListViewBinder getBinder() {
		return lvb;
	}


	public NewsCategory getCategory() {
		return category;
	}

	@Override
	public IComponentHolder getComponent() {
		return content;
	}


	private void requestLoadingMore()
	{
		category.setRefresh(NewsCategory.IS_LOADINGMORE);
		String cid = category.getid();
		App.get().getService(IReportService.class).recordList(cid);
		rs.getList(cid, ds.getEarlyReleaseTime(cid,category.isCache()), RemoteService.EARLY_TIME);

	}

	private void requestRefresh()
	{
		category.setRefresh(NewsCategory.IS_REFRESHING);
		String cid = category.getid();
		App.get().getService(IReportService.class).recordList(cid);
		rs.getList(cid, ds.getLatestReleaseTime(cid,category.isCache()), RemoteService.LATER_TIME);
	}


	private synchronized void initWebSettings(WebView wv) {
		WebSettings webSettings = wv.getSettings();
		webSettings.setDomStorageEnabled(true);


		/*
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);


		webSettings.setAppCacheEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		webSettings.setDatabaseEnabled(true);



		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webSettings.setLoadsImagesAutomatically(true);
		} else {
			webSettings.setLoadsImagesAutomatically(false);
		}*/
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setBlockNetworkImage(false);

		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	}



}
