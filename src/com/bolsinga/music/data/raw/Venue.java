package com.bolsinga.music.data.raw;

import java.io.*;
import java.util.*;

public class Venue implements com.bolsinga.music.data.Venue {
  // name -> Venue
  private static final HashMap<String, Venue> sMap = new HashMap<String, Venue>();

  private String fID;
  private String fName;
  private com.bolsinga.music.data.Location fLocation;
  private String fComment;
  
  static List<com.bolsinga.music.data.Venue> create(final String filename) throws com.bolsinga.web.WebException {
    BufferedReader in = null;
    try {
      try {
        in = new BufferedReader(new FileReader(filename));
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }

      String s = null;
      StringTokenizer st = null;
      String name, city, state, address, url;
      try {
        while ((s = in.readLine()) != null) {
          st = new StringTokenizer(s, "*");

          name = st.nextToken();
          city = st.nextToken();
          state = st.nextToken();
          
          if (st.hasMoreElements()) {
            address = st.nextToken();
          } else {
            address = null;
          }
          
          if (st.hasMoreElements()) {
            url = st.nextToken();
          } else {
            url = null;
          }
          
          Venue v = new Venue(name, Location.create(address, city, state, url));

          sMap.put(name, v);
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read venuemap file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(filename);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }

    ArrayList<com.bolsinga.music.data.Venue> venues = new ArrayList<com.bolsinga.music.data.Venue>(sMap.values());
    java.util.Collections.sort(venues, com.bolsinga.music.Compare.VENUE_COMPARATOR);

    int index = 0;
    for (com.bolsinga.music.data.Venue v : venues) {
      v.setID("v" + index++);
    }
    
    return venues;
  }
  
  static Venue get(final String name) throws com.bolsinga.web.WebException {
    synchronized (sMap) {
      Venue result = sMap.get(name);
      if (result == null) {
        StringBuilder sb = new StringBuilder();
        sb.append("Venue: ");
        sb.append(name);
        sb.append(" does not exist.");
        throw new com.bolsinga.web.WebException(sb.toString());
      }
      return result;
    }
  }
  
  private Venue(final String name, final com.bolsinga.music.data.Location location) {
    fName = name;
    fLocation = location;
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
  
  public String getName() {
    return fName;
  }
  
  public void setName(final String name) {
    fName = name;
  }
  
  public com.bolsinga.music.data.Location getLocation() {
    return fLocation;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
  }
}