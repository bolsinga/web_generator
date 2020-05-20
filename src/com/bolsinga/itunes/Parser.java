package com.bolsinga.itunes;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class Parser {
  private static final String TK_GENRE_VOICE_MEMO = "Voice Memo";

  private static final HashSet<String> sArtistNotAdded = new HashSet<String>();

  private static final Pattern sLTPattern = Pattern.compile("<");
  private static final String sLTReplacement = "&lt;";
  private static final Pattern sGTPattern = Pattern.compile(">");
  private static final String sGTReplacement = "&gt;";

  // ArtistName -> Artist
  private final HashMap<String, Artist> fArtistMap = new HashMap<String, Artist>();
  
  // AlbumKey -> Album
  private final HashMap<String, Album> fAlbumMap = new HashMap<String, Album>();
  
  // AlbumKey's
  private final HashSet<String> fArtistAlbumSet = new HashSet<String>();

  public List<Album> parse(final String itunesFile) throws ParserException {
    List<Track> tracks = createTracks(itunesFile);

    addTracks(tracks);

    setAlbumYears();
                
    sortAlbumOrder();

    sortAlbumsSongOrder();

    Track.report();

    if (sArtistNotAdded.size() > 0) {
		System.out.println("Artist not Added:");
		for (String artist : sArtistNotAdded) {
			System.out.println(artist);
		}
	}
    
    return Collections.unmodifiableList(new ArrayList<Album>(fAlbumMap.values()));
  }

  private void addTracks(final List<Track> tracks) {
    for (Track track : tracks) {
      addTrack(track);
    }
  }

  private void addTrack(final Track track) {
    boolean compilation = false;
    boolean isVideo = false;
    boolean isPodcast = false;
    boolean isAudioFile = track.isAudioKind();

    compilation = track.getCompilation() != null && "true".equals(track.getCompilation());

    isVideo = track.getHas_Video() != null && "true".equals(track.getHas_Video());

    isPodcast = track.getPodcast() != null && "true".equals(track.getPodcast());

    if (track.getAlbum() == null) {
      track.setAlbum(track.getName() + " - Single");
    }

    boolean isVoiceMemo = (track.getGenre() != null) && track.getGenre().equals(TK_GENRE_VOICE_MEMO);

    boolean isAlbum = !isVideo && !isPodcast && isAudioFile && !isVoiceMemo;
    if (!isAlbum) {
      sArtistNotAdded.add(track.getArtist());
    }

    if (isAlbum && (track.getArtist() != null) && !track.getAlbum().equals("Apple Financial Results")) {
      int year = (track.getYear() != null) ? Integer.parseInt(track.getYear()) : 0;
      int trackNumber = (track.getTrack_Number() != null) ? Integer.parseInt(track.getTrack_Number()) : 0;
      int playCount = (track.getPlay_Count() != null) ? Integer.parseInt(track.getPlay_Count()) : 0;
      GregorianCalendar lastPlayed = (track.getPlay_Date_UTC() != null) ? com.bolsinga.web.Util.fromJSONCalendar(track.getPlay_Date_UTC()) : null;
      createTrack(track.getArtist(), track.getSort_Artist(), track.getName(), track.getAlbum(), year, trackNumber, track.getGenre(), lastPlayed, playCount, compilation);
    }
  }
        
  private void createTrack(final String artistName, final String sortArtist, final String songTitle, final String albumTitle, final int year, final int index, final String genre, final GregorianCalendar lastPlayed, final int playCount, final boolean compilation) {
    // Get or create the artist
    if (!fArtistMap.containsKey(artistName)) {
      Artist artist = new Artist(artistName, sortArtist);
      fArtistMap.put(artistName, artist);
    }
    Artist artist = fArtistMap.get(artistName);
    if (artist.getSortname() != null && sortArtist == null) {
        System.out.println("Song: " + songTitle + " for: " + artistName + " is not sorted in iTunes.");
    }
                
    // Get or create the album.
    Album album = addAlbum(albumTitle, compilation ? null : artist);
                
    // The song is always the new item. The artist and album may already be known.
    addAlbumTrack(artist, album, songTitle, year, index, genre, lastPlayed, playCount);
  }
  
  private static String getAlbumKey(final String albumTitle, final Artist artist) {
    StringBuilder sb = new StringBuilder();
    sb.append(albumTitle);
    if (artist != null) {
      sb.append(artist.getName());
    }
    return sb.toString();
  }
  
  private static String getAlbumKey(final Album album) {
    return Parser.getAlbumKey(album.getTitle(), album.getArtist());
  }
  
  private Album addAlbum(final String name, final Artist artist) {
    String key = Parser.getAlbumKey(name, artist);    
    if (!fAlbumMap.containsKey(key)) {      
      Album album = new Album(name, artist);
      fAlbumMap.put(key, album);
    }
    return fAlbumMap.get(key);
  }

  private void addAlbumTrack(final Artist artist, final Album album, final String songTitle, final int year, final int index, final String genre, final GregorianCalendar lastPlayed, final int playCount) {
    // Create the song
    Song song = createSong(artist, songTitle, year, index, genre, lastPlayed, playCount);
            
    // Add the song to the album
    album.addSong(song);

    // Add the album to the artist if it isn't there already.
    String key = Parser.getAlbumKey(album.getTitle(), artist);
    if (!fArtistAlbumSet.contains(key)) {
      fArtistAlbumSet.add(key);
      if (artist != null) {
        artist.addAlbum(album);
      }
    }
  }
  
  private static String cleanHTML(final String s) {
    // This is strictly for the song "Bad Days <aurally excited version>".
    // This keeps 'bad' titles out of the XML for simplicity sake.
    return sGTPattern.matcher(sLTPattern.matcher(s).replaceAll(sLTReplacement)).replaceAll(sGTReplacement);
  }
  
  private Song createSong(final Artist artist, final String songTitle, final int year, final int index, final String genre, final GregorianCalendar lastPlayed, final int playCount) {
    String cleanTitle = Parser.cleanHTML(songTitle);
    int releaseYear = Album.UNKNOWN_YEAR;
    if (year != -1) {
      releaseYear = year;
    }
    int track = Song.UNKNOWN_TRACK;
    if (index != -1) {
      track = index;
    }
    return new Song(artist, cleanTitle, releaseYear, lastPlayed, track, genre, playCount);
  }

  private void sortAlbumOrder() {
    for (Artist a : fArtistMap.values()) {
      a.sortAlbums();
    }
  }

  private void sortAlbumsSongOrder() {
    for (Album a : fAlbumMap.values()) {
      a.sortSongs();
    }
  }
        
  private void setAlbumYears() {
    int albumYear, songYear;
    for (Album a : fAlbumMap.values()) {
      if (a.getReleaseYear() != Album.UNKNOWN_YEAR) {
        // The album already has a date; don't change it.
        break;
      }
                        
      albumYear = Album.UNKNOWN_YEAR;
      List<Song> songs = a.getSongs();
      for (Song song : songs) {
        songYear = song.getReleaseYear();
        if (albumYear == Album.UNKNOWN_YEAR) {
          albumYear = songYear;
        } else {
          if (songYear != albumYear) {
            albumYear = Album.UNKNOWN_YEAR;
            break;
          }
        }
      }
                        
      if (albumYear != Album.UNKNOWN_YEAR) {
        a.setReleaseYear(albumYear);
      }
    }
  }

  private static com.bolsinga.plist.data.Plist createPlist(final String sourceFile) throws ParserException {
    com.bolsinga.plist.data.Plist plist = null;

    InputStream is = null;
    try {
      try {
        is = new FileInputStream(sourceFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find plist file: ");
        sb.append(sourceFile);
        throw new ParserException(sb.toString(), e);
      }

      javax.xml.stream.XMLStreamReader xmlStreamReader = null;
      try {
	    javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newInstance();
	    xmlInputFactory.setProperty(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "http");
        xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
      } catch (javax.xml.stream.XMLStreamException e) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Can't create XML Reader: ");
	    sb.append(is);
	    throw new ParserException(sb.toString(), e);
	  }

      try {
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.plist.data");
        Unmarshaller u = jc.createUnmarshaller();

        plist = (com.bolsinga.plist.data.Plist)u.unmarshal(xmlStreamReader);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't unmarshal plist file: ");
        sb.append(sourceFile);
        throw new ParserException(sb.toString(), e);
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close plist file: ");
          sb.append(sourceFile);
          throw new ParserException(sb.toString(), e);
        }
      }
    }

    return plist;
  }

  private static List<Track> createTracks(final String sourceFile) throws ParserException {
    com.bolsinga.plist.data.Dict dict = createTracksDict(sourceFile);

    ArrayList<Track> tracks = new ArrayList<Track>();

    Iterator<Object> i = dict.getKeyAndArrayOrData().iterator();
    while (i.hasNext()) {
      Object key = i.next(); // key not used

      com.bolsinga.plist.data.Dict trackDict = (com.bolsinga.plist.data.Dict)i.next();
      Track track = createTrack(trackDict);
      tracks.add(track);
    }

    return tracks;
  }

  private static com.bolsinga.plist.data.Dict createTracksDict(final String sourceFile) throws ParserException {
    com.bolsinga.plist.data.Plist plist = createPlist(sourceFile);

    Iterator<Object> i = plist.getDict().getKeyAndArrayOrData().iterator();
    while (i.hasNext()) {
      JAXBElement<? extends Object> jo = (JAXBElement<? extends Object>)i.next();
      String key = (String)jo.getValue();
      if (key.equals("Tracks")) {
        com.bolsinga.plist.data.Dict dict = (com.bolsinga.plist.data.Dict)i.next();
        return dict;
      } else {
        Object o = i.next();
      }
    }
    throw new ParserException("No Tracks key in plist: " + plist.toString());
  }

  private static Track createTrack(final com.bolsinga.plist.data.Dict trackDict) throws ParserException {
    Iterator<Object> i = trackDict.getKeyAndArrayOrData().iterator();

    Track t = new Track();

    while (i.hasNext()) {
      JAXBElement<? extends Object> jokey = (JAXBElement<? extends Object>)i.next();
      String key = (String)jokey.getValue();

      // always pull off the value, it may be unused.
      JAXBElement<? extends Object> jovalue = (JAXBElement<? extends Object>)i.next();

      Object value = jovalue.getValue();
      String stringValue = null;
      if (value instanceof java.lang.String) {
        stringValue = (String)value;
      } else if (value instanceof java.lang.Number) {
        stringValue = String.valueOf((Number)value);
      } else if (value instanceof javax.xml.datatype.XMLGregorianCalendar) {
        GregorianCalendar gc = ((XMLGregorianCalendar)value).toGregorianCalendar();
        stringValue = com.bolsinga.web.Util.toJSONCalendar(gc);
      } else if (value instanceof java.lang.Object && ("true".equals(jovalue.getName().getLocalPart()) || "false".equals(jovalue.getName().getLocalPart()))) {
        stringValue = jovalue.getName().getLocalPart();
      } else {
        throw new ParserException("Unhandled JAXB Name: " + jovalue.getName() + " Value: " + value.toString() + " Type: " + jovalue.getDeclaredType().toString());
      }

      t.set(key, stringValue);
    }

    return t;
  }
}
