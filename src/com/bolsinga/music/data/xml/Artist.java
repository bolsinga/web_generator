package com.bolsinga.music.data.xml;

import java.util.*;

import javax.xml.bind.JAXBElement;

public class Artist implements com.bolsinga.music.data.Artist {
  private static final HashMap<String, Artist> sMap = new HashMap<String, Artist>();

  private final com.bolsinga.music.data.xml.impl.Artist fArtist;
  private final com.bolsinga.music.data.Location fLocation;
  private final List<com.bolsinga.music.data.Album> fAlbums;

  public static Artist get(final com.bolsinga.music.data.xml.impl.Artist item) {
    synchronized (sMap) {
      Artist result = sMap.get(item.getId());
      if (result == null) {
        result = new Artist(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Artist(final com.bolsinga.music.data.xml.impl.Artist artist) {
    fArtist = artist;
    fLocation = Location.create(fArtist.getLocation());
    
    fAlbums = new ArrayList<com.bolsinga.music.data.Album>(fArtist.getAlbum().size());
    for (JAXBElement<Object> jalbum : fArtist.getAlbum()) {
      com.bolsinga.music.data.xml.impl.Album album = (com.bolsinga.music.data.xml.impl.Album)jalbum.getValue();
      fAlbums.add(Album.get(album));
    }
  }
  
  public String getID() {
    return fArtist.getId();
  }
  
  public void setID(final String id) {
    fArtist.setId(id);
  }
  
  public String getName() {
    return fArtist.getName();
  }
  
  public void setName(final String name) {
    fArtist.setName(name);
  }
  
  public String getSortname() {
    return fArtist.getSortname();
  }
  
  public void setSortname(final String name) {
    fArtist.setSortname(name);
  }

  public com.bolsinga.music.data.Location getLocation() {
    return fLocation;
  }
  
  public String getComment() {
    return fArtist.getComment();
  }
  
  public void setComment(final String comment) {
    fArtist.setComment(comment);
  }
  
  public List<? extends com.bolsinga.music.data.Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }
  
  public List<? extends com.bolsinga.music.data.Album> getAlbumsCopy() {
    return new ArrayList<com.bolsinga.music.data.Album>(fAlbums);
  }
}
