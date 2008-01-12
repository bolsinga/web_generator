package com.bolsinga.music.data.xml;

import java.util.*;

import javax.xml.bind.JAXBElement;

public class Album implements com.bolsinga.music.data.Album {
  private static final HashMap<String, Album> sMap = new HashMap<String, Album>();

  private final com.bolsinga.music.data.xml.impl.Album fAlbum;
  private final List<Song> fSongs;
  private final List<String> fFormats;

  public static Album get(final com.bolsinga.music.data.xml.impl.Album item) {
    synchronized (sMap) {
      Album result = sMap.get(item.getId());
      if (result == null) {
        result = new Album(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Album(final com.bolsinga.music.data.xml.impl.Album album) {
    fAlbum = album;

    fSongs = new ArrayList<Song>(fAlbum.getSong().size());
    for (JAXBElement<Object> jsong : fAlbum.getSong()) {
      com.bolsinga.music.data.xml.impl.Song song = (com.bolsinga.music.data.xml.impl.Song)jsong.getValue();
      fSongs.add(Song.get(song));
    }

    fFormats = new ArrayList<String>(fAlbum.getFormat().size());
    for (JAXBElement<String> jformat : fAlbum.getFormat()) {
      String format = (String)jformat.getValue();
      fFormats.add(format);
    }
  }
  
  public String getID() {
    return fAlbum.getId();
  }
  
  public void setID(final String id) {
    fAlbum.setId(id);
  }
  
  public String getTitle() {
    return fAlbum.getTitle();
  }
  
  public void setTitle(final String title) {
    fAlbum.setTitle(title);
  }
  
  public Artist getPerformer() {
    Object performer = fAlbum.getPerformer();
    return (performer != null) ? Artist.get((com.bolsinga.music.data.xml.impl.Artist)performer) : null;
  }
  
  public com.bolsinga.music.data.Date getReleaseDate() {
    return Date.create(fAlbum.getReleaseDate());
  }
  
  public com.bolsinga.music.data.Date getPurchaseDate() {
    return Date.create(fAlbum.getPurchaseDate());
  }
  
  public boolean isCompilation() {
    Boolean value = fAlbum.isCompilation();
    return (value != null) ? value.booleanValue() : false;
  }
  
  public void setIsCompilation(final boolean isCompilation) {
    fAlbum.setCompilation(isCompilation);
  }

  public List<String> getFormats() {
    return Collections.unmodifiableList(fFormats);
  }
  
  public Label getLabel() {
    return Label.get((com.bolsinga.music.data.xml.impl.Label)fAlbum.getLabel());
  }

  public String getComment() {
    return fAlbum.getComment();
  }
  
  public void setComment(final String comment) {
    fAlbum.setComment(comment);
  }

  public List<Song> getSongs() {
    return Collections.unmodifiableList(fSongs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(fSongs);
  }
}
