package com.bolsinga.diary.data;

import java.util.*;

public interface Entry {
  public String getComment();
  public void setComment(final String comment);
  public GregorianCalendar getTimestamp();
  public void setTimestamp(final GregorianCalendar timestamp);
  public String getTitle();
  public void setTitle(final String title);
  public String getID();
  public void setID(final String id);
}