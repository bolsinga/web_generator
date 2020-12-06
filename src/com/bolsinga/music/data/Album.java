package com.bolsinga.music.data;

import java.util.*;

public interface Album {
  public String getID();
  public void setID(final String id);
  public String getTitle();
  public void setTitle(final String title);
  public Artist getPerformer();
  public Date getReleaseDate();
  public Date getPurchaseDate();
  public boolean isCompilation();
  public void setIsCompilation(final boolean isCompilation);
  public String getComment();
  public void setComment(final String comment);
  public List<? extends Song> getSongs();
  public List<? extends Song> getSongsCopy();
}
