package com.gmobi.poponews.cases.setting;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.EditionData;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.provider.PoponewsProvider;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IOfflineService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.ISocialDataService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.LocaleHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.PreferenceHelper;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.util.WidgetDataHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder.Setter;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IHttpService;
import com.momock.service.IMessageService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

public class EditionCase extends Case<CaseActivity> {

	@Inject
	IConfigService configService;
	@Inject
	IMessageService msgService;
	@Inject
	IRemoteService remoteService;
	@Inject
	Resources resource;

	private DataList<EditionData> editions = new DataList<EditionData>();
	private ListViewBinder editionBinder = null;
	private int defEdition;

	public EditionCase(String name) {
		super(name);
	}

	@Override
	public void onCreate() {

		ItemBinder ib = new ItemBinder(
				R.layout.setting_edition_item,
				new int[]{R.id.iv_flag, R.id.ctv_name},
				new String[]{EditionData.TAG_ICON, EditionData.TAG_NAME});

		Setter ibSetter = new Setter() {

			@Override
			public boolean onSet(View view, String viewProp, int index, String key,
								 Object val, View parent, IContainerBinder container) {
				if (view.getId() == R.id.ctv_name) {
					CheckedTextView ctv = (CheckedTextView) view;
					ctv.setTextColor(NightModeUtil.isNightMode() ? resource.getColor(R.color.bg_white_night) : resource.getColor(R.color.bg_black));
					ctv.setCheckMarkDrawable(NightModeUtil.isNightMode() ? R.drawable.list_choice_indicator_single_night : R.drawable.list_choice_indicator_single);
				}
				return false;
			}
		};
		ib.addSetter(ibSetter);

		editionBinder = new ListViewBinder(ib);

		editionBinder.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {

			@Override
			public void process(Object sender, ItemEventArgs args) {
				EditionData data = (EditionData) args.getItem();
				String ch = data.getChannel();
				if (null != ch && !ch.isEmpty()) {
					configService.updateCurEdition(ch, data.getLang(), null, null);


					notifyWidgetEditionChanged();


					App.get().getService(ISocialDataService.class).removeAllFromAccList(SocialExtra.SOCIAL_TYPE_GOOGLE, true);
					App.get().getService(IOfflineService.class).stopDownloadCategory(false);

					if(getAttachedObject()!=null)
						getAttachedObject().finish();


					//App.get().getService(ISocialDataService.class).removeAllFromPostList();
					Context ctx = App.get().getBaseContext();
					Intent i = ctx.getPackageManager()
							.getLaunchIntentForPackage(ctx.getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

					Logger.error("Setting:Local select " + data.getLang());
					LocaleHelper.setDefaultLocal(data.getLang());


					UiHelper.stopAllTimer();

					App.get().startActivity(i);
				}
			}
		});
		msgService.addHandler(MessageTopics.EDITION_LIST_READY, new IMessageHandler() {
			@Override
			public void process(Object sender, Message msg) {
				if (isAttached()) {
					defEdition = 0;
					pariseEditionData(configService.getEditionList(), editions);
					if (editions.getItemCount() > 0) {
						ListView lv = (ListView) getAttachedObject().findViewById(R.id.edition_list);
						editionBinder.bind(lv, editions);
						lv.setItemChecked(defEdition, true);
					}
				}
			}
		});
	}

	@Override
	public void onAttach(CaseActivity target) {
		AnalysisUtil.recordMeEditon();

		remoteService.getEditionList(GlobalConfig.FROM_SETTING);
		if (!configService.isEditionListExist()) {
			return;
		}

		defEdition = 0;
		pariseEditionData(configService.getEditionList(), editions);
		if (editions.getItemCount() > 0) {
			ListView lv = (ListView) getAttachedObject().findViewById(R.id.edition_list);
			editionBinder.bind(lv, editions);
			lv.setItemChecked(defEdition, true);
		}
	}

	private void pariseEditionData(JSONObject dataSrc, DataList<EditionData> out) {
		String curEdition = configService.getCurChannel();
		out.beginBatchChange();
		out.removeAllItems();
		try {
			String baseUrl = dataSrc.getString("baseUrl");
			JSONArray ja = dataSrc.getJSONArray("data");
			JSONObject tmpJo;
			for (int i = 0; i < ja.length(); i++) {
				tmpJo = ja.getJSONObject(i);
				EditionData ed = new EditionData(baseUrl + tmpJo.getString("icon"),
						tmpJo.getString("name"),
						tmpJo.getString("id"), tmpJo.getString("lang"));
				out.addItem(ed);
				if (curEdition.equals(ed.getChannel())) {
					defEdition = i;
				}
			}
		} catch (Exception e) {
			out.removeAllItems();
		}
		out.endBatchChange();
	}

	@Override
	public void onShow() {
		NightModeUtil.setViewColor(getAttachedObject(), R.id.edition_list,
				resource.getColor(R.color.bg_white), resource.getColor(R.color.bg_black_night));

		if (editionBinder != null && editionBinder.getAdapter() != null)
			editionBinder.getAdapter().notifyDataSetChanged();

		super.onShow();
	}

	private void notifyWidgetEditionChanged() {
		PreferenceHelper.saveCategory(App.get(), "");
		WidgetDataHelper.getNewsInExecutor(App.get());
		Intent refreshIntent = new Intent(App.get(), PoponewsProvider.class);
		refreshIntent.setAction(IntentNames.REFRESH);
		App.get().sendBroadcast(refreshIntent);
	}


}
