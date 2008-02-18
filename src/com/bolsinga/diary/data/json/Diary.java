package com.bolsinga.diary.data.json;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.text.*;
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
  private String statics;
  private String header;
  private String friends;
  private String colophon;
  private List<Entry> entries;
  
  public static Diary create(final String sourceFile) throws com.bolsinga.web.WebException {
    FileChannel fc = null;
    try {
      try {
        fc = new FileInputStream(new File(sourceFile)).getChannel();
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }

      ByteBuffer bb = null;
      try {
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't map: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      CharBuffer cb = null;
      try {
        cb = Charset.forName("UTF-8").newDecoder().decode(bb);
      } catch (java.nio.charset.CharacterCodingException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bad Encoding UTF-8: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      StringBuilder sbjson = new StringBuilder(cb);
      JSONObject json = null;
      try {
        json = new JSONObject(sbjson.toString());
      } catch (JSONException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cannot instantiate JSONObject: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      try {
        return new Diary(json);
      } catch (JSONException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cannot create Diary from JSON: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (fc != null) {
        try {
          fc.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(sourceFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
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
        if (com.bolsinga.web.Util.getPrettyOutput()) {
          fw.write(json.toString(2));
        } else {
          json.write(fw);
        }
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

  static JSONObject createJSON(final com.bolsinga.diary.data.Diary diary) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(TIMESTAMP, com.bolsinga.web.Util.toJSONCalendar(diary.getTimestamp()));
    json.put(TITLE, diary.getTitle());
    json.put(STATICS, diary.getStatic());
    json.put(HEADER, diary.getHeader());
    json.put(FRIENDS, diary.getFriends());
    json.put(COLOPHON, diary.getColophon());
    
    JSONObject entries = new JSONObject();
    for (final com.bolsinga.diary.data.Entry e : diary.getEntries()) {
      entries.put(e.getID(), Entry.createJSON(e));
    }
    json.put(ENTRIES, entries);
    
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
    setTimestamp(com.bolsinga.web.Util.fromJSONCalendar(json.getString(TIMESTAMP)));
    title = json.getString(TITLE);
    statics = json.getString(STATICS);
    header = json.getString(HEADER);
    friends = json.getString(FRIENDS);
    colophon = json.getString(COLOPHON);
    
    JSONObject jsonEntries = json.getJSONObject(ENTRIES);
    entries = new ArrayList<Entry>(jsonEntries.length());
    Iterator i = jsonEntries.keys();
    while (i.hasNext()) {
      String key = (String)i.next();
      JSONObject jsonEntry = jsonEntries.getJSONObject(key);
      entries.add(Entry.create(jsonEntry));
    }
  }

  public GregorianCalendar getTimestamp() {
    return com.bolsinga.web.Util.fromJSONCalendar(timestamp);
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    this.timestamp = com.bolsinga.web.Util.toJSONCalendar(timestamp);
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(final String title) {
    this.title = title;
  }
  
  public String getStatic() {
    return statics;
  }
  
  public void setStatic(final String staticData) {
    this.statics = staticData;
  }
  
  public String getHeader() {
    return header;
  }
  
  public void setHeader(final String header) {
    this.header = header;
  }
  
  public String getFriends() {
    return friends;
  }
  
  public void setFriends(final String friends) {
    this.friends = friends;
  }
  
  public String getColophon() {
    return colophon;
  }
  
  public void setColophon(final String colophon) {
    this.colophon = colophon;
  }
  
  public List<Entry> getEntries() {
    return Collections.unmodifiableList(entries);
  }
  
  public List<Entry> getEntriesCopy() {
    return new ArrayList<Entry>(entries);
  }
}
