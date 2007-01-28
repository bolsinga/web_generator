package com.bolsinga.itunes.converter;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

import com.bolsinga.plist.*;
import com.bolsinga.music.data.xml.*;

public class ITunes {

  private static final String TK_ALBUM                = "Album";
  private static final String TK_ARTIST               = "Artist";
  private static final String TK_ARTWORK_COUNT        = "Artwork Count";
  private static final String TK_BIT_RATE             = "Bit Rate";
  private static final String TK_COMMENTS             = "Comments";
  private static final String TK_COMPILATION          = "Compilation";
  private static final String TK_COMPOSER             = "Composer";
  private static final String TK_DATE_ADDED           = "Date Added";
  private static final String TK_DATE_MODIFIED        = "Date Modified";
  private static final String TK_DISC_COUNT           = "Disc Count";
  private static final String TK_DISC_NUMBER          = "Disc Number";
  private static final String TK_FILE_CREATOR         = "File Creator";
  private static final String TK_FILE_FOLDER_COUNT    = "File Folder Count";
  private static final String TK_FILE_TYPE            = "File Type";
  private static final String TK_GENRE                = "Genre";
  private static final String TK_KIND                 = "Kind";
  private static final String TK_LIBRARY_FOLDER_COUNT = "Library Folder Count";
  private static final String TK_LOCATION             = "Location";
  private static final String TK_NAME                 = "Name";
  private static final String TK_PLAY_COUNT           = "Play Count";
  private static final String TK_PLAY_DATE            = "Play Date";
  private static final String TK_PLAY_DATE_UTC        = "Play Date UTC";
  private static final String TK_SAMPLE_RATE          = "Sample Rate";
  private static final String TK_SIZE                 = "Size";
  private static final String TK_TOTAL_TIME           = "Total Time";
  private static final String TK_TRACK_COUNT          = "Track Count";
  private static final String TK_TRACK_ID             = "Track ID";
  private static final String TK_TRACK_NUMBER         = "Track Number";
  private static final String TK_TRACK_TYPE           = "Track Type";
  private static final String TK_YEAR                 = "Year";
  private static final String TK_SEASON               = "Season";
  private static final String TK_PERSISTENT_ID        = "Persistent ID";
  private static final String TK_SERIES               = "Series";
  private static final String TK_EPISODE              = "Episode";
  private static final String TK_EPISODE_ORDER        = "Episode Order";
  private static final String TK_HAS_VIDEO            = "Has Video";
  private static final String TK_TV_SHOW              = "TV Show";
  private static final String TK_PROTECTED            = "Protected";
  private static final String TK_BPM                  = "BPM";
  private static final String TK_ALBUM_ARTIST         = "Album Artist";
  private static final String TK_EXPLICIT             = "Explicit";
  private static final String TK_SKIP_COUNT           = "Skip Count";
  private static final String TK_SKIP_DATE            = "Skip Date";
  private static final String TK_RELEASE_DATE         = "Release Date";
  private static final String TK_PODCAST              = "Podcast";
    
  private static final String FORMAT_12_INCH_LP       = "12 Inch LP";
  private static final String FORMAT_12_INCH_EP       = "12 Inch EP";
  private static final String FORMAT_12_INCH_SINGLE   = "12 Inch Single";
  private static final String FORMAT_10_INCH_EP       = "10 Inch EP";
  private static final String FORMAT_7_INCH_SINGLE    = "7 Inch Single";
  private static final String FORMAT_CASSETTE         = "Cassette";
  private static final String FORMAT_CD               = "CD";
  private static final String FORMAT_DIGITAL_FILE     = "Digital File";
    
  private static final HashMap<String, Album> sAlbums = new HashMap<String, Album>();

  private static final HashSet<String> sITunesKeys = new HashSet<String>();
  
  private static final HashMap<String, HashSet<String>> sArtistAlbums= new HashMap<String, HashSet<String>>();
  
  private static final Pattern sLTPattern = Pattern.compile("<");
  private static final String sLTReplacement = "&lt;";
  private static final Pattern sGTPattern = Pattern.compile(">");
  private static final String sGTReplacement = "&gt;";
          
