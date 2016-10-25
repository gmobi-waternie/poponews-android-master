package com.gmobi.poponews.cases.browser;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.util.AnalysisUtil;


public class BrowserActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
        setContentView(R.layout.activity_webview);
    }

	@Override
	protected String getSelfName() {
			return CaseNames.USER_BROWSER;
		
	}

	@Override
	public void onResume() {
		super.onResume();
		AnalysisUtil.onActivityResume(this,AnalysisUtil.SCR_ARTICLE);
	}

	@Override
	public void onPause() {
		super.onPause();
		AnalysisUtil.onActivityPause(this);
	}
	
}
