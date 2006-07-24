package com.bolsinga.music;

import java.math.*;
import java.sql.*;
import java.util.*;

import javax.xml.bind.*;

import com.bolsinga.music.data.*;

class MySQLCreator {
  ObjectFactory objFactory;
  Music music;
  Connection conn;
  Statement stmt;

  PreparedStatement locationStmt;
  PreparedStatement albumStmt;
  PreparedStatement albumDistinctStmt;
  PreparedStatement performanceStmt;

  public MySQLCreator(ObjectFactory objFactory, Connection conn) {
    this.objFactory = objFactory;
    this.music = objFactory.createMusic();
    try {
      this.conn = conn;
      this.stmt = conn.createStatement();

      this.locationStmt = conn.prepareStatement("SELECT * FROM location WHERE id= ?;");
      this.albumStmt = conn.prepareStatement("SELECT * FROM album WHERE id= ?;");
      this.albumDistinctStmt = conn.prepareStatement("SELECT album_id, COUNT(DISTINCT performer_id), performer_id, COUNT(DISTINCT producer_id), producer_id, COUNT(DISTINCT release), release, release_vague, COUNT(DISTINCT purchase), purchase, purchase_vague, COUNT(DISTINCT format), format FROM song WHERE album_id= ? GROUP BY album_id;");
      this.performanceStmt = conn.prepareStatement("SELECT * FROM performance WHERE id= ? ORDER BY playorder;");
    } catch (SQLException se) {
      System.err.println("Exception: " + se);
      se.printStackTrace();
      System.exit(1);
    }
  }

  private String toXMLID(String prefix, long sqlID) {
    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(sqlID - 1);
    return sb.toString();
  }

  private com.bolsinga.music.data.Date createDate(String sqlDate, boolean dateVague) throws JAXBException {

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
    
    // Vague dates have broken when changing underlying MySQL releases with JDBC.
    // This is because JDBC doesn't handle vagues dates as MySQL supports.
    if (dateVague) {
      // JDBC / MySQL will mangle dates that aren't complete (1977-00-00) becomes
      // (1976-11-30). This handles this situation.
      if (year != 1900) {
        if ((month == 11) && (day == 30)) {
          year++;
          month = 0;
          day = 0;
        } else {
          // This case occurs when the month but not the day is known.
          month++;
          day = 0;
        }
      }
    }

    if ((month == 0) || (day == 0) || (year == 1900) || dateVague) {
      result.setUnknown(true);
    }
    
    if (month != 0) {
      result.setMonth(BigInteger.valueOf(month));
    }
    
    if (day != 0) {
      result.setDay(BigInteger.valueOf(day));
    }
    
    if (year != 1900) {
      result.setYear(BigInteger.valueOf(year));
    } 
    
    return result;
  }
  
  private Location createLocation(long locationID) throws SQLException, JAXBException {
    Location location = objFactory.createLocation();
    ResultSet rset = null;
    try {
      locationStmt.setLong(1, locationID);
      rset = locationStmt.executeQuery();
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
    }
    return location;
  }

  private HashMap<String, Artist> artists = new HashMap<String, Artist>();
  private Object artistsLock = new Object();

  private Artist getArtist(String xmlID) throws JAXBException {
    Artist item = null;

    synchronized (artistsLock) {
      if (artists.containsKey(xmlID)) {
        item = artists.get(xmlID);
        return item;
      }
    }

    item = objFactory.createArtist();
    
    item.setId(xmlID);
    
    music.getArtist().add(item);
    
    synchronized (artistsLock) {
      artists.put(xmlID, item);
    }
    return item;
  }

  private HashMap<String, Album> albums = new HashMap<String, Album>();
  private Object albumsLock = new Object();

