package com.gmobi.poponews.outlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.cases.article.ArticleActivity;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.NewsListItem;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.binder.ComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ValueBinderSelector;
import com.momock.binder.ViewBinder.Setter;
import com.momock.data.IDataList;
import com.reach.IAdItem;

import org.w3c.dom.Text;

public class NewsListItemBinderFactory {
	
	private Context ctx;
	private IDataService ds;
	private String cid;
	
	private boolean isSupportFeature;
	
	
	public NewsListItemBinderFactory(Context ctx)
	{
		this.ctx = ctx;
		this.isSupportFeature = true;
		ds = App.get().getService(IDataService.class);
	}
	
	public NewsListItemBinderFactory setFeatureSupport(boolean s)
	{
		this.isSupportFeature = s;
		return this; 
	}
	
	public NewsListItemBinderFactory setCategoryId(String cid)
	{
		this.cid = cid;
		return this; 
	}
	
	private IDataList<NewsListItem> getDataSource()
	{

		return ds.getNewsListInCategory(cid);
	}
	

	private void doReadAction(NewsItem i)
	{
		final IRemoteService rs = App.get().getService(IRemoteService.class);
		final DBHelper dh = DBHelper.getInstance();

		ds.setCurNid(i.get_id());
		
		dh.setRead(i.get_id(), true);
		i.setHaveRead(NewsItem.NEWS_HAVE_READ);
		ds.addIntoReadList(i);



		if(!i.getGo2Src())
			UiHelper.openArticleFromApp(ctx, i.get_id());
		else
			UiHelper.openBrowserActivity(ctx, i.get_id(), i.getType(), i.getSource(), i.getPname(), i.getPdomain(), "");


		//App.get().getActiveCase().getCase(CaseNames.ARTICLE).run();
		if(!i.getType().equals(NewsItem.NEWS_TYPE_IMAGE))
			rs.getBodyContent(i.get_id(),i.getBody());

	}
	public ComposedItemBinder build()
	{
		ComposedItemBinder cib = new ComposedItemBinder();
		
		ItemBinder binder1 = new ItemBinder(
				R.layout.news_regular_1_l,
				new int[] { R.id.news_title, R.id.news_img},
                new String[] { "title1", "preview1|"+R.drawable.homepage_newslist_nonpicture});
		Setter newsbinder1Setter = new Setter() {
			
			@Override
			public boolean onSet(View view, String viewProp, int index, String key,
					Object val, final View parent, IContainerBinder container) {

				FrameLayout adRoot = (FrameLayout) parent.findViewById(R.id.fl_type_regular_1_l_ad);
				LinearLayout newsRoot = (LinearLayout) parent.findViewById(R.id.ll_type_regular_1_l);
				LayoutInflater inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));

				final NewsListItem li = getDataSource().getItem(index);
				if(view.getId() == R.id.news_title)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD )
					{

						adRoot.setVisibility(View.VISIBLE);
						newsRoot.setVisibility(View.GONE);


						if(li.getItem(0).getAdObj() instanceof IAdItem) {
							adRoot.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad1, adRoot);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(0).getAdObj());
							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem1).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem1});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem1).setVisibility(View.GONE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(0).getAdObj());
							inflateFacebookView(aditem,adRoot,R.layout.facebook_listitem_ad1);
						}




						return true;
					}
					else
					{
						adRoot.setVisibility(View.GONE);
						newsRoot.setVisibility(View.VISIBLE);
					}

				}


				if(view.getId() == R.id.news_img)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;

					View ll = parent.findViewById(R.id.ll_type_regular_1_l);
					ll.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(0);
							doReadAction(i);	
						}
					});

					setTextViewStyle(parent.findViewById(R.id.news_title),  li.getItem(0));
				}
				return false;
			}
		};
		binder1.addSetter(newsbinder1Setter);
		
		

		
		ItemBinder binder2 = new ItemBinder(
				R.layout.news_regular_1_r,
				new int[] { R.id.news_title, R.id.news_img},
                new String[] { "title1", "preview1|"+R.drawable.homepage_newslist_nonpicture});
		Setter newsbinder2Setter = new Setter() {
			
			@Override
			public boolean onSet(View view, String viewProp, int index, String key,
					Object val, final View parent, IContainerBinder container) {

				final NewsListItem li = getDataSource().getItem(index);

				FrameLayout adRoot = (FrameLayout) parent.findViewById(R.id.fl_type_regular_1_r_ad);
				LinearLayout newsRoot = (LinearLayout) parent.findViewById(R.id.ll_type_regular_1_r);
				LayoutInflater inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));

				if(view==null)
					return false;

				if(view.getId() == R.id.news_title)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
					{
						adRoot.setVisibility(View.VISIBLE);
						newsRoot.setVisibility(View.GONE);


						if(li.getItem(0).getAdObj() instanceof IAdItem) {
							adRoot.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad1, adRoot);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(0).getAdObj());


							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem1).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem1});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem1).setVisibility(View.GONE);
								adItem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(0).getAdObj());
							inflateFacebookView(aditem,adRoot,R.layout.facebook_listitem_ad1);
						}

						return true;
					}
					else
					{
						adRoot.setVisibility(View.GONE);
						newsRoot.setVisibility(View.VISIBLE);
					}

				}

				if(view.getId() == R.id.news_img)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD  ||li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;

					View ll_l = parent.findViewById(R.id.ll_type_regular_1_r);
					ll_l.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(0);
							doReadAction(i);	
						}
					});
					ll_l.setOnFocusChangeListener(new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (hasFocus)
								v.setBackgroundResource(R.color.bg_grey);
							else
								v.setBackgroundResource(R.color.bg_white);

						}
					});
					setTextViewStyle(parent.findViewById(R.id.news_title), li.getItem(0));

				}
				return false;
			}
		};
		binder2.addSetter(newsbinder2Setter);
		
		
		
		ItemBinder binder3 = new ItemBinder(
				R.layout.news_regular_2,
				new int[] { R.id.news_title_1,R.id.news_title_2,
							R.id.news_img_1,R.id.news_img_2}, 
				new String[] { "title1","title2", 
						"preview1|"+R.drawable.homepage_newslist_nonpicture,
						"preview2|"+R.drawable.homepage_newslist_nonpicture});
		
		Setter newsbinder3Setter = new Setter() {
			
			@Override
			public boolean onSet(View view, String viewProp, int index, String key,
					Object val, final View parent, IContainerBinder container) {
				
				final NewsListItem li = getDataSource().getItem(index);
				FrameLayout adRoot_l = (FrameLayout) parent.findViewById(R.id.fl_type_regular_2_l_ad);
				FrameLayout adRoot_r = (FrameLayout) parent.findViewById(R.id.fl_type_regular_2_r_ad);

				LinearLayout newsRoot_l = (LinearLayout) parent.findViewById(R.id.ll_type_regular_2_l);
				LinearLayout newsRoot_r = (LinearLayout) parent.findViewById(R.id.ll_type_regular_2_r);

				LayoutInflater inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				if(view.getId() == R.id.news_title_1)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
					{
						adRoot_l.setVisibility(View.VISIBLE);
						newsRoot_l.setVisibility(View.GONE);


						if(li.getItem(0).getAdObj() instanceof IAdItem) {
							adRoot_l.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad2, adRoot_l);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(0).getAdObj());


							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem2).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem2});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem2).setVisibility(View.GONE);
								adItem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(0).getAdObj());
							inflateFacebookView(aditem,adRoot_l,R.layout.facebook_listitem_ad2);
						}


						return true;
					}
					else
					{
						adRoot_l.setVisibility(View.GONE);
						newsRoot_l.setVisibility(View.VISIBLE);
					}

				}

				if(view.getId() == R.id.news_title_2)
				{
					if(li.getItem(1).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(1).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
					{
						adRoot_r.setVisibility(View.VISIBLE);
						newsRoot_r.setVisibility(View.GONE);


						if(li.getItem(1).getAdObj() instanceof IAdItem) {
							adRoot_r.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad2, adRoot_r);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(1).getAdObj());

							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem2).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem2});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem2).setVisibility(View.GONE);
								adItem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(1).getAdObj());
							inflateFacebookView(aditem,adRoot_r,R.layout.facebook_listitem_ad2);
						}


						return true;
					}
					else
					{
						adRoot_r.setVisibility(View.GONE);
						newsRoot_r.setVisibility(View.VISIBLE);
					}

				}




				if(view.getId() == R.id.news_img_1)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;

					newsRoot_l.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(0);
							doReadAction(i);	
						}
					});


					setTextViewStyle(parent.findViewById(R.id.news_title_1), li.getItem(0));

				}
				if(view.getId() == R.id.news_img_2)
				{
					if(li.getItem(1).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(1).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;
					newsRoot_r.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(1);
							doReadAction(i);
						}
					});

					setTextViewStyle(parent.findViewById(R.id.news_title_2), li.getItem(1));
				}
				return false;
			}
		};
		binder3.addSetter(newsbinder3Setter);

		ItemBinder binder4 = new ItemBinder(
				R.layout.news_regular_3,
				new int[] { R.id.news_title_1,R.id.news_title_2,R.id.news_title_3,
							R.id.news_img_1,R.id.news_img_2,R.id.news_img_3}, 
				new String[] { "title1","title2", "title3",
						"preview1|"+R.drawable.homepage_newslist_nonpicture,
						"preview2|"+R.drawable.homepage_newslist_nonpicture,
						"preview3|"+R.drawable.homepage_newslist_nonpicture});
		Setter newsbinder4Setter = new Setter() {
			
			@Override
			public boolean onSet(View view, String viewProp, int index, String key,
					Object val, final View parent, IContainerBinder container) {
				parent.setBackgroundColor(NightModeUtil.isNightMode() ? ctx.getResources().getColor(R.color.bg_black_night) :
						ctx.getResources().getColor(R.color.bg_white));
				final NewsListItem li = getDataSource().getItem(index);

				FrameLayout adRoot_l = (FrameLayout) parent.findViewById(R.id.fl_type_regular_3_l_ad);
				FrameLayout adRoot_m = (FrameLayout) parent.findViewById(R.id.fl_type_regular_3_m_ad);
				FrameLayout adRoot_r = (FrameLayout) parent.findViewById(R.id.fl_type_regular_3_r_ad);

				LinearLayout newsRoot_l = (LinearLayout) parent.findViewById(R.id.ll_type_regular_3_l);
				LinearLayout newsRoot_m = (LinearLayout) parent.findViewById(R.id.ll_type_regular_3_m);
				LinearLayout newsRoot_r = (LinearLayout) parent.findViewById(R.id.ll_type_regular_3_r);

				LayoutInflater inflater=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				if(view == null)
					return false;

				if(view.getId() == R.id.news_title_1)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
					{
						adRoot_l.setVisibility(View.VISIBLE);
						newsRoot_l.setVisibility(View.GONE);


						if(li.getItem(0).getAdObj() instanceof IAdItem) {
							adRoot_l.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad3, adRoot_l);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(0).getAdObj());

							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem3).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem3});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem3).setVisibility(View.GONE);
								adItem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(0).getAdObj());
							inflateFacebookView(aditem,adRoot_l,R.layout.facebook_listitem_ad3);
						}

						return true;
					}
					else
					{
						adRoot_l.setVisibility(View.GONE);
						newsRoot_l.setVisibility(View.VISIBLE);
					}
				}


				if(view.getId() == R.id.news_title_2)
				{
					if(li.getItem(1).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(1).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
					{
						adRoot_m.setVisibility(View.VISIBLE);
						newsRoot_m.setVisibility(View.GONE);


						if(li.getItem(1).getAdObj() instanceof IAdItem) {
							adRoot_m.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad3, adRoot_m);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(1).getAdObj());

							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem3).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem3});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem3).setVisibility(View.GONE);
								adItem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(1).getAdObj());
							inflateFacebookView(aditem,adRoot_m,R.layout.facebook_listitem_ad3);
						}
						return true;
					}
					else
					{
						adRoot_m.setVisibility(View.GONE);
						newsRoot_m.setVisibility(View.VISIBLE);
					}
				}


				if(view.getId() == R.id.news_title_3)
				{
					if(li.getItem(2).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(2).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
					{
						adRoot_r.setVisibility(View.VISIBLE);
						newsRoot_r.setVisibility(View.GONE);


						if(li.getItem(2).getAdObj() instanceof IAdItem) {
							adRoot_r.removeAllViews();
							View adView = inflater.inflate(R.layout.news_listitem_ad3, adRoot_r);
							ViewGroup adContainer = (ViewGroup) adView;
							IAdItem adItem = (IAdItem) (li.getItem(2).getAdObj());


							if (adItem.has(IAdItem.VIDEO)) {

								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.GONE);
								adContainer.findViewById(R.id.vpPlayer_listitem3).setVisibility(View.VISIBLE);
								adItem.bind(adContainer, new String[]{IAdItem.TITLE, IAdItem.MEDIA_CONTAINER},
										new int[]{R.id.news_title_image, R.id.vpPlayer_listitem3});
							} else {
								adContainer.findViewById(R.id.iv_ad_image).setVisibility(View.VISIBLE);
								adContainer.findViewById(R.id.vpPlayer_listitem3).setVisibility(View.GONE);
								adItem.bind(parent, new String[]{IAdItem.TITLE, IAdItem.IMAGE},
										new int[]{R.id.news_title_image, R.id.iv_ad_image});
							}
						}
						else
						{
							NativeAd aditem = (NativeAd) (li.getItem(2).getAdObj());
							inflateFacebookView(aditem,adRoot_r,R.layout.facebook_listitem_ad3);
						}

						return true;
					}
					else
					{
						adRoot_r.setVisibility(View.GONE);
						newsRoot_r.setVisibility(View.VISIBLE);
					}
				}


				if(view.getId() == R.id.news_img_1)
				{
					if(li.getItem(0).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(0).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;
					newsRoot_l.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(0);
							doReadAction(i);
						}
					});

					setTextViewStyle(parent.findViewById(R.id.news_title_1), li.getItem(0));

				}
				if(view.getId() == R.id.news_img_2)
				{
					if(li.getItem(1).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(1).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;

					newsRoot_m.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(1);
							doReadAction(i);
						}
					});


					setTextViewStyle(parent.findViewById(R.id.news_title_2), li.getItem(1));

				}

				if(view.getId() == R.id.news_img_3)
				{
					if(li.getItem(2).getType() == NewsItem.NEWS_TYPE_AD || li.getItem(2).getType() == NewsItem.NEWS_TYPE_FACEBOOKAD)
						return true;

					newsRoot_r.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							NewsItem i = li.getItem(2);
							doReadAction(i);
						}
					});
					setTextViewStyle(parent.findViewById(R.id.news_title_3), li.getItem(2));
				}
				return false;
			}
		};
		binder4.addSetter(newsbinder4Setter);
		
		
		cib.addBinder(new ValueBinderSelector("ListType", Integer.valueOf(NewsListItem.LIST_TYPE_1L)), binder1);
		cib.addBinder(new ValueBinderSelector("ListType", Integer.valueOf(NewsListItem.LIST_TYPE_1R)), binder2);
		cib.addBinder(new ValueBinderSelector("ListType", Integer.valueOf(NewsListItem.LIST_TYPE_2)), binder3);
		cib.addBinder(new ValueBinderSelector("ListType", Integer.valueOf(NewsListItem.LIST_TYPE_3)), binder4);

		
		if(isSupportFeature)
			cib.addBinder(new ValueBinderSelector("ListType", NewsItem.NEWS_TYPE_FEATURED), binder3);
		
		return cib;
	}



	private void setTextViewStyle(View view, NewsItem ni)
	{
		boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false :true;
		boolean haveread = DBHelper.getInstance().getRead(ni.get_id());

		TextView tv = (TextView)view;

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
	}

	private void inflateFacebookView(NativeAd facebookAd,ViewGroup container,int layoutId)
	{

		if(container.getChildCount() > 0)
			return;



		container.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup adView = (ViewGroup)inflater.inflate(layoutId, container);
		facebookAd.unregisterView();

		// Create native UI using the ad metadata.

		TextView nativeAdTitle = (TextView)adView.findViewById(R.id.native_ad_title);
		MediaView nativeAdMedia = (MediaView)adView.findViewById(R.id.native_ad_media);

		NativeAd.Image adCoverImage = facebookAd.getAdCoverImage();
		nativeAdMedia.setNativeAd(facebookAd);

		// Setting the Text.
		nativeAdTitle.setText(facebookAd.getAdTitle());
		facebookAd.registerViewForInteraction(adView);
	}
}
