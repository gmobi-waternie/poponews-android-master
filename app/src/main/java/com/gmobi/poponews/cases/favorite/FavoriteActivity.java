package com.gmobi.poponews.cases.favorite;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.util.AnalysisUtil;


public class FavoriteActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
        setContentView(R.layout.activity_fav);
    }

	@Override
	protected String getSelfName() {
			return CaseNames.USER_FAVORITE;
		
	}


	@Override
	public void onResume() {
		super.onResume();
		AnalysisUtil.onActivityResume(this,AnalysisUtil.SCR_ME);
	}

	@Override
	public void onPause() {
		super.onPause();
		AnalysisUtil.onActivityPause(this);
	}
}
