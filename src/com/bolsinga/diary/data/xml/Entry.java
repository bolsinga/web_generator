package com.bolsinga.diary.data.xml;

import java.util.*;

public class Entry implements com.bolsinga.diary.data.Entry {
  private final com.bolsinga.diary.data.xml.impl.Entry fEntry;

  public static Entry create(final com.bolsinga.diary.data.xml.impl.Entry entry) {
    return new Entry(entry);
  }
  
  private Entry(final com.bolsinga.diary.data.xml.impl.Entry entry) {
    fEntry = entry;
  }
  
  public String getComment() {
    return fEntry.getComment();
  }
  
  public void setComment(final String comment) {
    fEntry.setComment(comment);
  }
  
  public GregorianCalendar getTimestamp() {
    return fEntry.getTimestamp().toGregorianCalendar();
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    fEntry.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(timestamp));
  }
  
  public String getID() {
    return fEntry.getId();
  }
  
  public void setID(final String id) {
    fEntry.setId(id);
  }
}
