package com.bolsinga.music;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

/*
 * http://www.imc.org/pdi/vcal-10.txt
 */

public class ICal {

  public static void main(String[] args) {
    if ((args.length != 4) && (args.length != 5)) {
      ICal.usage();
    }

    String type = args[0];

    String settings = null;
    String output = null;

    Music music = null;

    if (type.equals("xml")) {
      if (args.length != 4) {
        ICal.usage();
      }
      
      String musicFile = args[1];
      settings = args[2];
      output = args[3];

      music = Util.createMusic(musicFile);
    } else if (type.equals("db")) {
      if (args.length != 5) {
        ICal.usage();
      }

      String user = args[1];
      String password = args[2];
      settings = args[3];
      output = args[4];
      
      music = com.bolsinga.music.Util.createMusic(user, password);
    } else {
      ICal.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);
        
    ICal.generate(music, output);
  }

  private static void usage() {
    System.out.println("Usage: ICal xml [source.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: ICal db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }

  public static void generate(String sourceFile, String outputDir) {
    Music music = Util.createMusic(sourceFile);
                
    generate(music, outputDir);
  }
        
  public static void generate(Music music, String outputDir) {
    OutputStreamWriter w = null;

    String name = com.bolsinga.web.Util.getSettings().getIcalName();
                
    try {
      File f = new File(outputDir, "ical/" + name + ".ics");
      File parent = new File(f.getParent());
      if (!parent.exists()) {
        if (!parent.mkdirs()) {
          System.out.println("Can't: " + parent.getAbsolutePath());
        }
      }
      w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
                
    generate(music, name, w);
  }
        
  public static void generate(Music music, String name, Writer w) {
    List items = music.getShow();
    Show item = null;
                
    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);
                
    VCalendar cal = new VCalendar(name);
                
    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Show)i.next();
                        
      if (!item.getDate().isUnknown()) {
        addItem(item, cal);
      }
    }
                
    cal.output(w);
  }
        
  public static void addItem(Show show, VCalendar calendar) {
    com.bolsinga.music.data.Date d = show.getDate();
    Calendar date = Calendar.getInstance(); // UTC isn't required
    date.set(Calendar.MONTH, d.getMonth().intValue() - 1);
    date.set(Calendar.DAY_OF_MONTH, d.getDay().intValue());
                
    StringBuffer summary = new StringBuffer();
    String url = null;
                
    ListIterator bi = show.getArtist().listIterator();
    while (bi.hasNext()) {
      Artist a = (Artist)bi.next();
                        
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
                
    calendar.add(new VEvent(date, summary.toString(), url, show.getId()));
  }
}
