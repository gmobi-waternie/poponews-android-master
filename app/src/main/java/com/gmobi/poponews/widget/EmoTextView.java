package com.gmobi.poponews.widget;

import java.util.List;

import com.gmobi.poponews.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class EmoTextView extends View{

	private List<EmoItemHolder> emoHolderList = null;
	
	public EmoTextView(Context context) {
		this(context, null);
	}
	
	public EmoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
	
	public EmoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public EmoTextView setData(List<EmoItemHolder> data){
		emoHolderList = data;
		return this;
	}
	
	public EmoTextView addTextView(ViewGroup vg, int index){
		setId(R.id.emo_text);
		if(index < 0){
			vg.addView(this);
		}else{
			vg.addView(this, index);
		}
		return this;
	}
	
	@Override
    protected void onDraw(Canvas canvas){
		if(null == emoHolderList){
			return;
		}
		int s = canvas.save();
		canvas.translate(getWidth() / 2, 0);
		for(EmoItemHolder eih : emoHolderList){
            if(eih.isCenterItem()){
                continue;
            }
			eih.drawText(canvas, isSelected());
		}
		canvas.restoreToCount(s);
	}
}
