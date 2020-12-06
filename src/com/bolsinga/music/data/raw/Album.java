package com.bolsinga.music.data.raw;

import java.util.*;

public class Album implements com.bolsinga.music.data.Album {
  private String fID;
  private String fTitle;
  private final Artist fArtist;
  private com.bolsinga.music.data.Date fReleaseDate;
  private com.bolsinga.music.data.Date fPurchaseDate;
  private boolean fIsCompilation;
  private String fComment;
  private final List<Song> fSongs;
  
  static Album create(final int id, final String title, final Artist artist, final com.bolsinga.music.data.Date releaseDate, final List<Song> songs) {
    String s = (artist != null) ? artist.getName() : "null";
    return new Album("a" + id, title, artist, releaseDate, songs);
  }
  
  Album(final String id, final String title, final Artist artist, final com.bolsinga.music.data.Date releaseDate, final List<Song> songs) {
    fID = id;
    fTitle = title;
    fArtist = artist;
    fReleaseDate = releaseDate;
    
    fIsCompilation = (artist == null);

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
  
  public Artist getPerformer() {
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

  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
  }
  
  public List<Song> getSongs() {
    return Collections.unmodifiableList(fSongs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(fSongs);
  }
}