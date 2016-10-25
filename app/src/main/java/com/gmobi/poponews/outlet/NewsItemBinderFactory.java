package com.gmobi.poponews.outlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.util.DBHelper;

import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.widget.MenuInteresting;
import com.gmobi.poponews.widget.MenuPopupWindow;
import com.gmobi.poponews.widget.MenuSetting;
import com.momock.app.App;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ValueBinderSelector;
import com.momock.binder.ViewBinder.Setter;
import com.momock.data.IDataList;
import com.momock.holder.ViewHolder;
import com.momock.service.ImageService;
import com.momock.util.ImageHelper;
import com.momock.util.Logger;
import com.reach.IAdItem;

public class NewsItemBinderFactory {

	private Context ctx;
	private IDataService ds;
	private int type;
	private String cid;
	private PopupWindow mPopupWindow;

	private boolean isSupportFeature;

	public static final int TYPE_LIST = 1;
	public static final int TYPE_PUSH = 2;
	public static final int TYPE_FAV = 3;
	public static final int TYPE_READ = 4;


	public NewsItemBinderFactory(Context ctx, int type) {
		this.ctx = ctx;
		this.type = type;

		this.isSupportFeature = true;
		ds = App.get().getService(IDataService.class);
	}

	public NewsItemBinderFactory setFeatureSupport(boolean s) {
		this.isSupportFeature = s;
		return this;
	}

	public NewsItemBinderFactory setCategoryId(String cid) {
		this.cid = cid;
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

		Setter newsRegularSetter = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, final View parent,
								 IContainerBinder container) {

				boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false : true;


//					parent.setBackgroundColor((index % 2 == 0) ? ctx.getResources().getColor(R.color.bg_black_night) :
//							ctx.getResources().getColor(R.color.bg_white));
				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));


				NewsItem n = getDataSource().getItem(index);

				if(view== null)
					return false;

				if (view.getId() == R.id.news_img) {
					TextView tv = (TextView) parent.findViewById(R.id.news_title);
					if (val == null || val.equals("")) {
						((ImageView)view).setImageResource(R.drawable.news_nonpicture);
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

				} else if (view.getId() == R.id.news_fav_num) {

					Integer favNum = (Integer) val;
					if (favNum == 0) {
						boolean fav = DBHelper.getInstance().getFav(n.get_id());
						if (fav)
							favNum = 1;

					} else if (favNum < 0)
						favNum = 0;

					((TextView) view).setText(favNum + "");

					boolean fav = DBHelper.getInstance().getFav(n.get_id());
					ImageView iv = (ImageView) parent.findViewById(R.id.news_fav_star);
					iv.setImageResource(fav ? R.drawable.star_on : R.drawable.star_off);
					return true;
				} else if (view.getId() == R.id.iv_news_emo) {

					EmoVote ev = n.getEmo();
					if (ev.getMainEmoVote() > 0) {
						view.setVisibility(View.VISIBLE);
						int ic = EmoVote.getSmallVoteIcon(ev.getMainEmo());
						((ImageView) view).setImageResource(ic);
					} else {
						view.setVisibility(View.INVISIBLE);
					}
					return true;
				} else if (view.getId() == R.id.tv_news_emo) {

					EmoVote ev = n.getEmo();
					if(ev == null)
						return true;
					if (ev.getMainEmoVote() > 0) {
						view.setVisibility(View.VISIBLE);
						int pc = ev.getMainEmoVote() * 100 / ev.getTotalVote();
						((TextView) view).setText(pc + "%");
					} else {
						view.setVisibility(View.INVISIBLE);
					}
					return true;
				}


				/*
				if(view.getId() == R.id.news_time)
				{
					long utcTime = (Long)val;
					((TextView)view).setText(TimeUtil.getInstance().getDataFormatStr(utcTime));
					return true;
				}
				*/


				return false;
			}

		};


		ItemBinder binder1 = new ItemBinder(
				R.layout.news_regular,
				new int[]{R.id.news_title, R.id.news_img, R.id.news_source, R.id.news_fav_num, R.id.tv_news_emo, R.id.iv_news_emo},
				new String[]{"title", "listpreview|" + R.drawable.homepage_newslist_nonpicture, "p_name", "fav", "dummy", "dummy"});

		binder1.addSetter(newsRegularSetter);


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


				return false;
			}

		};

		ItemBinder binder2 = new ItemBinder(
				R.layout.news_image,
				new int[]{R.id.news_img_title, R.id.news_img1, R.id.news_img2, R.id.news_img3}, new String[]{"title", "preview1|" + R.drawable.homepage_newslist_nonpicture, "preview2|" + R.drawable.homepage_newslist_nonpicture, "preview3|" + R.drawable.homepage_newslist_nonpicture});
		binder2.addSetter(newsImageSetter);


		ItemBinder binder3 = new ItemBinder(
				R.layout.news_featured,
				new int[]{R.id.news_title, R.id.news_img}, new String[]{"title", "listpreview|" + R.drawable.news_featured_nonpicture});
		Setter test = new Setter() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {
				Log.e("Binder", "User Bind 3");
				boolean nightmode = NightModeUtil.isNightMode();
				if (view.getId() == R.id.news_title) {
					if (!nightmode)
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_white));
					else
						((TextView) view).setTextColor(ctx.getResources().getColor(R.color.bg_white_night));

					return false;
				}
				return false;
			}

		};
		binder3.addSetter(test);


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


		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_REGULAR), binder1);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_IMAGE), binder2);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_AD), binder4);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FACEBOOKAD), binder5);

		if (isSupportFeature) {
			cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FEATURED), binder3);
		} else
			cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FEATURED), binder2);

		return cib;
	}


	private void inflateFacebookView(NativeAd facebookAd,View adView)
	{
		Logger.error("[AD] inflateFacebookView");

		


		facebookAd.unregisterView();


		// Create native UI using the ad metadata.
/*
		TextView nativeAdTitle = (TextView)adView.findViewById(R.id.native_ad_title);
		MediaView nativeAdMedia = (MediaView)adView.findViewById(R.id.native_ad_media);

		NativeAd.Image adCoverImage = facebookAd.getAdCoverImage();
		int bannerWidth = adCoverImage.getWidth();
		int bannerHeight = adCoverImage.getHeight();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		int px = DipHelper.dip2px(8);
		LinearLayout.LayoutParams llp =
				new LinearLayout.LayoutParams(
						screenWidth - 2*px,
						Math.min((int) (((double) screenWidth / (double) bannerWidth) * bannerHeight), screenHeight / 3)
				);

		llp.setMargins(px, 0, px, 0);
		nativeAdMedia.setLayoutParams(llp);
		nativeAdMedia.setNativeAd(facebookAd);

		// Setting the Text.
		nativeAdTitle.setText(facebookAd.getAdTitle());
		Logger.error("[AD] Facebook ad title="+facebookAd.getAdTitle()+",id="+facebookAd.getId());


//		AdChoicesView adChoicesView = null;
//		if (adChoicesView == null) {
//			adChoicesView = new AdChoicesView(ctx, aditem);
//			adView.addView(adChoicesView, 0);
//		}
*/

		facebookAd.registerViewForInteraction(adView);
	}

}
