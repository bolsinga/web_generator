package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

public class WebDB {

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: WebDB [user] [password] [settings.xml] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    WebDB.generate(args[0], args[1], args[3]);
  }

  public static void generate(String user, String password, String outputDir) {
    Diary diary = Util.createDiary(user, password);
    WebDB.generate(diary, user, password, outputDir);
  }

  public static void generate(Diary diary, String user, String password, String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(user, password);
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    Web.generate(diary, music, encoder, outputDir);
  }
}
