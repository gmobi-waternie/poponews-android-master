package com.gmobi.poponews.widget;

import java.util.ArrayList;
import java.util.List;

import com.gmobi.poponews.R;
import com.momock.util.Convert;
import com.momock.util.Logger;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

public class EmoMenu extends FrameLayout {

	public interface IEmoListener{
		void onClicked(int emoId);
	}

    public static class EmoData{
        private static int total;
        private int id;
        private int vote;

    }

	private List<EmoItemHolder> itemList = new ArrayList<EmoItemHolder>();
	private int mInnerRadius, mOuterRadius, mMaxOuterRadius, mIsAnimaRunning;
	private Point mCenter;
    private boolean mIsMenuOpen, mIsReadOnly;
    private EmoTextView emoTextView;
    private EmoBackgroundView emoBgView;
    private EmoCenterView emoCenterView;
    private View mSelected, mSelectDefault;  
    IEmoListener clickProcess = null;
    private static Handler closeHandler = new Handler();
    private Runnable closeDelyCallback = new Runnable() {
        @Override
        public void run() {
            if(isMenuOpen()){
                setMenuOpen(false, false, 0);
            }
        }
    };
	private AnimatorListener animaSelectListener = new AnimatorListener(){

		@Override
		public void onAnimationStart(Animator animation) {
			mIsAnimaRunning = 1;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mIsAnimaRunning = 0;
			if(!mSelected.isSelected()){
                mSelected.setSelected(true);
                mSelected.setScaleX(1.2f);
                mSelected.setScaleY(1.2f);
                // set select item
                if(!mIsReadOnly) {
                    if (null != clickProcess && null != mSelected) {
                        clickProcess.onClicked(mSelected.getId());
                        closeHandler.postDelayed(closeDelyCallback, 3000);
                    }
                    mIsReadOnly = true;
                }
                emoTextView.setSelected(mIsReadOnly);
                emoTextView.invalidate();
                emoCenterView.invalidate();
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mIsAnimaRunning = 0;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
		
	};
	
    private AnimationListener animaOpenListener = new AnimationListener(){

		@Override
		public void onAnimationStart(Animation animation) {
			mIsAnimaRunning++;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mIsAnimaRunning--;
			if(mIsAnimaRunning == 0){
				if(null != mSelectDefault){
					setItemSelect((EmoItemHolder) mSelectDefault.getTag());
				}
				if(null != emoTextView){
					emoTextView.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
    	
    };
    
	public EmoMenu(Context context) {
		this(context, null);
	}
	
	public EmoMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
	
	public EmoMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(true);
        setDrawingCacheEnabled(true);
		mCenter = new Point(0, 0);	
        mIsMenuOpen = false;
        mIsAnimaRunning = 0;
        mIsReadOnly = false;
        mSelectDefault = null;
        mSelected = null;
        setVisibility(View.INVISIBLE);
        addMenuItems(getResources());
	}
	
	RectF innerRect = new RectF();

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCenter.x = w / 2;
		mMaxOuterRadius = Math.min(mCenter.x - getPaddingLeft(), h);
		mOuterRadius = (int) (mMaxOuterRadius * 0.9);
		mInnerRadius = mOuterRadius / 3;
		for(EmoItemHolder eih : itemList){
			eih.onRadiusChanged(mInnerRadius, mOuterRadius);
        }
		if(null != emoBgView){
			emoBgView.onRadiusChanged(mInnerRadius, mOuterRadius, mMaxOuterRadius);
		}
        if(null != emoCenterView){
            emoCenterView.onRadiusChanged(mInnerRadius);
        }
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		//super.onLayout(changed, left, top, right, bottom);
		final int count = getChildCount();
		for(int i = 0; i < count; i++){
			final View v = getChildAt(i);
			switch(v.getId()){
            case R.id.emo_center_heart:
			case R.id.emo_background:
			case R.id.emo_text:
				v.layout(left, 0, right, bottom);
				break;
			default:
				final EmoItemHolder eih = (EmoItemHolder) v.getTag();
				eih.layoutIcon(mCenter, v);
				break;
			}
		}
		
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent evt) {
        int x = (int) evt.getX();
        int y = (int) evt.getY();
        int action = evt.getActionMasked();
        if (MotionEvent.ACTION_DOWN == action) {
        	
        } else if (MotionEvent.ACTION_UP == action) {
            Log.d("touch", "up");
            if(mIsMenuOpen && mIsAnimaRunning <= 0){
            	EmoItemHolder eih = findItem(x, y);
	            if(null == eih){
	            	setMenuOpen(false, false, -1);
	            }else if(mIsReadOnly){
	            	Logger.debug("read only, do nothing when touch it");
	            }else if(R.id.emo_center_heart == eih.getEmoId()){
	            	if(null != clickProcess && null != mSelected){
        				clickProcess.onClicked(mSelected.getId());
        			}
	            	setMenuOpen(false, false, -1);
	            }else{
	            	setItemSelect(eih);
	            }
            }
            
        } else if (MotionEvent.ACTION_CANCEL == action) {
//	        	mIsTouchMove = true;
        } else if (MotionEvent.ACTION_MOVE == action) {

        }
        return mIsMenuOpen;
    }

	private void setItemSelect(EmoItemHolder eih){
		
		if(null != mSelected){
			if(null != eih && mSelected.getId() == eih.getEmoId()){
				Log.d("set select", "is selected");
				return;
			}
			if(mSelected.isSelected()){
                mSelected.setScaleX(1.0f);
                mSelected.setScaleY(1.0f);
				mSelected.setSelected(false);
			}
			mSelected = null;
		}
		
		if(null != emoBgView){
			if(null != eih){
				mSelected = findViewById(eih.getEmoId());
			}
			emoBgView.startItemSelectedAnima(eih);
		}
	}
	
	private EmoItemHolder findItem(float x, float y){
		Log.d("findItem", "x, y = " + x + " ," + y);
        x -= mCenter.x;
        y -= mCenter.y; 
        int r = (int) Math.sqrt(x * x + y * y);
        if(r < mInnerRadius){
        	for(EmoItemHolder eih : itemList){
        		if(eih.isCenterItem()){
        			return eih;
        		}
        	}
        }else if(r < mOuterRadius){
        	int angle = (int) Math.toDegrees(Math.atan2(y, x));
        	for(EmoItemHolder eih : itemList){
        		if(angle > eih.getStartAngle() && angle < eih.getEndAngle()){
        			return eih;
        		}
        	}
        }
        return null;
	}

	/**
	 * TODO: change to add items in layout xml
	 */
	private void addMenuItems(Resources res){
		DisplayMetrics dm = res.getDisplayMetrics();
		int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm); 
		int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, dm);
	    int centerTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, dm);

	    Paint textCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
	    textCenter.setColor(Color.BLACK);
	    textCenter.setStyle(Style.FILL);
	    textCenter.setTextSize(centerTextSize);
	    textCenter.setTextAlign(Paint.Align.LEFT);
        
	    Paint textNormal = new Paint(Paint.ANTI_ALIAS_FLAG);
	    textNormal.setColor(Color.WHITE);
	    textNormal.setStyle(Style.FILL);
	    textNormal.setTextSize(textSize);
	    textNormal.setTextAlign(Paint.Align.LEFT);
	    
	    emoBgView = new EmoBackgroundView(getContext()).setData(itemList)
	    		.addAnimaListener(animaSelectListener).addBackgroundView(this, -1);

		TypedArray menu = res.obtainTypedArray(R.array.emo_item);
		for(int i = 0; i < menu.length(); i++) {
            int start = i;
            int id = menu.getResourceId(i++, 0);
            String name = menu.getString(i++);
            int icon = menu.getResourceId(i, 0);
            itemList.add(EmoItemHolder
                    .createNormalItem(id, start * 10, 30, icon, iconSize, textNormal, textNormal)
                    .setTextName(name).addIconView(this).setTextInfo("88%"));
        }
        itemList.add(EmoItemHolder.createCenterItem(R.id.emo_center_heart, textCenter, textCenter).addIconView(this));
        emoCenterView = (EmoCenterView)findViewById(R.id.emo_center_heart);
        //emoCenterView = new EmoCenterView(getContext()).addCenterView(this, -1);
        emoTextView = new EmoTextView(getContext()).setData(itemList).addTextView(this, -1);
		menu.recycle();	
	}
	
