package com.gmobi.poponews.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyTimeUtils {

	public static long getReplyTime() {
		return System.currentTimeMillis();
	}

	public static String getLastTime(long l) {
		long now = getReplyTime();
		long last = now - l;
		last = last / 1000;
		if (last < 30) {
			return "刚刚";
		}
		if (last < 60) {
			return "刚刚";
			// return String.format("%s秒前", last);
		}
		if (last < 3600) {
			return String.format("%s分钟前", last / 60);
		}

		// 获取今天凌晨的时间
		long todayStart = getMorning(new Date()).getTime();
		if (l >= todayStart) { // 今天
			return String.format("%s小时前", last / 3600);

		}

//		if (l < todayStart && l >= todayStart - 86400000) {
//			return "昨天 ";
//		}

		return dateLongToString(l, "yyyy-MM-dd");
	}

	private static Date getMorning(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public static String dateLongToString(long time, String format) {
		if (time <= 0) {
			return "Empty";
		}
		DateFormat format2 = new SimpleDateFormat(format);
		String dateString = format2.format(new Date(time));
		return dateString;
	}

}
