package com.bolsinga.site;

import com.bolsinga.diary.*;
import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.util.*;

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
      } else if (command.equals("xml-site")) {
        main.generateSite(diaryFile, musicFile, output, cssFile);
      } else if (command.equals("site")) {
        main.generateDirect(itunes, shows, venue, sort, relations, comments, statics, output, cssFile);
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
  
  private void generateXML(final String diaryFile, final String musicFile, final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    com.bolsinga.music.data.xml.Music.export(music, musicFile);

    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);
    com.bolsinga.diary.data.xml.Diary.export(diary, diaryFile);
  }
  
  private void generateSite(final String diaryFile, final String musicFile, final String output, final String cssFile) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.xml.Music.create(musicFile);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.xml.Diary.create(diaryFile);
    
    generateSite(music, diary, output, cssFile);
  }

  private void generateDirect(final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics, final String output, final String cssFile) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);

    generateSite(music, diary, output, cssFile);
  }

  private void dumpSimilarArtists(final com.bolsinga.music.data.Music music) {
    String s;
    HashSet<String> bands = new HashSet<String>();
    boolean displayed = false;
    
    List<com.bolsinga.music.data.Artist> artists = music.getArtists();
    for (com.bolsinga.music.data.Artist artist : artists) {
      s = artist.getName().toLowerCase();
      if (bands.contains(s)) {
        if (!displayed) {
          System.out.println("--Similar Artist Names--");
          displayed = true;
        }
        System.out.println(s);
      } else {
        bands.add(s);
      }
    }
  }
  
  private void generateSite(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary, final String output, final String cssFile) throws Exception {
  
    CSS.install(cssFile, output);

    dumpSimilarArtists(music);
    
    final Encode encoder = Encode.getEncode(music, diary);

    // Diary items
    MainDocumentCreator.createDocuments(fBackgrounder, this, diary, output, encoder, music);
    EntryRecordDocumentCreator.createDocuments(fBackgrounder, this, diary, output, encoder);
    AltDocumentCreator.createDocuments(fBackgrounder, this, diary, output);

    // Music items
    ArtistRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    VenueRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    ShowRecordDocumentCreator.createDocuments(fBackgrounder, this, music, encoder, output);
    CityRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);
    TracksRecordDocumentCreator.createDocuments(fBackgrounder, this, music, output);

    com.bolsinga.music.ICal.generate(music, output);
    
    com.bolsinga.rss.RSS.generate(diary, music, output);
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [layout.css] [output.dir] <xml|xml-site|site>");
    System.out.println(reason);
    System.out.println("Arguments:");
    for (int i = 0; i < badargs.length; i++) {
      System.out.print(badargs[i] + " ");
    }
    System.out.println();
    System.exit(0);
  }

}
