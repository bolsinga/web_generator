package com.bolsinga.web;

import java.io.*;
import java.util.*;

public class SettingsProperties implements com.bolsinga.web.Settings {
  private final Properties fProperties;
  private final com.bolsinga.web.Settings.Image fLogoImage;
  private final com.bolsinga.web.Settings.Image fRssImage;

  public static com.bolsinga.web.Settings create(final String sourceFile) throws com.bolsinga.web.WebException {
    InputStream is = null;
    Properties properties = null;
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
        properties = new Properties();

        properties.load(is);
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't load properties settings file: ");
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

    return new SettingsProperties(properties);
  }

  private static com.bolsinga.web.Settings.Image createImage(final Properties properties, final String imageKeyName) {
    final String location = properties.getProperty(imageKeyName + ".location");
    final int width = Integer.parseInt(properties.getProperty(imageKeyName + ".width"));
    final int height = Integer.parseInt(properties.getProperty(imageKeyName + ".height"));
    final String alt = properties.getProperty(imageKeyName + ".alt");
    return com.bolsinga.web.Settings.Image.createImage(location, width, height, alt);
  }

  private SettingsProperties(final Properties properties) {
    fProperties = properties;
    fLogoImage = createImage(properties, "logoImage");
    fRssImage = createImage(properties, "rssImage");
  }

  public String getContact() {
    return fProperties.getProperty("contact");
  }
  
  public String getIco() {
    return fProperties.getProperty("ico");
  }

  public String getWebClipIcon() {
    return fProperties.getProperty("webClipIcon");
  }

  public Image getLogoImage() {
    return fLogoImage;
  }

  public Image getRssImage() {
    return fRssImage;
  }

  public String getCssFile() {
    return fProperties.getProperty("cssFile");
  }

  public String getRssFile() {
    return fProperties.getProperty("rssFile");
  }

  public int getDiaryCount() {
    return Integer.parseInt(fProperties.getProperty("diaryCount"));
  }

  public int getRecentCount() {
    return Integer.parseInt(fProperties.getProperty("recentCount"));
  }

  public String getRoot() {
    return fProperties.getProperty("root");
  }

  public String getRssDescription() {
    return fProperties.getProperty("rssDescription");
  }

  public String getIcalName() {
    return fProperties.getProperty("icalName");
  }

  public String getLinksTitle() {
    return fProperties.getProperty("linksTitle");
  }

  public String getFriendsTitle() {
    return fProperties.getProperty("friendsTitle");
  }

  public boolean isEmbedLinks() {
    return Boolean.parseBoolean(fProperties.getProperty("embedLinks"));
  }

  public int getShowTime() {
    return Integer.parseInt(fProperties.getProperty("showTime"));
  }

  public int getCopyrightStartYear() {
    return Integer.parseInt(fProperties.getProperty("copyrightStartYear"));
  }

  public int getDiaryEntryTime() {
    return Integer.parseInt(fProperties.getProperty("diaryEntryTime"));
  }
}
