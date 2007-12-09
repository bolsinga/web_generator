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
  private String fID;

  // The *? construct is a reluctant quantifier. This means it is not greedy, and matches the first it can.
  private static final Pattern sCommentPattern = Pattern.compile("<comment>(.*?)</comment>", Pattern.DOTALL);
  private static final Pattern sDatePattern    = Pattern.compile("<date>(.*?)</date>", Pattern.DOTALL);

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

    int diaryTime = com.bolsinga.web.Util.getSettings().getDiaryEntryTime().intValue();
    localTime.clear();
    localTime.set(year, month - 1, day, diaryTime, 0);

    // Convert to UTC.
    GregorianCalendar result = com.bolsinga.web.Util.nowUTC();
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }

  static List<com.bolsinga.diary.data.Entry> create(final String filename) throws com.bolsinga.web.WebException {
    TreeMap<GregorianCalendar, String> comments = new TreeMap<GregorianCalendar, String>();

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
      
      Matcher commentMatcher = sCommentPattern.matcher(cb);
      if (commentMatcher.find()) {
        do {
          String entry = commentMatcher.group(1);
          Matcher dateMatcher = sDatePattern.matcher(entry);
          if (dateMatcher.find()) {
            Matcher dataMatcher = Diary.sDataPattern.matcher(entry);
            if (dataMatcher.find()) {
              comments.put(Entry.toCalendarUTC(dateMatcher.group(1)), dataMatcher.group(1));
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

    ArrayList<com.bolsinga.diary.data.Entry> entries = new ArrayList<com.bolsinga.diary.data.Entry>(comments.keySet().size());

    int index = 0;
    for (Map.Entry<GregorianCalendar, String> entry : comments.entrySet()) {
      entries.add(new Entry(entry.getKey(), entry.getValue(), "e" + index++));
    }

    java.util.Collections.sort(entries, com.bolsinga.web.Util.ENTRY_COMPARATOR);
    
    return entries;
  }
  
  private Entry(final GregorianCalendar date, final String comment, final String id) {
    fDate = date;
    fComment = comment;
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
  
  public String getID() {
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
}
