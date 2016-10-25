package com.gmobi.poponews.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.FontMetricsInt;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class EmoItemHolder {
	
	/**               shape center point
	 *                       /|\
	 *                      / | \
	 *                     /  |  \
	 *                    /   |   \
	 *                   /    |    \
	 *                  /     |     \
	 *                 *______*______* 
	 *                /XXXXXXXXXXXXXXX\
	 *               /XXXXXXXXXXXXXXXXX\
	 *        rpEnd *XXXXXXXXX*XXXXXXXXX* rpStart                                       
	 *                     rpCenter
	 */   
	private static final int REFERENCE_RADIUS = 300;
	private Paint mNamePaint, mInfoPaint;
	private Point mRpStart, mRpEnd, mRpCenter, mRpPosInside, mRpPosOutside;    // Reference Point
	private int mEmoId, mStart, mSweep, mIcon, mIcSize;
	private String mName, mInfo;
	private Point mInfoPos, mIconPos, mNamePos, mLeftDivInside, mLeftDivOutside;    // draw loacation
	private float mRateOfName, mRateOfInfo;
	
	FontMetricsInt mFontMetrics;

	public static EmoItemHolder createCenterItem(int id, Paint namePaint, Paint infoPaint){
		return new EmoItemHolder(id, namePaint, infoPaint);
	}
	
	public static EmoItemHolder createNormalItem(int id, int startAngle, int sweepAngle, int icon, int iconSize, Paint namePaint, Paint infoPaint){
		return new EmoItemHolder(id, startAngle, sweepAngle, icon, iconSize, namePaint, infoPaint);
	}
	
	private EmoItemHolder(int id, int startAngle, int sweepAngle, int icon, int iconSize, Paint namePaint, Paint infoPaint){
		mStart = startAngle % 360;
		mSweep = sweepAngle % 360;
		mIcon = icon;
		mIcSize = iconSize;
		mRateOfName = 1.25f;
		mRateOfInfo = 2.75f;
		mEmoId = id;
		mNamePaint = namePaint;
		mInfoPaint = infoPaint;
		// prepare fm tmp for draw text
		mFontMetrics = new FontMetricsInt();	
		// prepare reference point for better performance
		mRpStart = new Point(0, 0);
		mRpEnd = new Point(0, 0);
		mRpCenter = new Point(0, REFERENCE_RADIUS);	
		if(0 != sweepAngle){
			Double angle;
			angle = Math.toRadians(mStart);
			mRpStart.y = (int) (REFERENCE_RADIUS * Math.sin(angle));
			mRpStart.x = (int) (REFERENCE_RADIUS * Math.cos(angle));
			
			angle = Math.toRadians(mStart + mSweep);
			mRpEnd.y = (int) (REFERENCE_RADIUS * Math.sin(angle));
			mRpEnd.x = (int) (REFERENCE_RADIUS * Math.cos(angle));

			angle = Math.toRadians(mStart + mSweep / 2);
			mRpCenter.y = (int) (REFERENCE_RADIUS * Math.sin(angle));
			mRpCenter.x = (int) (REFERENCE_RADIUS * Math.cos(angle));
		}
		// init other parameters
		mName = null;
		mInfo = null;
		mInfoPos = new Point();
		mIconPos = new Point();
		mNamePos = new Point();
		mRpPosInside = null;
		mRpPosOutside = null;
	}
	
	/**
	 * if this is a center item, use this to create a holder
	 */
	private EmoItemHolder(int id, Paint namePaint, Paint infoPaint){
		this(id, 0, 0, 0, 0, namePaint, infoPaint);
		mRateOfName = 0.4f;
		mRateOfInfo = 0.8f;
	}
	
	public int getEmoId(){
		return mEmoId;
	}
	
	public boolean isCenterItem(){
		return 0 == mSweep;
	}
	
	public EmoItemHolder setTextName(String itemName){
		mName = itemName;
		calcNameLocation(mRpPosOutside, mNamePaint);
		return this;
	}
	
	public EmoItemHolder setTextInfo(String infomation){
		mInfo = infomation;
		calcInfoLocation(mRpPosInside, mInfoPaint);
		return this;
	}
	
	public int getStartAngle(){
		return mStart;
	}
	
	public int getSweepAngle(){
		return mSweep;
	}
	
	public int getEndAngle(){
		return mStart + mSweep;
	}
	
	private Point calcPointInRing(Point rpPosInRing, int rpRingRadius, int targetRingRadius, Point out){
		Point pos;
		if(null != out){
			pos = out;
		}else{
			pos = new Point(0, 0);
		}
		if(rpRingRadius != 0){
			pos.x = targetRingRadius * rpPosInRing.x / rpRingRadius;
			pos.y = targetRingRadius * rpPosInRing.y / rpRingRadius;
		}else{
			pos.set(0, 0);
		}
		return pos;
	}
	
	private int getFontHeight(Paint p){
		p.getFontMetricsInt(mFontMetrics);
		return mFontMetrics.bottom - mFontMetrics.top;
	}
	
	private void calcReferencePoint(int insideRadius, int outSideRadius){
		if(isCenterItem()){
			outSideRadius = insideRadius;
			insideRadius = 0;
		}
		int referenceHeight = outSideRadius - insideRadius;
        if(mIcSize > 0){
            referenceHeight = mIcSize;
        }
		mRpPosInside = calcPointInRing(mRpCenter, REFERENCE_RADIUS,
				(int)(outSideRadius - referenceHeight  * mRateOfInfo), mRpPosInside);
		mRpPosOutside = calcPointInRing(mRpCenter, REFERENCE_RADIUS, 
				(int)(outSideRadius - referenceHeight * mRateOfName), mRpPosOutside);
		mLeftDivInside = calcPointInRing(mRpEnd, REFERENCE_RADIUS, insideRadius, mLeftDivInside);
		mLeftDivOutside = calcPointInRing(mRpEnd, REFERENCE_RADIUS, outSideRadius, mLeftDivOutside);	
	}
	
	private void calcInfoLocation(Point rpPos, Paint p){
		// calc info text pos(left top)
		if(null != mInfo && null != p && null != rpPos){
			int strHeight = getFontHeight(p);
			int strWidth = (int) p.measureText(mInfo);
			mInfoPos.x = rpPos.x - strWidth / 2;
			mInfoPos.y = rpPos.y + strHeight / 2;
		}
	}
	
	private void calcIconLocation(Point rpPos, Paint fontPaint){
		// calc info icon pos(left top)
		if(mIcSize > 0 && null != rpPos){
			int strHeight = getFontHeight(fontPaint);
			mIconPos.y = rpPos.y - (strHeight + mIcSize) / 2;
			mIconPos.x = rpPos.x - mIcSize / 2;
		}
	}
	
	private void calcNameLocation(Point rpPos, Paint p){
		// calc info name pos(left top)
		if(null != mName && null != p && null != rpPos){
			int tmpX;
			int strHeight = getFontHeight(p);
			int nameWidth = (int) p.measureText(mName);
			int halfNameWidth = nameWidth / 2;
			// add half font space between icon and name
			mNamePos.y = rpPos.y + (strHeight + mIcSize) / 2;
			if(!isCenterItem()){
				int angleJudge = mStart + mSweep / 2;
				if(angleJudge < 90){
					if(mRpEnd.x < rpPos.x){
						mNamePos.x = rpPos.x - halfNameWidth;
					}else{
						// add half font space in the begin of text
						tmpX = (mNamePos.y * mRpEnd.x / mRpEnd.y + strHeight / 2);
						if(tmpX + halfNameWidth < mNamePos.x){
							mNamePos.x = rpPos.x - halfNameWidth;
						}else{
							mNamePos.x = tmpX;
						}
					}		
				}else if (angleJudge < 180){
					if(mRpStart.x > rpPos.x){
						mNamePos.x = rpPos.x - halfNameWidth;
					}else{
						// add half font space in the begin of text
						tmpX = (mNamePos.y * mRpStart.x / mRpStart.y - strHeight / 2);
						if(tmpX - halfNameWidth > mNamePos.x){
							mNamePos.x = rpPos.x - halfNameWidth;
						}else{
							mNamePos.x = tmpX - nameWidth;
						}
					}	
				}else {
					// TODO: do not use in this project, maybe finished it in later
					mName = null;
				}
			}else{
				mNamePos.x = rpPos.x - nameWidth / 2;
			}
		}
	}
	
	public void onRadiusChanged(int insideRadius, int outSideRadius){
		calcReferencePoint(insideRadius, outSideRadius);
		calcInfoLocation(mRpPosInside, mInfoPaint);
		calcIconLocation(mRpPosOutside, mNamePaint);
		calcNameLocation(mRpPosOutside, mNamePaint);
	}
	
	public void drawLeftDividers(Canvas canvas, Paint p){
		if(!isCenterItem() && getEndAngle() < 180){
			canvas.drawLine(mLeftDivInside.x, mLeftDivInside.y, mLeftDivOutside.x, mLeftDivOutside.y, p);
		}
	}
	
	public void drawText(Canvas canvas, boolean showInfo){
		if(null != mInfo  && showInfo){
			canvas.drawText(mInfo, mInfoPos.x, mInfoPos.y, mInfoPaint);
		}
		if(null != mName){
			canvas.drawText(mName, mNamePos.x, mNamePos.y, mNamePaint);
		}
	}
	
	public EmoItemHolder addIconView(ViewGroup vg){
		if(!isCenterItem()){
			ImageView iv = new ImageView(vg.getContext());
			iv.setId(mEmoId);
			iv.setImageResource(mIcon);
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			iv.setTag(this);
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(mIcSize, mIcSize);
			iv.setLayoutParams(lp);
			vg.addView(iv);
		}else{
            EmoCenterView center = new EmoCenterView(vg.getContext());
            center.setId(mEmoId);
            center.setTag(this);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            center.setLayoutParams(lp);
            vg.addView(center);
        }
		return this;
	}
	
	public void layoutIcon(Point center, View ic){
		ic.layout(center.x + mIconPos.x, center.y + mIconPos.y, 
				center.x + mIconPos.x + ic.getMeasuredWidth(), 
				center.y + mIconPos.y + ic.getMeasuredHeight());
	}
	
	public void startItemOpenAnima(View v, AnimationListener al){
		if(isCenterItem()){
			// there is no animation in center item
			al.onAnimationStart(null);
			al.onAnimationEnd(null);
			return;
		}

		AnimationSet as= new AnimationSet(true);
		Animation rotate = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		TranslateAnimation translation = new TranslateAnimation(
				-(mIconPos.x + v.getMeasuredWidth() / 2), 0, 
				-(mIconPos.y + v.getMeasuredHeight() / 2), 0);
		as.addAnimation(rotate);
		as.addAnimation(translation);
		as.setDuration(500);
		as.setAnimationListener(al);
		v.startAnimation(as);
	}
	
}
