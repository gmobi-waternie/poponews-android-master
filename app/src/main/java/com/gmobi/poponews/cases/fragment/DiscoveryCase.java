package com.gmobi.poponews.cases.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.OutletNames;
import com.gmobi.poponews.cases.socialsetting.SocialSettingActivity;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.outlet.MyTabPlug;
import com.gmobi.poponews.outlet.SocialBinderFactory;
import com.gmobi.poponews.service.IBaiduService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.IGoogleService;
import com.gmobi.poponews.service.ISocialDataService;
import com.gmobi.poponews.service.ISocialService;
import com.gmobi.poponews.service.ITwitterService;
import com.gmobi.poponews.service.IWeiboService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.ICase;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.FragmentHolder;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;

import javax.inject.Inject;


public class DiscoveryCase extends Case<Fragment> {

	public DiscoveryCase(ICase<?> parent) {
		super(parent);
	}

	@Inject
	Resources resources;
	@Inject
	IMessageService messageService;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IFacebookService facebookService;
	@Inject
	ITwitterService twitterService;
	@Inject
	IGoogleService googleService;
	@Inject
	IWeiboService weiboService;
	@Inject
	IBaiduService baiduService;
	@Inject
	ISocialDataService socialDataService;

	private MyTabPlug discoveryPlug;
	private View moreView;


