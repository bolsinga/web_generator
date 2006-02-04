package com.bolsinga.music;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.bolsinga.music.data.*;

public class Util {

  private static DateFormat sMonthFormat      = new SimpleDateFormat("MMMM");
  public  static DateFormat sWebFormat        = new SimpleDateFormat("M/d/yyyy");
  private static DecimalFormat sPercentFormat = new DecimalFormat("##.##");
        
  public static Calendar toCalendar(com.bolsinga.music.data.Date date) {
    Calendar d = Calendar.getInstance();
    if (!date.isUnknown()) {
      d.clear();
      d.set(date.getYear().intValue(), date.getMonth().intValue() - 1, date.getDay().intValue());
    } else {
      System.err.println("Can't convert Unknown com.bolsinga.music.data.Date");
      System.exit(1);
    }
    return d;
  }

  public static String toString(com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sWebFormat.format(toCalendar(date).getTime());
    } else {
      Object[] args = {   ((date.getMonth() != null) ? date.getMonth() : BigInteger.ZERO),
                          ((date.getDay() != null) ? date.getDay() : BigInteger.ZERO),
                          ((date.getYear() != null) ? date.getYear() : BigInteger.ZERO) };
      return MessageFormat.format(com.bolsinga.web.Util.getResourceString("unknowndate"), args);
    }
  }
        
  public static String toMonth(com.bolsinga.music.data.Date date) {
    if (!date.isUnknown()) {
      return sMonthFormat.format(toCalendar(date).getTime());
    } else {
      Calendar d = Calendar.getInstance();
      if (date.getMonth() != null) {
        d.set(Calendar.MONTH, date.getMonth().intValue() - 1);
        return sMonthFormat.format(d.getTime());
      } else {
        return com.bolsinga.web.Util.getResourceString("unknownmonth");
      }
    }
  }
        
  public static String toString(double value) {
    return sPercentFormat.format(value);
  }

  public static Music createMusic(String sourceFile) {
    Music music = null;
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
      Unmarshaller u = jc.createUnmarshaller();
                        
      music = (Music)u.unmarshal(new java.io.FileInputStream(sourceFile));
    } catch (Exception ume) {
      System.err.println("Exception: " + ume);
      ume.printStackTrace();
      System.exit(1);
    }
    return music;
  }

  private static String toXMLID(String prefix, long sqlID) {
    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(sqlID - 1);
    return sb.toString();
  }

  private static Location createLocation(Connection conn, long locationID, ObjectFactory objFactory) throws SQLException, JAXBException {
    Location location = objFactory.createLocation();
    ResultSet rset = null;
    Statement stmt = null;
    StringBuffer sb = null;
    try {
      stmt = conn.createStatement();
      sb = new StringBuffer();
      sb.append("SELECT * FROM location WHERE id ='");
      sb.append(locationID);
      sb.append("';");
      rset = stmt.executeQuery(sb.toString());
      if (rset.first()) {
        location.setStreet(rset.getString("street"));
        location.setCity(rset.getString("city"));
        location.setState(rset.getString("state"));
        long zip = rset.getLong("zip");
        if (!rset.wasNull()) {
          location.setZip(BigInteger.valueOf(zip));
        }
        location.setWeb(rset.getString("url"));
      } else {
        System.err.println("Can't find Location ID: " + locationID);
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
      if (stmt != null) {
        stmt.close();
      }
    }
    return location;
  }

  private static HashMap sVenues = new HashMap();
  private static Object sVenuesLock = new Object();

  private static Venue getVenue(String xmlID, Music music, ObjectFactory objFactory) throws JAXBException {
    Venue item = null;
    synchronized (sVenuesLock) {
      if (sVenues.containsKey(xmlID)) {
        item = (Venue)sVenues.get(xmlID);
      }
    }
    if (item == null) {
      item = objFactory.createVenue();

      item.setId(xmlID);
      
      synchronized (sVenuesLock) {
        sVenues.put(xmlID, item);
      }

      music.getVenue().add(item);
    }
    return item;
  }

  private static void createVenues(Connection conn, Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Venue venue = null;

    rset = stmt.executeQuery("SELECT * FROM venue;");
    while (rset.next()) {
      String xmlID = Util.toXMLID("v", rset.getLong("id"));

      venue = Util.getVenue(xmlID, music, objFactory);
      
      venue.setName(rset.getString("name"));
      venue.setComment(rset.getString("comment"));
      long location_id = rset.getLong("location_id");
      if (!rset.wasNull()) {
        venue.setLocation(Util.createLocation(conn, location_id, objFactory));
      }
      boolean active = rset.getBoolean("active");
      if (!rset.wasNull()) {
        venue.setActive(active);
      }
    }
  }

  private static HashMap sArtists = new HashMap();
  private static Object sArtistsLock = new Object();

  private static Artist getArtist(String xmlID, Music music, ObjectFactory objFactory) throws JAXBException {
    Artist item = null;
    synchronized (sArtistsLock) {
      if (sArtists.containsKey(xmlID)) {
        item = (Artist)sArtists.get(xmlID);
      }
    }
    if (item == null) {
      item = objFactory.createArtist();

      item.setId(xmlID);
      
      music.getArtist().add(item);

      synchronized (sArtistsLock) {
        sArtists.put(xmlID, item);
      }
    }
    return item;
  }

  private static void createArtists(Connection conn, Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Artist artist = null;

    rset = stmt.executeQuery("SELECT * FROM artist;");
    while (rset.next()) {
      
      long artistID = rset.getLong("id");
      String xmlID = Util.toXMLID("ar", artistID);

      artist = Util.getArtist(xmlID, music, objFactory);

      artist.setName(rset.getString("name"));
      artist.setSortname(rset.getString("sortname"));
      artist.setComment(rset.getString("comment"));
      long location_id = rset.getLong("location_id");
      if (!rset.wasNull()) {
        artist.setLocation(Util.createLocation(conn, location_id, objFactory));
      }
      boolean active = rset.getBoolean("active");
      if (!rset.wasNull()) {
        artist.setActive(active);
      }
    }
  }

  private static HashMap sAlbums = new HashMap();
  private static Object sAlbumsLock = new Object();

  private static Album getAlbum(String xmlID, long album_id, Connection conn, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    Album item = null;
    synchronized (sAlbumsLock) {
      if (sAlbums.containsKey(xmlID)) {
        item = (Album)sAlbums.get(xmlID);
      }
    }
    if (item == null) {
      item = objFactory.createAlbum();

      item.setId(xmlID);

      Statement stmt = null;
      ResultSet rset = null;
      try {
        stmt = conn.createStatement();

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM album WHERE id=");
        sb.append(album_id);
        sb.append(";");
        
        rset = stmt.executeQuery(sb.toString());
        if (rset.first()) {
          item.setTitle(rset.getString("title"));
          boolean compilation = rset.getBoolean("compilation");
          if (!rset.wasNull()) {
            item.setCompilation(compilation);
          }
          item.setComment(rset.getString("comment"));
          // ToDo: label
        }

        sb = new StringBuffer();
        sb.append("SELECT album_id, COUNT(DISTINCT performer_id), performer_id, COUNT(DISTINCT producer_id), producer_id, COUNT(DISTINCT release), release, COUNT(DISTINCT purchase), purchase, COUNT(DISTINCT format), format FROM song WHERE album_id=");
        sb.append(album_id);
        sb.append(" GROUP BY album_id;");

        rset = stmt.executeQuery(sb.toString());
        if (rset.first()) {
          boolean distinct = (rset.getLong(2) == 1);
          if (distinct) {
            item.setPerformer(Util.getArtist(Util.toXMLID("ar", rset.getLong("performer_id")), music, objFactory));
          }
          distinct = (rset.getLong(4) == 1);
          if (distinct) {
            item.getProducer().add(Util.getArtist(Util.toXMLID("ar", rset.getLong("producer_id")), music, objFactory));
          }
          distinct = (rset.getLong(6) == 1);
          if (distinct) {
            byte[] sqlDateBytes = rset.getBytes("release");
            if (!rset.wasNull()) {
              item.setReleaseDate(Util.createDate(new String(sqlDateBytes), objFactory));
            }
          }
          distinct = (rset.getLong(8) == 1);
          if (distinct) {
            byte[] sqlDateBytes = rset.getBytes("purchase");
            if (!rset.wasNull()) {
              item.setPurchaseDate(Util.createDate(new String(sqlDateBytes), objFactory));
            }
          }
          distinct = (rset.getLong(10) == 1);
          if (distinct) {
            String formatSQLenum = rset.getString("format");
            String[] formats = formatSQLenum.split(",");
            for (int i = 0; i < formats.length; i++) {
              item.getFormat().add(formats[0]);
            }
          }
        }
      } finally {
        if (rset != null) {
          rset.close();
        }
        if (stmt != null) {
          stmt.close();
        }
      }
      
      synchronized (sAlbumsLock) {
        sAlbums.put(xmlID, item);
      }

      music.getAlbum().add(item);
    }
    return item;
  }

  // songs per album:
  // SELECT album_id, COUNT(*) FROM song GROUP BY album_id;
  
  // songs per artist:
  // SELECT performer_id, COUNT(*) FROM song GROUP BY performer_id;
  
  // songs per artist specifically
  // SELECT performer_id, COUNT(*) FROM song WHERE performer_id=101 GROUP BY performer_id;
  
  // albums per artist with track count
  // SELECT album_id, COUNT(*) FROM song WHERE performer_id=101 GROUP BY album_id;

  // # performers per album:
  // SELECT album_id, COUNT(DISTINCT performer_id) FROM song GROUP BY album_id;

  private static void createSongs(Connection conn, Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Artist artist = null;
    Album album = null;
    Song song = null;

    try {
      StringBuffer sb = new StringBuffer();
      sb.append("SELECT * FROM song ORDER BY release, album_id, track;");

      rset = stmt.executeQuery(sb.toString());
      while (rset.next()) {
        
        String perfID = Util.toXMLID("ar", rset.getLong("performer_id"));
        artist = Util.getArtist(perfID, music, objFactory);
        
        long album_id = rset.getLong("album_id");
        String albumXMLID = Util.toXMLID("a", album_id);
        album = Util.getAlbum(albumXMLID, album_id, conn, music, objFactory);

        List albums = artist.getAlbum();
        if (!albums.contains(album)) {
          albums.add(album);
        }
        
        String songID = Util.toXMLID("s", rset.getLong("id"));

        song = objFactory.createSong();

        song.setId(songID);
        song.setTitle(rset.getString("title"));
        song.setPerformer(artist);
        long composer_id = rset.getLong("composer_id");
        if (!rset.wasNull()) {
          song.getComposer().add(Util.getArtist(Util.toXMLID("ar", composer_id), music, objFactory));
        }
        long producer_id = rset.getLong("producer_id");
        if (!rset.wasNull()) {
          song.getProducer().add(Util.getArtist(Util.toXMLID("ar", producer_id), music, objFactory));
        }
        byte[] sqlDateBytes = rset.getBytes("release");
        com.bolsinga.music.data.Date releaseDate = null;
        if (!rset.wasNull()) {
          releaseDate = Util.createDate(new String(sqlDateBytes), objFactory);
          song.setReleaseDate(releaseDate);
        }
        java.sql.Timestamp lastPlayed = rset.getTimestamp("last_played");
        if (!rset.wasNull()) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(new java.util.Date(lastPlayed.getTime()));
          song.setLastPlayed(cal);
        }
        long playCount = rset.getLong("playcount");
        if (!rset.wasNull()) {
          song.setPlayCount(BigInteger.valueOf(playCount));
        }
        song.setGenre(rset.getString("genre"));
        long track = rset.getLong("track");
        if (!rset.wasNull()) {
          song.setTrack(BigInteger.valueOf(track));
        }
        String sqlFormat = rset.getString("format");
        if (sqlFormat.matches("Digital File")) {
          song.setDigitized(true);
        }

        album.getSong().add(song);
        music.getSong().add(song);
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private static void addPerformances(Connection conn, Show show, long performanceID) throws SQLException, JAXBException {
    Statement stmt = null;
    ResultSet rset = null;

    try {
      stmt = conn.createStatement();
      StringBuffer sb = new StringBuffer();
      sb.append("SELECT * FROM performance WHERE id = '");
      sb.append(performanceID);
      sb.append("' ORDER BY playorder;");
      rset = stmt.executeQuery(sb.toString());
      while (rset.next()) {
        String xmlID = Util.toXMLID("ar", rset.getLong("artist_id"));
        // Pass null since this artist already should have been created.
        Artist artist = Util.getArtist(xmlID, null, null);
        show.getArtist().add(artist);
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  private static com.bolsinga.music.data.Date createDate(String sqlDate, ObjectFactory objFactory) throws JAXBException {
    com.bolsinga.music.data.Date result = objFactory.createDate();

    String monthString, dayString, yearString = null;
    int month, day, year = 0;
                
    StringTokenizer st = new StringTokenizer(sqlDate, "-");
                
    yearString = st.nextToken();
    monthString = st.nextToken();
    dayString = st.nextToken();
                
    month = Integer.parseInt(monthString);
    day = Integer.parseInt(dayString);
    year = Integer.parseInt(yearString);
                
    if ((month == 0) || (day == 0) || (year == 1900)) {
      result.setUnknown(true);
    }

    if (month != 0) {
      result.setMonth(new java.math.BigInteger(monthString));
    }
                
    if (day != 0) {
      result.setDay(new java.math.BigInteger(dayString));
    }
                
    if (year != 1900) {
      result.setYear(new java.math.BigInteger(yearString));
    } 

    return result;
  }

  private static void createShows(Connection conn, Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Show show = null;

    rset = stmt.executeQuery("SELECT * FROM shows;");
    while (rset.next()) {
      show = objFactory.createShow();

      show.setId(Util.toXMLID("sh", rset.getLong("id")));

      String xmlID = Util.toXMLID("v", rset.getLong("venue_id"));
      Venue venue = Util.getVenue(xmlID, music, objFactory);
      show.setVenue(venue);

      addPerformances(conn, show, rset.getLong("performance_id"));

      // Need to use raw bytes for the date. MySQL allows 'illegal' dates that
      //  this program takes advantage of in the DB. Unfortunately, getting at
      //  these with java.sql.Date or even getting it as a java.lang.String
      //  will not work. Get the byte array, create the String and parse it
      //  ourselves.
      String sqlDate = new String(rset.getBytes("date"));
      show.setDate(Util.createDate(sqlDate, objFactory));

      show.setComment(rset.getString("comment"));

      music.getShow().add(show);
    }
  }

  private static void addMemberToRelation(Relation relation, String type, long id) throws JAXBException {
    if (type.equals("artist")) {
      relation.setType(type);
      String xmlID = Util.toXMLID("ar", id);
      // Pass null since this artist already should have been created.
      Artist artist = Util.getArtist(xmlID, null, null);
      if (artist == null) {
        System.err.println("Relation: Unknown Artist ID: " + id);
        System.exit(1);
      }
      relation.getMember().add(artist);
    } else if (type.equals("venue")) {
      relation.setType(type);
      String xmlID = Util.toXMLID("v", id);
      // Pass null since this venue already should have been created.
      Venue venue = Util.getVenue(xmlID, null, null);
      if (venue == null) {
        System.err.println("Relation: Unknown Venue ID: " + id);
        System.exit(1);
      }
      relation.getMember().add(venue);
    } else {
      System.err.println("Unknown Relation type: " + type);
    }
  }

  private static void createRelations(Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Relation relation = null;
    long relationID = -1, lastRelationID = -1;

    rset = stmt.executeQuery("SELECT * FROM relation ORDER BY id;");
    while (rset.next()) {
      relationID = rset.getLong("id");
      if (relationID != lastRelationID) {
        // Add the last relation
        if (relation != null) {
          music.getRelation().add(relation);
        }

        // Create a new relation
        relation = objFactory.createRelation();

        relation.setId(Util.toXMLID("r", relationID));
        String reason = rset.getString("reason");
        if (!rset.wasNull()) {
          relation.setReason(reason);
        }

        String type = rset.getString("type");
        long id = rset.getLong("related_id");
        addMemberToRelation(relation, type, id);

        lastRelationID = relationID;
      } else {
        String type = rset.getString("type");
        long id = rset.getLong("related_id");
        addMemberToRelation(relation, type, id);
      }
    }
    if (relation != null) {
      music.getRelation().add(relation);
    }
  }

  public static Music createMusic(String user, String password) {
    Music music = null;
    Connection conn = null;
    Statement stmt = null;

    ObjectFactory objFactory = new ObjectFactory();
    try {
      music = objFactory.createMusic();

      Class.forName("org.gjt.mm.mysql.Driver");

      conn = DriverManager.getConnection("jdbc:mysql:///music", user, password);
      stmt = conn.createStatement();

      Util.createArtists(conn, stmt, music, objFactory);

      Util.createSongs(conn, stmt, music, objFactory);
      
      Util.createVenues(conn, stmt, music, objFactory);

      Util.createShows(conn, stmt, music, objFactory);

      Util.createRelations(stmt, music, objFactory);

      music.setTimestamp(Calendar.getInstance());
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
      } catch (Exception e) {
        System.err.println("Exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }
    return music;
  }
        
  public static int trackCount(Artist artist) {
    int tracks = 0;
    List albums = artist.getAlbum();
    if (albums != null) {
      ListIterator i = albums.listIterator();
      while (i.hasNext()) {
        Album album = (Album)i.next();
        List songs = album.getSong();
        ListIterator si = songs.listIterator();
        while (si.hasNext()) {
          Song song = (Song)si.next();
          if (song.getPerformer().equals(artist)) {
            tracks++;
          }
        }
      }
    }
                
    return tracks;
  }
}
