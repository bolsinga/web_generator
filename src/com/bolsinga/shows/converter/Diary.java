package com.bolsinga.shows.converter;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.diary.data.xml.*;

public class Diary {
        
  public static void convert(final String commentsFile, final String staticsFile, final String outputFile) throws ConvertException {
    List<Comments> comments = Convert.comments(commentsFile);
    List<Statics> statics = Convert.statics(staticsFile);

    ObjectFactory objFactory = new ObjectFactory();
                
    com.bolsinga.diary.data.xml.Diary diary = objFactory.createDiary();

    createStatics(diary, statics);

    createComments(objFactory, diary, comments);

    diary.setTimestamp(Util.toXMLGregorianCalendar(Util.nowUTC()));

    OutputStream os = null;
    try {
      try {
        os = new FileOutputStream(outputFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(outputFile);
        throw new ConvertException(sb.toString(), e);
      }
      
      try {
        // Write out to the output file.
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data.xml");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(diary, os);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't marshall: ");
        sb.append(outputFile);
        throw new ConvertException(sb.toString(), e);
      }
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(outputFile);
          throw new ConvertException(sb.toString(), e);
        }
      }
    }
  }
        
  private static void createStatics(final com.bolsinga.diary.data.xml.Diary diary, final List<Statics> statics) throws ConvertException {
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
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown statics location: ");
        sb.append(location);
        throw new ConvertException(sb.toString());
      }
    }
  }

  private static void createComments(final ObjectFactory objFactory, final com.bolsinga.diary.data.xml.Diary diary, final List<Comments> comments) {
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
