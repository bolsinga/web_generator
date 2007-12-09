package com.bolsinga.web;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import org.json.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

public class Util {

  private static final ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.web.web");

  private static DatatypeFactory sXMLDatatypeFactory = null;
  private static com.bolsinga.settings.data.Settings sSettings = null;
  private static final boolean sDebugOutput = Boolean.getBoolean("web.debug_output");
  private static final boolean sPrettyOutput = Boolean.parseBoolean(System.getProperty("web.pretty_output", Boolean.valueOf(sDebugOutput).toString()));
  private static final String sLineSeparator = System.getProperty("line.separator");
  private static final Pattern sHTMLPattern = Pattern.compile("&([^agl])");
  private static final Pattern sNewLinePattern = Pattern.compile("\\n");
  private static String sNewLineReplacement = null;

  private static int sDiaryStartYear = 0;

  private static final ThreadLocal<DateFormat> sWebFormat   = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("M/d/yyyy");
    }
  };
  private static final ThreadLocal<DateFormat> sMonthFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("MMMM");
    }
  };
  private static final ThreadLocal<DateFormat> sShortMonthFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("MMM");
    }
  };
  private static final ThreadLocal<DecimalFormat> sPercentFormat = new ThreadLocal<DecimalFormat>() {
    public DecimalFormat initialValue() {
      return new DecimalFormat("##.##");
    }
  };

  public static final Comparator<Entry> ENTRY_COMPARATOR = new Comparator<Entry>() {
      public int compare(final Entry e1, final Entry e2) {
        return e1.getTimestamp().compareTo(e2.getTimestamp());
      }
    };
  
  public static final Comparator<Calendar> MONTH_COMPARATOR = new Comparator<Calendar>() {
    public int compare(final Calendar c1, final Calendar c2) {
      int m1 = c1.get(Calendar.MONTH);
      int m2 = c2.get(Calendar.MONTH);
      return (m1 - m2);
    }
  };
  
  static {
    try {
      sXMLDatatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new Error(e);
    }
    
    StringBuilder sb = new StringBuilder();
    Util.appendPretty(sb);
    sb.append(new P());
    sNewLineReplacement = sb.toString();
  }
  
  public static StringBuilder appendPretty(StringBuilder sb) {
    if (Util.getPrettyOutput()) {
      sb.append(sLineSeparator);
    }
    return sb;
  }

  public static String toHTMLSafe(final String s) {
    return sHTMLPattern.matcher(s).replaceAll("&amp;$1");
  }

  public static boolean getPrettyOutput() {
    return sPrettyOutput;
  }
  
  public static boolean getDebugOutput() {
    return sDebugOutput;
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(final GregorianCalendar cal) {
    return sXMLDatatypeFactory.newXMLGregorianCalendar(cal);
  }

  public static GregorianCalendar nowUTC() {
    return new GregorianCalendar(TimeZone.getTimeZone("UTC"));
  }
  
  public static String createPageTitle(final String specialty, final String type) {
    Object[] args = { specialty, type };
    return MessageFormat.format(Util.getResourceString("htmltitle"), args);
  }

  public static Link getIconLink() {
    Link result = new Link();
    result.setRel("SHORTCUT ICON");
    result.setHref(Util.getSettings().getIco());
    return result;
  }
        
  public static IMG getLogo() {
    com.bolsinga.settings.data.Image image = Util.getSettings().getLogoImage();

    IMG i = new IMG(image.getLocation());
    i.setHeight(image.getHeight().intValue());
    i.setWidth(image.getWidth().intValue());
    i.setAlt(image.getAlt());
    i.setTitle(image.getAlt());
        
    return i;
  }
        
  public static UL convertToUnOrderedList(final String data) {
    UL list = new UL();
    list.setPrettyPrint(Util.getPrettyOutput());
                
    // Convert each line to a li tag.
    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      LI item = new LI(lines[i]);
      item.setPrettyPrint(Util.getPrettyOutput());
      list.addElement(item);
    }
                
    return list;
  }
        
  public static org.apache.ecs.Element convertToParagraphs(final String data) {
    // Convert each line to <p> tags except when within a tag...
    ElementContainer ec = new ElementContainer();
    if (data != null) {
      ec.addElement(new P().addElement(Encode.encodeUntagged(data, new UntaggedEncoder() {
        public String encodeUntagged(final String s) {
          return sNewLinePattern.matcher(s).replaceAll(sNewLineReplacement);
        }
      })));
    }
    return ec;
  }
        
  public static Div createDiv(final String className) {
    Div d = new Div();
    d.setClass(className);
    d.setPrettyPrint(Util.getPrettyOutput());
    return d;
  }
        
  public static A createInternalA(final String url, final String value) {
    return Util.createInternalA(url, value, null);
  }
        
  public static A createInternalA(final String url, final String value, final String title) {
    return internalA(url, value, title, CSS.INTERNAL);
  }
  
  public static A createPermaLink(final String url) {
    return internalA(url, Util.getResourceString("link"), Util.getResourceString("linktitle"), CSS.PERMANENT);
  }
  
  private static A internalA(final String url, final String value, final String title, final String cssClass) {
    A an = new A(url, value);
    an.setClass(cssClass);
    if (title != null) {
      an.setTitle(title);
    }
    return an;
  }
    
  public static A createNamedTarget(final String name, final String value) {
    A an = new A();
    an.setName(name);
    an.addElement("t", value);
    return an;
  }

  public static UL createUnorderedList(final Vector<org.apache.ecs.Element> elements) {
    return Util.createUnorderedList(elements, null);
  }

  public static UL createUnorderedList(final Vector<org.apache.ecs.Element> elements, final org.apache.ecs.Element curElement) {
    UL list = new UL();
    list.setPrettyPrint(Util.getPrettyOutput());

    for (org.apache.ecs.Element e : elements) {
      LI item = new LI(e);
      item.setPrettyPrint(Util.getPrettyOutput());
      if (e.equals(curElement)) {
        item.setClass(CSS.ACTIVE);
      }
      list.addElement(item);
    }

    return list;
  }
  
  public static UL appendToUnorderedList(final UL list, final Vector<org.apache.ecs.Element> elements) {
    for (org.apache.ecs.Element e : elements) {
      LI item = new LI(e);
      item.setPrettyPrint(Util.getPrettyOutput());
      list.addElement(item);
    }

    return list;
  }

  public static OL createOrderedList(final Vector<org.apache.ecs.Element> elements) {
    OL list = new OL();
    list.setPrettyPrint(Util.getPrettyOutput());

    for (org.apache.ecs.Element e : elements) {
      LI item = new LI(e);
      item.setPrettyPrint(Util.getPrettyOutput());
      list.addElement(item);
    }

    return list;
  }
  
  public static Table makeTable(final String caption, final String summary, final TableHandler handler) {
    Table t = new Table();
    t.setSummary(summary);
    t.setPrettyPrint(Util.getPrettyOutput());
    Caption capt = new Caption();
    capt.setPrettyPrint(Util.getPrettyOutput());
    capt.addElement(caption);
    t.addElement(capt);
    
    TR trow = handler.getHeaderRow();
    trow.setClass(CSS.TABLE_HEADER);
    trow.setPrettyPrint(Util.getPrettyOutput());
    t.addElement(trow);

    int i = 0;
    int count = handler.getRowCount();
    while (i < count) {
      trow = handler.getRow(i);
      
      trow.setPrettyPrint(Util.getPrettyOutput());
      if (((i + 1) % 2) == 0) {
        trow.setClass(CSS.TABLE_ROW_ALT);
      }

      t.addElement(trow);

      i++;
    }
    
    trow = handler.getFooterRow();
    trow.setPrettyPrint(Util.getPrettyOutput());
    trow.setClass(CSS.TABLE_FOOTER);
    t.addElement(trow);
                
    return t;
  }
  
  public static org.apache.ecs.Element addCurrentIndexNavigator(final java.util.Map<String, IndexPair> m, final String curLetter, final org.apache.ecs.Element parentElement) {
    ElementContainer ec = new ElementContainer();
    ec.addElement(parentElement);
    if (m != null) {
      Vector<org.apache.ecs.Element> e = new Vector<org.apache.ecs.Element>();
      org.apache.ecs.Element curElement = null;
      for (String s : m.keySet()) {
        if (s.equals(curLetter)) {
          curElement = new StringElement(s);
          e.add(curElement);
        } else {
          IndexPair p = m.get(s);
          e.add(Util.createInternalA(p.getLink(), s, p.getTitle()));
        }
      }

      Div d = Util.createDiv(CSS.ENTRY_INDEX_SUB);
      d.addElement(Util.createUnorderedList(e, curElement));

      ec.addElement(d);
    }
    return ec;
  }

  public static String getGenerator() {
    StringBuilder sb = new StringBuilder();

    if (Util.getDebugOutput()) {
      sb.append("generator information");
    } else {
      sb.append(Util.getResourceString("program"));
                  
      sb.append(" (built: ");
      sb.append(Util.getResourceString("builddate"));
      sb.append(" running ");
      sb.append(System.getProperty("java.runtime.name"));
      sb.append(" (");
      sb.append(System.getProperty("java.runtime.version"));
      sb.append(") ");
      sb.append(System.getProperty("java.vm.name"));
      sb.append(" (");
      sb.append(System.getProperty("java.vm.version"));
      sb.append(") - ");
      sb.append(System.getProperty("java.vm.vendor"));
      sb.append(" ");
      sb.append(System.getProperty("os.name"));
      sb.append(" ");
      sb.append(System.getProperty("os.version"));
      sb.append(" ");
      sb.append(System.getProperty("os.arch"));
                  
      sb.append(" [");
      sb.append(Util.getResourceString("copyright"));
      sb.append("]");
                  
      sb.append(")");
    }
                
    return sb.toString();
  }

  public static String getCopyright(final int startYear) {
    StringBuilder cp = new StringBuilder();
    
    if (Util.getDebugOutput()) {
      cp.append("copyright year");
    } else {
      int cur_year = Calendar.getInstance().get(Calendar.YEAR); // LocalTime OK
                  
      cp.append("Contents Copyright (c) ");
      cp.append(startYear);
      if (startYear != cur_year) {
        cp.append(" - ");
        cp.append(cur_year);
      }
                  
      cp.append(" ");
      cp.append(System.getProperty("user.name"));
    }
                
    return cp.toString();
  }
 
  public static List<Object> getRecentItems(final int count, final Music music, final Diary diary) {
    List<Show> shows = music.getShowsCopy();
    Collections.sort(shows, com.bolsinga.music.Compare.SHOW_COMPARATOR);
    Collections.reverse(shows);

    List<Entry> entries = diary.getEntriesCopy();
    Collections.sort(entries, Util.ENTRY_COMPARATOR);
    Collections.reverse(entries);
    
    List<Object> items = new Vector<Object>(count * 2);
    items.addAll(shows.subList(0, count));
    items.addAll(entries.subList(0, count));
    
    Collections.sort(items, CHANNEL_ITEM_COMPARATOR);
    Collections.reverse(items);
    
    return Collections.unmodifiableList(items.subList(0, count));
  }

  public static final Comparator<Object> CHANNEL_ITEM_COMPARATOR = new Comparator<Object>() {
      public int compare(final Object o1, final Object o2) {
        Calendar c1 = null;
        Calendar c2 = null;
                        
        if (o1 instanceof Show) {
          c1 = Util.toCalendarUTC(((Show)o1).getDate());
        } else if (o1 instanceof Entry) {
          c1 = ((Entry)o1).getTimestamp();
        } else {
          System.err.println("Unknown " + getClass().getName() + ": " + o1.getClass().getName());
        }

        if (o2 instanceof Show) {
          c2 = Util.toCalendarUTC(((Show)o2).getDate());
        } else if (o2 instanceof Entry) {
          c2 = ((Entry)o2).getTimestamp();
        } else {
          System.err.println("Unknown " + getClass().getName() + ": " + o2.getClass().getName());
        }
        
        return c1.getTime().compareTo(c2.getTime());
      }
    };
        
  public synchronized static void createSettings(final String sourceFile) throws WebException {
    if (sSettings == null) {
      InputStream is = null;
      try {
        try {
          is = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Can't find settings file: ");
          sb.append(sourceFile);
          throw new WebException(sb.toString(), e);
        }
        
        try {
          JAXBContext jc = JAXBContext.newInstance("com.bolsinga.settings.data");
          Unmarshaller u = jc.createUnmarshaller();
                  
          sSettings = (com.bolsinga.settings.data.Settings)u.unmarshal(is);
        } catch (JAXBException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Can't unmarsal settings file: ");
          sb.append(sourceFile);
          throw new WebException(sb.toString(), e);
        }
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to close settings file: ");
            sb.append(sourceFile);
            throw new WebException(sb.toString(), e);
          }
        }
      }
    }
  }

  public synchronized static com.bolsinga.settings.data.Settings getSettings() {
    return sSettings;
  }
    
  public static String getResourceString(final String key) {
    return sResource.getString(key); 
  }

  public static String getTitle(final Entry entry) {
    return sWebFormat.get().format(entry.getTimestamp().getTime());
  }
  
  public static String getMonth(final Calendar month) {
    return sMonthFormat.get().format(month.getTime());
  }
  
  public static String getMonth(final Entry entry) {
    return Util.getMonth(entry.getTimestamp());
  }
  
  public static String getShortMonthName(final Calendar month) {
    return sShortMonthFormat.get().format(month.getTime());
  }

  public static int getStartYear(final Diary diary) {
    synchronized (Util.class) {
      if (sDiaryStartYear == 0) {
        List<Entry> items = diary.getEntriesCopy();
        Entry item = null;

        Collections.sort(items, Util.ENTRY_COMPARATOR);

        item = items.get(0);

        sDiaryStartYear = item.getTimestamp().get(Calendar.YEAR);
      }
    }
    return sDiaryStartYear;
  }

  private static void dumpJSON(final String sourceFile) throws WebException {
    File src = new File(sourceFile);
    StringBuilder xmlsb = new StringBuilder();

    BufferedReader br = null;
    try {
      try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(sourceFile);
        throw new WebException(sb.toString(), e);
      }
    
      String line;
      try {
        while ((line = br.readLine()) != null) {
          xmlsb.append(line);
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read file: ");
        sb.append(sourceFile);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close file: ");
          sb.append(sourceFile);
          throw new WebException(sb.toString(), e);
        }
      }
    }

    JSONObject json;
    try {
      json = XML.toJSONObject(xmlsb.toString());
    } catch (JSONException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't convert xml file to JSON: ");
      sb.append(sourceFile);
      throw new WebException(sb.toString(), e);
    }

    String name = new StringBuilder(sourceFile).append(".json").toString();
    File f = new File(name);

    try {
      PrintWriter pw = null;
      try {
        pw = new PrintWriter(f);
        pw.print(json.toString());
      } finally {
        if (pw != null) {
          pw.close();
        }
      }
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("The file: ");
      sb.append(f);
      sb.append(" could not be written.");
      System.err.println(sb.toString());
      System.err.println(e);
      e.printStackTrace();
    }
  }
      
  public static GregorianCalendar toCalendarLocal(final com.bolsinga.music.data.Date date) {
    GregorianCalendar localTime = new GregorianCalendar(); // LocalTime OK
    if (!date.isUnknown()) {
      int showTime = Util.getSettings().getShowTime().intValue();
      localTime.clear();
      localTime.set(date.getYear(), date.getMonth() - 1, date.getDay(), showTime, 0);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't convert unknown date to GregorianCalendar: ");
      sb.append(date.toString());
      throw new Error(sb.toString());
    }
    return localTime;
  }

  public static GregorianCalendar toCalendarUTC(final com.bolsinga.music.data.Date date) {
    Calendar localTime = Util.toCalendarLocal(date);
    // Convert to UTC
    GregorianCalendar result = Util.nowUTC();
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }

  public static String toString(final com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sWebFormat.get().format(Util.toCalendarUTC(date).getTime());
    } else {
      Object[] args = {   ((date.getMonth() != com.bolsinga.music.data.Date.UNKNOWN) ? BigInteger.valueOf(date.getMonth()) : BigInteger.ZERO),
                          ((date.getDay() != com.bolsinga.music.data.Date.UNKNOWN) ? BigInteger.valueOf(date.getDay()) : BigInteger.ZERO),
                          ((date.getYear() != com.bolsinga.music.data.Date.UNKNOWN) ? BigInteger.valueOf(date.getYear()) : BigInteger.ZERO) };
      return MessageFormat.format(Util.getResourceString("unknowndate"), args);
    }
  }
        
  public static String toMonth(final com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sMonthFormat.get().format(Util.toCalendarUTC(date).getTime());
    } else {
      Calendar d = Calendar.getInstance(); // UTC isn't relevant here.
      if (date.getMonth() != com.bolsinga.music.data.Date.UNKNOWN) {
        d.set(Calendar.MONTH, date.getMonth() - 1);
        return sMonthFormat.get().format(d.getTime());
      } else {
        return Util.getResourceString("unknownmonth");
      }
    }
  }
        
  public static String toString(final double value) {
    return sPercentFormat.get().format(value);
  }
  
  public static String createTitle(final String resource, final String name) {
    Object[] args = { Util.toHTMLSafe(name) };
    return MessageFormat.format(Util.getResourceString(resource), args);
  }

  public static int trackCount(final Artist artist) {
    int tracks = 0;
    List<Album> albums = artist.getAlbums();
    if (albums != null) {
      for (Album album : albums) {
        List<Song> songs = album.getSongs();
        for (Song song : songs) {
          if (song.getPerformer().equals(artist)) {
            tracks++;
          }
        }
      }
    }
                
    return tracks;
  }
  
  public static Div getisForwardMessage(final String filePath) {
      Div d = Util.createDiv(CSS.FORWARD);
      
      StringBuilder sb = new StringBuilder();
      sb.append(Util.getSettings().getRoot());
      sb.append("/");
      sb.append(filePath);
      
      String link = sb.toString();
      d.addElement(new StringElement(MessageFormat.format(Util.getResourceString("isForward"), new A(link, link).toString())));
      return d;
  }
}
