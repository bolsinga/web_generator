package com.bolsinga.music.data.raw;

import java.util.*;

public class Album implements com.bolsinga.music.data.Album {
  private String fID;
  private String fTitle;
  private final com.bolsinga.music.data.Artist fArtist;
  private com.bolsinga.music.data.Date fReleaseDate;
  private com.bolsinga.music.data.Date fPurchaseDate;
  private boolean fIsCompilation;
  private List<String> fFormats;
  private com.bolsinga.music.data.Label fLabel;
  private String fComment;
  private final List<com.bolsinga.music.data.Song> fSongs;
  
  static Album create(final int id, final String title, final com.bolsinga.music.data.Artist artist, final com.bolsinga.music.data.Date releaseDate, final List<com.bolsinga.music.data.Song> songs) {
    String s = (artist != null) ? artist.getName() : "null";
    return new Album("a" + id, title, artist, releaseDate, songs);
  }
  
  Album(final String id, final String title, final com.bolsinga.music.data.Artist artist, final com.bolsinga.music.data.Date releaseDate, final List<com.bolsinga.music.data.Song> songs) {
    fID = id;
    fTitle = title;
    fArtist = artist;
    fReleaseDate = releaseDate;
    
    fIsCompilation = (artist == null);
    
    fFormats = new ArrayList<String>();
    fFormats.add(com.bolsinga.music.data.Album.FORMAT_DIGITAL_FILE);
    
    fSongs = songs;
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
  
  public String getTitle() {
    return fTitle;
  }
  
  public void setTitle(final String title) {
    fTitle = title;
  }
  
  public com.bolsinga.music.data.Artist getPerformer() {
    return fArtist;
  }
  
  public com.bolsinga.music.data.Date getReleaseDate() {
    return fReleaseDate;
  }
  
  public com.bolsinga.music.data.Date getPurchaseDate() {
    return fPurchaseDate;
  }
  
  public boolean isCompilation() {
    return fIsCompilation;
  }
  
  public void setIsCompilation(final boolean isCompilation) {
    fIsCompilation = isCompilation;
  }
  
  public List<String> getFormats() {
    return fFormats;
  }
  
  public com.bolsinga.music.data.Label getLabel() {
    return fLabel;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
  }
  
  public List<? extends com.bolsinga.music.data.Song> getSongs() {
    return Collections.unmodifiableList(fSongs);
  }
  
  public List<? extends com.bolsinga.music.data.Song> getSongsCopy() {
    return new ArrayList<com.bolsinga.music.data.Song>(fSongs);
  }
}