package com.bolsinga.music.data.xml;

import java.util.*;

public class Venue implements com.bolsinga.music.data.Venue {
  private static final HashMap<String, Venue> sMap = new HashMap<String, Venue>();

  private final com.bolsinga.music.data.xml.impl.Venue fVenue;
  private final Location fLocation;
  
  public static Venue get(final com.bolsinga.music.data.xml.impl.Venue item) {
    synchronized (sMap) {
      Venue result = sMap.get(item.getId());
      if (result == null) {
        result = new Venue(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Venue(final com.bolsinga.music.data.xml.impl.Venue venue) {
    fVenue = venue;
    fLocation = Location.create(fVenue.getLocation());
  }
  
  public String getID() {
     return fVenue.getId();
  }
  
  public void setID(final String id) {
    fVenue.setId(id);
  }
  
  public String getName() {
    return fVenue.getName();
  }
  
  public void setName(final String name) {
    fVenue.setName(name);
  }
  
  public Location getLocation() {
    return fLocation;
  }

  public String getComment() {
    return fVenue.getComment();
  }
  
  public void setComment(final String comment) {
    fVenue.setComment(comment);
  }
}
