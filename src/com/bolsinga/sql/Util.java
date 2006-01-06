package com.bolsinga.sql;

import java.sql.*;
import java.util.regex.*;

public class Util {
  private static Pattern sSQL = Pattern.compile("'");

  public static void insert(Statement stmt, String table, String[] rowItems) throws SQLException {
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

    stmt.execute(sb.toString());
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
