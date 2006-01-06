package com.bolsinga.diary.importer;

import java.sql.*;
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
      conn = DriverManager.getConnection("jdbc:mysql:///diary", user, password);
      
      stmt = conn.createStatement();
    } catch (Exception e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    }

    List items = diary.getEntry();
    Entry item = null;
                
    Collections.sort(items, Util.ENTRY_COMPARATOR);

    String[] rowItems = new String[4];
    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Entry)i.next();

      rowItems[0] = null;
      rowItems[1] = item.getComment();
      rowItems[2] = Util.getTitle(item);
      rowItems[3] = new Timestamp(item.getTimestamp().getTime().getTime()).toString();
      
      try {
        com.bolsinga.sql.Util.insert(stmt, "entry", rowItems);
      } catch (SQLException e) {
        System.err.println("Exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }
    
    try {
      // Close statement and DB connection
      //
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
