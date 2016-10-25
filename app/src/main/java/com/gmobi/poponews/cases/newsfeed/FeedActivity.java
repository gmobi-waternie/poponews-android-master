package com.gmobi.poponews.cases.newsfeed;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;


public class FeedActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
        setContentView(R.layout.activity_feed);
    }

	@Override
	protected String getSelfName() {
			return CaseNames.USER_FEED;
	}


	
}
