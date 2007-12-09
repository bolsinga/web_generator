package com.bolsinga.music.data;

import java.util.*;

public interface Album {
  public static final String FORMAT_12_INCH_LP       = "12 Inch LP";
  public static final String FORMAT_12_INCH_EP       = "12 Inch EP";
  public static final String FORMAT_12_INCH_SINGLE   = "12 Inch Single";
  public static final String FORMAT_10_INCH_EP       = "10 Inch EP";
  public static final String FORMAT_7_INCH_SINGLE    = "7 Inch Single";
  public static final String FORMAT_CASSETTE         = "Cassette";
  public static final String FORMAT_CD               = "CD";
  public static final String FORMAT_DIGITAL_FILE     = "Digital File";
  
  public String getID();
  public void setID(final String id);
  public String getTitle();
  public void setTitle(final String title);
  public Artist getPerformer();
  public Date getReleaseDate();
  public Date getPurchaseDate();
  public boolean isCompilation();
  public void setIsCompilation(final boolean isCompilation);
  public List<String> getFormats();
  public Label getLabel();
  public String getComment();
  public void setComment(final String comment);
  public List<Song> getSongs();
  public List<Song> getSongsCopy();
}
