package com.bolsinga.diary.importer;

import com.bolsinga.diary.data.*;

import java.sql.*;

public class Import {
  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Web [diary.xml] [user] [password]");
      System.exit(0);
    }

    Import.importData(args[0], args[1], args[2]);
  }

  public static void importData(String sourceFile, String user, String password) {
    Diary diary = com.bolsinga.diary.Util.createDiary(sourceFile);
    importData(diary, user, password);
  }

  public static void importData(Diary diary, String user, String password) {

    try {
      // Load the driver class
      //
      Class.forName("org.gjt.mm.mysql.Driver");
      
      // Try to connect to the DB server.
      // We tell JDBC to use the "mysql" driver
      // and to connect to the "test" database
      // which should always exist in MySQL.
      //
      // We use the username "" and no
      // password to connect. This should always
      // work for the "test" database.
      //
      Connection conn = DriverManager.getConnection("jdbc:mysql:///diary", user, password);
      
      // Set up and run a query that fetches
      // the current date using the "now()" SQL function.
      // 
      Statement stmt = conn.createStatement();
      ResultSet rset = stmt.executeQuery("SELECT now();");
      
		// Iterate through the rows of the result set
		// (obviously only one row in this example) and
		// print each one.
		//
      while (rset.next()) {
        System.out.println(rset.getString(1));
      }    
      
      // Close result set, statement and DB connection
		//
      rset.close();
      stmt.close();
      conn.close();
    } catch (Exception e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
