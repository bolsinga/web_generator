package com.bolsinga.music.util;

import com.bolsinga.music.data.*;

import java.util.*;
import java.util.regex.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public class Encode {

	private static final String MORE_ARTIST_INFO = "Artist: ";
	private static final String MORE_VENUE_INFO = "Venue: ";
	private static final String MORE_ALBUM_INFO = "Album: ";
	
	private static Encode sEncode = null;

	static private Pattern sSpecialChars = Pattern.compile("([\\(\\)\\?])");

	static private boolean sEmbedLinks = System.getProperty("music.no_embedded_links", "false").equals("false");
	
	private TreeSet fEncodings = new TreeSet(DATA_COMPARATOR);

	class Data {
		String fName = null;
		Pattern fPattern = null;
		String fStandardLink = null;
		String fUpLink = null;
		
		Data(Artist artist, Links standardLinks, Links upLinks) {
			fName = artist.getName();
			fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);

			StringBuffer sb = new StringBuffer(MORE_ARTIST_INFO);
			sb.append(fName);
			
			fStandardLink = com.bolsinga.web.util.Util.createInternalA(standardLinks.getLinkTo(artist), "$2", sb.toString()).toString();
			fUpLink = com.bolsinga.web.util.Util.createInternalA(upLinks.getLinkTo(artist), "$2", sb.toString()).toString();
		}
		
		Data(Venue venue, Links standardLinks, Links upLinks) {
			fName = venue.getName();
			fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);

			StringBuffer sb = new StringBuffer(MORE_VENUE_INFO);
			sb.append(fName);

			fStandardLink = com.bolsinga.web.util.Util.createInternalA(standardLinks.getLinkTo(venue), "$2", sb.toString()).toString();
			fUpLink = com.bolsinga.web.util.Util.createInternalA(upLinks.getLinkTo(venue), "$2", sb.toString()).toString();
		}

		Data(Album album, Links standardLinks, Links upLinks) {
			fName = album.getTitle();
			fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);

			StringBuffer sb = new StringBuffer(MORE_ALBUM_INFO);
			sb.append(fName);

			fStandardLink = com.bolsinga.web.util.Util.createInternalA(standardLinks.getLinkTo(album), "$2", sb.toString()).toString();
			fUpLink = com.bolsinga.web.util.Util.createInternalA(upLinks.getLinkTo(album), "$2", sb.toString()).toString();
		}
		
		String getName() {
			// This is only used for sorting.
			return fName;
		}
		
		String createRegex(String name) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("(^|\\W)(");
			
			Matcher m = sSpecialChars.matcher(name);
			while (m.find()) {
				m.appendReplacement(sb, "\\\\$1");
			}
			m.appendTail(sb);

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
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();
			
			fEncodings.add(new Data(item, standardLinks, upLinks));
		}
		
		items = music.getVenue();
		Venue vitem = null;
		
		// Don't use venues with lower case names, these are 'vague' venues.
		Pattern startsLowerCase = Pattern.compile("\\p{Lower}.*");
		
		li = items.listIterator();
		while (li.hasNext()) {
			vitem = (Venue)li.next();
			
			if (!startsLowerCase.matcher(vitem.getName()).matches()) {
				fEncodings.add(new Data(vitem, standardLinks, upLinks));
			}
		}
		
		items = music.getAlbum();
		Album aitem = null;
		
		li = items.listIterator();
		while (li.hasNext()) {
			aitem = (Album)li.next();
			
			fEncodings.add(new Data(aitem, standardLinks, upLinks));
		}
	}
	
	private static final Pattern sHTMLTag = Pattern.compile("(.*)(<([a-z][a-z0-9]*)[^>]*>[^<]*</\\3>)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	public String addLinks(String source, boolean upOneLevel) {
	
		if (!sEmbedLinks) {
			return source;
		}
		
		String result = source;
		Data data = null;
		
		Iterator li = fEncodings.iterator();
		while (li.hasNext()) {
			data = (Data)li.next();
			
			result = addLinks(data, result, upOneLevel);
		}

		return result;
	}
	
	private String addLinks(Data data, String source, boolean upOneLevel) {
		String result = source;
						
		Matcher entryMatch = data.getPattern().matcher(source);
		if (entryMatch.find()) {			

			StringBuffer sb = new StringBuffer();
			
			Matcher html = sHTMLTag.matcher(source);
			if (html.find()) {
				sb.append(addLinks(data, html.group(1), upOneLevel));
				sb.append(html.group(2));
				sb.append(addLinks(data, html.group(4), upOneLevel));
			} else {
				do {
					entryMatch.appendReplacement(sb, data.getLink(upOneLevel));
				} while (entryMatch.find());
				entryMatch.appendTail(sb);
			}
			
			result = sb.toString();
		}
		
		return result;
	}
}
