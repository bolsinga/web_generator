package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Artist implements com.bolsinga.music.data.Artist {
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String SORTNAME = "sortname";
  private static final String LOCATION = "location";
  private static final String COMMENT = "comment";
  private static final String ALBUMS = "albums";

  private String id;
  private String name;
  private String sortname; // optional
  private Location location; // optional
  private String comment; // optional
  private List<Album> albums; // optional
  
  // This is for getting Albums lazily due to circular references
  private Map<String, Album> albumMap;
  
  private static final Map<String, Artist> sMap = new HashMap<String, Artist>();
  
  static Artist get(final String id) throws JSONException {
    synchronized (sMap) {
      return sMap.get(id);
    }
  }

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

  static Artist createFromJSON(final JSONObject json) throws JSONException {
    Artist result = new Artist(json);
    synchronized (sMap) {
      sMap.put(result.getID(), result);
    }
    return result;
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Artist artist) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, artist.getID());
    json.put(NAME, artist.getName());
    String sortname = artist.getSortname();
    if (sortname != null) {
      json.put(SORTNAME, sortname);
    }
    com.bolsinga.music.data.Location location = artist.getLocation();
    if (location != null) {
      json.put(LOCATION, Location.createJSON(location));
    }
    String comment = artist.getComment();
    if (comment != null) {
      json.put(COMMENT, comment);
    }
    List<? extends com.bolsinga.music.data.Album> albums = artist.getAlbums();
    if (albums != null && albums.size() > 0) {
      List<String> albumIDs = new ArrayList<String>(albums.size());
      for (final com.bolsinga.music.data.Album album : albums) {
        albumIDs.add(album.getID());
      }
      json.put(ALBUMS, albumIDs);
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
  
  private Artist(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    name = json.getString(NAME);
    sortname = json.optString(SORTNAME, null);
    JSONObject optJSON = json.optJSONObject(LOCATION);
    if (optJSON != null) {
      location = Location.create(optJSON);
    }
    comment = json.optString(COMMENT, null);

    // Circular reference for Album, so Album are created lazily.
    //  see addAlbum(), getAlbumsInternal() below.
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
  
  private List<Album> getAlbumsInternal() {
    if (albums == null) {
      // This lazily builds the Album list
      if (albumMap != null) {
        albums = new ArrayList<Album>(albumMap.values());
        albumMap = null;
      } else {
        albums = new ArrayList<Album>();
      }
    }
    return albums;
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(getAlbumsInternal());
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(getAlbumsInternal());
  }

  void addAlbum(final Album album) {
    if (albumMap == null) {
        albumMap = new HashMap<String, Album>();
    }
    String id = album.getID();
    if (!albumMap.containsKey(id)) {
      albumMap.put(id, album);
    }
  }
}
