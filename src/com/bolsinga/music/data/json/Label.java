package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Label implements com.bolsinga.music.data.Label {
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String LOCATION = "location";
  private static final String COMMENT = "comment";

  private String id;
  private String name;
  private Location location = null;
  private String comment = null;

  private static final Map<String, Label> sMap = new HashMap<String, Label>();
  
  static Label get(final String id) {
    synchronized (sMap) {
      return sMap.get(id);
    }
  }

  static Label get(final com.bolsinga.music.data.Label src) {
    return Label.get(src.getID());
  }
  
  static Label createOrGet(final com.bolsinga.music.data.Label src) {
    synchronized (sMap) {
      Label result = sMap.get(src.getID());
      if (result == null) {
        result = new Label(src);
        sMap.put(src.getID(), result);
      }
      return result;
    }
  }

  static Label createOrGet(final String id, final JSONObject json) throws JSONException {
    synchronized (sMap) {
      Label result = sMap.get(id);
      if (result == null) {
        result = new Label(json);
        sMap.put(id, result);
      }
      return result;
    }
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Label label) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, label.getID());
    json.put(NAME, label.getName());
    com.bolsinga.music.data.Location location = label.getLocation();
    if (location != null) {
      json.put(LOCATION, Location.createJSON(location));
    }
    String comment = label.getComment();
    if (comment != null) {
      json.put(COMMENT, comment);
    }
    
    return json;
  }
  
  private Label() {
  
  }
  
  private Label(final com.bolsinga.music.data.Label label) {
    id = label.getID();
    name = label.getName();
    location = Location.create(label.getLocation());
    comment = label.getComment();
  }
  
  private Label(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    name = json.getString(NAME);
    JSONObject optJSON = json.optJSONObject(LOCATION);
    if (optJSON != null) {
      location = Location.create(optJSON);
    }
    comment = json.optString(COMMENT, null);
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
