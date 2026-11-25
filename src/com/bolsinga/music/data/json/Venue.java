package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Venue implements com.bolsinga.music.data.Venue {
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String LOCATION = "location";
  private static final String SORTNAME = "sortname";
  private static final String MAPKIT = "mapkit";

  private String id;
  private String name;
  private Location location = null;
  private String sortname = null;
  private String mapKitID = null;
  
  private static final Map<String, Venue> sMap = new HashMap<String, Venue>();
  
  static Venue get(final String id) {
    synchronized (sMap) {
      return sMap.get(id);
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
    String sortname = venue.getSortname();
    if (sortname != null) {
      json.put(SORTNAME, sortname);
    }
    String mapKitID = venue.getMapKitID();
    if (mapKitID != null) {
      json.put(MAPKIT, mapKitID);
    }

    return json;
  }
  
  private Venue() {
  
  }
  
  private Venue(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    name = json.getString(NAME);
    JSONObject optJSON = json.optJSONObject(LOCATION);
    if (optJSON != null) {
      location = Location.create(optJSON);
    }
    sortname = json.optString(SORTNAME, null);
    mapKitID = json.optString(MAPKIT, null);
  }
  
  public String getID() {
    return id;
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
  
  public String getSortname() {
    return sortname;
  }

  public void setSortname(final String name) {
    this.sortname = name;
  }

  public String getMapKitID() {
    return mapKitID;
  }

  public void setMapKitID(final String mapKitID) {
    this.mapKitID = mapKitID;
  }
}
