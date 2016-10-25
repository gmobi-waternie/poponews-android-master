package com.gmobi.poponews.widget;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.momock.app.App;
import com.momock.service.IRService;

/**
 */
public class MenuSetting extends MenuPopupWindow {
	private final View root;
//	private final ImageView mArrowUp;
//	private final ImageView mArrowDown;
	private final LayoutInflater inflater;
	private final Context context;
	
	protected static final int ANIM_GROW_FROM_LEFT = 1;
	protected static final int ANIM_GROW_FROM_RIGHT = 2;
	protected static final int ANIM_GROW_FROM_CENTER = 3;
	protected static final int ANIM_REFLECT = 4;
	protected static final int ANIM_AUTO = 5;
	
	private int animStyle;
	private ViewGroup mTrack, mTrackSec;

	IRService getRService()	{
		return  App.get().getService(IRService.class);
	}
	/**
	 * Constructor
	 * 
	 * @param anchor {@link View} on where the popup window should be displayed
	 */
	public MenuSetting(View anchor,int Layoutid) {
		super(anchor);
		

		context		= anchor.getContext();
		inflater 	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		root		= inflater.inflate(Layoutid, null);
				
		setContentView(root);
	    
		animStyle		= ANIM_AUTO;
	}

	/**
	 * Set animation style
	 * 
	 * @param animStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	
	/**
	 * Show popup window. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show () {
		preShow();
		
		int xPos, yPos;
		
		int[] location 		= new int[2];
	
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		
		
		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		int rootHeight 		= root.getMeasuredHeight();
		int rootWidth		= root.getMeasuredWidth();
		
		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		int screenHeight	= windowManager.getDefaultDisplay().getHeight();
		
		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos = anchorRect.left - (rootWidth-anchor.getWidth());
		} else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}
		}
		
		int dyTop			= anchorRect.top;
		int dyBottom		= screenHeight - anchorRect.bottom;

		boolean onTop		= (dyTop > dyBottom) ? true : false;

		if (onTop) {
			if (rootHeight > dyTop) {
				yPos = 0;
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;
			
		}
		
//		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX()-xPos);
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		
		window.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorRect.right - rootWidth, anchorRect.bottom);
	}
	
	/**
	 * Set animation style
	 * 
	 * @param screenWidth screen width
	 * @param requestedX distance from left edge
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
	 * 		  and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX;

		switch (animStyle) {
		case ANIM_GROW_FROM_LEFT:
			window.setAnimationStyle((onTop) ? getRService().getStyle("Animations.PopUpMenu.Left") : getRService().getStyle("Animations.PopDownMenu.Left"));
			break;
					
		case ANIM_GROW_FROM_RIGHT:
			window.setAnimationStyle((onTop) ? getRService().getStyle("Animations.PopUpMenu.Right") : getRService().getStyle("Animations.PopDownMenu.Right"));
			break;
					
		case ANIM_GROW_FROM_CENTER:
			window.setAnimationStyle((onTop) ? getRService().getStyle("Animations.PopUpMenu.Center") : getRService().getStyle("Animations.PopDownMenu.Center"));
		break;
			
		case ANIM_REFLECT:
			//window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
		break;
		
		case ANIM_AUTO:
			/*
			if (arrowPos <= screenWidth/4) {
				window.setAnimationStyle((onTop) ? getRService().getStyle("Animations.PopUpMenu.Left") : getRService().getStyle("Animations.PopDownMenu.Left"));
			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
				window.setAnimationStyle((onTop) ? getRService().getStyle("Animations.PopUpMenu.Center") : getRService().getStyle("Animations.PopDownMenu.Center"));
			} else {
				window.setAnimationStyle((onTop) ? getRService().getStyle("Animations.PopUpMenu.Right") : getRService().getStyle("Animations.PopDownMenu.Right"));
			}
			*/
			break;
		}
		
		
	}
	
	public View findViewById(int id)
	{
		return root.findViewById(id);
	}
	

	
		
}