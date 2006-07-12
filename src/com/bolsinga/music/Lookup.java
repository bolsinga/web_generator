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
  private final HashMap<String, Collection<String>> fArtistRelationMap =
    new HashMap<String, Collection<String>>();
  private final HashMap<String, Collection<String>> fVenueRelationMap =
    new HashMap<String, Collection<String>>();
  private final HashMap<String, Collection<String>> fLabelRelationMap =
    new HashMap<String, Collection<String>>();
        
  public synchronized static Lookup getLookup(Music music) {
    if (sLookup == null) {
      sLookup = new Lookup(music);
    }
    return sLookup;
  }

  private Lookup(Music music) {
    Show show = null;
    String id = null;
    Collection<Show> showCollection = null;
    ListIterator ai = null;
    Artist artist = null;
    Set set = null;

    ListIterator i = music.getShow().listIterator();
    while (i.hasNext()) {
      show = (Show)i.next();
                        
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
                        
      ai = show.getArtist().listIterator();
      while (ai.hasNext()) {
        artist = (Artist)ai.next();
                                
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
                
    i = music.getRelation().listIterator();
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
          id = ((Artist)o).getId();
          if (!fArtistRelationMap.containsKey(id)) {
            set = new HashSet<String>();
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
            set = new HashSet<String>();
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
            set = new HashSet<String>();
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
