package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.sql.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {
  public  static DateFormat sWebFormat   = new SimpleDateFormat("M/d/yyyy");
  private static DateFormat sMonthFormat = new SimpleDateFormat("MMMM");

  public static final Comparator ENTRY_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Entry e1 = (Entry)o1;
        Entry e2 = (Entry)o2;
                        
        return e1.getTimestamp().before(e2.getTimestamp()) ? -1 : 1;
      }
    };

  public static String getTitle(Entry entry) {
    return sWebFormat.format(entry.getTimestamp().getTime());
  }
        
  public static String getMonth(Entry entry) {
    return sMonthFormat.format(entry.getTimestamp().getTime());
  }

  public static int getStartYear(Diary diary) {
    List items = diary.getEntry();
    Entry item = null;

    Collections.sort(items, Util.ENTRY_COMPARATOR);

    item = (Entry)items.get(0);

    return item.getTimestamp().get(Calendar.YEAR);
  }
    
  public static com.bolsinga.diary.data.Diary createDiary(String sourceFile) {
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

  public static com.bolsinga.diary.data.Diary createDiary(String user, String password) {
    Diary diary = null;
    Entry entry = null;
    Connection conn = null;
    Statement stmt = null;
    ResultSet rset = null;

    ObjectFactory objFactory = new ObjectFactory();
    try {
      diary = objFactory.createDiary();

      Class.forName("org.gjt.mm.mysql.Driver");

      conn = DriverManager.getConnection("jdbc:mysql:///diary", user, password);
      stmt = conn.createStatement();
      rset = stmt.executeQuery("SELECT * FROM entry ORDER BY id;");

      while (rset.next()) {
        entry = objFactory.createEntry();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date(rset.getTimestamp("timestamp").getTime()));
        entry.setTimestamp(cal);
        entry.setComment(rset.getString("comment"));
        entry.setId("e" + (rset.getLong("id") - 1));

        diary.getEntry().add(entry);
      }
    } catch (Exception e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        if (rset != null) {
          rset.close();
        }
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
