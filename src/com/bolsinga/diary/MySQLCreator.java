package com.bolsinga.diary;

import com.bolsinga.diary.data.xml.*;
import com.bolsinga.web.*;

import java.sql.*;
import java.util.*;

import javax.xml.bind.*;

public class MySQLCreator {
  private static void createEntries(final Statement stmt, final Diary diary, final ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    try {
      Entry entry = null;

      rset = stmt.executeQuery("SELECT * FROM entry;");
      while (rset.next()) {
        entry = objFactory.createEntry();
        
        String sqlDATETIME = rset.getString("timestamp");
        GregorianCalendar utcCal = com.bolsinga.sql.Util.toCalendarUTC(sqlDATETIME);
        entry.setTimestamp(Util.toXMLGregorianCalendar(utcCal));
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

  public static Diary createDiary(final String user, final String password) {
    Diary diary = null;
    Connection conn = null;
    Statement stmt = null;

    ObjectFactory objFactory = new ObjectFactory();
    try {
      diary = objFactory.createDiary();

      Class.forName("org.gjt.mm.mysql.Driver");

      conn = DriverManager.getConnection("jdbc:mysql:///diary?useUnicode=true&characterEncoding=utf-8", user, password);
      stmt = conn.createStatement();

      MySQLCreator.createEntries(stmt, diary, objFactory);

      MySQLCreator.createHeaders(stmt, diary);

      MySQLCreator.createSides(stmt, diary);

      MySQLCreator.createFriends(stmt, diary);

      MySQLCreator.createTitle(stmt, diary);

      // timestamp
      diary.setTimestamp(Util.toXMLGregorianCalendar(Util.nowUTC()));
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
