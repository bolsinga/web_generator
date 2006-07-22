package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.util.*;

public class Lookup {

  private static Lookup sLookup = null;
        
  private final HashMap<String, Collection<Show>> fArtistMap =
    new HashMap<String, Collection<Show>>();
  private final HashMap<String, Collection<Show>> fVenueMap =
    new HashMap<String, Collection<Show>>();
  private final HashMap<String, Collection<Show>> fCityMap =
    new HashMap<String, Collection<Show>>();
  private final HashMap<String, Collection<Artist>> fArtistRelationMap =
    new HashMap<String, Collection<Artist>>();
  private final HashMap<String, Collection<Venue>> fVenueRelationMap =
    new HashMap<String, Collection<Venue>>();
  private final HashMap<String, Collection<Label>> fLabelRelationMap =
    new HashMap<String, Collection<Label>>();
        
  public synchronized static Lookup getLookup(Music music) {
    if (sLookup == null) {
      sLookup = new Lookup(music);
    }
    return sLookup;
  }

  private Lookup(Music music) {
    String id = null;
    Collection<Show> showCollection = null;

    List<Show> shows = (List<Show>)music.getShow();
    for (Show show : shows) {
      id = ((Venue)show.getVenue()).getId();
      if (fVenueMap.containsKey(id)) {
        showCollection = fVenueMap.get(id);
        showCollection.add(show);
      } else {
        showCollection = new Vector<Show>();
        showCollection.add(show);
        fVenueMap.put(id, showCollection);
      }
                        
      id = ((Location)((Venue)show.getVenue()).getLocation()).getCity();
      if (fCityMap.containsKey(id)) {
        showCollection = fCityMap.get(id);
        showCollection.add(show);
      } else {
        showCollection = new Vector<Show>();
        showCollection.add(show);
        fCityMap.put(id, showCollection);
      }

      List<Artist> artists = (List<Artist>)show.getArtist();
      for (Artist artist : artists) {
        id = artist.getId();
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
                
    Relation rel = null;
    List ritems = null;
    ListIterator ri = null;
                
    ListIterator i = music.getRelation().listIterator();
    while (i.hasNext()) {
      rel = (Relation)i.next();
      if (rel == null) {
        continue;
      }
      ritems = rel.getMember();
      if (ritems == null) {
        continue;
      }
      ri = ritems.listIterator();
      while (ri.hasNext()) {
        Object o = ri.next();
        if (o instanceof Artist) {
          Collection<Artist> rArtists;
          id = ((Artist)o).getId();
          if (!fArtistRelationMap.containsKey(id)) {
            rArtists = new HashSet<Artist>();
            fArtistRelationMap.put(id, rArtists);
          }
          ListIterator nri = rel.getMember().listIterator();
          while (nri.hasNext()) {
            Artist artist = (Artist)nri.next();
            rArtists = fArtistRelationMap.get(id);
            rArtists.add(artist);
          }
        } else if (o instanceof Venue) {
          Collection<Venue> rVenues;
          id = ((Venue)o).getId();
          if (!fVenueRelationMap.containsKey(id)) {
            rVenues = new HashSet<Venue>();
            fVenueRelationMap.put(id, rVenues);
          }
          ListIterator nri = rel.getMember().listIterator();
          while (nri.hasNext()) {
            Venue venue = (Venue)nri.next();
            rVenues = fVenueRelationMap.get(id);
            rVenues.add(venue);
          }
        } else if (o instanceof Label) {
          Collection<Label> rLabels;
          id = ((Label)o).getId();
          if (!fLabelRelationMap.containsKey(id)) {
            rLabels = new HashSet<Label>();
            fLabelRelationMap.put(id, rLabels);
          }
          ListIterator nri = rel.getMember().listIterator();
          while (nri.hasNext()) {
            Label label = (Label)nri.next();
            rLabels = fLabelRelationMap.get(id);
            rLabels.add(label);
          }
        }
      }
    }
  }
        
  public Collection<Show> getShows(Artist artist) {
    return fArtistMap.get(artist.getId());
  }
        
  public Collection<Show> getShows(Venue venue) {
    return fVenueMap.get(venue.getId());
  }
        
  public Collection<Show> getShows(String city) {
    return fCityMap.get(city);
  }
        
  public Collection<Artist> getRelations(Artist artist) {
    return fArtistRelationMap.get(artist.getId());
  }
        
  public Collection<Venue> getRelations(Venue venue) {
    return fVenueRelationMap.get(venue.getId());
  }
        
  public Collection<Label> getRelations(Label label) {
    return fLabelRelationMap.get(label.getId());
  }
        
  public Collection<String> getCities() {
    return fCityMap.keySet();
  }
}
