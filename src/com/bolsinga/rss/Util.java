package com.bolsinga.rss;

import java.text.*;
import java.util.*;

/*
 * http://blogs.law.harvard.edu/tech/rss
 */

public class Util {
  private static final DateFormat sRSSDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
  static {
    sRSSDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
        
  public static String getRSSDate(GregorianCalendar c) {
    return sRSSDateFormat.format(c.getTime());
  }
        
  public static com.bolsinga.rss.data.TImage createLogo(com.bolsinga.rss.data.ObjectFactory objFactory) throws javax.xml.bind.JAXBException {
    com.bolsinga.rss.data.TImage logo = objFactory.createTImage();

    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getLogoImage();
    logo.setHeight(image.getHeight());
    logo.setWidth(image.getWidth());
    logo.setUrl(image.getLocation());
    logo.setTitle(image.getAlt());
                
    return logo;
  }
}
