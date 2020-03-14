package com.bolsinga.test;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

import com.bolsinga.rss.*;

public class RSSTest {

  public static void main(String[] args) {
    if (args.length != 5) {
      RSSTest.usage();
    }

    String type = args[0];

    String settings = args[3];
    String output = args[4];

    Diary diary = null;
    Music music = null;

    try {
      if (type.equals("json")) {
        String diaryFile = args[1];
        String musicFile = args[2];

        diary = com.bolsinga.diary.data.json.Diary.create(diaryFile);
        music = com.bolsinga.music.data.json.Music.create(musicFile);
      } else {
        RSSTest.usage();
      }

      com.bolsinga.web.Util.createSettings(settings);
    } catch (com.bolsinga.web.WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
    
    try {
      RSS.generate(diary, music, output);
    } catch (RSSException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void usage() {
    System.out.println("Usage: RSSTest json [diary.json] [music.json] [settings.xml] [output.dir]");
    System.exit(0);
  }
}
