package com.gmobi.poponews.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.EditionList;
import com.gmobi.poponews.model.EditionList.EditionEntity;
import com.gmobi.poponews.outlet.EditionAdapter;
import com.gmobi.poponews.provider.PoponewsProvider;
import com.gmobi.poponews.util.PreferenceHelper;
import com.gmobi.poponews.util.WidgetDataHelper;



import java.util.ArrayList;
import java.util.List;


public class WidgetEditionsActivity extends Activity {

	public String DOWNLOADURl_TEMPLATE = "https://play.google.com/store/apps/details?id={appid}&referrer=dch%3D{dch}";
	public String POPONEWS_START_ACTIVITY = "com.gmobi.poponews.activity.SplashActivity";


	public static final String SP_NAME = "poponewsCommon";
	public static SharedPreferences sp = null;
	public static SharedPreferences.Editor editor;
	public static final String SP_KEY_COMMON_DCH = "commondch";

	public static Handler handler;
	public ListView lvEdition;
	public ImageView ivBack;
	public EditionAdapter editionAdapter;


	private List<EditionEntity> editionList = new ArrayList<>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_activity_edition);
		lvEdition  = (ListView) findViewById(R.id.edition_list);
		ivBack= (ImageView) findViewById(R.id.ed_back);
		ivBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});


		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case MessageTopics.SYSTEM_MSG_EDITION_READY:
						EditionList el = WidgetDataHelper.getEditionList(WidgetEditionsActivity.this);
						if(el!=null)
						{
							List<EditionList.EditionEntity> data = el.getData();
							editionList.clear();
							for(int i=0; i<data.size();i++)
							{
								editionList.add(data.get(i));
							}
							editionAdapter.notifyDataSetChanged();

							//setListViewHeightBasedOnChildren(lvEdition);

						}

						break;
					case MessageTopics.SYSTEM_MSG_EDITION_IMG_READY:
						editionAdapter.notifyDataSetChanged();
						break;
				}
			}
		};

		editionAdapter = new EditionAdapter(this,editionList);
		lvEdition.setAdapter(editionAdapter);
		lvEdition.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(editionList == null)
					return;

				EditionList.EditionEntity ee = editionList.get(position);
				PreferenceHelper.saveCategory(WidgetEditionsActivity.this, "");
				PreferenceHelper.setCurChannel(WidgetEditionsActivity.this, ee.getId());

				WidgetDataHelper.getNewsInExecutor(WidgetEditionsActivity.this);

				Intent refreshIntent = new Intent(WidgetEditionsActivity.this, PoponewsProvider.class);
				refreshIntent.setAction(IntentNames.REFRESH);
				WidgetEditionsActivity.this.sendBroadcast(refreshIntent);

				finish();
			}
		});

		WidgetDataHelper.remoteGetEditionList(this,true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}

		return super.onOptionsItemSelected(item);
	}






}
