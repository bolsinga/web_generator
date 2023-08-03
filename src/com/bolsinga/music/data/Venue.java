package com.bolsinga.music.data;

public interface Venue {
  public String getID();
  public String getName();
  public void setName(final String name);
  public Location getLocation();
  public String getComment();
  public void setComment(final String comment);
  public String getSortname();
  public void setSortname(final String name);
}
