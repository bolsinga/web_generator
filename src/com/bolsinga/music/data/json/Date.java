package com.bolsinga.music.data.json;

import org.json.*;

public class Date implements com.bolsinga.music.data.Date {
  private static final String YEAR = "year";
  private static final String MONTH = "month";
  private static final String DAY = "day";

  private int year;
  private int month;
  private int day;
  
  static Date create(final com.bolsinga.music.data.Date date) {
    return new Date(date);
  }
  
  static Date create(final JSONObject json) throws JSONException {
    return new Date(json);
  }
  
  static JSONObject createJSON(com.bolsinga.music.data.Date date) throws JSONException {
    JSONObject json = new JSONObject();
    
    int i = date.getYear();
    if (i != com.bolsinga.music.data.Date.UNKNOWN) {
      json.put(YEAR, i);
    }
    i = date.getMonth();
    if (i != com.bolsinga.music.data.Date.UNKNOWN) {
      json.put(MONTH, i);
    }
    i = date.getDay();
    if (i != com.bolsinga.music.data.Date.UNKNOWN) {
      json.put(DAY, i);
    }
    
    return json;
  }
  
  private Date() {
  
  }
  
  private Date(final com.bolsinga.music.data.Date date) {
    year = date.getYear();
    month = date.getMonth();
    day = date.getDay();
  }
  
  private Date(final JSONObject json) throws JSONException {
    year = json.optInt(YEAR, com.bolsinga.music.data.Date.UNKNOWN);
    month = json.optInt(MONTH, com.bolsinga.music.data.Date.UNKNOWN);
    day = json.optInt(DAY, com.bolsinga.music.data.Date.UNKNOWN);
  }
  
  public boolean isUnknown() {
    return (year == com.bolsinga.music.data.Date.UNKNOWN) || (month == com.bolsinga.music.data.Date.UNKNOWN) || (day == com.bolsinga.music.data.Date.UNKNOWN);
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
