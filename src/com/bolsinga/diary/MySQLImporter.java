package com.bolsinga.diary;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import com.bolsinga.diary.data.*;

import com.bolsinga.web.*;

public class MySQLImporter {

  private static final Pattern sFriends = Pattern.compile("\"(.*)\">(.*)<");

  public static void main(String[] args) {
    if ((args.length != 3) && (args.length != 4)) {
      MySQLImporter.usage();
    }

    boolean clearDB = false;
    if (args.length == 4) {
      clearDB = args[3].equals("clear");
    }

    MySQLImporter.importData(args[0], args[1], args[2], clearDB);
  }

  private static void usage() {
    System.out.println("Usage: MySQLImporter [diary.xml] [user] [password] <clear>");
    System.exit(0);
  }

  public static void importData(final String sourceFile, final String user, final String password, final boolean clearDB) {
    Diary diary = Util.createDiary(sourceFile);
    importData(diary, user, password, clearDB);
  }

  public static void importData(final Diary diary, final String user, final String password, final boolean clearDB) {
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

      if (clearDB) {
        clearDB(stmt);
      }

      importEntries(stmt, diary);

      importHeaders(stmt, diary);

      importSides(stmt, diary);

      importFriends(stmt, diary);

      String[] title = { diary.getTitle() };
      com.bolsinga.sql.Util.insert(stmt, "title", title);

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
      } catch (SQLException e) {
        System.err.println("Exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  private static void clearDB(final Statement stmt) throws SQLException {
    com.bolsinga.sql.Util.truncate(stmt, "category");
    com.bolsinga.sql.Util.truncate(stmt, "entry");
    com.bolsinga.sql.Util.truncate(stmt, "friend");
    com.bolsinga.sql.Util.truncate(stmt, "header");
    com.bolsinga.sql.Util.truncate(stmt, "side");
    com.bolsinga.sql.Util.truncate(stmt, "title");
  }

  private static void importEntry(final Statement stmt, final Entry entry) throws SQLException {
    String[] rowItems = new String[4];
    
    rowItems[0] = null;
    rowItems[1] = entry.getComment();
    rowItems[2] = Util.getTitle(entry);
    rowItems[3] = com.bolsinga.sql.Util.toDATETIME(entry.getTimestamp().toGregorianCalendar());
    
    com.bolsinga.sql.Util.insert(stmt, "entry", rowItems);
  }

  private static void importEntries(final Statement stmt, final Diary diary) throws SQLException {
    List<Entry> items = Util.getEntriesCopy(diary);
                
    Collections.sort(items, Util.ENTRY_COMPARATOR);

    for (Entry item : items) {
      try {
        importEntry(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + Util.getTitle(item));
        throw e;
      }
    }
  }

  private static void importHeader(final Statement stmt, final String header) throws SQLException {
    String[] rowItems = new String[2];

    rowItems[0] = null;
    rowItems[1] = header;

    com.bolsinga.sql.Util.insert(stmt, "header", rowItems);
  }

  private static void importHeaders(final Statement stmt, final Diary diary) throws SQLException {
    String data = diary.getHeader();

    if (data == null) {
      return;
    }

    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      try {
        MySQLImporter.importHeader(stmt, lines[i]);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + lines[i]);
        throw e;
      }
    }
  }

  private static void importSide(final Statement stmt, final String side) throws SQLException {
    String[] rowItems = new String[2];

    rowItems[0] = null;
    rowItems[1] = side;

    com.bolsinga.sql.Util.insert(stmt, "side", rowItems);
  }

  private static void importSides(final Statement stmt, final Diary diary) throws SQLException {
    String data = diary.getStatic();

    if (data == null) {
      return;
    }

    String[] lines = data.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      try {
        MySQLImporter.importSide(stmt, lines[i]);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + lines[i]);
        throw e;
      }
    }
  }

  private static void importFriend(final Statement stmt, final String name, final String displayName, final String url) throws SQLException {
    String [] rowItems = new String[4];

    rowItems[0] = null;
    rowItems[1] = name;
    rowItems[2] = displayName;
    rowItems[3] = url;

    com.bolsinga.sql.Util.insert(stmt, "friend", rowItems);
  }

  private static void importFriends(final Statement stmt, final Diary diary) throws SQLException {
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
          MySQLImporter.importFriend(stmt, name, displayName, url);
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
