package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;

public class DBImporter {
  public static void main(String[] args) {
    if ((args.length != 3) && (args.length != 4)) {
      DBImporter.usage();
    }

    boolean clearDB = false;
    if (args.length == 4) {
      clearDB = args[3].equals("clear");
    }

    DBImporter.importData(args[0], args[1], args[2], clearDB);
  }

  private static void usage() {
    System.out.println("Usage: DBImporter [music.xml] [user] [password] <clear>");
    System.exit(0);
  }

  public static void importData(String sourceFile, String user, String password, boolean clearDB) {
    Music music = com.bolsinga.music.Util.createMusic(sourceFile);
    importData(music, user, password, clearDB);
  }

  public static void importData(Music music, String user, String password, boolean clearDB) {
    Connection conn = null;
    Statement stmt = null;
    try {
      // Load the driver class
      //
      Class.forName("org.gjt.mm.mysql.Driver");
      
      // Try to connect to the DB server.
      // We tell JDBC to use the "mysql" driver
      // and to connect to the "music" database
      //
      conn = DriverManager.getConnection("jdbc:mysql:///music?useUnicode=true&characterEncoding=utf-8", user, password);

      stmt = conn.createStatement();

      if (clearDB) {
        clearDB(stmt);
      }

      importVenues(stmt, music);

      importLabels(stmt, music);

      importArtists(stmt, music);

      importAlbums(stmt, music);

      // go through each show. Create a performance for each first.
      importShows(stmt, music);

      // go through the relations.
      importRelations(stmt, music);
    } catch (Exception e) {
      System.err.println("Exception: " + e);
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        System.err.println("Exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  private static void clearDB(Statement stmt) throws SQLException {
    com.bolsinga.sql.Util.truncate(stmt, "album");
    com.bolsinga.sql.Util.truncate(stmt, "artist");
    com.bolsinga.sql.Util.truncate(stmt, "label");
    com.bolsinga.sql.Util.truncate(stmt, "location");
    com.bolsinga.sql.Util.truncate(stmt, "performance");
    com.bolsinga.sql.Util.truncate(stmt, "relation");
    com.bolsinga.sql.Util.truncate(stmt, "shows");
    com.bolsinga.sql.Util.truncate(stmt, "song");
    com.bolsinga.sql.Util.truncate(stmt, "venue");
  }

  private static long sLocationID = 0;
  private static Object sLocationLock = new Object();

  private static long importLocation(Statement stmt, Location location) throws SQLException {
    String[] rowItems = new String[6];
    
    long locationID = -1;
    synchronized (sLocationLock) {
      locationID = ++sLocationID;
    }
    
    rowItems[0] = Long.toString(locationID);
    rowItems[1] = location.getStreet();
    rowItems[2] = location.getCity();
    rowItems[3] = location.getState();
    java.math.BigInteger zip = location.getZip();
    rowItems[4] = (zip != null) ? zip.toString() : null;
    rowItems[5] = location.getWeb();
    
    com.bolsinga.sql.Util.insert(stmt, "location", rowItems);
  
    return locationID;
  }

  private static String toSQLID(int index, String id) {
    return Long.toString(Long.valueOf(id.substring(index)).longValue() + 1);
  }

  private static String toSQLID(Label label) {
    // 'l'
    return toSQLID(1, label.getId());
  }

  private static void importLabel(Statement stmt, Label label) throws SQLException {
    Location location = (Location)label.getLocation();
    long locationID = -1;
    if (location != null) {
      try {
        locationID = DBImporter.importLocation(stmt, location);
      } catch (SQLException e) {
        System.err.println("SQLException importing location for: " + label.getName());
        throw e;
      }
    }
    
    String[] rowItems = new String[5];
    
    rowItems[0] = DBImporter.toSQLID(label);
    rowItems[1] = label.getName();
    rowItems[2] = (locationID != -1) ? Long.toString(locationID) : null;
    rowItems[3] = label.getComment();
    // The active state isn't tracked in the text files. Only
    //  use it coming out of the DB.
    //    rowItems[4] = Integer.toString((label.isActive() ? 1 : 0));
    rowItems[4] = null;
    
    com.bolsinga.sql.Util.insert(stmt, "label", rowItems);
  }

  private static void importLabels(Statement stmt, Music music) throws SQLException {
    List items = music.getLabel();
    Label item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Label)iterator.next();
      try {
        DBImporter.importLabel(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.getName());
        throw e;
      }
    }
  }

  private static String toSQLID(Song song) {
    // 's'
    return toSQLID(1, song.getId());
  }

  private static String toSQLenum(java.util.List strings) {
    StringBuffer sb = new StringBuffer();
    Iterator i = strings.iterator();
    while (i.hasNext()) {
      String s = (String)i.next();
      if (sb.length() != 0) {
        sb.append(",");
      }
      sb.append(s);
    }
    return sb.toString();
  }

