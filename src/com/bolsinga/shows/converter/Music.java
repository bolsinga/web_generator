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
			createVenues(objFactory, venues, mVenues);
			
			createBandSort(objFactory, bands);

			createRelations(objFactory, relations);
		
			List mShows = music.getShow();
			convert(objFactory, shows, mShows);
			dump(music);
		} catch (JAXBException e) {
			System.err.println(e);
			System.exit(1);
		}
	}
	
	private static void createVenues(ObjectFactory objFactory, List venues, List mVenues) throws JAXBException {
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
			
			sVenues.put(name, xVenue.getId());
		}
	}
	
	private static void createBandSort(ObjectFactory objFactory, List bands) throws JAXBException {
		// Make a hash of the band sort names by name
		BandMap bandMap = null;
		
		ListIterator li = bands.listIterator();
		while (li.hasNext()) {
			bandMap = (BandMap)li.next();
			sBandSorts.put(bandMap.getName(), bandMap.getSortName());
		}
	}
	
	private static void createRelations(ObjectFactory objFactory, List relations) throws JAXBException {
	
	}
	
	private static com.bolsinga.music.Artist addArtist(ObjectFactory objFactory, String name) throws JAXBException {
		com.bolsinga.music.Artist result = null;
		if (!sArtists.containsKey(name)) {
			result = objFactory.createArtist();
			result.setName(name);
			result.setId("artist_" + sArtists.size());
			if (sBandSorts.containsKey(name)) {
				result.setSortname((String)sBandSorts.get(name));
			}
			sArtists.put(name, result);
		} else {
			result = (com.bolsinga.music.Artist)sArtists.get(name);
		}
		return result;
	}
		
	private static void convert(ObjectFactory objFactory, List shows, List mShows) throws JAXBException {
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

		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			oldShow = (Show)li.next();
			
//			mShows.add(xShow);
		}
	}
	
	private static void dump(com.bolsinga.music.Music music) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music");
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		m.marshal( music, System.out );
	}
}
