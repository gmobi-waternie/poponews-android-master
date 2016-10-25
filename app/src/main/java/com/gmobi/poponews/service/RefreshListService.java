package com.gmobi.poponews.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.provider.PoponewsProvider;
import com.gmobi.poponews.util.WidgetDataHelper;
import com.gmobi.poponews.util.PreferenceHelper;
import com.momock.util.Logger;

import org.json.JSONException;

public class RefreshListService extends Service implements Runnable {

    private static boolean sThreadRunning = false;

    private static Object sLock = new Object();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Logger.debug("onStart startId = " + startId);
        synchronized (sLock) {
            if (!sThreadRunning) {
                sThreadRunning = true;
                new Thread(this).start();
            }
        }
    }

    @Override
    public void run() {
        updataMiddleLayout();
        startAlarmFlash();
    }

    /**
     * 启动一个定时器，定时器只启动一次，下次启动服务时有会再次启动，从而实现循环，所以只用了am.set()做处理
     */
    public void startAlarmFlash() {

        Logger.debug("startAlarmFlash start");
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, RefreshListService.class);
        intent.setAction(IntentNames.LIST_REFRESH);
        PendingIntent pi = PendingIntent.getService(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pi);
        Logger.debug("startAlarmFlash end");


    }

    /**
     * 取消定时器，这个方法可以在provide里面的onDisabled里面执行
     *
     * @param context
     */
    public static void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RefreshListService.class);
        intent.setAction(IntentNames.LIST_REFRESH);
        PendingIntent pi = PendingIntent.getService(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        pi.cancel();
        pi = null;
    }

    public void updataMiddleLayout() {

        int idx = PreferenceHelper.readCurCategoryIdx(this);
        idx = idx + 1;
        PreferenceHelper.saveCurCategoryIdx(this,idx);

        String cid = PreferenceHelper.readCurCategoryId(this);
        if(!cid.equals("")) {
            try {
                WidgetDataHelper.getNewsRemote(this, cid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(this);
        int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                this, PoponewsProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appIds, R.id.lv_apps);

        sThreadRunning = false;

    }


}
