package com.gmobi.poponews.cases.categorysetting;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.ViewHolder;
import com.momock.service.IMessageService;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class CategorySettingCase extends Case<CaseActivity> {

	public CategorySettingCase(String name) {
		super(name);
	}

	public CategorySettingCase(ICase<?> parent) {
		super(parent);
	}

	@Inject
	Resources resources;
	@Inject
	IMessageService messageService;
	@Inject
	IDataService ds;

	DragSortListView dslv;
	ListView lv;
	ListViewBinder dslvBinder;
	ListViewBinder lvBinder;
	DragSortController dslvController;

	private boolean editMode = true;
	private boolean isChanged = false;

	@Override
	public void onCreate() {
		ItemBinder visibleItemBinder = new ItemBinder(R.layout.visible_ctg_list_item,
				new int[]{R.id.cs_title}, new String[]{"name"});





		ItemBinder itemBinder = new ItemBinder(R.layout.ctg_list_item,
				new int[]{R.id.cs_title}, new String[]{"name"});
		itemBinder.addSetter(new ViewBinder.Setter() {

			@Override
			public boolean onSet(View view, String viewProp, final int index, String key,
								 Object val, View parent, IContainerBinder container) {
				if (view.getId() == R.id.cs_title) {
					DataList<NewsCategory> ncList = (DataList<NewsCategory>) dslvBinder.getDataSource();
					NewsCategory c = ncList.getItem(index);
					dslv.setItemChecked(index, c.isVisible() == NewsCategory.VISIBLE ? true : false);

					final CheckBox cb = (CheckBox) parent.findViewById(R.id.cs_cb);
					final TextView tv = (TextView) parent.findViewById(R.id.cs_title);
					final RelativeLayout rlMove = (RelativeLayout) parent.findViewById(R.id.drag_handle);
					cb.setTag("CheckBox");
					tv.setTag("TextView");
					if (editMode) {
						tv.setTextColor(NightModeUtil.isNightMode() ? resources.getColor(R.color.bg_white_night) : resources.getColor(R.color.bg_black));
						cb.setVisibility(View.VISIBLE);
						rlMove.setVisibility(View.VISIBLE);
						cb.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								DataList<NewsCategory> ncList = (DataList<NewsCategory>) dslvBinder.getDataSource();
								NewsCategory c = ncList.getItem(index);
								boolean allClear = true;
								for (int i = 0; i < ncList.getItemCount(); i++) {
									if (i == index)
										continue;
									NewsCategory nc = ncList.getItem(i);
									if (nc.isVisible() == NewsCategory.VISIBLE)
										allClear = false;
								}
								if (!allClear) {
									c.setVisible(c.isVisible() == NewsCategory.VISIBLE ? NewsCategory.INVISIBLE : NewsCategory.VISIBLE);
									AnalysisUtil.recordCategoryVisible(c.getid(), c.getname(), (c.isVisible() == NewsCategory.VISIBLE) + "");


									ds.refreshVisibleCategory();
									isChanged = true;
									DBHelper.getInstance().setCtgVisible(c.getid(), c.isVisible());
									messageService.send(this, MessageTopics.CATEGORY_CHANGED);
								} else
									Toast.makeText(getAttachedObject(), getAttachedObject().getResources().getString(R.string.ctg_cannot_all_clear), Toast.LENGTH_SHORT).show();

								dslvBinder.getAdapter().notifyDataSetChanged();
							}
						});


					} else {
						cb.setVisibility(View.GONE);
						rlMove.setVisibility(View.INVISIBLE);

					}

				}
				return false;
			}
		});

		lvBinder = new ListViewBinder(visibleItemBinder);

		dslvBinder = new ListViewBinder(itemBinder);
	}

	@Override
	public void run(Object... args) {
		App.get().startActivity(CategorySettingActivity.class);
	}

	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);
		isChanged = false;
		/*

		dslvBinder.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
			@Override
			public void process(Object sender, ItemEventArgs args) {
				Log.e("DSLV", "DSLV click " + args.getIndex());

				DataList<NewsCategory> ncList = (DataList<NewsCategory>) dslvBinder.getDataSource();
				NewsCategory c = ncList.getItem(args.getIndex());
				boolean allClear = true;
				for (int i = 0; i < ncList.getItemCount(); i++) {
					if (i == args.getIndex())
						continue;
					NewsCategory nc = ncList.getItem(i);
					if (nc.isVisible() == NewsCategory.VISIBLE)
						allClear = false;
				}
				if (!allClear) {
					c.setVisible(c.isVisible() == NewsCategory.VISIBLE ? NewsCategory.INVISIBLE : NewsCategory.VISIBLE);
					DBHelper.getInstance().setCtgVisible(c.getid(),c.isVisible());
					ds.refreshVisibleCategory();
					messageService.send(this, MessageTopics.CATEGORY_CHANGED);
				} else
					Toast.makeText(getAttachedObject(), getAttachedObject().getResources().getString(R.string.ctg_cannot_all_clear), Toast.LENGTH_SHORT).show();

				dslvBinder.getAdapter().notifyDataSetChanged();
			}
		});*/

		dslv = ViewHolder.get(target, R.id.cs_list).getView();
		lv =  ViewHolder.get(target, R.id.cs_visible_list).getView();

		dslvController = buildController(dslv);

		dslv.setFloatViewManager(dslvController);
		dslv.setOnTouchListener(dslvController);
		dslv.setDropListener(onDrop);
		dslv.setDragEnabled(editMode);
		dslvBinder.bind(dslv, ds.getAllCategories());
		lvBinder.bind(lv, ds.getAllVisibleCategories());




		final View editBtn = ViewHolder.get(getAttachedObject(), R.id.cs_action_bar_edit).getView();
		final View doneBtn = ViewHolder.get(getAttachedObject(), R.id.cs_action_bar_done).getView();
		final View backBtn = ViewHolder.get(getAttachedObject(), R.id.cs_back).getView();
		editMode = true;
		editBtn.setVisibility(View.GONE);
		doneBtn.setVisibility(View.VISIBLE);
		dslvBinder.getAdapter().notifyDataSetChanged();
		dslv.setDragEnabled(true);
		lv.setVisibility(View.GONE);
		dslv.setVisibility(View.VISIBLE);


		editBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editMode = true;
				editBtn.setVisibility(View.GONE);
				doneBtn.setVisibility(View.VISIBLE);
				dslvBinder.getAdapter().notifyDataSetChanged();
				dslv.setDragEnabled(true);
				lv.setVisibility(View.GONE);
				dslv.setVisibility(View.VISIBLE);

			}
		});
		doneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*editMode = false;
				doneBtn.setVisibility(View.GONE);
				editBtn.setVisibility(View.VISIBLE);
				ds.saveCategories();
				dslvBinder.getAdapter().notifyDataSetChanged();
				dslv.setDragEnabled(false);


				ds.refreshVisibleCategory();
				lv.setVisibility(View.VISIBLE);

				dslv.setVisibility(View.GONE);*/
				doBackAction();
				if(refTarget.get()!=null)
					refTarget.get().finish();

			}
		});
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doBackAction();
				if(refTarget.get()!=null)
					refTarget.get().finish();
			}
		});



			/*
		View iv_ctg_restore = ViewHolder.get(getAttachedObject(), R.id.iv_ctg_restore).getView();
		iv_ctg_restore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ds.restoreCategories();
				ds.refreshVisibleCategory();
				dslvBinder.getAdapter().notifyDataSetChanged();
				messageService.send(this, MessageTopics.CATEGORY_CHANGED);

			}
		});*/

	}

	@Override
	public boolean onBack() {
		doBackAction();
		return super.onBack();
	}

	@Override
	public void onShow() {
		NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_cs_action_bar);
		UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
	}

	private DragSortListView.DropListener onDrop =
			new DragSortListView.DropListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void drop(int from, int to) {
					if (from != to) {
						DragSortListView list = dslv;
						NewsCategory item = (NewsCategory) dslvBinder.getAdapter().getItem(from);
						DataList<NewsCategory> itemList = (DataList<NewsCategory>) dslvBinder.getDataSource();
						itemList.removeItemAt(from);
						itemList.insertItem(to, item);

						AnalysisUtil.recordCategoryOrder(item.getid(),item.getname(),(to+1)+"");

						dslvBinder.getAdapter().notifyDataSetChanged();
						isChanged = true;
						list.moveCheckState(from, to);
						//messageService.send(this, MessageTopics.CATEGORY_CHANGED);

					}
				}
			};


	public boolean dslvSortEnabled = true;
	public boolean dslvDragEnabled = true;
	public int dslvDragStartMode = DragSortController.ON_DOWN;

	public DragSortController buildController(DragSortListView dslv) {
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		controller.setSortEnabled(dslvSortEnabled);
		controller.setDragInitMode(dslvDragStartMode);

		return controller;
	}


	private void doBackAction()
	{
		ds.saveCategories();
		if(isChanged)
		  messageService.send(getAttachedObject() == null ? App.get() : getAttachedObject(), MessageTopics.CATEGORY_CHANGED);
	}


}





