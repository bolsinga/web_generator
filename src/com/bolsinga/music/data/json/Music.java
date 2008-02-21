package com.bolsinga.music.data.json;

import java.io.*;
import java.util.*;

import org.json.*;

public class Music implements com.bolsinga.music.data.Music {
  private static final String TIMESTAMP = "timestamp";
  private static final String VENUES = "venues";
  private static final String ARTISTS = "artists";
  private static final String LABELS = "labels";
  private static final String RELATIONS = "relations";
  private static final String SONGS = "songs";
  private static final String ALBUMS = "albums";
  private static final String SHOWS = "shows";

  private String timestamp;
  private List<Venue> venues;
  private List<Artist> artists;
  private List<Label> labels;
  private List<Relation> relations;
  private List<Song> songs;
  private List<Album> albums;
  private List<Show> shows;

  public static Music create(final String sourceFile) throws com.bolsinga.web.WebException {
    JSONObject json = com.bolsinga.web.Util.createJSON(sourceFile);
    try {
      return new Music(json);
    } catch (Exception e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Cannot create Music from JSON: ");
      sb.append(sourceFile);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }
  
  public static void export(final com.bolsinga.music.data.Music music, final String outputFile) throws com.bolsinga.web.WebException {    
    JSONObject json = null;
    try {
      json = Music.createJSON(music);
    } catch (JSONException e) {
      throw new com.bolsinga.web.WebException("Can't export music to json", e);
    }
    com.bolsinga.web.Util.writeJSON(json, outputFile);
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Music music) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(TIMESTAMP, com.bolsinga.web.Util.toJSONCalendar(music.getTimestamp()));
    
    JSONObject sub = new JSONObject();
    for (final com.bolsinga.music.data.Venue v : music.getVenues()) {
      sub.put(v.getID(), Venue.createJSON(v));
    }
    json.put(VENUES, sub);
    
    sub = new JSONObject();
    for (final com.bolsinga.music.data.Artist a : music.getArtists()) {
      sub.put(a.getID(), Artist.createJSON(a));
    }
    json.put(ARTISTS, sub);

    sub = new JSONObject();
    for (final com.bolsinga.music.data.Label l : music.getLabels()) {
      sub.put(l.getID(), Label.createJSON(l));
    }
    json.put(LABELS, sub);

    sub = new JSONObject();
    for (final com.bolsinga.music.data.Relation r : music.getRelations()) {
      sub.put(r.getID(), Relation.createJSON(r));
    }
    json.put(RELATIONS, sub);

    sub = new JSONObject();
    for (final com.bolsinga.music.data.Song s : music.getSongs()) {
      sub.put(s.getID(), Song.createJSON(s));
    }
    json.put(SONGS, sub);
    
    sub = new JSONObject();
    for (final com.bolsinga.music.data.Album a : music.getAlbums()) {
      sub.put(a.getID(), Album.createJSON(a));
    }
    json.put(ALBUMS, sub);
    
    sub = new JSONObject();
    for (final com.bolsinga.music.data.Show s : music.getShows()) {
      sub.put(s.getID(), Show.createJSON(s));
    }
    json.put(SHOWS, sub);
    
    return json;
  }
  
  private Music() {
  
  }

