package com.bolsinga.music;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.xml.*;
import com.bolsinga.settings.data.*;

import com.bolsinga.web.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.*;

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

    try {
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
        
        music = MySQLCreator.createMusic(user, password);
      } else {
        ICal.usage();
      }

      Util.createSettings(settings);
        
      ICal.generate(music, output);
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void usage() {
    System.out.println("Usage: ICal xml [source.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: ICal db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }

  private static void generate(final String sourceFile, final String outputDir) throws WebException {
    Music music = Util.createMusic(sourceFile);
                
    generate(music, outputDir);
  }
        
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
      os = new FileOutputStream(f);
    } catch (FileNotFoundException e) {
      sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(f.toString());
      throw new WebException(sb.toString(), e);
    }

    Writer w = null;
    try {
      w = new OutputStreamWriter(os, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      sb = new StringBuilder();
      sb.append("Can't handle encoding UTF-8: ");
      sb.append(os.toString());
      throw new WebException(sb.toString(), e);
    }
    
    try {
      generate(music, name, w);
    } catch (IOException e) {
      sb = new StringBuilder();
      sb.append("Can't write: ");
      sb.append(name);
      throw new WebException(sb.toString(), e);
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
    com.bolsinga.music.data.xml.Date d = show.getDate();
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
