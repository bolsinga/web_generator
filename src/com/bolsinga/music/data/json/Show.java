package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Show implements com.bolsinga.music.data.Show {
  private List<Artist> artists;
  private Date date;
  private Venue venue;
  private String comment = null;
  private String id;
  
  static Show create(final com.bolsinga.music.data.Show show) {
    return new Show(show);
  }
  
  private Show() {
  
  }
  
  private Show(final com.bolsinga.music.data.Show show) {
    id = show.getID();
    comment = show.getComment();
//    venue = Venue.
//    date = ;
  }
  
  public List<Artist> getArtists() {
    return artists;
  }
  
  public Date getDate() {
    return date;
  }
  
  public Venue getVenue() {
    return venue;
  }
  
  public String getComment() {
    return comment;
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
}
