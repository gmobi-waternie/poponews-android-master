package com.gmobi.poponews.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.service.IShareService;
import com.gmobi.poponews.share.IShare;
import com.momock.app.App;

import java.util.ArrayList;

/**
 * Created by Administrator on 1/14 0014.
 * NOTICE : Deprecated.
 */
public class ShareActivity extends Activity{

	private String title,webUrl,imageUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {



		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		Intent in = getIntent();
		if(in !=null)
		{
			Bundle bundle = in.getExtras();
			if(bundle!=null)
			{
				title = bundle.getString(IntentNames.INTENT_EXTRA_TITLE);
				webUrl = bundle.getString(IntentNames.INTENT_EXTRA_URL);
				imageUri = bundle.getString(IntentNames.INTENT_EXTRA_IMAGE);
			}

		}

		ArrayList<IShare> shares = App.get().getService(IShareService.class).getCurShares();
		LinearLayout ll_share = (LinearLayout) findViewById(R.id.ll_share);

		if(shares!=null)
		{
			for(int i=0; i<shares.size(); i++)
			{
				IShare share = shares.get(i);
				initShareView(ll_share,share.getControlDrawable(),share.getControlName(),share.getTitle());
			}
		}


		findViewById(R.id.rl_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.activity_new,
						R.anim.activity_finish);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			finish();
			overridePendingTransition(R.anim.activity_new,
					R.anim.activity_finish);
			return true;
		}

		return super.onKeyDown(keyCode, event);

	}

	private View.OnClickListener shareClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String tagName = (String) v.getTag();

			App.get().getService(IShareService.class).share(tagName, title, webUrl, imageUri);
		}
	};

	private void initShareView(ViewGroup parent,int drawable,String name, String title)
	{
		View share = getLayoutInflater().inflate(R.layout.share_item,parent);
		LinearLayout.LayoutParams  params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
		share.setLayoutParams(params);



		((ImageView)share.findViewById(R.id.iv_share_icon)).setImageResource(drawable);
		((TextView)share.findViewById(R.id.tv_share_title)).setText(title);

		share.setTag(name);
		share.setOnClickListener(shareClickListener);
	}


}
