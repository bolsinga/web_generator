package com.bolsinga.music.data.raw;

public class Label implements com.bolsinga.music.data.Label {
  private String fID;
  private String fName;
  private Location fLocation;
  private String fComment;
  
  static Label create(final String id, final String name, final Location location, final String comment) {
    return new Label(id, name, location, comment);
  }
  
  private Label(final String id, final String name, final Location location, final String comment) {
    fID = id;
    fName = name;
    fLocation = location;
    fComment = comment; 
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