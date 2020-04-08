package com.bolsinga.itunes;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class Parser {
  private static final String TK_ALBUM                 = "Album";
  private static final String TK_ARTIST                = "Artist";
  private static final String TK_ARTWORK_COUNT         = "Artwork Count";
  private static final String TK_BIT_RATE              = "Bit Rate";
  private static final String TK_COMMENTS              = "Comments";
  private static final String TK_COMPILATION           = "Compilation";
  private static final String TK_COMPOSER              = "Composer";
  private static final String TK_DATE_ADDED            = "Date Added";
  private static final String TK_DATE_MODIFIED         = "Date Modified";
  private static final String TK_DISC_COUNT            = "Disc Count";
  private static final String TK_DISC_NUMBER           = "Disc Number";
  private static final String TK_FILE_CREATOR          = "File Creator";
  private static final String TK_FILE_FOLDER_COUNT     = "File Folder Count";
  private static final String TK_FILE_TYPE             = "File Type";
  private static final String TK_GENRE                 = "Genre";
  private static final String TK_KIND                  = "Kind";
  private static final String TK_LIBRARY_FOLDER_COUNT  = "Library Folder Count";
  private static final String TK_LOCATION              = "Location";
  private static final String TK_NAME                  = "Name";
  private static final String TK_PLAY_COUNT            = "Play Count";
  private static final String TK_PLAY_DATE             = "Play Date";
  private static final String TK_PLAY_DATE_UTC         = "Play Date UTC";
  private static final String TK_SAMPLE_RATE           = "Sample Rate";
  private static final String TK_SIZE                  = "Size";
  private static final String TK_TOTAL_TIME            = "Total Time";
  private static final String TK_TRACK_COUNT           = "Track Count";
  private static final String TK_TRACK_ID              = "Track ID";
  private static final String TK_TRACK_NUMBER          = "Track Number";
  private static final String TK_TRACK_TYPE            = "Track Type";
  private static final String TK_YEAR                  = "Year";
  private static final String TK_SEASON                = "Season";
  private static final String TK_PERSISTENT_ID         = "Persistent ID";
  private static final String TK_SERIES                = "Series";
  private static final String TK_EPISODE               = "Episode";
  private static final String TK_EPISODE_ORDER         = "Episode Order";
  private static final String TK_HAS_VIDEO             = "Has Video";
  private static final String TK_TV_SHOW               = "TV Show";
  private static final String TK_PROTECTED             = "Protected";
  private static final String TK_BPM                   = "BPM";
  private static final String TK_ALBUM_ARTIST          = "Album Artist";
  private static final String TK_EXPLICIT              = "Explicit";
  private static final String TK_SKIP_COUNT            = "Skip Count";
  private static final String TK_SKIP_DATE             = "Skip Date";
  private static final String TK_RELEASE_DATE          = "Release Date";
  private static final String TK_PODCAST               = "Podcast";
  private static final String TK_MOVIE                 = "Movie";
  private static final String TK_UNPLAYED              = "Unplayed";
  private static final String TK_SORT_ALBUM            = "Sort Album";
  private static final String TK_SORT_ALBUM_ARTIST     = "Sort Album Artist";
  private static final String TK_SORT_ARTIST           = "Sort Artist";
  private static final String TK_SORT_COMPOSER         = "Sort Composer";
  private static final String TK_SORT_NAME             = "Sort Name";
  private static final String TK_CONTENT_RATING        = "Content Rating";
  private static final String TK_DISABLED              = "Disabled";
  private static final String TK_PURCHASED             = "Purchased";
  private static final String TK_VIDEO_HEIGHT          = "Video Height";
  private static final String TK_VIDEO_WIDTH           = "Video Width";
  private static final String TK_EID                   = "EID";
  private static final String TK_HD                    = "HD";
  private static final String TK_ALBUM_RATING          = "Album Rating";
  private static final String TK_ALBUM_RATING_COMPUTED = "Album Rating Computed";
  private static final String TK_RATING                = "Rating";
  private static final String TK_GROUPING              = "Grouping";
  private static final String TK_PART_OF_GAPLESS_ALBUM = "Part Of Gapless Album";
  private static final String TK_MUSIC_VIDEO           = "Music Video";
  private static final String TK_MATCH_ID              = "MatchID";
  private static final String TK_XID                   = "XID";
  private static final String TK_RATING_COMPUTED       = "Rating Computed";
  private static final String TK_SORT_SERIES           = "Sort Series";

  private static final String TKIND_AAC_AUDIO_FILE = "AAC audio file";
  private static final String TKIND_BOOK = "Book";
  private static final String TKIND_INTERNET_AUDIO_STREAM = "Internet audio stream";
  private static final String TKIND_MPEG_AUDIO_FILE = "MPEG audio file";
  private static final String TKIND_MPEG_4_VIDEO_FILE = "MPEG-4 video file";
  private static final String TKIND_PDF_DOCUMENT = "PDF document";
  private static final String TKIND_PROTECTED_AAC_AUDIO_FILE = "Protected AAC audio file";
  private static final String TKIND_PROTECTED_MPEG_4_VIDEO_FILE = "Protected MPEG-4 video file";
  private static final String TKIND_PROTECTED_BOOK = "Protected book";
  private static final String TKIND_PURCHASED_AAC_AUDIO_FILE = "Purchased AAC audio file";
  private static final String TKIND_PURCHASED_MPEG_4_VIDEO_FILE = "Purchased MPEG-4 video file";
  private static final String TKIND_PURCHASED_BOOK = "Purchased book";
  private static final String TKIND_IPAD_APP = "iPad app";
  private static final String TKIND_IPHONE_IPOD_TOUCH_APP = "iPhone/iPod touch app";
  private static final String TKIND_IPHONE_IPOD_TOUCH_IPAD_APP = "iPhone/iPod touch/iPad app";
  private static final String TKIND_ITUNES_EXTRAS = "iTunes Extras";
  private static final String TKIND_MPEG_AUDIO_STREAM = "MPEG audio stream";

  private static final String TK_GENRE_VOICE_MEMO = "Voice Memo";


  private static final HashSet<String> sITunesKeys = new HashSet<String>();
  private static final Set<String> sNewITunesKeys = new TreeSet<String>();

  private static final HashSet<String> sITunesKinds = new HashSet<String>();
  private static final Set<String> sNewITunesKinds = new TreeSet<String>();
  private static final HashSet<String> sITunesAudioKinds = new HashSet<String>();
  
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
    sITunesKeys.add(TK_MOVIE);
    sITunesKeys.add(TK_UNPLAYED);
    sITunesKeys.add(TK_SORT_ALBUM);
    sITunesKeys.add(TK_SORT_ALBUM_ARTIST);
    sITunesKeys.add(TK_SORT_ARTIST);
    sITunesKeys.add(TK_SORT_COMPOSER);
    sITunesKeys.add(TK_SORT_NAME);
    sITunesKeys.add(TK_CONTENT_RATING);
    sITunesKeys.add(TK_DISABLED);
    sITunesKeys.add(TK_PURCHASED);
    sITunesKeys.add(TK_VIDEO_HEIGHT);
    sITunesKeys.add(TK_VIDEO_WIDTH);
    sITunesKeys.add(TK_EID);
    sITunesKeys.add(TK_HD);
    sITunesKeys.add(TK_ALBUM_RATING);
    sITunesKeys.add(TK_ALBUM_RATING_COMPUTED);
    sITunesKeys.add(TK_RATING);
    sITunesKeys.add(TK_GROUPING);
    sITunesKeys.add(TK_PART_OF_GAPLESS_ALBUM);
    sITunesKeys.add(TK_MUSIC_VIDEO);
    sITunesKeys.add(TK_MATCH_ID);
    sITunesKeys.add(TK_XID);
	sITunesKeys.add(TK_RATING_COMPUTED);
	sITunesKeys.add(TK_SORT_SERIES);
  }

  private static void createKnownKinds() {
	  sITunesKinds.add(TKIND_AAC_AUDIO_FILE);
	  sITunesKinds.add(TKIND_BOOK);
	  sITunesKinds.add(TKIND_INTERNET_AUDIO_STREAM);
	  sITunesKinds.add(TKIND_MPEG_AUDIO_FILE);
	  sITunesKinds.add(TKIND_MPEG_4_VIDEO_FILE);
	  sITunesKinds.add(TKIND_PDF_DOCUMENT);
	  sITunesKinds.add(TKIND_PROTECTED_AAC_AUDIO_FILE);
	  sITunesKinds.add(TKIND_PROTECTED_MPEG_4_VIDEO_FILE);
	  sITunesKinds.add(TKIND_PROTECTED_BOOK);
	  sITunesKinds.add(TKIND_PURCHASED_AAC_AUDIO_FILE);
	  sITunesKinds.add(TKIND_PURCHASED_MPEG_4_VIDEO_FILE);
	  sITunesKinds.add(TKIND_PURCHASED_BOOK);
	  sITunesKinds.add(TKIND_IPAD_APP);
	  sITunesKinds.add(TKIND_IPHONE_IPOD_TOUCH_APP);
	  sITunesKinds.add(TKIND_IPHONE_IPOD_TOUCH_IPAD_APP);
	  sITunesKinds.add(TKIND_ITUNES_EXTRAS);
	  sITunesKinds.add(TKIND_MPEG_AUDIO_STREAM);
	  
	  sITunesAudioKinds.add(TKIND_AAC_AUDIO_FILE);
	  sITunesAudioKinds.add(TKIND_MPEG_AUDIO_FILE);
	  sITunesAudioKinds.add(TKIND_PROTECTED_AAC_AUDIO_FILE);
	  sITunesAudioKinds.add(TKIND_PURCHASED_AAC_AUDIO_FILE);
  }

  static {
    // Create a list of all known iTunes keys. This way if a new one shows up, the program will let us know.
    Parser.createKnownKeys();
	
	// Create a list of all known iTunes kinds. This way if a new one shows up, the program will let us know.
	Parser.createKnownKinds();
  }
  
  public List<Album> parse(final String itunesFile) throws ParserException {
    com.bolsinga.plist.data.Plist plist = createPlist(itunesFile);

    Iterator<Object> i = plist.getDict().getKeyAndArrayOrData().iterator();
    while (i.hasNext()) {
      JAXBElement<? extends Object> jo = (JAXBElement<? extends Object>)i.next();
      String key = (String)jo.getValue();
      if (key.equals("Tracks")) {
        com.bolsinga.plist.data.Dict dict = (com.bolsinga.plist.data.Dict)i.next();
                                
        List<Object> tracks = dict.getKeyAndArrayOrData();
        addTracks(tracks);
      } else {
        Object o = i.next();
      }
    }

    setAlbumYears();
                
    sortAlbumOrder();

    sortAlbumsSongOrder();
    
    if (sNewITunesKeys.size() > 0) {
        System.out.println("iTunes added new keys:");
        for (String key : sNewITunesKeys) {
            System.out.println("private static final String TK_VAR = \"" + key + "\";");
        }
    }
	
    if (sNewITunesKinds.size() > 0) {
        System.out.println("iTunes added new kinds:");
        for (String kind : sNewITunesKinds) {
			String varName = kind.toUpperCase().replaceAll(" ", "_").replaceAll("/", "_").replaceAll("-", "_");
            System.out.println("private static final String TKIND_" + varName + " = \"" + kind + "\";");
        }
    }
	
	if (sArtistNotAdded.size() > 0) {
		System.out.println("Artist not Added:");
		for (String artist : sArtistNotAdded) {
			System.out.println(artist);
		}
	}
    
    return Collections.unmodifiableList(new ArrayList<Album>(fAlbumMap.values()));
  }
        
  private void addTracks(final java.util.List<Object> tracks) throws ParserException {
    Iterator<Object> i = tracks.iterator();
    while (i.hasNext()) {
      Object key = i.next(); // key not used

      com.bolsinga.plist.data.Dict track = (com.bolsinga.plist.data.Dict)i.next();
      addTrack(track);
    }
  }
        
  private void addTrack(final com.bolsinga.plist.data.Dict track) throws ParserException {
    Iterator<Object> i = track.getKeyAndArrayOrData().iterator();
            
    String songTitle = null;
    String artist = null;
    String sortArtist = null;
    GregorianCalendar lastPlayed = null;
    int playCount = 0;
    String genre = null;
    String albumTitle = null;
    int index = -1, year = -1;
    boolean compilation = false;
	boolean isVideo = false;
	boolean isPodcast = false;
	boolean isAudioFile = false;
            
    while (i.hasNext()) {
      JAXBElement<? extends Object> jokey = (JAXBElement<? extends Object>)i.next();
      String key = (String)jokey.getValue();

      // always pull off the value, it may be unused.
      JAXBElement<? extends Object> jovalue = (JAXBElement<? extends Object>)i.next();

      if (key.equals(TK_NAME)) {
        songTitle = (String)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_ARTIST)) {
        artist = (String)jovalue.getValue();
        continue;
      }
      if (key.equals(TK_SORT_ARTIST)) {
        sortArtist = (String)jovalue.getValue();
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
        lastPlayed = ((XMLGregorianCalendar)jovalue.getValue()).toGregorianCalendar();
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
	  if (key.equals(TK_KIND)) {
		  String kind = (String)jovalue.getValue();
	      if (!sITunesKinds.contains(kind)) {
	        sNewITunesKinds.add(kind);
	      }
		  
		  isAudioFile = sITunesAudioKinds.contains(kind);
	  }

      if (!sITunesKeys.contains(key)) {
        sNewITunesKeys.add(key);
      }
    }

    if (albumTitle == null) {
      albumTitle = songTitle + " - Single";
    }

    boolean isVoiceMemo = (genre != null) && genre.equals(TK_GENRE_VOICE_MEMO);

    boolean isAlbum = !isVideo && !isPodcast && isAudioFile && !isVoiceMemo;;
	if (!isAlbum) {
		sArtistNotAdded.add(artist);
	}

    if (isAlbum && (artist != null) && !albumTitle.equals("Apple Financial Results")) {
      createTrack(artist, sortArtist, songTitle, albumTitle, year, index, genre, lastPlayed, playCount, compilation);
    }
  }
        
  private void createTrack(final String artistName, final String sortArtist, final String songTitle, final String albumTitle, final int year, final int index, final String genre, final GregorianCalendar lastPlayed, final int playCount, final boolean compilation) throws ParserException {
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
}
