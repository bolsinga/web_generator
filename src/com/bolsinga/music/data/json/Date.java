package com.bolsinga.music.data.json;

import org.json.*;

public class Date implements com.bolsinga.music.data.Date {
  private boolean unknown;
  private int year;
  private int month;
  private int day;
  
  private Date() {
  
  }
  
  public boolean isUnknown() {
    return unknown;
  }
  
  public int getYear() {
    return year;
  }
  
  public void setYear(final int year) {
    this.year = year;
  }

  // 1 based
  public int getMonth() {
    return month;
  }
  
  public void setMonth(final int month) {
    this.month = month;
  }
  
  public int getDay() {
    return day;
  }
  
  public void setDay(final int day) {
    this.day = day;
  }
}
