package com.bolsinga.music.web;

import com.bolsinga.music.data.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Web {

	public static final String HTML_EXT = ".html";
	
	public static final String ARTIST_DIR = "artists";
	public static final String VENUE_DIR = "venues";
	public static final String SHOW_DIR = "shows";
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: Web [source.xml] [output.dir]");
			System.exit(0);
		}
		
		Web.generate(args[0], args[1]);
	}
	
	public static void generate(String sourceFile, String outputDir) {
		Music music = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			music = (Music)u.unmarshal(new FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		
		generateArtistPages(music, outputDir);
		
		generateVenuePages(music, outputDir);
		
		generateDatePages(music, outputDir);
	}
	
	// NOTE: Instead of a List of ID's, JAXB returns a List of real items.
	
	public static void generateArtistPages(Music music, String outputDir) {
		List artists = music.getArtist();
		Artist artist = null;
		
		Collections.sort(artists, com.bolsinga.music.util.Compare.ARTIST_COMPARATOR);
		
		ListIterator li = artists.listIterator();
		while (li.hasNext()) {
			artist = (Artist)li.next();
			
			System.out.println(getLinkTo(artist));
		}
	}
	
	public static void generateVenuePages(Music music, String outputDir) {
		List venues = music.getVenue();
		Venue venue = null;

		Collections.sort(venues, com.bolsinga.music.util.Compare.VENUE_COMPARATOR);
		
		ListIterator li = venues.listIterator();
		while (li.hasNext()) {
			venue = (Venue)li.next();
			
			System.out.println(getLinkTo(venue));
		}
	}
	
	public static void generateDatePages(Music music, String outputDir) {
		List shows = music.getShow();
		Show show = null;

		Collections.sort(shows, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			show = (Show)li.next();
			
			System.out.println(getLinkTo(show));
		}
	}
	
	public static String getLinkTo(Artist artist) {
		StringBuffer link = new StringBuffer();
		
		String name = artist.getSortname();
		if (name == null) {
			name = artist.getName();
		}
		
		link.append(Web.ARTIST_DIR);
		link.append("/");
		link.append(name.substring(0, 1).toUpperCase());
		link.append(Web.HTML_EXT);
		link.append("#");
		link.append(artist.getId());
		
		return link.toString();
	}
	
	public static String getLinkTo(Venue venue) {
		StringBuffer link = new StringBuffer();
		
		String name = venue.getName();
		
		link.append(Web.VENUE_DIR);
		link.append("/");
		link.append(name.substring(0, 1).toUpperCase());
		link.append(Web.HTML_EXT);
		link.append("#");
		link.append(venue.getId());
		
		return link.toString();
	}
	
	public static String getLinkTo(Show show) {
		StringBuffer link = new StringBuffer();
		BigInteger current = null;
		String file = null;
		com.bolsinga.music.data.Date date = show.getDate();
		
		current = date.getYear();
		if (current != null) {
			file = current.toString();
		} else {
			file = "unknown";
		}
		
		link.append(Web.SHOW_DIR);
		link.append("/");
		link.append(file);
		link.append(Web.HTML_EXT);
		link.append("#");
		link.append(show.getId());
		
		return link.toString();
	}
	
	public static String getEntry(Artist artist) {
		return null;
	}
	
	public static String getEntry(Venue venue) {
		return null;
	}
	
	public static String getEntry(com.bolsinga.music.data.Date date) {
		return null;
	}
}
