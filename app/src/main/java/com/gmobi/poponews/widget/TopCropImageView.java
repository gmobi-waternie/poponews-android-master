package com.gmobi.poponews.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by v3450 on 2016/7/13.
 */
public class TopCropImageView extends ImageView {


	public TopCropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setScaleType(ScaleType.MATRIX);
	}

	public TopCropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
	}

	public TopCropImageView(Context context) {
		super(context);
		setScaleType(ScaleType.MATRIX);
	}

	//
//	@Override
//	protected boolean setFrame(int l, int t, int r, int b) {
//		if (getDrawable() == null) {
//			return super.setFrame(l, t, r, b);
//		}
//		Matrix matrix = getImageMatrix();
//
//		float scaleWidth = getWidth() / (float) getDrawable().getIntrinsicWidth();
//		float scaleHeight = getHeight() / (float) getDrawable().getIntrinsicHeight();
//		float scaleFactor = (scaleWidth > scaleHeight) ? scaleWidth : scaleHeight;
//		matrix.setScale(scaleFactor, scaleFactor, 0, 0);
//
//
//		if (scaleFactor == scaleHeight) {
//			float tanslateX = ((getDrawable().getIntrinsicWidth() * scaleFactor) - getWidth()) / 2;
//			matrix.postTranslate(-tanslateX, 0);
//		}
//
//		setImageMatrix(matrix);
//
//		return super.setFrame(l, t, r, b);
//	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		recomputeImgMatrix();
	}

	@Override
	protected boolean setFrame(int l, int t, int r, int b) {
		recomputeImgMatrix();
		return super.setFrame(l, t, r, b);
	}

	private void recomputeImgMatrix() {
		final Matrix matrix = getImageMatrix();

		float scale;
		final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		final int drawableWidth = getDrawable().getIntrinsicWidth();
		final int drawableHeight = getDrawable().getIntrinsicHeight();

		if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
			scale = (float) viewHeight / (float) drawableHeight;
		} else {
			scale = (float) viewWidth / (float) drawableWidth;
		}

		matrix.setScale(scale, scale);
		setImageMatrix(matrix);
	}
}
