package com.gmobi.poponews.cases.favorite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.NativeAd;
import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.TimeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.PopoDialog;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ValueBinderSelector;
import com.momock.binder.ViewBinder.Setter;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.ViewHolder;
import com.momock.util.Logger;
import com.reach.IAdItem;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

public class FavoriteCase extends Case<CaseActivity> {

	public FavoriteCase(String name) {
		super(name);
	}

//	public FavoriteCase(ICase<?> parent) {
//		super(parent);
//	}

	@Inject
	IDataService dataService;
	@Inject
	Resources res;
	@Inject
	IRemoteService remoteService;



	ComposedItemBinder newsBinder;
	ListViewBinder lvBinder;
	HashMap<Integer, Boolean> isCheckMap = new HashMap<>();
	PopoDialog removeDialog;

	@Override
	public void onCreate() {

		newsBinder = createBinder();

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
		App.get().startActivity(FavoriteActivity.class);
	}


	private void removeFavorites() {
		DataList<NewsItem> l = (DataList<NewsItem>) dataService.getFavList();
		DataList<NewsItem> delList = new DataList<>();
		DBHelper dh = DBHelper.getInstance();

		//需要删除3个地方：Checkmap,Database,favlist
		for (int i = 0; i < l.getItemCount(); i++) {
			if (isCheckMap.containsKey(i) && isCheckMap.get(i)) {
				delList.addItem(l.getItem(i));
				isCheckMap.remove(i);
			}
		}

		for (int i = 0; i < delList.getItemCount(); i++) {
			dh.setFav(delList.getItem(i).get_id(), false);
			l.removeItem(delList.getItem(i));
		}
	}


	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<>(target);


