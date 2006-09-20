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

  private static String constructInsert(final String table, final String[] rowItems) {
    StringBuilder sb = new StringBuilder("INSERT INTO ");
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

  public static void insert(final Statement stmt, final String table, final String[] rowItems) throws SQLException {
    stmt.execute(Util.constructInsert(table, rowItems));
  }

  public static void truncate(final Statement stmt, final String table) throws SQLException {
    StringBuilder sb = new StringBuilder("TRUNCATE ");
    sb.append(table);

    stmt.execute(sb.toString());
  }

  public static GregorianCalendar toCalendarUTC(final String sqlDATETIME) {
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

  public static String toDATETIME(final GregorianCalendar c) {
    return sSQLDateTimeFormat.format(c.getTime());
  }
  
  private static String quote(final String s) {
    if (s != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("'");
      sb.append(Util.encodeSQLString(s));
      sb.append("'");
      return sb.toString();
    } else {
      return "NULL";
    }
  }

  private static String encodeSQLString(final String s) {
    Matcher m = sSQL.matcher(s);
    String result = m.replaceAll("''");
    return result;
  }
}
