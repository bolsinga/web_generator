package com.bolsinga.test;

import com.bolsinga.shows.converter.*;

public class ConvertDiaryTest {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: ConvertDiaryTest [comments] [statics] [output]");
      System.exit(0);
    }
    
    try {
      Diary.convert(args[0], args[1], args[2]);
    } catch (ConvertException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
