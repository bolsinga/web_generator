package com.bolsinga.music;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.xml.impl.*;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import javax.xml.bind.*;

/*
 * http://www.imc.org/pdi/vcal-10.txt
 */

public class ICal {
  public static void generate(final Music music, final String outputDir) throws WebException {
    if (Util.getSettings().isRedirect()) {
      return;
    }
    
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
    List<Show> items = Util.getShowsCopy(music);
    Collections.sort(items, Compare.SHOW_COMPARATOR);
                    
    VCalendar cal = new VCalendar(name);

    for (Show item : items) {
      boolean unknown = Util.convert(item.getDate().isUnknown());
      if (!unknown) {
        addItem(item, cal);
      }
    }
                
    cal.output(w);
  }
        
  private static void addItem(final Show show, final VCalendar calendar) {
    com.bolsinga.music.data.xml.impl.Date d = show.getDate();
    Calendar date = Calendar.getInstance(); // UTC isn't required
    date.set(Calendar.MONTH, d.getMonth().intValue() - 1);
    date.set(Calendar.DAY_OF_MONTH, d.getDay().intValue());
                
    StringBuilder summary = new StringBuilder();

    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist a = (Artist)bi.next().getValue();
                        
      summary.append(a.getName());
                        
      if (bi.hasNext()) {
        summary.append(", ");
      }
    }
                
    summary.append(" @ ");
                
    summary.append(((Venue)show.getVenue()).getName());
                
    summary.append(" (");
    summary.append(d.getYear());
    summary.append(")");
                
    calendar.add(new VEvent(date, summary.toString(), show.getId()));
  }
}
