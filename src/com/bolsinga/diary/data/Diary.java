package com.bolsinga.diary.data;

import java.util.*;

public interface Diary {
  public GregorianCalendar getTimestamp();
  public void setTimestamp(final GregorianCalendar timestamp);
  public String getTitle();
  public void setTitle(final String title);
  public List<String> getStatic();
  public void setStatic(final List<String> staticData);
  public List<String> getHeader();
  public void setHeader(final List<String> header);
  public List<String> getFriends();
  public void setFriends(final List<String> friends);
  public List<String> getColophon();
  public void setColophon(final List<String> colophon);
  public List<? extends Entry> getEntries();
  public List<? extends Entry> getEntriesCopy();
}
