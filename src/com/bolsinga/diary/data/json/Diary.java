package com.bolsinga.diary.data.json;

import java.io.*;
import java.text.*;
import java.util.*;

import org.json.*;
import com.twolattes.json.*;

@Entity
public class Diary implements com.bolsinga.diary.data.Diary {
  @Value
  private String timestamp;
  @Value
  private String title;
  @Value
  private String statics;
  @Value
  private String header;
  @Value
  private String friends;
  @Value
  private String colophon;
  @Value
  private List<Entry> entries;
  
  public static void export(final com.bolsinga.diary.data.Diary diary, final String outputFile) throws com.bolsinga.web.WebException {    
    com.bolsinga.diary.data.json.Diary jsonDiary = null;
    if (diary instanceof com.bolsinga.diary.data.json.Diary) {
      jsonDiary = (com.bolsinga.diary.data.json.Diary)diary;
    } else {
      jsonDiary = new Diary(diary);
    }

    JSONObject json = null;
    Marshaller<com.bolsinga.diary.data.json.Diary> m = Marshaller.create(com.bolsinga.diary.data.json.Diary.class);
    try {
      json = m.marshall(jsonDiary);
    } catch (JSONException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't convert diary: ");
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
    
    entries = new ArrayList<Entry>(srcEntries.size());
    for (com.bolsinga.diary.data.Entry entry : srcEntries) {
      entries.add(Entry.create(entry));
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
