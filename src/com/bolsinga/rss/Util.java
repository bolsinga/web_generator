package com.bolsinga.rss.util;

import java.text.*;

public class Util {
	private static DateFormat sRSSDateFormat = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss z");
	
	public static String getRSSDate(java.util.Date date) {
		return sRSSDateFormat.format(date);
	}

	public static String createDescription(String data, int maxlength) {
		return (data.length() > maxlength) ? data.substring(0, maxlength) : data;
	}
}
