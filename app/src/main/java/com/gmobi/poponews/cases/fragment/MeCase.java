package com.gmobi.poponews.cases.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.app.OutletNames;
import com.gmobi.poponews.cases.accountsetting.AccountSettingActivity;
import com.gmobi.poponews.cases.browser.BrowserActivity;
import com.gmobi.poponews.cases.login.LoginActivity;
import com.gmobi.poponews.cases.setting.SettingsActivity;
import com.gmobi.poponews.model.MeItem;
import com.gmobi.poponews.outlet.MyTabPlug;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.ICase;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.FragmentHolder;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.ImageHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import javax.inject.Inject;

public class MeCase extends Case<Fragment> implements View.OnClickListener {

	public MeCase(ICase<?> parent) {
		super(parent);
	}

	@Inject
	Resources resources;
	@Inject
	IConfigService configService;
	@Inject
	IMessageService messageService;
	@Inject
	IUserService userService;
	@Inject
	IImageService imageService;
	@Inject
	IUITaskService uiTaskService;

	private MyTabPlug mePlug;
	private ImageView ivAvatar;
	private TextView tvName;

	private DataList<MeItem> meList = new DataList<>();
	private ListViewBinder menuLvBinder;


	private int[] menuResList = {
			R.drawable.sidebar_myfavorites,
			R.drawable.sidebar_newsfeeds,
			R.drawable.sidebar_recentlyread,
			R.drawable.sidebar_offlinedownload,
			R.drawable.set_store,
			R.drawable.slidebar_my_comment,
			R.drawable.slidebar_account_setting,
			R.drawable.sidebar_settings
	};

	private int[] menuTextList = {
			R.string.label_menu_fav,
			R.string.label_menu_feed,
			R.string.label_menu_histroy,
			R.string.label_menu_download,
			R.string.set_store,
			R.string.label_menu_mycomment,
			R.string.label_menu_account_setting,
			R.string.label_menu_config
	};

	private String[] menuActionList = {
			"com.gmobi.intent.action.FAVORITE",
			"com.gmobi.intent.action.FEED",
			"com.gmobi.intent.action.HISTORY",
			"com.gmobi.intent.action.OFFLINE",
			"",
			"com.gmobi.intent.action.MYCOMMENT",
			"com.gmobi.intent.action.ACCOUNTSETTING",
			"com.gmobi.intent.action.SETTING"
	};

	private String[] menuTypeList = {
			MeItem.ME_TYPE_NORMAL,
			MeItem.ME_TYPE_NORMAL,
			MeItem.ME_TYPE_NORMAL,
			MeItem.ME_TYPE_NORMAL,
			MeItem.ME_TYPE_URL,
			MeItem.ME_TYPE_NORMAL,
			MeItem.ME_TYPE_NORMAL,
			MeItem.ME_TYPE_SETTING
	};


	@Override
	public void onCreate() {
		//initMenuData();

		mePlug = new MyTabPlug(R.drawable.me,
				R.drawable.me_sel,
				R.string.tabs_me,
				FragmentHolder.create(R.layout.fragment_me, this), OutletNames.PLUG_ME);

		getOutlet(OutletNames.MAIN_TABS).addPlug(mePlug);

		if (UiHelper.isCherryVersion()) {
			messageService.addHandler(MessageTopics.USER_LOGIN_SUCCESS, new IMessageHandler() {
				@Override
				public void process(Object o, Message message) {
					setUserUI();
				}
			});
		}
	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onAttach(Fragment target) {
		UiHelper.setMainAlive(true);

		initMenuData();

		ItemBinder itemBinder = new ItemBinder(R.layout.menu_item_normal,
				new int[]{R.id.menu_title}, new String[]{"text"});

		ViewBinder.Setter setter = new ViewBinder.Setter() {
			@Override
			public boolean onSet(View view, String s, int i, String s1, Object o, View view1, IContainerBinder iContainerBinder) {
				if (view.getId() == R.id.menu_title) {
					ViewGroup parent = (ViewGroup) view1;
					MeItem mi = meList.getItem(i);
					ImageView iv = (ImageView) parent.findViewById(R.id.menu_icon);
					iv.setImageResource(mi.getIconId());

					TextView tv = (TextView) parent.findViewById(R.id.menu_title);
					if (!NightModeUtil.isNightMode())
						tv.setTextColor(resources.getColor(R.color.bg_black));
					else
						tv.setTextColor(resources.getColor(R.color.bg_white_night));


				}
				return false;
			}
		};
		itemBinder.addSetter(setter);
		menuLvBinder = new ListViewBinder(itemBinder);
		menuLvBinder.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
			@Override
			public void process(Object o, ItemEventArgs itemEventArgs) {
				onClickListener((MeItem) itemEventArgs.getItem());
			}
		});
		menuLvBinder.bind(ViewHolder.get(getAttachedObject().getActivity(), R.id.me_list), meList);


