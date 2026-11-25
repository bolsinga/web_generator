package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Artist implements com.bolsinga.music.data.Artist {
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String SORTNAME = "sortname";
  private static final String ALBUMS = "albums";

  private String id;
  private String name;
  private String sortname; // optional
  private List<Album> albums = Collections.emptyList(); // optional

  // Albums are read after artists and add themselves here; finishAlbums() turns this into albums.
  private Map<String, Album> albumMap;
  
  private static final Map<String, Artist> sMap = new HashMap<String, Artist>();
  
  static Artist get(final String id) throws JSONException {
    synchronized (sMap) {
      return sMap.get(id);
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
    List<? extends com.bolsinga.music.data.Album> albums = artist.getAlbums();
    if (albums != null && albums.size() > 0) {
      List<String> albumIDs = new ArrayList<String>(albums.size());
      for (final com.bolsinga.music.data.Album album : albums) {
        albumIDs.add(album.getID());
      }
      Collections.sort(albumIDs);
      json.put(ALBUMS, albumIDs);
    }
    
    return json;
  }

  private Artist() {
  
  }
  
  private Artist(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    name = json.getString(NAME);
    sortname = json.optString(SORTNAME, null);

    // Albums refer to their artists, so they are read later and add themselves: see addAlbum() and
    // finishAlbums() below.
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
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(albums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(albums);
  }

  // Called by Music once every album is read, before any page runs, so pool threads only ever read
  // a finished list.
  void finishAlbums() {
    if (albumMap != null) {
      albums = new ArrayList<Album>(albumMap.values());
      albumMap = null;
    }
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
