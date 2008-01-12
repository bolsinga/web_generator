package com.bolsinga.music.data;

import java.util.*;

public interface Artist {
  public String getID();
  public void setID(final String id);
  public String getName();
  public void setName(final String name);
  public String getSortname();
  public void setSortname(final String name);
  public Location getLocation();
  public String getComment();
  public void setComment(final String comment);
  public List<? extends Album> getAlbums();
  public List<? extends Album> getAlbumsCopy();
}
