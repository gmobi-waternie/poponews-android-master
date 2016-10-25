package com.gmobi.poponews.widget;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.model.UninterestReason;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.DipHelper;
import com.momock.app.App;
import com.momock.service.IRService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

/**
 */
public class MenuInteresting extends MenuPopupWindow implements View.OnClickListener{
	IUserService userService;
	IDataService dataService;
	IReportService reportService;

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
	private ViewGroup llLeft, llRight;
	private TextView tv_num,tv_confirm;

	private ViewGroup mTrack, mTrackSec;
	private NewsItem newsItem;

	private ArrayList<UninterestReason> mReasonList = new ArrayList<>();
	private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
	private DBHelper dh;


	IRService getRService()	{
		return  App.get().getService(IRService.class);
	}
	/**
	 * Constructor
	 *
	 * @param anchor {@link View} on where the popup window should be displayed
	 */
	public MenuInteresting(View anchor, NewsItem n) {
		super(anchor);
		this.newsItem = n;

		userService = App.get().getService(IUserService.class);
		dataService = App.get().getService(IDataService.class);
		reportService= App.get().getService(IReportService.class);
		dh = DBHelper.getInstance();

		context		= anchor.getContext();
		inflater 	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		root		= inflater.inflate(R.layout.news_uninteresting_menu, null);
		mReasonList.clear();
		setContentView(root);
//		llLeft = (ViewGroup) root.findViewById(R.id.ll_reason_left);
//		llRight = (ViewGroup) root.findViewById(R.id.ll_reason_right);

		tv_confirm = (TextView) root.findViewById(R.id.tv_news_item_confirm);
//		tv_num = (TextView) root.findViewById(R.id.tv_news_item_num);


		animStyle		= ANIM_AUTO;
		anchor.setOnClickListener(this);
		tv_confirm.setOnClickListener(this);
	}


	/*private CheckBox createReasonCheckbox(String r)
	{
		CheckBox cb= new CheckBox(context);
		cb.setText(r);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(20, 10, 20, 10);
		cb.setGravity(Gravity.CENTER);
		cb.setLayoutParams(layoutParams);
		cb.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
		cb.setButtonDrawable(App.get().getResources().getDrawable(android.R.color.transparent));
		cb.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.checkbox_news_item_selector));
//		Drawable drawable = App.get().getResources().getDrawable(R.drawable.checkbox_news_item_text_selector);
		cb.setTextColor(App.get().getResources().getColorStateList(R.drawable.checkbox_news_item_text_selector));
		cb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int count = 0;
				for (int i = 0; i < checkBoxes.size(); i++) {
					if (checkBoxes.get(i).getId() == v.getId()) {
						mReasonList.get(i).setSelected(checkBoxes.get(i).isChecked());
					}

					if (checkBoxes.get(i).isChecked()) {
						count++;
					}

				}

				if (count > 0) {
					tv_num.setText(App.get().getResources().getString(R.string.uninteresting_menu_selected));
					tv_num.append(Html.fromHtml("<font color=\'#ff4131'>" + count + "</font>"));
					tv_num.append(App.get().getResources().getString(R.string.uninteresting_menu_optional));
					tv_confirm.setText(App.get().getResources().getString(R.string.uninteresting_menu_confirm));
				} else {
					tv_num.setText(App.get().getResources().getString(R.string.uninteresting_menu_optional_reason));
					tv_confirm.setText(App.get().getResources().getString(R.string.uninteresting_menu_uninterest));
				}
			}
		});
		return cb;
	}*/

	/*public void addReason(String r)
	{
		UninterestReason utr = new UninterestReason(r,false);
		mReasonList.add(utr);
		CheckBox cb = createReasonCheckbox(r);
		checkBoxes.add(cb);
		if(mReasonList.size()  % 2 == 0)
		{
			llRight.addView(cb);
		}
		else
		{
			llLeft.addView(cb);
		}


	}*/
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.news_uninteresting:
				show();
				break;
			case R.id.tv_news_item_confirm:
				CommentUserInfo info = userService.getUserInfo();
				if (info != null){
					dh.setUninterest(info.getUId(), newsItem.get_id());
				} else {
					dh.setUninterest(GlobalConfig.USER_ID, newsItem.get_id());
				}
				dataService.removeUninterestItem(newsItem);
				reportService.recordUninterest(newsItem.get_id(),newsItem.get_cid());
				AnalysisUtil.recordNewsClose(newsItem.get_id(),newsItem.getTitle());
//				Toast.makeText(App.get().getCurrentContext(), App.get().getResources().getString(R.string.uninteresting_menu_thank), Toast.LENGTH_SHORT).show();
				window.dismiss();
				break;
		}
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
		int rootWidth		= DipHelper.dip2px(100);

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
		window.setAnimationStyle(R.style.MyPopupInterest);
		WindowManager.LayoutParams lp = App.get().getCurrentActivity().getWindow().getAttributes();
		lp.alpha = 0.6f;
		App.get().getCurrentActivity().getWindow().setAttributes(lp);

		window.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorRect.right - rootWidth, anchorRect.bottom);
		window.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				WindowManager.LayoutParams lp = App.get().getCurrentActivity().getWindow().getAttributes();
				lp.alpha = 1f;
				App.get().getCurrentActivity().getWindow().setAttributes(lp);
			}
		});
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