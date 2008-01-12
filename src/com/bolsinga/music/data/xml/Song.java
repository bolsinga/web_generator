package com.bolsinga.music.data.xml;

import java.util.*;
import javax.xml.datatype.*;

public class Song implements com.bolsinga.music.data.Song {
  private static final HashMap<String, Song> sMap = new HashMap<String, Song>();

  private final com.bolsinga.music.data.xml.impl.Song fSong;

  public static Song get(final com.bolsinga.music.data.xml.impl.Song item) {
    synchronized (sMap) {
      Song result = sMap.get(item.getId());
      if (result == null) {
        result = new Song(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Song(final com.bolsinga.music.data.xml.impl.Song song) {
    fSong = song;
  }
  
  public String getID() {
    return fSong.getId();
  }
  
  public void setID(final String id) {
    fSong.setId(id);
  }
  
  public Artist getPerformer() {
    return Artist.get((com.bolsinga.music.data.xml.impl.Artist)fSong.getPerformer());
  }
  
  public String getTitle() {
    return fSong.getTitle();
  }
  
  public void setTitle(final String title) {
    fSong.setTitle(title);
  }
  
  public com.bolsinga.music.data.Date getReleaseDate() {
    return Date.create(fSong.getReleaseDate());
  }

  public GregorianCalendar getLastPlayed() {
    XMLGregorianCalendar r = fSong.getLastPlayed();
    return (r != null) ? r.toGregorianCalendar() : null;
  }
  
  public void setLastPlayed(final GregorianCalendar c) {
    fSong.setLastPlayed(com.bolsinga.web.Util.toXMLGregorianCalendar(c));
  }
  
  public int getTrack() {
    java.math.BigInteger value = fSong.getTrack();
    return (value != null) ? value.intValue() : 0;
  }
  
  public void setTrack(final int track) {
    fSong.setTrack(java.math.BigInteger.valueOf(track));
  }

  public String getGenre() {
    return fSong.getGenre();
  }
  
  public void setGenre(final String genre) {
    fSong.setGenre(genre);
  }

  public int getPlayCount() {
    return fSong.getPlayCount().intValue();
  }
  
  public void setPlayCount(final int playCount) {
    fSong.setPlayCount(java.math.BigInteger.valueOf(playCount));
  }

  public boolean isDigitized() {
    Boolean value = fSong.isDigitized();
    return (value != null) ? value.booleanValue() : false;
  }
  
  public boolean isLive() {
    Boolean value = fSong.isLive();
    return (value != null) ? value.booleanValue() : false;
  }
}
