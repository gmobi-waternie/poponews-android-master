package com.gmobi.poponews.widget;

import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.gmobi.poponews.R;

import java.util.List;

public class EmoCenterView extends View{

	/**
	 * this background is design for semicircle;
	 */
	private int mInnerRadius;
	private Paint mCenterPaint;
	private RectF drawTmpRect = new RectF();

	public EmoCenterView(Context context) {
		this(context, null);
	}

	public EmoCenterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

	public EmoCenterView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		Resources res = getResources();
		int BgCenterColor = res.getColor(R.color.emo_heart);
	    
		mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterPaint.setColor(BgCenterColor);
		mCenterPaint.setStyle(Paint.Style.FILL);
	}
	
	public void onRadiusChanged(int inner){
		mInnerRadius = inner;
	}

//	public EmoCenterView addCenterView(ViewGroup vg, int index){
//		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
//				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//		setId(R.id.emo_center_bg);
//		if(index < 0){
//			vg.addView(this, lp);
//		}else{
//			vg.addView(this, index, lp);
//		}
//		return this;
//	}
	
	@Override
    protected void onDraw(Canvas canvas){
		// draw background shape 
		int s1 = canvas.save();
		canvas.translate(getWidth() / 2, 0);
		drawTmpRect.set(-mInnerRadius, -mInnerRadius, mInnerRadius, mInnerRadius);
		canvas.drawArc(drawTmpRect, 0, 180, true, mCenterPaint);
        EmoItemHolder eih = (EmoItemHolder)getTag();
        eih.drawText(canvas, true);
		canvas.restoreToCount(s1);
	}
}