		removeDialog = new PopoDialog(refTarget.get(),
				R.style.PopoDialogStyle, R.layout.dialog_query_fav,
				false,
				R.id.positiveButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				removeDialog.dismiss();
				removeFavorites();
			}
		},
				R.id.negativeButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				removeDialog.dismiss();
			}
		});


		lvBinder.bind(ViewHolder.get(refTarget.get(), R.id.lv_favlist), dataService.getFavList());


		View ivBackBtn = ViewHolder.get(target, R.id.fav_back).getView();

		ivBackBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (refTarget.get() != null) {
					UiHelper.setFavEditMode(false);

					if (isCheckMap != null)
						isCheckMap.clear();

					lvBinder.getAdapter().notifyDataSetChanged();

					Activity currActivity = App.get().getCurrentActivity();
					if (currActivity != null)
						currActivity.finish();
				}
			}
		});

		View ivEditBtn = ViewHolder.get(target, R.id.fav_action_bar_edit).getView();

		ivEditBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean editmode = UiHelper.isFavEditMode();
				UiHelper.setFavEditMode(!editmode);
				lvBinder.getAdapter().notifyDataSetChanged();

				View ivDelBtn = ViewHolder.get(refTarget.get(), R.id.fav_action_bar_del).getView();
				if (UiHelper.isFavEditMode()) {
					ivDelBtn.setVisibility(View.VISIBLE);
				} else {
					ivDelBtn.setVisibility(View.GONE);
				}

			}
		});

		View ivDelBtn = ViewHolder.get(refTarget.get(), R.id.fav_action_bar_del).getView();
		ivDelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean select=false;
				if (isCheckMap.size() > 0)
				{
					Iterator<Map.Entry<Integer, Boolean>> entries = isCheckMap.entrySet().iterator();


					while (entries.hasNext()) {
						Map.Entry<Integer, Boolean> entry = entries.next();
						if(entry.getValue())
							select =true;
					}
				}


				if (!select) {
					Toast.makeText(refTarget.get(), res.getString(R.string.fav_select_empty), Toast.LENGTH_SHORT).show();
				} else {
					if (removeDialog != null && !removeDialog.isShowing())
						removeDialog.show();
				}
			}
		});


	}


	@Override
	public void onShow() {

		NightModeUtil.setViewColor(getAttachedObject(), R.id.lv_favlist,
				getAttachedObject().getResources().getColor(R.color.bg_white),
				getAttachedObject().getResources().getColor(R.color.bg_black_night));

		NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_fav_action_bar);
		lvBinder.getAdapter().notifyDataSetChanged();

		UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));


	}


	@Override
	public boolean onBack() {
		UiHelper.setFavEditMode(false);
		if (isCheckMap != null)
			isCheckMap.clear();
		return super.onBack();
	}


	private ComposedItemBinder createBinder()
	{
		newsBinder = new ComposedItemBinder();
		Setter newsRegularSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {

				boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false : true;
				Context ctx = getAttachedObject();

				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));


				final NewsItem n = dataService.getFavList().getItem(index);

				if (view == null)
					return false;

				if (view.getId() == R.id.news_img) {
					TextView tv = (TextView) parent.findViewById(R.id.news_title);
					if (val == null || val.equals("")) {
						((ImageView) view).setImageResource(R.drawable.news_nonpicture);

						//view.setVisibility(View.GONE);
						//tv.setTextSize(18);
					} else {
						view.setVisibility(View.VISIBLE);
						tv.setTextSize(16);
					}


					boolean haveread = DBHelper.getInstance().getRead(n.get_id());

					if (!nightmode) {
						if (haveread) {
							tv.setTextColor(ctx.getResources().getColor(R.color.bg_grey));
						} else {
							tv.setTextColor(ctx.getResources().getColor(R.color.bg_black));
						}
					} else
						tv.setTextColor(ctx.getResources().getColor(R.color.bg_white_night));


					final CheckBox cb = (CheckBox) parent.findViewById(R.id.fav_cb);
					if (cb != null) {
						if (isCheckMap != null && isCheckMap.containsKey(index)) {
							Log.e("fav", "containsKey index=" + index);
							cb.setChecked(isCheckMap.get(index));
						} else {
							Log.e("fav", "not containsKey index=" + index);
							cb.setChecked(false);
						}

						if (UiHelper.isFavEditMode()) {
							final int p = index;
							cb.setVisibility(View.VISIBLE);
							cb.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									CheckBox cb = (CheckBox) v;
									isCheckMap.put(p, cb.isChecked());

								}
							});

						} else
							cb.setVisibility(View.GONE);
					}

					return false;

				} else if (view.getId() == R.id.news_date) {

					((TextView) view).setText(TimeUtil.getInstance().getLastTime(n.getReleaseTime()));
					return true;
				}

				return false;
			}

		};


		ItemBinder binder1 = new ItemBinder(
				R.layout.fav_news_regular_right,
				new int[]{R.id.news_title, R.id.news_img, R.id.news_source, R.id.news_date},
				new String[]{"title", "listpreview|" + R.drawable.homepage_newslist_nonpicture, "p_name","dummy"});

		binder1.addSetter(newsRegularSetter);





		//三图模式

		Setter newsImageSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, final int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {

				boolean nightmode = NightModeUtil.isNightMode();
				Context ctx = getAttachedObject();
				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				final NewsItem n = dataService.getFavList().getItem(index);
				if(view == null)
					return false;

				if (view.getId() == R.id.news_img_title) {

					boolean haveread = DBHelper.getInstance().getRead(n.get_id());

					if (!nightmode) {
						if (haveread) {
							((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_grey));
						} else {
							((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_black));
						}
					} else
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_white_night));


					CheckBox cb = (CheckBox) parent.findViewById(R.id.fav_cb);
					if (cb != null) {
						if (isCheckMap != null && isCheckMap.containsKey(index)) {
							Log.e("fav", "containsKey index=" + index);
							cb.setChecked(isCheckMap.get(index));
						} else {
							Log.e("fav", "not containsKey index=" + index);
							cb.setChecked(false);
						}

						if (UiHelper.isFavEditMode()) {
							cb.setVisibility(View.VISIBLE);
							cb.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									CheckBox cb = (CheckBox) v;
									isCheckMap.put(index, cb.isChecked());

								}
							});

						} else
							cb.setVisibility(View.GONE);
					}


					return false;

				}
				else if (view.getId() == R.id.news_date) {

					((TextView) view).setText(TimeUtil.getInstance().getLastTime(n.getReleaseTime()));
					return true;
				}

				return false;
			}

		};

		ItemBinder binder2 = new ItemBinder(
				R.layout.fav_news_image_ext,
				new int[]{R.id.news_img_title, R.id.news_img1, R.id.news_img2, R.id.news_img3,R.id.news_source, R.id.news_date},
				new String[]{"title", "preview1|" + R.drawable.homepage_newslist_nonpicture, "preview2|" + R.drawable.homepage_newslist_nonpicture, "preview3|" + R.drawable.homepage_newslist_nonpicture,"p_name","dummy"});
		binder2.addSetter(newsImageSetter);




		//大图模式
		ItemBinder binder3 = new ItemBinder(
				R.layout.fav_news_pin_ext,
				new int[]{R.id.news_title, R.id.news_img,R.id.news_source, R.id.news_date},
				new String[]{"title", "pinpreview|" + R.drawable.news_featured_nonpicture,"p_name","dummy"});
		Setter pinSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, final int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {
				Log.e("Binder", "User Bind 3");
				Context ctx = getAttachedObject();
				boolean nightmode = NightModeUtil.isNightMode();
				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				final NewsItem n = dataService.getFavList().getItem(index);
				if (view.getId() == R.id.news_title) {
					if (!nightmode)
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_black));
					else
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_white_night));

					CheckBox cb = (CheckBox) parent.findViewById(R.id.fav_cb);
					if (cb != null) {
						if (isCheckMap != null && isCheckMap.containsKey(index)) {
							Log.e("fav", "containsKey index=" + index);
							cb.setChecked(isCheckMap.get(index));
						} else {
							Log.e("fav", "not containsKey index=" + index);
							cb.setChecked(false);
						}

						if (UiHelper.isFavEditMode()) {
							cb.setVisibility(View.VISIBLE);
							cb.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									CheckBox cb = (CheckBox) v;
									isCheckMap.put(index, cb.isChecked());

								}
							});

						} else
							cb.setVisibility(View.GONE);
					}


					return false;
				}
				else if (view.getId() == R.id.news_date) {

					((TextView) view).setText(TimeUtil.getInstance().getLastTime(n.getReleaseTime()));
					return true;
				}
				return false;
			}

		};
		binder3.addSetter(pinSetter);









		newsBinder.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_NORMAL_RIGHT), binder1);
		newsBinder.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_MULTIPICS), binder2);
		newsBinder.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_LARGEPIC), binder3);
		newsBinder.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_TITLEONLY), binder1);
		return newsBinder;
	}

}





