package com.bolsinga.music.util;

import com.bolsinga.music.data.*;

import java.util.*;

public class Lookup {

	private static Lookup sLookup = null;
	
	private HashMap fArtistMap = new HashMap();
	private HashMap fVenueMap = new HashMap();
	
	public synchronized static Lookup getLookup(Music music) {
		if (sLookup == null) {
			sLookup = new Lookup(music);
		}
		return sLookup;
	}

	private Lookup(Music music) {
		List shows = music.getShow();
		Show show = null;
		String id = null;
		List venues = null;
		List artists = null;
		ListIterator pi = null;
		Performance perf = null;
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			show = (Show)li.next();
			
			id = ((Venue)show.getVenue()).getId();
			if (fVenueMap.containsKey(id)) {
				venues = (List)fVenueMap.get(id);
				venues.add(show);
			} else {
				venues = new Vector();
				venues.add(show);
				fVenueMap.put(id, venues);
			}
			
			pi = show.getPerformance().listIterator();
			while (pi.hasNext()) {
				perf = (Performance)pi.next();
				
				id = ((Artist)perf.getArtist()).getId();
				if (fArtistMap.containsKey(id)) {
					artists = (List)fArtistMap.get(id);
					artists.add(show);
				} else {
					artists = new Vector();
					artists.add(show);
					fArtistMap.put(id, artists);
				}
			}
		}
	}
	
	public List getShows(Artist artist) {
		return (List)fArtistMap.get(artist.getId());
	}
	
	public List getShows(Venue venue) {
		return (List)fVenueMap.get(venue.getId());
	}
}
