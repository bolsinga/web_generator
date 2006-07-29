package com.bolsinga.sql;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class Util {
  private static final Pattern sSQL = Pattern.compile("'");

  private static final DateFormat sSQLDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  static {
    sSQLDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private static String constructInsert(String table, String[] rowItems) {
    StringBuffer sb = new StringBuffer("INSERT INTO ");
    sb.append(table);
    sb.append(" VALUES (");

    for (int i = 0; i < rowItems.length; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(Util.quote(rowItems[i]));
    }

    sb.append(");");
    
    return sb.toString();
  }

  public static void insert(Statement stmt, String table, String[] rowItems) throws SQLException {
    stmt.execute(Util.constructInsert(table, rowItems));
  }

  public static void truncate(Statement stmt, String table) throws SQLException {
    StringBuffer sb = new StringBuffer("TRUNCATE ");
    sb.append(table);

    stmt.execute(sb.toString());
  }

  public static GregorianCalendar toCalendarUTC(String sqlDATETIME) {
    java.util.Date d = null;
    try {
      d = sSQLDateTimeFormat.parse(sqlDATETIME);
    } catch (ParseException e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    }
    GregorianCalendar c = new GregorianCalendar(sSQLDateTimeFormat.getTimeZone());
    c.setTime(d);
    return c;
  }

  public static String toDATETIME(GregorianCalendar c) {
    return sSQLDateTimeFormat.format(c.getTime());
  }
  
  private static String quote(String s) {
    if (s != null) {
      StringBuffer sb = new StringBuffer();
      sb.append("'");
      sb.append(Util.encodeSQLString(s));
      sb.append("'");
      return sb.toString();
    } else {
      return "NULL";
    }
  }

  private static String encodeSQLString(String s) {
    Matcher m = sSQL.matcher(s);
    String result = m.replaceAll("''");
    return result;
  }
}
