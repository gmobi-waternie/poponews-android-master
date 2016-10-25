package com.gmobi.poponews.util;

import com.gmobi.poponews.model.NewsListItem;



/*
 * For Vogue&GQ Version.
 */

public class RandomLayoutFactory {
	private static final int LAYOUT_COUNT = 4;
	private static int[] layoutTypeArray = {
		NewsListItem.LIST_TYPE_1L,NewsListItem.LIST_TYPE_1R,NewsListItem.LIST_TYPE_2,NewsListItem.LIST_TYPE_3
	};
	private int preIdx = -1;
	private int leftCount = 0;


	public RandomLayoutFactory setLeftItemCount(int c)
	{
		leftCount = c;
		return this;
	}
	
	public RandomLayoutFactory setPreIdx(int idx)
	{
		preIdx = idx;
		return this;
	}
	

	public int getRandomIndex()
	{
		boolean flag = true; 
		int idx = 0;
		while(flag)
		{
			idx = (int)(Math.random()*LAYOUT_COUNT);
			if((idx == preIdx&&preIdx>-1) || idx >= LAYOUT_COUNT || idx < 0)
				continue;

			if(leftCount <3 && idx == 3)
				continue;
			else if(leftCount <2 && idx >1)
				continue;
			
			
			flag = false;
		}
		return idx;
	}
	
	
	
	public int generateLayoutType(int idx)
	{
		return layoutTypeArray[idx];
	}
	
	
	
}
