package com.gmobi.poponews.util;

import java.text.DecimalFormat;

public class SizeHelper {
	static final long OneK = 1024;
	static final long OneM = OneK * 1024;
	public static String getSize(long len){
		DecimalFormat df = new DecimalFormat("#.##");
		return len > OneM ? df.format((double)len / OneM) + "MB" : (len / OneK) + "KB";		
	}
}
