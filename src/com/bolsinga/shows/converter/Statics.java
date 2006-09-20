package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import com.bolsinga.shows.converter.*;

public class Statics {
  private final String fLocation;
  private final String fData;
        
  public Statics(String location, String data) {
    fLocation = location;
    fData = data;
  }
        
  public String getLocation() {
    return fLocation;
  }
        
  public String getData() {
    return fData;
  }
        
  public String toString() {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getClass().getName());
    sb.append(" Location: ");
    sb.append(getLocation());
    sb.append(" Data: ");
    sb.append(getData());
                
    return sb.toString();
  }
}
