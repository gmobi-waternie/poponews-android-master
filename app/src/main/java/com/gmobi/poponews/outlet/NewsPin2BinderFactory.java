package com.gmobi.poponews.outlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.momock.app.App;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ValueBinderSelector;
import com.momock.binder.ViewBinder.Setter;
import com.momock.data.IDataList;
import com.reach.IAd;
import com.reach.IAdItem;

public class NewsPin2BinderFactory {

	private Context ctx;
	private IDataService ds;

	private String cid;

	private boolean isSupportFeature;

	public NewsPin2BinderFactory(Context ctx)
	{
		this.ctx = ctx;
		
		ds = App.get().getService(IDataService.class);
		this.isSupportFeature = true;
	}

	public NewsPin2BinderFactory setFeatureSupport(boolean s)
	{
		this.isSupportFeature = s;
		return this;
	}

	
	public NewsPin2BinderFactory setCategoryId(String cid)
	{
		this.cid = cid;
		return this; 
	}
	
	private IDataList<NewsItem> getDataSource()
	{
		NewsCategory c = ds.getCategoryById(cid);
		return ds.getNewsInCategory(cid,c.isCache());
	}
	
	public ComposedItemBinder build()
	{
		ComposedItemBinder cib = new ComposedItemBinder();
		
		Setter newsRegularSetter = new Setter(){

			@SuppressLint("ResourceAsColor") @Override
			public boolean onSet(View view, String viewProp, int index,
					String key, Object val, View parent,
					IContainerBinder container) {

				boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false : true;
				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_pinlist));

				RelativeLayout rl_news = (RelativeLayout) parent.findViewById(R.id.rl_news_pin2);
				RelativeLayout rl_ad = (RelativeLayout) parent.findViewById(R.id.rl_news_pin2_ad);
				RelativeLayout rl_ad_video = (RelativeLayout) parent.findViewById(R.id.rl_news_pin2_ad_video);
				RelativeLayout rl_ad_facebook = (RelativeLayout) parent.findViewById(R.id.facebook_ad_container);

				NewsItem n = getDataSource().getItem(index);

				if (n.getType() == NewsItem.NEWS_TYPE_AD || n.getType() == NewsItem.NEWS_TYPE_FACEBOOKAD) {

					rl_news.setVisibility(View.GONE);

					if (view.getId() == R.id.news_title) {

						if (n.getAdObj() instanceof IAdItem) {
							IAdItem aditem = (IAdItem) n.getAdObj();
							if (aditem != null) {
								if (aditem.has(IAdItem.VIDEO)) {
									rl_ad.setVisibility(View.GONE);
									rl_ad_facebook.setVisibility(View.GONE);
									rl_ad_video.setVisibility(View.VISIBLE);


									rl_ad_video.setBackgroundResource(nightmode ? R.color.bg_black_night :
											R.color.bg_pinlist);
									rl_ad_video.findViewById(R.id.ll_news_pin2_ad_video).setBackgroundResource(nightmode ? R.color.bg_pinlist_night :
											R.drawable.box);

									TextView tvAd = (TextView) rl_ad_video.findViewById(R.id.tv_ad_video);
									if (!NightModeUtil.isNightMode()) {
										tvAd.setTextColor(ctx.getResources().getColor(R.color.bg_black));
									} else
										tvAd.setTextColor(ctx.getResources().getColor(R.color.bg_white_night));

									aditem.bind(rl_ad_video.findViewById(R.id.ll_news_pin2_ad_video), new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
											new int[]{R.id.tv_ad_video, R.id.pin2_vpPlayer});
								} else if (aditem.has(IAdItem.IMAGE)) {
									rl_ad.setVisibility(View.VISIBLE);
									rl_ad_video.setVisibility(View.GONE);
									rl_ad_facebook.setVisibility(View.GONE);

									rl_ad.setBackgroundResource(nightmode ? R.color.bg_black_night :
											R.color.bg_pinlist);
									rl_ad.findViewById(R.id.ll_news_pin2_ad).setBackgroundResource(nightmode ? R.color.bg_pinlist_night :
											R.drawable.box);

									TextView tvAd = (TextView) rl_ad.findViewById(R.id.tv_ad);
									if (!NightModeUtil.isNightMode()) {
										tvAd.setTextColor(ctx.getResources().getColor(R.color.bg_black));
									} else
										tvAd.setTextColor(ctx.getResources().getColor(R.color.bg_white_night));


									aditem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
											new int[]{R.id.tv_ad, R.id.iv_ad});
								} else {
									rl_ad.setVisibility(View.VISIBLE);
									rl_ad_video.setVisibility(View.GONE);
									rl_ad_facebook.setVisibility(View.GONE);

									rl_ad.setBackgroundResource(nightmode ? R.color.bg_black_night :
											R.color.bg_pinlist);
									rl_ad.findViewById(R.id.ll_news_pin2_ad).setBackgroundResource(nightmode ? R.color.bg_pinlist_night :
											R.drawable.box);


									TextView tvAd = (TextView) rl_ad.findViewById(R.id.tv_ad);
									if (!NightModeUtil.isNightMode()) {
										tvAd.setTextColor(ctx.getResources().getColor(R.color.bg_black));
									} else
										tvAd.setTextColor(ctx.getResources().getColor(R.color.bg_white_night));




									aditem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.ICON},
											new int[]{R.id.tv_ad, R.id.iv_ad});
								}
							}
						}
						else
						{
							rl_ad.setVisibility(View.GONE);
							rl_ad_video.setVisibility(View.GONE);
							rl_ad_facebook.setVisibility(View.VISIBLE);
							rl_ad_facebook.setBackgroundResource(nightmode ? R.color.bg_black_night :
									R.color.bg_pinlist);

							NativeAd aditem = (NativeAd) n.getAdObj();
							inflateFacebookView(aditem,rl_ad_facebook);
						}