	public void setEmoListener(IEmoListener iemcl){
		clickProcess = iemcl;
	}
	
	public boolean isMenuOpen(){
		return mIsMenuOpen;
	}
	
	public void setMenuOpen(boolean open, boolean readOnly, int defSelect){
		if(mIsAnimaRunning > 0){
			return;
		}else{
			mIsAnimaRunning = 0;
		}
		mIsMenuOpen = open;
		if(mIsMenuOpen){
			setItemSelect(null);
			setVisibility(View.VISIBLE);
			mIsReadOnly = readOnly;
			mSelectDefault = null;
			for(int i = 0; i < getChildCount(); i++){
				View tmpView = getChildAt(i);
            	switch(tmpView.getId()){
                case R.id.emo_center_heart:
                    tmpView.invalidate();
                    tmpView.setSelected(mIsReadOnly);
            	case R.id.emo_background:
            		continue;
            	case R.id.emo_text:
            		tmpView.setVisibility(View.INVISIBLE);
            		break;
            	default:
            		EmoItemHolder eih = (EmoItemHolder) tmpView.getTag();
	            	eih.startItemOpenAnima(tmpView, animaOpenListener);
	            	if(defSelect == eih.getEmoId()){
	            		mSelectDefault = tmpView;
            		}
	            	break;
            	}
            }
	        
        }else{
            closeHandler.removeCallbacks(closeDelyCallback);
        	setVisibility(View.INVISIBLE);
        	mIsReadOnly = false;
        	setItemSelect(null);
        }
	}

    public boolean initVoteMenuData(SparseArray<Integer> data, int vote){
        int total = data.get(R.id.emo_center_heart);

        for(EmoItemHolder eih : itemList){
            if(R.id.emo_center_heart == eih.getEmoId()){
                if(vote > 0){
                    eih.setTextInfo("" + total);
                    eih.setTextName("VOTES");
                }else{
                    eih.setTextInfo("Please");
                    eih.setTextName("Vote");
                }
            }else{
                if(vote > 0){
                	if(total != 0)
                		eih.setTextInfo(data.get(eih.getEmoId()) * 100 / total + "%");
                	else
                		eih.setTextInfo("100%");
                }else{
                    eih.setTextInfo(null);
                }
            }
        }
        return vote > 0;
    }
	
}
