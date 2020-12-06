package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Album implements com.bolsinga.music.data.Album {
  private static final String ID = "id";
  private static final String TITLE = "title";
  private static final String PERFORMER = "performer";
  private static final String COMPILATION = "compilation";
  private static final String RELEASE = "release";
  private static final String PURCHASE = "purchase";
  private static final String COMMENT = "comment";
  private static final String SONGS = "songs";

  private String id;
  private String title;
  private Artist performer; // optional
  private boolean compilation; // optional
  private Date release; // optional
  private Date purchase; // optional
  private String comment; // optional
  private List<Song> songs;

  private static final Map<String, Album> sMap = new HashMap<String, Album>();
  
  static Album get(final String id) throws JSONException {
    synchronized (sMap) {
      return sMap.get(id);
    }
  }

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

  static Album createOrGet(final String id, final JSONObject json) throws JSONException {
    synchronized (sMap) {
      Album result = sMap.get(id);
      if (result == null) {
        result = new Album(json);
        Artist performer = result.getPerformer();
        if (performer != null) {
          // There is a single Artist for the Album
          performer.addAlbum(result);
        } else {
          // There is a different Artist for each Song, but each Artist is on the Album
          for (Song song : result.getSongs()) {
            performer = song.getPerformer();
            performer.addAlbum(result);
          }
        }
        sMap.put(id, result);
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
      json.put(COMPILATION, true);
    }
    com.bolsinga.music.data.Date d = album.getReleaseDate();
    if (d != null) {
      json.put(RELEASE, Date.createJSON(d));
    }
    d = album.getPurchaseDate();
    if (d != null) {
      json.put(PURCHASE, Date.createJSON(d));
    }
    String comment = album.getComment();
    if (comment != null) {
      json.put(COMMENT, comment);
    }
    List<? extends com.bolsinga.music.data.Song> songs = album.getSongs();
    List<String> songIDs = new ArrayList<String>(songs.size());
    for (final com.bolsinga.music.data.Song song : songs) {
      songIDs.add(song.getID());
    }
    json.put(SONGS, songIDs);
    
    return json;
  }
  
  private Album() {
  
  }
  
  private Album(final com.bolsinga.music.data.Album src) {
    id = src.getID();
    title = src.getTitle();
    performer = Artist.createOrGet(src.getPerformer());
    compilation = src.isCompilation();
    release = Date.create(src.getReleaseDate());
    purchase = Date.create(src.getPurchaseDate());
    comment = src.getComment();

    List<? extends com.bolsinga.music.data.Song> srcSongs = src.getSongs();
    songs = new ArrayList<Song>(srcSongs.size());
    for (com.bolsinga.music.data.Song song : srcSongs) {
      songs.add(Song.createOrGet(song));
    }
  }

  private Album(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    title = json.getString(TITLE);
    String jsonID = json.optString(PERFORMER, null);
    if (jsonID != null) {
      performer = Artist.get(jsonID);
      assert performer != null : "Can't get Artist: " + jsonID;
    }
    compilation = json.optBoolean(COMPILATION, false);
    JSONObject optJSON = json.optJSONObject(RELEASE);
    if (optJSON != null) {
      release = Date.create(optJSON);
    }
    optJSON = json.optJSONObject(PURCHASE);
    if (optJSON != null) {
      purchase = Date.create(optJSON);
    }
    comment = json.optString(COMMENT, null);
    JSONArray jsonArray = json.getJSONArray(SONGS);
    songs = new ArrayList<Song>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      jsonID = jsonArray.getString(i);
      Song song = Song.get(jsonID);
      assert song != null : "Can't get Song: " + jsonID;
      songs.add(song);
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
