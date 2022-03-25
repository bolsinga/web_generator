package com.bolsinga.diary.data.raw;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

public class Diary implements com.bolsinga.diary.data.Diary {

  private List<Entry> fEntries = null;
  private ZonedDateTime fDate = null;
  private String fTitle = null;
  private List<String> fStatic = null;
  private List<String> fHeader = null;
  private List<String> fFriends = null;
  private List<String> fColophon = null;
  
  // The *? construct is a reluctant quantifier. This means it is not greedy, and matches the first it can.
  private static final Pattern sStaticPattern   = Pattern.compile("<static>(.*?)</static>", Pattern.DOTALL);
  private static final Pattern sLocationPattern = Pattern.compile("<location>(.*?)</location>", Pattern.DOTALL);
  public  static final Pattern sDataPattern     = Pattern.compile("<data>(.*?)</data>", Pattern.DOTALL);

  public static Diary create(final String commentsFile, final String staticsFile) throws com.bolsinga.web.WebException {
    Diary diary = new Diary(Entry.create(commentsFile));
    diary.addStatics(staticsFile);
    return diary;
  }
    
  private Diary(final List<Entry> entries) {
    fEntries = entries;
    fDate = ZonedDateTime.now();
  }

  private void setStatic(final String location, final String data) throws com.bolsinga.web.WebException {
    List<String> lines = Arrays.asList(data.split("\\n"));
    if (location.equalsIgnoreCase("left")) {
      setStatic(lines);
    } else if (location.equalsIgnoreCase("header")) {
      setHeader(lines);
    } else if (location.equalsIgnoreCase("links")) {
      setFriends(lines);
    } else if (location.equalsIgnoreCase("title")) {
      setTitle(data);
    } else if (location.equalsIgnoreCase("colophon")) {
      setColophon(lines);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Unknown statics location: ");
      sb.append(location);
      throw new com.bolsinga.web.WebException(sb.toString());
    }
  }
  
  private void addStatics(final String filename) throws com.bolsinga.web.WebException {
    try {
    try (FileChannel fc = new FileInputStream(new File(filename)).getChannel()) {
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
    }
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }
  
  public ZonedDateTime getTimestamp() {
    return fDate;
  }
  
  public void setTimestamp(final ZonedDateTime timestamp) {
    fDate = timestamp;
  }
  
  public String getTitle() {
    return fTitle;
  }
  
  public void setTitle(final String title) {
    fTitle = title;
  }
  
  public List<String> getStatic() {
    return fStatic;
  }
  
  public void setStatic(final List<String> staticData) {
    fStatic = staticData;
  }
  
  public List<String> getHeader() {
    return fHeader;
  }
  
  public void setHeader(final List<String> header) {
    fHeader = header;
  }
  
  public List<String> getFriends() {
    return fFriends;
  }
  
  public void setFriends(final List<String> friends) {
    fFriends = friends;
  }
  
  public List<String> getColophon() {
    return fColophon;
  }
  
  public void setColophon(final List<String> colophon) {
    fColophon = colophon;
  }
  
  public List<Entry> getEntries() {
    return Collections.unmodifiableList(fEntries);
  }

  public List<Entry> getEntriesCopy() {
    return new ArrayList<Entry>(fEntries);
  }
}