package com.bolsinga.music.data;

import java.time.*;
import java.util.*;

public interface Music {
  public ZonedDateTime getTimestamp();
  public void setTimestamp(final ZonedDateTime timestamp);
  public List<? extends Venue> getVenues();
  public List<? extends Venue> getVenuesCopy();
  public List<? extends Artist> getArtists();
  public List<? extends Artist> getArtistsCopy();
  public List<? extends Relation> getRelations();
  public List<? extends Relation> getRelationsCopy();
  public List<? extends Song> getSongs();
  public List<? extends Song> getSongsCopy();
  public List<? extends Album> getAlbums();
  public List<? extends Album> getAlbumsCopy();
  public List<? extends Show> getShows();
  public List<? extends Show> getShowsCopy();
}
