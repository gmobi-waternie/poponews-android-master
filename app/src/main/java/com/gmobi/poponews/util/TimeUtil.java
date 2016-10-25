package com.gmobi.poponews.util;

import android.annotation.SuppressLint;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;


import com.gmobi.poponews.R;
import com.momock.app.App;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {
	long lastNewsTime = 0;
	long firstNewsTime =0;
	long curUtcTime =0;
	
	private static TimeUtil mTu = null;
	
	public static TimeUtil getInstance()
	{
		if(mTu != null)
			return mTu;
		else
			return new TimeUtil();
			
	}
	public long getUtcFromFacebookTime(String u)
	{
		SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
		try {
			Date d = dfs.parse(u);
			return d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public long getCurUtcTime()
	{
		return System.currentTimeMillis();
	}

	public long getLastNewsTime() {
		return lastNewsTime;
	}

	public void setLastNewsTime(long lastNewsTime) {
		this.lastNewsTime = lastNewsTime;
	}

	public long getFirstNewsTime() {
		return firstNewsTime;
	}

	public void setFirstNewsTime(long firstNewsTime) {
		this.firstNewsTime = firstNewsTime;
	}
	
	@SuppressLint("SimpleDateFormat") 
	public String getDataFormatStr(long utctime)
	{
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		 Date now =new Date(utctime);
	     String d = df.format(now);
	     return d;
	     
	}
	public String getLastTime(long l) {
		long now = System.currentTimeMillis();
		long last = now - l;
		last = last / 1000;
		if (last < 30) {
			return App.get().getResources().getString(R.string.time_util_just);
		}
		if (last < 60) {
			return  App.get().getResources().getString(R.string.time_util_just);
		}
		if (last < 3600) {
			if (last / 60 <= 1){
				return String.format("%s"+App.get().getResources().getString(R.string.time_util_minute), last / 60);
			} else {
				return String.format("%s"+App.get().getResources().getString(R.string.time_util_minutes), last / 60);
			}
		}
		// 获取今天凌晨的时间
		long todayStart = getMorning(new Date()).getTime();
		if (l >= todayStart) { // 今天
			if (last / 3600 <= 1){
				return String.format("%s"+App.get().getResources().getString(R.string.time_util_hour), last / 3600);
			} else {
				return String.format("%s"+App.get().getResources().getString(R.string.time_util_hours), last / 3600);
			}
		}
//		if (l < todayStart && l >= todayStart - 86400000) {
//			return App.get().getResources().getString(R.string.time_util_yesterday);
//		}
		if (l < todayStart  && l >= todayStart - 86400000 * 2){
			return App.get().getResources().getString(R.string.time_util_before_yesterday);
		}

		return getTimeFormatStr(l);
	}
	private static Date getMorning(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	@SuppressLint("SimpleDateFormat")
	public static String getTimeFormatStr(long utctime)
	{
		SimpleDateFormat df = new SimpleDateFormat("MM-dd");
		Date now =new Date(utctime);
		String d = df.format(now);
		return d;

	}
	
	public long getNextTimeIntervl(int nextHour,int nextMin)
	{
		
		Calendar curCal = Calendar.getInstance();
		Calendar nextCal = Calendar.getInstance();
		int curHour = curCal.get(Calendar.HOUR_OF_DAY);
		int curMin = curCal.get(Calendar.MINUTE);
		/*已经过了*/
		if(curHour > nextHour || (curHour == nextHour && curMin >= nextMin))
		{
			nextCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		nextCal.set(Calendar.HOUR_OF_DAY, nextHour);
		nextCal.set(Calendar.MINUTE, nextMin);
		
		return (nextCal.getTimeInMillis() - curCal.getTimeInMillis());
	}

	public static String getYYMMDDDate(String dateStr)
	{
		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.US);
		sf.setLenient(true);
		try {
			TimeZone tz=TimeZone.getTimeZone("US/Central ");
			sf.setTimeZone(tz);
			Date   date=   sf.parse(dateStr);
			SimpleDateFormat   format1   =   new   SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			format1.setTimeZone(TimeZone.getTimeZone("GMT+8:00 "));
			return format1.format(date);
		} catch (ParseException e) {
			Log.e("time", e.toString());
			return dateStr;
		}
	}
	
}
