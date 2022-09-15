package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Venue implements com.bolsinga.music.data.Venue {
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String LOCATION = "location";
  private static final String COMMENT = "comment";
  private static final String SORTNAME = "sortname";

  private String id;
  private String name;
  private Location location = null;
  private String comment = null;
  private String sortname = null;
  
  private static final Map<String, Venue> sMap = new HashMap<String, Venue>();
  
  static Venue get(final String id) {
    synchronized (sMap) {
      return sMap.get(id);
    }
  }

  static Venue get(final com.bolsinga.music.data.Venue src) {
    return Venue.get(src.getID());
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
  
  static Venue createFromJSON(final JSONObject json) throws JSONException {
    Venue result = new Venue(json);
    synchronized (sMap) {
      sMap.put(result.getID(), result);
    }
    return result;
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Venue venue) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, venue.getID());
    json.put(NAME, venue.getName());
    com.bolsinga.music.data.Location location = venue.getLocation();
    if (location != null) {
      json.put(LOCATION, Location.createJSON(location));
    }
    String comment = venue.getComment();
    if (comment != null) {
      json.put(COMMENT, comment);
    }
    String sortname = venue.getSortname();
    if (sortname != null) {
      json.put(SORTNAME, sortname);
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
  
  private Venue(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    name = json.getString(NAME);
    JSONObject optJSON = json.optJSONObject(LOCATION);
    if (optJSON != null) {
      location = Location.create(optJSON);
    }
    comment = json.optString(COMMENT, null);
    sortname = json.optString(SORTNAME, null);
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

  public String getSortname() {
    return sortname;
  }

  public void setSortname(final String name) {
    this.sortname = name;
  }
}
