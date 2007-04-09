package com.bolsinga.shows.converter;

import java.util.*;

public class Relation {

  private final String fType;
  private final String fReason;
  private final Collection<String> fMembers;
        
  public Relation(String type, String reason) {
    fType = type;
    fReason = reason;
    fMembers = new Vector<String>();
  }

  public String getType() {
    return fType;
  }
        
  public String getReason() {
    return fReason;
  }
        
  public Collection<String> getMembers() {
    return fMembers;
  }
        
  public String toString() {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getClass().getName());
    sb.append(" [");
    sb.append(getType());
    sb.append(", ");
    sb.append(getReason());
    sb.append(" (");

    for (String s : getMembers()) {
      sb.append(s);
      sb.append(", ");
    }
                
    sb.append(")]");
                
    return sb.toString();
  }
}
