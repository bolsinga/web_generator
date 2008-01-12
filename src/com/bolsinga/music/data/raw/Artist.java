package com.bolsinga.music.data.raw;

import java.io.*;
import java.util.*;

public class Artist implements com.bolsinga.music.data.Artist {
  // name -> Artist
  private static final HashMap<String, Artist> sMap = new HashMap<String, Artist>();

  private String fID;
  private String fName;
  private String fSortname;
  private com.bolsinga.music.data.Location fLocation;
  private String fComment;
  private List<com.bolsinga.music.data.Album> fAlbums;
  private HashSet<com.bolsinga.music.data.Album> fAlbumSet;

  private static String getSortName(final String name, final String sortName) {
    // This will return a diacritical-free sortName, based upon sortName if it is provided, otherwise name.
    //  It will return null if sortName is null and name does not include diacriticals.
    String result = null;
    
    String nameToBeStripped = (sortName != null) ? sortName : name;

    // The following is JDK 1.5 only. It was moved to java.text.Normalizer in JDK 1.6 with a different API.
    String stripped = sun.text.Normalizer.normalize(nameToBeStripped, sun.text.Normalizer.DECOMP, 0);
    
    if (!stripped.equals(nameToBeStripped)) {
      result = stripped;
    }
    
    if (result == null) {
      result = sortName;
    }
    
    return result;
  }
  
  private static void setSortNames(final String filename) throws com.bolsinga.web.WebException {
    BufferedReader in = null;
    try {
      try {
        in = new BufferedReader(new FileReader(filename));
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      String s = null;
      StringTokenizer st = null;
      try {
        while ((s = in.readLine()) != null) {
          st = new StringTokenizer(s, "*");
          
          String name = st.nextToken();
          String sortName = st.nextToken();
          
          Artist a = Artist.get(name);
          if (a != null) {
            a.setSortname(sortName);
          } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Artist: ");
            sb.append(name);
            sb.append(" (");
            sb.append(sortName);
            sb.append(") does not exist.");
            throw new com.bolsinga.web.WebException(sb.toString());
          }
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read bandsort file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(filename);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
  }

  static Artist get(final String name) {
    synchronized (sMap) {
      return sMap.get(name);
    }
  }
  
  static Artist createOrGet(final String name) {
    synchronized (sMap) {
      Artist result = sMap.get(name);
      if (result == null) {
        result = new Artist(name);
        sMap.put(name, result);
      }
      return result;
    }
  }
  
  static List<com.bolsinga.music.data.Artist> getList(final String bandFile) throws com.bolsinga.web.WebException {
    synchronized (sMap) {
      setSortNames(bandFile);
      
      for (Artist a : sMap.values()) {
        a.sortAlbums();
      }
      
      List<com.bolsinga.music.data.Artist> artists = new ArrayList<com.bolsinga.music.data.Artist>(sMap.values());
      java.util.Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_COMPARATOR);

      int index = 0;
      for (com.bolsinga.music.data.Artist a : artists) {
        a.setID("ar" + index++);
      }
            
      return artists;
    }
  }
  
  private Artist(final String name) {
    fName = name;
    fSortname = Artist.getSortName(name, null);
    fAlbumSet = new HashSet<com.bolsinga.music.data.Album>();
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
  
  public String getName() {
    return fName;
  }
  
  public void setName(final String name) {
    fName = name;
  }
  
  public String getSortname() {
    return fSortname;
  }
  
  public void setSortname(final String name) {
    fSortname = Artist.getSortName(fName, name);
  }
  
  public com.bolsinga.music.data.Location getLocation() {
    return fLocation;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
  }
  
  public List<? extends com.bolsinga.music.data.Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }
  
  public List<? extends com.bolsinga.music.data.Album> getAlbumsCopy() {
    return new ArrayList<com.bolsinga.music.data.Album>(fAlbums);
  }
  
  void addAlbum(final com.bolsinga.music.data.Album album) {
    fAlbumSet.add(album);
  }
  
  void sortAlbums() {
    fAlbums = new ArrayList<com.bolsinga.music.data.Album>(fAlbumSet);
    fAlbumSet = null;
    
    Collections.sort(fAlbums, com.bolsinga.music.Compare.ALBUM_ORDER_COMPARATOR);
  }
}
