package com.gmobi.poponews.cases.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.capricorn.ArcMenu;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.OutletNames;
import com.gmobi.poponews.cases.categorysetting.CategorySettingActivity;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.outlet.CategoryPlugProvider;
import com.gmobi.poponews.outlet.CategoryTabPlug;
import com.gmobi.poponews.outlet.MyTabPlug;
import com.gmobi.poponews.outlet.SlidePagerTabOutlet;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.RemoteService;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.PagerSlidingTabStrip;
import com.gmobi.poponews.widget.RefreshableRecycleView;
import com.gmobi.poponews.widget.RefreshableView;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.data.IDataMutableList;
import com.momock.event.EventArgs;
import com.momock.event.IEventHandler;
import com.momock.holder.FragmentHolder;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.outlet.IPlug;
import com.momock.outlet.card.ICardPlug;
import com.momock.service.IAsyncTaskService;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

public class HomeCase extends Case<Fragment> {
	public HomeCase(ICase<?> parent) {
		super(parent);
	}


	boolean isAttached = false;

	@Inject
	Resources resources;
	@Inject
	IDataService ds;
	@Inject
	IMessageService messageService;
	@Inject
	IRemoteService remoteService;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IConfigService configService;
	@Inject
	IAsyncTaskService asyncTaskService;


	private boolean layoutTypeChanged = false; //点击了layoutType扇形菜单，导致plug改变
	private SlidePagerTabOutlet newsTabsOutlet = new SlidePagerTabOutlet();
	private PagerSlidingTabStrip tabhost;
	private ViewPager pager;
	private View refreshableView;
	private ImageButton mBtnTop;

	private MyTabPlug newsPlug;


