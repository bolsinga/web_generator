package com.bolsinga.diary.data.json;

import java.util.*;

import org.json.*;

public class Entry implements com.bolsinga.diary.data.Entry {
  private static final String ID        = "id";
  private static final String TIMESTAMP = "timestamp";
  private static final String COMMENT   = "comment";
  
  private final JSONObject fJSON;

  static JSONObject export(final com.bolsinga.diary.data.Entry srcEntry) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, srcEntry.getID());
    Diary.setCalendar(json, TIMESTAMP, srcEntry.getTimestamp());
    json.put(COMMENT, srcEntry.getComment());
      
    return json;
  }
  
  private Entry(final JSONObject json) {
    fJSON = json;
  }
  
  JSONObject getJSON() {
    return fJSON;
  }
  
  public String getComment() {
    try {
      return fJSON.getString(COMMENT);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
    
  public void setComment(final String comment) {
    try {
      fJSON.put(COMMENT, comment);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  public GregorianCalendar getTimestamp() {
    return Diary.getCalendar(fJSON, TIMESTAMP);
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    Diary.setCalendar(fJSON, TIMESTAMP, timestamp);
  }
  
  public String getID() {
    try {
      return fJSON.getString(ID);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
  
  public void setID(final String id) {
    try {
      fJSON.put(ID, id);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
}
