package com.bolsinga.music.data.xml;

import java.math.*;

public class Date implements com.bolsinga.music.data.Date {
  private final com.bolsinga.music.data.xml.impl.Date fDate;

  public static Date create(final com.bolsinga.music.data.xml.impl.Date date) {
    if (date != null) {
      return new Date(date);
    } else {
      return null;
    }
  }
  
  private Date(final com.bolsinga.music.data.xml.impl.Date date) {
    fDate = date;
  }
  
  public boolean isUnknown() {
    Boolean value = fDate.isUnknown();
    return (value != null) ? value.booleanValue() : false;
  }
  
  public int getYear() {
    BigInteger i = fDate.getYear();
    return (i != null) ? i.intValue() : com.bolsinga.music.data.Date.UNKNOWN;
  }
  
  public void setYear(final int year) {
    BigInteger i = null;
    if (year != com.bolsinga.music.data.Date.UNKNOWN) {
      i = BigInteger.valueOf(year);
    }
    fDate.setYear(i);
  }

  public int getMonth() {
    Integer i = fDate.getMonth();
    return (i != null) ? i.intValue() : com.bolsinga.music.data.Date.UNKNOWN;
  }
  
  public void setMonth(final int month) {
    Integer i = null;
    if (month != com.bolsinga.music.data.Date.UNKNOWN) {
      i = Integer.valueOf(month);
    }
    fDate.setMonth(i);
  }

  public int getDay() {
    Integer i = fDate.getDay();
    return (i != null) ? i.intValue() : com.bolsinga.music.data.Date.UNKNOWN;
  }
  
  public void setDay(final int day) {
    Integer i = null;
    if (day != com.bolsinga.music.data.Date.UNKNOWN) {
      i = Integer.valueOf(day);
    }
    fDate.setDay(i);
  }
}
