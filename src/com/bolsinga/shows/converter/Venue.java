package com.bolsinga.shows.converter;

public class Venue {
  private final String fName;
  private final String fCity;
  private final String fState;
  private final String fAddress;
  private final String fURL;
        
  public Venue(String name, String city, String state, String address, String url) {
    fName = name;
    fCity = city;
    fState = state;
    fAddress = address;
    fURL = url;
  }
        
  public String getName() {
    return fName;
  }
        
  public String getCity() {
    return fCity;
  }
        
  public String getState() {
    return fState;
  }
        
  public String getAddress() {
    return fAddress;
  }

  public String getURL() {
    return fURL;
  }
        
  public String toString() {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getClass().getName());
    sb.append(" Name: ");
    sb.append(getName());
    sb.append(" City: ");
    sb.append(getCity());
    sb.append(" State: ");
    sb.append(getState());
    sb.append(" Address: ");
    sb.append(getAddress());
    sb.append(" URL: ");
    sb.append(getURL());
                
    return sb.toString();
  }
}
