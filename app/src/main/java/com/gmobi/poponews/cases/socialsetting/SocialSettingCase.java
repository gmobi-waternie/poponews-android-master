package com.gmobi.poponews.cases.socialsetting;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.service.IBaiduService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.IGoogleService;
import com.gmobi.poponews.service.ISocialDataService;
import com.gmobi.poponews.service.ISocialService;
import com.gmobi.poponews.service.ITwitterService;
import com.gmobi.poponews.service.IWeiboService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.ISystemService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class SocialSettingCase extends Case<CaseActivity> {

	public SocialSettingCase(String name) {
		super(name);
	}

	public SocialSettingCase(ICase<?> parent) {
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
	IFacebookService fbService;
	@Inject
	IGoogleService googleService;
	@Inject
	ITwitterService twitterService;
	@Inject
	IWeiboService weiboService;
	@Inject
	IBaiduService baiduService;
	@Inject
	ISocialDataService socialDataService;

	private int social_type_int;
	private String social_type;
	private ISocialService socialService;
	private boolean[] checkMap;
	private View.OnClickListener saveAndBackListener;

	private int[][] socialRes = {
			{
					R.string.facebook_name,
					R.drawable.facebook_1,
			},
			{
					R.string.twitter_name,
					R.drawable.twitter_1,
			},
			{
					R.string.google_name,
					R.drawable.google_1,
			},

			{
					R.string.google_name,
					R.drawable.weibo_1,
			},
			{
					R.string.google_name,
					R.drawable.baidu_1,
			},

	};
	private ListViewBinder lvb;
	private IMessageHandler getAccountProcHandler;
	private IMessageHandler logAccountFailHandler;


	@Override
	public void onCreate() {
		ItemBinder binder1 = new ItemBinder(
				R.layout.social_ctg_list_item,
				new int[]{R.id.ss_ctg_name},
				new String[]{"name"});
		binder1.addSetter(new ViewBinder.Setter() {
			@Override
			public boolean onSet(View view, String s, int i, String s1, Object o, View parent, IContainerBinder iContainerBinder) {
				IDataList<SocialAccount> dataSrc = socialDataService.getAccList(social_type);

				if (view.getId() == R.id.ss_ctg_name) {
					if(i<dataSrc.getItemCount()) {
						final SocialAccount sa = dataSrc.getItem(i);
						CheckBox cb = (CheckBox) parent.findViewById(R.id.cb_ss_select);
						cb.setChecked(sa.isSelected());
						cb.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								CheckBox cb = (CheckBox) v;
								sa.setSelect(cb.isChecked());
								SocialSetting.setCategorySelect(social_type, sa.getId(), cb.isChecked());
								updateSelectAllStatus();
							}
						});
					}
				}
				return false;
			}
		});

		lvb = new ListViewBinder(binder1);

		saveAndBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (social_type.equals(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
					fbService.remoteGetAllAccountPosts(false);
					fbService.setFetchedData(true);
					fbService.syncData();

				} else if (social_type.equals(SocialExtra.SOCIAL_TYPE_TWITTER)) {
					twitterService.remoteGetAllAccountPosts(false);
					twitterService.setFetchedData(true);
					twitterService.syncData();

				} else if (social_type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
					googleService.remoteGetAccountPosts(null, null);
					googleService.setFetchedData(true);
					googleService.syncData();

				} else if (social_type.equals(SocialExtra.SOCIAL_TYPE_BAIDU)) {
					baiduService.remoteGetAccountPosts(null, null);
					baiduService.setFetchedData(true);
					baiduService.syncData();
				}
				else if (social_type.equals(SocialExtra.SOCIAL_TYPE_WEIBO)) {
					weiboService.remoteGetAccountPosts(null, null);
					weiboService.setFetchedData(true);
					weiboService.syncData();
				}


				Activity currActivity = App.get().getCurrentActivity();
				if (currActivity != null)
					currActivity.finish();

			}
		};

		getAccountProcHandler = new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (isAttached())
					ViewHolder.get(getAttachedObject(), R.id.rl_loading).getView().setVisibility(View.GONE);
			}
		};


		messageService.addHandler(MessageTopics.GET_FACEBOOK_LIKES_SETTING, getAccountProcHandler);
		messageService.addHandler(MessageTopics.GET_GOOGLE_CHANNEL_SETTING, getAccountProcHandler);
		messageService.addHandler(MessageTopics.GET_TWITTER_CHANNEL_SETTING, getAccountProcHandler);
		messageService.addHandler(MessageTopics.GET_BAIDU_CHANNEL_SETTING, getAccountProcHandler);
		messageService.addHandler(MessageTopics.GET_WEIBO_CHANNEL_SETTING, getAccountProcHandler);



		logAccountFailHandler = new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if (getAttachedObject() != null) {
					CheckBox open = ViewHolder.get(getAttachedObject(), R.id.cb_socail).getView();
					if (open != null)
						open.setChecked(false);

					View loading = ViewHolder.get(getAttachedObject(), R.id.rl_loading).getView();
					if (loading != null)
						loading.setVisibility(View.GONE);
				}
			}
		};

		messageService.addHandler(MessageTopics.LOGIN_FACEBOOK_FAIL, logAccountFailHandler);
		messageService.addHandler(MessageTopics.LOGIN_TWITTER_FAIL, logAccountFailHandler);
		messageService.addHandler(MessageTopics.LOGIN_WEIBO_FAIL, logAccountFailHandler);


	}

	@Override
	public void run(Object... args) {
		App.get().startActivity(SocialSettingActivity.class);
	}

	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);

		Intent in = target.getIntent();
		social_type = in.getStringExtra("social_type");
		if (social_type.equals(""))
			social_type = SocialExtra.SOCIAL_TYPE_FACEBOOK;


		if (social_type.equals(SocialExtra.SOCIAL_TYPE_FACEBOOK))
			socialService = App.get().getService(IFacebookService.class);
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_TWITTER))
			socialService = App.get().getService(ITwitterService.class);
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE))
			socialService = App.get().getService(IGoogleService.class);
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_WEIBO))
			socialService = App.get().getService(IWeiboService.class);
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_BAIDU))
			socialService = App.get().getService(IBaiduService.class);


		ImageView iv = ViewHolder.get(target, R.id.iv_loading).getView();
		AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
		loadAnim.setOneShot(false);
		loadAnim.start();


		initializeUI(refTarget.get());


		if (socialDataService.getAccList(social_type).getItemCount() > 0)
			ViewHolder.get(target, R.id.rl_loading).getView().setVisibility(View.GONE);
		lvb.bind(ViewHolder.get(target, R.id.lv_ss_ctglist), socialDataService.getAccList(social_type));


	}


	@Override
	public void onShow() {
		if (getAttachedObject() != null) {
			NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_ss_action_bar);
			UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
					NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
		} else {
			Logger.error("SocialSetting not Attach!");
		}
		//lvb.getAdapter().notifyDataSetChanged();

	}


	private void initializeUI(final CaseActivity target) {
		int social_type_int;
		if (social_type.equals(SocialExtra.SOCIAL_TYPE_TWITTER))
			social_type_int = 1;
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE))
			social_type_int = 2;
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_WEIBO))
			social_type_int = 3;
		else if (social_type.equals(SocialExtra.SOCIAL_TYPE_BAIDU))
			social_type_int = 4;
		else
			social_type_int = 0;


		CheckBox open = ViewHolder.get(target, R.id.cb_socail).getView();
		TextView title = ViewHolder.get(target, R.id.ss_action_bar_title).getView();

		ImageView icon = ViewHolder.get(target, R.id.ss_icon).getView();
		CheckBox cb_all = ViewHolder.get(target, R.id.cb_social_select_all).getView();

		open.setChecked(SocialSetting.getStatus(social_type));
		if (!SocialSetting.getStatus(social_type))
			ViewHolder.get(target, R.id.rl_loading).getView().setVisibility(View.GONE);


		title.setText(socialService.getTitle());
		icon.setImageDrawable(resources.getDrawable(socialRes[social_type_int][1]));


		cb_all.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				socialDataService.setAllAccountsSelectStatus(social_type, cb.isChecked());
				DataList<SocialAccount> accList = (DataList<SocialAccount>) socialDataService.getAccList(social_type);

				if (accList != null) {
					for (int i = 0; i < accList.getItemCount(); i++) {
						SocialSetting.setCategorySelect(social_type, accList.getItem(i).getId(), cb.isChecked());
					}
				}

			}
		});


		open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SocialSetting.setStatus(social_type, isChecked);
				if (!isChecked) {

					doLogoutAction(social_type);
					ViewHolder.get(target, R.id.rl_loading).getView().setVisibility(View.GONE);



				} else {

					ViewHolder.get(target, R.id.rl_loading).getView().setVisibility(View.VISIBLE);
					ImageView iv = ViewHolder.get(target, R.id.iv_loading).getView();
					AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
					loadAnim.setOneShot(false);
					loadAnim.start();


					doLoginAction(social_type,target);


				}

			}
		});

		View ivBackBtn = ViewHolder.get(target, R.id.ss_back).getView();
		ivBackBtn.setOnClickListener(saveAndBackListener);

		View ivSaveBtn = ViewHolder.get(target, R.id.ss_save).getView();
		ivSaveBtn.setOnClickListener(saveAndBackListener);


		updateSelectAllStatus();
	}


	private void doLoginAction(String type,CaseActivity activity) {
		if (type.equals(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
			fbService.doLogin(activity, ISocialService.FROM_SETTING);
		} else if (type.equals(SocialExtra.SOCIAL_TYPE_TWITTER)) {
			twitterService.doLogin(activity, ISocialService.FROM_SETTING);
		} else if (type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
			googleService.doLogin(activity, ISocialService.FROM_SETTING);
		} else if (type.equals(SocialExtra.SOCIAL_TYPE_BAIDU)) {
			baiduService.doLogin(activity, ISocialService.FROM_SETTING);
		} else if (type.equals(SocialExtra.SOCIAL_TYPE_WEIBO)) {
			weiboService.doLogin(activity, ISocialService.FROM_SETTING);
		}

	}

	private void doLogoutAction(String type) {
		if (type.equals(SocialExtra.SOCIAL_TYPE_FACEBOOK)) {
			fbService.doLogout(null);
		} else if (type.equals(SocialExtra.SOCIAL_TYPE_TWITTER)) {
			twitterService.doLogout(null);
		} else if (type.equals(SocialExtra.SOCIAL_TYPE_GOOGLE)) {
			googleService.doLogout(null);
		}
		else if (type.equals(SocialExtra.SOCIAL_TYPE_WEIBO)) {
			weiboService.doLogout(null);
		}
		else if (type.equals(SocialExtra.SOCIAL_TYPE_BAIDU)) {
			baiduService.doLogout(null);
		}
	}


	private void updateSelectAllStatus() {
		CheckBox cb = ViewHolder.get(getAttachedObject(), R.id.cb_social_select_all).getView();
		cb.setChecked(socialDataService.isAllAccountsSelect(social_type));
	}


}





