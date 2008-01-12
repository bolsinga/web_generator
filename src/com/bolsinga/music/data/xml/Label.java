package com.bolsinga.music.data.xml;

import java.util.*;

public class Label implements com.bolsinga.music.data.Label {
  private static final HashMap<String, Label> sMap = new HashMap<String, Label>();

  private final com.bolsinga.music.data.xml.impl.Label fLabel;
  private final Location fLocation;

  public static Label get(final com.bolsinga.music.data.xml.impl.Label item) {
    synchronized (sMap) {
      Label result = sMap.get(item.getId());
      if (result == null) {
        result = new Label(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Label(final com.bolsinga.music.data.xml.impl.Label label) {
    fLabel = label;
    fLocation = Location.create(fLabel.getLocation());
  }
  
  public String getID() {
    return fLabel.getId();
  }
  
  public void setID(final String id) {
    fLabel.setId(id);
  }
  
  public String getName() {
    return fLabel.getName();
  }
  
  public void setName(final String name) {
    fLabel.setName(name);
  }

  public Location getLocation() {
    return fLocation;
  }
  
  public String getComment() {
    return fLabel.getComment();
  }
  
  public void setComment(final String comment) {
    fLabel.setComment(comment);
  }
}
