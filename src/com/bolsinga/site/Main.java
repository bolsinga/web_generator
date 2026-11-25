package com.bolsinga.site;

import com.bolsinga.diary.*;
import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.util.*;

public class Main implements Backgroundable {

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length < 3) {
      Main.usage(args, "Initial: Wrong number of arguments: " + args.length);
    }

    int i = 0;
    String command = args[i++];
    String output = args[i++];
    String settingsFile = args[i++];

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    
    try {
      Main main = new Main(backgrounder, settingsFile);
      if (command.equals("site")) {
        if (args.length != 11) {
          Main.usage(args, "site: Wrong number of arguments: " + args.length);
        }

        String shows = args[i++];
        String venue = args[i++];
        String sort = args[i++];
        String relations = args[i++];
        String comments = args[i++];
        String statics = args[i++];
        String artistIDs = args[i++];
        String itunes = args[i++];

        main.generateDirect(itunes, shows, venue, sort, relations, comments, statics, artistIDs, output);
      } else if (command.equals("json-site")) {
        if (args.length != 4) {
          Main.usage(args, "json-site: Wrong number of arguments: " + args.length);
        }

        String jsonDir = args[i++];

        main.generateJSONSite(jsonDir, output);
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

  private void generateDirect(final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics, final String artistIDs, final String output) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes, artistIDs);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);

    generateSite(music, diary, output);
  }

  // Reads the files the export in generateSite writes, by the names in the settings.
  private void generateJSONSite(final String jsonDir, final String output) throws Exception {
    final java.io.File jsonDirectory = new java.io.File(jsonDir);
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.json.Diary.create(Main.getJSONFile(jsonDirectory, Util.getSettings().getDiaryJsonFile()).getPath());
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.json.Music.create(Main.getJSONFile(jsonDirectory, Util.getSettings().getMusicJsonFile()).getPath());

    generateSite(music, diary, output);
  }

  private static java.io.File getJSONFile(final java.io.File jsonDirectory, final String name) {
    return new java.io.File(jsonDirectory, name + com.bolsinga.web.Links.JSON_EXT);
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

  private boolean dumpMissingArtistIDs(final com.bolsinga.music.data.Music music) {
    List<? extends com.bolsinga.music.data.Artist> artists = music.getArtists();
    List<String> artistsMissingIDs = new ArrayList<String>();
    for (com.bolsinga.music.data.Artist artist : artists) {
      if (artist.getID() == null) {
        artistsMissingIDs.add(artist.getName());
      }
    }
    boolean hasMissingArtistIDs = artistsMissingIDs.size() != 0;
    if (hasMissingArtistIDs) {
      int nextID = artists.size() - artistsMissingIDs.size();
      System.out.println("--Artist Missing IDs--");
      for (String artistName : artistsMissingIDs) {
        System.out.println("ar" + nextID + "^" + artistName);
        nextID++;
      }
    }
    return hasMissingArtistIDs;
  }

  private void generateSite(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary, final String output) throws Exception {
    CSS.install(output);

    dumpSimilarArtists(music);
    if (dumpMissingArtistIDs(music)) {
      throw new com.bolsinga.web.WebException("Missing Artist IDs");
    }

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

      java.io.File musicJSON = Main.getJSONFile(jsonDirectory, com.bolsinga.web.Util.getSettings().getMusicJsonFile());
      com.bolsinga.music.data.json.Music.export(music, musicJSON.getAbsolutePath(), false);

      java.io.File showsJSON = Main.getJSONFile(jsonDirectory, com.bolsinga.web.Util.getSettings().getShowsJsonFile());
      com.bolsinga.music.data.json.Music.export(music, showsJSON.getAbsolutePath(), true);

      java.io.File diaryJSON = Main.getJSONFile(jsonDirectory, com.bolsinga.web.Util.getSettings().getDiaryJsonFile());
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
