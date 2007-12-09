package com.bolsinga.test;

import com.bolsinga.web.*;

import java.util.*;

import java.io.*;

public class XMLConversionTest implements Backgroundable {

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 13) {
      XMLConversionTest.usage(args, "Wrong number of arguments");
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
      XMLConversionTest main = new XMLConversionTest(backgrounder, settingsFile);
      if (command.equals("dump_direct")) {
        main.dumpDirect(diaryFile, musicFile, itunes, shows, venue, sort, relations, comments, statics);
      } else {
        XMLConversionTest.usage(args, "Invalid action");
      }
      main.complete();
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
    
  private XMLConversionTest(final Backgrounder backgrounder, final String settingsFile) throws WebException {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);

    Util.createSettings(settingsFile);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }
  
  private static final String sNull = "<null>";
  private static final String sLineSeparator = System.getProperty("line.separator");
  private static final java.text.DateFormat sDateFormat = new java.text.SimpleDateFormat();

  private void dumpDirect(final String diaryFile, final String musicFile, final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics) throws Exception {
    generateDirectXML(diaryFile, musicFile, itunes, shows, venue, sort, relations, comments, statics);
    dumpStripped(diaryFile, musicFile, "-direct");
  }
  
  private void dumpStripped(final String diaryFile, final String musicFile, final String dumpSuffix) throws Exception {
    dumpStrippedDiary(diaryFile, dumpSuffix);
    dumpStrippedMusic(musicFile, dumpSuffix);
  }
  
  private void dumpSafe(final Writer w, String s) throws Exception {
    if (s == null) {
      s = sNull;
    }
    w.write(s);
    w.write(sLineSeparator);
  }
  
  private void dumpStrippedDiary(final String diaryFile, final String dumpSuffix) throws Exception {
    final com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.xml.Diary.create(diaryFile);

    File f = new File(new File(diaryFile).getParent(), "diary-dump" + dumpSuffix + ".txt");

    Writer w = null;
    try {
      try {
        w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      } catch (UnsupportedEncodingException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't handle encoding UTF-8: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }      

      try {
        dumpSafe(w, diary.getTitle());
        dumpSafe(w, diary.getStatic());
        dumpSafe(w, diary.getHeader());
        dumpSafe(w, diary.getFriends());
        dumpSafe(w, diary.getColophon());

        for (com.bolsinga.diary.data.Entry entry : diary.getEntries()) {
          dumpSafe(w, sDateFormat.format(entry.getTimestamp().getTime()));
          dumpSafe(w, entry.getComment());
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't write file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Can't close file: ");
          sb.append(f);
          throw new WebException(sb.toString(), e);
        }
      }
    }
  }
  
  private void dumpLocation(final Writer w, final com.bolsinga.music.data.Location location) throws Exception {
    if (location == null) {
      dumpSafe(w, sNull);
      dumpSafe(w, sNull);
      dumpSafe(w, sNull);
      dumpSafe(w, sNull);
      dumpSafe(w, sNull);
    } else {
      dumpSafe(w, location.getStreet());
      dumpSafe(w, location.getCity());
      dumpSafe(w, location.getState());
      dumpSafe(w, Integer.toString(location.getZip()));
      dumpSafe(w, location.getWeb());
    }
  }
  
  private void dumpStrippedMusic(final String musicFile, final String dumpSuffix) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.xml.Music.create(musicFile);

    File f = new File(new File(musicFile).getParent(), "music-dump" + dumpSuffix + ".txt");

    Writer w = null;
    try {
      try {
        w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      } catch (UnsupportedEncodingException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't handle encoding UTF-8: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }      
      
      try {
        for (com.bolsinga.music.data.Venue venue : music.getVenues()) {
          dumpSafe(w, venue.getName());
          dumpLocation(w, venue.getLocation());
          dumpSafe(w, venue.getComment());
        }
        
        for (com.bolsinga.music.data.Artist artist : music.getArtists()) {
          dumpSafe(w, artist.getName());
          dumpSafe(w, artist.getSortname());
          dumpLocation(w, artist.getLocation());
          dumpSafe(w, artist.getComment());
          
          for (com.bolsinga.music.data.Album album : artist.getAlbums()) {
            dumpSafe(w, album.getTitle());
          }
        }
        
        for (com.bolsinga.music.data.Label label : music.getLabels()) {
          assert false : "Labels not supported";
        }
        
        for (com.bolsinga.music.data.Relation relation : music.getRelations()) {
          dumpSafe(w, relation.getReason());
          
          for (Object o : relation.getMembers()) {
            if (o instanceof com.bolsinga.music.data.Artist) {
              dumpSafe(w, ((com.bolsinga.music.data.Artist)o).getName());
            } else if (o instanceof com.bolsinga.music.data.Venue) {
              dumpSafe(w, ((com.bolsinga.music.data.Venue)o).getName());
            }
          }
        }
        
        for (com.bolsinga.music.data.Song song : music.getSongs()) {
          dumpSafe(w, song.getPerformer().getName());
          dumpSafe(w, song.getTitle());
          com.bolsinga.music.data.Date d = song.getReleaseDate();
          dumpSafe(w, (d != null) ? com.bolsinga.web.Util.toString(d) : null);
          GregorianCalendar lp = song.getLastPlayed();
          dumpSafe(w, (lp != null) ? sDateFormat.format(lp.getTime()) : null);
          dumpSafe(w, Integer.toString(song.getTrack()));
          dumpSafe(w, song.getGenre());
          dumpSafe(w, Integer.toString(song.getPlayCount()));
          dumpSafe(w, Boolean.toString(song.isDigitized()));
          dumpSafe(w, Boolean.toString(song.isLive()));
        }
        
        for (com.bolsinga.music.data.Album album : music.getAlbums()) {
          dumpSafe(w, album.getTitle());
          com.bolsinga.music.data.Artist artist = album.getPerformer();
          dumpSafe(w, (artist != null) ? artist.getName() : null);
          com.bolsinga.music.data.Date d = album.getReleaseDate();
          dumpSafe(w, (d != null) ? com.bolsinga.web.Util.toString(d) : null);
          d = album.getPurchaseDate();
          dumpSafe(w, (d != null) ? com.bolsinga.web.Util.toString(d) : null);
          dumpSafe(w, Boolean.toString(album.isCompilation()));
          StringBuilder sb = new StringBuilder();
          for (String format : album.getFormats()) {
            sb.append(format);
            sb.append(" ");
          }
          dumpSafe(w, sb.toString());
          // ignore label
          dumpSafe(w, album.getComment());
          for (com.bolsinga.music.data.Song song : album.getSongs()) {
            dumpSafe(w, song.getTitle());
          }
        }
        
        for (com.bolsinga.music.data.Show show : music.getShows()) {
          for (com.bolsinga.music.data.Artist artist : show.getArtists()) {
            dumpSafe(w, artist.getName());
          }
          
          com.bolsinga.music.data.Date d = show.getDate();
          dumpSafe(w, (d != null) ? com.bolsinga.web.Util.toString(d) : null);
          dumpSafe(w, show.getVenue().getName());
          dumpSafe(w, show.getComment());
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't write file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Can't close file: ");
          sb.append(f);
          throw new WebException(sb.toString(), e);
        }
      }
    }
  }
  
  private void generateDirectXML(final String diaryFile, final String musicFile, final String itunes, final String shows, final String venue, final String sort, final String relations, final String comments, final String statics) throws Exception {
    final com.bolsinga.music.data.Music music = com.bolsinga.music.data.raw.Music.create(shows, venue, sort, relations, itunes);
    com.bolsinga.music.data.xml.Music.export(music, musicFile);

    com.bolsinga.diary.data.Diary diary = com.bolsinga.diary.data.raw.Diary.create(comments, statics);
    com.bolsinga.diary.data.xml.Diary.export(diary, diaryFile);
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: XMLConversionTest [iTunes Music.xml] [shows.txt] [venuemap.txt] [bandsort.txt] [relations.txt] [comments.txt] [statics.txt] [diary.xml] [music.xml] [settings.xml] [layout.css] [output.dir] <dump_direct>");
    System.out.println(reason);
    System.out.println("Arguments:");
    for (int i = 0; i < badargs.length; i++) {
      System.out.print(badargs[i] + " ");
    }
    System.out.println();
    System.exit(0);
  }

}