  private static void importSong(Statement stmt, Song song, Album album) throws SQLException {
    String[] rowItems = new String[16];
    int index = 0;

    rowItems[index++] = DBImporter.toSQLID(song);
    rowItems[index++] = song.getTitle();
    Artist performer = (Artist)song.getPerformer();
    if (performer == null) {
      performer = (Artist)album.getPerformer();
    }
    rowItems[index++] = DBImporter.toSQLID(performer);
    //     Artist composer = (Artist)song.getComposer();
    //     rowItems[index++] = (composer != null) ? DBImporter.toSQLID(composer) : null;
    rowItems[index++] = null;
    //     Artist producer = (Artist)song.getProducer();
    //     if (producer == null) {
    //       producer = (Artist)album.getProducer();
    //     }
    //     rowItems[index++] = (producer != null) ? DBImporter.toSQLID(producer) : null;
    rowItems[index++] = null;
    com.bolsinga.music.data.Date releaseDate = song.getReleaseDate();
    if (releaseDate == null) {
      releaseDate = album.getReleaseDate();
    }
    rowItems[index++] = (releaseDate != null) ? DBImporter.toSQLString(releaseDate) : null;
    rowItems[index++] = Integer.toString(((releaseDate == null || releaseDate.isUnknown()) ? 1 : 0));
    com.bolsinga.music.data.Date purchaseDate = album.getPurchaseDate();
    rowItems[index++] = (purchaseDate != null) ? DBImporter.toSQLString(purchaseDate) : null;
    rowItems[index++] = Integer.toString(((purchaseDate == null || purchaseDate.isUnknown()) ? 1 : 0));
    rowItems[index++] = song.getGenre();
    java.math.BigInteger track = song.getTrack();
    rowItems[index++] = (track != null) ? track.toString() : null;
    Calendar lastPlayed = song.getLastPlayed();
    String lastPlayedString = null;
    if (lastPlayed != null) {
      lastPlayedString = com.bolsinga.sql.Util.toDATETIME(lastPlayed);
    }
    rowItems[index++] = lastPlayedString;
    // Live isn't currently tracked in the raw text files.
    //  Only use it coming out of the DB.
    //    rowItems[index++] = Integer.toString((song.isLive() ? 1 : 0));
    rowItems[index++] = null;
    rowItems[index++] = DBImporter.toSQLID(album);
    rowItems[index++] = DBImporter.toSQLenum(album.getFormat());
    rowItems[index++] = song.getPlayCount().toString();
    
    com.bolsinga.sql.Util.insert(stmt, "song", rowItems);
  }

