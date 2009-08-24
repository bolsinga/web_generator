package com.bolsinga.music;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

/*
 * http://www.imc.org/pdi/vcal-10.txt
 */

public class ICal {
  public static void generate(final Music music, final String outputDir) throws WebException {
    String name = Util.getSettings().getIcalName();
                
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(Links.ALT_DIR);
    File f = new File(sb.toString(), name + ".ics");
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.err.println("ICal cannot mkdirs: " + parent.getAbsolutePath());
      }
    }
    
    OutputStream os = null;
    try {
      try {
        os = new FileOutputStream(f);
      } catch (FileNotFoundException e) {
        sb = new StringBuilder();
        sb.append("Can't find ical file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }

      Writer w = null;
      try {
        w = new OutputStreamWriter(os, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        sb = new StringBuilder();
        sb.append("Can't handle encoding UTF-8: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }
      
      try {
        generate(music, name, w);
      } catch (IOException e) {
        sb = new StringBuilder();
        sb.append("Can't write ical file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          sb = new StringBuilder();
          sb.append("Unable to close ical file: ");
          sb.append(f);
          throw new WebException(sb.toString(), e);
        }
      }
    }
  }
        
  private static void generate(final Music music, final String name, final Writer w) throws IOException {                
    List<? extends Show> items = music.getShowsCopy();
    Collections.sort(items, Compare.SHOW_COMPARATOR);
                    
    VCalendar cal = new VCalendar(name);

    for (Show item : items) {
      if (!item.getDate().isUnknown()) {
        addItem(item, cal);
      }
    }
                
    cal.output(w);
  }
        
  private static void addItem(final Show show, final VCalendar calendar) {
    com.bolsinga.music.data.Date d = show.getDate();
    Calendar date = Calendar.getInstance(); // UTC isn't required
    date.set(Calendar.MONTH, d.getMonth() - 1);
    date.set(Calendar.DAY_OF_MONTH, d.getDay());
                
    StringBuilder summary = new StringBuilder();

    Iterator<? extends Artist> bi = show.getArtists().iterator();
    while (bi.hasNext()) {
      Artist a = bi.next();
                        
      summary.append(a.getName());
                        
      if (bi.hasNext()) {
        summary.append(", ");
      }
    }
                
    summary.append(" @ ");
                
    summary.append(show.getVenue().getName());
                
    summary.append(" (");
    summary.append(d.getYear());
    summary.append(")");
                
    calendar.add(new VEvent(date, summary.toString(), show.getID()));
  }
}
