package com.bolsinga.rss;

import java.text.*;

public class Util {
  private static DateFormat sRSSDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        
  public static String getRSSDate(java.util.Date date) {
    return sRSSDateFormat.format(date);
  }
        
  public static com.bolsinga.rss.data.TRssChannel.Image createLogo(com.bolsinga.rss.data.ObjectFactory objFactory) throws javax.xml.bind.JAXBException {
    com.bolsinga.rss.data.TRssChannel.Image logo = objFactory.createTRssChannelImage();

    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getLogoImage();
    logo.setHeight(image.getHeight());
    logo.setWidth(image.getWidth());
    logo.setUrl(image.getLocation());
    logo.setTitle(image.getAlt());
                
    return logo;
  }
}