		if (UiHelper.isCherryVersion()) {
			if (userService.isLogged()) {
				setUserUI();
			} else {
				RelativeLayout rlLogin = ViewHolder.get(getAttachedObject().getActivity(), R.id.rl_avatar).getView();
				rlLogin.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						userService.doLogin(getAttachedObject().getActivity());
					}
				});
			}
		} else {
			tvName = ViewHolder.get(getAttachedObject().getActivity(), R.id.tv_name).getView();
			tvName.setText(App.get().getResources().getString(R.string.clicklogin));

			messageService.addHandler("login", new IMessageHandler() {
				@Override
				public void process(Object o, Message message) {
					uiTaskService.run(new Runnable() {
						@Override
						public void run() {
							tvName.setText(userService.getUserInfo().getUserName());
						}
					});
				}
			});
			ivAvatar = ViewHolder.get(getAttachedObject().getActivity(), R.id.iv_avatar).getView();
			tvName.setOnClickListener(this);
			ivAvatar.setOnClickListener(this);
		}
		AnalysisUtil.onActivityResume(getAttachedObject().getActivity(), AnalysisUtil.SCR_ME);
		AnalysisUtil.recordMe();
		Log.e("Mecase", "onAttach");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_name:
			case R.id.iv_avatar:
				if (userService.getUserInfo() != null && userService.isLogin()) {
					App.get().startActivity(AccountSettingActivity.class);
				} else {
					App.get().startActivity(LoginActivity.class);
				}
				break;
		}
	}


	@Override
	public void onActivate() {
		Log.e("Mecase", "onActivate");

	}

	@Override
	public void run(Object... args) {
		getOutlet(OutletNames.MAIN_TABS).setActivePlug(mePlug);
	}


	@Override
	public void onShow() {
		Log.e("Mecase", "onShow");
		if(!UiHelper.isCherryVersion()) {

			if (userService.getUserInfo() != null && userService.isLogin()) {
				tvName.setText(userService.getUserInfo().getUserName());
				String avatar = userService.getUserInfo().getAvatar();
				if (avatar != null) {
					Bitmap bitmap = getBitmapIfFileExist(avatar);
					if (bitmap == null)
						imageService.bind(avatar, ivAvatar);
					else
						ivAvatar.setImageBitmap(bitmap);
				} else {
					ivAvatar.setImageResource(R.drawable.head);
				}
			} else {
				tvName.setText(App.get().getResources().getString(R.string.clicklogin));
				ivAvatar.setImageResource(R.drawable.head);
			}
		}

		if (getAttachedObject() != null) {
			if (NightModeUtil.isNightMode()) {
				getAttachedObject().getActivity().findViewById(R.id.rl_avatar).setBackgroundColor(resources.getColor(R.color.bg_red_night));
				getAttachedObject().getActivity().findViewById(R.id.ll_me).setBackgroundColor(resources.getColor(R.color.bg_black_night));
			} else {
				getAttachedObject().getActivity().findViewById(R.id.rl_avatar).setBackgroundColor(resources.getColor(R.color.bg_red));
				getAttachedObject().getActivity().findViewById(R.id.ll_me).setBackgroundColor(resources.getColor(R.color.bg_white));
			}

		}
		if (menuLvBinder != null && menuLvBinder.getAdapter() != null)
			menuLvBinder.getAdapter().notifyDataSetChanged();

		super.onShow();
	}

	private void initMenuData() {
		meList.removeAllItems();
		for (int i = 0; i < menuResList.length; i++) {
			if (UiHelper.isCherryVersion()) {
				if (i == 5 || i == 6)
					continue;
			}

			if(i == 1 && !BuildConfig.SUPPORT_PUSH)
				continue;

			if(i == 4)
				continue;


			MeItem mi = new MeItem();
			mi.setType(menuTypeList[i]);
			mi.setIconId(menuResList[i]);
			mi.setText(resources.getString(menuTextList[i]));
			mi.setAction(menuActionList[i]);
			meList.addItem(mi);
		}
	}


	private void onClickListener(MeItem mi) {
		Intent intent;
		Context ctx = getAttachedObject().getActivity();
		if (mi.getType().equals(MeItem.ME_TYPE_NORMAL)) {
			if (mi.getAction().equals(menuActionList[0]))
				AnalysisUtil.recordMeFav();
			else if (mi.getAction().equals(menuActionList[1]))
				AnalysisUtil.recordMeFeed();
			else if (mi.getAction().equals(menuActionList[2]))
				AnalysisUtil.recordMeRecent();
			else if (mi.getAction().equals(menuActionList[3]))
				AnalysisUtil.recordMeOffline();

			intent = new Intent();
			intent.setAction(mi.getAction());
			ctx.startActivity(intent);


		} else if (mi.getType().equals(MeItem.ME_TYPE_URL)) {
			AnalysisUtil.recordMeOffers();

			intent = new Intent(ctx, BrowserActivity.class);
			intent.putExtra(IntentNames.INTENT_EXTRA_ID, "store");
			intent.putExtra(IntentNames.INTENT_EXTRA_TYPE, "store");
			intent.putExtra(IntentNames.INTENT_EXTRA_SNS, "store");

			intent.putExtra(IntentNames.INTENT_EXTRA_TITLE, resources.getString(R.string.set_store));
			intent.putExtra(IntentNames.INTENT_EXTRA_URL, configService.getBuiltinStoreUrl());
			intent.putExtra(IntentNames.INTENT_EXTRA_DOMAIN, "store");

			ctx.startActivity(intent);
		} else if (mi.getType().equals(MeItem.ME_TYPE_SETTING)) {
			AnalysisUtil.recordMeSetting();
			SettingsActivity.startMainActivity(ctx);
		}
	}

	private void setUserUI() {
		if (getAttachedObject() == null)
			return;

		JSONObject info = configService.getUserInfo();
		if (info != null && info.has("fb")) {
			try {
				JSONObject fbInfo = info.getJSONObject("fb");
				TextView tvName = ViewHolder.get(getAttachedObject().getActivity(), R.id.tv_name).getView();
				tvName.setText(fbInfo.getString("name"));


				String avatarUrl = fbInfo.getString("avatar");
				ivAvatar = ViewHolder.get(getAttachedObject().getActivity(), R.id.iv_avatar).getView();

				Bitmap bitmap = getBitmapIfFileExist(avatarUrl);
				if (bitmap == null)
					imageService.bind(avatarUrl, ivAvatar);
				else
					ivAvatar.setImageBitmap(bitmap);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}


	}


	private Bitmap getBitmapIfFileExist(String fullUri) {
		File bmpFile = imageService.getCacheOf(fullUri);
		Bitmap bitmap = null;
		if (bmpFile.exists()) {
			bitmap = ImageHelper.fromFile(bmpFile, 0, 0);
		}
		return bitmap;
	}

}





