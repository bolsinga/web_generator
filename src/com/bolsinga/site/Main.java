package com.bolsinga.site;

public class Main implements com.bolsinga.web.Backgroundable {

  private final com.bolsinga.web.Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 15) {
      Main.usage(args, "Wrong number of arguments");
    }

    // General arguments
    String command = args[14];
    String diaryFile = args[7];
    String musicFile = args[8];

    String user = args[11];
    String password = args[12];

    String output = args[13];

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
    
    String cssFile = args[10];

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
   
    Main main = new Main(backgrounder);
    boolean success = main.generate(command, diaryFile, musicFile, user, password, output, itunes, shows, venue, sort, relations, comments, statics, settingsFile, cssFile);
    if (!success) {
      Main.usage(args, "Invalid action");
    }
    main.complete();
  }
    
  Main(final com.bolsinga.web.Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);
  }
  
  void complete() {
    fBackgrounder.removeInterest(this);
  }
  
  boolean generate(String command, String diaryFile, String musicFile, String user, String password, String output, String itunes, String shows, String venue, String sort, String relations, String comments, String statics, String settingsFile, String cssFile) {
    boolean musicXML = command.equals("musicxml") || command.equals("xml");
    boolean diaryXML = command.equals("diaryxml") || command.equals("xml");
    boolean musicImport = command.equals("musicimport") || command.equals("import");
    boolean diaryImport = command.equals("diaryimport") || command.equals("import");
    boolean site = command.matches("^site.*");
    boolean musicsite = command.matches("^musicsite.*");
    boolean diarysite = command.matches("^diarysite.*");

    if (!(musicXML | diaryXML | musicImport | diaryImport | site | musicsite | diarysite)) {
      return false;
    }

    com.bolsinga.web.Util.createSettings(settingsFile);

    if (musicXML || diaryXML) {
      if (musicXML) {
        com.bolsinga.shows.converter.Music.convert(shows, venue, sort, relations, itunes, musicFile);
      }
      if (diaryXML) {
        com.bolsinga.shows.converter.Diary.convert(comments, statics, diaryFile);
      }
      return true;
    }

    if (musicImport || diaryImport) {
      if (musicImport) {
        com.bolsinga.music.MySQLImporter.importData(musicFile, user, password, true);
      }
      if (diaryImport) {
        com.bolsinga.diary.MySQLImporter.importData(diaryFile, user, password, true);
      }
      return true;
    }

    boolean useDB = command.matches(".*-db$");

    com.bolsinga.music.data.Music music = null;
    com.bolsinga.diary.data.Diary diary = null;

    if (!useDB) {
      diary = com.bolsinga.diary.Util.createDiary(diaryFile);
      music = com.bolsinga.music.Util.createMusic(musicFile);
    } else {
      diary = com.bolsinga.diary.MySQLCreator.createDiary(user, password);
      music = com.bolsinga.music.MySQLCreator.createMusic(user, password);
    }
    
    // Everything needs the CSS file.
    com.bolsinga.web.CSS.install(cssFile, output);

    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);

    if (site) {
      com.bolsinga.site.Site.generate(fBackgrounder, this, encoder, diary, music, output, "all");
    }
    if (musicsite) {
      com.bolsinga.site.Site.generate(fBackgrounder, this, encoder, diary, music, output, "music");
    }
    if (diarysite) {
      com.bolsinga.site.Site.generate(fBackgrounder, this, encoder, diary, music, output, "diary");
    }

    return true;
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [layout.css] [user] [password] [output.dir] <xml|musicxml|diaryxml|import|musicimport|diaryimport|site|musicsite|diarysite|site-ddb|musicsite-db|diarysite-db>");
    System.out.println(reason);
    System.out.println("Arguments:");
    for (int i = 0; i < badargs.length; i++) {
      System.out.print(badargs[i] + " ");
    }
    System.out.println();
    System.exit(0);
  }

}
