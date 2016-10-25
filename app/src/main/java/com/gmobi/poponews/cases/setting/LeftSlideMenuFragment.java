package com.gmobi.poponews.cases.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.cases.browser.BrowserActivity;
import com.gmobi.poponews.cases.favorite.FavoriteActivity;
import com.gmobi.poponews.cases.newsfeed.FeedActivity;
import com.gmobi.poponews.cases.offline.OfflineActivity;
import com.gmobi.poponews.cases.read.ReadActivity;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.util.NightModeUtil;
import com.momock.app.App;

public class LeftSlideMenuFragment extends PreferenceFragment  implements OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final IConfigService configSvc = App.get().getService(IConfigService.class);
        
        addPreferencesFromResource(R.xml.pref_slide_menu_left);
        
        String key = getString(R.string.key_store);
        Preference pfSetting = findPreference(key);
        pfSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                Intent intent = new Intent(App.get(), BrowserActivity.class);
                intent.putExtra(IntentNames.INTENT_EXTRA_TITLE, getString(R.string.set_store));
                intent.putExtra(IntentNames.INTENT_EXTRA_URL, configSvc.getBuiltinStoreUrl());
				intent.putExtra(IntentNames.INTENT_EXTRA_DOMAIN, "store");
                App.get().getCurrentContext().startActivity(intent);
                return true;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ListView lv = (ListView)v.findViewById(android.R.id.list);

        lv.setPadding(0, 0, 0, 0);
        lv.setDivider(null);
        lv.setDividerHeight(0);
        return v;
    }

    
    
	@Override
	public void onResume() {
		Log.e("fragment","onResume");
		super.onResume();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.e("fragment","onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.e("fragment","onStart");
		super.onStart();
	}

	@Override
	public void onInflate(Activity activity, AttributeSet attrs,
			Bundle savedInstanceState) {
		Log.e("fragment","onInflate");
		super.onInflate(activity, attrs, savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		Log.e("fragment","onAttach");
		super.onAttach(activity);
	}

	@Override
	public View getView() {
		Log.e("fragment","getView");
		return super.getView();
	}

	@SuppressLint("NewApi") 
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		Log.e("fragment","onViewStateRestored");
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onPause() {
		Log.e("fragment","onPause");
		super.onPause();
	}

	@Override
	public void onDetach() {
		Log.e("fragment","onDetach");
		super.onDetach();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		
	}
	
    
    
    
    

}