	@Override
	public void onCreate() {

		discoveryPlug = new MyTabPlug(R.drawable.discover,
				R.drawable.discover_sel,
				R.string.tabs_discovery,
				FragmentHolder.create(R.layout.news_social, this), OutletNames.PLUG_DISCOVERY);

		getOutlet(OutletNames.MAIN_TABS).addPlug(discoveryPlug);


		messageService.addHandler(MessageTopics.GET_FACEBOOK_LIKES_MAIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				Logger.debug("Handle GET_FACEBOOK_LIKES_MAIN Message");
				int ret = (Integer) message.getData();
				if (ret > 0) {
					facebookService.remoteGetAllAccountPosts(false);
				}
			}
		});

		messageService.addHandler(MessageTopics.GET_TWITTER_CHANNEL_MAIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				Logger.debug("Handle GET_TWITTER_CHANNEL_MAIN Message");
				int ret = (Integer) message.getData();
				if (ret > 0) {
					twitterService.remoteGetAllAccountPosts(false);
				}
			}
		});

		messageService.addHandler(MessageTopics.GET_WEIBO_CHANNEL_MAIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				Logger.debug("Handle GET_WEIBO_CHANNEL_MAIN Message");
				int ret = (Integer) message.getData();
				if (ret > 0) {
					weiboService.remoteGetAllAccountPosts(false);
				}
			}
		});

		messageService.addHandler(MessageTopics.GET_GOOGLE_CHANNEL_MAIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				googleService.remoteGetAccountPosts(null, null);
			}
		});

		messageService.addHandler(MessageTopics.GET_BAIDU_CHANNEL_MAIN, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				baiduService.remoteGetAccountPosts(null, null);
			}
		});

		IMessageHandler getPartDataHandler = new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {

				uiTaskService.run(new Runnable() {

					@Override
					public void run() {
						if (moreView != null) {
							AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DipHelper.dip2px(1));
							moreView.setLayoutParams(lp);
							moreView.setVisibility(View.GONE);
						}

					}
				});
				Logger.error("Recv GET_PART_OF_POST MSG");
				UiHelper.stopFetchTimer();
			}
		};

		IMessageHandler dataReadyHandler = new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {

				if (isAllReady()) {
					if (isAttached()) {
						View more = ViewHolder.get(getAttachedObject(), R.id.discover_more_view).getView();
						if (more != null) {
							AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DipHelper.dip2px(1));
							more.setLayoutParams(lp);
							more.setVisibility(View.GONE);
						}

					}


					UiHelper.stopFetchTimer();
					facebookService.setUpdateStatus(ISocialService.NOT_UPDATE);
					googleService.setUpdateStatus(ISocialService.NOT_UPDATE);
					twitterService.setUpdateStatus(ISocialService.NOT_UPDATE);
					weiboService.setUpdateStatus(ISocialService.NOT_UPDATE);
					baiduService.setUpdateStatus(ISocialService.NOT_UPDATE);
				}
			}
		};
		messageService.addHandler(MessageTopics.GET_GOOGLE_POST, dataReadyHandler);
		messageService.addHandler(MessageTopics.GET_FACEBOOK_POST, dataReadyHandler);
		messageService.addHandler(MessageTopics.GET_TWITTER_POST, dataReadyHandler);
		messageService.addHandler(MessageTopics.GET_BAIDU_POST, dataReadyHandler);
		messageService.addHandler(MessageTopics.GET_WEIBO_POST, dataReadyHandler);

		messageService.addHandler(MessageTopics.GET_PART_OF_POST, getPartDataHandler);

		messageService.addHandler(MessageTopics.CLEAR_REFRESH_UI, new IMessageHandler() {
					@Override
					public void process(Object o, Message message) {
						uiTaskService.runDelayed(new Runnable() {

							@Override
							public void run() {
								if (moreView != null) {
									AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DipHelper.dip2px(1));
									moreView.setLayoutParams(lp);
									moreView.setVisibility(View.GONE);
								}

							}
						}, 100);


						Logger.error("Recv CLEAR_REFRESH_UI MSG");
						UiHelper.stopFetchTimer();
					}
				}

		);


	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onAttach(Fragment target) {
		UiHelper.setMainAlive(true);

		TypedValue.applyDimension(0, 0, null);

		initialSocialButton(target.getView(), R.id.facebook_login_button, R.drawable.facebook_1, R.drawable.facebook_2, facebookService, SocialExtra.SOCIAL_TYPE_FACEBOOK);
		initialSocialButton(target.getView(), R.id.twitter_login_button, R.drawable.twitter_1, R.drawable.twitter_2, twitterService, SocialExtra.SOCIAL_TYPE_TWITTER);
		initialSocialButton(target.getView(), R.id.google_login_button, R.drawable.google_1, R.drawable.google_2, googleService, SocialExtra.SOCIAL_TYPE_GOOGLE);
		initialSocialButton(target.getView(), R.id.baidu_login_button, R.drawable.baidu_1, R.drawable.baidu_2, baiduService, SocialExtra.SOCIAL_TYPE_BAIDU);
		initialSocialButton(target.getView(), R.id.weibo_login_button, R.drawable.weibo_1, R.drawable.weibo_2, weiboService, SocialExtra.SOCIAL_TYPE_WEIBO);

		ComposedItemBinder socialBinder = new SocialBinderFactory().build();

		ListViewBinder lvb = new ListViewBinder(socialBinder);
		lvb.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
			@Override
			public void process(Object sender, ItemEventArgs args) {
				SocialPost fp = (SocialPost) args.getItem();

				AnalysisUtil.recordSnsRead(fp.getSocialtype(), fp.getLink(), fp.getName());
				UiHelper.openBrowserActivity(App.get().getCurrentActivity(),
						fp.getId(),fp.getType(), fp.getLink(), fp.getName(),"social", fp.getSocialtype());


			}

		});


		ListView newsListview = ViewHolder.get(getAttachedObject().getView(), R.id.lv_sociallist).getView();

		createFooterView();

		newsListview.addFooterView(moreView);

		ImageView iv = ViewHolder.get(moreView, R.id.iv_loading).getView();
		iv.setBackgroundResource(R.drawable.loading_anim);
		AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
		loadAnim.setOneShot(false);
		loadAnim.start();

		newsListview.setOnScrollListener(new AbsListView.OnScrollListener() {
			int lastItem;
			int count;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (lastItem == count && scrollState == SCROLL_STATE_IDLE) {
					Log.e("fresh", "拉到最底部");
					AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DipHelper.dip2px(81));
					moreView.setLayoutParams(lp);
					moreView.setVisibility(View.VISIBLE);
					moreView.setClickable(false);
					UiHelper.startFetchTimer();
					facebookService.remoteGetNextPage();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				lastItem = firstVisibleItem + visibleItemCount - 1;
				count = totalItemCount - 1;
			}
		});


		ViewHolder vh = ViewHolder.get(target.getView(), R.id.lv_sociallist);
		lvb.bind(vh, socialDataService.getPostList());


					/*如果已经登录，并且setting里也打开的情况下，主动开始获取*/
		if (facebookService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
			facebookService.remoteGetAccountList(ISocialService.FROM_MAIN);
		}

		if (twitterService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_TWITTER)) {
			twitterService.remoteGetAccountList(ISocialService.FROM_MAIN);
		}

		if (googleService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
			googleService.remoteGetAccountList(ISocialService.FROM_MAIN);
		}

		if (weiboService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_WEIBO)) {
			weiboService.remoteGetAccountList(ISocialService.FROM_MAIN);
		}

		if (baiduService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_BAIDU)) {
			baiduService.remoteGetAccountList(ISocialService.FROM_MAIN);
		}
		AnalysisUtil.onActivityResume(getAttachedObject().getActivity(),AnalysisUtil.SCR_DISCOVER);
		AnalysisUtil.recordSns();
		Log.e("activity", "onAttach");
	}


	@Override
	public void onActivate() {
		Log.e("activity", "onActivate");

	}

	@Override
	public void run(Object... args) {
		getOutlet(OutletNames.MAIN_TABS).setActivePlug(discoveryPlug);
	}


	@Override
	public void onShow() {
		Log.e("activity", "onShow");

		initSocialButtonStatus();

		NightModeUtil.setActionBarColor(getAttachedObject().getActivity(), R.id.rl_main_action_bar);


		NightModeUtil.setViewColor(getAttachedObject().getActivity(), R.id.main_action_bar_title,
				resources.getColor(R.color.actionbar_text_color),
				resources.getColor(R.color.actionbar_text_color_night));


		ListView newsListview = ViewHolder.get(getAttachedObject().getView(), R.id.lv_sociallist).getView();

		if (!SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK) && !SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_TWITTER) && !SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE) &&
				!SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_WEIBO) && !SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_BAIDU)) {
			newsListview.setVisibility(View.INVISIBLE);
		} else {
			newsListview.setVisibility(View.VISIBLE);
		}


		super.onShow();
	}


	private void initSocialButtonStatus() {

		ImageButton ib1 = ViewHolder.get(getAttachedObject(), R.id.facebook_login_button).getView();
		if (facebookService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK))
			ib1.setImageResource(R.drawable.facebook_1);
		else
			ib1.setImageResource(R.drawable.facebook_2);


		ImageButton ib2 = ViewHolder.get(getAttachedObject(), R.id.twitter_login_button).getView();
		if (twitterService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_TWITTER))
			ib2.setImageResource(R.drawable.twitter_1);
		else
			ib2.setImageResource(R.drawable.twitter_2);


		ImageButton ib3 = ViewHolder.get(getAttachedObject(), R.id.google_login_button).getView();
		if (googleService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE))
			ib3.setImageResource(R.drawable.google_1);
		else
			ib3.setImageResource(R.drawable.google_2);

		ImageButton ib4 = ViewHolder.get(getAttachedObject(), R.id.baidu_login_button).getView();
		if (baiduService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_BAIDU))
			ib4.setImageResource(R.drawable.baidu_1);
		else
			ib4.setImageResource(R.drawable.baidu_2);

		ImageButton ib5 = ViewHolder.get(getAttachedObject(), R.id.weibo_login_button).getView();
		if (weiboService.isLogged() && SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_WEIBO))
			ib5.setImageResource(R.drawable.weibo_1);
		else
			ib5.setImageResource(R.drawable.weibo_2);

	}


	private void initialSocialButton(View viewgroup, int buttonId, int onIcon, int offIcon,
									 ISocialService socialService, final String type) {
		ImageButton ib = ViewHolder.get(viewgroup, buttonId).getView();
		if (!socialService.isEnabled()) {
			ib.setVisibility(View.GONE);
			return;
		}

		if (socialService.isLogged() && SocialSetting.getStatus(type))
			ib.setImageResource(onIcon);
		else
			ib.setImageResource(offIcon);

		ib.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AnalysisUtil.recordSnsClick(type);

				Intent intent = new Intent(App.get().getCurrentActivity(), SocialSettingActivity.class);
				intent.putExtra("social_type", type);
				App.get().getCurrentActivity().startActivity(intent);


			}
		});
	}


	private void createFooterView() {
		if (moreView != null)
			return;

		boolean nightmode = NightModeUtil.isNightMode();
		moreView = getAttachedObject().getActivity().getLayoutInflater().inflate(
				nightmode ? R.layout.footer_fresh_item_night : R.layout.footer_fresh_item,
				null);
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, DipHelper.dip2px(81));
		moreView.setLayoutParams(lp);
		moreView.setVisibility(View.INVISIBLE);
		moreView.setId(R.id.discover_more_view);
		moreView.setClickable(false);

	}