  private Album getAlbum(String xmlID, long album_id) throws SQLException, JAXBException {
    Album item = null;
    synchronized (albumsLock) {
      if (albums.containsKey(xmlID)) {
        item = albums.get(xmlID);
        return item;
      }
    }
    
    item = objFactory.createAlbum();
    
    item.setId(xmlID);

    ResultSet rset = null;
    try {
      albumStmt.setLong(1, album_id);
      rset = albumStmt.executeQuery();
      if (rset.first()) {
        item.setTitle(rset.getString("title"));
        boolean compilation = rset.getBoolean("compilation");
        if (!rset.wasNull()) {
          item.setCompilation(compilation);
        }
        item.setComment(rset.getString("comment"));
        // ToDo: label
      }
      
      albumDistinctStmt.setLong(1, album_id);
      rset = albumDistinctStmt.executeQuery();
      if (rset.first()) {
        boolean distinct = (rset.getLong(2) == 1);
        if (distinct) {
          item.setPerformer(getArtist(toXMLID("ar", rset.getLong("performer_id"))));
          }
        distinct = (rset.getLong(4) == 1);
        if (distinct) {
          item.getProducer().add(objFactory.createAlbumProducer(getArtist(toXMLID("ar", rset.getLong("producer_id")))));
        }
        distinct = (rset.getLong(6) == 1);
        if (distinct) {
          String sqlDate = rset.getString("release");
          if (!rset.wasNull()) {
            boolean dateVague = (rset.getInt("release_vague") == 1);
            item.setReleaseDate(createDate(sqlDate, dateVague));
          }
        }
        distinct = (rset.getLong(9) == 1);
        if (distinct) {
          String sqlDate = rset.getString("purchase");
          if (!rset.wasNull()) {
            boolean dateVague = (rset.getInt("purchase_vague") == 1);
            item.setPurchaseDate(createDate(sqlDate, dateVague));
          }
        }
        distinct = (rset.getLong(12) == 1);
        if (distinct) {
          String formatSQLenum = rset.getString("format");
          String[] formats = formatSQLenum.split(",");
          for (int i = 0; i < formats.length; i++) {
            item.getFormat().add(objFactory.createAlbumFormat(formats[0]));
          }
        }
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
    
    synchronized (albumsLock) {
      albums.put(xmlID, item);
    }
    
    music.getAlbum().add(item);
    
    return item;
  }

  private HashMap<String, Venue> venues = new HashMap<String, Venue>();
  private Object venuesLock = new Object();

  private Venue getVenue(String xmlID) throws JAXBException {
    Venue item = null;
    synchronized (venuesLock) {
      if (venues.containsKey(xmlID)) {
        item = venues.get(xmlID);
        return item;
      }
    }

    item = objFactory.createVenue();
    
    item.setId(xmlID);
    
    synchronized (venuesLock) {
      venues.put(xmlID, item);
    }
    
    music.getVenue().add(item);
  
    return item;
  }

  private void addPerformances(Show show, long performanceID) throws SQLException, JAXBException {
    ResultSet rset = null;

    try {
      performanceStmt.setLong(1, performanceID);
      rset = performanceStmt.executeQuery();
      while (rset.next()) {
        String xmlID = toXMLID("ar", rset.getLong("artist_id"));
        Artist artist = getArtist(xmlID);
        show.getArtist().add(objFactory.createShowArtist(artist));
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private void addMemberToRelation(Relation relation, String type, long id) throws JAXBException {
    if (type.equals("artist")) {
      relation.setType(RelationType.fromValue(type));
      String xmlID = toXMLID("ar", id);
      Artist artist = getArtist(xmlID);
      if (artist == null) {
        System.err.println("Relation: Unknown Artist ID: " + id);
        System.exit(1);
      }
      relation.getMember().add(objFactory.createRelationMember(artist));
    } else if (type.equals("venue")) {
      relation.setType(RelationType.fromValue(type));
      String xmlID = toXMLID("v", id);
      Venue venue = getVenue(xmlID);
      if (venue == null) {
        System.err.println("Relation: Unknown Venue ID: " + id);
        System.exit(1);
      }
      relation.getMember().add(objFactory.createRelationMember(venue));
    } else {
      System.err.println("Unknown Relation type: " + type);
    }
  }

  private void createArtists() throws SQLException, JAXBException {
    ResultSet rset = null;
    Artist artist = null;

    try {
      rset = stmt.executeQuery("SELECT * FROM artist;");
      while (rset.next()) {
        
        long artistID = rset.getLong("id");
        String xmlID = toXMLID("ar", artistID);
        
        artist = getArtist(xmlID);
        
        artist.setName(rset.getString("name"));
        artist.setSortname(rset.getString("sortname"));
        artist.setComment(rset.getString("comment"));
        long location_id = rset.getLong("location_id");
        if (!rset.wasNull()) {
          artist.setLocation(createLocation(location_id));
        }
        boolean active = rset.getBoolean("active");
        if (!rset.wasNull()) {
          artist.setActive(active);
        }
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }

  }

  private static final HashMap<String, HashSet<String>> sArtistAlbums= new HashMap<String, HashSet<String>>();

  private void createSongs() throws SQLException, JAXBException {
    ResultSet rset = null;
    Artist artist = null;
    Album album = null;
    Song song = null;

    try {
      StringBuffer sb = new StringBuffer();
      sb.append("SELECT * FROM song ORDER BY track, album_id;");

      rset = stmt.executeQuery(sb.toString());
      while (rset.next()) {
        
        String perfID = toXMLID("ar", rset.getLong("performer_id"));
        artist = getArtist(perfID);
        
        long album_id = rset.getLong("album_id");
        String albumXMLID = toXMLID("a", album_id);
        album = getAlbum(albumXMLID, album_id);

        // Add the album to the artist if it isn't there already.
        HashSet<String> artistAlbums = sArtistAlbums.get(artist.getId());
        if (artistAlbums == null) {
          artistAlbums = new HashSet<String>();
          sArtistAlbums.put(artist.getId(), artistAlbums);
        }
        if (!artistAlbums.contains(album.getId())) {
          artistAlbums.add(album.getId());
          JAXBElement<Object> jalbum = objFactory.createArtistAlbum(album);
          artist.getAlbum().add(jalbum);
        }

        String songID = toXMLID("s", rset.getLong("id"));

        song = objFactory.createSong();

        song.setId(songID);
        song.setTitle(rset.getString("title"));
        song.setPerformer(artist);
        long composer_id = rset.getLong("composer_id");
        if (!rset.wasNull()) {
          song.getComposer().add(objFactory.createSongComposer(getArtist(toXMLID("ar", composer_id))));
        }
        long producer_id = rset.getLong("producer_id");
        if (!rset.wasNull()) {
          song.getProducer().add(objFactory.createSongProducer(getArtist(toXMLID("ar", producer_id))));
        }
        String sqlDate = rset.getString("release");
        com.bolsinga.music.data.Date releaseDate = null;
        if (!rset.wasNull()) {
          boolean dateVague = (rset.getInt("release_vague") == 1);
          releaseDate = createDate(sqlDate, dateVague);
          song.setReleaseDate(releaseDate);
        }
        String sqlDATETIME = rset.getString("last_played");
        if (!rset.wasNull()) {
          GregorianCalendar utcCal = com.bolsinga.sql.Util.toCalendarUTC(sqlDATETIME);
          song.setLastPlayed(com.bolsinga.web.Util.toXMLGregorianCalendar(utcCal));
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

        album.getSong().add(objFactory.createAlbumSong(song));
        music.getSong().add(song);
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private void createVenues() throws SQLException, JAXBException {
    ResultSet rset = null;
    Venue venue = null;
    
    try {
      rset = stmt.executeQuery("SELECT * FROM venue;");
      while (rset.next()) {
        String xmlID = toXMLID("v", rset.getLong("id"));
        
        venue = getVenue(xmlID);
        
        venue.setName(rset.getString("name"));
        venue.setComment(rset.getString("comment"));
        long location_id = rset.getLong("location_id");
        if (!rset.wasNull()) {
          venue.setLocation(createLocation(location_id));
        }
        boolean active = rset.getBoolean("active");
        if (!rset.wasNull()) {
          venue.setActive(active);
        }
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private void createShows() throws SQLException, JAXBException {
    ResultSet rset = null;
    Show show = null;

    try {
      rset = stmt.executeQuery("SELECT * FROM shows;");
      while (rset.next()) {
        show = objFactory.createShow();
        
        show.setId(toXMLID("sh", rset.getLong("id")));
        
        String xmlID = toXMLID("v", rset.getLong("venue_id"));
        Venue venue = getVenue(xmlID);
        show.setVenue(venue);
        
        addPerformances(show, rset.getLong("performance_id"));

        String sqlDate = rset.getString("date");
        boolean dateVague = (rset.getInt("date_vague") == 1);
        show.setDate(createDate(sqlDate, dateVague));
        
        show.setComment(rset.getString("comment"));
        
        music.getShow().add(show);
      }
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  private void createRelations() throws SQLException, JAXBException {
    ResultSet rset = null;
    Relation relation = null;
    long relationID = -1, lastRelationID = -1;

    try {
      rset = stmt.executeQuery("SELECT * FROM relation ORDER BY id, related_id;");
      while (rset.next()) {
        relationID = rset.getLong("id");
        if (relationID != lastRelationID) {
          // Add the last relation
          if (relation != null) {
            music.getRelation().add(relation);
          }
          
          // Create a new relation
          relation = objFactory.createRelation();
          
          relation.setId(toXMLID("r", relationID));
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
    } finally {
      if (rset != null) {
        rset.close();
      }
    }
  }

  public Music createMusic() throws SQLException, JAXBException {
    createArtists();
    createSongs();
    createVenues();
    createShows();
    createRelations();

    music.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(com.bolsinga.web.Util.nowUTC()));

    return music;
  }

  public void close() {
    try {
      if (stmt != null) {
        stmt.close();
      }
      if (locationStmt != null) {
        locationStmt.close();
      }
      if (albumStmt != null) {
        albumStmt.close();
      }
      if (albumDistinctStmt != null) {
        albumDistinctStmt.close();
      }
      if (performanceStmt != null) {
        performanceStmt.close();
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
