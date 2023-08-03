package com.bolsinga.music.data;

import java.util.*;

public interface Show {
  public List<? extends Artist> getArtists();
  public Date getDate();
  public Venue getVenue();
  public String getComment();
  public String getID();
}
