package com.bolsinga.test;

public class ConvertDiaryTest {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: ConvertDiaryTest [comments] [statics] [output]");
      System.exit(0);
    }
    
    try {
      com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(args[0], args[1]);
      com.bolsinga.diary.data.xml.Diary.export(diary, args[2]);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
