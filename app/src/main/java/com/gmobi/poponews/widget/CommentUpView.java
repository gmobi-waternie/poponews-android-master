package com.gmobi.poponews.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmobi.poponews.R;


public class CommentUpView extends LinearLayout implements View.OnClickListener{
	private ImageView img;
	private TextView textView,tv_anim;
	private Context context;

	private boolean ding = false;
	private int dingNum = 0;
	
	public CommentUpView(Context context) {
		super(context);
		init(context);
	}

	public CommentUpView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CommentUpView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		inflate(context, R.layout.comment_ding, this);
		img = (ImageView) findViewById(R.id.item_img_ding);
		textView = (TextView) findViewById(R.id.item_tv_ding);
		tv_anim = (TextView) findViewById(R.id.tv_anim);
		setOnClickListener(this);
		
	}


	public void setDing(boolean ding) {
		this.ding = ding;
		if (ding) {
			setClickable(false);
			img.setImageResource(R.drawable.icon_unlike);
		} else {
			setClickable(true);
			img.setImageResource(R.drawable.icon_like);
		}
		
	}


	public void setDingNum(int dingNum) {
		this.dingNum = dingNum;
		textView.setText(dingNum+"");
	}

	private OnUpListener listener;

	public interface OnUpListener { // 内部的监听接口
		public abstract void onClick();
	}

	// 设置按钮被选中的监听
	public void setOnUpListener(OnUpListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		if (listener != null) {
			tv_anim.setVisibility(View.VISIBLE);
			Animation an = AnimationUtils.loadAnimation(context, R.anim.comment_ding);
			tv_anim.startAnimation(an);
			setClickable(false);
			an.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					listener.onClick();
					tv_anim.setVisibility(View.GONE);
					dingNum++;
					setDingNum(dingNum);
					setDing(true);
				}
			});
		}
		
		
		
	}
	
}