  private Music(final com.bolsinga.music.data.Music music) {
    setTimestamp(music.getTimestamp());
    
    List<? extends com.bolsinga.music.data.Venue> srcVenues = music.getVenues();
    venues = new ArrayList<Venue>(srcVenues.size());
    for (com.bolsinga.music.data.Venue a : srcVenues) {
      venues.add(Venue.createOrGet(a));
    }
    
    List<? extends com.bolsinga.music.data.Artist> srcArtists = music.getArtists();
    artists = new ArrayList<Artist>(srcArtists.size());
    for (com.bolsinga.music.data.Artist a : srcArtists) {
      artists.add(Artist.createOrGet(a));
    }

    List<? extends com.bolsinga.music.data.Label> srcLabels = music.getLabels();
    labels = new ArrayList<Label>(srcLabels.size());
    for (com.bolsinga.music.data.Label a : srcLabels) {
      labels.add(Label.createOrGet(a));
    }

    List<? extends com.bolsinga.music.data.Relation> srcRelations = music.getRelations();
    relations = new ArrayList<Relation>(srcRelations.size());
    for (com.bolsinga.music.data.Relation a : srcRelations) {
      relations.add(Relation.create(a));
    }

    List<? extends com.bolsinga.music.data.Song> srcSongs = music.getSongs();
    songs = new ArrayList<Song>(srcSongs.size());
    for (com.bolsinga.music.data.Song a : srcSongs) {
      songs.add(Song.createOrGet(a));
    }

    List<? extends com.bolsinga.music.data.Album> srcAlbums = music.getAlbums();
    albums = new ArrayList<Album>(srcAlbums.size());
    for (com.bolsinga.music.data.Album a : srcAlbums) {
      albums.add(Album.createOrGet(a));
    }
    
    List<? extends com.bolsinga.music.data.Show> srcShows = music.getShows();
    shows = new ArrayList<Show>(srcShows.size());
    for (com.bolsinga.music.data.Show a : srcShows) {
      shows.add(Show.create(a));
    }
  }
  
  private Music(final JSONObject json) throws Exception {
    setTimestamp(com.bolsinga.web.Util.fromJSONCalendar(json.getString(TIMESTAMP)));
    
    JSONObject jsonMap = json.getJSONObject(VENUES);
    venues = new ArrayList<Venue>(jsonMap.length());
    Iterator i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      venues.add(Venue.createOrGet(key, jsonItem));
    }

    jsonMap = json.getJSONObject(LABELS);
    labels = new ArrayList<Label>(jsonMap.length());
    i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      labels.add(Label.createOrGet(key, jsonItem));
    }
  
    // Create all the Artists before Relations, Songs, Albums, Shows
    jsonMap = json.getJSONObject(ARTISTS);
    artists = new ArrayList<Artist>(jsonMap.length());
    i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      artists.add(Artist.createOrGet(key, jsonItem));
    }

    jsonMap = json.getJSONObject(RELATIONS);
    relations = new ArrayList<Relation>(jsonMap.length());
    i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      relations.add(Relation.create(jsonItem));
    }

    jsonMap = json.getJSONObject(SONGS);
    songs = new ArrayList<Song>(jsonMap.length());
    i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      songs.add(Song.createOrGet(key, jsonItem));
    }

    jsonMap = json.getJSONObject(ALBUMS);
    albums = new ArrayList<Album>(jsonMap.length());
    i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      albums.add(Album.createOrGet(key, jsonItem));
    }

    jsonMap = json.getJSONObject(SHOWS);
    shows = new ArrayList<Show>(jsonMap.length());
    i = jsonMap.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonItem = jsonMap.getJSONObject(key);
      shows.add(Show.create(jsonItem));
    }
  }
  
  public GregorianCalendar getTimestamp() {
    return com.bolsinga.web.Util.fromJSONCalendar(timestamp);
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    this.timestamp = com.bolsinga.web.Util.toJSONCalendar(timestamp);
  }
  
  public List<Venue> getVenues() {
    return Collections.unmodifiableList(venues);
  }
  
  public List<Venue> getVenuesCopy() {
    return new ArrayList<Venue>(venues);
  }
  
  public List<Artist> getArtists() {
    return Collections.unmodifiableList(artists);
  }
  
  public List<Artist> getArtistsCopy() {
    return new ArrayList<Artist>(artists);
  }
  
  public List<Label> getLabels() {
    return Collections.unmodifiableList(labels);
  }
  
  public List<Label> getLabelsCopy() {
    return new ArrayList<Label>(labels);
  }
  
  public List<Relation> getRelations() {
    return Collections.unmodifiableList(relations);
  }
  
  public List<Relation> getRelationsCopy() {
    return new ArrayList<Relation>(relations);
  }
  
  public List<Song> getSongs() {
    return Collections.unmodifiableList(songs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(songs);
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(albums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(albums);
  }
  
  public List<Show> getShows() {
    return Collections.unmodifiableList(shows);
  }
  
  public List<Show> getShowsCopy() {
    return new ArrayList<Show>(shows);
  }
}