	@Override
	public void onCreate() {

		newsPlug = new MyTabPlug(
				R.drawable.news,
				R.drawable.news_sel,
				R.string.tabs_news,
				FragmentHolder.create(R.layout.fragment_news, this), OutletNames.PLUG_NEWS);

		getOutlet(OutletNames.MAIN_TABS).addPlug(newsPlug);
		addOutlet(OutletNames.NEWS_CONTAINER, newsTabsOutlet);


		newsTabsOutlet.setPlugProvider(new CategoryPlugProvider(getCase(CaseNames.MAIN), (IDataMutableList<NewsCategory>) ds.getAllVisibleCategories()));


		if (swicthPagehandler == null)
			swicthPagehandler = new IEventHandler<EventArgs>() {

				@Override
				public void process(Object sender, EventArgs args) {

					uiTaskService.runDelayed(new Runnable() {
						@Override
						public void run() {
							final CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getActivePlug();
							if (plug == null)
								return;

							final String cid = plug.getCategory().getid();
							MainActivity.curCid = cid;
							//reportService.recordList(cid);

							if (!layoutTypeChanged) {
								AnalysisUtil.recordNewsList(cid, plug.getCategory().getname());
							} else
								layoutTypeChanged = false;


							if (isAttached()) {
								ImageButton btn = ViewHolder.get(getAttachedObject(), R.id.btn_top).getView();
								if (btn != null)
									btn.setVisibility(View.GONE);
								UiHelper.stopBtnTopTimer();

								ArcMenu am = ViewHolder.get(getAttachedObject(), R.id.arc_menu).getView();
								if (am != null) {
									am.closeMenu();
									int type = plug.getCategory().getNewsLayoutType();
									if (type > 0 && type <= NewsCategory.LAYOUT_PIN2)
										am.setHintViewImage(arcImage[type - 1]);
								}
							}

							final NewsCategory ctg = plug.getCategory();
							if (ctg.getCategoryType().equals(NewsCategory.TYPE_NEWS)) {
								if (!ds.isListReady(cid)) {

									if (ctg.isCache()) {
										ds.setNewsList(cid, null, true, false);


									}

									final Timer fetchTimer = new Timer();

									fetchTimer.schedule(new TimerTask() {
										@Override
										public void run() {
											remoteService.getList(cid, ctg.isCache() ? 0 :ds.getLatestReleaseTime(cid, plug.getCategory().isCache()), RemoteService.EARLY_TIME);
											doAdAction(cid);
											fetchTimer.cancel();

										}
									}, 0, 1000);

								}
							}
						}
					},300);






				}
			};


//		messageService.addHandler(MessageTopics.CACHE_INIT_FINISH, new IMessageHandler() {
//			@Override
//			public void process(Object o, Message message) {
//				final CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getActivePlug();
//				if (plug == null)
//					return;
//
//				final NewsCategory ctg = plug.getCategory();
//
//				if (!ds.isListReady(ctg.getid())
//						&& ctg.isCache()
//						&& ctg.getCategoryType().equals(NewsCategory.TYPE_NEWS)) {
//
//					uiTaskService.run(new Runnable() {
//						@Override
//						public void run() {
//							ds.setNewsList(ctg.getid(), null, true, false);
//						}
//					});
//				}
//
//			}
//		});


		messageService.addHandler(MessageTopics.CLEAR_TOP_BTN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (!isAttached())
					return;
				uiTaskService.run(new Runnable() {
					@Override
					public void run() {
						if (getAttachedObject() == null)
							return;
						final ImageButton btn = ViewHolder.get(getAttachedObject(), R.id.btn_top).getView();
						if (btn != null) {
							startAnimation(btn, R.anim.btn_disappear, new Animation.AnimationListener() {
								@Override
								public void onAnimationStart(Animation animation) {

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									btn.setVisibility(View.GONE);
								}

								@Override
								public void onAnimationRepeat(Animation animation) {

								}
							});

						}

					}
				});
			}
		});
		messageService.addHandler(MessageTopics.CLOSE_ARC_MENU, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (!isAttached())
					return;
				uiTaskService.run(new Runnable() {
					@Override
					public void run() {
						if (getAttachedObject() == null)
							return;
						ArcMenu am = ViewHolder.get(getAttachedObject(), R.id.arc_menu).getView();
						if (am != null)
							am.closeMenu();
					}
				});
			}
		});


		messageService.addHandler(MessageTopics.CATEGORY_CHANGED,
				new IMessageHandler() {
					@Override
					public void process(Object sender, Message msg) {
						if (!isAttached())
							return;

						CaseActivity target = (CaseActivity) getCase(CaseNames.MAIN).getAttachedObject();
						if (target == null)
							return;

						CategoryPlugProvider p = (CategoryPlugProvider) newsTabsOutlet.getPlugProvider();
						p.refreshPlugs();

						tabhost = ViewHolder.get(target, R.id.slidetabs).getView();
						pager = ViewHolder.get(target, R.id.realtabcontent).getView();
						if (pager != null) {
							newsTabsOutlet.attach(pager, (DataList<NewsCategory>) ds.getAllVisibleCategories());
							pager.getAdapter().notifyDataSetChanged();
							tabhost.setViewPager(pager);
						}

						CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getPlugs().getItem(0);
						newsTabsOutlet.setActivePlug(plug);



					}
				});


		messageService.addHandler(MessageTopics.CATEGORY_LOADED,
				new IMessageHandler() {
					@Override
					public void process(Object sender, Message msg) {
						if (!isAttached())
							return;

						CaseActivity target = (CaseActivity) getCase(CaseNames.MAIN).getAttachedObject();
						if (target == null)
							return;

						tabhost = ViewHolder.get(target, R.id.slidetabs).getView();
						pager = ViewHolder.get(target, R.id.realtabcontent).getView();
						if (pager != null) {
							newsTabsOutlet.attach(pager, (DataList<NewsCategory>) ds.getAllVisibleCategories());
							tabhost.setViewPager(pager);


							if (MainActivity.curCid.equals("")) {
								CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getPlugs().getItem(0);
								newsTabsOutlet.setActivePlug(plug);
							} else {
								boolean found = false;
								for (int i = 0; i < newsTabsOutlet.getPlugs().getItemCount(); i++) {
									CategoryTabPlug ctp = (CategoryTabPlug) newsTabsOutlet.getPlugs().getItem(i);
									if (MainActivity.curCid.equals(ctp.getCategory().getid())) {
										layoutTypeChanged = true;
										newsTabsOutlet.setActivePlug(ctp);
										found = true;
										break;
									}
								}
								if (!found) {
									CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getPlugs().getItem(0);
									newsTabsOutlet.setActivePlug(plug);
								}
							}

						}


					}
				});

		messageService.addHandler(MessageTopics.AD_READY, new IMessageHandler() {
					@Override
					public void process(Object o, Message message) {

					}
				}
		);

		messageService.addHandler(MessageTopics.NEWS_LOADED,
				new IMessageHandler() {
					@Override
					public void process(Object sender, Message msg) {
						@SuppressWarnings("unchecked")
						HashMap<String, Object> m = (HashMap<String, Object>) msg.getData();


						final String cid = (String) m.get("cid");

						Logger.error("Got MSG MessageTopics.NEWS_LOADED : cid=" + cid);

						if (cid.equals(""))
							return;


						//CaseActivity target = (CaseActivity) getCase(CaseNames.MAIN).getAttachedObject();

						if (!isAttached())
							return;

						if (ds.getCategoryById(cid).isRefresh() != NewsCategory.NOT_REFRESHING) {
							uiTaskService.runDelayed(new Runnable() {
								public void run() {
									Logger.debug("uiTask finishRefreshing");
									IDataList<IPlug> plugs = newsTabsOutlet.getPlugs();
									for (int i = 0; i < plugs.getItemCount(); i++) {
										CategoryTabPlug ctp = (CategoryTabPlug) plugs.getItem(i);
										if (cid.equals(ctp.getCategory().getid())) {
											ViewHolder parent = (ViewHolder) ctp.getComponent();

											if (getAttachedObject() != null && parent != null && parent.getView() != null) {
												refreshableView = ViewHolder.get(parent.getView(), R.id.refreshable_view).getView();
												if (refreshableView instanceof RefreshableView)
													((RefreshableView) refreshableView).finishRefreshing();
												else if (refreshableView instanceof RefreshableRecycleView)
													((RefreshableRecycleView) refreshableView).finishRefreshing();
												else if (refreshableView instanceof BGARefreshLayout) {
													if (ds.getCategoryById(cid).isRefresh() == NewsCategory.IS_LOADINGMORE)
														((BGARefreshLayout) refreshableView).endLoadingMore();
													else
														((BGARefreshLayout) refreshableView).endRefreshing();
												}
											}
											ds.getCategoryById(cid).setRefresh(NewsCategory.NOT_REFRESHING);
											break;
										}
									}

								}
							}, 2000);
						}

					}
				});
	}

	IEventHandler<EventArgs> swicthPagehandler = null;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onAttach(final Fragment target) {
		UiHelper.setMainAlive(true);
		isAttached = true;

		if (!UiHelper.isFetchedData()) {
			DataList<NewsCategory> ctgList = (DataList<NewsCategory>) ds.getAllVisibleCategories();
			for (int i = 0; i < ctgList.getItemCount(); i++) {
				String cid = ctgList.getItem(i).getid();
				//remoteService.getList(cid, ds.getLatestReleaseTime(cid,ctgList.getItem(i).isCache()), RemoteService.EARLY_TIME);
				doAdAction(cid);
			}

			UiHelper.setFetchedData(true);
		}


		DrawerLayout mDrawerLayout = ViewHolder.get(target, R.id.drawer_layout).getView();
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);



		CategoryPlugProvider p = (CategoryPlugProvider) newsTabsOutlet.getPlugProvider();
		p.refreshPlugs();

		tabhost = ViewHolder.get(target, R.id.slidetabs).getView();
		pager = ViewHolder.get(target, R.id.realtabcontent).getView();
		if (pager != null) {
			newsTabsOutlet.attach(pager, (DataList<NewsCategory>) ds.getAllVisibleCategories());
			pager.getAdapter().notifyDataSetChanged();
			tabhost.setViewPager(pager);
		}

		if (!newsTabsOutlet.getActivePlugChangedEvent().hasEventHandler(swicthPagehandler))
			newsTabsOutlet.getActivePlugChangedEvent().addEventHandler(swicthPagehandler);


		if (!MainActivity.curCid.equals("")) {
			for (int i = 0; i < newsTabsOutlet.getPlugs().getItemCount(); i++) {
				CategoryTabPlug ctp = (CategoryTabPlug) newsTabsOutlet.getPlugs().getItem(i);
				if (MainActivity.curCid.equals(ctp.getCategory().getid())) {
					layoutTypeChanged = true;
					newsTabsOutlet.setActivePlug(ctp);
					break;
				}
			}
		}


		initArcMenu(target);


		tabhost.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pos) {
				ICardPlug plug = (ICardPlug) newsTabsOutlet.getPlugs().getItem(pos);
				newsTabsOutlet.setActivePlug(plug);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				Log.e("pager", "onPageScrolled");
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				Log.e("pager", "onPageScrollStateChanged");
			}
		});


		CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getActivePlug();
		if (plug != null && plug.getBinder() != null) {
			plug.getBinder().getAdapter().notifyDataSetChanged();
		}


		Intent in = getAttachedObject().getActivity().getIntent();
		if (in == null || in.getIntExtra(IntentNames.INTENT_EXTRA_FROM, UiHelper.FROM_LAUCHER) == UiHelper.FROM_LAUCHER
				|| in.getIntExtra(IntentNames.INTENT_EXTRA_FROM, UiHelper.FROM_LAUCHER) == UiHelper.FROM_PUSH) {
			if (ds.isCtgReady()) {
				messageService.send(this, MessageTopics.CATEGORY_LOADED);
			} else {
				Logger.error("Category is not ready!!! So wait!!!");

				Context ctx = App.get().getBaseContext();
				Intent i = ctx.getPackageManager()
						.getLaunchIntentForPackage(ctx.getPackageName());
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

				UiHelper.stopAllTimer();
				App.get().startActivity(i);
/*
				final Timer readyTimer = new Timer();

				readyTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (ds.isCtgReady()) {
							messageService.send(this, MessageTopics.CATEGORY_LOADED);
							readyTimer.cancel();
						}
					}
				}, 0, 2000);*/

			}

		}

		View ib_ctg_manager = ViewHolder.get(getAttachedObject(), R.id.main_action_bar_ctg).getView();
		ib_ctg_manager.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AnalysisUtil.recordCategorySetting();

				Intent editionIntent = new Intent(target.getActivity(), CategorySettingActivity.class);
				editionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				target.getActivity().startActivity(editionIntent);
			}
		});


		mBtnTop = ViewHolder.get(getAttachedObject(), R.id.btn_top).getView();
		if (mBtnTop != null)
			mBtnTop.setVisibility(View.GONE);

		mBtnTop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getActivePlug();
				AnalysisUtil.recordNewsListTop(plug.getCategory().getid(), plug.getCategory().getname());



				ListView list = ViewHolder.get(((ViewHolder)plug.getComponent()).getView(), R.id.plv_newslist).getView();
				list.smoothScrollToPosition(0);
