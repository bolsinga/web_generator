package com.bolsinga.web.site;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

public class Site {
  public static void main(String[] args) {
    if (args.length != 5) {
      System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir] <all|web|music|diary>");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    Site.generate(args[0], args[1], args[3], args[4]);
  }
    
  public static void generate(String diaryFile, String musicFile, String outputDir, String variant) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
        
    boolean musicOnly = variant.equals("music");
    boolean diaryOnly = variant.equals("diary");
    boolean webOnly = variant.equals("web");
        
    if (!musicOnly) {
      com.bolsinga.diary.Web.generate(diary, music, outputDir);
    }
    if (!diaryOnly) {
      com.bolsinga.music.Web.generate(music, outputDir);
      if (!webOnly) {
        com.bolsinga.music.ICal.generate(music, outputDir);
      }
    }
    if (!webOnly) {
      com.bolsinga.rss.RSS.generate(diary, music, outputDir);
    }
  }
}
