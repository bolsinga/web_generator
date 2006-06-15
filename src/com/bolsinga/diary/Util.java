package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.sql.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {
  public  static final DateFormat sWebFormat   = new SimpleDateFormat("M/d/yyyy");
  private static final DateFormat sMonthFormat = new SimpleDateFormat("MMMM");

  public static final Comparator ENTRY_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Entry e1 = (Entry)o1;
        Entry e2 = (Entry)o2;
                        
        return e1.getTimestamp().before(e2.getTimestamp()) ? -1 : 1;
      }
    };

  public static String getTitle(Entry entry) {
    return sWebFormat.format(entry.getTimestamp().getTime());
  }
        
  public static String getMonth(Entry entry) {
    return sMonthFormat.format(entry.getTimestamp().getTime());
  }

  public static int getStartYear(Diary diary) {
    List items = diary.getEntry();
    Entry item = null;

    Collections.sort(items, Util.ENTRY_COMPARATOR);

    item = (Entry)items.get(0);

    return item.getTimestamp().get(Calendar.YEAR);
  }
    
  public static com.bolsinga.diary.data.Diary createDiary(String sourceFile) {
    com.bolsinga.diary.data.Diary diary = null;
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data");
      Unmarshaller u = jc.createUnmarshaller();
                        
      diary = (com.bolsinga.diary.data.Diary)u.unmarshal(new java.io.FileInputStream(sourceFile));
    } catch (Exception ume) {
      System.err.println("Exception: " + ume);
      ume.printStackTrace();
      System.exit(1);
    }
    return diary;
  }

  private static void createEntries(Statement stmt, Diary diary, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    try {
      Entry entry = null;

      rset = stmt.executeQuery("SELECT * FROM entry;");
      while (rset.next()) {
        entry = objFactory.createEntry();
        
        String sqlDATETIME = rset.getString("timestamp");
        Calendar utcCal = com.bolsinga.sql.Util.toUTCCalendar(sqlDATETIME);
        entry.setTimestamp(utcCal);
        entry.setComment(rset.getString("comment"));
        entry.setId("e" + (rset.getLong("id") - 1));
        
        diary.getEntry().add(entry);
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private static void createHeaders(Statement stmt, Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      StringBuffer data = new StringBuffer();
      
      rset = stmt.executeQuery("SELECT * FROM header;");
      while (rset.next()) {
        data.append(rset.getString("data"));
        data.append("\n");
      }
      
      diary.setHeader(data.toString());
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private static void createSides(Statement stmt, Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      StringBuffer data = new StringBuffer();
      
      rset = stmt.executeQuery("SELECT * FROM side;");
      while (rset.next()) {
        data.append(rset.getString("data"));
        data.append("\n");
      }
      
      diary.setStatic(data.toString());
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private static void createFriends(Statement stmt, Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      StringBuffer data = new StringBuffer();
      
      rset = stmt.executeQuery("SELECT * FROM friend;");
      while (rset.next()) {
        data.append("<a href=\"");
        data.append(rset.getString("url"));
        data.append("\">");
        data.append(rset.getString("display_name"));
        data.append("</a>\n");
      }
      
      diary.setFriends(data.toString());
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }
  
  private static void createTitle(Statement stmt, Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      rset = stmt.executeQuery("SELECT * FROM title;");
      if (rset.first()) {
        diary.setTitle(rset.getString("title"));
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  public static com.bolsinga.diary.data.Diary createDiary(String user, String password) {
    Diary diary = null;
    Connection conn = null;
    Statement stmt = null;

    ObjectFactory objFactory = new ObjectFactory();
    try {
      diary = objFactory.createDiary();

      Class.forName("org.gjt.mm.mysql.Driver");

      conn = DriverManager.getConnection("jdbc:mysql:///diary?useUnicode=true&characterEncoding=utf-8", user, password);
      stmt = conn.createStatement();

      Util.createEntries(stmt, diary, objFactory);

      Util.createHeaders(stmt, diary);

      Util.createSides(stmt, diary);

      Util.createFriends(stmt, diary);

      Util.createTitle(stmt, diary);

      // timestamp
      diary.setTimestamp(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
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
    return diary;
  }
}