/*
	private void requestRefresh() {
		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
			facebookService.setUpdateStatus(ISocialService.IS_REFRESHING);
			facebookService.remoteGetAllAccountPosts(false);
		}
		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
			googleService.setUpdateStatus(ISocialService.IS_REFRESHING);
			googleService.remoteGetAccountPosts(null, null);
		}
	}

	private void requestLoadMore() {
		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
			facebookService.setUpdateStatus(ISocialService.IS_LOADINGMORE);
			facebookService.remoteGetNextPage();
		}
		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
			googleService.setUpdateStatus(ISocialService.IS_LOADINGMORE);
			googleService.remoteGetAccountPosts(null, null);
		}
	}
*/

	private boolean isAllReady() {
		boolean allIsReady = true;
		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
			if (!socialDataService.isAllAccountsFinishUpdating(SocialExtra.SOCIAL_TYPE_FACEBOOK))
				allIsReady = false;
		}

		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_TWITTER)) {
			if (!socialDataService.isAllAccountsFinishUpdating(SocialExtra.SOCIAL_TYPE_TWITTER))
				allIsReady = false;
		}

		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
			if (!socialDataService.isAllAccountsFinishUpdating(SocialExtra.SOCIAL_TYPE_GOOGLE))
				allIsReady = false;
		}

		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_BAIDU)) {
			if (!socialDataService.isAllAccountsFinishUpdating(SocialExtra.SOCIAL_TYPE_BAIDU))
				allIsReady = false;
		}

		if (SocialSetting.getStatus(SocialExtra.SOCIAL_TYPE_WEIBO)) {
			if (!socialDataService.isAllAccountsFinishUpdating(SocialExtra.SOCIAL_TYPE_WEIBO))
				allIsReady = false;
		}

		return allIsReady;
	}

}






