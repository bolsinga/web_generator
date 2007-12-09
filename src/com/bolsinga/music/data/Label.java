package com.bolsinga.music.data;

public interface Label {
  public String getID();
  public void setID(final String id);
  public String getName();
  public void setName(final String name);
  public Location getLocation();
  public String getComment();
  public void setComment(final String comment);
}
