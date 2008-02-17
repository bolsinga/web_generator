package com.bolsinga.music.data.json;

import java.io.*;
import java.util.*;

import org.json.*;

public class Music implements com.bolsinga.music.data.Music {
  private String timestamp;
  private List<Venue> venues;
  private List<Artist> artists;
  private List<Label> labels;
  private List<Relation> relations;
  private List<Song> songs;
  private List<Album> albums;
  private List<Show> shows;

  public static void export(final com.bolsinga.music.data.Music music, final String outputFile) throws com.bolsinga.web.WebException {    
    JSONObject json = null;
    try {
      json = Music.createJSON(music);
    } catch (JSONException e) {
      throw new com.bolsinga.web.WebException("Can't export json music", e);
    }
    
    FileWriter fw = null;
    try {
      try {
        fw = new FileWriter(outputFile);
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      try {
        if (com.bolsinga.web.Util.getPrettyOutput()) {
          fw.write(json.toString(2));
        } else {
          json.write(fw);
        }
      } catch (Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't write file: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(outputFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Music music) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("timestamp", com.bolsinga.web.Util.toJSONCalendar(music.getTimestamp()));
    
    JSONObject sub = new JSONObject();
    for (final com.bolsinga.music.data.Venue v : music.getVenues()) {
      sub.put(v.getID(), Venue.createJSON(v));
    }
    json.put("venues", sub);
    
    sub = new JSONObject();
    for (final com.bolsinga.music.data.Artist a : music.getArtists()) {
      sub.put(a.getID(), Artist.createJSON(a));
    }
    json.put("artists", sub);

    sub = new JSONObject();
    for (final com.bolsinga.music.data.Label l : music.getLabels()) {
      sub.put(l.getID(), Label.createJSON(l));
    }
    json.put("labels", sub);

    sub = new JSONObject();
    for (final com.bolsinga.music.data.Relation r : music.getRelations()) {
      sub.put(r.getID(), Relation.createJSON(r));
    }
    json.put("relations", sub);

    sub = new JSONObject();
    for (final com.bolsinga.music.data.Song s : music.getSongs()) {
      sub.put(s.getID(), Song.createJSON(s));
    }
    json.put("songs", sub);
    
    sub = new JSONObject();
    for (final com.bolsinga.music.data.Album a : music.getAlbums()) {
      sub.put(a.getID(), Album.createJSON(a));
    }
    json.put("albums", sub);
    
    sub = new JSONObject();
    for (final com.bolsinga.music.data.Show s : music.getShows()) {
      sub.put(s.getID(), Show.createJSON(s));
    }
    json.put("shows", sub);
    
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
