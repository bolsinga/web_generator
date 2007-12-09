package com.bolsinga.music.data.raw;

public class Location implements com.bolsinga.music.data.Location {
  private String fStreet;
  private String fCity;
  private String fState;
  private int fZip;
  private String fWeb;
  
  static Location create(final String street, final String city, final String state, final String web) {
    return new Location(street, city, state, web);
  }
  
  private Location(final String street, final String city, final String state, final String web) {
    fStreet = street;
    fCity = city;
    fState = state;
    fWeb = web;
  }
  
  public String getStreet() {
    return fStreet;
  }
  
  public void setStreet(final String street) {
    fStreet = street;
  }
  
  public String getCity() {
    return fCity;
  }
  
  public void setCity(final String city) {
    fCity = city;
  }
  
  public String getState() {
    return fState;
  }
  
  public void setState(final String state) {
    fState = state;
  }
  
  public int getZip() {
    return fZip;
  }
  
  public void setZip(final int zip) {
    fZip = zip;
  }
  
  public String getWeb() {
    return fWeb;
  }
  
  public void setWeb(final String web) {
    fWeb = web;
  }
}