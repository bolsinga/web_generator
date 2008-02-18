package com.bolsinga.diary.data.json;

import java.util.*;

import org.json.*;

public class Entry implements com.bolsinga.diary.data.Entry {
  private static final String ID = "id";
  private static final String TIMESTAMP = "timestamp";
  private static final String COMMENT = "comment";
  
  private String id;
  private String timestamp;
  private String comment;
  
  static Entry create(final com.bolsinga.diary.data.Entry entry) {
    return new Entry(entry);
  }
  
  static Entry create(final JSONObject json) throws JSONException {
    return new Entry(json);
  }
  
  static JSONObject createJSON(final com.bolsinga.diary.data.Entry entry) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, entry.getID());
    json.put(TIMESTAMP, com.bolsinga.web.Util.toJSONCalendar(entry.getTimestamp()));
    json.put(COMMENT, entry.getComment());
    
    return json;
  }
  
  private Entry() {
  }
  
  private Entry(final com.bolsinga.diary.data.Entry entry) {
    setTimestamp(entry.getTimestamp());
    id = entry.getID();
    comment = entry.getComment();
  }
  
  private Entry(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    setTimestamp(com.bolsinga.web.Util.fromJSONCalendar(json.getString(TIMESTAMP)));
    comment = json.getString(COMMENT);
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
