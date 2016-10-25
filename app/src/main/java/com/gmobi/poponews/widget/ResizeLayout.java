package com.gmobi.poponews.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by nage on 2016/5/23.
 */
public class ResizeLayout extends LinearLayout {
    public ResizeLayout(Context context) {
        super(context);
    }

    public ResizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mListener != null){
            mListener.OnResizeListener(w, h, oldw, oldh);
        }
    }

    private OnResizeListener mListener;

    public interface OnResizeListener{
        void OnResizeListener(int w, int h, int oldw, int oldh);
    }

    public void setOnResizeListener(OnResizeListener l){
        this.mListener = l;
    }


}
