package com.bolsinga.music.util;

import com.bolsinga.music.data.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Links {

	public static final String HTML_EXT = ".html";

	public static final String ARTIST_DIR = "bands";
	public static final String VENUE_DIR = "venues";
	public static final String SHOW_DIR = "dates";
	public static final String CITIES_DIR = "cities";
	public static final String RSS_DIR = "rss";
	public static final String ICAL_DIR = "ical";
	public static final String TRACKS_DIR = "tracks";
	
	private static final String OTHER = "other";
	public static final String STATS = "stats";
	public static final String ALBUM_STATS = "albumstats";
	private static final String HASH = "#";

	private boolean fUpOneLevel;
	
	public static Links getLinks(boolean upOneLevel) {
		return new Links(upOneLevel);
	}
	
	Links(boolean upOneLevel) {
		fUpOneLevel = upOneLevel;
		
		String root = System.getProperty("music.root");
		if (root == null) {
			root = System.getProperty("diary.root");
			if (root == null) {
				root = System.getProperty("rss.root");
			}
			System.setProperty("music.root", root);
		}
	}
	
	public void addWebNavigator(Music music, Document doc, String program) {
		Center c = new Center();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Generated ");
		sb.append(Util.sWebFormat.format(music.getTimestamp().getTime()));
		sb.append(" ");

		StringBuffer link = new StringBuffer();
		link.append("mailto:");
		link.append(System.getProperty("music.contact"));
		link.append("?Subject=");
		link.append(program);
		link.append("%20Message");
		link.append("&amp;Body=");
		link.append(program);
		link.append("%20Message%0A");
		A a = new A(link.toString(), "Contact");
		sb.append(a.toString());
		sb.append(" ");

		a = new A(System.getProperty("music.root"), "Home");
		sb.append(a.toString());
		sb.append(" ");
		
		sb.append(getArtistLink());
		sb.append(" ");
		
		sb.append(getTracksLink());
		sb.append(" ");

		sb.append(getShowLink());
		sb.append(" ");

		sb.append(getVenueLink());
		sb.append(" ");

		sb.append(getCityLink());
		sb.append(" ");

		c.addElement(sb.toString());
		
		doc.getBody().addElement(c);
	}
	
	public String getPageFileName(String name) {
		String file = Compare.simplify(name).substring(0, 1).toUpperCase();
		if (file.matches("\\W")) {
			file = OTHER;
		}
		return file;
	}
	
	public String getPageFileName(BigInteger year) {
		if (year == null) {
			return OTHER;
		} else {
			return year.toString();
		}
	}
	
	public String getPageFileName(Artist artist) {
		String name = artist.getSortname();
		if (name == null) {
			name = artist.getName();
		}
		return getPageFileName(name);
	}
	
	public String getPageFileName(Venue venue) {
		return getPageFileName(venue.getName());
	}
	
	public String getPageFileName(Show show) {
		BigInteger current = show.getDate().getYear();
		return getPageFileName(current);
	}

	public String getPageFileName(Album album) {
		return getPageFileName(album.getTitle());
	}
	
	public String getPagePath(Artist artist) {
		StringBuffer sb = new StringBuffer();
		sb.append(ARTIST_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(artist));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public String getPagePath(Venue venue) {
		StringBuffer sb = new StringBuffer();
		sb.append(VENUE_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(venue));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public String getPagePath(Show show) {
		StringBuffer sb = new StringBuffer();
		sb.append(SHOW_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(show));
		sb.append(HTML_EXT);
		return sb.toString();
	}

	public String getPagePath(Album album) {
		StringBuffer sb = new StringBuffer();
		sb.append(TRACKS_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(album));
		sb.append(HTML_EXT);
		return sb.toString();
	}

	public String getLinkToPage(Artist artist) {
		StringBuffer link = new StringBuffer();
		
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(ARTIST_DIR);
		link.append(File.separator);
		link.append(getPageFileName(artist));
		link.append(HTML_EXT);
		
		return link.toString();
	}
	
	public String getLinkToPage(Venue venue) {
		StringBuffer link = new StringBuffer();
		
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(VENUE_DIR);
		link.append(File.separator);
		link.append(getPageFileName(venue));
		link.append(HTML_EXT);

		return link.toString();
	}
	
	public String getLinkToPage(Show show) {
		StringBuffer link = new StringBuffer();
		
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(SHOW_DIR);
		link.append(File.separator);
		link.append(getPageFileName(show));
		link.append(HTML_EXT);

		return link.toString();
	}
	
	public String getLinkToPage(Album album) {
		StringBuffer link = new StringBuffer();
		
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(TRACKS_DIR);
		link.append(File.separator);
		link.append(getPageFileName(album));
		link.append(HTML_EXT);

		return link.toString();
	}

	public String getLinkTo(Artist artist) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(artist));
		link.append(HASH);
		link.append(artist.getId());
		
		return link.toString();
	}
	
	public String getLinkTo(Venue venue) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(venue));
		link.append(HASH);
		link.append(venue.getId());
		
		return link.toString();
	}
	
	public String getLinkTo(Show show) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(show));
		link.append(HASH);
		link.append(show.getId());
		
		return link.toString();
	}
	
	public String getLinkTo(Album album) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(album));
		link.append(HASH);
		link.append(album.getId());
		
		return link.toString();
	}

	public String getArtistLink() {
		StringBuffer link = new StringBuffer();
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(ARTIST_DIR);
		link.append(File.separator);
		link.append(STATS);
		link.append(HTML_EXT);
		A a = new A(link.toString(), "Bands");
		return a.toString();
	}
		
	public String getShowLink() {
		StringBuffer link = new StringBuffer();
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(SHOW_DIR);
		link.append(File.separator);
		link.append(STATS);
		link.append(HTML_EXT);
		A a = new A(link.toString(), "Dates");
		return a.toString();
	}
	
	public String getTracksLink() {
		StringBuffer link = new StringBuffer();
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(TRACKS_DIR);
		link.append(File.separator);
		link.append(STATS);
		link.append(HTML_EXT);
		A a = new A(link.toString(), "Tracks");
		return a.toString();
	}

	public String getAlbumsLink() {
		StringBuffer link = new StringBuffer();
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(TRACKS_DIR);
		link.append(File.separator);
		link.append(ALBUM_STATS);
		link.append(HTML_EXT);
		A a = new A(link.toString(), "Albums");
		return a.toString();
	}

	public String getVenueLink() {
		StringBuffer link = new StringBuffer();
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(VENUE_DIR);
		link.append(File.separator);
		link.append(STATS);
		link.append(HTML_EXT);
		A a = new A(link.toString(), "Venues");
		return a.toString();
	}
	
	public String getCityLink() {
		StringBuffer link = new StringBuffer();
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		link.append(CITIES_DIR);
		link.append(File.separator);
		link.append(STATS);
		link.append(HTML_EXT);
		A a = new A(link.toString(), "Cities");
		return a.toString();
	}

	public String getICalLink() {
		StringBuffer link = new StringBuffer();
		
		link.append("webcal:");

		link.append(System.getProperty("music.root"));

		link.append(ICAL_DIR);
		link.append(File.separator);
		link.append(System.getProperty("music.ical.url"));

		IMG img = new IMG(System.getProperty("ical.image.url"));
		img.setHeight(System.getProperty("ical.image.height"));
		img.setWidth(System.getProperty("ical.image.width"));
		img.setAlt(System.getProperty("ical.image.alt"));
		
		return new A(link.toString(), img.toString()).toString();
	}
}
