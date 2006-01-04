package com.bolsinga.diary.importer;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

import com.bolsinga.diary.*;
import com.bolsinga.diary.data.*;

public class Import {

  private static Pattern sSQL = Pattern.compile("'");

  private static String sPrefix = "INSERT INTO entry VALUES (NULL, '";
  private static String sSuffix = "');";
  private static String sNext = "', '";

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

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Entry)i.next();

      StringBuffer sb = new StringBuffer(sPrefix);
      sb.append(Import.encodeSQLString(item.getComment()));
      sb.append(sNext);
      sb.append(Import.encodeSQLString(Util.getTitle(item)));
      sb.append(sNext);
      sb.append(new Timestamp(item.getTimestamp().getTime().getTime()).toString());
      sb.append(sSuffix);
      
      boolean result = false;
      try {
        result = stmt.execute(sb.toString());
        // true means there is a result to read, false means there isn't
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
  
  private static String encodeSQLString(String s) {
    Matcher m = sSQL.matcher(s);
    String result = m.replaceAll("''");
    return result;
  }
}
