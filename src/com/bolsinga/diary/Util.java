package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.sql.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class Util {
  private static int sStartYear = 0;
  
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

  public static final Comparator<Entry> ENTRY_COMPARATOR = new Comparator<Entry>() {
      public int compare(final Entry e1, final Entry e2) {
        int comparison = e1.getTimestamp().compare(e2.getTimestamp());
        return (comparison == DatatypeConstants.LESSER) ? -1 : 1;
      }
    };

  public static String getTitle(final Entry entry) {
    return sWebFormat.get().format(entry.getTimestamp().toGregorianCalendar().getTime());
  }
        
  public static String getMonth(final Entry entry) {
    return sMonthFormat.get().format(entry.getTimestamp().toGregorianCalendar().getTime());
  }

  public static int getStartYear(final Diary diary) {
    synchronized (Util.class) {
      if (sStartYear == 0) {
        List<Entry> items = Util.getEntriesCopy(diary);
        Entry item = null;

        Collections.sort(items, Util.ENTRY_COMPARATOR);

        item = items.get(0);

        sStartYear = item.getTimestamp().getYear();
      }
    }
    return sStartYear;
  }
  
  public static List<Entry> getEntriesUnmodifiable(final Diary diary) {
    return Collections.unmodifiableList(diary.getEntry());
  }

  public static List<Entry> getEntriesCopy(final Diary diary) {
    return new ArrayList<Entry>(diary.getEntry());
  }
    
  public static com.bolsinga.diary.data.Diary createDiary(final String sourceFile) {
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

  private static void createEntries(final Statement stmt, final Diary diary, final ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    try {
      Entry entry = null;

      rset = stmt.executeQuery("SELECT * FROM entry;");
      while (rset.next()) {
        entry = objFactory.createEntry();
        
        String sqlDATETIME = rset.getString("timestamp");
        GregorianCalendar utcCal = com.bolsinga.sql.Util.toCalendarUTC(sqlDATETIME);
        entry.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(utcCal));
        entry.setComment(rset.getString("comment"));
        entry.setId("e" + (rset.getLong("id") - 1));
        
        diary.getEntry().add(entry);  // Modification required.
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private static void createHeaders(final Statement stmt, final Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      StringBuilder data = new StringBuilder();
      
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

  private static void createSides(final Statement stmt, final Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      StringBuilder data = new StringBuilder();
      
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

  private static void createFriends(final Statement stmt, final Diary diary) throws SQLException {
    ResultSet rset = null;
    try {
      StringBuilder data = new StringBuilder();
      
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
  
  private static void createTitle(final Statement stmt, final Diary diary) throws SQLException {
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

  public static com.bolsinga.diary.data.Diary createDiary(final String user, final String password) {
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
      diary.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(com.bolsinga.web.Util.nowUTC()));
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
