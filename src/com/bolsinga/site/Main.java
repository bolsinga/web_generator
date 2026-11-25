package com.bolsinga.site;

import com.bolsinga.diary.*;
import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.util.*;

public class Main implements Backgroundable {

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    // Check the command and its arguments before anything is read.
    if (args.length == 0) {
      Main.usage(args, "No command.");
    } else if (args[0].equals("site")) {
      if (args.length != 11) {
        Main.usage(args, "site needs 10 arguments after the command, not " + (args.length - 1) + ".");
      }
    } else if (args[0].equals("json-site")) {
      if (args.length != 4) {
        Main.usage(args, "json-site needs 3 arguments after the command, not " + (args.length - 1) + ".");
      }
    } else {
      Main.usage(args, "Unknown command: " + args[0]);
    }

    int i = 0;
    String command = args[i++];
    String output = args[i++];
    String settingsFile = args[i++];

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    
    try {
      Main main = new Main(backgrounder, settingsFile);
      if (command.equals("site")) {
        String shows = args[i++];
        String venue = args[i++];
        String sort = args[i++];
        String relations = args[i++];
        String comments = args[i++];
        String statics = args[i++];
        String artistIDs = args[i++];
        String itunes = args[i++];

        main.generateDirect(itunes, shows, venue, sort, relations, comments, statics, artistIDs, output);
      } else {
        String jsonDir = args[i++];

        main.generateJSONSite(jsonDir, output);
      }
      main.complete();
    } catch (Throwable t) {
      // Throwable, not Exception: an Error here must still end the run, or the pool's threads keep the JVM alive.
      System.err.println(t);
      t.printStackTrace();
      System.exit(1);
    }
  }
    
  private Main(final Backgrounder backgrounder, final String settingsFile) throws WebException {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);

    Util.createSettings(settingsFile);
  }
  
  // Waits for every page to be written, and fails the run if any page failed.
  private void complete() throws InterruptedException, WebException {
    fBackgrounder.removeInterest(this);
    int failures = fBackgrounder.awaitTermination();
    if (failures > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(failures);
      sb.append(" page task(s) failed; see the errors above.");
      throw new WebException(sb.toString());
    }
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

  private void dumpMarkupWarnings(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary) {
    List<String> warnings = MarkupCheck.warnings(music, diary);
    if (!warnings.isEmpty()) {
      System.out.println("--Markup Warnings--");
      for (String warning : warnings) {
        System.out.println(warning);
      }
    }
  }

  private void dumpNameWarnings(final com.bolsinga.music.data.Music music) {
    List<String> warnings = NameCheck.warnings(music);
    if (!warnings.isEmpty()) {
      System.out.println("--Name Warnings--");
      for (String warning : warnings) {
        System.out.println(warning);
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

  // Every venue gets a page that lists its shows, so a venue with no shows is an error, from either reader.
  private void checkVenuesHaveShows(final com.bolsinga.music.data.Music music) throws com.bolsinga.web.WebException {
    Set<String> used = new HashSet<String>();
    for (com.bolsinga.music.data.Show show : music.getShows()) {
      used.add(show.getVenue().getID());
    }
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (com.bolsinga.music.data.Venue venue : music.getVenues()) {
      if (!used.contains(venue.getID())) {
        sb.append("\n  ");
        sb.append(venue.getID());
        sb.append(" ");
        sb.append(venue.getName());
        count++;
      }
    }
    if (count > 0) {
      sb.insert(0, " venue(s) with no shows:");
      sb.insert(0, count);
      throw new com.bolsinga.web.WebException(sb.toString());
    }
  }

  private void generateSite(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary, final String output) throws Exception {
    checkVenuesHaveShows(music);

    CSS.install(output);

    dumpSimilarArtists(music);
    dumpMarkupWarnings(music, diary);
    dumpNameWarnings(music);
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

  // Says what is wrong and how to run it, then fails the run.
  private static void usage(final String[] badargs, final String reason) {
    System.err.println(reason);
    System.err.println("Usage:");
    System.err.println("  Main site <output_dir> <settings.properties> <shows.txt> <venuemap.txt> <bandsort.txt> <relations.txt> <comments.txt> <statics.txt> <artistids.txt> <itunes.json>");
    System.err.println("  Main json-site <output_dir> <settings.properties> <json_dir>");
    System.err.println("json_dir holds the diary and music JSON, named by diaryJsonFile and musicJsonFile in the settings.");
    System.err.println("The site script runs these for you: site site <output_dir> <data_dir> <itunes.json>, or site json-site <output_dir> <settings.properties> <json_dir>.");
    if (badargs != null && badargs.length > 0) {
      System.err.println("Arguments given: " + String.join(" ", badargs));
    }
    System.exit(1);
  }

}
