package com.gmobi.poponews.widget;

import java.util.List;

import com.gmobi.poponews.R;
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

public class EmoBackgroundView extends View{

	/**
	 * this background is design for semicircle;
	 */
	private int mMaxOuterRadius, mOuterRadius, mInnerRadius, mPressedRadius;
	private Paint mNormalPaint, mPressedPaint, mCenterPaint, mDividerPaint;
	private ObjectAnimator mPressedAnima;
	private EmoItemHolder selected = null;
	private RectF drawTmpRect = new RectF();
	private List<EmoItemHolder> emoHolderList = null;
	
	
	public EmoBackgroundView(Context context) {
		this(context, null);
	}
	
	public EmoBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
	
	public EmoBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		Resources res = getResources();
		int bgColor = res.getColor(R.color.emo_button_bg);
		int bgPressColor = res.getColor(R.color.emo_button_pressed);
		int BgCenterColor = res.getColor(R.color.emo_heart);
		
		mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalPaint.setColor(bgColor);
		mNormalPaint.setStyle(Paint.Style.FILL);
	    
		mPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPressedPaint.setColor(bgPressColor);
		mPressedPaint.setStyle(Paint.Style.FILL);
	    
		mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterPaint.setColor(BgCenterColor);
		mCenterPaint.setStyle(Paint.Style.FILL);
		
		mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mDividerPaint.setColor(Color.WHITE);
		mDividerPaint.setStyle(Paint.Style.FILL);
		
		mPressedAnima = ObjectAnimator.ofInt(this, "curRadius", 0, 0);
		mPressedAnima.setDuration(200);
		mPressedAnima.addUpdateListener(new AnimatorUpdateListener(){

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				invalidate();
			}});
	}
	
	public void onRadiusChanged(int inner, int outer, int maxOuter){
		mMaxOuterRadius = maxOuter;
		mOuterRadius = outer;
		mInnerRadius = inner;
	}
	
	public EmoBackgroundView setData(List<EmoItemHolder> data){
		emoHolderList = data;
		return this;
	}
	
	public EmoBackgroundView addAnimaListener(AnimatorListener al) {
		mPressedAnima.addListener(al);
		return this;
	}
	
	public void startItemSelectedAnima(EmoItemHolder eih){
		selected = eih;
		if(null != selected){
			mPressedAnima.setIntValues(mOuterRadius, mMaxOuterRadius);
			mPressedAnima.start();
		}else{
			invalidate();
		}
	}
	
	public EmoBackgroundView addBackgroundView(ViewGroup vg, int index){
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		setId(R.id.emo_background);
		if(index < 0){
			vg.addView(this, lp);
		}else{
			vg.addView(this, index, lp);
		}
		return this;
	}
	
	@Override
    protected void onDraw(Canvas canvas){
		
		// draw background shape 
		int s1 = canvas.save();
		canvas.translate(getWidth() / 2, 0);		
		
		drawTmpRect.set(-mOuterRadius, -mOuterRadius, mOuterRadius, mOuterRadius);
		if(null != selected){
			canvas.drawArc(drawTmpRect, selected.getEndAngle(), 
					360 - selected.getSweepAngle(), true, mNormalPaint);
			drawTmpRect.set(-mPressedRadius, -mPressedRadius, mPressedRadius, mPressedRadius);
			canvas.drawArc(drawTmpRect, selected.getStartAngle(), selected.getSweepAngle(), 
					true, mPressedRadius == mMaxOuterRadius ? mPressedPaint : mNormalPaint);
		}else{
			canvas.drawArc(drawTmpRect, 0, 180, true, mNormalPaint);
		}
		if(null != emoHolderList){
			for(EmoItemHolder eih : emoHolderList){
				eih.drawLeftDividers(canvas, mDividerPaint);
			}
		}
//		drawTmpRect.set(-mInnerRadius, -mInnerRadius, mInnerRadius, mInnerRadius);
//		canvas.drawArc(drawTmpRect, 0, 180, true, mCenterPaint);
		canvas.restoreToCount(s1);
	}
	
	// animator callback, do not modify function name	
	public void setCurRadius(int value) {
		mPressedRadius = value;
	}
	
    public float getCurRadius() {
        return mPressedRadius;
    }
}
