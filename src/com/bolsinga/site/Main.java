package com.bolsinga.site;

public class Main {
  public static void main(String[] args) {
    if (args.length != 14) {
      Main.usage(args, "Wrong number of arguments");
    }

    // General arguments
    String command = args[13];
    String diaryFile = args[7];
    String musicFile = args[8];

    String user = args[10];
    String password = args[11];

    String output = args[12];

    // XML conversion arguments
    String itunes = args[0];
    String shows = args[1];
    String venue = args[2];
    String sort = args[3];
    String relations = args[4];
    String comments = args[5];
    String statics = args[6];
    
    // Site generation arguments
    String settingsFile = args[9];

    boolean musicXML = command.equals("musicxml") || command.equals("xml");
    boolean diaryXML = command.equals("diaryxml") || command.equals("xml");
    boolean musicImport = command.equals("musicimport") || command.equals("import");
    boolean diaryImport = command.equals("diaryimport") || command.equals("import");
    boolean site = command.matches("^site.*");
    boolean musicsite = command.matches("^musicsite.*");
    boolean diarysite = command.matches("^diarysite.*");

    if (!(musicXML | diaryXML | musicImport | diaryImport | site | musicsite | diarysite)) {
      Main.usage(args, "Invalid action");
    }

    if (musicXML || diaryXML) {
      if (musicXML) {
        com.bolsinga.shows.converter.Music.convert(shows, venue, sort, relations, itunes, musicFile);
      }
      if (diaryXML) {
        com.bolsinga.shows.converter.Diary.convert(comments, statics, diaryFile);
      }
      System.exit(0);
    }

    if (musicImport || diaryImport) {
      if (musicImport) {
        com.bolsinga.music.MySQLImporter.importData(musicFile, user, password, true);
      }
      if (diaryImport) {
        com.bolsinga.diary.MySQLImporter.importData(diaryFile, user, password, true);
      }
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(settingsFile);

    boolean useDB = command.matches(".*-db$");

    com.bolsinga.music.data.Music music = null;
    com.bolsinga.diary.data.Diary diary = null;

    if (!useDB) {
      diary = com.bolsinga.diary.Util.createDiary(diaryFile);
      music = com.bolsinga.music.Util.createMusic(musicFile);
    } else {
      diary = com.bolsinga.diary.Util.createDiary(user, password);
      music = com.bolsinga.music.Util.createMusic(user, password);
    }

    if (site) {
      com.bolsinga.site.Site.generate(diary, music, output, "all");
    }
    if (musicsite) {
      com.bolsinga.site.Site.generate(diary, music, output, "music");
    }
    if (diarysite) {
      com.bolsinga.site.Site.generate(diary, music, output, "diary");
    }
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [user] [password] [output.dir] <xml|musicxml|diaryxml|import|musicimport|diaryimport|site|musicsite|diarysite|site-ddb|musicsite-db|diarysite-db>");
    System.out.println(reason);
    System.out.println("Arguments:");
    for (int i = 0; i < badargs.length; i++) {
      System.out.print(badargs[i] + " ");
    }
    System.out.println();
    System.exit(0);
  }

}
