package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Artist implements com.bolsinga.music.data.Artist {
  private String id;
  private String name;
  private String sortname = null;
  private Location location = null;
  private String comment = null;
  private List<Album> albums = null;
  
  private static final Map<String, Artist> sMap = new HashMap<String, Artist>();

  static Artist get(final com.bolsinga.music.data.Artist src) {
    synchronized (sMap) {
      return sMap.get(src.getID());
    }
  }
  
  static Artist createOrGet(final com.bolsinga.music.data.Artist src) {
    synchronized (sMap) {
      Artist result = sMap.get(src.getID());
      if (result == null) {
        result = new Artist(src);
        sMap.put(src.getID(), result);
      }
      return result;
    }
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Artist artist) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("id", artist.getID());
    json.put("name", artist.getName());
    String sortname = artist.getSortname();
    if (sortname != null) {
      json.put("sortname", sortname);
    }
    com.bolsinga.music.data.Location location = artist.getLocation();
    if (location != null) {
      json.put("location", Location.createJSON(location));
    }
    String comment = artist.getComment();
    if (comment != null) {
      json.put("comment", comment);
    }
    List<? extends com.bolsinga.music.data.Album> albums = artist.getAlbums();
    if (albums != null && albums.size() > 0) {
      List<String> albumIDs = new ArrayList<String>(albums.size());
      for (final com.bolsinga.music.data.Album album : albums) {
        albumIDs.add(album.getID());
      }
      json.put("albums", albumIDs);
    }
    
    return json;
  }

  private Artist() {
  
  }
  
  private Artist(final com.bolsinga.music.data.Artist artist) {
    id = artist.getID();
    name = artist.getName();
    sortname = artist.getSortname();
    location = Location.create(artist.getLocation());
    comment = artist.getComment();

    List<? extends com.bolsinga.music.data.Album> srcAlbums = artist.getAlbums();
    albums = new ArrayList<Album>(srcAlbums.size());
    for (com.bolsinga.music.data.Album album : srcAlbums) {
      albums.add(Album.createOrGet(album));
    }
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(final String name) {
    this.name = name;
  }
  
  public String getSortname() {
    return sortname;
  }
  
  public void setSortname(final String name) {
    this.sortname = name;
  }
  
  public Location getLocation() {
    return location;
  }
  
  public String getComment() {
    return comment;
  }
  
  public void setComment(final String comment) {
    this.comment = comment;
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(albums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(albums);
  }
}
