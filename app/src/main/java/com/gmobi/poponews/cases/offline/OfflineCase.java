package com.gmobi.poponews.cases.offline;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.INewsCacheService;
import com.gmobi.poponews.service.IOfflineService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.OfflineAlarmHelper;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.PopoDialog;
import com.gmobi.poponews.widget.RoundProgressBar;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder.Setter;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.IDataList;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.ICacheService;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IRService;
import com.momock.service.ISystemService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.inject.Inject;

public class OfflineCase extends Case<CaseActivity> {

	public OfflineCase(String name) {
		super(name);
	}
	public OfflineCase(ICase<?> parent) {
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
	@Inject
	IOfflineService offlineService;
	
	
	
	

	

	ListViewBinder lvBinder;
	DBHelper dh = DBHelper.getInstance();
	
	IDataList<NewsCategory> datasource = null;
	

	PopoDialog timeDialog;
	
	@Override
	public void onCreate() {

		
		
		messageService.addHandler(MessageTopics.UPDATE_OFFLINE_PROGRESS, new IMessageHandler() {
			
			@Override
			public void process(Object sender, Message msg) {
				Bundle b = (Bundle)msg.getData();
				int per = b.getInt("per");
				String cid = b.getString("cid");


				NewsCategory c = dataService.getCategoryById(cid);
				if(c!=null)
					c.setOfflineProgress(per);


				if(isAttached()) {
					if (lvBinder != null && lvBinder.getAdapter() != null)
						lvBinder.getAdapter().notifyDataSetChanged();
				}
			}
		});

		messageService.addHandler(MessageTopics.UPDATE_OFFLINEALARM_START, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				if(isAttached())
				{
					initOfflinServiceList();
					if(checkSelectBoxStatus())
					{
						CheckBox cbAll = ViewHolder.get(getAttachedObject(), R.id.cb_offline_select_all).getView();
						View ivStartBtn = ViewHolder.get(getAttachedObject(), R.id.offline_action_bar_start).getView();
						View ivCancelBtn = ViewHolder.get(getAttachedObject(), R.id.offline_action_bar_cancel).getView();

						ivCancelBtn.setVisibility(View.VISIBLE);
						ivStartBtn.setVisibility(View.GONE);
						cbAll.setVisibility(View.INVISIBLE);
						lvBinder.getAdapter().notifyDataSetChanged();
					}
				}
			}
		});
		
		
		messageService.addHandler(MessageTopics.UPDATE_OFFLINE_FINISH, new IMessageHandler() {

			@Override
			public void process(Object sender, Message msg) {
				UiHelper.setOfflineDownloadMode(UiHelper.NOT_OFFLINE_DOWNLOAD);
				if (isAttached()) {
					CheckBox cbAll = ViewHolder.get(getAttachedObject(), R.id.cb_offline_select_all).getView();
					View ivStartBtn = ViewHolder.get(getAttachedObject(), R.id.offline_action_bar_start).getView();
					View ivCancelBtn = ViewHolder.get(getAttachedObject(), R.id.offline_action_bar_cancel).getView();

					ivStartBtn.setVisibility(View.VISIBLE);
					ivCancelBtn.setVisibility(View.GONE);
					cbAll.setVisibility(View.VISIBLE);
					if (lvBinder != null && lvBinder.getAdapter() != null)
						lvBinder.getAdapter().notifyDataSetChanged();
				}

				if (App.get().getCurrentActivity() != null && (Boolean) msg.getData() == true)
					Toast.makeText(App.get().getCurrentActivity(),
							App.get().getCurrentActivity().getResources().getString(R.string.offline_download_finish), Toast.LENGTH_SHORT).show();
			}
		});
		
		
		ItemBinder itemBinder = new ItemBinder(R.layout.offline_ctg_list_item,
				new int[] { R.id.offline_ctg_name}, new String[] { "name"});
		itemBinder.addSetter(new Setter() {
			
			@Override
			public boolean onSet(View view, String viewProp, int index, String key,
					Object val, View parent, IContainerBinder container) {
				final IDataList<NewsCategory> src = dataService.getAllOfflineCategories();
				
				if(view.getId() == R.id.offline_ctg_name)
				{
					NewsCategory c = src.getItem(index);
					RoundProgressBar pb = (RoundProgressBar) parent.findViewById(R.id.offline_download_percent);
					CheckBox cb = (CheckBox) parent.findViewById(R.id.cb_offline_select);

					if(UiHelper.isOfflineDownloadMode() > UiHelper.NOT_OFFLINE_DOWNLOAD)
					{
						cb.setVisibility(View.GONE);

						if(c.isOfflineFlag())
						{
							pb.setVisibility(View.VISIBLE);
							pb.setProgress(c.getOfflineProgress());
						}
						else
							pb.setVisibility(View.GONE);


					}
					else
					{
						pb.setVisibility(View.GONE);
						cb.setVisibility(View.VISIBLE);
						cb.setChecked(c.isOfflineFlag());

						final int p = index;
						cb.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								CheckBox c = (CheckBox) v;
								NewsCategory selCtg = src.getItem(p);
								selCtg.setOfflineFlag(c.isChecked());
								dh.setCtgOfflineSelect(selCtg.getid(), c.isChecked());
								setAllSelectCheckbox();
							}
						});
					}
					

					ImageView iv = (ImageView) parent.findViewById(R.id.offline_ctg_complete);

					
					if(c.isOfflineFlag() && c.getOfflineProgress() == 100)
						iv.setVisibility(View.VISIBLE);
					else
						iv.setVisibility(View.INVISIBLE);
					
				}
				return false;
			}
		});
		lvBinder = new ListViewBinder(itemBinder);

		

	}

	@Override
	public void run(Object... args) {
		App.get().startActivity(OfflineActivity.class);
	}
	
	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);
		final TextView tvTime = ViewHolder.get(target,R.id.tv_offline_timing).getView();
		final CheckBox cbTime = ViewHolder.get(target, R.id.cb_offline_timing).getView();
		final CheckBox cbPic = ViewHolder.get(target, R.id.cb_offline_dlpic).getView();
		final CheckBox cbAll = ViewHolder.get(target, R.id.cb_offline_select_all).getView();
		final View ivStartBtn = ViewHolder.get(target, R.id.offline_action_bar_start).getView();
		final View ivCancelBtn = ViewHolder.get(target, R.id.offline_action_bar_cancel).getView();


		boolean timeFlag = configService.getOfflineTimeFlag();
		boolean picFlag = configService.getOfflinePicFlag();
		int hour = configService.getOfflineTimeHour();
		int min= configService.getOfflineTimeMinute();
		
		cbPic.setChecked(picFlag);
		cbTime.setChecked(timeFlag);
		cbPic.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				configService.setOfflinePicFlag(isChecked);
			}
		});
		
		cbTime.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				configService.setOfflineTimeFlag(isChecked);
				if (isChecked) {
					OfflineAlarmHelper.startOfflineAlarm(App.get());
				} else
					OfflineAlarmHelper.stopOfflineAlarm(App.get());

			}
		});
		
		
		tvTime.setText(getTimeString(hour,min));
		if(cbTime.isChecked())
		{
			tvTime.setTextColor(getAttachedObject().getResources().getColor(R.color.bg_red));
		}
		else
		{
			tvTime.setTextColor(getAttachedObject().getResources().getColor(R.color.bg_grey));
		}
		
		datasource = dataService.getAllOfflineCategories();
		
		
		
		timeDialog = new PopoDialog(refTarget.get(), 
				R.style.PopoDialogStyle,R.layout.dialog_offline_time,
				false,
				R.id.positiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	int h = timeDialog.getTimePickerHour();
                    	int m = timeDialog.getTimePickerMinute();
                    	configService.setOfflineTime(h, m);
                    	if(configService.getOfflineTimeFlag())
                    	{
                    		OfflineAlarmHelper.startOfflineAlarm(App.get());
                    	}
                    	
                    	tvTime.setText(getTimeString(h,m));
                    	timeDialog.dismiss();
                    	
                    }
                },
                R.id.negativeButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	timeDialog.dismiss();
                    }
                });

		
		lvBinder.bind(ViewHolder.get(refTarget.get(), R.id.lv_offlinelist), dataService.getAllOfflineCategories());

		setAllSelectCheckbox();
		
		cbAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox)v;

				for(int i=0;i<datasource.getItemCount();i++)
				{
					datasource.getItem(i).setOfflineFlag(cb.isChecked());
					dh.setCtgOfflineSelect(datasource.getItem(i).getid(),cb.isChecked());
				}

				lvBinder.getAdapter().notifyDataSetChanged();
			}
		});
		

		
		tvTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(cbTime.isChecked())
				{
					timeDialog.show();
					int curh=configService.getOfflineTimeHour();
					int curm=configService.getOfflineTimeMinute();
					timeDialog.setTimePicker(curh, curm,new OnTimeChangedListener() {
						@Override
						public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
							Logger.debug("OfflineTime:" + hourOfDay + ":" + minute);
						}
					});
				}
			}
		});
		
		cbTime.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					tvTime.setTextColor(getAttachedObject().getResources().getColor(R.color.bg_red));
					configService.setOfflineTimeFlag(true);
				}
				else
				{
					tvTime.setTextColor(getAttachedObject().getResources().getColor(R.color.bg_grey));
					configService.setOfflineTimeFlag(false);
				}
			}
		});
		
		
		
		

		if(UiHelper.isOfflineDownloadMode() > UiHelper.NOT_OFFLINE_DOWNLOAD)
		{
			ivCancelBtn.setVisibility(View.VISIBLE);
			ivStartBtn.setVisibility(View.GONE);
			cbAll.setVisibility(View.INVISIBLE);
		}
		else
		{
			ivStartBtn.setVisibility(View.VISIBLE);
			ivCancelBtn.setVisibility(View.GONE);
			cbAll.setVisibility(View.VISIBLE);
		}
		
		ivStartBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				initOfflinServiceList();


				if(checkSelectBoxStatus())
				{

					if(!UiHelper.isNetworkConnected(App.get().getCurrentActivity()))
					{
						Toast.makeText(App.get().getCurrentActivity(),App.get().getCurrentActivity().getString(R.string.offline_download_no_network),Toast.LENGTH_SHORT).show();
						return;
					}

					if(UiHelper.isOfflineDownloadMode() == UiHelper.OFFLINE_ALARM_DOWNLOADING)
						offlineService.stopDownloadCategory(false);
					
					offlineService.startDownloadCategory(UiHelper.OFFLINE_DOWNLOADING);

					ivCancelBtn.setVisibility(View.VISIBLE);
					ivStartBtn.setVisibility(View.GONE);
					cbAll.setVisibility(View.INVISIBLE);
					lvBinder.getAdapter().notifyDataSetChanged();
				}
				else
				{
					Toast.makeText(App.get().getCurrentActivity(), getAttachedObject().getResources().getString(R.string.offline_download_empty), Toast.LENGTH_SHORT).show();
				}
					
				
			}
		});
		
		ivCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				offlineService.stopDownloadCategory(false);

				IDataList<NewsCategory> list = dataService.getAllOfflineCategories();

				for(int i=0;i<list.getItemCount();i++)
				{
					if(list.getItem(i).isOfflineFlag())
					{
						list.getItem(i).setOfflineProgress(0);
					}
				}

				ivStartBtn.setVisibility(View.VISIBLE);
				ivCancelBtn.setVisibility(View.GONE);
				cbAll.setVisibility(View.VISIBLE);
				lvBinder.getAdapter().notifyDataSetChanged();
			}
		});
		
		View ivBackBtn = ViewHolder.get(target, R.id.offline_back).getView();
		
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
		NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_offline_action_bar);
		UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
	}
	

	private void initOfflinServiceList()
	{
		offlineService.clearOfflineList();

		for(int i=0;i<datasource.getItemCount();i++)
		{
			NewsCategory c = datasource.getItem(i);
			if(c.isOfflineFlag())
			{
				offlineService.addCategoryToOfflineList(c.getid());
				c.setOfflineProgress(0);
			}
		}
	}
	


	private void setAllSelectCheckbox()
	{
		boolean allSet = true;
		
		for(int i=0;i<datasource.getItemCount();i++)
		{
			if(!datasource.getItem(i).isOfflineFlag())
			{
				allSet = false;
				break;
			}
		}
		
		CheckBox cb_all = ViewHolder.get(getAttachedObject(), R.id.cb_offline_select_all).getView();
		cb_all.setChecked(allSet);
	}
	

	private boolean checkSelectBoxStatus()
	{
		boolean hasSet = false;
		for(int i=0;i<datasource.getItemCount();i++)
		{
			if(datasource.getItem(i).isOfflineFlag())
			{
				hasSet = true;
				break;
			}
		}
		
		return hasSet;
		
	}
	
	
	
	private String getTimeString(int hour, int min)
	{
		return hour+":"+ (min>=10 ? ""+min : "0"+min);
	}
	

}





