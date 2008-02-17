package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Song implements com.bolsinga.music.data.Song {
  private String id;
  private Artist artist;
  private String title;
  private Date release = null;
  private String lastPlayed = null;
  private int track = 0;
  private String genre = null;
  private int playCount;
  private boolean digitized = true;
  private boolean live = false;

  private static final Map<String, Song> sMap = new HashMap<String, Song>();

  static Song get(final com.bolsinga.music.data.Song src) {
    synchronized (sMap) {
      return sMap.get(src.getID());
    }
  }
  
  static Song createOrGet(final com.bolsinga.music.data.Song src) {
    synchronized (sMap) {
      Song result = sMap.get(src.getID());
      if (result == null) {
        result = new Song(src);
        sMap.put(src.getID(), result);
      }
      return result;
    }
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Song song) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("id", song.getID());
    json.put("artist", song.getPerformer().getID());
    json.put("title", song.getTitle());
    com.bolsinga.music.data.Date date = song.getReleaseDate();
    if (date != null) {
      json.put("release", Date.createJSON(date));
    }
    GregorianCalendar lastPlayed = song.getLastPlayed();
    if (lastPlayed != null) {
      json.put("lastPlayed", com.bolsinga.web.Util.toJSONCalendar(lastPlayed));
    }
    if (song.getTrack() != 0) {
      json.put("track", song.getTrack());
    }
    String s = song.getGenre();
    if (s != null) {
      json.put("genre", s);
    }
    json.put("playCount", song.getPlayCount());
    if (song.isDigitized()) {
      json.put("digitized", true);
    }
    if (song.isLive()) {
      json.put("live", true);
    }
    
    return json;
  }
  
  private Song(final com.bolsinga.music.data.Song song) {
    id = song.getID();
    artist = Artist.createOrGet(song.getPerformer());
    title = song.getTitle();
    release = Date.create(song.getReleaseDate());
    lastPlayed = com.bolsinga.web.Util.toJSONCalendar(song.getLastPlayed());
    track = song.getTrack();
    genre = song.getGenre();
    playCount = song.getPlayCount();
    digitized = song.isDigitized();
    live = song.isLive();
  }
  
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
