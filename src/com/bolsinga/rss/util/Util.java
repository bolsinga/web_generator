package com.bolsinga.rss.util;

import java.text.*;
import java.util.regex.*;

public class Util {
	private static DateFormat sRSSDateFormat = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss z");
	
	public static String getRSSDate(java.util.Date date) {
		return sRSSDateFormat.format(date);
	}

	private static final Pattern sHTMLTag = Pattern.compile("<([a-z][a-z0-9]*)[^>]*>([^<]*)</\\1>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static String removeLinks(String data) {
		StringBuffer sb = new StringBuffer();
		
		Matcher match = sHTMLTag.matcher(data);
		while (match.find()) {
			match.appendReplacement(sb, "$2");
		}
		match.appendTail(sb);
		
		return sb.toString();
	}
	
	public static String createDescription(String data, int maxlength) {
		// This should strip out URLs, keeping only the regular data.
		String linkFree = removeLinks(data);
		
		// This truncate at the nearest word after maxlength characters, and add an ellipsis if necessary.
		StringBuffer sb = new StringBuffer();
		if (linkFree.length() > maxlength) {
			sb.append(linkFree.substring(0, maxlength));
			sb.append("É");
		} else {
			sb.append(linkFree);
		}
		return sb.toString();
	}
}
