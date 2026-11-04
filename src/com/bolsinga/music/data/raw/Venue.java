package com.bolsinga.music.data.raw;

import java.io.*;
import java.util.*;

public class Venue implements com.bolsinga.music.data.Venue {
  // name -> Venue
  private static final HashMap<String, Venue> sMap = new HashMap<String, Venue>();

  // Optional field, may follow any of the other optional fields.
  private static final String MAPKIT_TAG = "mapkit=";

  private String fID;
  private String fName;
  private Location fLocation;
  private String fComment;
  private String fSortname;
  private String fMapKitID;

  static List<Venue> create(final String filename) throws com.bolsinga.web.WebException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
      String s = null;
      StringTokenizer st = null;
      String id, name, city, state, address, url, sortname, mapKitID, token;
      try {
        while ((s = in.readLine()) != null) {
          st = new StringTokenizer(s, "*");

          id = st.nextToken();
          name = st.nextToken();
          city = st.nextToken();
          state = st.nextToken();
          
          address = null;
          url = null;
          sortname = null;
          mapKitID = null;

          while (st.hasMoreElements()) {
            token = st.nextToken();
            if (token.startsWith(MAPKIT_TAG)) {
              mapKitID = token.substring(MAPKIT_TAG.length());
            } else if (address == null) {
              address = token;
            } else if (url == null) {
              url = token;
            } else if (sortname == null) {
              sortname = token;
            }
          }

          Venue v = new Venue(id, name, Location.create(address, city, state, url), sortname, mapKitID);

          sMap.put(name, v);
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read venuemap file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
	  } catch (UnsupportedEncodingException e)  {
      StringBuilder sb = new StringBuilder();
      sb.append("Unsupported Encoding: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }

    ArrayList<Venue> venues = new ArrayList<Venue>(sMap.values());
    java.util.Collections.sort(venues, com.bolsinga.music.Compare.VENUE_COMPARATOR);

    HashMap<String, Venue> idMap = new HashMap<String, Venue>();
    for (Venue v : venues) {
      String id = v.getID();
      if (idMap.get(id) != null) {
        StringBuilder sb = new StringBuilder();
        sb.append("Duplicate Venue ID: ");
        sb.append(id);
        throw new com.bolsinga.web.WebException(sb.toString());
      }
      idMap.put(id, v);
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
  
  private Venue(final String id, final String name, final Location location, final String sortname, final String mapKitID) {
    fID = id;
    fName = name;
    fLocation = location;
    fSortname = sortname;
    fMapKitID = mapKitID;
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }

  public String getName() {
    return fName;
  }
  
  public void setName(final String name) {
    fName = name;
  }
  
  public Location getLocation() {
    return fLocation;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
  }

  public String getSortname() {
    return fSortname;
  }

  public void setSortname(final String name) {
    fSortname = name;
  }

  public String getMapKitID() {
    return fMapKitID;
  }

  public void setMapKitID(final String mapKitID) {
    fMapKitID = mapKitID;
  }
}
