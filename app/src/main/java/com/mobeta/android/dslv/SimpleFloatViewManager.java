package com.mobeta.android.dslv;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Color;
import android.widget.ListView;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.util.NightModeUtil;
import com.momock.app.App;

/**
 * Simple implementation of the FloatViewManager class. Uses list
 * items as they appear in the ListView to create the floating View.
 */
public class SimpleFloatViewManager implements DragSortListView.FloatViewManager {

    private Bitmap mFloatBitmap;

    private ImageView mImageView;

    private int mFloatBGColor = Color.GRAY;

    private ListView mListView;

    public SimpleFloatViewManager(ListView lv) {
        mListView = lv;
    }

    public void setBackgroundColor(int color) {
        mFloatBGColor = color;
    }

    /**
     * This simple implementation creates a Bitmap copy of the
     * list item currently shown at ListView <code>position</code>.
     */
    @Override
    public View onCreateFloatView(int position) {
        // Guaranteed that this will not be null? I think so. Nope, got
        // a NullPointerException once...
        View v = mListView.getChildAt(position + mListView.getHeaderViewsCount() - mListView.getFirstVisiblePosition());

        if (v == null) {
            return null;
        }

        DragSortItemViewCheckable item = (DragSortItemViewCheckable) v;
        ViewGroup vg = (ViewGroup) item.getChildAt(0);

        View childView1  = vg.getChildAt(0);
        View childView2  = vg.getChildAt(1);
        if(childView1.getTag().equals("TextView"))
        {
            TextView tv = (TextView)childView1;
            tv.setTextColor(App.get().getResources().getColor(R.color.bg_red));
        }
        else if(childView2.getTag().equals("TextView"))
        {
            TextView tv = (TextView)childView2;
            tv.setTextColor(App.get().getResources().getColor(R.color.bg_red));
        }

        v.setPressed(false);

        // Create a copy of the drawing cache so that it does not get
        // recycled by the framework when the list tries to clean up memory
        //v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        v.setDrawingCacheEnabled(true);
        mFloatBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        if(childView1.getTag().equals("TextView"))
        {
            TextView tv = (TextView)childView1;
            //tv.setTextColor(App.get().getResources().getColor(R.color.bg_black));
            tv.setTextColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_white_night) : App.get().getResources().getColor(R.color.bg_black));
        }
        else if(childView2.getTag().equals("TextView"))
        {
            TextView tv = (TextView)childView2;
            //tv.setTextColor(App.get().getResources().getColor(R.color.bg_black));
            tv.setTextColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_white_night) : App.get().getResources().getColor(R.color.bg_black));
        }

        if (mImageView == null) {
            mImageView = new ImageView(mListView.getContext());
        }
        mImageView.setBackgroundResource(R.drawable.shadow_box);
        mImageView.setPadding(0, 0, 0, 0);
        mImageView.setImageBitmap(mFloatBitmap);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(v.getWidth(), v.getHeight()));

        return mImageView;
    }

    /**
     * This does nothing
     */
    @Override
    public void onDragFloatView(View floatView, Point position, Point touch) {
        // do nothing
    }

    /**
     * Removes the Bitmap from the ImageView created in
     * onCreateFloatView() and tells the system to recycle it.
     */
    @Override
    public void onDestroyFloatView(View floatView) {
        ((ImageView) floatView).setImageDrawable(null);

        mFloatBitmap.recycle();
        mFloatBitmap = null;
    }

}

