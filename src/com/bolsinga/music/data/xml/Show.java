package com.bolsinga.music.data.xml;

import java.util.*;

import javax.xml.bind.JAXBElement;

public class Show implements com.bolsinga.music.data.Show {
  private static final HashMap<String, Show> sMap = new HashMap<String, Show>();

  private final com.bolsinga.music.data.xml.impl.Show fShow;
  private final List<com.bolsinga.music.data.Artist> fArtists;

  public static Show get(final com.bolsinga.music.data.xml.impl.Show item) {
    synchronized (sMap) {
      Show result = sMap.get(item.getId());
      if (result == null) {
        result = new Show(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Show(final com.bolsinga.music.data.xml.impl.Show show) {
    fShow = show;

    fArtists = new ArrayList<com.bolsinga.music.data.Artist>(fShow.getArtist().size());
    for (JAXBElement<Object> jartist : fShow.getArtist()) {
      com.bolsinga.music.data.xml.impl.Artist artist = (com.bolsinga.music.data.xml.impl.Artist)jartist.getValue();
      fArtists.add(Artist.get(artist));
    }
  }
  
  public List<com.bolsinga.music.data.Artist> getArtists() {
    return Collections.unmodifiableList(fArtists);
  }
  
  public com.bolsinga.music.data.Date getDate() {
    return Date.create(fShow.getDate());
  }
  
  public com.bolsinga.music.data.Venue getVenue() {
    return Venue.get((com.bolsinga.music.data.xml.impl.Venue)fShow.getVenue());
  }
  
  public String getComment() {
    return fShow.getComment();
  }
  
  public String getID() {
    return fShow.getId();
  }
  
  public void setID(final String id) {
    fShow.setId(id);
  }
}
