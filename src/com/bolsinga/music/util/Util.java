package com.bolsinga.music.util;

import java.util.*;

public class Util {
	static Vector sMonths = null;
	
	static {
		sMonths = new Vector();
		sMonths.add("Unknown");
		sMonths.add("January");
		sMonths.add("February");
		sMonths.add("March");
		sMonths.add("April");
		sMonths.add("May");
		sMonths.add("June");
		sMonths.add("July");
		sMonths.add("August");
		sMonths.add("September");
		sMonths.add("October");
		sMonths.add("November");
		sMonths.add("December");
	}
	
	public static String toString(com.bolsinga.music.data.Date date) {
		StringBuffer sb = new StringBuffer();
		
		sb.append((date.getMonth() != null) ? date.getMonth().intValue() : 0);
		sb.append("/");
		sb.append((date.getDay() != null) ? date.getDay().intValue() : 0);
		sb.append("/");
		sb.append((date.getYear() != null) ? date.getYear().intValue() : 0);

		return sb.toString();
	}
	
	public static String toMonth(com.bolsinga.music.data.Date date) {
		return (date.getMonth() != null) ? (String)sMonths.get(date.getMonth().intValue()) : "Unknown";
	}
}
