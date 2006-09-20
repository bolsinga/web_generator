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
      w.write("BEGIN:VEVENT");
      w.write("\r\n");

      writeAllDay(w);
                        
      w.write("UID:");
      w.write(fUID);
      w.write("\r\n");
                        
      w.write("SUMMARY:");
      w.write(fSummary);
      w.write("\r\n");
                        
      if (fURL != null) {
        w.write("URL;VALUE=URI:");
        w.write(fURL);
        w.write("\r\n");
      }

      w.write("RRULE:FREQ=YEARLY;INTERVAL=1");
      w.write("\r\n");

      w.write("END:VEVENT");
      w.write("\r\n");

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

    w.write("DTSTART;VALUE=DATE:");
    w.write(sFormatter.get().format(fDate.getTime()));
    w.write("\r\n");
                
    w.write("DTEND;VALUE=DATE:");
    w.write(sFormatter.get().format(end.getTime()));
    w.write("\r\n");
  }
}
