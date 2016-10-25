package com.gmobi.poponews.util;



import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.OfflineSystemService;
import com.momock.app.App;
import com.momock.util.Logger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.TimeZone;

public class OfflineAlarmHelper{

	public static void startOfflineAlarm(Context context) {
		Logger.error("startOfflineAlarm");

		Intent intent = new Intent(App.get(), OfflineSystemService.class);
		AlarmManager am = (AlarmManager) App.get().getSystemService(Service.ALARM_SERVICE);

		PendingIntent sender = PendingIntent.getService(App.get(), 0, intent, 0);
		if (sender != null){
			Logger.error("cancel alarm");
			am.cancel(sender);
		}else{
			Logger.error("sender == null");
		}



		IConfigService configService = App.get().getService(IConfigService.class);
		int hour=configService.getOfflineTimeHour();
		int min=configService.getOfflineTimeMinute();


		long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
		long systemTime = System.currentTimeMillis();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		//calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		calendar.set(Calendar.MINUTE, min);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		long selectTime = calendar.getTimeInMillis();

		if(systemTime > selectTime) {
			Logger.error("Maybe tomorrow");
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			selectTime = calendar.getTimeInMillis();
		}

		long time = selectTime - systemTime;



		Intent intent_ag = new Intent(App.get(), OfflineSystemService.class);
		PendingIntent p_intent_ag = PendingIntent.getService(App.get(), 0, intent_ag,
				0);
		Logger.error("time = "+time);
//		am.setRepeating(AlarmManager.RTC_WAKEUP,
//				System.currentTimeMillis() + time, 1000L * 60 * 60 * 24, p_intent_ag);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			am.setWindow(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + time, 1000L * 60 * 60 * 24, p_intent_ag);
		}
		else {
			am.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + time, 1000L * 60 * 60 * 24, p_intent_ag);
		}
	}


	public static void stopOfflineAlarm(Context context) {
		AlarmManager am = (AlarmManager) App.get()
				.getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(App.get(), OfflineSystemService.class);
		PendingIntent p_intent = PendingIntent.getService(App.get(), 0, intent,
				0);
		am.cancel(p_intent);


	}
	


}