  private static void importSongs(Statement stmt, Album album) throws SQLException {
    List items = album.getSong();
    Song item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Song)iterator.next();
      try {
        importSong(stmt, item, album);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.getTitle());
        throw e;
      }
    }
  }

  private static String toSQLID(Album album) {
    // 'a'
    return toSQLID(1, album.getId());
  }

  private static void importAlbum(Statement stmt, Album album) throws SQLException {
    String[] rowItems = new String[5];
    
    rowItems[0] = DBImporter.toSQLID(album);
    rowItems[1] = album.getTitle();
    Label label = (Label)album.getLabel();
    rowItems[2] = (label != null) ? DBImporter.toSQLID(label) : null;
    rowItems[3] = album.getComment();
    boolean isCompilation = album.isCompilation();
    rowItems[4] = (isCompilation) ? "1" : null;
    
    com.bolsinga.sql.Util.insert(stmt, "album", rowItems);

    importSongs(stmt, album);
  }

  private static void importAlbums(Statement stmt, Music music) throws SQLException {
    List items = music.getAlbum();
    Album item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Album)iterator.next();
      try {
        DBImporter.importAlbum(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.getTitle());
        throw e;
      }
    }
  }

  private static String toSQLID(Artist artist) {
    // 'ar'
    return toSQLID(2, artist.getId());
  }

  private static void importArtist(Statement stmt, Artist artist) throws SQLException {
    Location location = (Location)artist.getLocation();
    long locationID = -1;
    if (location != null) {
      try {
        locationID = DBImporter.importLocation(stmt, location);
      } catch (SQLException e) {
        System.err.println("SQLException importing location for: " + artist.getName());
        throw e;
      }
    }
    
    String[] rowItems = new String[6];
    
    rowItems[0] = DBImporter.toSQLID(artist);
    rowItems[1] = artist.getName();
    rowItems[2] = artist.getSortname();
    rowItems[3] = (locationID != -1) ? Long.toString(locationID) : null;
    rowItems[4] = artist.getComment();
    // The active state isn't tracked in the text files. Only
    //  use it coming out of the DB.
    //    rowItems[5] = Integer.toString((artist.isActive() ? 1 : 0));
    rowItems[5] = null;
    
    com.bolsinga.sql.Util.insert(stmt, "artist", rowItems);
  }

  private static void importArtists(Statement stmt, Music music) throws SQLException {
    List items = music.getArtist();
    Artist item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Artist)iterator.next();
      try {
        DBImporter.importArtist(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.getName());
        throw e;
      }
    }
  }

  private static String toSQLID(Venue venue) {
    // 'v'
    return toSQLID(1, venue.getId());
  }

  private static void importVenue(Statement stmt, Venue venue) throws SQLException {
    Location location = (Location)venue.getLocation();
    long locationID = -1;
    if (location != null) {
      try {
        locationID = DBImporter.importLocation(stmt, location);
      } catch (SQLException e) {
        System.err.println("SQLException importing location for: " + venue.getName());
        throw e;
      }
    }
    
    String[] rowItems = new String[5];
    
    rowItems[0] = DBImporter.toSQLID(venue);
    rowItems[1] = venue.getName();
    rowItems[2] = (locationID != -1) ? Long.toString(locationID) : null;
    rowItems[3] = venue.getComment();
    // The active state isn't tracked in the text files. Only
    //  use it coming out of the DB.
    //    rowItems[4] = Integer.toString((venue.isActive() ? 1 : 0));
    rowItems[4] = null;
    
    com.bolsinga.sql.Util.insert(stmt, "venue", rowItems);
  }

  private static void importVenues(Statement stmt, Music music) throws SQLException {
    List items = music.getVenue();
    Venue item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Venue)iterator.next();
      try {
        DBImporter.importVenue(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.getName());
        throw e;
      }
    }
  }

  private static long sPerformanceID = 0;
  private static Object sPerformanceLock = new Object();

  private static long importPerformance(Statement stmt, Show show) throws SQLException {
    List items = show.getArtist();
    Artist item = null;
    int playOrder = 1;

    String[] rowItems = new String[3];

    long performanceID = -1;
    synchronized (sPerformanceLock) {
      performanceID = ++sPerformanceID;
    }

    rowItems[0] = Long.toString(performanceID);
    
    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Artist)iterator.next();
      
      rowItems[1] = DBImporter.toSQLID(item);
      rowItems[2] = Integer.toString(playOrder++);
      
      com.bolsinga.sql.Util.insert(stmt, "performance", rowItems);
    }

    return performanceID;
  }

  private static String toSQLID(Show show) {
    // 'sh'
    return toSQLID(2, show.getId());
  }

  private static DateFormat sSQLFormat = new SimpleDateFormat("yyyy-M-d");
  private static MessageFormat sUnknownFormat = new MessageFormat("{2, number,####}-{0, number,integer}-{1, number,####}");

  private static String toSQLString(com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sSQLFormat.format(com.bolsinga.music.Util.toCalendar(date).getTime());
    } else {
      Object[] args = {   ((date.getMonth() != null) ? date.getMonth() : BigInteger.ZERO),
                          ((date.getDay() != null) ? date.getDay() : BigInteger.ZERO),
                          ((date.getYear() != null) ? date.getYear() : BigInteger.valueOf(1900)) };
      return sUnknownFormat.format(args);
    }
  }

  private static void importShow(Statement stmt, Show show) throws SQLException {
    long performanceID = DBImporter.importPerformance(stmt, show);
    
    String[] rowItems = new String[6];
    int index = 0;

    rowItems[index++] = DBImporter.toSQLID(show);
    com.bolsinga.music.data.Date showDate = show.getDate();
    rowItems[index++] = (showDate != null) ? DBImporter.toSQLString(showDate) : null;
    rowItems[index++] = Integer.toString(((showDate == null || showDate.isUnknown()) ? 1 : 0));
    rowItems[index++] = DBImporter.toSQLID((Venue)show.getVenue());
    rowItems[index++] = show.getComment();
    rowItems[index++] = Long.toString(performanceID);
    
    com.bolsinga.sql.Util.insert(stmt, "shows", rowItems);
  }

  private static void importShows(Statement stmt, Music music) throws SQLException {
    List items = music.getShow();
    Show item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Show)iterator.next();
      try {
        DBImporter.importShow(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.getDate());
        throw e;
      }
    }
  }

  private static long sRelationID = 0;
  private static Object sRelationLock = new Object();

  private static long importRelation(Statement stmt, Relation relation) throws SQLException {
    List items = relation.getMember();
    Object item = null;
    
    String[] rowItems = new String[4];
    
    long relationID = -1;
    synchronized (sRelationLock) {
      relationID = ++sRelationID;
    }
    
    rowItems[0] = Long.toString(relationID);
    
    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = iterator.next();
      if (item instanceof Artist) {
        rowItems[1] = DBImporter.toSQLID((Artist)item);
        rowItems[2] = "artist";
      } else if (item instanceof Venue) {
        rowItems[1] = DBImporter.toSQLID((Venue)item);
        rowItems[2] = "venue";
      } else {
        System.err.println("Unknown Relation: " + item);
      }

      rowItems[3] = relation.getReason();

      com.bolsinga.sql.Util.insert(stmt, "relation", rowItems);
    }
    
    return relationID;
  }

  private static void importRelations(Statement stmt, Music music) throws SQLException {
    List items = music.getRelation();
    Relation item = null;

    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Relation)iterator.next();
      try {
        DBImporter.importRelation(stmt, item);
      } catch (SQLException e) {
        System.err.println("SQLException importing: " + item.toString());
        throw e;
      }
    }
  }
}
