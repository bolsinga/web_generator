package com.bolsinga.site;

public class Main {
  public static void main(String[] args) {
    if (args.length != 12) {
      Main.usage(args, "Wrong number of arguments");
    }

    // General arguments
    String command = args[11];
    String diary = args[7];
    String music = args[8];
    String output = args[10];

    // XML conversion arguments
    String itunes = args[0];
    String shows = args[1];
    String venue = args[2];
    String sort = args[3];
    String relations = args[4];
    String comments = args[5];
    String statics = args[6];
    
    // Site generation arguments
    String settings = args[9];

    boolean musicXML = command.equals("musicxml") || command.equals("xml");
    boolean diaryXML = command.equals("diaryxml") || command.equals("xml");
    boolean site = command.equals("site");
    boolean musicsite = command.equals("musicsite");
    boolean diarysite = command.equals("diarysite");

    if (!(musicXML | diaryXML | site | musicsite | diarysite)) {
      Main.usage(args, "Invalid action");
    }

    if (musicXML) {
      com.bolsinga.shows.converter.Music.convert(shows, venue, sort, relations, itunes, music);
    }
    if (diaryXML) {
      com.bolsinga.shows.converter.Diary.convert(comments, statics, diary);
    }
    if (site) {
      com.bolsinga.site.Site.generate(diary, music, settings, output, "all");
    }
    if (musicsite) {
      com.bolsinga.site.Site.generate(diary, music, settings, output, "music");
    }
    if (diarysite) {
      com.bolsinga.site.Site.generate(diary, music, settings, output, "diary");
    }
  }

  private static void usage(String[] badargs, String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [output.dir] <xml|musicxml|diaryxml|site|musicsite|diarysite>");
    System.out.println(reason);
    System.out.println("Arguments:");
    for (int i = 0; i < badargs.length; i++) {
      System.out.print(badargs[i] + " ");
    }
    System.out.println();
    System.exit(0);
  }

}
