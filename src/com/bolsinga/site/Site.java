package com.bolsinga.site;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

public class Site {
  public static void main(String[] args) {
    if (args.length != 6) {
      Site.usage();
    }

    String type = args[0];

    String settingsFile = args[3];
    String outputDir = args[4];
    String variant = args[5];
    Music music = null;
    Diary diary = null;

    if (type.equals("xml")) {
      String diaryFile = args[1];
      String musicFile = args[2];

      diary = com.bolsinga.diary.Util.createDiary(diaryFile);
      music = com.bolsinga.music.Util.createMusic(musicFile);
    } else if (type.equals("db")) {
      String user = args[1];
      String password = args[2];
      
      diary = com.bolsinga.diary.Util.createDiary(user, password);
      music = com.bolsinga.music.Util.createMusic(user, password);
    } else {
      Site.usage();
    }

    com.bolsinga.web.Util.createSettings(settingsFile);

    Site.generate(diary, music, outputDir, variant);
  }

  private static void usage() {
      System.out.println("Usage: Web xml [diary.xml] [music.xml] [settings.xml] [output.dir] <all|web|music|diary>");
      System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir] <all|web|music|diary>");
      System.exit(0);
  }

  public static void generate(String diaryFile, String musicFile, String settingsFile, String outputDir, String variant) {
    com.bolsinga.web.Util.createSettings(settingsFile);
    Site.generate(diaryFile, musicFile, outputDir, variant);
  }

  public static void generate(String diaryFile, String musicFile, String outputDir, String variant) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
    
    Site.generate(diary, music, outputDir, variant);
  }

  public static void generate(Diary diary, Music music, String outputDir, String variant) {
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);

    boolean musicOnly = variant.equals("music");
    boolean diaryOnly = variant.equals("diary");
    boolean webOnly = variant.equals("web");
        
    if (!musicOnly) {
      com.bolsinga.diary.Web.generate(diary, music, encoder, outputDir);
    }
    if (!diaryOnly) {
      com.bolsinga.music.Web.generate(music, encoder, outputDir);
      if (!webOnly) {
        com.bolsinga.music.ICal.generate(music, outputDir);
      }
    }
    if (!webOnly) {
      com.bolsinga.rss.RSS.generate(diary, music, outputDir);
    }
  }
}
