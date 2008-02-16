package com.bolsinga.music.data.json;

import org.json.*;

public class Label implements com.bolsinga.music.data.Label {
  private String id;
  private String name;
  private Location location;
  private String comment;
  
  private Label() {
  
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(final String name) {
    this.name = name;
  }
  
  public Location getLocation() {
    return location;
  }
  
  public String getComment() {
    return comment;
  }
  
  public void setComment(final String comment) {
    this.comment = comment;
  }
}
