package com.bolsinga.diary.util;

import com.bolsinga.diary.data.*;

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
}
