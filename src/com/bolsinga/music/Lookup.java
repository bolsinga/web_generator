package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.util.*;

public class Lookup {

  private static Lookup sLookup = null;
        
  private HashMap fArtistMap         = new HashMap();
  private HashMap fVenueMap          = new HashMap();
  private HashMap fCityMap           = new HashMap();
  private HashMap fArtistRelationMap = new HashMap();
  private HashMap fVenueRelationMap  = new HashMap();
  private HashMap fLabelRelationMap  = new HashMap();
        
  public synchronized static Lookup getLookup(Music music) {
    if (sLookup == null) {
      sLookup = new Lookup(music);
    }
    return sLookup;
  }

  private Lookup(Music music) {
    Show show = null;
    String id = null;
    List list = null;
    ListIterator ai = null;
    Artist artist = null;
    Set set = null;
                
    ListIterator i = music.getShow().listIterator();
    while (i.hasNext()) {
      show = (Show)i.next();
                        
      id = ((Venue)show.getVenue()).getId();
      if (fVenueMap.containsKey(id)) {
        list = (List)fVenueMap.get(id);
        list.add(show);
      } else {
        list = new Vector();
        list.add(show);
        fVenueMap.put(id, list);
      }
                        
      id = ((Location)((Venue)show.getVenue()).getLocation()).getCity();
      if (fCityMap.containsKey(id)) {
        list = (List)fCityMap.get(id);
        list.add(show);
      } else {
        list = new Vector();
        list.add(show);
        fCityMap.put(id, list);
      }
                        
      ai = show.getArtist().listIterator();
      while (ai.hasNext()) {
        artist = (Artist)ai.next();
                                
        id = artist.getId();
        if (fArtistMap.containsKey(id)) {
          list = (List)fArtistMap.get(id);
          list.add(show);
        } else {
          list = new Vector();
          list.add(show);
          fArtistMap.put(id, list);
        }
      }
    }
                
    Relation rel = null;
    ListIterator ri = null;
                
    i = music.getRelation().listIterator();
    while (i.hasNext()) {
      rel = (Relation)i.next();
                        
      ri = rel.getMember().listIterator();
      while (ri.hasNext()) {
        Object o = ri.next();
        if (o instanceof Artist) {
          id = ((Artist)o).getId();
          if (!fArtistRelationMap.containsKey(id)) {
            set = new HashSet();
            fArtistRelationMap.put(id, set);
          }
          ListIterator nri = rel.getMember().listIterator();
          while (nri.hasNext()) {
            artist = (Artist)nri.next();
            set = (Set)fArtistRelationMap.get(id);
            set.add(artist);
          }
        } else if (o instanceof Venue) {
          id = ((Venue)o).getId();
          if (!fVenueRelationMap.containsKey(id)) {
            set = new HashSet();
            fVenueRelationMap.put(id, set);
          }
          ListIterator nri = rel.getMember().listIterator();
          while (nri.hasNext()) {
            Venue venue = (Venue)nri.next();
            set = (Set)fVenueRelationMap.get(id);
            set.add(venue);
          }
        } else if (o instanceof Label) {
          id = ((Label)o).getId();
          if (!fLabelRelationMap.containsKey(id)) {
            set = new HashSet();
            fLabelRelationMap.put(id, set);
          }
          ListIterator nri = rel.getMember().listIterator();
          while (nri.hasNext()) {
            Label label = (Label)nri.next();
            set = (Set)fLabelRelationMap.get(id);
            set.add(label);
          }
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
        
  public List getShows(String city) {
    return (List)fCityMap.get(city);
  }
        
  public Collection getRelations(Artist artist) {
    return (Collection)fArtistRelationMap.get(artist.getId());
  }
        
  public Collection getRelations(Venue venue) {
    return (Collection)fVenueRelationMap.get(venue.getId());
  }
        
  public Collection getRelations(Label label) {
    return (Collection)fLabelRelationMap.get(label.getId());
  }
        
  public Collection getCities() {
    return fCityMap.keySet();
  }
}