//				final Handler handler = new Handler();
//				handler.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						list.smoothScrollToPosition(0);
//					}}, 100);


			}
		});

		AnalysisUtil.onActivityResume(getAttachedObject().getActivity(),AnalysisUtil.SCR_NEWS);
		Log.e("activity", "onAttach");
	}


	@Override
	public void onActivate() {
		Log.e("activity", "onActivate");


	}

	@Override
	public void run(Object... args) {
		getOutlet(OutletNames.MAIN_TABS).setActivePlug(newsPlug);
	}


	@Override
	public void onShow() {
		Log.e("activity", "onShow");
		CategoryTabPlug plug = (CategoryTabPlug) newsTabsOutlet.getActivePlug();
		if (plug != null) {
			if (plug.getBinder() != null)
				plug.getBinder().getAdapter().notifyDataSetChanged();
		}


		NightModeUtil.setActionBarColor(getAttachedObject().getActivity(), R.id.rl_main_action_bar);


		NightModeUtil.setViewColor(getAttachedObject().getActivity(), R.id.main_action_bar_title,
				resources.getColor(R.color.actionbar_text_color),
				resources.getColor(R.color.actionbar_text_color_night));

		NightModeUtil.setViewColor(getAttachedObject().getActivity(), R.id.slidetabs,
				resources.getColor(R.color.tab_bg_color),
				resources.getColor(R.color.tab_bg_color_night));

		NightModeUtil.setViewColor(getAttachedObject().getActivity(), R.id.rl_loading,
				resources.getColor(R.color.bg_white),
				resources.getColor(R.color.bg_white_night));


		if (tabhost != null) {
			tabhost.setIndicatorColorResource(
					NightModeUtil.isNightMode() ? R.color.tab_indicator_color_night : R.color.tab_indicator_color);
			tabhost.setTextColorResource(
					NightModeUtil.isNightMode() ? R.color.tab_title_color_night : R.color.tab_title_color);
		}


		super.onShow();
	}


	@Override
	public void onHide() {
		if (getAttachedObject() != null) {
			DrawerLayout dl = ViewHolder.get(getAttachedObject(), R.id.drawer_layout).getView();
			dl.closeDrawers();

			ArcMenu am = ViewHolder.get(getAttachedObject(), R.id.arc_menu).getView();
			if (am == null)
				return;

			am.closeMenu();
		}

		super.onHide();
	}


