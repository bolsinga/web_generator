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
        location.setZip(BigInteger.valueOf(rset.getLong("zip")));
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

  private static void createVenues(Connection conn, Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Venue venue = null;

    rset = stmt.executeQuery("SELECT * FROM venue;");
    while (rset.next()) {
      venue = objFactory.createVenue();
      
      String xmlID = Util.toXMLID("v", rset.getLong("id"));
      venue.setId(xmlID);
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
      music.getVenue().add(venue);
      
      synchronized (sVenuesLock) {
        sVenues.put(xmlID, venue);
      }
    }
  }

  private static HashMap sArtists = new HashMap();
  private static Object sArtistsLock = new Object();

  private static void createArtists(Connection conn, Statement stmt, Music music, ObjectFactory objFactory) throws SQLException, JAXBException {
    ResultSet rset = null;
    Artist artist = null;

    rset = stmt.executeQuery("SELECT * FROM artist;");
    while (rset.next()) {
      artist = objFactory.createArtist();

      String xmlID = Util.toXMLID("ar", rset.getLong("id"));
      artist.setId(xmlID);
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

      music.getArtist().add(artist);
      
      synchronized (sArtistsLock) {
        sArtists.put(xmlID, artist);
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
        Artist artist = null;
        synchronized (sArtistsLock) {
          artist = (Artist)sArtists.get(xmlID);
        }
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
      Venue venue = null;
      synchronized (sVenuesLock) {
        venue = (Venue)sVenues.get(xmlID);
      }
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

  private static void addMemberToRelation(Relation relation, String type, long id) {
    if (type.equals("artist")) {
      relation.setType(type);
      String xmlID = Util.toXMLID("ar", id);
      Artist artist = null;
      synchronized (sArtistsLock) {
        artist = (Artist)sArtists.get(xmlID);
      }
      relation.getMember().add(artist);
    } else if (type.equals("venue")) {
      relation.setType(type);
      String xmlID = Util.toXMLID("v", id);
      Venue venue = null;
      synchronized (sVenuesLock) {
        venue = (Venue)sVenues.get(xmlID);
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
        music.getRelation().add(relation);

        // Create a new relation
        relation = objFactory.createRelation();

        relation.setId(Util.toXMLID("r", relationID));
        relation.setReason(rset.getString("reason"));

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
    music.getRelation().add(relation);
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
