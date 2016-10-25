package com.gmobi.poponews.cases.article;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.BaseAppActivity;
import com.gmobi.poponews.app.CaseNames;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.cases.comment.CommentActivity;
import com.gmobi.poponews.cases.main.MainActivity;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.holder.ViewHolder;
import com.momock.util.Logger;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.tauth.Tencent;


public class ArticleActivity extends BaseAppActivity{


    @Override
    public void onCaseCreate() {
        setContentView(R.layout.activity_article);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            int statusBarHeight = UiHelper.getStatusBarHeight(this);
            params.setMargins(0, -statusBarHeight,0,0);
            ViewHolder.get(this,R.id.relative_article).getView().setLayoutParams(params);
        }
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
		return CaseNames.ARTICLE;
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
    /**
     * 重写目的：点击EditText所在ViewGroup外部，隐藏软键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    v.clearFocus();
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    ViewHolder.get(this, R.id.rl_mask).getView().setVisibility(View.GONE);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }


    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
//			if ((v != null && (v instanceof Button)) || (v != null && (v instanceof EditText))) {
            int[] leftTop = { 0, 0 };
            // 获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + this.getWindowManager().getDefaultDisplay().getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
