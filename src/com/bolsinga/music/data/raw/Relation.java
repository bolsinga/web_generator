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
    
    BufferedReader in = null;
    try {
      try {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
  	  } catch (UnsupportedEncodingException e)  {
        StringBuilder sb = new StringBuilder();
        sb.append("Unsupported Encoding: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
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
                  StringBuilder sb = new StringBuilder();
                  sb.append("Unknown artist relation: ");
                  sb.append(reason);
                  sb.append(" -> ");
                  sb.append(member);
                  System.err.println(sb.toString());
                }
              }
          } else if (type.equals("venue")) {
              while (st.hasMoreElements()) {
                String member = st.nextToken();
                Venue v = Venue.get(member);
                if (v != null) {
                  members.add(v);
                } else {
                  StringBuilder sb = new StringBuilder();
                  sb.append("Unknown venue relation: ");
                  sb.append(reason);
                  sb.append(" -> ");
                  sb.append(member);
                  System.err.println(sb.toString());
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
                
    return relations;
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