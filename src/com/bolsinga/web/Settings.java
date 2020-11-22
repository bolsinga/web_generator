package com.bolsinga.web;

import java.util.*;

public interface Settings {
  public class Image {
    private final String fLocation;
    private final int fWidth;
    private final int fHeight;
    private final String fAlt;

    public static Image createImage(final String location, final int width, final int height, final String alt) {
      return new Image(location, width, height, alt);
    }

    private Image(final String location, final int width, final int height, final String alt) {
      fLocation = location;
      fWidth = width;
      fHeight = height;
      fAlt = alt;
    }

    public String getLocation() {
      return fLocation;
    }

    public int getWidth() {
      return fWidth;
    }

    public int getHeight() {
      return fHeight;
    }

    public String getAlt() {
      return fAlt;
    }
  }

  public String getContact();
  public String getIco();
  public String getWebClipIcon();
  public Image getLogoImage();
  public String getCssFile();
  public String getRssFile();
  public int getDiaryCount();
  public int getRecentCount();
  public String getRoot();
  public String getRssDescription();
  public String getIcalName();
  public String getLinksTitle();
  public String getFriendsTitle();
  public boolean isEmbedLinks();
  public int getShowTime();
  public int getCopyrightStartYear();
  public int getDiaryEntryTime();
}
