package com.bolsinga.web;

import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {

  private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.web.web");

  private static com.bolsinga.settings.data.Settings sSettings = null;
  private static boolean sPrettyPrint = false;
  static {
    String value = System.getProperty("web.pretty_containers");
    if (value != null) {
      sPrettyPrint = true;
    }
  }
        
  public static boolean getPrettyPrint() {
    return sPrettyPrint;
  }
        
  public static link getIconLink() {
    link result = new link();
    result.setRel("SHORTCUT ICON");
    result.setHref(Util.getSettings().getIco());
    return result;
  }
        
  public static img getLogo() {
    com.bolsinga.settings.data.Image image = Util.getSettings().getLogoImage();

    img i = new img(image.getLocation());
    i.setHeight(image.getHeight().intValue());
    i.setWidth(image.getWidth().intValue());
    i.setAlt(image.getAlt());
    i.setTitle(image.getAlt());
        
    return i;
  }
        
  public static ul convertToUnOrderedList(String data) {
    ul list = new ul();
                
    // Convert each line to a li tag.
    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      list.addElement(new li(lines[i]));
    }
                
    return list;
  }
        
  public static String convertToParagraphs(String data) {
    // Convert each line to <p> tags
    StringBuffer tagged = new StringBuffer();
    if (data != null) {
      String[] lines = data.split("\\n");
      for (int i = 0; i < lines.length; i++) {
        tagged.append(new p().addElement(lines[i]));
      }
    }
    return tagged.toString();
  }
        
  public static div createDiv(String className) {
    div d = new div();
    d.setClass(className);
    d.setPrettyPrint(Util.getPrettyPrint());
    return d;
  }
        
  public static a createInternalA(String url, String value) {
    return Util.createInternalA(url, value, null);
  }
        
  public static a createInternalA(String url, String value, String title) {
    a an = new a(url, value);
    an.setClass(CSS.INTERNAL);
    if (title != null) {
      an.setTitle(title);
    }
    return an;
  }
    
  public static a createNamedTarget(String name, String value) {
    a an = new a();
    an.setName(name);
    an.addElement("t", value);
    return an;
  }

  public static ul createUnorderedList(Vector elements) {
    ul list = new ul();
    list.setPrettyPrint(Util.getPrettyPrint());

    Iterator i = elements.iterator();
    while (i.hasNext()) {
      Element e = (Element)i.next();
      li item = new li(e);
      item.setPrettyPrint(Util.getPrettyPrint());
      list.addElement(item);
    }

    return list;
  }

  public static ol createOrderedList(Vector elements) {
    ol list = new ol();
    list.setPrettyPrint(Util.getPrettyPrint());

    Iterator i = elements.iterator();
    while (i.hasNext()) {
      Element e = (Element)i.next();
      li item = new li(e);
      item.setPrettyPrint(Util.getPrettyPrint());
      list.addElement(item);
    }

    return list;
  }

  public static String getGenerator() {
    StringBuffer sb = new StringBuffer();
                
    sb.append(com.bolsinga.web.Util.getResourceString("program"));
                
    sb.append(" (built: ");
    sb.append(com.bolsinga.web.Util.getResourceString("builddate"));
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
    sb.append(com.bolsinga.web.Util.getResourceString("copyright"));
    sb.append("]");
                
    sb.append(")");
                
    return sb.toString();
  }

  public static String getCopyright(int startYear) {
    StringBuffer cp = new StringBuffer();
                
    int cur_year = Calendar.getInstance().get(Calendar.YEAR);
                
    cp.append("Contents Copyright (c) ");
    cp.append(startYear);
    if (startYear != cur_year) {
      cp.append(" - ");
      cp.append(cur_year);
    }
                
    cp.append(" ");
    cp.append(System.getProperty("user.name"));
                
    return cp.toString();
  }

  public static List getRecentItems(int count, com.bolsinga.music.data.Music music, com.bolsinga.diary.data.Diary diary) {
    return Util.getRecentItems(count, music, diary, true);
  }
 
  public static List getRecentItems(int count, com.bolsinga.music.data.Music music, com.bolsinga.diary.data.Diary diary, boolean includeMusic) {
    List shows = null;
    if (includeMusic) {
      shows = music.getShow();
      Collections.sort(shows, com.bolsinga.music.Compare.SHOW_COMPARATOR);
      Collections.reverse(shows);
    }

    List entries = diary.getEntry();
    Collections.sort(entries, com.bolsinga.diary.Util.ENTRY_COMPARATOR);
    Collections.reverse(entries);
    
    Vector items = new Vector(count * 2);
    if (shows != null) {
      items.addAll(shows.subList(0, count));
    }
    items.addAll(entries.subList(0, count));
    
    Collections.sort(items, CHANNEL_ITEM_COMPARATOR);
    Collections.reverse(items);
    
    return items.subList(0, count);
  }

  public static final Comparator CHANNEL_ITEM_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Calendar c1 = null;
        Calendar c2 = null;
                        
        if (o1 instanceof com.bolsinga.music.data.Show) {
          c1 = com.bolsinga.music.Util.toCalendar(((com.bolsinga.music.data.Show)o1).getDate());
        } else if (o1 instanceof com.bolsinga.diary.data.Entry) {
          c1 = ((com.bolsinga.diary.data.Entry)o1).getTimestamp();
        } else {
          System.err.println("Unknown " + getClass().getName() + ": " + o1.getClass().getName());
          System.exit(1);
        }

        if (o2 instanceof com.bolsinga.music.data.Show) {
          c2 = com.bolsinga.music.Util.toCalendar(((com.bolsinga.music.data.Show)o2).getDate());
        } else if (o2 instanceof com.bolsinga.diary.data.Entry) {
          c2 = ((com.bolsinga.diary.data.Entry)o2).getTimestamp();
        } else {
          System.err.println("Unknown " + getClass().getName() + ": " + o1.getClass().getName());
          System.exit(1);
        }
        
        return c1.getTime().compareTo(c2.getTime());
      }
    };
        
  public static void createSettings(String sourceFile) {
    if (sSettings == null) {
      try {
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.settings.data");
        Unmarshaller u = jc.createUnmarshaller();
                
        sSettings = (com.bolsinga.settings.data.Settings)u.unmarshal(new java.io.FileInputStream(sourceFile));
      } catch (Exception ume) {
        System.err.println("Exception: " + ume);
        ume.printStackTrace();
        System.exit(1);
      }
    }
  }

  public static com.bolsinga.settings.data.Settings getSettings() {
    return sSettings;
  }
    
  public static String getResourceString(String key) {
    return sResource.getString(key); 
  }
}
