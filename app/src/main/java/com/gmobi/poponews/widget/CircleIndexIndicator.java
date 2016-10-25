package com.gmobi.poponews.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.gmobi.poponews.R;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.event.Event;
import com.momock.event.EventArgs;
import com.momock.event.IEvent;
import com.momock.service.IRService;
import com.momock.widget.IIndexIndicator;

public class CircleIndexIndicator extends LinearLayout implements IIndexIndicator{
	
	protected IEvent<IndicatorEvtArgs> indexChangedEvent = new Event<IndicatorEvtArgs>();
	private View self;
	private int count;
	public static class IndicatorEvtArgs extends EventArgs {
		int index;
		public IndicatorEvtArgs(int index) {
			this.index = index;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
	}
	
	public IEvent<IndicatorEvtArgs> getIndexChangedEvent() {
		return indexChangedEvent;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CircleIndexIndicator(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public CircleIndexIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleIndexIndicator(Context context) {
		super(context);
	}

	@Override
	public void setCurrentIndex(int index) {
	
		indexChangedEvent.fireEvent(this, new IndicatorEvtArgs(index));
		TextView tv = (TextView)this.getChildAt(0);
		tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tv.setText((index+1)+"/"+count);
	}

	@Override
	public void setCount(int count) {
		int i;
		if(self==null)
			this.addView(createNewIndicator());

		this.count = count;
		setCurrentIndex(0);

	}
	
	View createNewIndicator(){
		LayoutInflater li = ((Activity)this.getContext()).getLayoutInflater();
		self = li.inflate(R.layout.indicator, null);
		return self;
	}
}
