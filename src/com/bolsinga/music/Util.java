package com.bolsinga.music.util;

import java.text.*;
import java.util.*;

public class Util {

	static DateFormat sMonthFormat = new SimpleDateFormat("MMMM");
	static DateFormat sWebFormat = new SimpleDateFormat("M/d/yyyy");
	
	public static Calendar toCalendar(com.bolsinga.music.data.Date date) {
		Calendar d = Calendar.getInstance();
		if (!date.isUnknown()) {
			d.set(date.getYear().intValue(), date.getMonth().intValue() - 1, date.getDay().intValue());
		} else {
			System.err.println("Can't convert Unknown com.bolsinga.music.data.Date");
			System.exit(1);
		}
		return d;
	}

	public static String toString(com.bolsinga.music.data.Date date) {
		if (!date.isUnknown()) {
			return sWebFormat.format(toCalendar(date).getTime());
		} else {
			StringBuffer sb = new StringBuffer();
			
			sb.append((date.getMonth() != null) ? date.getMonth().intValue() : 0);
			sb.append("/");
			sb.append((date.getDay() != null) ? date.getDay().intValue() : 0);
			sb.append("/");
			sb.append((date.getYear() != null) ? date.getYear().intValue() : 0);
			
			sb.append(" (Unknown)");
			
			return sb.toString();
		}
	}
	
	public static String toMonth(com.bolsinga.music.data.Date date) {
		if (!date.isUnknown()) {
			return sMonthFormat.format(toCalendar(date).getTime());
		} else {
			Calendar d = Calendar.getInstance();
			if (date.getMonth() != null) {
				d.set(Calendar.MONTH, date.getMonth().intValue() - 1);
				return sMonthFormat.format(d.getTime());
			} else {
				return "Unknown";
			}
		}
	}
}
