package com.gmobi.poponews.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.app.ConfigNames;
import com.gmobi.poponews.app.PopoApplication;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.data.Settings;


/**
 * Created by Administrator on 10/19 0019.
 */
public class PreferenceHelper {
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";


    public static final String SP_KEY_DID = "Widgetdid";
    public static final String SP_KEY_CHANNEL = "WidgetChannel";
    public static final String SP_KEY_INSTALLERCHANNEL = "channel";
    public static final String SP_KEY_FILE_BASEURL  = "WidgetfilebaseUrl";

    public static final String SP_KEY_COUNTRY  = "Widgetcountry";
    public static final String SP_KEY_LANG  = "Widgetlang";
    public static final String SP_KEY_DCH  = "Widgetdch";


    public static final String SP_KEY_CATEGORY  = "Widgetcategory";
    public static final String SP_KEY_EDITION  = "Widgetedition";
    public static final String SP_KEY_CATEGORY_COUNT  = "Widgetcategorycount";
    public static final String SP_KEY_NEWS  = "Widgetnews";
    public static final String SP_KEY_CTG_IDX= "WidgetcategoryIdx";
    public static final String SP_KEY_CTG_ID= "WidgetcategoryId";
    public static final String SP_KEY_CTG_NAME= "WidgetcategoryName";

    public static final String SP_KEY_DIRECTION= "Widgetdirection";
    
    public static final String SP_KEY_NATIVE_AD_ENABLE = "Widgetnative_enable";
    public static final String SP_KEY_NATIVE_AD_COUNT = "Widgetnative_count";
    public static final String SP_KEY_NATIVE_FACEBOOK_AD_PERCENT = "Widgetnative_fb_per";






    public static final String SP_NAME = "poponewsWidget";
    public static SharedPreferences sp = null;
    public static SharedPreferences.Editor editor;

    public static synchronized void setDid(Context ctx ,String did)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_DID,did).commit();
    }

    public static synchronized String getDid(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_DID,"");
    }


    public static synchronized void setCurChannel(Context ctx ,String ch)
    {
        Settings st = PopoApplication.getSetting();
        st.setProperty(ConfigNames.SETTINGS_KEY_CHANNEL, ch);
    }

    public static synchronized String getInstallerChannel(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_INSTALLERCHANNEL, "");
    }

    public static synchronized String getCurChannel(Context ctx)
    {
        Settings st = PopoApplication.getSetting();
        String ch= st.getStringProperty(ConfigNames.SETTINGS_KEY_CHANNEL, "");
        if(!ch.equals(""))
            return ch;

        String installerCh = PreferenceHelper.getInstallerChannel(App.get());
        return installerCh;
    }


    public static synchronized void setFileBaseUrl(Context ctx ,String url)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_FILE_BASEURL,url).commit();
    }

    public static synchronized String getDch(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_DCH, BuildConfig.DISTRIBUTION_CHANNEL);
    }


    public static synchronized void setDch(Context ctx ,String dch)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_DCH,dch).commit();
    }

    public static synchronized String getFileBaseUrl(Context ctx,String url)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_FILE_BASEURL, "");
    }


    public static synchronized void updateEditonConfigure(Context ctx ,String channel, String country, String lang)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_CHANNEL,channel).commit();
        editor.putString(SP_KEY_COUNTRY,country).commit();
        editor.putString(SP_KEY_LANG,lang).commit();

    }


    public static void updateNativeAdConfigure(Context ctx ,boolean enable, int count) {
        sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putBoolean(SP_KEY_NATIVE_AD_ENABLE, enable).commit();
        editor.putInt(SP_KEY_NATIVE_AD_COUNT, count).commit();
    }


    public static synchronized boolean getAdEnable(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getBoolean(SP_KEY_NATIVE_AD_ENABLE, false);
    }

    public static synchronized int getAdCount(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getInt(SP_KEY_NATIVE_AD_COUNT, 0);
    }

    public static synchronized String getCurLang(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_LANG, "");
    }

    public static synchronized String getCurCountry(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_COUNTRY, "");
    }



    public static synchronized String readEdition(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_EDITION,"");
    }


    public static synchronized void saveEdition(Context ctx ,String editions)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_EDITION,editions).commit();
    }


    public static synchronized String readCategory(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_CATEGORY,"");
    }


    public static synchronized void saveCategory(Context ctx ,String ctg)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_CATEGORY,ctg).commit();
    }


    public static synchronized void saveProducts(Context ctx ,String ctgId,String products)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(ctgId, products).commit();
    }

    public static synchronized String readProducts(Context ctx,String ctgId)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(ctgId,"");
    }



    public static synchronized void saveCurCategoryId(Context ctx,String cid)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_CTG_ID, cid).commit();
    }

    public static synchronized String readCurCategoryId(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_CTG_ID, "");
    }


    public static synchronized void saveCurCategoryName(Context ctx,String name)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putString(SP_KEY_CTG_NAME, name).commit();
    }

    public static synchronized String readCurCategoryName(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getString(SP_KEY_CTG_NAME, ctx.getResources().getString(R.string.app_title));
    }

    public static synchronized void saveCurCategoryIdx(Context ctx ,int idx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putInt(SP_KEY_CTG_IDX, idx).commit();
    }

    public static synchronized int readCurCategoryIdx(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getInt(SP_KEY_CTG_IDX, 0);
    }
    public static synchronized void saveCategoryCount(Context ctx ,int count)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putInt(SP_KEY_CATEGORY_COUNT, count).commit();
    }

    public static synchronized int readCategoryCount(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getInt(SP_KEY_CATEGORY_COUNT, 0);
    }



    public static synchronized void saveNewsIconStatus(Context ctx ,String id, int iconStatus)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putInt(id + "icon", iconStatus).commit();
    }

    public static synchronized void saveNewsPreviewStatus(Context ctx ,String id, int previewStatus)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putInt(id+"preview", previewStatus).commit();
    }


    public static synchronized int readNewsIconStatus(Context ctx,String id)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getInt(id+"icon",0);
    }

    public static synchronized int readNewsPreviewStatus(Context ctx,String id)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getInt(id+"preview",0);
    }



    public static synchronized void saveFetchDirect(Context ctx ,int d)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
        editor = sp.edit();

        editor.putInt(SP_KEY_DIRECTION, d).commit();
    }

    public static synchronized int readFetchDirect(Context ctx)
    {
        sp = ctx.getSharedPreferences(SP_NAME,Context.MODE_WORLD_READABLE);
        return sp.getInt(SP_KEY_DIRECTION, 1);
    }



}

