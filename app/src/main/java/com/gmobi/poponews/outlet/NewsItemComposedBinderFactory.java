package com.gmobi.poponews.outlet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.ads.NativeAd;
import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.TimeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.MenuInteresting;
import com.momock.app.App;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ValueBinderSelector;
import com.momock.binder.ViewBinder.Setter;
import com.momock.data.IDataList;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;
import com.reach.IAdItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class NewsItemComposedBinderFactory {

	private Context ctx;
	private IDataService ds;
	private String cid;
	private int type;

	private boolean isHot = false;
	private String name;

	private boolean isShowUni = true;

	public static final int TYPE_LIST = 1;
	public static final int TYPE_PUSH = 2;
	public static final int TYPE_FAV = 3;
	public static final int TYPE_READ = 4;

	public NewsItemComposedBinderFactory(Context ctx, int type) {
		this.ctx = ctx;
		this.type = type;
		ds = App.get().getService(IDataService.class);
		if(UiHelper.isCherryVersion())
		{
			this.isHot = false;
			this.isShowUni = false;
		}
	}



	public NewsItemComposedBinderFactory setCategoryId(String cid) {
		this.cid = cid;
		NewsCategory c = ds.getCategoryById(cid);
		if(c!=null) {
			this.isHot = c.isHot();
			this.name = c.getname();
		}

		if(UiHelper.isCherryVersion())
		{
			this.isHot = false;
		}

		return this;
	}

	public NewsItemComposedBinderFactory setShowUni(boolean s) {
		if(UiHelper.isCherryVersion())
		{
			this.isShowUni = false;
		}
		else {
			this.isShowUni = s;
		}
		return this;
	}

	private IDataList<NewsItem> getDataSource() {
		switch (type) {
			case TYPE_LIST:
				NewsCategory c = ds.getCategoryById(cid);
				return ds.getNewsInCategory(cid,c.isCache());
			case TYPE_FAV:
				return ds.getFavList();
			case TYPE_PUSH:
				return ds.getPushList();
			case TYPE_READ:
				return ds.getReadList();
			default:
				break;
		}
		return null;
	}



	public ComposedItemBinder build() {
		ComposedItemBinder cib = new ComposedItemBinder();


		//正常模式
		Setter newsRegularSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {

				boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false : true;


				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));


				NewsItem n = getDataSource().getItem(index);

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

					return false;

				} else if (view.getId() == R.id.news_date) {

					((TextView) view).setText(TimeUtil.getInstance().getLastTime(n.getReleaseTime()));
					return true;
				}
				else if (view.getId() == R.id.news_tag) {
//					if(isHot) {
//						((TextView) view).setText(name);
//						view.setVisibility(View.VISIBLE);
//					}
//					else
						view.setVisibility(View.GONE);

					return true;
				} else if (view.getId() == R.id.news_uninteresting){
					initInterestPopup(view,n);
					return true;
				}
				
				return false;
			}

		};


		ItemBinder binder1 = new ItemBinder(
				R.layout.news_regular_right,
				new int[]{R.id.news_title, R.id.news_img, R.id.news_source, R.id.news_date,R.id.news_tag,R.id.news_uninteresting},
				new String[]{"title", "listpreview|" + R.drawable.homepage_newslist_nonpicture, "p_name","dummy","dummy","dummy"});

		binder1.addSetter(newsRegularSetter);





		//三图模式

		Setter newsImageSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {

				boolean nightmode = NightModeUtil.isNightMode();

				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				NewsItem n = getDataSource().getItem(index);
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


					return false;

				}
				else if (view.getId() == R.id.news_date) {

					((TextView) view).setText(TimeUtil.getInstance().getLastTime(n.getReleaseTime()));
					return true;
				}
				else if (view.getId() == R.id.news_tag) {
//					if(isHot) {
//						((TextView) view).setText(name);
//						view.setVisibility(View.VISIBLE);
//					}
//					else
						view.setVisibility(View.GONE);
					return true;
				}else if (view.getId() == R.id.news_uninteresting){
					initInterestPopup(view,n);
					return true;
				}

				return false;
			}

		};

		ItemBinder binder2 = new ItemBinder(
				R.layout.news_image_ext,
				new int[]{R.id.news_img_title, R.id.news_img1, R.id.news_img2, R.id.news_img3,R.id.news_source, R.id.news_date,R.id.news_tag,R.id.news_uninteresting},
				new String[]{"title", "preview1|" + R.drawable.homepage_newslist_nonpicture, "preview2|" + R.drawable.homepage_newslist_nonpicture, "preview3|" + R.drawable.homepage_newslist_nonpicture,"p_name","dummy","dummy","dummy"});
		binder2.addSetter(newsImageSetter);




		//大图模式
		ItemBinder binder3 = new ItemBinder(
				R.layout.news_pin_ext,
				new int[]{R.id.news_title, R.id.news_img,R.id.news_source, R.id.news_date,R.id.news_tag,R.id.news_uninteresting},
				new String[]{"title", "pinpreview|" + R.drawable.news_featured_nonpicture,"p_name","dummy","dummy","dummy"});
		Setter pinSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {
				Log.e("Binder", "User Bind 3");
				boolean nightmode = NightModeUtil.isNightMode();
				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				NewsItem n = getDataSource().getItem(index);
				if(view == null)
					return false;

				if (view.getId() == R.id.news_title) {
					if (!nightmode)
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_black));
					else
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_white_night));

					return false;
				}
				else if (view.getId() == R.id.news_date) {

					((TextView) view).setText(TimeUtil.getInstance().getLastTime(n.getReleaseTime()));
					return true;
				}
				else if (view.getId() == R.id.news_tag) {
//					if(isHot) {
//						((TextView) view).setText(name);
//						view.setVisibility(View.VISIBLE);
//					}
//					else
						view.setVisibility(View.GONE);
					return true;
				}else if (view.getId() == R.id.news_uninteresting){
					initInterestPopup(view,n);
					return true;
				}
				return false;
			}

		};
		binder3.addSetter(pinSetter);


		ItemBinder binder4 = new ItemBinder(
				R.layout.news_ad,
				new int[]{R.id.news_title_icon, R.id.news_title_image}, new String[]{"title", "title"});
		Setter adSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {
				Log.e("Binder", "User adSetter");
				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				if (view.getId() == R.id.news_title_icon || view.getId() == R.id.news_title_image) {
					NewsItem newsitem = getDataSource().getItem(index);
					Logger.error("[AD] Facebook list render idx=" + index);

					if (newsitem.getAdObj() instanceof IAdItem) {
						parent.findViewById(R.id.gmobi_ad_container).setVisibility(View.VISIBLE);
						parent.findViewById(R.id.facebook_ad_container).setVisibility(View.GONE);


						IAdItem aditem = (IAdItem) newsitem.getAdObj();
						if (aditem != null) {
							if (aditem.has(IAdItem.VIDEO)) {
								parent.findViewById(R.id.ll_ad_with_icon).setVisibility(View.GONE);
								parent.findViewById(R.id.ll_ad_with_image).setVisibility(View.GONE);
								parent.findViewById(R.id.ll_ad_with_video).setVisibility(View.VISIBLE);

								aditem.bind(parent.findViewById(R.id.ll_ad_with_video), new String[]{IAdItem.MEDIA_CONTAINER, IAdItem.TITLE},
										new int[]{R.id.vpPlayer, R.id.news_title_video});
							} else if (aditem.has(IAdItem.IMAGE)) {
								parent.findViewById(R.id.ll_ad_with_icon).setVisibility(View.GONE);
								parent.findViewById(R.id.ll_ad_with_image).setVisibility(View.VISIBLE);
								parent.findViewById(R.id.ll_ad_with_video).setVisibility(View.GONE);

								aditem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							} else {
								parent.findViewById(R.id.ll_ad_with_icon).setVisibility(View.VISIBLE);
								parent.findViewById(R.id.ll_ad_with_image).setVisibility(View.GONE);
								parent.findViewById(R.id.ll_ad_with_video).setVisibility(View.GONE);
								aditem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.ICON, IAdItem.RATE},
										new int[]{R.id.news_title_icon, R.id.iv_ad_icon, R.id.rb_ad});
							}

						}
					}
					else
					{
						parent.findViewById(R.id.gmobi_ad_container).setVisibility(View.GONE);
						parent.findViewById(R.id.facebook_ad_container).setVisibility(View.VISIBLE);
						NativeAd aditem = (NativeAd) newsitem.getAdObj();
						if (aditem != null) {
							inflateFacebookView(aditem,parent.findViewById(R.id.ad_unit));
						}

					}

					return true;
				}
				return false;
			}

		};
		binder4.addSetter(adSetter);



		ItemBinder binder5 = new ItemBinder(
				R.layout.facebook_ad,
				new int[] { R.id.native_ad_title,R.id.native_ad_media}, new String[] { "title","listpreview|" + R.drawable.news_featured_nonpicture});
		Setter facebookadSetter = new Setter(){

			@SuppressLint("ResourceAsColor") @Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {

				Log.e("Binder", "User adSetter parent="+parent.toString());

				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				if(view.getId() == R.id.native_ad_title)
				{
					NewsItem newsitem = getDataSource().getItem(index);
					NativeAd aditem = (NativeAd)newsitem.getAdObj();
					if (aditem != null) {
						inflateFacebookView(aditem, (ViewGroup) parent);
					}

				}
				return false;
			}

		};
		binder5.addSetter(facebookadSetter);


		cib.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_NORMAL_RIGHT), binder1);
		cib.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_MULTIPICS), binder2);
		cib.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_LARGEPIC), binder3);
		cib.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_TITLEONLY), binder1);
		cib.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_AD), binder4);
		cib.addBinder(new ValueBinderSelector("LayoutType", NewsItem.NEWS_LAYOUT_FACEBOOKAD), binder5);

		return cib;
	}



	private void inflateFacebookView(NativeAd facebookAd,View adView)
	{
		Logger.error("[AD] inflateFacebookView");
		facebookAd.unregisterView();

		facebookAd.registerViewForInteraction(adView);
	}


	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void initInterestPopup(View view,NewsItem n) {
		if(isShowUni)
			view.setVisibility(View.VISIBLE);
		else
			view.setVisibility(View.GONE);

		if(NightModeUtil.isNightMode())
			((ImageView)view).setImageDrawable(ctx.getResources().getDrawable(R.drawable.news_item_close_night));
		else
			((ImageView)view).setImageDrawable(ctx.getResources().getDrawable(R.drawable.news_item_close));

		MenuInteresting menu = new MenuInteresting(view, n);
	}

}
