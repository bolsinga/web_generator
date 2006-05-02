package com.bolsinga.music;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.bolsinga.music.data.*;

public class Util {

  private static DateFormat sMonthFormat      = new SimpleDateFormat("MMMM");
  public  static DateFormat sWebFormat        = new SimpleDateFormat("M/d/yyyy");
  private static DecimalFormat sPercentFormat = new DecimalFormat("##.##");
        
  public static Calendar toCalendar(com.bolsinga.music.data.Date date) {
    Calendar localTime = Calendar.getInstance();
    if (!date.isUnknown()) {
      // Set shows to 9 PM local time.
      localTime.clear();
      localTime.set(date.getYear().intValue(), date.getMonth().intValue() - 1, date.getDay().intValue(), 12 + 9, 0);
    } else {
      System.err.println("Can't convert Unknown com.bolsinga.music.data.Date");
      System.exit(1);
    }
    // Convert to UTC
    Calendar result = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }

  public static String toString(com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sWebFormat.format(toCalendar(date).getTime());
    } else {
      Object[] args = {   ((date.getMonth() != null) ? date.getMonth() : BigInteger.ZERO),
                          ((date.getDay() != null) ? date.getDay() : BigInteger.ZERO),
                          ((date.getYear() != null) ? date.getYear() : BigInteger.ZERO) };
      return MessageFormat.format(com.bolsinga.web.Util.getResourceString("unknowndate"), args);
    }
  }
        
  public static String toMonth(com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sMonthFormat.format(toCalendar(date).getTime());
    } else {
      Calendar d = Calendar.getInstance();
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
    Statement stmt = null;

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
        if (stmt != null) {
          stmt.close();
        }
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
    List albums = artist.getAlbum();
    if (albums != null) {
      ListIterator i = albums.listIterator();
      while (i.hasNext()) {
        Album album = (Album)i.next();
        List songs = album.getSong();
        ListIterator si = songs.listIterator();
        while (si.hasNext()) {
          Song song = (Song)si.next();
          if (song.getPerformer().equals(artist)) {
            tracks++;
          }
        }
      }
    }
                
    return tracks;
  }
}
