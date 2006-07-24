package com.bolsinga.music;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.*;

import com.bolsinga.music.data.*;

public class Util {

  private static final DateFormat sMonthFormat      = new SimpleDateFormat("MMMM");
  public  static final DateFormat sWebFormat        = new SimpleDateFormat("M/d/yyyy");
  private static final DecimalFormat sPercentFormat = new DecimalFormat("##.##");
        
  public static GregorianCalendar toCalendarUTC(com.bolsinga.music.data.Date date) {
    Calendar localTime = Calendar.getInstance(); // LocalTime OK
    boolean unknown = com.bolsinga.web.Util.convert(date.isUnknown());
    if (!unknown) {
      // Set shows to 9 PM local time.
      localTime.clear();
      localTime.set(date.getYear().intValue(), date.getMonth().intValue() - 1, date.getDay().intValue(), 12 + 9, 0);
    } else {
      System.err.println("Can't convert Unknown com.bolsinga.music.data.Date");
      System.exit(1);
    }
    // Convert to UTC
    GregorianCalendar result = com.bolsinga.web.Util.nowUTC();
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }

  public static String toString(com.bolsinga.music.data.Date date) {
    boolean unknown = com.bolsinga.web.Util.convert(date.isUnknown());
    if (!unknown) {
      return sWebFormat.format(Util.toCalendarUTC(date).getTime());
    } else {
      Object[] args = {   ((date.getMonth() != null) ? date.getMonth() : BigInteger.ZERO),
                          ((date.getDay() != null) ? date.getDay() : BigInteger.ZERO),
                          ((date.getYear() != null) ? date.getYear() : BigInteger.ZERO) };
      return MessageFormat.format(com.bolsinga.web.Util.getResourceString("unknowndate"), args);
    }
  }
        
  public static String toMonth(com.bolsinga.music.data.Date date) {
    boolean unknown = com.bolsinga.web.Util.convert(date.isUnknown());
    if (!unknown) {
      return sMonthFormat.format(Util.toCalendarUTC(date).getTime());
    } else {
      Calendar d = Calendar.getInstance(); // UTC isn't relevant here.
      if (date.getMonth() != null) {
        d.set(Calendar.MONTH, date.getMonth().intValue() - 1);
        return sMonthFormat.format(d.getTime());
      } else {
        return com.bolsinga.web.Util.getResourceString("unknownmonth");
      }
    }
  }
        
  public static String toString(double value) {
    return sPercentFormat.format(value);
  }

  public static Music createMusic(String sourceFile) {
    Music music = null;
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
      Unmarshaller u = jc.createUnmarshaller();
                        
      music = (Music)u.unmarshal(new java.io.FileInputStream(sourceFile));
    } catch (Exception ume) {
      System.err.println("Exception: " + ume);
      ume.printStackTrace();
      System.exit(1);
    }
    return music;
  }

  public static Music createMusic(String user, String password) {
    Music music = null;
    Connection conn = null;

    ObjectFactory objFactory = new ObjectFactory();
    try {
      Class.forName("org.gjt.mm.mysql.Driver");

      conn = DriverManager.getConnection("jdbc:mysql:///music?useUnicode=true&characterEncoding=utf-8", user, password);

      MySQLCreator c = new MySQLCreator(objFactory, conn);
      music = c.createMusic();
      c.close();
    } catch (Exception e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        System.err.println("Exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }
    return music;
  }
        
  public static int trackCount(Artist artist) {
    int tracks = 0;
    List<JAXBElement<Object>> albums = artist.getAlbum();
    if (albums != null) {
      for (JAXBElement<Object> jalbum : albums) {
        Album album = (Album)jalbum.getValue();
        for (JAXBElement<Object> jsong : album.getSong()) {
          Song song = (Song)jsong.getValue();
          if (song.getPerformer().equals(artist)) {
            tracks++;
          }
        }
      }
    }
                
    return tracks;
  }
}
