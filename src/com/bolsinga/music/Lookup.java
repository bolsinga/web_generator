package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

public class Lookup {

  private static Lookup sLookup = null;
        
  private final HashMap<String, String> fArtistHTMLMap =
    new HashMap<String, String>();
  private final HashMap<String, String> fAlbumHTMLMap =
    new HashMap<String, String>();
  private final HashMap<String, String> fVenueHTMLMap =
    new HashMap<String, String>();
  private final HashMap<String, Collection<Show>> fArtistMap =
    new HashMap<String, Collection<Show>>();
  private final HashMap<String, Collection<Show>> fVenueMap =
    new HashMap<String, Collection<Show>>();
  private final HashMap<String, Collection<Show>> fCityMap =
    new HashMap<String, Collection<Show>>();
  private final HashMap<String, Integer> fStateMap = new HashMap<String, Integer>();
  private final HashMap<String, Collection<Artist>> fArtistRelationMap =
    new HashMap<String, Collection<Artist>>();
  private final HashMap<String, Collection<Venue>> fVenueRelationMap =
    new HashMap<String, Collection<Venue>>();
        
  public synchronized static Lookup getLookup(final Music music) {
    if (sLookup == null) {
      sLookup = new Lookup(music);
    }
    return sLookup;
  }

  private Lookup(final Music music) {
    String id = null;
    Collection<Show> showCollection = null;
    
    for (Artist performer : music.getArtists()) {
      fArtistHTMLMap.put(performer.getID(), Util.toHTMLSafe(performer.getName()));
    }

    for (Album album : music.getAlbums()) {
      fAlbumHTMLMap.put(album.getID(), Util.toHTMLSafe(album.getTitle()));
    }

    for (Venue venue : music.getVenues()) {
      fVenueHTMLMap.put(venue.getID(), Util.toHTMLSafe(venue.getName()));
    }

    List<? extends Show> shows = music.getShows();
    for (Show show : shows) {
      id = show.getVenue().getID();
      if (fVenueMap.containsKey(id)) {
        showCollection = fVenueMap.get(id);
        showCollection.add(show);
      } else {
        showCollection = new Vector<Show>();
        showCollection.add(show);
        fVenueMap.put(id, showCollection);
      }

      Location loc = show.getVenue().getLocation();
      Object typeArgs[] = { loc.getCity(), loc.getState() };
      id = MessageFormat.format(Util.getResourceString("cityformat"), typeArgs);
      if (fCityMap.containsKey(id)) {
        showCollection = fCityMap.get(id);
        showCollection.add(show);
      } else {
        showCollection = new Vector<Show>();
        showCollection.add(show);
        fCityMap.put(id, showCollection);
      }
      
      int count = 1;
      id = loc.getState();
      if (fStateMap.containsKey(id)) {
        count = fStateMap.get(id) + 1;
      }
      fStateMap.put(id, count);

      List<? extends Artist> artists = show.getArtists();
      for (Artist artist : artists) {
        id = artist.getID();
        if (fArtistMap.containsKey(id)) {
          showCollection = fArtistMap.get(id);
          showCollection.add(show);
        } else {
          showCollection = new Vector<Show>();
          showCollection.add(show);
          fArtistMap.put(id, showCollection);
        }
      }
    }

    List<? extends Relation> relations = music.getRelations();
    for (Relation rel : relations) {
      for (Object o : rel.getMembers()) {
        if (o instanceof Artist) {
          Collection<Artist> rArtists;
          id = ((Artist)o).getID();
          if (!fArtistRelationMap.containsKey(id)) {
            rArtists = new TreeSet<Artist>(Compare.ARTIST_COMPARATOR);
            fArtistRelationMap.put(id, rArtists);
          }
          for (Object ja : rel.getMembers()) {
            Artist artist = (Artist)ja;
            rArtists = fArtistRelationMap.get(id);
            rArtists.add(artist);
          }
        } else if (o instanceof Venue) {
          Collection<Venue> rVenues;
          id = ((Venue)o).getID();
          if (!fVenueRelationMap.containsKey(id)) {
            rVenues = new TreeSet<Venue>(Compare.VENUE_COMPARATOR);
            fVenueRelationMap.put(id, rVenues);
          }
          for (Object jv : rel.getMembers()) {
            Venue venue = (Venue)jv;
            rVenues = fVenueRelationMap.get(id);
            rVenues.add(venue);
          }
        } else {
          throw new Error("No Relation: " + o.toString());
        }
      }
    }
  }
  
  public String getHTMLName(final Artist artist) {
    return fArtistHTMLMap.get(artist.getID());
  }

  public String getHTMLName(final Album album) {
    return fAlbumHTMLMap.get(album.getID());
  }
  
  public String getHTMLName(final Venue venue) {
    return fVenueHTMLMap.get(venue.getID());
  }
        
  public Collection<Show> getShows(final Artist artist) {
    return fArtistMap.get(artist.getID());
  }
  
  public int getLiveCount() {
    return fArtistMap.size();
  }
  
  public int getSetCount() {
    int total = 0;
    
    for (String id : fArtistMap.keySet())
        total += fArtistMap.get(id).size();
    
    return total;
  }
  
  public Collection<Show> getShows(final Venue venue) {
    return fVenueMap.get(venue.getID());
  }
        
  public Collection<Show> getShows(final String city) {
    return fCityMap.get(city);
  }
        
  public Collection<Artist> getRelations(final Artist artist) {
    return fArtistRelationMap.get(artist.getID());
  }
        
  public Collection<Venue> getRelations(final Venue venue) {
    return fVenueRelationMap.get(venue.getID());
  }

  public Collection<String> getCities() {
    return fCityMap.keySet();
  }
  
  public int getStateCount() {
    return fStateMap.size();
  }
}
