package com.bolsinga.music.util;

import com.bolsinga.music.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Util {

	public static final String HTML_EXT = ".html";

	public static final String ARTIST_DIR = "artists";
	public static final String VENUE_DIR = "venues";
	public static final String SHOW_DIR = "shows";
	public static final String CITIES_DIR = "cities";
	
	private static final String OTHER = "other";
	public static final String STATS = "stats";
	public static final String SEPARATOR = "/";
	private static final String HASH = "#";

	private static DateFormat sMonthFormat = new SimpleDateFormat("MMMM");
	private static DateFormat sWebFormat = new SimpleDateFormat("M/d/yyyy");
	private static DecimalFormat sPercentFormat = new DecimalFormat("##.##");
	
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
	
	public static String toString(double value) {
		return sPercentFormat.format(value);
	}
	
	public static void addWebNavigator(Document doc) {
		Center c = new Center();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Generated ");
		sb.append(Util.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append(" ");

		StringBuffer link = new StringBuffer();
		link.append("mailto:");
		link.append(System.getProperty("music.contact"));
		A a = new A(link.toString(), "Contact");
		sb.append(a.toString());
		sb.append(" ");

		a = new A(System.getProperty("music.root"), "Home");
		sb.append(a.toString());
		sb.append(" ");
		
		link = new StringBuffer();
		link.append("..");
		link.append(SEPARATOR);
		link.append(ARTIST_DIR);
		link.append(SEPARATOR);
		link.append(STATS);
		link.append(HTML_EXT);
		a = new A(link.toString(), "Bands");
		sb.append(a.toString());
		sb.append(" ");
		
		link = new StringBuffer();
		link.append("..");
		link.append(SEPARATOR);
		link.append(SHOW_DIR);
		link.append(SEPARATOR);
		link.append(STATS);
		link.append(HTML_EXT);
		a = new A(link.toString(), "Dates");
		sb.append(a.toString());
		sb.append(" ");

		link = new StringBuffer();
		link.append("..");
		link.append(SEPARATOR);
		link.append(VENUE_DIR);
		link.append(SEPARATOR);
		link.append(STATS);
		link.append(HTML_EXT);
		a = new A(link.toString(), "Venues");
		sb.append(a.toString());
		sb.append(" ");

		link = new StringBuffer();
		link.append("..");
		link.append(SEPARATOR);
		link.append(CITIES_DIR);
		link.append(SEPARATOR);
		link.append(STATS);
		link.append(HTML_EXT);
		a = new A(link.toString(), "Cities");
		sb.append(a.toString());
		sb.append(" ");

		c.addElement(sb.toString());
		
		doc.getBody().addElement(c);
	}
	
	public static String getPageFileName(String name) {
		String file = Compare.simplify(name).substring(0, 1).toUpperCase();
		if (file.matches("\\W")) {
			file = OTHER;
		}
		return file;
	}
	
	public static String getPageFileName(BigInteger year) {
		if (year == null) {
			return OTHER;
		} else {
			return year.toString();
		}
	}
	
	public static String getPageFileName(Artist artist) {
		String name = artist.getSortname();
		if (name == null) {
			name = artist.getName();
		}
		return getPageFileName(name);
	}
	
	public static String getPageFileName(Venue venue) {
		return getPageFileName(venue.getName());
	}
	
	public static String getPageFileName(Show show) {
		BigInteger current = show.getDate().getYear();
		return getPageFileName(current);
	}
	
	public static String getPagePath(Artist artist) {
		StringBuffer sb = new StringBuffer();
		sb.append(ARTIST_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(artist));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public static String getPagePath(Venue venue) {
		StringBuffer sb = new StringBuffer();
		sb.append(VENUE_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(venue));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public static String getPagePath(Show show) {
		StringBuffer sb = new StringBuffer();
		sb.append(SHOW_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(show));
		sb.append(HTML_EXT);
		return sb.toString();
	}

	public static String getLinkToPage(Artist artist) {
		StringBuffer link = new StringBuffer();
		
		link.append("../");
		link.append(ARTIST_DIR);
		link.append(SEPARATOR);
		link.append(getPageFileName(artist));
		link.append(HTML_EXT);
		
		return link.toString();
	}
	
	public static String getLinkToPage(Venue venue) {
		StringBuffer link = new StringBuffer();
		
		link.append("../");
		link.append(VENUE_DIR);
		link.append(SEPARATOR);
		link.append(getPageFileName(venue));
		link.append(HTML_EXT);

		return link.toString();
	}
	
	public static String getLinkToPage(Show show) {
		StringBuffer link = new StringBuffer();
		
		link.append("../");
		link.append(SHOW_DIR);
		link.append(SEPARATOR);
		link.append(getPageFileName(show));
		link.append(HTML_EXT);

		return link.toString();
	}
	
	public static String getLinkTo(Artist artist) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(artist));
		link.append(HASH);
		link.append(artist.getId());
		
		return link.toString();
	}
	
	public static String getLinkTo(Venue venue) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(venue));
		link.append(HASH);
		link.append(venue.getId());
		
		return link.toString();
	}
	
	public static String getLinkTo(Show show) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(show));
		link.append(HASH);
		link.append(show.getId());
		
		return link.toString();
	}
}
