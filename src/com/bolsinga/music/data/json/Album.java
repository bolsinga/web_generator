package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Album implements com.bolsinga.music.data.Album {
  private String id;
  private String title;
  private Artist performer; // TODO serialize ID
  private boolean compilation = false;
  private List<String> formats;
  private Date release = null;
  private Date purchase = null;
  private Label label = null;  // TODO serialize ID
  private String comment = null;
  private List<Song> songs;  // TODO serialize ID List

  private static final Map<String, Album> sMap = new HashMap<String, Album>();

  static Album get(final com.bolsinga.music.data.Album src) {
    synchronized (sMap) {
      return sMap.get(src.getID());
    }
  }
  
  static Album createOrGet(final com.bolsinga.music.data.Album src) {
    synchronized (sMap) {
      Album result = sMap.get(src.getID());
      if (result == null) {
        result = new Album(src);
        sMap.put(src.getID(), result);
      }
      return result;
    }
  }
  
  private Album() {
  
  }
  
  private Album(final com.bolsinga.music.data.Album src) {
    id = src.getID();
    title = src.getTitle();
    compilation = src.isCompilation();
    performer = Artist.createOrGet(src.getPerformer());
    formats = src.getFormats();
  }

  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(final String title) {
    this.title = title;
  }
  
  public Artist getPerformer() {
    return performer;
  }
  
  public Date getReleaseDate() {
    return release;
  }
  
  public Date getPurchaseDate() {
    return purchase;
  }
  
  public boolean isCompilation() {
    return compilation;
  }
  
  public void setIsCompilation(final boolean isCompilation) {
    this.compilation = isCompilation;
  }
  
  public List<String> getFormats() {
    return formats;
  }
  
  public Label getLabel() {
    return label;
  }
  
  public String getComment() {
    return comment;
  }
  
  public void setComment(final String comment) {
    this.comment = comment;
  }
  
  public List<Song> getSongs() {
    return Collections.unmodifiableList(songs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(songs);
  }
}
