package com.bolsinga.diary.data.raw;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Diary implements com.bolsinga.diary.data.Diary {

  private List<com.bolsinga.diary.data.Entry> fEntries = null;
  private GregorianCalendar fDate = null;
  private String fTitle = null;
  private String fStatic = null;
  private String fHeader = null;
  private String fFriends = null;
  private String fColophon = null;
  
  // The *? construct is a reluctant quantifier. This means it is not greedy, and matches the first it can.
  private static final Pattern sStaticPattern   = Pattern.compile("<static>(.*?)</static>", Pattern.DOTALL);
  private static final Pattern sLocationPattern = Pattern.compile("<location>(.*?)</location>", Pattern.DOTALL);
  public  static final Pattern sDataPattern     = Pattern.compile("<data>(.*?)</data>", Pattern.DOTALL);

  public static Diary create(final String commentsFile, final String staticsFile) throws com.bolsinga.web.WebException {
    Diary diary = new Diary(Entry.create(commentsFile));
    diary.addStatics(staticsFile);
    return diary;
  }
    
  private Diary(final List<com.bolsinga.diary.data.Entry> entries) {
    fEntries = entries;
    fDate = com.bolsinga.web.Util.nowUTC();
  }

  private void setStatic(final String location, final String data) throws com.bolsinga.web.WebException {
    if (location.equalsIgnoreCase("left")) {
      setStatic(data);
    } else if (location.equalsIgnoreCase("header")) {
      setHeader(data);
    } else if (location.equalsIgnoreCase("links")) {
      setFriends(data);
    } else if (location.equalsIgnoreCase("title")) {
      setTitle(data);
    } else if (location.equalsIgnoreCase("colophon")) {
      setColophon(data);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Unknown statics location: ");
      sb.append(location);
      throw new com.bolsinga.web.WebException(sb.toString());
    }
  }
  
  private void addStatics(final String filename) throws com.bolsinga.web.WebException {
    FileChannel fc = null;
    try {
      try {
        fc = new FileInputStream(new File(filename)).getChannel();
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }

      ByteBuffer bb = null;
      try {
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't map: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      CharBuffer cb = null;
      try {
        cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
      } catch (java.nio.charset.CharacterCodingException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bad Encoding US-ASCII: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      Matcher staticMatcher = sStaticPattern.matcher(cb);
      if (staticMatcher.find()) {
        do {
          String entry = staticMatcher.group(1);
          Matcher locationMatcher = sLocationPattern.matcher(entry);
          if (locationMatcher.find()) {
            Matcher dataMatcher = Diary.sDataPattern.matcher(entry);
            if (dataMatcher.find()) {
              setStatic(locationMatcher.group(1), dataMatcher.group(1));
            } else {
              throw new com.bolsinga.web.WebException("Statics Error: No Data: " + entry);
            }
          } else {
            throw new com.bolsinga.web.WebException("Statics Error: No Location: " + entry);
          }
        } while (staticMatcher.find());
      } else {
        throw new com.bolsinga.web.WebException("Statics Error: No statics: " + cb);
      }
    } finally {
      if (fc != null) {
        try {
          fc.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(filename);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
  }
  
  public GregorianCalendar getTimestamp() {
    return fDate;
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    fDate = timestamp;
  }
  
  public String getTitle() {
    return fTitle;
  }
  
  public void setTitle(final String title) {
    fTitle = title;
  }
  
  public String getStatic() {
    return fStatic;
  }
  
  public void setStatic(final String staticData) {
    fStatic = staticData;
  }
  
  public String getHeader() {
    return fHeader;
  }
  
  public void setHeader(final String header) {
    fHeader = header;
  }
  
  public String getFriends() {
    return fFriends;
  }
  
  public void setFriends(final String friends) {
    fFriends = friends;
  }
  
  public String getColophon() {
    return fColophon;
  }
  
  public void setColophon(final String colophon) {
    fColophon = colophon;
  }
  
  public List<? extends com.bolsinga.diary.data.Entry> getEntries() {
    return Collections.unmodifiableList(fEntries);
  }

  public List<? extends com.bolsinga.diary.data.Entry> getEntriesCopy() {
    return new ArrayList<com.bolsinga.diary.data.Entry>(fEntries);
  }
}