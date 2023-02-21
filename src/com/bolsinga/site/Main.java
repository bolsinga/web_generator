package com.bolsinga.site;

import com.bolsinga.diary.*;
import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.util.*;

public class Main implements Backgroundable {

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 12) {
      Main.usage(args, "Wrong number of arguments");
    }

    int i = 0;
    String itunes = args[i++];
    String shows = args[i++];
    String venue = args[i++];
    String sort = args[i++];
    String relations = args[i++];
    String comments = args[i++];
    String statics = args[i++];
    
    String settingsFile = args[i++];
        
    String diaryFile = args[i++];
    String musicFile = args[i++];

    String output = args[i++];

    String command = args[i++];

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    
    try {
      Main main = new Main(backgrounder, settingsFile);
      if (command.equals("site")) {
        main.generateDirect(itunes, shows, venue, sort, relations, comments, statics, output);
      } else if (command.equals("json")) {
        main.generateJSON(diaryFile, musicFile, itunes, shows, venue, sort, relations, comments, statics);
      } else if (command.equals("json-site")) {
        main.generateJSONSite(diaryFile, musicFile, output);
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

  private void generateDirect(final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics, final String output) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);

    generateSite(music, diary, output);
  }
  
  private void generateJSON(final String diaryFile, final String musicFile, final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    com.bolsinga.music.data.json.Music.export(music, musicFile);

    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);
    com.bolsinga.diary.data.json.Diary.export(diary, diaryFile);
  }

  private void generateJSONSite(final String diaryFile, final String musicFile, final String output) throws Exception {
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.json.Diary.create(diaryFile);
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.json.Music.create(musicFile);
    
    generateSite(music, diary, output);
  }

  private void dumpSimilarArtists(final com.bolsinga.music.data.Music music) {
    String s;
    HashMap<String, com.bolsinga.music.data.Artist> bands = new HashMap<String, com.bolsinga.music.data.Artist>();
    boolean displayed = false;
    
    List<? extends com.bolsinga.music.data.Artist> artists = music.getArtists();
    for (com.bolsinga.music.data.Artist artist : artists) {
      s = com.bolsinga.music.Compare.simplify(artist.getName());
      if (bands.containsKey(s)) {
        if (!displayed) {
          System.out.println("--Similar Artist Names--");
          displayed = true;
        }
        com.bolsinga.music.data.Artist existingArtist = bands.get(s);
        System.out.println("Existing: " + existingArtist.getName() + " Duplicate: " + artist.getName() + " Simplified: " + s);
      } else {
        bands.put(s, artist);
      }
    }
  }

  private void generateSite(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary, final String output) throws Exception {
    CSS.install(output);

    dumpSimilarArtists(music);

    // Diary items
    MainDocumentCreator.createDocuments(fBackgrounder, this, diary, output, music);
    EntryRecordDocumentCreator.createDocuments(fBackgrounder, this, diary, output);
    AltDocumentCreator.createDocuments(fBackgrounder, this, diary, output);

    // Music items
    ArtistRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    VenueRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    ShowRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    CityRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    TracksRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);

    com.bolsinga.music.ICal.generate(music, output);
    
    com.bolsinga.rss.RSS.generate(diary, music, output);

    {
      java.io.File jsonDirectory = com.bolsinga.web.Util.createJSONDirectory(output);

      StringBuilder musicFile = new StringBuilder(com.bolsinga.web.Util.getSettings().getMusicJsonFile());
      musicFile.append(com.bolsinga.web.Links.JSON_EXT);
      java.io.File musicJSON = new java.io.File(jsonDirectory, musicFile.toString());
      com.bolsinga.music.data.json.Music.export(music, musicJSON.getAbsolutePath());

      StringBuilder diaryFile = new StringBuilder(com.bolsinga.web.Util.getSettings().getDiaryJsonFile());
      diaryFile.append(com.bolsinga.web.Links.JSON_EXT);
      java.io.File diaryJSON = new java.io.File(jsonDirectory, diaryFile.toString());
      com.bolsinga.diary.data.json.Diary.export(diary, diaryJSON.getAbsolutePath());
    }
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [itunes.json] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [settings.properties] [diary input/output file] [music input/output file] [output.dir] <json|json-site|site|index>");
    System.out.println(reason);
    if (badargs != null) {
      System.out.println("Arguments:");
      for (int i = 0; i < badargs.length; i++) {
        System.out.print(badargs[i] + " ");
      }
    }
    System.out.println();
    System.exit(0);
  }

}
