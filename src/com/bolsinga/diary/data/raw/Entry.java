package com.bolsinga.diary.data.raw;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

public class Entry implements com.bolsinga.diary.data.Entry {

  private ZonedDateTime fDate;
  private String fComment;
  private String fTitle;
  private String fID;

  // The *? construct is a reluctant quantifier. This means it is not greedy, and matches the first it can.
  private static final Pattern sCommentPattern = Pattern.compile("<comment>(.*?)</comment>", Pattern.DOTALL);
  private static final Pattern sDatePattern    = Pattern.compile("<date>(.*?)</date>", Pattern.DOTALL);
  private static final Pattern sTitlePattern   = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);

  private static ZonedDateTime toZDT(final String date) {
    String monthString, dayString, yearString = null;
    int month, day, year = 0;
                
    StringTokenizer st = new StringTokenizer(date, "-");
                
    monthString = st.nextToken();
    dayString = st.nextToken();
    yearString = st.nextToken();
                
    month = Integer.parseInt(monthString);
    day = Integer.parseInt(dayString);
    year = Integer.parseInt(yearString);

    int diaryTime = com.bolsinga.web.Util.getSettings().getDiaryEntryTime();

    return ZonedDateTime.of(year, month, day, diaryTime, 0, 0, 0, ZoneId.systemDefault());
  }

  static List<Entry> create(final String filename) throws com.bolsinga.web.WebException {
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
        cb = Charset.forName("UTF-8").newDecoder().decode(bb);
      } catch (java.nio.charset.CharacterCodingException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bad Encoding UTF-8: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      TreeMap<ZonedDateTime, Entry> entries = new TreeMap<ZonedDateTime, Entry>();
      
      Matcher commentMatcher = sCommentPattern.matcher(cb);
      if (commentMatcher.find()) {
        do {
          String entry = commentMatcher.group(1);
          Matcher dateMatcher = sDatePattern.matcher(entry);
          if (dateMatcher.find()) {
            ZonedDateTime date = Entry.toZDT(dateMatcher.group(1));
            Matcher titleMatcher = sTitlePattern.matcher(entry);
            String title = null;
            if (titleMatcher.find()) {
              title = titleMatcher.group(1);
            }
            Matcher dataMatcher = Diary.sDataPattern.matcher(entry);
            if (dataMatcher.find()) {
              while (entries.containsKey(date)) {
                // Decrementing minute for same date entry
                date = date.minusMinutes(1);
              }
              entries.put(date, new Entry(date, dataMatcher.group(1), title, null));
            } else {
              throw new com.bolsinga.web.WebException("ConvertError: No diary data: " + entry);
            }
          } else {
            throw new com.bolsinga.web.WebException("ConvertError: No diary date: " + entry);
          }
        } while (commentMatcher.find());
      } else {
        throw new com.bolsinga.web.WebException("ConvertError: No comment: " + cb);
      }
      
      ArrayList<Entry> entryList = new ArrayList<Entry>(entries.values());
      
      int index = 0;
      for (Entry e: entryList) {
        e.setID("e" + index++);
      }
      
      return entryList;
    }
    } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("file issues: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }
  
  private Entry(final ZonedDateTime date, final String comment, final String title, final String id) {
    fDate = date;
    fComment = comment;
    fTitle = title;
    fID = id;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public void setComment(final String comment) {
    fComment = comment;
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
  
  public String getID() {
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
}
