package com.bolsinga.rss;

import java.text.*;
import java.util.*;

/*
 * http://blogs.law.harvard.edu/tech/rss
 */

public class Util {
  private static final ThreadLocal<DateFormat> sRSSDateFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      DateFormat result = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
      result.setTimeZone(TimeZone.getTimeZone("GMT"));
      return result;
    }
  };
        
  public static String getRSSDate(final Calendar c) {
    return sRSSDateFormat.get().format(c.getTime());
  }
}
