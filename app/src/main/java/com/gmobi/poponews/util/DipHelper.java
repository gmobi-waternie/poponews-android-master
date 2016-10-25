package com.gmobi.poponews.util;
import android.content.Context;

public class DipHelper {
	
	static float scale = 0;
	
	public static void init(Context ctx){
		scale = ctx.getResources().getDisplayMetrics().density;
	}
	
    public static int dip2px(float dpValue) {
            return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
            return (int) (pxValue / scale + 0.5f);
    }
}
