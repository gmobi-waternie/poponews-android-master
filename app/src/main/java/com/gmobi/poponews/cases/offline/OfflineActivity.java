package com.gmobi.poponews.cases.offline;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.util.NightModeUtil;


public class OfflineActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
		NightModeUtil.onActivityCreateSetTheme(this);
		setContentView(R.layout.activity_offline);
    }

	@Override
	protected String getSelfName() {
			return CaseNames.USER_OFFLINE;
		
	}


	
}
