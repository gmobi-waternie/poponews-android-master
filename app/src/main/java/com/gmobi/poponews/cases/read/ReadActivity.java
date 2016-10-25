package com.gmobi.poponews.cases.read;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;


public class ReadActivity extends BaseAppActivity {

    @Override
    public void onCaseCreate() {
        setContentView(R.layout.activity_read);
    }

	@Override
	protected String getSelfName() {
			return CaseNames.USER_READ;
		
	}


	
}
