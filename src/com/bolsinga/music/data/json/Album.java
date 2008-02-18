package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Album implements com.bolsinga.music.data.Album {
  private static final String ID = "id";
  private static final String TITLE = "title";
  private static final String PERFORMER = "performer";
  private static final String COMPLIATION = "compilation";
  private static final String FORMATS = "formats";
  private static final String RELEASE = "release";
  private static final String PURCHASE = "purchase";
  private static final String LABEL = "label";
  private static final String COMMENT = "comment";
  private static final String SONGS = "songs";

  private String id;
  private String title;
  private Artist performer;
  private boolean compilation = false;
  private List<String> formats;
  private Date release = null;
  private Date purchase = null;
  private Label label = null;
  private String comment = null;
  private List<Song> songs;

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

  static JSONObject createJSON(final com.bolsinga.music.data.Album album) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, album.getID());
    json.put(TITLE, album.getTitle());
    com.bolsinga.music.data.Artist a = album.getPerformer();
    if (a != null) {
      json.put(PERFORMER, a.getID());
    }
    if (album.isCompilation()) {
      json.put(COMPLIATION, true);
    }
    List<String> formats = album.getFormats();
    if (formats != null && formats.size() > 0) {
      json.put(FORMATS, formats);
    }
    com.bolsinga.music.data.Date d = album.getReleaseDate();
    if (d != null) {
      json.put(RELEASE, Date.createJSON(d));
    }
    d = album.getPurchaseDate();
    if (d != null) {
      json.put(PURCHASE, Date.createJSON(d));
    }
    com.bolsinga.music.data.Label l = album.getLabel();
    if (l != null) {
      json.put(LABEL, l.getID());
    }
    String comment = album.getComment();
    if (comment != null) {
      json.put(COMMENT, comment);
    }
    List<? extends com.bolsinga.music.data.Song> songs = album.getSongs();
    if (songs != null && songs.size() > 0) {
      List<String> songIDs = new ArrayList<String>(songs.size());
      for (final com.bolsinga.music.data.Song song : songs) {
        songIDs.add(song.getID());
      }
      json.put(SONGS, songIDs);
    }
    
    return json;
  }
  
  private Album() {
  
  }
  
  private Album(final com.bolsinga.music.data.Album src) {
    id = src.getID();
    title = src.getTitle();
    performer = Artist.createOrGet(src.getPerformer());
    compilation = src.isCompilation();
    formats = src.getFormats();
    release = Date.create(src.getReleaseDate());
    purchase = Date.create(src.getPurchaseDate());
    label = Label.createOrGet(src.getLabel());
    comment = src.getComment();

    List<? extends com.bolsinga.music.data.Song> srcSongs = src.getSongs();
    songs = new ArrayList<Song>(srcSongs.size());
    for (com.bolsinga.music.data.Song song : srcSongs) {
      songs.add(Song.createOrGet(song));
    }
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
