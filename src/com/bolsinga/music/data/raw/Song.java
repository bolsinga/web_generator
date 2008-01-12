package com.bolsinga.music.data.raw;

import java.util.*;

public class Song implements com.bolsinga.music.data.Song {
  private String fID;
  private Artist fArtist;
  private String fTitle;
  private com.bolsinga.music.data.Date fReleaseDate;
  private GregorianCalendar fLastPlayed;
  private int fTrack;
  private String fGenre;
  private int fPlayCount;
  
  static Song create(final int id, final String title, final Artist artist, final GregorianCalendar lastPlayed, final int playCount, final String genre, final com.bolsinga.music.data.Date releaseDate, final int track) {
    return new Song("s" + id, title, artist, lastPlayed, playCount, genre, releaseDate, track);
  }
  
  private Song(final String id, final String title, final Artist artist, final GregorianCalendar lastPlayed, final int playCount, final String genre, final com.bolsinga.music.data.Date releaseDate, final int track) {
    fID = id;
    fTitle = title;
    fArtist = artist;
    fLastPlayed = lastPlayed;
    fPlayCount = playCount;
    fGenre = genre;
    fReleaseDate = releaseDate;
    fTrack = track;
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
  
  public Artist getPerformer() {
    return fArtist;
  }
  
  public String getTitle() {
    return fTitle;
  }
  
  public void setTitle(final String title) {
    fTitle = title;
  }
  
  public com.bolsinga.music.data.Date getReleaseDate() {
    return fReleaseDate;
  }
  
  public GregorianCalendar getLastPlayed() {
    return fLastPlayed;
  }
  
  public void setLastPlayed(final GregorianCalendar c) {
    fLastPlayed = c;
  }
  
  // return 0 if unknown
  public int getTrack() {
    return fTrack;
  }

  public void setTrack(final int track) {
    fTrack = track;
  }
  
  public String getGenre() {
    return fGenre;
  }
  
  public void setGenre(final String genre) {
    fGenre = genre;
  }
  
  public int getPlayCount() {
    return fPlayCount;
  }
  
  public void setPlayCount(final int playCount) {
    fPlayCount = playCount;
  }

  public boolean isDigitized() {
    return true;
  }
  
  public boolean isLive() {
    // Not currently tracked.
    return false;
  }
}