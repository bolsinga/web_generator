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
    com.bolsinga.music.data.json.Music jsonMusic = null;
    if (music instanceof com.bolsinga.music.data.json.Music) {
      jsonMusic = (com.bolsinga.music.data.json.Music)music;
    } else {
      jsonMusic = new Music(music);
    }
    
    JSONObject json = null;
    /*
    Marshaller<com.bolsinga.music.data.json.Music> m = Marshaller.create(com.bolsinga.music.data.json.Music.class);
    json = m.marshall(jsonMusic);
    */
    
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
        fw.write(json.toString(2));
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
  
  private Music() {
  
  }

  private Music(final com.bolsinga.music.data.Music music) {
    setTimestamp(music.getTimestamp());
    
    List<? extends com.bolsinga.music.data.Venue> srcVenues = music.getVenues();
    venues = new ArrayList<Venue>(srcVenues.size());
    for (com.bolsinga.music.data.Venue venue : srcVenues) {
      venues.add(Venue.create(venue));
    }
    
    List<? extends com.bolsinga.music.data.Album> srcAlbums = music.getAlbums();
    albums = new ArrayList<Album>(srcAlbums.size());
    for (com.bolsinga.music.data.Album album : srcAlbums) {
      albums.add(Album.createOrGet(album));
    }
    
    List<? extends com.bolsinga.music.data.Artist> srcArtists = music.getArtists();
    artists = new ArrayList<Artist>(srcArtists.size());
    for (com.bolsinga.music.data.Artist artist : srcArtists) {
      artists.add(Artist.createOrGet(artist));
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
