package com.bolsinga.diary.data.json;

import java.time.*;
import java.util.*;

import org.json.*;

public class Entry implements com.bolsinga.diary.data.Entry {
  private static final String ID = "id";
  private static final String TIMESTAMP = "timestamp";
  private static final String COMMENT = "comment";
  private static final String TITLE = "title";
  
  private String id;
  private String timestamp;
  private String comment;
  private String title;
  
  static Entry create(final com.bolsinga.diary.data.Entry entry) {
    return new Entry(entry);
  }
  
  static Entry create(final JSONObject json) throws JSONException {
    return new Entry(json);
  }
  
  static JSONObject createJSON(final com.bolsinga.diary.data.Entry entry) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, entry.getID());
    json.put(TIMESTAMP, com.bolsinga.web.Util.zonedDateTimeWithSecondsPrecision(entry.getTimestamp()));
    json.put(COMMENT, entry.getComment());
    json.put(TITLE, entry.getTitle());
    
    return json;
  }
  
  private Entry() {
  }
  
  private Entry(final com.bolsinga.diary.data.Entry entry) {
    setTimestamp(entry.getTimestamp());
    id = entry.getID();
    comment = entry.getComment();
    title = entry.getTitle();
  }
  
  private Entry(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    setTimestamp(ZonedDateTime.parse(json.getString(TIMESTAMP)));
    comment = json.getString(COMMENT);
    title = json.optString(TITLE, null);
  }
  
  public String getComment() {
    return comment;
  }
    
  public void setComment(final String comment) {
    this.comment = comment;
  }
  
  public ZonedDateTime getTimestamp() {
    return ZonedDateTime.parse(timestamp);
  }
  
  public void setTimestamp(final ZonedDateTime timestamp) {
    this.timestamp = timestamp.toString();
  }

  public String getTitle() {
    return title;
  }
  
  public void setTitle(final String title) {
    this.title = title;
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
}
