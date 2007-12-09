package com.bolsinga.music.data.raw;

import java.math.*;
import java.util.*;

public class Date implements com.bolsinga.music.data.Date {
  private static final int PARSER_UNKNOWN_YEAR = 1900;

  private int fYear, fMonth, fDay;

  static Date create(final String data) {
    String monthString, dayString, yearString = null;
    int month, day, year = 0;
                
    StringTokenizer st = new StringTokenizer(data, "-");
                
    monthString = st.nextToken();
    dayString = st.nextToken();
    yearString = st.nextToken();
                
    month = Integer.parseInt(monthString);
    if (month == 0) {
      month = com.bolsinga.music.data.Date.UNKNOWN;
    }
    day = Integer.parseInt(dayString);
    if (day == 0) {
      day = com.bolsinga.music.data.Date.UNKNOWN;
    }
    year = Integer.parseInt(yearString);
    if (year == PARSER_UNKNOWN_YEAR) {
      year = com.bolsinga.music.data.Date.UNKNOWN;
    }
    
    return Date.create(year, month, day);
  }
  
  static Date create(final int year, final int month, final int day) {
    return new Date(year, month, day);
  }
  
  static Date create(final int year) {
    return Date.create(year, com.bolsinga.music.data.Date.UNKNOWN, com.bolsinga.music.data.Date.UNKNOWN);
  }
  
  private Date(final int year, final int month, final int day) {
    fYear = year;
    fMonth = month;
    fDay = day;
  }
  
  public boolean isUnknown() {
    return (fYear == com.bolsinga.music.data.Date.UNKNOWN) || (fMonth == com.bolsinga.music.data.Date.UNKNOWN) || (fDay == com.bolsinga.music.data.Date.UNKNOWN);
  }
  
  public int getYear() {
    return fYear;
  }
  
  public void setYear(final int year) {
    fYear = year;
  }

  // 1 based
  public int getMonth() {
    return fMonth;
  }
  
  public void setMonth(final int month) {
    fMonth = month;
  }

  public int getDay() {
    return fDay;
  }
  
  public void setDay(final int day) {
    fDay = day;
  }
}