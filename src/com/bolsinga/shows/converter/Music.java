package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.*;

public class Music {

	private static HashMap sVenues = new HashMap();
	private static HashMap sBandSorts = new HashMap();
	private static HashMap sArtists = new HashMap();
	
	public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println("Usage: Music [shows] [venuemap] [bandsort] [relations] [output]");
			System.exit(0);
		}
		
		Music.convert(args[0], args[1], args[2], args[3], args[4]);
	}
	
	public static void convert(String showsFile, String venueFile, String bandFile, String relationFile, String outputFile) {
		List shows = null;
		List venues = null;
		List bands = null;
		List relations = null;
		
		try {
			shows = Convert.shows(showsFile);
		
			venues = Convert.venuemap(venueFile);

			bands = Convert.bandsort(bandFile);

			relations = Convert.relation(relationFile);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		
		ObjectFactory objFactory = new ObjectFactory();
		
		try {
			com.bolsinga.music.Music music = objFactory.createMusic();
		
			List mVenues = music.getVenue();
			createVenues(objFactory, music, venues, mVenues);
			
			createBandSort(objFactory, music, bands);

			createRelations(objFactory, music, relations);
		
			convert(objFactory, music, shows);
			dump(music);
		} catch (JAXBException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void createVenues(ObjectFactory objFactory, com.bolsinga.music.Music music, List venues, List mVenues) throws JAXBException {
		// Go through each venue.
		//  Create a Venue for each		
		// Make a hash of the Venue information by name

		Venue oldVenue = null;
		com.bolsinga.music.Venue xVenue = null;
		Location xLocation = null;
		String name = null;
		int index = 0;
		
		ListIterator li = venues.listIterator();
		while (li.hasNext()) {
			oldVenue = (Venue)li.next();
			
			name = oldVenue.getName();
			
			xLocation = objFactory.createLocation();
			xLocation.setState(oldVenue.getState());
			xLocation.setWeb(oldVenue.getURL());
			xLocation.setCity(oldVenue.getCity());
			xLocation.setStreet(oldVenue.getAddress());
			
			xVenue = objFactory.createVenue();
			xVenue.setName(name);
			xVenue.setLocation(xLocation);
			xVenue.setId("venue_" + index++);
			
			mVenues.add(xVenue);
			
			sVenues.put(name, xVenue);
		}
	}
	
	private static void createBandSort(ObjectFactory objFactory, com.bolsinga.music.Music music, List bands) throws JAXBException {
		// Make a hash of the band sort names by name
		BandMap bandMap = null;
		
		ListIterator li = bands.listIterator();
		while (li.hasNext()) {
			bandMap = (BandMap)li.next();
			sBandSorts.put(bandMap.getName(), bandMap.getSortName());
		}
	}
	
	private static void createRelations(ObjectFactory objFactory, com.bolsinga.music.Music music, List relations) throws JAXBException {
	
	}
	
	private static com.bolsinga.music.Date createDate(ObjectFactory objFactory, String date) throws JAXBException {
		com.bolsinga.music.Date result = objFactory.createDate();
		
		String monthString, dayString, yearString = null;
		int month, day, year = 0;
		
		StringTokenizer st = new StringTokenizer(date, "-");
		
		monthString = st.nextToken();
		dayString = st.nextToken();
		yearString = st.nextToken();
		
		month = Integer.parseInt(monthString);
		day = Integer.parseInt(dayString);
		year = Integer.parseInt(yearString);
		
		if ((month == 0) || (day == 0) || (year == 1900)) {
			result.setUnknown(true);
		}
		
		if (month != 0) {
			result.setMonth(new java.math.BigInteger(monthString));
		}
		
		if (day != 0) {
			result.setDay(new java.math.BigInteger(dayString));
		}
		
		if (year != 1900) {
			result.setYear(new java.math.BigInteger(yearString));
		}
		
		return result;
	}
	
	private static com.bolsinga.music.Artist addArtist(ObjectFactory objFactory, com.bolsinga.music.Music music, String name) throws JAXBException {
		com.bolsinga.music.Artist result = null;
		if (!sArtists.containsKey(name)) {
			result = objFactory.createArtist();
			result.setName(name);
			result.setId("artist_" + sArtists.size());
			if (sBandSorts.containsKey(name)) {
				result.setSortname((String)sBandSorts.get(name));
			}
			music.getArtist().add(result);
			sArtists.put(name, result);
		} else {
			result = (com.bolsinga.music.Artist)sArtists.get(name);
		}
		return result;
	}
	
	static int sPerformanceID = 0;

	private static com.bolsinga.music.Performance createPerformance(ObjectFactory objFactory, com.bolsinga.music.Music music, String name, com.bolsinga.music.Date date) throws JAXBException {
			
		com.bolsinga.music.Artist xArtist = addArtist(objFactory, music, name);
				
		com.bolsinga.music.Performance perf = objFactory.createPerformance();
		
		perf.setArtist(xArtist);
		perf.setDate(date);
		perf.setId("performance_" + sPerformanceID++);
		
		return perf;
	}
		
	private static void convert(ObjectFactory objFactory, com.bolsinga.music.Music music, List shows) throws JAXBException {
		// Go through each show.
		//  Create an Artist for each band in the set, if it doesn't already exist. Use the sort name.
		//  Create a Date.
		//  Create a new Performance for each Artist and Date.
		//  Get the Venue from the hash.
		//  Create a Comment.
		//  Create a Show with the above.
		
		Show oldShow = null;
		com.bolsinga.music.Show xShow = null;
		com.bolsinga.music.Artist xArtist = null;
		com.bolsinga.music.Performance xPerf = null;
		com.bolsinga.music.Date xDate = null;

		int index = 0;
		
		List oldBands = null;
		ListIterator bi = null;
		String oldBand = null;
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			oldShow = (Show)li.next();
			
			xShow = objFactory.createShow();
			
			xDate = createDate(objFactory, oldShow.getDate());
			xShow.setDate(xDate);
			
			oldBands = oldShow.getBands();
			bi = oldBands.listIterator();
			while (bi.hasNext()) {
				oldBand = (String)bi.next();
								
				xPerf = createPerformance(objFactory, music, oldBand, xDate);
				music.getPerformance().add(xPerf);
				
				xShow.getPerformance().add(xPerf);
			}

			xShow.setVenue(sVenues.get(oldShow.getVenue()));
			
			xShow.setId("show_" + index++);
			
			music.getShow().add(xShow);
		}
	}
	
	private static void dump(com.bolsinga.music.Music music) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music");
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		m.marshal( music, System.out );
	}
}