//	public void setSlidingMenuAction(int gravity) {
//		if (!mDrawerLayout.isDrawerOpen(gravity)) {
//			mDrawerLayout.closeDrawers();
//			mDrawerLayout.openDrawer(gravity);
//		} else {
//			mDrawerLayout.closeDrawers();
//		}
//	}

	private int[] arcImage = {
			R.drawable.layout_list_selector,
			R.drawable.layout_grid_selector,
			R.drawable.layout_pin1_selector,
			R.drawable.layout_pin2_selector
	};
	private String[] arcLayoutStr = {"list", "grid", "pin", "pin2"};
	private int baseMenuId = 1000;


	private void initArcMenu(final Fragment target) {
		final ArcMenu am = ViewHolder.get(target, R.id.arc_menu).getView();

		if (am == null)
			return;
		am.setVisibility(View.GONE);

		View.OnClickListener layoutMenuListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(target.getActivity(), "position:"+(v.getId() - baseMenuId), Toast.LENGTH_SHORT).show();
				am.setHintViewImage(arcImage[v.getId() - baseMenuId]);

				CategoryTabPlug curPlug = (CategoryTabPlug) newsTabsOutlet.getActivePlug();
				if (curPlug == null)
					return;

				String curCid = curPlug.getCategory().getid();

				curPlug.getCategory().setLayoutType(arcLayoutStr[v.getId() - baseMenuId]);
				configService.setCategoryLayoutType(curCid, v.getId() - baseMenuId + 1);

				AnalysisUtil.recordNewsListLayout(curPlug.getCategory().getid(), curPlug.getCategory().getname(),
						arcLayoutStr[v.getId() - baseMenuId]);


				((ViewHolder) curPlug.getComponent()).reset();
				CategoryPlugProvider p = (CategoryPlugProvider) newsTabsOutlet.getPlugProvider();
				p.refreshPlugs();
				tabhost = ViewHolder.get(target, R.id.slidetabs).getView();
				pager = ViewHolder.get(target, R.id.realtabcontent).getView();
				newsTabsOutlet.attach(pager, (DataList<NewsCategory>) ds.getAllVisibleCategories());
				pager.getAdapter().notifyDataSetChanged();
				tabhost.setViewPager(pager);

				layoutTypeChanged = true;
				for (int i = 0; i < p.getPlugs().getItemCount(); i++) {
					CategoryTabPlug ctp = (CategoryTabPlug) p.getPlugs().getItem(i);
					if (ctp.getCategory().getid().equals(curCid)) {
						newsTabsOutlet.setActivePlug(ctp);
						break;
					}
				}


			}
		};


		am.setControlTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				mBtnTop.setVisibility(View.INVISIBLE);
				UiHelper.stopBtnTopTimer();
				return false;
			}
		});

		for (int i = 0; i < arcImage.length; i++) {
			ImageView iv = new ImageView(target.getActivity());
			iv.setId(baseMenuId + i);
			iv.setImageResource(arcImage[i]);
			iv.setVisibility(View.INVISIBLE);
			am.addItem(iv, layoutMenuListener);
		}


	}


	private void doAdAction(String cid) {
		if (configService.isNativeAdEnable()) {
			Logger.debug("show native ad");
			if (UiHelper.needShow(configService.getNativeFacebookAdPercent())) {
				Logger.debug("is facebook ad");
				if (configService.getNativeAdCount() > 0) {

					ds.getCategoryById(cid).setAdSrc(AdHelper.NATIVE_AD_SRC_FACEBOOK);


				}
			} else {
				Logger.debug("is gmobi ad");
				if (configService.getNativeAdCount() > 0) {

					ds.getCategoryById(cid).setAdSrc(AdHelper.NATIVE_AD_SRC_GMOBI);


				}
			}
		}
	}


	private void startAnimation(View v, int resAnim, Animation.AnimationListener al) {
		if (getAttachedObject() != null) {
			Animation anim = AnimationUtils.loadAnimation(getAttachedObject().getActivity(), resAnim);
			anim.reset();
			if (null != al) {
				anim.setAnimationListener(al);
			}

			v.setVisibility(View.VISIBLE);
			v.clearAnimation();
			v.startAnimation(anim);
		}
	}

}





