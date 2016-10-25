package com.gmobi.poponews.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckedLinearLayout extends LinearLayout implements Checkable {

    private boolean isChecked = false;
    
	public CheckedLinearLayout(Context context) {
        super(context);
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
	@Override
	public void setChecked(boolean checked) {
		View child;
		isChecked = checked;
		for(int i = 0; i < getChildCount(); i++){
			child = getChildAt(i);
			 if (child instanceof Checkable) {
				 ((Checkable) child).setChecked(checked);
			 }
			
		}

	}

	@Override
	public boolean isChecked() {
		return isChecked;
	}

	@Override
	public void toggle() {
		setChecked(!isChecked);
	}

}
