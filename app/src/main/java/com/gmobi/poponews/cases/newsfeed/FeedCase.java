package com.gmobi.poponews.cases.newsfeed;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.view.View;

import com.gmobi.poponews.R;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.outlet.NewsItemBinderFactory;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.INewsCacheService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.event.EventArgs;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.ViewHolder;
import com.momock.service.ICacheService;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IRService;
import com.momock.service.ISystemService;
import com.momock.service.IUITaskService;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class FeedCase extends Case<CaseActivity> {

	public FeedCase(String name) {
		super(name);
	}

	public FeedCase(ICase<?> parent) {
		super(parent);
	}

	@Inject
	Resources resources;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IImageService imageService;
	@Inject
	ISystemService systemService;
	@Inject
	IMessageService messageService;
	@Inject
	IDataService dataService;
	@Inject
	IConfigService configService;
	@Inject
	IRService rService;
	@Inject
	NotificationManager notifier;
	@Inject
	Resources res;
	@Inject
	ICacheService cacheService;
	@Inject
	IRemoteService remoteService;
	@Inject
	INewsCacheService newsCacheService;


	ComposedItemBinder newsBinder;
	ListViewBinder lvBinder;

	@Override
	public void onCreate() {

		newsBinder = new NewsItemBinderFactory(getApplication().getCurrentContext(), NewsItemBinderFactory.TYPE_PUSH).setFeatureSupport(false).build();
		lvBinder = new ListViewBinder(newsBinder);

		lvBinder.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
			@Override
			public void process(Object sender, ItemEventArgs args) {

				NewsItem i = (NewsItem) args.getItem();
				dataService.setCurNid(i.get_id());
				if (!i.getGo2Src())
					UiHelper.openArticleFromApp(getAttachedObject(), i.get_id());
				else
					UiHelper.openBrowserActivity(getAttachedObject(), i.get_id(), i.getType(), i.getSource(), i.getTitle(), i.getPdomain(), "");

				if (!i.getType().equals(NewsItem.NEWS_TYPE_IMAGE))
					remoteService.getBodyContent(i.get_id(), i.getBody());

			}

		});
	}

	@Override
	public void run(Object... args) {
		App.get().startActivity(FeedActivity.class);
	}


	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);
		final View empty = ViewHolder.get(getAttachedObject(), R.id.rl_feed_empty).getView();
		if (dataService.getPushList().getItemCount() == 0)
			empty.setVisibility(View.VISIBLE);
		else
			empty.setVisibility(View.GONE);

		lvBinder.bind(ViewHolder.get(refTarget.get(), R.id.lv_feedlist), dataService.getPushList());


		lvBinder.getDataChangedEvent().addEventHandler(new IEventHandler<EventArgs>() {

			@Override
			public void process(Object sender, EventArgs args) {
				View empty = ViewHolder.get(getAttachedObject(), R.id.rl_feed_empty).getView();

				if (dataService.getPushList().getItemCount() != 0 && empty != null) {
					empty.setVisibility(View.GONE);
				}


			}
		});


		View ivBackBtn = ViewHolder.get(target, R.id.feed_back).getView();

		ivBackBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Activity currActivity = App.get().getCurrentActivity();
				if (currActivity != null)
					currActivity.finish();

			}
		});


	}


	@Override
	public void onShow() {
		UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));


		NightModeUtil.setViewColor(getAttachedObject(), R.id.rl_feed_empty,
				resources.getColor(R.color.bg_white), resources.getColor(R.color.bg_black_night));

		NightModeUtil.setViewColor(getAttachedObject(), R.id.feed_empty,
				resources.getColor(R.color.bg_black), resources.getColor(R.color.bg_white_night));

		NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_feed_action_bar);

		lvBinder.getAdapter().notifyDataSetChanged();
	}

}





