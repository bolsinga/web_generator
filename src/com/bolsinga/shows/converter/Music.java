package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.data.*;

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
			com.bolsinga.music.data.Music music = objFactory.createMusic();
		
			createVenues(objFactory, music, venues);
			
			createBandSort(objFactory, music, bands);
		
			convert(objFactory, music, shows);

			createRelations(objFactory, music, relations);

			// Write out to the output file.
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			OutputStream os = null;
			try {
				os = new FileOutputStream(outputFile);
			} catch (IOException ioe) {
				System.err.println(ioe);
				ioe.printStackTrace();
				System.exit(1);
			}
			m.marshal(music, os);
			
		} catch (JAXBException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void createVenues(ObjectFactory objFactory, com.bolsinga.music.data.Music music, List venues) throws JAXBException {
		// Go through each venue.
		//  Create a Venue for each		
		// Make a hash of the Venue information by name

		Venue oldVenue = null;
		com.bolsinga.music.data.Venue xVenue = null;
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
			xVenue.setId("v" + index++);
			
			music.getVenue().add(xVenue);
			
			sVenues.put(name, xVenue);
		}
	}
	
	private static void createBandSort(ObjectFactory objFactory, com.bolsinga.music.data.Music music, List bands) throws JAXBException {
		// Make a hash of the band sort names by name
		BandMap bandMap = null;
		
		ListIterator li = bands.listIterator();
		while (li.hasNext()) {
			bandMap = (BandMap)li.next();
			sBandSorts.put(bandMap.getName(), bandMap.getSortName());
		}
	}
	
	private static void createRelations(ObjectFactory objFactory, com.bolsinga.music.data.Music music, List relations) throws JAXBException {
		Relation oldRelation = null;
		com.bolsinga.music.data.Relation xRelation = null;
		String type = null, member = null, reason = null;
		ListIterator mi = null;
		int index = 0;
		
		ListIterator li = relations.listIterator();
		while (li.hasNext()) {
			oldRelation = (Relation)li.next();
			type = oldRelation.getType();
			
			xRelation = objFactory.createRelation();
			reason = oldRelation.getReason();
			if (!reason.equals("^")) {
				xRelation.setReason(reason);
			}
			xRelation.setId("r" + index++);
			
			if (type.equals("band")) {
				xRelation.setType("artist");

				mi = oldRelation.getMembers().listIterator();
				while (mi.hasNext()) {
					member = (String)mi.next();
					xRelation.getMember().add(sArtists.get(member));
				}
			} else if (type.equals("venue")) {
				xRelation.setType(type);
				
				mi = oldRelation.getMembers().listIterator();
				while (mi.hasNext()) {
					member = (String)mi.next();
					xRelation.getMember().add(sVenues.get(member));
				}
			} else {
				System.err.println("Unknown relation type: " + type);
				System.exit(1);
			}
			
			music.getRelation().add(xRelation);
		}
	}
	
	private static com.bolsinga.music.data.Date createDate(ObjectFactory objFactory, String date) throws JAXBException {
		com.bolsinga.music.data.Date result = objFactory.createDate();
		
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
	
	private static com.bolsinga.music.data.Artist addArtist(ObjectFactory objFactory, com.bolsinga.music.data.Music music, String name) throws JAXBException {
		com.bolsinga.music.data.Artist result = null;
		if (!sArtists.containsKey(name)) {
			result = objFactory.createArtist();
			result.setName(name);
			result.setId("ar" + sArtists.size());
			if (sBandSorts.containsKey(name)) {
				result.setSortname((String)sBandSorts.get(name));
			}
			music.getArtist().add(result);
			sArtists.put(name, result);
		} else {
			result = (com.bolsinga.music.data.Artist)sArtists.get(name);
		}
		return result;
	}
	
	static int sPerformanceID = 0;

	private static com.bolsinga.music.data.Performance createPerformance(ObjectFactory objFactory, com.bolsinga.music.data.Music music, String name) throws JAXBException {
			
		com.bolsinga.music.data.Artist xArtist = addArtist(objFactory, music, name);
				
		com.bolsinga.music.data.Performance perf = objFactory.createPerformance();
		
		perf.setArtist(xArtist);
		perf.setId("p" + sPerformanceID++);
		
		return perf;
	}
		
	private static void convert(ObjectFactory objFactory, com.bolsinga.music.data.Music music, List shows) throws JAXBException {
		// Go through each show.
		//  Create an Artist for each band in the set, if it doesn't already exist. Use the sort name.
		//  Create a Date.
		//  Create a new Performance for each Artist and Date.
		//  Get the Venue from the hash.
		//  Create a Comment.
		//  Create a Show with the above.
		
		Show oldShow = null;
		com.bolsinga.music.data.Show xShow = null;
		com.bolsinga.music.data.Artist xArtist = null;
		com.bolsinga.music.data.Performance xPerf = null;
		com.bolsinga.music.data.Date xDate = null;

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
								
				xPerf = createPerformance(objFactory, music, oldBand);
				music.getPerformance().add(xPerf);
				
				xShow.getPerformance().add(xPerf);
			}

			xShow.setVenue(sVenues.get(oldShow.getVenue()));
			if (oldShow.getComment() != null) {
				xShow.setComment(oldShow.getComment());
			}
			xShow.setId("sh" + index++);
			
			music.getShow().add(xShow);
		}
	}
	
	private static void dump(com.bolsinga.music.data.Music music) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		m.marshal( music, System.out );
	}
}
