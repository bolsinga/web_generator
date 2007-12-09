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
        
  public static com.bolsinga.rss.data.TImage createLogo(final com.bolsinga.rss.data.ObjectFactory objFactory) {
    com.bolsinga.rss.data.TImage logo = objFactory.createTImage();

    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getLogoImage();
    logo.setHeight(image.getHeight());
    logo.setWidth(image.getWidth());
    logo.setUrl(image.getLocation());
    logo.setTitle(image.getAlt());
                
    return logo;
  }
}
