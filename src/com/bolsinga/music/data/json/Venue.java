package com.bolsinga.music.data.json;

import org.json.*;

public class Venue implements com.bolsinga.music.data.Venue {
  private String id;
  private String name;
  private Location location = null;
  private String comment = null;
  
  static Venue create(final com.bolsinga.music.data.Venue venue) {
    return new Venue(venue);
  }
  
  private Venue() {
  
  }
  
  private Venue(final com.bolsinga.music.data.Venue venue) {
    this.id = venue.getID();
    this.name = venue.getName();
    this.location = Location.create(venue.getLocation());
    this.comment = venue.getComment();
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(final String name) {
    this.name = name;
  }
  
  public Location getLocation() {
    return location;
  }
  
  public String getComment() {
    return comment;
  }
  
  public void setComment(final String comment) {
    this.comment = comment;
  }
}
