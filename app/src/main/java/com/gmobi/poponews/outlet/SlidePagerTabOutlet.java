package com.gmobi.poponews.outlet;

import java.lang.ref.WeakReference;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.gmobi.poponews.model.NewsCategory;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.holder.ViewHolder;
import com.momock.outlet.IPlug;
import com.momock.outlet.Outlet;
import com.momock.outlet.card.ICardOutlet;
import com.momock.outlet.card.ICardPlug;
import com.momock.outlet.tab.ITabPlug;
import com.momock.util.Logger;

public class SlidePagerTabOutlet extends Outlet implements ICardOutlet{	
	WeakReference<ViewPager> refTarget = null;

	public void attach(ViewHolder target, final DataList<NewsCategory> ds) {
		attach((ViewPager)target.getView(),ds);
	}
	public void attach(ViewPager target, final DataList<NewsCategory> ds) {
		boolean reset = refTarget != null && refTarget.get() != target;
		refTarget = new WeakReference<ViewPager>(target);
		ViewPager pager = target;
		pager.setAdapter(new PagerAdapter(){
			@Override
			public CharSequence getPageTitle(int position) {
				return ds.getItem(position).getname();
			}
			
			@Override
			public int getCount() {
				return getPlugs().getItemCount();
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ICardPlug plug = (ICardPlug)getPlugs().getItem(position);
				View view = ((ViewHolder)plug.getComponent()).getView();
	            container.addView(view);
				return view;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView((View)object);
			}
		});
		
		
		IDataList<IPlug> plugs = getPlugs();
		for(int i = 0; i < plugs.getItemCount(); i++){
			ICardPlug plug = (ICardPlug)plugs.getItem(i);
			if (reset) ((ViewHolder)plug.getComponent()).reset(); 
			if (plug == this.getActivePlug()){
				onActivate(plug);
			}
		}
	}

	@Override
	public void onActivate(IPlug plug) {
		if (((ICardPlug)plug).getComponent() != null){
			ViewPager pager = refTarget.get();
			pager.setCurrentItem(getIndexOf(plug), true);
		} else {
			Logger.debug("The active plug in PagerCardOutlet has not been attached!");
		}
	}
	
}
