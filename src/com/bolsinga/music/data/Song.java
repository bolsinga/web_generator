package com.bolsinga.music.data;

import java.time.*;
import java.util.*;

public interface Song {
  public String getID();
  public void setID(final String id);
  public Artist getPerformer();
  public String getTitle();
  public void setTitle(final String title);
  public Date getReleaseDate();
  public ZonedDateTime getLastPlayed();
  public void setLastPlayed(final ZonedDateTime c);
  public int getTrack(); // return 0 if unknown
  public void setTrack(final int track);
  public String getGenre();
  public void setGenre(final String genre);
  public int getPlayCount();
  public void setPlayCount(final int playCount);
  public boolean isDigitized();
  public boolean isLive();
}
