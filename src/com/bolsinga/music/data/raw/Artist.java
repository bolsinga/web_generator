package com.bolsinga.music.data.raw;

import java.io.*;
import java.util.*;

public class Artist implements com.bolsinga.music.data.Artist {
  // name -> Artist
  private static final HashMap<String, Artist> sMap = new HashMap<String, Artist>();

  private String fID;
  private String fName;
  private String fSortname;
  private Location fLocation;
  private String fComment;
  private List<Album> fAlbums;
  private HashSet<Album> fAlbumSet;

  private static String getSortName(final String name, final String sortName) {
    // This will return a diacritical-free sortName, based upon sortName if it is provided, otherwise name.
    //  It will return null if sortName is null and name does not include diacriticals.
    String result = null;
    
    String nameToBeStripped = (sortName != null) ? sortName : name;

    String stripped = java.text.Normalizer.normalize(nameToBeStripped, java.text.Normalizer.Form.NFD);
    
    if (!stripped.equals(nameToBeStripped)) {
      result = stripped;
    }
    
    if (result == null) {
      result = sortName;
    }
    
    return result;
  }
  
  private static void setSortNames(final String filename) throws com.bolsinga.web.WebException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
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
    } catch (UnsupportedEncodingException e)  {
      StringBuilder sb = new StringBuilder();
      sb.append("Unsupported Encoding: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }

  private static void setIDs(final String filename) throws com.bolsinga.web.WebException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
      String s = null;
      StringTokenizer st = null;
      try {
        while ((s = in.readLine()) != null) {
          st = new StringTokenizer(s, "^");

          String id = st.nextToken();
          String name = st.nextToken();

          Artist a = Artist.get(name);
          if (a != null) {
            a.setID(id);
          } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Artist: ");
            sb.append(name);
            sb.append(" (");
            sb.append(id);
            sb.append(") does not exist.");
            throw new com.bolsinga.web.WebException(sb.toString());
          }
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read artistIDs file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } catch (UnsupportedEncodingException e)  {
      StringBuilder sb = new StringBuilder();
      sb.append("Unsupported Encoding: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
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
  
  static List<Artist> getList(final String bandFile, final String artistIDsFile) throws com.bolsinga.web.WebException {
    synchronized (sMap) {
      setSortNames(bandFile);

      setIDs(artistIDsFile);

      for (Artist a : sMap.values()) {
        a.sortAlbums();
      }
      
      List<Artist> artists = new ArrayList<Artist>(sMap.values());
      java.util.Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_COMPARATOR);

      return artists;
    }
  }
  
  private Artist(final String name) {
    fName = name;
    fSortname = Artist.getSortName(name, null);
    fAlbumSet = new HashSet<Album>();
  }
  
  public String getID() {
    assert fID != null : "No ID for: " + fName;
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
  
  public Location getLocation() {
    return fLocation;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(fAlbums);
  }
  
  void addAlbum(final Album album) {
    fAlbumSet.add(album);
  }
  
  void sortAlbums() {
    fAlbums = new ArrayList<Album>(fAlbumSet);
    fAlbumSet = null;
    
    Collections.sort(fAlbums, com.bolsinga.music.Compare.ALBUM_ORDER_COMPARATOR);
  }
}
