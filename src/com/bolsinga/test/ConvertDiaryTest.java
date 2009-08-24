package com.bolsinga.test;

import com.bolsinga.web.*;

public class ConvertDiaryTest {

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: ConvertDiaryTest [comments] [statics] [output] [settings.xml]");
      System.exit(0);
    }
    
    try {
      Util.createSettings(args[0]);

      com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(args[1], args[2]);
      com.bolsinga.diary.data.xml.Diary.export(diary, args[3]);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
