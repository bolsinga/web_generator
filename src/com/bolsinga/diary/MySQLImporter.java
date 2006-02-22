package com.bolsinga.diary.importer;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import com.bolsinga.diary.*;
import com.bolsinga.diary.data.*;

public class Import {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Web [diary.xml] [user] [password]");
      System.exit(0);
    }

    Import.importData(args[0], args[1], args[2]);
  }

  public static void importData(String sourceFile, String user, String password) {
    Diary diary = Util.createDiary(sourceFile);
    importData(diary, user, password);
  }

  public static void importData(Diary diary, String user, String password) {
    Connection conn = null;
    Statement stmt = null;
    try {
      // Load the driver class
      //
      Class.forName("org.gjt.mm.mysql.Driver");
      
      // Try to connect to the DB server.
      // We tell JDBC to use the "mysql" driver
      // and to connect to the "diary" database
      //
      conn = DriverManager.getConnection("jdbc:mysql:///diary?useUnicode=true&characterEncoding=utf-8", user, password);
      
      stmt = conn.createStatement();

      importEntries(stmt, diary);

      importHeaders(stmt, diary);

      importSides(stmt, diary);

      importFriends(stmt, diary);

      String[] title = { diary.getTitle() };
      com.bolsinga.sql.Util.insert(stmt, "title", title);

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
      } catch (SQLException e) {
        System.err.println("Exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  private static void importEntry(Statement stmt, Entry entry) throws SQLException {
    String[] rowItems = new String[4];
    
    rowItems[0] = null;
    rowItems[1] = entry.getComment();
    rowItems[2] = Util.getTitle(entry);
    rowItems[3] = com.bolsinga.sql.Util.toDATETIME(entry.getTimestamp());
    
    com.bolsinga.sql.Util.insert(stmt, "entry", rowItems);
  }

  private static void importEntries(Statement stmt, Diary diary) throws SQLException {
    List items = diary.getEntry();
    Entry item = null;
                
    Collections.sort(items, Util.ENTRY_COMPARATOR);

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Entry)i.next();

      try {
        importEntry(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + Util.getTitle(item));
        throw e;
      }
    }
  }

  private static void importHeader(Statement stmt, String header) throws SQLException {
    String[] rowItems = new String[2];

    rowItems[0] = null;
    rowItems[1] = header;

    com.bolsinga.sql.Util.insert(stmt, "header", rowItems);
  }

  private static void importHeaders(Statement stmt, Diary diary) throws SQLException {
    String data = diary.getHeader();

    if (data == null) {
      return;
    }

    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      try {
        Import.importHeader(stmt, lines[i]);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + lines[i]);
        throw e;
      }
    }
  }

  private static void importSide(Statement stmt, String side) throws SQLException {
    String[] rowItems = new String[2];

    rowItems[0] = null;
    rowItems[1] = side;

    com.bolsinga.sql.Util.insert(stmt, "side", rowItems);
  }

  private static void importSides(Statement stmt, Diary diary) throws SQLException {
    String data = diary.getStatic();

    if (data == null) {
      return;
    }

    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      try {
        Import.importSide(stmt, lines[i]);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + lines[i]);
        throw e;
      }
    }
  }

  private static void importFriend(Statement stmt, String name, String displayName, String url) throws SQLException {
    String [] rowItems = new String[4];

    rowItems[0] = null;
    rowItems[1] = name;
    rowItems[2] = displayName;
    rowItems[3] = url;

    com.bolsinga.sql.Util.insert(stmt, "friend", rowItems);
  }

  private static Pattern sFriends = Pattern.compile("\"(.*)\">(.*)<");

  private static void importFriends(Statement stmt, Diary diary) throws SQLException {
    String data = diary.getFriends();
    String name = null, displayName = null, url = null;

    if (data == null) {
      return;
    }

    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      Matcher m = sFriends.matcher(lines[i]);
      if (m.find()) {
        try {
          name = displayName = m.group(2);
          url = m.group(1);
          Import.importFriend(stmt, name, displayName, url);
        } catch (SQLException e) {
          System.err.println("SQLException importing: " + lines[i]);
          throw e;
        }
      } else {
        System.err.println("Can't match: " + lines[i]);
        System.exit(1);
      }
    }
  }
}
