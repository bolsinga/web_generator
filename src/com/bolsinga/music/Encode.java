package com.bolsinga.music.util;

import com.bolsinga.music.data.*;

import java.util.*;
import java.util.regex.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Encode {

	private static Encode sEncode = null;

	private TreeSet fEncodings = new TreeSet(DATA_COMPARATOR);

	class Data {
		String fName = null;
		Pattern fPattern = null;
		String fStandardLink = null;
		String fUpLink = null;
		
		Data(Artist artist, Links standardLinks, Links upLinks) {
			fName = artist.getName();
			fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);

			fStandardLink = new A(standardLinks.getLinkTo(artist), "$2").toString();
			fUpLink = new A(upLinks.getLinkTo(artist), "$2").toString();
		}
		
		Data(Venue venue, Links standardLinks, Links upLinks) {
			fName = venue.getName();
			fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);

			fStandardLink = new A(standardLinks.getLinkTo(venue), "$2").toString();
			fUpLink = new A(upLinks.getLinkTo(venue), "$2").toString();
		}
		
		String getName() {
			return fName;
		}
		
		String createRegex(String name) {
			StringBuffer sb = new StringBuffer();
			sb.append("(^|\\W)(");
			sb.append(name);
			sb.append(")(\\W)");
			return sb.toString();
		}
		
		Pattern getPattern() {
			return fPattern;
		}
		
		String getLink(boolean upOneLevel) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("$1");
			sb.append(upOneLevel ? fUpLink : fStandardLink);
			sb.append("$3");
			
			return sb.toString();
		}
	}
	
	static final Comparator DATA_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Data d1 = (Data)o1;
			Data d2 = (Data)o2;
			
			int result = d2.getName().length() - d1.getName().length();
			if (result == 0) {
				result = d2.getName().compareTo(d1.getName());
			}
			return result;
		}
	};
	
	public synchronized static Encode getEncode(Music music) {
		if (sEncode == null) {
			sEncode = new Encode(music);
		}
		return sEncode;
	}

	private Encode(Music music) {
		List items = music.getArtist();
		Artist item = null;

		Links standardLinks = Links.getLinks(false);
		Links upLinks = Links.getLinks(true);
		
		// Don't encode anything with punctuation in their name for now.
		Pattern notEncoded = Pattern.compile(".*\\p{Punct}.*");
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();
			
			if (!notEncoded.matcher(item.getName()).matches()) {
				fEncodings.add(new Data(item, standardLinks, upLinks));
			}
		}
		
		items = music.getVenue();
		Venue vitem = null;
		
		// Don't use venues with lower case names, these are 'vague' venues.
		Pattern startsLowerCase = Pattern.compile("\\p{Lower}.*");
		
		li = items.listIterator();
		while (li.hasNext()) {
			vitem = (Venue)li.next();
			
			if (!notEncoded.matcher(vitem.getName()).matches()) {
				if (!startsLowerCase.matcher(vitem.getName()).matches()) {
					fEncodings.add(new Data(vitem, standardLinks, upLinks));
				}
			}
		}
	}
	
	private static final Pattern sHTMLTag = Pattern.compile("(.*)(<([a-z][a-z0-9]*)[^>]*>[^<]*</\\3>)", Pattern.CASE_INSENSITIVE);
	
	public String addLinks(String source, boolean upOneLevel) {
		String result = source;
		Data data = null;
		
		StringBuffer sb = new StringBuffer();
/*		
		Matcher html = sHTMLTag.matcher(result);
		while (html.find()) {
			sb.append(addLinks(html.group(1), upOneLevel));
			sb.append(html.group(2));
		}
		html.appendTail(sb);
		
		result = sb.toString();
*/		
		Iterator li = fEncodings.iterator();
		while (li.hasNext()) {
			data = (Data)li.next();
			
			sb = new StringBuffer();
			Matcher m = data.getPattern().matcher(result);
			while (m.find()) {
				m.appendReplacement(sb, data.getLink(upOneLevel));
			}
			m.appendTail(sb);
			
			result = sb.toString();
		}
		
		return result;
	}
}
