package com.gmobi.poponews.cases.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.gmobi.poponews.R;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;

public class SettingsActivity extends PreferenceActivity {

   public static void startMainActivity(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(ctx, SettingsActivity.class);
        
        intent.putExtra(EXTRA_SHOW_FRAGMENT, MainSettingFragment.class.getName());
        //intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, null);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, R.string.label_menu_config);
        //intent.putExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, shortTitleRes);
        intent.putExtra(EXTRA_NO_HEADERS, true);
        ctx.startActivity(intent);
   }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	if(NightModeUtil.isNightMode())
    		this.setTheme(R.style.SimpleActionBarNightTheme);
    	else
    		this.setTheme(R.style.SimpleActionBarTheme);

        super.onCreate(savedInstanceState);
        //UiHelper.setStatusBarColor(this, findViewById(R.id.statusBarBackground),
          //      getResources().getColor(R.color.bg_red));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