  private static void createKnownKeys() {
    sITunesKeys.add(TK_ALBUM);
    sITunesKeys.add(TK_ARTIST);
    sITunesKeys.add(TK_ARTWORK_COUNT);
    sITunesKeys.add(TK_BIT_RATE);
    sITunesKeys.add(TK_COMMENTS);
    sITunesKeys.add(TK_COMPILATION);
    sITunesKeys.add(TK_COMPOSER);
    sITunesKeys.add(TK_DATE_ADDED);
    sITunesKeys.add(TK_DATE_MODIFIED);
    sITunesKeys.add(TK_DISC_COUNT);
    sITunesKeys.add(TK_DISC_NUMBER);
    sITunesKeys.add(TK_FILE_CREATOR);
    sITunesKeys.add(TK_FILE_FOLDER_COUNT);
    sITunesKeys.add(TK_FILE_TYPE);
    sITunesKeys.add(TK_GENRE);
    sITunesKeys.add(TK_KIND);
    sITunesKeys.add(TK_LIBRARY_FOLDER_COUNT);
    sITunesKeys.add(TK_LOCATION);
    sITunesKeys.add(TK_NAME);
    sITunesKeys.add(TK_PLAY_COUNT);
    sITunesKeys.add(TK_PLAY_DATE);
    sITunesKeys.add(TK_PLAY_DATE_UTC);
    sITunesKeys.add(TK_SAMPLE_RATE);
    sITunesKeys.add(TK_SIZE);
    sITunesKeys.add(TK_TOTAL_TIME);
    sITunesKeys.add(TK_TRACK_COUNT);
    sITunesKeys.add(TK_TRACK_ID);
    sITunesKeys.add(TK_TRACK_NUMBER);
    sITunesKeys.add(TK_TRACK_TYPE);
    sITunesKeys.add(TK_YEAR);
    sITunesKeys.add(TK_SEASON);
    sITunesKeys.add(TK_PERSISTENT_ID);
    sITunesKeys.add(TK_SERIES);
    sITunesKeys.add(TK_EPISODE);
    sITunesKeys.add(TK_EPISODE_ORDER);
    sITunesKeys.add(TK_HAS_VIDEO);
    sITunesKeys.add(TK_TV_SHOW);
    sITunesKeys.add(TK_PROTECTED);
    sITunesKeys.add(TK_BPM);
    sITunesKeys.add(TK_ALBUM_ARTIST);
    sITunesKeys.add(TK_EXPLICIT);
    sITunesKeys.add(TK_SKIP_COUNT);
    sITunesKeys.add(TK_SKIP_DATE);
    sITunesKeys.add(TK_RELEASE_DATE);
    sITunesKeys.add(TK_PODCAST);
  }
        
  public static void addMusic(final ObjectFactory objFactory, final Music music, final String itunesFile) throws ITunesException {
    // Create a list of all known iTunes keys. This way if a new one shows up, the program will let us know.
    createKnownKeys();

    com.bolsinga.plist.data.Plist plist = null;
    try {
      plist = Util.createPlist(itunesFile);
    } catch (com.bolsinga.plist.PlistException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't parse file: ");
      sb.append(itunesFile);
      throw new ITunesException(sb.toString(), e);
    }

    Iterator<Object> i = plist.getDict().getKeyAndArrayOrData().iterator();
    while (i.hasNext()) {
      JAXBElement<Object> jo = (JAXBElement<Object>)i.next();
      String key = (String)jo.getValue();
      if (key.equals("Tracks")) {
        com.bolsinga.plist.data.Dict dict = (com.bolsinga.plist.data.Dict)i.next();
                                
        List<Object> tracks = dict.getKeyAndArrayOrData();
        ITunes.addTracks(objFactory, music, tracks);
      } else {
        Object o = i.next();
      }
    }
  }
        
  private static void addTracks(final ObjectFactory objFactory, final Music music, final java.util.List<Object> tracks) {
    Iterator<Object> i = tracks.iterator();
    while (i.hasNext()) {
      Object key = i.next(); // key not used

      com.bolsinga.plist.data.Dict track = (com.bolsinga.plist.data.Dict)i.next();
      ITunes.addTrack(objFactory, music, track);
    }
                
    setAlbumYears(objFactory, music);
                
    sortAlbumOrder(music);

    sortAlbumsSongOrder(music);
  }
        
