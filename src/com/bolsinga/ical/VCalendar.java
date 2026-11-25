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

  // RFC 5545 section 3.1: a line is at most 75 octets of UTF-8, not counting its CRLF. A longer line
  // is folded: CRLF and a space, then the rest. A character is never split across lines.
  private static final int MAX_LINE_OCTETS = 75;

  public static void write(Writer w, String s) throws IOException {
    int octets = 0;
    int i = 0;
    while (i < s.length()) {
      int cp = s.codePointAt(i);
      int width = VCalendar.utf8Length(cp);
      if (octets + width > MAX_LINE_OCTETS) {
        w.write("\r\n ");
        octets = 1;
      }
      w.write(Character.toChars(cp));
      octets += width;
      i += Character.charCount(cp);
    }
    w.write("\r\n");
  }

  private static int utf8Length(final int cp) {
    if (cp < 0x80) {
      return 1;
    } else if (cp < 0x800) {
      return 2;
    } else if (cp < 0x10000) {
      return 3;
    }
    return 4;
  }

  // RFC 5545 section 3.3.11: in a TEXT value, backslash, semicolon and comma are escaped with a
  // backslash, and a line break is written as \n.
  public static String escapeText(final String s) {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
        case ';':
        case ',':
          sb.append('\\');
          sb.append(c);
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }
  
  public void output(final Writer w) throws IOException {
    VCalendar.write(w, "BEGIN:VCALENDAR");
    VCalendar.write(w, "VERSION:2.0");
    
    StringBuilder sb = new StringBuilder();
    sb.append("X-WR-CALNAME:");
    sb.append(VCalendar.escapeText(fName));
    VCalendar.write(w, sb.toString());
    VCalendar.write(w, "CALSCALE:GREGORIAN");

    for (VEvent event : fEvents) {
      event.output(w);
    }

    VCalendar.write(w, "END:VCALENDAR");
                      
    w.flush();
  }
}