						return true;
					}
				} else {
					rl_news.setVisibility(View.VISIBLE);
					rl_ad.setVisibility(View.GONE);
					rl_ad_video.setVisibility(View.GONE);

					if (view.getId() == R.id.news_title) {
						((View)view.getParent()).setBackgroundResource(nightmode ? R.color.bg_black_night :
								R.drawable.box);
						TextView tv = (TextView) view;

						if (val.equals("")) {
							view.setVisibility(View.GONE);
							tv.setTextSize(18);
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
						if (ev.getMainEmoVote() > 0) {
							view.setVisibility(View.VISIBLE);
							int pc = ev.getMainEmoVote() * 100 / ev.getTotalVote();
							((TextView) view).setText(pc + "%");
						} else {
							view.setVisibility(View.INVISIBLE);
						}
						return true;
					}



				}
				return false;
			}
			
		};
		
		
		ItemBinder binder1 = new ItemBinder(
				R.layout.news_pin2,
				new int[] { R.id.news_title, R.id.news_img,R.id.news_source,R.id.news_fav_num, R.id.tv_news_emo, R.id.iv_news_emo},
                new String[] { "title", "pin2preview|"+R.drawable.news_featured_nonpicture,"p_name","fav", "dummy", "dummy"});
		
		binder1.addSetter(newsRegularSetter);



		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_REGULAR), binder1);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_IMAGE), binder1);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_AD), binder1);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FACEBOOKAD), binder1);
		if(isSupportFeature)
			cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FEATURED), binder1);
		return cib;
	}


	private void inflateFacebookView(NativeAd facebookAd,ViewGroup container)
	{
		if(container.getChildCount() > 0)
			return;
		container.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup adView = (ViewGroup)inflater.inflate(R.layout.facebook_ad_pin2, container);
		facebookAd.unregisterView();

		// Create native UI using the ad metadata.

		TextView nativeAdTitle = (TextView)adView.findViewById(R.id.native_ad_title);
		MediaView nativeAdMedia = (MediaView)adView.findViewById(R.id.native_ad_media);

		if (!NightModeUtil.isNightMode()) {
			nativeAdTitle.setTextColor(ctx.getResources().getColor(R.color.bg_black));
		} else
			nativeAdTitle.setTextColor(ctx.getResources().getColor(R.color.bg_white_night));

		NativeAd.Image adCoverImage = facebookAd.getAdCoverImage();
		int bannerWidth = adCoverImage.getWidth();
		int bannerHeight = adCoverImage.getHeight();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;

		/*LinearLayout.LayoutParams llp =
				new LinearLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT
				);


		nativeAdMedia.setLayoutParams(llp);*/
		nativeAdMedia.setNativeAd(facebookAd);

		// Setting the Text.
		nativeAdTitle.setText(facebookAd.getAdTitle());


//		AdChoicesView adChoicesView = null;
//		if (adChoicesView == null) {
//			adChoicesView = new AdChoicesView(ctx, aditem);
//			adView.addView(adChoicesView, 0);
//		}


		facebookAd.registerViewForInteraction(adView);
	}
	
}
