package com.gmobi.poponews.outlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.SocialExtra;
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

public class SocialBinderFactory {

	private Context ctx;
	private IDataService ds;


	public SocialBinderFactory()
	{
		ds = App.get().getService(IDataService.class);
	}

	public ComposedItemBinder build()
	{
		ComposedItemBinder cib = new ComposedItemBinder();


		ItemBinder binder1 = new ItemBinder(
				R.layout.facebook_post,
				new int[]{R.id.post_title, R.id.post_from_name, R.id.post_img,R.id.post_from_avatar},
				new String[]{"name", "fromname", "picture|" + R.drawable.news_featured_nonpicture,"fromavatar"});

		ItemBinder binder2 = new ItemBinder(
				R.layout.twitter_post,
				new int[]{R.id.post_title,R.id.post_from_name,R.id.post_img,R.id.post_from_avatar},
				new String[]{"name","fromname","picture|" + R.drawable.news_featured_nonpicture,"fromavatar"});


		ItemBinder binder3 = new ItemBinder(
				R.layout.google_post,
				new int[]{R.id.post_title,R.id.post_from_name,R.id.post_img,R.id.post_category},
				new String[]{"name","fromname","picture|" + R.drawable.homepage_newslist_nonpicture,"fromid"});

		ItemBinder binder4 = new ItemBinder(
				R.layout.baidu_post,
				new int[]{R.id.post_title,R.id.post_from_name,R.id.post_img,R.id.post_category},
				new String[]{"name","fromname","picture|" + R.drawable.news_featured_nonpicture,"fromid"});



		ItemBinder binder5 = new ItemBinder(
				R.layout.weibo_post,
				new int[]{R.id.post_title,R.id.post_from_name,R.id.post_img,R.id.post_from_avatar},
				new String[]{"name","fromname","picture|" + R.drawable.news_featured_nonpicture,"fromavatar"});


		cib.addBinder(new ValueBinderSelector("Socialtype", SocialExtra.SOCIAL_TYPE_FACEBOOK), binder1);
		cib.addBinder(new ValueBinderSelector("Socialtype", SocialExtra.SOCIAL_TYPE_TWITTER), binder2);
		cib.addBinder(new ValueBinderSelector("Socialtype", SocialExtra.SOCIAL_TYPE_GOOGLE), binder3);
		cib.addBinder(new ValueBinderSelector("Socialtype", SocialExtra.SOCIAL_TYPE_BAIDU), binder4);
		cib.addBinder(new ValueBinderSelector("Socialtype", SocialExtra.SOCIAL_TYPE_WEIBO), binder5);

		return cib;
	}
	
}
