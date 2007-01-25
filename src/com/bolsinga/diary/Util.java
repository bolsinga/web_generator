package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.text.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class Util {
  private static int sDiaryStartYear = 0;
  
  private static final ThreadLocal<DateFormat> sWebFormat   = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("M/d/yyyy");
    }
  };
  private static final ThreadLocal<DateFormat> sMonthFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("MMMM");
    }
  };
  private static final ThreadLocal<DateFormat> sShortMonthFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("MMM");
    }
  };

  public static final Comparator<Entry> ENTRY_COMPARATOR = new Comparator<Entry>() {
      public int compare(final Entry e1, final Entry e2) {
        int comparison = e1.getTimestamp().compare(e2.getTimestamp());
        return (comparison == DatatypeConstants.LESSER) ? -1 : 1;
      }
    };
  
  public static final Comparator<Calendar> MONTH_COMPARATOR = new Comparator<Calendar>() {
    public int compare(final Calendar c1, final Calendar c2) {
      int m1 = c1.get(Calendar.MONTH);
      int m2 = c2.get(Calendar.MONTH);
      return (m1 - m2);
    }
  };

  public static String getTitle(final Entry entry) {
    return sWebFormat.get().format(entry.getTimestamp().toGregorianCalendar().getTime());
  }
  
  public static String getMonth(final Calendar month) {
    return sMonthFormat.get().format(month.getTime());
  }
  
  public static String getMonth(final Entry entry) {
    return Util.getMonth(entry.getTimestamp().toGregorianCalendar());
  }
  
  public static String getShortMonthName(final Calendar month) {
    return sShortMonthFormat.get().format(month.getTime());
  }

  public static int getStartYear(final Diary diary) {
    synchronized (Util.class) {
      if (sDiaryStartYear == 0) {
        List<Entry> items = Util.getEntriesCopy(diary);
        Entry item = null;

        Collections.sort(items, Util.ENTRY_COMPARATOR);

        item = items.get(0);

        sDiaryStartYear = item.getTimestamp().getYear();
      }
    }
    return sDiaryStartYear;
  }
  
  public static List<Entry> getEntriesUnmodifiable(final Diary diary) {
    return Collections.unmodifiableList(diary.getEntry());
  }

  public static List<Entry> getEntriesCopy(final Diary diary) {
    return new ArrayList<Entry>(diary.getEntry());
  }
    
  public static Diary createDiary(final String sourceFile) {
    Diary diary = null;
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data");
      Unmarshaller u = jc.createUnmarshaller();
                        
      diary = (Diary)u.unmarshal(new java.io.FileInputStream(sourceFile));
    } catch (Exception ume) {
      System.err.println("Exception: " + ume);
      ume.printStackTrace();
      System.exit(1);
    }
    return diary;
  }
}
