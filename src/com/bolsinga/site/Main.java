package com.bolsinga.site;

public class Main {
  public static void main(String[] args) {
    if (args.length != 12) {
      Main.usage(args);
    }

    String command = args[11];
    String diary = args[7];
    String music = args[8];
    String output = args[10];

    if (command.matches("xml")) {
      String itunes = args[0];
      String shows = args[1];
      String venue = args[2];
      String sort = args[3];
      String relations = args[4];
      String comments = args[5];
      String statics = args[6];

      boolean musicXML = command.equals("musicxml") || command.equals("xml");
      boolean diaryXML = command.equals("diaryxml") || command.equals("xml");

      if (!musicXML && !diaryXML) {
        Main.usage(args);
      } else {
        if (musicXML) {
          com.bolsinga.shows.converter.Music.convert(shows, venue, sort, relations, itunes, music);
        }
        if (diaryXML) {
          com.bolsinga.shows.converter.Diary.convert(comments, statics, diary);
        }
      }
    } else if (command.matches("site")) {
      String settings = args[9];
      if (command.equals("site")) {
        com.bolsinga.site.Site.generate(diary, music, settings, output, "all");
      } else if (command.equals("musicsite")) {
        com.bolsinga.site.Site.generate(diary, music, settings, output, "music");
      } else if (command.equals("diarysite")) {
        com.bolsinga.site.Site.generate(diary, music, settings, output, "diary");
      } else {
        Main.usage(args);
      }
    } else {
      Main.usage(args);
    }
  }

  private static void usage(String[] badargs) {
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [output.dir] <xml|musicxml|diaryxml|site|musicsite|diarysite>");
    System.out.println("");
    System.exit(0);
  }

}
