package com.bolsinga.music.util;

import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {

	private static DateFormat sMonthFormat = new SimpleDateFormat("MMMM");
	public static DateFormat sWebFormat = new SimpleDateFormat("M/d/yyyy");
	private static DecimalFormat sPercentFormat = new DecimalFormat("##.##");
	
	public static Calendar toCalendar(com.bolsinga.music.data.Date date) {
		Calendar d = Calendar.getInstance();
		if (!date.isUnknown()) {
			d.clear();
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
	
	public static String toString(double value) {
		return sPercentFormat.format(value);
	}

	public static com.bolsinga.music.data.Music createMusic(String sourceFile) {
		com.bolsinga.music.data.Music music = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			music = (com.bolsinga.music.data.Music)u.unmarshal(new java.io.FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		return music;
	}
}
