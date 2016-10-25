package com.gmobi.poponews.cases.mycomment;

import android.content.Intent;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.NightModeUtil;

/**
 * Created by vivian on 2016/6/30.
 */
public class MyCommentActivity extends BaseAppActivity {
    @Override
    protected void onCaseCreate() {
        setContentView(R.layout.activity_mycomment);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != PopoApplication.mTencent){
            PopoApplication.mTencent.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    protected String getSelfName() {
        return CaseNames.MY_COMMENT;
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
