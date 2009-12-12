package com.bolsinga.site;

import com.bolsinga.diary.*;
import com.bolsinga.music.*;
import com.bolsinga.web.*;

import org.json.*;

import java.util.*;

public class Main implements Backgroundable {

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 14) {
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
    
    String cssFile = args[i++];

    String javaScriptFile = args[i++];
    
    String diaryFile = args[i++];
    String musicFile = args[i++];

    String output = args[i++];

    String command = args[i++];

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    
    try {
      Main main = new Main(backgrounder, settingsFile);
      if (command.equals("xml")) {
        main.generateXML(diaryFile, musicFile, itunes, shows, venue, sort, relations, comments, statics);
      } else if (command.equals("xml-site")) {
        main.generateSite(diaryFile, musicFile, output, cssFile, javaScriptFile);
      } else if (command.equals("site")) {
        main.generateDirect(itunes, shows, venue, sort, relations, comments, statics, output, cssFile, javaScriptFile);
      } else if (command.equals("json")) {
        main.generateJSON(diaryFile, musicFile, itunes, shows, venue, sort, relations, comments, statics);
      } else if (command.equals("json-site")) {
        main.generateJSONSite(diaryFile, musicFile, output, cssFile, javaScriptFile);
      } else if (command.startsWith("gen-json-")) {
        String type = command.substring("gen-json-".length());
        // In this case, diaryFile is the output directory.
        String outputDir = diaryFile;
        main.generateJSONFile(itunes, shows, venue, sort, relations, comments, statics, type, outputDir);
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
  
  private void generateSite(final String diaryFile, final String musicFile, final String output, final String cssFile, final String javaScriptFile) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.xml.Music.create(musicFile);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.xml.Diary.create(diaryFile);
    
    generateSite(music, diary, output, cssFile, javaScriptFile);
  }

  private void generateDirect(final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics, final String output, final String cssFile, final String javaScriptFile) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);

    generateSite(music, diary, output, cssFile, javaScriptFile);
  }
  
  private void generateJSON(final String diaryFile, final String musicFile, final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    com.bolsinga.music.data.json.Music.export(music, musicFile);

    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);
    com.bolsinga.diary.data.json.Diary.export(diary, diaryFile);
  }

  private void generateJSONSite(final String diaryFile, final String musicFile, final String output, final String cssFile, final String javaScriptFile) throws Exception {
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.json.Diary.create(diaryFile);
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.json.Music.create(musicFile);
    
    generateSite(music, diary, output, cssFile, javaScriptFile);
  }

  private void generateJSONFile(final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics, final String type, final String outputDir) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);

    JSONObject json = new JSONObject();
    try {
      if (type.equals("entries")) {
        json.put(type, com.bolsinga.diary.data.json.Diary.createEntriesJSON(diary.getEntries()));
      } else if (type.equals("venues")) {
        json.put(type, com.bolsinga.music.data.json.Music.createVenuesJSON(music.getVenues()));
      } else if (type.equals("artists")) {
        json.put(type, com.bolsinga.music.data.json.Music.createArtistsJSON(music.getArtists()));
      } else if (type.equals("labels")) {
        json.put(type, com.bolsinga.music.data.json.Music.createLabelsJSON(music.getLabels()));
      } else if (type.equals("relations")) {
        json.put(type, com.bolsinga.music.data.json.Music.createRelationsJSON(music.getRelations()));
      } else if (type.equals("songs")) {
        json.put(type, com.bolsinga.music.data.json.Music.createSongsJSON(music.getSongs()));
      } else if (type.equals("albums")) {
        json.put(type, com.bolsinga.music.data.json.Music.createAlbumsJSON(music.getAlbums()));
      } else if (type.equals("shows")) {
        json.put(type, com.bolsinga.music.data.json.Music.createShowsJSON(music.getShows()));
      } else {
        Main.usage(null, "Unknown JSON type: " + type);
      }
    } catch (JSONException e) {
      throw new com.bolsinga.web.WebException("Can't export music to json", e);
    }
    java.io.File result = new java.io.File(outputDir, type + ".json");
    com.bolsinga.web.Util.writeJSON(json, result.getPath());
  }

  private void dumpSimilarArtists(final com.bolsinga.music.data.Music music) {
    String s;
    HashSet<String> bands = new HashSet<String>();
    boolean displayed = false;
    
    List<? extends com.bolsinga.music.data.Artist> artists = music.getArtists();
    for (com.bolsinga.music.data.Artist artist : artists) {
      s = com.bolsinga.music.Compare.simplify(artist.getName());
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
  
  private void generateSite(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary, final String output, final String cssFile, final String javaScriptFile) throws Exception {
    CSS.install(cssFile, output);
    JavaScript.install(javaScriptFile, output);

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
    System.out.println("Usage: Main [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [settings.xml] [layout.css] [site.js] [diary input/output file] [music input/output file] [output.dir] <xml|xml-site|json|json-site|site>");
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
