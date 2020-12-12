package com.bolsinga.diary.data.json;

import java.text.*;
import java.time.*;
import java.util.*;

import org.json.*;

public class Diary implements com.bolsinga.diary.data.Diary {
  private static final String TIMESTAMP = "timestamp";
  private static final String TITLE = "title";
  private static final String STATICS = "statics";
  private static final String HEADER = "header";
  private static final String FRIENDS = "friends";
  private static final String COLOPHON = "colophon";
  private static final String ENTRIES = "entries";

  private String timestamp;
  private String title;
  private List<String> statics;
  private List<String> header;
  private List<String> friends;
  private List<String> colophon;
  private List<Entry> entries;
  
  public static Diary create(final String sourceFile) throws com.bolsinga.web.WebException {
    JSONObject json = com.bolsinga.web.Util.createJSON(sourceFile);
    try {
      return new Diary(json);
    } catch (JSONException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Cannot create Diary from JSON: ");
      sb.append(sourceFile);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }
  
  public static void export(final com.bolsinga.diary.data.Diary diary, final String outputFile) throws com.bolsinga.web.WebException {    
    JSONObject json = null;
    try {
      json = Diary.createJSON(diary);
    } catch (JSONException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't export json: ");
      sb.append(diary.getTitle());
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
    com.bolsinga.web.Util.writeJSON(json, outputFile);
  }

  public static JSONArray createEntriesJSON(final List<? extends com.bolsinga.diary.data.Entry> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.diary.data.Entry item : items) {
      json.put(Entry.createJSON(item));
    }
    return json;
  }
  
  static JSONObject createJSON(final com.bolsinga.diary.data.Diary diary) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(TIMESTAMP, com.bolsinga.web.Util.zonedDateTimeWithSecondsPrecision(diary.getTimestamp()).toString());
    json.put(TITLE, diary.getTitle());
    json.put(STATICS, diary.getStatic());
    json.put(HEADER, diary.getHeader());
    json.put(FRIENDS, diary.getFriends());
    json.put(COLOPHON, diary.getColophon());
    
    json.put(ENTRIES, Diary.createEntriesJSON(diary.getEntries()));
    
    return json;
  }
  
  private Diary() {
  
  }

  private Diary(final com.bolsinga.diary.data.Diary diary) {
    setTimestamp(diary.getTimestamp());
    title = diary.getTitle();
    statics = diary.getStatic();
    header = diary.getHeader();
    friends = diary.getFriends();
    colophon = diary.getColophon();
    
    List<? extends com.bolsinga.diary.data.Entry> srcEntries = diary.getEntries();
    
    List<Entry> entries = new ArrayList<Entry>(srcEntries.size());
    for (com.bolsinga.diary.data.Entry entry : srcEntries) {
      entries.add(Entry.create(entry));
    }
  }
  
  private Diary(final JSONObject json) throws JSONException {
    setTimestamp(ZonedDateTime.parse(json.getString(TIMESTAMP)));
    title = json.getString(TITLE);

    JSONArray jsonArray = json.getJSONArray(STATICS);
    statics = new ArrayList<String>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      statics.add(jsonArray.getString(i));
    }

    jsonArray = json.getJSONArray(HEADER);
    header = new ArrayList<String>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      header.add(jsonArray.getString(i));
    }

    jsonArray = json.getJSONArray(FRIENDS);
    friends = new ArrayList<String>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      friends.add(jsonArray.getString(i));
    }

    jsonArray = json.getJSONArray(COLOPHON);
    colophon = new ArrayList<String>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      colophon.add(jsonArray.getString(i));
    }

    jsonArray = json.getJSONArray(ENTRIES);
    entries = new ArrayList<Entry>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonEntry = jsonArray.getJSONObject(i);
      entries.add(Entry.create(jsonEntry));
    }
  }

  public ZonedDateTime getTimestamp() {
    return ZonedDateTime.parse(this.timestamp);
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
  
  public List<String> getStatic() {
    return statics;
  }
  
  public void setStatic(final List<String> staticData) {
    this.statics = staticData;
  }
  
  public List<String> getHeader() {
    return header;
  }
  
  public void setHeader(final List<String> header) {
    this.header = header;
  }
  
  public List<String> getFriends() {
    return friends;
  }
  
  public void setFriends(final List<String> friends) {
    this.friends = friends;
  }
  
  public List<String> getColophon() {
    return colophon;
  }
  
  public void setColophon(final List<String> colophon) {
    this.colophon = colophon;
  }
  
  public List<Entry> getEntries() {
    return Collections.unmodifiableList(entries);
  }
  
  public List<Entry> getEntriesCopy() {
    return new ArrayList<Entry>(entries);
  }
}
