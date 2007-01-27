package com.bolsinga.music;

import com.bolsinga.music.data.xml.*;

import com.bolsinga.web.*;

import java.util.*;
import javax.xml.bind.*;

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
  private final HashMap<String, Collection<Artist>> fArtistRelationMap =
    new HashMap<String, Collection<Artist>>();
  private final HashMap<String, Collection<Venue>> fVenueRelationMap =
    new HashMap<String, Collection<Venue>>();
  private final HashMap<String, Collection<Label>> fLabelRelationMap =
    new HashMap<String, Collection<Label>>();
        
  public synchronized static Lookup getLookup(final Music music) {
    if (sLookup == null) {
      sLookup = new Lookup(music);
    }
    return sLookup;
  }

  private Lookup(final Music music) {
    String id = null;
    Collection<Show> showCollection = null;
    
    for (Artist performer : Util.getArtistsUnmodifiable(music)) {
      fArtistHTMLMap.put(performer.getId(), Util.toHTMLSafe(performer.getName()));
    }

    for (Album album : Util.getAlbumsUnmodifiable(music)) {
      fAlbumHTMLMap.put(album.getId(), Util.toHTMLSafe(album.getTitle()));
    }

    for (Venue venue : Util.getVenuesUnmodifiable(music)) {
      fVenueHTMLMap.put(venue.getId(), Util.toHTMLSafe(venue.getName()));
    }

    List<Show> shows = Util.getShowsUnmodifiable(music);
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

      List<JAXBElement<Object>> artists = show.getArtist();
      for (JAXBElement<Object> artist : artists) {
        id = ((Artist)artist.getValue()).getId();
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
    
    List<Relation> relations = Util.getRelationsUnmodifiable(music);
    for (Relation rel : relations) {
      for (JAXBElement<Object> jo : rel.getMember()) {
        Object o = jo.getValue();
        if (o instanceof Artist) {
          Collection<Artist> rArtists;
          id = ((Artist)o).getId();
          if (!fArtistRelationMap.containsKey(id)) {
            rArtists = new TreeSet<Artist>(Compare.ARTIST_COMPARATOR);
            fArtistRelationMap.put(id, rArtists);
          }
          for (JAXBElement<Object> ja : rel.getMember()) {
            Artist artist = (Artist)ja.getValue();
            rArtists = fArtistRelationMap.get(id);
            rArtists.add(artist);
          }
        } else if (o instanceof Venue) {
          Collection<Venue> rVenues;
          id = ((Venue)o).getId();
          if (!fVenueRelationMap.containsKey(id)) {
            rVenues = new TreeSet<Venue>(Compare.VENUE_COMPARATOR);
            fVenueRelationMap.put(id, rVenues);
          }
          for (JAXBElement<Object> jv : rel.getMember()) {
            Venue venue = (Venue)jv.getValue();
            rVenues = fVenueRelationMap.get(id);
            rVenues.add(venue);
          }
        } else if (o instanceof Label) {
          Collection<Label> rLabels;
          id = ((Label)o).getId();
          if (!fLabelRelationMap.containsKey(id)) {
            rLabels = new TreeSet<Label>(Compare.LABEL_COMPARATOR);
            fLabelRelationMap.put(id, rLabels);
          }
          for (JAXBElement<Object> jl : rel.getMember()) {
            Label label = (Label)jl.getValue();
            rLabels = fLabelRelationMap.get(id);
            rLabels.add(label);
          }
        } else {
          System.err.println("No Relation: " + o);
          System.exit(1);
        }
      }
    }
  }
  
  public String getHTMLName(final Artist artist) {
    return fArtistHTMLMap.get(artist.getId());
  }

  public String getHTMLName(final Album album) {
    return fAlbumHTMLMap.get(album.getId());
  }
  
  public String getHTMLName(final Venue venue) {
    return fVenueHTMLMap.get(venue.getId());
  }
        
  public Collection<Show> getShows(final Artist artist) {
    return fArtistMap.get(artist.getId());
  }
        
  public Collection<Show> getShows(final Venue venue) {
    return fVenueMap.get(venue.getId());
  }
        
  public Collection<Show> getShows(final String city) {
    return fCityMap.get(city);
  }
        
  public Collection<Artist> getRelations(final Artist artist) {
    return fArtistRelationMap.get(artist.getId());
  }
        
  public Collection<Venue> getRelations(final Venue venue) {
    return fVenueRelationMap.get(venue.getId());
  }
        
  public Collection<Label> getRelations(final Label label) {
    return fLabelRelationMap.get(label.getId());
  }
        
  public Collection<String> getCities() {
    return fCityMap.keySet();
  }
}
