package com.gmobi.poponews.cases.guide;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.model.EditionData;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.LocaleHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder.Setter;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IMessageService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

public class GuideEditonCase extends Case<CaseActivity>{

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
	
	public GuideEditonCase(String name) {
		super(name);
	}
	public GuideEditonCase(ICase<?> parent) {
		super(parent);
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
				if(view.getId() == R.id.ctv_name)
				{
					CheckedTextView ctv = (CheckedTextView) view;
					ctv.setTextColor(NightModeUtil.isNightMode() ? resource.getColor(R.color.bg_white_night) : resource.getColor(R.color.bg_black));
					ctv.setCheckMarkDrawable(NightModeUtil.isNightMode() ? R.drawable.list_choice_indicator_single_night :R.drawable.list_choice_indicator_single);
				}
				return false;
			}
		};
		ib.addSetter(ibSetter);
			
		editionBinder = new ListViewBinder(ib);
		
		editionBinder.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>(){

			@Override
			public void process(Object sender, ItemEventArgs args) {
					EditionData data = (EditionData) args.getItem();
					String ch = data.getChannel();
					if(null != ch && !ch.isEmpty()){
						configService.updateCurEdition(ch, data.getLang(), null, null);
						//App.get().getService(ISocialDataService.class).removeAllFromAccList(SocialExtra.SOCIAL_TYPE_GOOGLE);
						//App.get().getService(ISocialDataService.class).removeAllFromPostList();
						Context ctx = App.get().getBaseContext();
						Intent i = ctx.getPackageManager() 
								.getLaunchIntentForPackage(ctx.getPackageName()); 
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

						Logger.error("Setting:Local select "+data.getLang());
						LocaleHelper.setDefaultLocal(data.getLang());


						App.get().startActivity(i);
					}
				}
			});
        msgService.addHandler(MessageTopics.EDITION_LIST_READY_SPLASH, new IMessageHandler(){
            @Override
            public void process(Object sender, Message msg) {
                if(isAttached()){
					defEdition = 0;
					pariseEditionData(configService.getEditionList(), editions);
					if(editions.getItemCount() > 0){
						ListView lv = (ListView) getAttachedObject().findViewById(R.id.edition_list);
						editionBinder.bind(lv, editions);
						lv.setItemChecked(defEdition, true);
					}
                }
            }
        });
	}

	@Override
	public void onAttach(CaseActivity target)
	{
        if(!configService.isEditionListExist()){
            return;
        }

		View rlSkip = ViewHolder.get(target, R.id.rl_editon_skip).getView();
		rlSkip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context ctx = App.get().getBaseContext();
				Intent i = ctx.getPackageManager()
						.getLaunchIntentForPackage(ctx.getPackageName());
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

				Logger.error("Setting:Local select "+editions.getItem(defEdition).getLang());
				LocaleHelper.setDefaultLocal(editions.getItem(defEdition).getLang());


				App.get().startActivity(i);



				Intent mainIntent = new Intent(getAttachedObject(), MainActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getAttachedObject().startActivity(mainIntent);
				getAttachedObject().finish();
			}
		});

		defEdition = 0;
		pariseEditionData(configService.getEditionList(), editions);

		if(editions.getItemCount() > 0) {
			ListView lv = (ListView) getAttachedObject().findViewById(R.id.edition_list);
			editionBinder.bind(lv, editions);
			lv.setItemChecked(defEdition, true);
		}
	}
	
	private void pariseEditionData(JSONObject dataSrc, DataList<EditionData> out){
		String curEdition = configService.getCurChannel();
        out.beginBatchChange();
		out.removeAllItems();
		try{
            String baseUrl = dataSrc.getString("baseUrl");
			JSONArray ja = dataSrc.getJSONArray("data");
			JSONObject tmpJo;
			for(int i = 0; i < ja.length(); i++){
				tmpJo = ja.getJSONObject(i);
				EditionData ed = new EditionData(baseUrl + tmpJo.getString("icon"),
						tmpJo.getString("name"), 
						tmpJo.getString("id"),tmpJo.getString("lang"));
				out.addItem(ed);
				if(curEdition.equals(ed.getChannel())){
					defEdition = i;
				}
			}
		}catch(Exception e){
			out.removeAllItems();
		}
        out.endBatchChange();
	}

	@Override
	public void onShow() {
		NightModeUtil.setViewColor(getAttachedObject(), R.id.edition_list,
				resource.getColor(R.color.bg_white), resource.getColor(R.color.bg_black_night));
		
		if(editionBinder!=null && editionBinder.getAdapter()!=null)
			editionBinder.getAdapter().notifyDataSetChanged();
		
		super.onShow();
	}
	
	

}
