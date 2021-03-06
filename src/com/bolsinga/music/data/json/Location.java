package com.bolsinga.music.data.json;

import org.json.*;

public class Location implements com.bolsinga.music.data.Location {
  private static final String STREET = "street";
  private static final String CITY = "city";
  private static final String STATE = "state";
  private static final String ZIP = "zip";
  private static final String WEB = "web";

  private String street;
  private String city;
  private String state;
  private int zip = 0;
  private String web = null;
  
  static Location create(final com.bolsinga.music.data.Location location) {
    if (location == null) {
      return null;
    }
    return new Location(location);
  }
  
  static Location create(final JSONObject json) throws JSONException {
    return new Location(json);
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Location location) throws JSONException {
    JSONObject json = new JSONObject();
    
    String street = location.getStreet();
    if (street != null) {
      json.put(STREET, street);
    }
    String city = location.getCity();
    if (city != null) {
      json.put(CITY, city);
    }
    String state = location.getState();
    if (state != null) {
      json.put(STATE, state);
    }
    int zip = location.getZip();
    if (zip != 0) {
      json.put(ZIP, zip);
    }
    String web = location.getWeb();
    if (web != null) {
      json.put(WEB, web);
    }
    
    return json;
  }
  
  private Location() {
  
  }
  
  private Location(final com.bolsinga.music.data.Location location) {
    street = location.getStreet();
    city = location.getCity();
    state = location.getState();
    zip = location.getZip();
    web = location.getWeb();
  }
  
  private Location(final JSONObject json) throws JSONException {
    street = json.optString(STREET, null);
    city = json.optString(CITY, null);
    state = json.optString(STATE, null);
    zip = json.optInt(ZIP);
    web = json.optString(WEB, null);
  }
  
  public String getStreet() {
    return street;
  }
  
  public void setStreet(final String street) {
    this.street = street;
  }
  
  public String getCity() {
    return city;
  }
  
  public void setCity(final String city) {
    this.city = city;
  }
  
  public String getState() {
    return state;
  }
  
  public void setState(final String state) {
    this.state = state;
  }
  
  // return 0 if unknown
  public int getZip() {
    return zip;
  }

  public void setZip(final int zip) {
    this.zip = zip;
  }
  
  public String getWeb() {
    return web;
  }
  
  public void setWeb(final String web) {
    this.web = web;
  }
}
