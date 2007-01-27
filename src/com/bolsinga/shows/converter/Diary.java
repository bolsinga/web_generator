package com.bolsinga.shows.converter;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.diary.data.xml.*;

public class Diary {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Diary [comments] [statics] [output]");
      System.exit(0);
    }
                
    Diary.convert(args[0], args[1], args[2]);
  }
        
  public static void convert(final String commentsFile, final String staticsFile, final String outputFile) {
    List<Comments> comments = null;
    List<Statics> statics = null;
                
    try {
      comments = Convert.comments(commentsFile);
                        
      statics = Convert.statics(staticsFile);
    } catch (IOException e) {
      System.err.println(e);
      System.exit(1);
    }

    ObjectFactory objFactory = new ObjectFactory();
                
    try {
      com.bolsinga.diary.data.xml.Diary diary = objFactory.createDiary();

      createStatics(objFactory, diary, statics);

      createComments(objFactory, diary, comments);

      diary.setTimestamp(Util.toXMLGregorianCalendar(Util.nowUTC()));

      // Write out to the output file.
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data.xml");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      OutputStream os = null;
      try {
        os = new FileOutputStream(outputFile);
      } catch (IOException ioe) {
        System.err.println(ioe);
        ioe.printStackTrace();
        System.exit(1);
      }
      m.marshal(diary, os);
                        
    } catch (JAXBException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
        
  private static void createStatics(final ObjectFactory objFactory, final com.bolsinga.diary.data.xml.Diary diary, final List<Statics> statics) throws JAXBException {
    for (Statics oldStatic : statics) {
      String location = oldStatic.getLocation();
                        
      if (location.equalsIgnoreCase("left")) {
        diary.setStatic(oldStatic.getData());
      } else if (location.equalsIgnoreCase("header")) {
        diary.setHeader(oldStatic.getData());
      } else if (location.equalsIgnoreCase("links")) {
        diary.setFriends(oldStatic.getData());
      } else if (location.equalsIgnoreCase("title")) {
        diary.setTitle(oldStatic.getData());
      } else if (location.equalsIgnoreCase("colophon")) {
        diary.setColophon(oldStatic.getData());
      } else {
        System.err.println("Unknown statics location: " + location);
      }
    }
  }

  private static void createComments(final ObjectFactory objFactory, final com.bolsinga.diary.data.xml.Diary diary, final List<Comments> comments) throws JAXBException {
    com.bolsinga.diary.data.xml.Entry xEntry = null;
    int index = comments.size() - 1;

    List<com.bolsinga.diary.data.xml.Entry> entries = diary.getEntry(); // Modification required.
    
    for (Comments oldComment : comments) {
      xEntry = objFactory.createEntry();
      xEntry.setTimestamp(Util.toXMLGregorianCalendar(Diary.toCalendarUTC(oldComment.getDate())));
      xEntry.setComment(oldComment.getData());
      xEntry.setId("e" + index--);
                        
      entries.add(xEntry);
    }

    java.util.Collections.sort(entries, Util.ENTRY_COMPARATOR);
  }
        
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

    int diaryTime = Util.getSettings().getDiaryEntryTime().intValue();
    localTime.clear();
    localTime.set(year, month - 1, day, diaryTime, 0);

    // Convert to UTC.
    GregorianCalendar result = Util.nowUTC();
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }
}
