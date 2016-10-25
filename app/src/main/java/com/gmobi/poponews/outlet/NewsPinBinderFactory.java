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
import android.widget.Button;
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
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.momock.app.App;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ValueBinderSelector;
import com.momock.binder.ViewBinder.Setter;
import com.momock.data.IDataList;
import com.momock.holder.ViewHolder;
import com.reach.IAdItem;

public class NewsPinBinderFactory {
	
	private Context ctx;
	private IDataService ds;
	
	private String cid;

	private boolean isSupportFeature;
	
	public NewsPinBinderFactory(Context ctx)
	{
		this.ctx = ctx;
		
		ds = App.get().getService(IDataService.class);
		this.isSupportFeature = true;
	}

	public NewsPinBinderFactory setFeatureSupport(boolean s)
	{
		this.isSupportFeature = s;
		return this;
	}

	
	public NewsPinBinderFactory setCategoryId(String cid)
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
				
				boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false :true;
				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) : 
					ctx.getResources().getColor(R.color.bg_white));
				
				
				NewsItem n = getDataSource().getItem(index);
				
				
				if(view.getId() == R.id.news_title)
				{
					TextView tv = (TextView) view;
					if(val.equals(""))
					{
						view.setVisibility(View.GONE);
						tv.setTextSize(18);
					}
					else
					{
						view.setVisibility(View.VISIBLE);
						tv.setTextSize(16);
					}
					
					
					boolean haveread = DBHelper.getInstance().getRead(n.get_id());
					if(!nightmode)
					{
						if(haveread)
						{
							tv.setTextColor(ctx.getResources().getColor(R.color.bg_grey));
						}
						else
						{
							tv.setTextColor(ctx.getResources().getColor(R.color.bg_black));
						}
					}
					else
						tv.setTextColor(ctx.getResources().getColor(R.color.bg_white_night));
					
					return false;
					
				}else if(view.getId() == R.id.news_fav_num)
				{
					
					Integer favNum = (Integer)val;
					if(favNum == 0)
					{
						boolean fav = DBHelper.getInstance().getFav(n.get_id());
						if(fav)
							favNum = 1;
						
					}
					else if(favNum < 0)
						favNum = 0;
						
					((TextView)view).setText(favNum+"");
					
					boolean fav = DBHelper.getInstance().getFav(n.get_id());	
					ImageView iv = (ImageView) parent.findViewById(R.id.news_fav_star);
					iv.setImageResource(fav ? R.drawable.star_on:R.drawable.star_off);
					return true;
				}else if(view.getId() == R.id.iv_news_emo){
                    
                    EmoVote ev = n.getEmo();
                    if(ev.getMainEmoVote() > 0){
                        view.setVisibility(View.VISIBLE);
                        int ic = EmoVote.getSmallVoteIcon(ev.getMainEmo());
                        ((ImageView)view).setImageResource(ic);
                    }else{
                        view.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }else if(view.getId() == R.id.tv_news_emo){
                    
                    EmoVote ev = n.getEmo();
                    if(ev.getMainEmoVote() > 0){
                        view.setVisibility(View.VISIBLE);
                        int pc = ev.getMainEmoVote() * 100 / ev.getTotalVote();
                        ((TextView)view).setText(pc + "%");
                    }else{
                        view.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
				
				
				return false;
			}
			
		};
		
		
		ItemBinder binder1 = new ItemBinder(
				R.layout.news_pin,
				new int[] { R.id.news_title, R.id.news_img,R.id.news_source,R.id.news_fav_num, R.id.tv_news_emo, R.id.iv_news_emo},
                new String[] { "title", "pinpreview|"+R.drawable.news_featured_nonpicture,"p_name","fav", "dummy", "dummy"});
		
		binder1.addSetter(newsRegularSetter);
		
		
		
		
		Setter newsImageSetter = new Setter(){

			@SuppressLint("ResourceAsColor") @Override
			public boolean onSet(View view, String viewProp, int index,
					String key, Object val, View parent,
					IContainerBinder container) {
				
				boolean nightmode = NightModeUtil.isNightMode();
				
				parent.setBackgroundColor(nightmode ? ctx.getResources().getColor(R.color.bg_black_night) : 
					ctx.getResources().getColor(R.color.bg_white));
				NewsItem n = getDataSource().getItem(index);
				if(view.getId() == R.id.news_img_title)
				{
					
					boolean haveread = DBHelper.getInstance().getRead(n.get_id());
					
					if(!nightmode)
					{
						if(haveread)
						{
							((TextView)view).setTextColor(ctx.getResources().getColor(R.color.bg_grey));
						}
						else
						{
							((TextView)view).setTextColor(ctx.getResources().getColor(R.color.bg_black));
						}
					}
					else
						((TextView)view).setTextColor(ctx.getResources().getColor(R.color.bg_white_night));
					
			
					
					return false;
					
				}
				


				
				return false;
			}
			
		};


		ItemBinder binder4 = new ItemBinder(
				R.layout.news_ad_pin,
				new int[] { R.id.news_title}, new String[] { "title"});
		Setter adSetter = new Setter(){

			@SuppressLint("ResourceAsColor") @Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {
				Log.e("Binder", "User adSetter parent=" + parent.toString());

				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				if(view.getId() == R.id.news_title)
				{
					NewsItem newsitem = getDataSource().getItem(index);
					ViewGroup listitem = (ViewGroup) container.getContainerView().getChildAt(index);
					ViewGroup gmobiContainer =ViewHolder.get(parent, R.id.gmobi_ad_container).getView();
					ViewGroup fbContainer =ViewHolder.get(parent, R.id.facebook_ad_container).getView();


					if (newsitem.getAdObj() instanceof IAdItem) {
						gmobiContainer.setVisibility(View.VISIBLE);
						fbContainer.setVisibility(View.GONE);
						IAdItem aditem = (IAdItem) newsitem.getAdObj();
						if (aditem != null) {
							if (aditem.has(IAdItem.VIDEO)) {
								ViewHolder.get(parent, R.id.rl_type_pin).getView().setVisibility(View.GONE);
								ViewHolder.get(parent, R.id.rl_type_pin_video).getView().setVisibility(View.VISIBLE);

								aditem.bind(ViewHolder.get(parent, R.id.rl_type_pin_video).getView(), new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_video, R.id.vpPlayer});
							} else if (aditem.has(IAdItem.IMAGE)) {
								ViewHolder.get(parent, R.id.rl_type_pin).getView().setVisibility(View.VISIBLE);
								ViewHolder.get(parent, R.id.rl_type_pin_video).getView().setVisibility(View.GONE);
								aditem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title, R.id.iv_ad});
							} else {
								ViewHolder.get(parent, R.id.rl_type_pin).getView().setVisibility(View.VISIBLE);
								ViewHolder.get(parent, R.id.rl_type_pin_video).getView().setVisibility(View.GONE);
								aditem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.ICON},
										new int[]{R.id.news_title, R.id.iv_ad});
							}

						}
					}
					else
					{
						gmobiContainer.setVisibility(View.GONE);
						fbContainer.setVisibility(View.VISIBLE);
						NativeAd aditem = (NativeAd)newsitem.getAdObj();
						if (aditem != null) {
							inflateFacebookView(aditem,listitem);
						}
					}

					return true;
				}
				return false;
			}

		};
		binder4.addSetter(adSetter);

		ItemBinder binder5 = new ItemBinder(
				R.layout.facebook_ad_pin,
				new int[] { R.id.native_ad_title}, new String[] { "title"});
		Setter facebookadSetter = new Setter(){

			@SuppressLint("ResourceAsColor") @Override
			public boolean onSet(View view, String viewProp, int index,
								 String key, Object val, View parent,
								 IContainerBinder container) {
				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				Log.e("Binder", "User adSetter parent="+parent.toString());
				if(view.getId() == R.id.native_ad_title)
				{
					NewsItem newsitem = getDataSource().getItem(index);
						NativeAd aditem = (NativeAd)newsitem.getAdObj();
						if (aditem != null) {
							inflateFacebookView(aditem, (ViewGroup) parent);
						}


					return true;
				}
				return false;
			}

		};
		binder5.addSetter(facebookadSetter);


		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_REGULAR), binder1);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_IMAGE), binder1);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_AD), binder4);
		cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FACEBOOKAD), binder5);
		if(isSupportFeature)
			cib.addBinder(new ValueBinderSelector("Type", NewsItem.NEWS_TYPE_FEATURED), binder1);
		return cib;
	}

	private void inflateFacebookView(NativeAd facebookAd,ViewGroup container)
	{
		/*if(container.getChildCount() > 0)
			return;*/
		/*container.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup adView = (ViewGroup)inflater.inflate(R.layout.facebook_ad_pin, container);*/
		facebookAd.unregisterView();

		// Create native UI using the ad metadata.

		TextView nativeAdTitle = (TextView)container.findViewById(R.id.native_ad_title);
		MediaView nativeAdMedia = (MediaView)container.findViewById(R.id.native_ad_media);

		NativeAd.Image adCoverImage = facebookAd.getAdCoverImage();

		int bannerWidth = adCoverImage.getWidth();
		int bannerHeight = adCoverImage.getHeight();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;

		RelativeLayout.LayoutParams llp =
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.MATCH_PARENT
				);


		nativeAdMedia.setLayoutParams(llp);
		nativeAdMedia.setNativeAd(facebookAd);

		// Setting the Text.
		nativeAdTitle.setText(facebookAd.getAdTitle());


//		AdChoicesView adChoicesView = null;
//		if (adChoicesView == null) {
//			adChoicesView = new AdChoicesView(ctx, aditem);
//			adView.addView(adChoicesView, 0);
//		}


		facebookAd.registerViewForInteraction(container);
	}
	
}
