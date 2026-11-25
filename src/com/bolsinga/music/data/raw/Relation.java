package com.bolsinga.music.data.raw;

import java.io.*;
import java.util.*;

public class Relation implements com.bolsinga.music.data.Relation {
  private String fID;
  private String fReason;
  private List<Object> fMembers;
  
  static List<Relation> create(final String filename) throws com.bolsinga.web.WebException {
    ArrayList<Relation> relations = new ArrayList<Relation>();
    int index = 0;
    // Every member must be a known artist or venue, as the JSON reader requires.
    List<String> unknown = new ArrayList<String>();
    
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
      String s = null;
      StringTokenizer st = null;
      try {
        while ((s = in.readLine()) != null) {
          st = new StringTokenizer(s, "|");
          
          String type = st.nextToken();
          String reason = st.nextToken();
          if (reason.equals("^")) {
            reason = null;
          }
          List<Object> members = new ArrayList<Object>();
          
          if (type.equals("band")) {
              while (st.hasMoreElements()) {
                String member = st.nextToken();
                Artist a = Artist.get(member);
                if (a != null) {
                  members.add(a);
                } else {
                  unknown.add(Relation.describe("artist", reason, member));
                }
              }
          } else if (type.equals("venue")) {
              while (st.hasMoreElements()) {
                String member = st.nextToken();
                Venue v = Venue.find(member);
                if (v != null) {
                  members.add(v);
                } else {
                  unknown.add(Relation.describe("venue", reason, member));
                }
              }
          } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown relation type: ");
            sb.append(type);
            throw new com.bolsinga.web.WebException(sb.toString());
          }
          
          Relation r = new Relation("r" + index++, reason, members);
          relations.add(r);
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read relations file: ");
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

    if (!unknown.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append(filename);
      sb.append(" names ");
      sb.append(unknown.size());
      sb.append(" unknown member(s):");
      for (String u : unknown) {
        sb.append("\n  ");
        sb.append(u);
      }
      throw new com.bolsinga.web.WebException(sb.toString());
    }

    return relations;
  }

  private static String describe(final String kind, final String reason, final String member) {
    StringBuilder sb = new StringBuilder();
    sb.append(kind);
    sb.append(" ");
    sb.append(member);
    if (reason != null) {
      sb.append(" (");
      sb.append(reason);
      sb.append(")");
    }
    return sb.toString();
  }
  
  private Relation(final String id, final String reason, final List<Object> members) {
    fID = id;
    fReason = reason;
    fMembers = members;
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
  
  public String getReason() {
    return fReason;
  }
  
  public void setReason(final String reason) {
    fReason = reason;
  }
  
  public List<Object> getMembers() {
    return Collections.unmodifiableList(fMembers);
  }
}