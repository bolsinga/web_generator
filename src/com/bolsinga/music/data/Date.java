package com.bolsinga.music.data;

public interface Date {
  public static final int UNKNOWN = 0;
  
  public boolean isUnknown();
  
  public int getYear();
  public void setYear(final int year);

  // 1 based
  public int getMonth();
  public void setMonth(final int month);

  public int getDay();
  public void setDay(final int day);
}
