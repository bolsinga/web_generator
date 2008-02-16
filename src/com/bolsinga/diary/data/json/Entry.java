package com.bolsinga.diary.data.json;

import java.util.*;

import org.json.*;

public class Entry implements com.bolsinga.diary.data.Entry {
  private String id;
  private String timestamp;
  private String comment;
  
  public static Entry create(com.bolsinga.diary.data.Entry entry) {
    return new Entry(entry);
  }

  static JSONObject createJSON(final com.bolsinga.diary.data.Entry entry) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("id", entry.getID());
    json.put("timestamp", com.bolsinga.web.Util.toJSONCalendar(entry.getTimestamp()));
    json.put("comment", entry.getComment());
    
    return json;
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
    return com.bolsinga.web.Util.fromJSONCalendar(timestamp);
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    this.timestamp = com.bolsinga.web.Util.toJSONCalendar(timestamp);
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
}
