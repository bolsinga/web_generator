package com.bolsinga.diary.data;

import java.util.*;

public interface Diary {
  public GregorianCalendar getTimestamp();
  public void setTimestamp(final GregorianCalendar timestamp);
  public String getTitle();
  public void setTitle(final String title);
  public String getStatic();
  public void setStatic(final String staticData);
  public String getHeader();
  public void setHeader(final String header);
  public String getFriends();
  public void setFriends(final String friends);
  public String getColophon();
  public void setColophon(final String colophon);
  public List<? extends Entry> getEntries();
  public List<? extends Entry> getEntriesCopy();
}