package com.bolsinga.music.util;

import java.util.*;

import com.bolsinga.music.data.*;

public class Compare {
	private static int convert(com.bolsinga.music.data.Date d) {
		return ((d.getYear() != null) ? d.getYear().intValue() * 10000 : 0) +
				((d.getMonth() != null) ? d.getMonth().intValue() * 100 : 0) +
				((d.getDay() != null) ? d.getDay().intValue() : 0);
	}
	
	private static final String THE = "the ";
	private static final String THEE = "thee ";
	private static final String A = "a ";
	private static final String AN = "an ";
	
	private static String simplify(String s) {
		String lower = s.toLowerCase();
		int len = s.length();
		if (lower.startsWith(THE)) {
			return s.substring(THE.length(), len);
		}
		if (lower.startsWith(A)) {
			return s.substring(A.length(), len);
		}
		if (lower.startsWith(AN)) {
			return s.substring(AN.length(), len);
		}
		if (lower.startsWith(THEE)) {
			return s.substring(THEE.length(), len);
		}
		return s;
	}
	
	public static final Comparator LIBRARY_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			String s1 = (String)o1;
			String s2 = (String)o2;
			
			return simplify(s1).compareToIgnoreCase(simplify(s2));
		}
	};
	
	public static final Comparator DATE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			com.bolsinga.music.data.Date r1 = (com.bolsinga.music.data.Date)o1;
			com.bolsinga.music.data.Date r2 = (com.bolsinga.music.data.Date)o2;
			
			int result = 0;
			
			if (r1.isUnknown()) {
				if (r2.isUnknown()) {
					result = convert(r1) - convert(r2);
				} else {
					result = -1;
				}
			} else {
				if (r2.isUnknown()) {
					result = 1;
				} else {
					result = convert(r1) - convert(r2);
				}
			}
			
			return result;
		}
	};
	
	public static final Comparator VENUE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Venue r1 = (Venue)o1;
			Venue r2 = (Venue)o2;
			
			return LIBRARY_COMPARATOR.compare(r1.getName(), r2.getName());
		}
	};
	
	public static final Comparator ARTIST_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Artist r1 = (Artist)o1;
			Artist r2 = (Artist)o2;
			
			String n1 = r1.getSortname();
			if (n1 == null) {
				n1 = r1.getName();
			}
			String n2 = r2.getSortname();
			if (n2 == null) {
				n2 = r2.getName();
			}
			
			return LIBRARY_COMPARATOR.compare(n1, n2);
		}
	};
	
	public static final Comparator SHOW_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Show r1 = (Show)o1;
			Show r2 = (Show)o2;
			
			int result = DATE_COMPARATOR.compare(r1.getDate(), r2.getDate());
			if (result == 0) {
				result = VENUE_COMPARATOR.compare(r1.getVenue(), r2.getVenue());
				if (result == 0) {
					result = ARTIST_COMPARATOR.compare( ((Performance)r1.getPerformance().get(0)).getArtist(),
														((Performance)r2.getPerformance().get(0)).getArtist());
				}
			}
			return result;
		}
	};
}