  private static void addTrack(final ObjectFactory objFactory, final Music music, final com.bolsinga.plist.data.Dict track) {
    Iterator<Object> i = track.getKeyAndArrayOrData().iterator();
            
    String songTitle = null;
    String artist = null;
    XMLGregorianCalendar lastPlayed = null;
    int playCount = 0;
    String genre = null;
    String albumTitle = null;
    int index = -1, year = -1;
    boolean compilation = false;
    boolean isVideo = false;
    boolean isPodcast = false;
            
    while (i.hasNext()) {
      JAXBElement<Object> jokey = (JAXBElement<Object>)i.next();
      String key = (String)jokey.getValue();

      // always pull off the value, it may be unused.
      JAXBElement<Object> jovalue = (JAXBElement<Object>)i.next();

      if (key.equals(TK_NAME)) {
        songTitle = (String)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_ARTIST)) {
        artist = (String)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_ALBUM)) {
        albumTitle = (String)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_GENRE)) {
        genre = (String)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_TRACK_NUMBER)) {
        index = ((java.math.BigInteger)jovalue.getValue()).intValue();
        continue;
      }
      if (key.equals(TK_YEAR)) {
        year = ((java.math.BigInteger)jovalue.getValue()).intValue();
        continue;
      }
      if (key.equals(TK_PLAY_DATE_UTC)) {
        lastPlayed = (XMLGregorianCalendar)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_PLAY_COUNT)) {
        playCount = ((java.math.BigInteger)jovalue.getValue()).intValue();
        continue;
      }
      if (key.equals(TK_COMPILATION)) {
        // Ignore the value, but it needs to be pulled.
        compilation = (jovalue != null);
        continue;
      }
      if (key.equals(TK_HAS_VIDEO)) {
        // Ignore the value, but it needs to be pulled.
        isVideo = (jovalue != null);
        continue;
      }
      if (key.equals(TK_PODCAST)) {
        // Ignore the value, but it needs to be pulled.
        isPodcast = (jovalue != null);
        continue;
      }

      if (!sITunesKeys.contains(key)) {
        System.out.println("iTunes added a new key: " + key);
      }
    }

    if (albumTitle == null) {
      albumTitle = songTitle + " - Single";
    }

    if (!isVideo && !isPodcast) {
      ITunes.createTrack(objFactory, music, artist, songTitle, albumTitle, year, index, genre, lastPlayed, playCount, compilation);
    }
  }
        
  private static void createTrack(final ObjectFactory objFactory, final Music music, final String artistName, final String songTitle, final String albumTitle, final int year, final int index, final String genre, final XMLGregorianCalendar lastPlayed, final int playCount, final boolean compilation) {
    // Get or create the artist
    Artist artist = com.bolsinga.shows.converter.Music.addArtist(objFactory, music, artistName);
                
    // Get or create the album.
    Album album = ITunes.addAlbum(objFactory, music, albumTitle, compilation ? null : artist);
                
    // The song is always the new item. The artist and album may already be known.
    ITunes.addAlbumTrack(objFactory, music, artist, album, songTitle, year, index, genre, lastPlayed, playCount);
  }
        
  private static Album addAlbum(final ObjectFactory objFactory, final Music music, final String name, final Artist artist) {
    Album result = null;
    StringBuilder keyBuffer = new StringBuilder();
    keyBuffer.append(name);
    if (artist != null) {
      keyBuffer.append(artist.getName());
    }
    String key = keyBuffer.toString();
    if (!sAlbums.containsKey(key)) {
      result = objFactory.createAlbum();
                        
      result.setTitle(name);
      if (artist != null) {
        result.setPerformer(artist);
      } else {
        result.setCompilation(true);
      }
      result.getFormat().add(objFactory.createAlbumFormat(FORMAT_DIGITAL_FILE));
      result.setId("a" + sAlbums.size());
                        
      music.getAlbum().add(result); // Modification required.
      sAlbums.put(key, result);
    } else {
      result = sAlbums.get(key);
    }
    return result;
  }

  private static void addAlbumTrack(final ObjectFactory objFactory, final Music music, final Artist artist, final Album album, final String songTitle, final int year, final int index, final String genre, final XMLGregorianCalendar lastPlayed, final int playCount) {
    // Create the song
    Song song = ITunes.createSong(objFactory, music, artist, songTitle, year, index, genre, lastPlayed, playCount);
            
    // Add the song to the album
    List<JAXBElement<Object>> songs = album.getSong(); // Modification required.
    songs.add(objFactory.createAlbumSong(song));
            
    // Add the album to the artist if it isn't there already.
    HashSet<String> artistAlbums = sArtistAlbums.get(artist.getId());
    if (artistAlbums == null) {
      artistAlbums = new HashSet<String>();
      sArtistAlbums.put(artist.getId(), artistAlbums);
    }
    if (!artistAlbums.contains(album.getId())) {
      artistAlbums.add(album.getId());
      JAXBElement<Object> jalbum = objFactory.createArtistAlbum(album);
      artist.getAlbum().add(jalbum); // Modification required.
    }
  }
  
  private static String cleanHTML(final String s) {
    // This is strictly for the song "Bad Days <aurally excited version>".
    // This keeps 'bad' titles out of the XML for simplicity sake.
    return sGTPattern.matcher(sLTPattern.matcher(s).replaceAll(sLTReplacement)).replaceAll(sGTReplacement);
  }
  
  private static Song createSong(final ObjectFactory objFactory, final Music music, final Artist artist, final String songTitle, final int year, final int index, final String genre, final XMLGregorianCalendar lastPlayed, final int playCount) {
    List<Song> songs = music.getSong(); // Modification required.
            
    Song result = null;
            
    result = objFactory.createSong();
    String cleanTitle = ITunes.cleanHTML(songTitle);
    result.setTitle(cleanTitle);
    result.setPerformer(artist);
    result.setLastPlayed(lastPlayed);
    result.setPlayCount(java.math.BigInteger.valueOf(playCount));
    result.setGenre(genre);
            
    if (year != -1) {
      result.setReleaseDate(releaseYear(objFactory, year));
    }
            
    if (index != -1) {
      result.setTrack(java.math.BigInteger.valueOf(index));
    }
                    
    result.setId("s" + songs.size());
    result.setDigitized(true);
            
    songs.add(result);
            
    return result;
  }

  private static com.bolsinga.music.data.xml.Date releaseYear(final ObjectFactory objFactory, final int year) {
    com.bolsinga.music.data.xml.Date release = objFactory.createDate();
    release.setUnknown(true);
    release.setYear(java.math.BigInteger.valueOf(year));
    return release;
  }
        
  private static void sortAlbumOrder(final Music music) {
    List<Artist> artists = com.bolsinga.web.Util.getArtistsUnmodifiable(music);
    for (Artist a : artists) {
      List<JAXBElement<Object>> albums = a.getAlbum(); // Modification required.
      Collections.sort(albums, com.bolsinga.music.Compare.JAXB_ALBUM_ORDER_COMPARATOR);
    }
  }

  private static void sortAlbumsSongOrder(final Music music) {
    List<Album> albums = com.bolsinga.web.Util.getAlbumsUnmodifiable(music);
    for (Album a : albums) {
      List<JAXBElement<Object>> songs = a.getSong(); // Modification required.
      Collections.sort(songs, com.bolsinga.music.Compare.JAXB_SONG_ORDER_COMPARATOR);
    }
  }
        
  private static void setAlbumYears(final ObjectFactory objFactory, final Music music) {
    List<Album> albums = com.bolsinga.web.Util.getAlbumsUnmodifiable(music);
    int albumYear, songYear;
    com.bolsinga.music.data.xml.Date date;
    for (Album a : albums) {
      if (a.getReleaseDate() != null) {
        // The album already has a date; don't change it.
        break;
      }
                        
      albumYear = -1;
      List<JAXBElement<Object>> songs = com.bolsinga.web.Util.getSongsUnmodifiable(a);
      for (JAXBElement<Object> song : songs) {
        date = ((Song)song.getValue()).getReleaseDate();
        if (date != null) {
          songYear = date.getYear().intValue();
          if (albumYear == -1) {
            albumYear = songYear;
          } else {
            if (songYear != albumYear) {
              albumYear = -1;
              break;
            }
          }
        } else {
          albumYear = -1;
        }
      }
                        
      if (albumYear != -1) {
        a.setReleaseDate(releaseYear(objFactory, albumYear));
      }
    }
  }
}
