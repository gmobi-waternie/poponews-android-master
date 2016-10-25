package com.gmobi.poponews.cases.facebook;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;

import com.gmobi.poponews.R;
import com.gmobi.poponews.cases.browser.BrowserActivity;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IFacebookService;
import com.gmobi.poponews.service.INewsCacheService;
import com.gmobi.poponews.service.IRemoteService;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.app.ICase;
import com.momock.binder.ItemBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.service.ICacheService;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IRService;
import com.momock.service.ISystemService;
import com.momock.service.IUITaskService;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class FacebookCase extends Case<CaseActivity> {

	public FacebookCase(String name) {
		super(name);
	}
	public FacebookCase(ICase<?> parent) {
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
	IFacebookService facebookService;

	ListViewBinder lvBinder;




	@Override
	public void onCreate() {

		ItemBinder binder1 = new ItemBinder(
				R.layout.facebook_post,
				new int[] { R.id.post_title, R.id.post_from_name,R.id.post_img},
				new String[] { "name", "fromname","picture|"+R.drawable.homepage_newslist_nonpicture});


		lvBinder = new ListViewBinder(binder1);
		lvBinder.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
			@Override
			public void process(Object sender, ItemEventArgs args) {
				SocialPost fp = (SocialPost) args.getItem();
				Intent intent = new Intent(App.get().getCurrentActivity(), BrowserActivity.class);
				intent.putExtra("url", fp.getLink());
				intent.putExtra("title", "Facebook");
				intent.putExtra("domain", "social");
				App.get().getCurrentActivity().startActivity(intent);

			}

		});


		/*
		messageService.addHandler(MessageTopics.GET_FACEBOOK_FRIENDS, new IMessageHandler() {
			@Override
			public void process(Object o, Message message ) {
				int ret = (Integer)message.getData();
				if(ret > 0)
				{
					DataList<SocialAccount> friList = (DataList<SocialAccount>)facebookService.getFacebookFriendList();
					for(int i=0; i<friList.getItemCount(); i++)
					{
						facebookService.remoteGetFriendPosts(friList.getItem(i).getId());
					}

					UiHelper.setFetchFacebookData(true);
				}
			}
		});*/

	}

	@Override
	public void onAttach(CaseActivity target) {
		final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);

/*
		lvBinder.bind(ViewHolder.get(refTarget.get(), R.id.lv_postlist), facebookService.getFacebookPostList());


		View ivBackBtn = ViewHolder.get(target, R.id.fb_back).getView();

		ivBackBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (refTarget.get() != null) {
					Activity currActivity = App.get().getCurrentActivity();
					if (currActivity != null)
						currActivity.finish();
				}
			}
		});

		if(!UiHelper.hasFetchFacebookData())
		{
			if( AccessToken.getCurrentAccessToken() != null)
				facebookService.remoteGetFriendsList();
			else
			{
				LoginButton loginButton = (LoginButton) target.findViewById(R.id.facebook_login_button);
				loginButton.setReadPermissions(Arrays.asList("user_posts", "user_friends", "read_stream"));
				loginButton.registerCallback(FacebookActivity.callbackManager, new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {
						Set<String> pm = AccessToken.getCurrentAccessToken().getPermissions();
						Log.e("facebook", pm.toString());

						facebookService.remoteGetFriendsList();
					}

					@Override
					public void onCancel() {
						// App code
					}

					@Override
					public void onError(FacebookException exception) {
						// App code
					}
				});
			}


		}

*/


	}




	@Override
	public void run(Object... args) {
		App.get().startActivity(FacebookActivity.class);
	}



}





