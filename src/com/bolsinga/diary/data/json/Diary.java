package com.bolsinga.diary.data.json;

import java.io.*;
import java.text.*;
import java.util.*;

import org.json.*;

public class Diary implements com.bolsinga.diary.data.Diary {
  private static final String TIMESTAMP = "timestamp";
  private static final String TITLE     = "title";
  private static final String STATIC    = "static";
  private static final String HEADER    = "header";
  private static final String FRIENDS   = "friends";
  private static final String COLOPHON  = "colophon";
  private static final String ENTRIES   = "entries";

  private static final ThreadLocal<DateFormat> sJSONTimeFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      DateFormat result = DateFormat.getDateInstance();
      result.setTimeZone(TimeZone.getTimeZone("UTC"));
      return result;
    }
  };
  
  private final JSONObject fJSON;
  
  public static void export(final com.bolsinga.diary.data.Diary diary, final String outputFile) throws com.bolsinga.web.WebException {
    JSONObject json = null;
    if (diary instanceof com.bolsinga.diary.data.json.Diary) {
      json = ((com.bolsinga.diary.data.json.Diary)diary).fJSON;
    } else {
      try {
        json = Diary.export(diary);
      } catch (JSONException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't convert diary: ");
        sb.append(diary.getTitle());
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    }
    
    FileWriter fw = null;
    try {
      try {
        fw = new FileWriter(outputFile);
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      try {
        fw.write(json.toString(2));
      } catch (Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't write file: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(outputFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
  }
  
  static JSONObject export(final com.bolsinga.diary.data.Diary diary) throws JSONException {
    JSONObject json = new JSONObject();

    Diary.setCalendar(json, TIMESTAMP, diary.getTimestamp());
    json.put(TITLE, diary.getTitle());
    json.put(STATIC, diary.getStatic());
    json.put(HEADER, diary.getHeader());
    json.put(FRIENDS, diary.getFriends());
    json.put(COLOPHON, diary.getColophon());
    
    JSONArray array = new JSONArray();
    for (com.bolsinga.diary.data.Entry entry : diary.getEntries()) {
      array.put(Entry.export(entry));
    }
    json.put(ENTRIES, array);
    
    return json;
  }
  
  static GregorianCalendar getCalendar(final JSONObject json, final String key) {
    String ts = null;
    try {
      ts = json.getString(key);
    } catch (JSONException e) {
      System.err.println(e);
      ts = null;
    }
    java.util.Date d = null;
    try {
      d = sJSONTimeFormat.get().parse(ts);
    } catch (ParseException e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    }
    GregorianCalendar c = new GregorianCalendar(sJSONTimeFormat.get().getTimeZone());
    c.setTime(d);
    return c;
  }
  
  static void setCalendar(final JSONObject json, final String key, final GregorianCalendar cal) {
    try {
      json.put(key, sJSONTimeFormat.get().format(cal.getTime()));
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  private Diary(final JSONObject json) {
    fJSON = json;
  }
  
  public GregorianCalendar getTimestamp() {
    return Diary.getCalendar(fJSON, TIMESTAMP);
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    Diary.setCalendar(fJSON, TIMESTAMP, timestamp);
  }
  
  public String getTitle() {
    try {
      return fJSON.getString(TITLE);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
  
  public void setTitle(final String title) {
    try {
      fJSON.put(TITLE, title);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  public String getStatic() {
    try {
      return fJSON.getString(STATIC);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
  
  public void setStatic(final String staticData) {
    try {
      fJSON.put(STATIC, staticData);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  public String getHeader() {
    try {
      return fJSON.getString(HEADER);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
  
  public void setHeader(final String header) {
    try {
      fJSON.put(HEADER, header);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  public String getFriends() {
    try {
      return fJSON.getString(FRIENDS);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
  
  public void setFriends(final String friends) {
    try {
      fJSON.put(FRIENDS, friends);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  public String getColophon() {
    try {
      return fJSON.getString(COLOPHON);
    } catch (JSONException e) {
      System.err.println(e);
      return null;
    }
  }
  
  public void setColophon(final String colophon) {
    try {
      fJSON.put(COLOPHON, colophon);
    } catch (JSONException e) {
      System.err.println(e);
    }
  }
  
  public List<? extends Entry> getEntries() {
    return null;
  }
  
  public List<? extends Entry> getEntriesCopy() {
    return null;
  }
}
