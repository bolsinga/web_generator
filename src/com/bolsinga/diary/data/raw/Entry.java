package com.bolsinga.diary.data.raw;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Entry implements com.bolsinga.diary.data.Entry {

  private GregorianCalendar fDate;
  private String fComment;
  private String fTitle;
  private String fID;

  // The *? construct is a reluctant quantifier. This means it is not greedy, and matches the first it can.
  private static final Pattern sCommentPattern = Pattern.compile("<comment>(.*?)</comment>", Pattern.DOTALL);
  private static final Pattern sDatePattern    = Pattern.compile("<date>(.*?)</date>", Pattern.DOTALL);
  private static final Pattern sTitlePattern   = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);

  private static GregorianCalendar toCalendarUTC(final String date) {
    Calendar localTime = Calendar.getInstance(); // LocalTime OK
                
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
    localTime.clear();
    localTime.set(year, month - 1, day, diaryTime, 0);

    // Convert to UTC.
    GregorianCalendar result = com.bolsinga.web.Util.nowUTC();
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }

  static List<Entry> create(final String filename) throws com.bolsinga.web.WebException {
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
      
      TreeMap<GregorianCalendar, Entry> entries = new TreeMap<GregorianCalendar, Entry>();
      
      Matcher commentMatcher = sCommentPattern.matcher(cb);
      if (commentMatcher.find()) {
        do {
          String entry = commentMatcher.group(1);
          Matcher dateMatcher = sDatePattern.matcher(entry);
          if (dateMatcher.find()) {
            GregorianCalendar date = Entry.toCalendarUTC(dateMatcher.group(1));
            Matcher titleMatcher = sTitlePattern.matcher(entry);
            String title = null;
            if (titleMatcher.find()) {
              title = titleMatcher.group(1);
            }
            Matcher dataMatcher = Diary.sDataPattern.matcher(entry);
            if (dataMatcher.find()) {
              while (entries.containsKey(date)) {
                // Decrementing minute for same date entry
                date.add(Calendar.MINUTE, -1);
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
  
  private Entry(final GregorianCalendar date, final String comment, final String title, final String id) {
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
  
  public String getID() {
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
}
