package com.bolsinga.diary.data.json;

import java.util.*;

import org.json.*;
import com.twolattes.json.*;

@Entity
public class Entry implements com.bolsinga.diary.data.Entry {
  @Value
  private String id;
  @Value
  private String timestamp;
  @Value
  private String comment;
  
  public static Entry create(com.bolsinga.diary.data.Entry entry) {
    return new Entry(entry);
  }
  
  private Entry() {
  }
  
  private Entry(final com.bolsinga.diary.data.Entry entry) {
    setTimestamp(entry.getTimestamp());
    id = entry.getID();
    comment = entry.getComment();
  }
  
  public String getComment() {
    return comment;
  }
    
  public void setComment(final String comment) {
    this.comment = comment;
  }
  
  public GregorianCalendar getTimestamp() {
    return Diary.getCalendar(timestamp);
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    this.timestamp = Diary.setCalendar(timestamp);
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
}
