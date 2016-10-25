package com.gmobi.poponews.util;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.ConfigNames;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.app.CaseActivity;
import com.momock.holder.ViewHolder;
import com.momock.util.Convert;

import android.app.Activity;  
import android.content.Context;  
import android.content.Intent;  
import android.content.SharedPreferences;  
import android.provider.Settings;
import android.view.View;  
import android.view.WindowManager;
import android.widget.TextView;  


public class NightModeUtil {
    public final static int THEME_SUN = 1;  
  
    public final static int THEME_NIGHT = 2;  
  
    public static void changeToTheme(Activity activity) {  
        int theme1 = getDayNightMode();  
        int theme = (theme1 == THEME_SUN ? THEME_NIGHT : THEME_SUN);  
        setDayNightMode(theme);  
  
        activity.finish();  
        activity.startActivity(new Intent(activity, activity.getClass()));  
    }  
  
  
    public static void onActivityCreateSetTheme(Activity activity) {  
        int theme = getDayNightMode();  
        switch (theme) {  
        case THEME_SUN:  
            activity.setTheme(R.style.DayTheme);
            break;  
        case THEME_NIGHT:  
            activity.setTheme(R.style.NightTheme);
            break;  
        default:  
            break;  
        }  
    }  
    
    public final static int COLOR_BLACK = 0;
    public final static int COLOR_WHITE = 1;
    public final static int COLOR_RED = 2;
    public final static int COLOR_DARK_RED = 3;
    public final static int COLOR_YELLOW = 4;
    public final static int COLOR_GREY = 5;
    
    private static int[][] colorArray = {
    	{R.color.bg_black,R.color.bg_black_night},
    	{R.color.bg_white,R.color.bg_black_night},
    	{R.color.bg_red,R.color.bg_red_night},
    	{R.color.bg_dark_red,R.color.bg_dark_red_night},
    	{R.color.bg_yellow,R.color.bg_yellow_night},
    	{R.color.bg_grey,R.color.bg_grey_night}
    };
  
    public static void setBackGroundColor(Context context, View view, int color) {
    	int theme = getDayNightMode();
        int bgColor = context.getResources().getColor(  
                theme == THEME_SUN ? colorArray[color][0] : colorArray[color][1]);  
        view.setBackgroundColor(bgColor);  
    }  
  
    public static void setTextColor(Context context, View view, int color) {
    	int theme = getDayNightMode();
        int textcolor = context.getResources().getColor(  
        		theme == THEME_SUN ? colorArray[color][0] : colorArray[color][1]);  
        TextView textView = (TextView) view;  
        textView.setTextColor(textcolor);  
    }  
    
    
    public static void setActionBarColor(Context ctx, int viewId)
    {
    	int nightMode = getDayNightMode();
    	View rlActionBar = ViewHolder.get((CaseActivity)ctx, viewId).getView();
        if(rlActionBar == null)
            return;

		if(nightMode == THEME_NIGHT)
		{
			rlActionBar.setBackgroundColor(ctx.getResources().getColor(R.color.action_bg_night));
		}
		else
		{
			rlActionBar.setBackgroundColor(ctx.getResources().getColor(R.color.action_bg));
		}
    }
    
    public static void setViewColor(Context ctx, int viewId,int sunResColor,int nightResColor)
    {
    	int nightMode = getDayNightMode();
    	View view = ViewHolder.get((CaseActivity)ctx, viewId).getView();
		if(view == null)
			return;
		if(nightMode == THEME_SUN)
		{
			if(view instanceof TextView)
				((TextView)view).setTextColor(sunResColor);
			else	
				view.setBackgroundColor(sunResColor);
		}
		else
		{
			if(view instanceof TextView)
				((TextView)view).setTextColor(nightResColor);
			else	
				view.setBackgroundColor(nightResColor);
		}
    }
    
  
    public static int getSwitchDayNightMode() {  
        int mode = getDayNightMode();  
        return mode == THEME_SUN ? THEME_NIGHT : THEME_SUN;  
    }  
  
    public static void setDayNightMode(int mode) {  
        App.get().getSettings().setProperty(ConfigNames.SETTINGS_KEY_NIGHTMODE, mode);
        if(mode == THEME_NIGHT)
            App.get().setTheme(R.style.NightTheme);
        else
            App.get().setTheme(R.style.DayTheme);
    }  
  
    public static int getDayNightMode() {  
    	return App.get().getSettings().getIntProperty(ConfigNames.SETTINGS_KEY_NIGHTMODE, THEME_SUN);
    }  
    
    public static boolean isNightMode() {  
    	return getDayNightMode() == THEME_NIGHT ? true :false; 
    }  
    
  
    
    public static void setBrightness(int b)
	{
		int curb = Settings.System.getInt(App.get().getContentResolver(),  
	            Settings.System.SCREEN_BRIGHTNESS, 255);
		App.get().getSettings().setProperty(ConfigNames.SETTINGS_KEY_BRIGHTNESS, curb);
		
		// 当进度小于80时，设置成80，防止太黑看不见的后果。  
        if (b < 100) {  
            b = 100;  
        }  

        // 根据当前进度改变亮度  
        Settings.System.putInt(App.get().getContentResolver(),  
                Settings.System.SCREEN_BRIGHTNESS, b);  
        int tmpInt = Settings.System.getInt(App.get().getContentResolver(),  
                Settings.System.SCREEN_BRIGHTNESS, -1);  
        WindowManager.LayoutParams wl = App.get().getCurrentActivity().getWindow().getAttributes();  

        float tmpFloat = (float) tmpInt / 255;  
        if (tmpFloat > 0 && tmpFloat <= 1) {  
            wl.screenBrightness = tmpFloat;  
        }  
        App.get().getCurrentActivity().getWindow().setAttributes(wl); 
		
		
		
	}
	public static void resetBrightness()
	{
		int curb =  App.get().getSettings().getIntProperty(ConfigNames.SETTINGS_KEY_BRIGHTNESS, 255);
		
		Settings.System.putInt(App.get().getContentResolver(),  
                Settings.System.SCREEN_BRIGHTNESS, curb);  
        int tmpInt = Settings.System.getInt(App.get().getContentResolver(),  
                Settings.System.SCREEN_BRIGHTNESS, -1);  
        WindowManager.LayoutParams wl = App.get().getCurrentActivity().getWindow().getAttributes();  

        float tmpFloat = (float) tmpInt / 255;  
        if (tmpFloat > 0 && tmpFloat <= 1) {  
            wl.screenBrightness = tmpFloat;  
        }  
        App.get().getCurrentActivity().getWindow().setAttributes(wl); 
		
	}
	
	

	
}
