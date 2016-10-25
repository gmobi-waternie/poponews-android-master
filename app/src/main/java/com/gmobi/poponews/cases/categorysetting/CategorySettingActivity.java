package com.gmobi.poponews.cases.categorysetting;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.NightModeUtil;


public class CategorySettingActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
		NightModeUtil.onActivityCreateSetTheme(this);
		setContentView(R.layout.activity_ctg_setting);

    }

	@Override
	protected String getSelfName() {
			return CaseNames.CATEGORY_SETTING;
		
	}
	@Override
	public void onResume() {
		super.onResume();
		AnalysisUtil.onActivityResume(this,AnalysisUtil.SCR_NEWS);
	}

	@Override
	public void onPause() {
		super.onPause();
		AnalysisUtil.onActivityPause(this);
	}

	
}
