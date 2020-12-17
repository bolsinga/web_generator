package com.bolsinga.music.data.json;

import java.time.*;
import java.util.*;

import org.json.*;

public class Song implements com.bolsinga.music.data.Song {
  private static final String ID = "id";
  private static final String ARTIST = "artist";
  private static final String TITLE = "title";
  private static final String RELEASE = "release";
  private static final String LASTPLAYED = "lastPlayed";
  private static final String TRACK = "track";
  private static final String GENRE = "genre";
  private static final String PLAYCOUNT = "playCount";
  private static final String DIGITIZED = "digitized";
  private static final String LIVE = "live";

  private String id;
  private Artist artist;
  private String title;
  private Date release = null;
  private ZonedDateTime lastPlayed = null;
  private int track = 0;
  private String genre = null;
  private int playCount;
  private boolean digitized = true;
  private boolean live = false;

  private static final Map<String, Song> sMap = new HashMap<String, Song>();
  
  static Song get(final String id) {
    synchronized (sMap) {
      return sMap.get(id);
    }
  }

  static Song get(final com.bolsinga.music.data.Song src) {
    return Song.get(src.getID());
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

  static Song createFromJSON(final JSONObject json) throws JSONException {
    Song result = new Song(json);
    synchronized (sMap) {
      sMap.put(result.getID(), result);
    }
    return result;
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Song song) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, song.getID());
    json.put(ARTIST, song.getPerformer().getID());
    json.put(TITLE, song.getTitle());
    com.bolsinga.music.data.Date date = song.getReleaseDate();
    if (date != null) {
      json.put(RELEASE, Date.createJSON(date));
    }
    ZonedDateTime lastPlayed = song.getLastPlayed();
    if (lastPlayed != null) {
      json.put(LASTPLAYED, com.bolsinga.web.Util.conformingISO8601String(lastPlayed));
    }
    if (song.getTrack() != 0) {
      json.put(TRACK, song.getTrack());
    }
    String s = song.getGenre();
    if (s != null) {
      json.put(GENRE, s);
    }
    int playCount = song.getPlayCount();
    if (playCount != 0) {
      json.put(PLAYCOUNT, playCount);
    }
    if (song.isDigitized()) {
      json.put(DIGITIZED, true);
    }
    if (song.isLive()) {
      json.put(LIVE, true);
    }
    
    return json;
  }
  
  private Song(final com.bolsinga.music.data.Song song) {
    id = song.getID();
    artist = Artist.createOrGet(song.getPerformer());
    title = song.getTitle();
    release = Date.create(song.getReleaseDate());
    lastPlayed = song.getLastPlayed();
    track = song.getTrack();
    genre = song.getGenre();
    playCount = song.getPlayCount();
    digitized = song.isDigitized();
    live = song.isLive();
  }
  
  private Song(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    String artistID = json.getString(ARTIST);
    artist = Artist.get(artistID);
    assert artist != null : "Song has no artist!";
    title = json.getString(TITLE);
    JSONObject optJSON = json.optJSONObject(RELEASE);
    if (optJSON != null) {
      release = Date.create(optJSON);
    }
    String lastPlayedString = json.optString(LASTPLAYED, null);
    if (lastPlayedString != null) {
      lastPlayed = ZonedDateTime.parse(lastPlayedString);
    }
    track = json.optInt(TRACK);
    genre = json.optString(GENRE, null);
    playCount = json.optInt(PLAYCOUNT);
    digitized = json.optBoolean(DIGITIZED);
    live = json.optBoolean(LIVE);
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
  
  public ZonedDateTime getLastPlayed() {
    return lastPlayed;
  }
  
  public void setLastPlayed(final ZonedDateTime c) {
    lastPlayed = c;
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
