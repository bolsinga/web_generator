package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Venue implements com.bolsinga.music.data.Venue {
  private String id;
  private String name;
  private Location location = null;
  private String comment = null;
  
  private static final Map<String, Venue> sMap = new HashMap<String, Venue>();

  static Venue get(final com.bolsinga.music.data.Venue src) {
    synchronized (sMap) {
      return sMap.get(src.getID());
    }
  }
  
  static Venue createOrGet(final com.bolsinga.music.data.Venue src) {
    synchronized (sMap) {
      Venue result = sMap.get(src.getID());
      if (result == null) {
        result = new Venue(src);
        sMap.put(src.getID(), result);
      }
      return result;
    }
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Venue venue) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("id", venue.getID());
    json.put("name", venue.getName());
    com.bolsinga.music.data.Location location = venue.getLocation();
    if (location != null) {
      json.put("location", Location.createJSON(location));
    }
    String comment = venue.getComment();
    if (comment != null) {
      json.put("comment", comment);
    }
    
    return json;
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
