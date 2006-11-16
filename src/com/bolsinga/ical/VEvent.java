package com.bolsinga.ical;

import java.io.*;
import java.text.*;
import java.util.*;

public class VEvent {

  private static final ThreadLocal<DateFormat> sFormatter = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("yyyyMMdd");
    }
  };
        
  private final Calendar fDate;
  private final String fSummary;
  private final String fURL;
  private final String fUID;
        
  public VEvent(final Calendar date, final String summary, final String url, final String uid) {
    fDate = date;
    fSummary = summary;
    fURL = url;
    fUID = uid;
  }

  public void output(final Writer w) {
    try {
      VCalendar.write(w, "BEGIN:VEVENT");

      writeAllDay(w);
      
      StringBuilder sb = new StringBuilder();
      sb.append("UID:");
      sb.append(fUID);
      VCalendar.write(w, sb.toString());
      
      sb = new StringBuilder();
      sb.append("SUMMARY:");
      sb.append(fSummary);
      VCalendar.write(w, sb.toString());
                        
      if (fURL != null) {
        sb = new StringBuilder();
        sb.append("URL;VALUE=URI:");
        sb.append(fURL);
        VCalendar.write(w, sb.toString());
        }

      VCalendar.write(w, "RRULE:FREQ=YEARLY;INTERVAL=1");

      VCalendar.write(w, "END:VEVENT");

      w.flush();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
        
  private void writeAllDay(final Writer w) throws IOException {
    Calendar end = (Calendar)fDate.clone();
    end.add(Calendar.DATE, 1);

    StringBuilder sb = new StringBuilder();
    sb.append("DTSTART;VALUE=DATE:");
    sb.append(sFormatter.get().format(fDate.getTime()));
    VCalendar.write(w, sb.toString());
    
    sb = new StringBuilder();
    sb.append("DTEND;VALUE=DATE:");
    sb.append(sFormatter.get().format(end.getTime()));
    VCalendar.write(w, sb.toString());
  }
}
