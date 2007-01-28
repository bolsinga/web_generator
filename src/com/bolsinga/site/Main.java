package com.bolsinga.site;

import com.bolsinga.web.*;

public class Main implements Backgroundable {

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 13) {
      Main.usage(args, "Wrong number of arguments");
    }

    String itunes = args[0];
    String shows = args[1];
    String venue = args[2];
    String sort = args[3];
    String relations = args[4];
    String comments = args[5];
    String statics = args[6];

    String diaryFile = args[7];
    String musicFile = args[8];
    
    String settingsFile = args[9];
    
    String cssFile = args[10];

    String output = args[11];

    String command = args[12];

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    
    try {
      Main main = new Main(backgrounder, settingsFile);
      if (command.equals("xml")) {
        main.generateXML(diaryFile, musicFile, itunes, shows, venue, sort, relations, comments, statics);
      } else if (command.equals("site")) {
        main.generateSite(diaryFile, musicFile, output, cssFile);
      } else {
        Main.usage(args, "Invalid action");
      }
      main.complete();
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
    
  private Main(final Backgrounder backgrounder, final String settingsFile) throws WebException {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);

    Util.createSettings(settingsFile);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }
  
  private void generateXML(final String diaryFile, final String musicFile, final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics) throws com.bolsinga.shows.converter.ConvertException {
    com.bolsinga.shows.converter.Music.convert(shows, venue, sort, relations, itunes, musicFile);
    com.bolsinga.shows.converter.Diary.convert(comments, statics, diaryFile);
  }
  
  private void generateSite(final String diaryFile, final String musicFile, final String output, final String cssFile) throws WebException {
    final com.bolsinga.music.data.xml.Music music = Util.createMusic(musicFile);
    final com.bolsinga.diary.data.xml.Diary diary = Util.createDiary(diaryFile);
  
    CSS.install(cssFile, output);

    final Encode encoder = Encode.getEncode(music, diary);

    com.bolsinga.diary.Web.generate(fBackgrounder, this, diary, music, encoder, output);
    com.bolsinga.music.Web.generate(fBackgrounder, this, music, encoder, output);
    com.bolsinga.music.ICal.generate(music, output);
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [layout.css] [output.dir] <xml|site>");
    System.out.println(reason);
    System.out.println("Arguments:");
    for (int i = 0; i < badargs.length; i++) {
      System.out.print(badargs[i] + " ");
    }
    System.out.println();
    System.exit(0);
  }

}
