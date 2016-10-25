package com.gmobi.poponews.cases.guide;

import android.os.Bundle;
import android.view.MenuItem;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.util.NightModeUtil;
import com.momock.app.CaseActivity;

public class GuideEditionActivity extends CaseActivity {

	@Override
	protected String getCaseName() {
		return CaseNames.GUIDE_EDITION;
	}

	@Override
	public void onCreate() {
		/*if(NightModeUtil.isNightMode())
			setTheme(R.style.SimpleActionBarNightTheme);
		else
			setTheme(R.style.SimpleActionBarTheme);*/
		setContentView(R.layout.activity_guide_edtion);

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
