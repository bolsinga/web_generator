package com.bolsinga.music.data.json;

import org.json.*;

public class Date implements com.bolsinga.music.data.Date {
  private boolean unknown;
  private int year;
  private int month;
  private int day;
  
  static Date create(final com.bolsinga.music.data.Date date) {
    return new Date(date);
  }
  
  static JSONObject createJSON(com.bolsinga.music.data.Date date) throws JSONException {
    JSONObject json = new JSONObject();
    
    boolean unknown = date.isUnknown();
    if (unknown) {
      json.put("unknown", true);
    }
    int i = date.getYear();
    if (i != com.bolsinga.music.data.Date.UNKNOWN) {
      json.put("year", i);
    }
    i = date.getMonth();
    if (i != com.bolsinga.music.data.Date.UNKNOWN) {
      json.put("month", i);
    }
    i = date.getDay();
    if (i != com.bolsinga.music.data.Date.UNKNOWN) {
      json.put("day", i);
    }
    
    return json;
  }
  
  private Date() {
  
  }
  
  private Date(final com.bolsinga.music.data.Date date) {
    unknown = date.isUnknown();
    year = date.getYear();
    month = date.getMonth();
    day = date.getDay();
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
