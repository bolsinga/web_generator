package com.bolsinga.music.util;

public class Util {
	public static String toString(com.bolsinga.music.data.Date date) {
		StringBuffer sb = new StringBuffer();
		
		sb.append((date.getMonth() != null) ? date.getMonth().intValue() : 0);
		sb.append("/");
		sb.append((date.getDay() != null) ? date.getDay().intValue() : 0);
		sb.append("/");
		sb.append((date.getYear() != null) ? date.getYear().intValue() : 0);

		return sb.toString();
	}
}
