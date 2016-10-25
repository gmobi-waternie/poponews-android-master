package com.gmobi.poponews.util;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

public class AnimUtils {
	
	public static void ShowLoading(ImageView view,  int resid, boolean visible, boolean onShot)
	{
		if(view == null)
			return;
		
		
		view.setBackgroundResource(resid); 
		AnimationDrawable  loadAnim = (AnimationDrawable)view.getBackground();   
		loadAnim.setOneShot(onShot);  

		if(visible)
		{
			view.setVisibility(View.VISIBLE);
			loadAnim.start();
		}
		else
		{
			loadAnim.stop();
			view.setVisibility(View.GONE);
		}
	}
}
