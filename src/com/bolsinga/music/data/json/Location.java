package com.bolsinga.music.data.json;

import org.json.*;

public class Location implements com.bolsinga.music.data.Location {
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
  
  private Location() {
  
  }
  
  private Location(final com.bolsinga.music.data.Location location) {
    street = location.getStreet();
    city = location.getCity();
    state = location.getState();
    zip = location.getZip();
    web = location.getWeb();
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
