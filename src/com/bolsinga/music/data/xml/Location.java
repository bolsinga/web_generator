package com.bolsinga.music.data.xml;

public class Location implements com.bolsinga.music.data.Location {
  private final com.bolsinga.music.data.xml.impl.Location fLocation;
  
  public static Location create(final com.bolsinga.music.data.xml.impl.Location item) {
    return new Location(item);
  }
  
  private Location(final com.bolsinga.music.data.xml.impl.Location location) {
    fLocation = location;
  }
  
  public String getStreet() {
    String r = null;
    if (fLocation != null) {
      r = fLocation.getStreet();
    }
    return r;
  }
  
  public void setStreet(final String street) {
    fLocation.setStreet(street);
  }

  public String getCity() {
    String r = null;
    if (fLocation != null) {
      r = fLocation.getCity();
    }
    return r;
  }
  
  public void setCity(final String city) {
    fLocation.setCity(city);
  }
  
  public String getState() {
    String r = null;
    if (fLocation != null) {
      r = fLocation.getState();
    }
    return r;
  }
  
  public void setState(final String state) {
    fLocation.setState(state);
  }

  public int getZip() {
    int result = 0;
    java.math.BigInteger zip = (fLocation != null) ? fLocation.getZip() : null;
    if (zip != null) {
      result = zip.intValue();
    }
    return result;
  }
  
  public void setZip(final int zip) {
    java.math.BigInteger val = null;
    if (zip != 0) {
      val = java.math.BigInteger.valueOf(zip);
    }
    fLocation.setZip(val);
  }

  public String getWeb() {
    String r = null;
    if (fLocation != null) {
      r = fLocation.getWeb();
    }
    return r;
  }
  
  public void setWeb(final String web) {
    fLocation.setWeb(web);
  }
}
