package com.gmobi.poponews.cases.main;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.OutletNames;
import com.gmobi.poponews.cases.fragment.DiscoveryCase;
import com.gmobi.poponews.cases.fragment.HomeCase;
import com.gmobi.poponews.cases.fragment.MeCase;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.outlet.MyTabPlug;
import com.gmobi.poponews.outlet.SlidePagerTabOutlet;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.IGoogleService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.service.IUpdateService;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.PagerSlidingTabStrip;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder.Setter;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.event.EventArgs;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.FragmentTabHolder;
import com.momock.holder.ViewHolder;
import com.momock.outlet.tab.FragmentTabOutlet;
import com.momock.outlet.tab.ITabPlug;
import com.momock.service.IAsyncTaskService;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class MainCase extends Case<CaseActivity> {
	public final static int MAIN_TAB_SHOW_COUNT = 3;


	public MainCase(String name) {
		super(name);
	}

	boolean isAttached = false;

	@Inject
	Resources resources;
	@Inject
	IDataService ds;
	@Inject
	IImageService imageService;
	@Inject
	IMessageService messageService;
	@Inject
	IRemoteService remoteService;
	@Inject
	IAsyncTaskService asyncTaskService;
	@Inject
	IUITaskService uiTaskService;
	@Inject
	IConfigService configService;
	@Inject
	IUpdateService updateService;
	@Inject
	IReportService reportService;
	@Inject
	IFacebookService facebookService;
	@Inject
	IGoogleService googleService;

	FragmentTabOutlet mainTabsOutlet = new FragmentTabOutlet();




	public static TwitterLoginButton twitterLoginButton;



	@Override
	public void onCreate() {
		addOutlet(OutletNames.MAIN_TABS, mainTabsOutlet);
		addCase(new HomeCase(this));
		addCase(new DiscoveryCase(this));
		addCase(new MeCase(this));

	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onAttach(CaseActivity target) {

		UiHelper.setMainAlive(true);
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);


		TypedValue.applyDimension(0, 0, null);
		//ShowLoading(target,true);

		FragmentTabHolder holder = FragmentTabHolder.get(target, R.id.content_frame);

		holder.setOnCreateTabIndicatorHandler(new FragmentTabHolder.OnCreateTabIndicatorHandler() {
			@Override
			public View onCreateTabIndicator(ITabPlug iTabPlug) {
				MyTabPlug plug = (MyTabPlug) iTabPlug;

				StateListDrawable sd = new StateListDrawable();
				sd.addState(new int[]{android.R.attr.state_selected}, resources.getDrawable(plug.getImageSelectedId()));
				sd.addState(StateSet.WILD_CARD, resources.getDrawable(plug.getImageId()));

				LinearLayout rl_tab = new LinearLayout(refTarget.get());
				rl_tab.setOrientation(LinearLayout.VERTICAL);


				TextView tv = new TextView(refTarget.get());
				tv.setText(plug.getText().getText());
				tv.setGravity(Gravity.CENTER);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);


				ImageView iv = new ImageView(refTarget.get());
				iv.setImageDrawable(sd);

				rl_tab.addView(iv);
				rl_tab.addView(tv);


				return rl_tab;


			}
		});



		mainTabsOutlet.attach(holder);
		mainTabsOutlet.setActivePlug(mainTabsOutlet.getPlugs().getItem(0));


		isAttached = true;
		Log.e("activity", "onAttach");
	}


	@Override
	public void onActivate() {
		Log.e("activity", "onActivate");

	}

	@Override
	public void run(Object... args) {
		App.get().startActivity(MainActivity.class);
	}

	@Override
	public void onShow() {
		Log.e("activity", "onShow");

		UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));



		super.onShow();
	}







}





