package com.gmobi.poponews.app;

import com.gmobi.poponews.R;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.CaseActivity;

/**
 * Created by Administrator on 8/21 0021.
 */
public abstract class BaseAppActivity extends CaseActivity {

	protected abstract void onCaseCreate();
	protected abstract String getSelfName();

	@Override
	protected String getCaseName() {
		return getSelfName();
	}

	@Override
	protected void onCreate() {
		this.onCaseCreate();
		UiHelper.setStatusBarColor(this, findViewById(R.id.statusBarBackground),
				NightModeUtil.isNightMode() ? getResources().getColor(R.color.bg_red_night) : getResources().getColor(R.color.bg_red));

	}
}
