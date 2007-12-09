package com.bolsinga.music.data;

import java.util.*;

public interface Music {
  public GregorianCalendar getTimestamp();
  public void setTimestamp(final GregorianCalendar timestamp);
  public List<Venue> getVenues();
  public List<Venue> getVenuesCopy();
  public List<Artist> getArtists();
  public List<Artist> getArtistsCopy();
  public List<Label> getLabels();
  public List<Label> getLabelsCopy();
  public List<Relation> getRelations();
  public List<Relation> getRelationsCopy();
  public List<Song> getSongs();
  public List<Song> getSongsCopy();
  public List<Album> getAlbums();
  public List<Album> getAlbumsCopy();
  public List<Show> getShows();
  public List<Show> getShowsCopy();
}
