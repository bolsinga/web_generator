package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Song implements com.bolsinga.music.data.Song {
  private String id;
  private Artist artist;
  private String title;
  private Date release;
  private String lastPlayed;
  private int track;
  private String genre;
  private int playCount;
  private boolean digitized;
  private boolean live;
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public Artist getPerformer() {
    return artist;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(final String title) {
    this.title = title;
  }
  
  public Date getReleaseDate() {
    return release;
  }
  
  public GregorianCalendar getLastPlayed() {
    return com.bolsinga.web.Util.fromJSONCalendar(lastPlayed);
  }
  
  public void setLastPlayed(final GregorianCalendar c) {
    lastPlayed = com.bolsinga.web.Util.toJSONCalendar(c);
  }
  
  // return 0 if unknown
  public int getTrack() {
    return track;
  }

  public void setTrack(final int track) {
    this.track = track;
  }
  
  public String getGenre() {
    return genre;
  }
  
  public void setGenre(final String genre) {
    this.genre = genre;
  }
  
  public int getPlayCount() {
    return playCount;
  }
  
  public void setPlayCount(final int playCount) {
    this.playCount = playCount;
  }
  
  public boolean isDigitized() {
    return digitized;
  }
  
  public boolean isLive() {
    return live;
  }
}
