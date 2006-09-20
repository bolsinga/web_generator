package com.bolsinga.site;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

public class Site implements com.bolsinga.web.Backgroundable {

  private final com.bolsinga.web.Backgrounder fBackgrounder;
  
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
    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    
    Site site = new Site(backgrounder);
    site.generate(diary, music, outputDir, variant);
    site.complete();
  }
  
  Site(final com.bolsinga.web.Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);
  }
  
  void complete() {
    fBackgrounder.removeInterest(this);
  }

  private static void usage() {
      System.out.println("Usage: Web xml [diary.xml] [music.xml] [settings.xml] [output.dir] <all|web|music|diary>");
      System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir] <all|web|music|diary>");
      System.exit(0);
  }

  public void generate(final Diary diary, final Music music, final String outputDir, final String variant) {
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(fBackgrounder, music, diary);
    Site.generate(fBackgrounder, this, encoder, diary, music, outputDir, variant);
  }

  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final com.bolsinga.web.Encode encoder, final Diary diary, final Music music, final String outputDir, final String variant) {
    boolean musicOnly = variant.equals("music");
    boolean diaryOnly = variant.equals("diary");
    boolean webOnly = variant.equals("web");

    if (!musicOnly) {
      com.bolsinga.diary.Web.generate(backgrounder, backgroundable, diary, music, encoder, outputDir);
    }
    if (!diaryOnly) {
      com.bolsinga.music.Web.generate(backgrounder, backgroundable, music, encoder, outputDir);
      if (!webOnly) {
        com.bolsinga.music.ICal.generate(music, outputDir);
      }
    }
    if (!webOnly) {
      com.bolsinga.rss.RSS.generate(diary, music, outputDir);
    }
  }
}
