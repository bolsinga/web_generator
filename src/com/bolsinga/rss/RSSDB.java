package com.bolsinga.rss;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

public class RSSDB {
  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: RSSDB [user] [password] [settings.xml] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    RSSDB.generate(args[0], args[1], args[3]);
  }

  public static void generate(String user, String password, String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(user, password);
    Diary diary = com.bolsinga.diary.Util.createDiary(user, password);

    RSS.generate(diary, music, outputDir);
  }
}
