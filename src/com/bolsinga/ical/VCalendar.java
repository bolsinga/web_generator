package com.bolsinga.ical;

import java.io.*;
import java.util.*;

public class VCalendar {
  private final Vector<VEvent> fEvents = new Vector<VEvent>();
  private final String fName;
        
  public VCalendar(final String name) {
    fName = name;
  }
        
  public void add(final VEvent event) {
    fEvents.add(event);
  }

  public static void write(Writer w, String s) throws IOException {
    w.write(s);
    w.write("\r\n");
  }
  
  public void output(final Writer w) {
    try {
      VCalendar.write(w, "BEGIN:VCALENDAR");
      VCalendar.write(w, "VERSION:2.0");
      
      StringBuilder sb = new StringBuilder();
      sb.append("X-WR-CALNAME:");
      sb.append(fName);
      VCalendar.write(w, sb.toString());
      VCalendar.write(w, "CALSCALE:GREGORIAN");

      for (VEvent event : fEvents) {
        event.output(w);
      }

      VCalendar.write(w, "END:VCALENDAR");
                        
      w.flush();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
}
