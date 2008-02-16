package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Relation implements com.bolsinga.music.data.Relation {
  private String id;
  private String reason;
  private List<Object> members;
  
  private Relation() {
  
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public String getReason() {
    return reason;
  }
  
  public void setReason(final String reason) {
    this.reason = reason;
  }
  
  public List<Object> getMembers() {
    return members;
  }
}
