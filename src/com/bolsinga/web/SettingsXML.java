package com.bolsinga.web;

import java.io.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class SettingsXML implements com.bolsinga.web.Settings {
  private final com.bolsinga.settings.data.Settings fSettings;
  private final com.bolsinga.web.Settings.Image fLogoImage;
  private final com.bolsinga.web.Settings.Image fRssImage;
  private final com.bolsinga.web.Settings.Image fiCalImage;
  private final com.bolsinga.web.Settings.GoogleMeta fGoogleMeta;

  public static com.bolsinga.web.Settings create(final String sourceFile) throws com.bolsinga.web.WebException {
    InputStream is = null;
    com.bolsinga.settings.data.Settings settings = null;
    try {
      try {
        is = new FileInputStream(sourceFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find settings file: ");
        sb.append(sourceFile);
        throw new WebException(sb.toString(), e);
      }
        
      try {
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.settings.data");
        Unmarshaller u = jc.createUnmarshaller();
                  
        settings = (com.bolsinga.settings.data.Settings)u.unmarshal(is);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't unmarsal settings file: ");
        sb.append(sourceFile);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close settings file: ");
          sb.append(sourceFile);
          throw new WebException(sb.toString(), e);
        }
      }
    }

    return new SettingsXML(settings);
  }

  private static com.bolsinga.web.Settings.Image createImage(final com.bolsinga.settings.data.Image image) {
    return com.bolsinga.web.Settings.Image.createImage(image.getLocation(), image.getWidth(), image.getHeight(), image.getAlt());
  }

  private SettingsXML(final com.bolsinga.settings.data.Settings settings) {
    fSettings = settings;
    fLogoImage = createImage(settings.getLogoImage());
    fRssImage = createImage(settings.getRssImage());
    fiCalImage = createImage(settings.getIcalImage());
    fGoogleMeta = com.bolsinga.web.Settings.GoogleMeta.createGoogleMeta(settings.getGoogleMeta().getName(), settings.getGoogleMeta().getContent());
  }

  public String getContact() {
    return fSettings.getContact();
  }
  
  public String getIco() {
    return fSettings.getIco();
  }

  public String getWebClipIcon() {
    return fSettings.getWebClipIcon();
  }

  public Image getLogoImage() {
    return fLogoImage;
  }

  public Image getRssImage() {
    return fRssImage;
  }

  public Image getIcalImage() {
    return fiCalImage;
  }

  public String getCssFile() {
    return fSettings.getCssFile();
  }

  public String getRssFile() {
    return fSettings.getRssFile();
  }

  public int getDiaryCount() {
    return fSettings.getDiaryCount().intValue();
  }

  public int getRecentCount() {
    return fSettings.getRecentCount().intValue();
  }

  public String getRoot() {
    return fSettings.getRoot();
  }

  public String getRssDescription() {
    return fSettings.getRssDescription();
  }

  public String getIcalName() {
    return fSettings.getIcalName();
  }

  public String getLinksTitle() {
    return fSettings.getLinksTitle();
  }

  public String getFriendsTitle() {
    return fSettings.getFriendsTitle();
  }

  public boolean isEmbedLinks() {
    return fSettings.isEmbedLinks();
  }

  public int getShowTime() {
    return fSettings.getShowTime().intValue();
  }

  public int getCopyrightStartYear() {
    return fSettings.getCopyrightStartYear().intValue();
  }

  public int getDiaryEntryTime() {
    return fSettings.getDiaryEntryTime().intValue();
  }

  public String getPageFooter() {
    return fSettings.getPageFooter();
  }

  public GoogleMeta getGoogleMeta() {
    return fGoogleMeta;
  }
}
