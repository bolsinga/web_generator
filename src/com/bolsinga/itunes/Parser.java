package com.bolsinga.itunes;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

import com.bolsinga.plist.*;

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
  private static final String TK_SORT_ALBUM_ARTITST    = "Sort Album Artist";
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
  private static final String TK_GAPLESS_ALBUM         = "Part Of Gapless Album";
  private static final String TK_MUSIC_VIDEO           = "Music Video";
  private static final String TK_MATCH_ID              = "MatchID";
  private static final String TK_XID                   = "XID";
    
  public class Album {
    public static final int UNKNOWN_YEAR = 0;
    
    private final String fTitle;
    private final Artist fArtist;
    private int fReleaseYear;
    private final boolean fIsCompilation;
    private final List<Song> fSongs;
    
    Album(final String title, final Artist artist) {
      fTitle = title;
      fArtist = artist;
      fReleaseYear = Album.UNKNOWN_YEAR;
      fIsCompilation = (artist == null);
      fSongs = new ArrayList<Song>();
    }
    
    public String getTitle() {
      return fTitle;
    }
    
    public Artist getArtist() {
      return fArtist;
    }
        
    public int getReleaseYear() {
      return fReleaseYear;
    }
    
    void setReleaseYear(final int year) {
      fReleaseYear = year;
    }
    
    public boolean isCompilation() {
      return fIsCompilation;
    }
    
    public List<Song> getSongs() {
      return Collections.unmodifiableList(fSongs);
    }
    
    void addSong(Song song) {
      fSongs.add(song);
    }

    void sortSongs() {
      Collections.sort(fSongs, Parser.SONG_ORDER_COMPARATOR);
    }
  }
  
  public class Artist {
    private final String fName;
    private final String fSortName;
    private final List<Album> fAlbums;
    
    Artist(final String name, final String sortName) {
      fName = name;
      fSortName = sortName;
      fAlbums = new ArrayList<Album>();
    }
    
    public String getName() {
      return fName;
    }
    
    public String getSortname() {
      return fSortName;
    }

    public List<Album> getAlbums() {
      return Collections.unmodifiableList(fAlbums);
    }
    
    void addAlbum(Album album) {
      fAlbums.add(album);
    }
    
    void sortAlbums() {
      Collections.sort(fAlbums, ALBUM_ORDER_COMPARATOR);
    }
  }
  
  public class Song {
    private static final int UNKNOWN_TRACK = 0;
    
    private final Artist fArtist;
    private final String fTitle;
    private final int fReleaseYear;
    private final GregorianCalendar fLastPlayed;
    private final int fTrack;
    private final String fGenre;
    private final int fPlayCount;
    
    Song(final Artist artist, final String title, final int year, final GregorianCalendar lastPlayed, final int track, final String genre, final int playCount) {
      fArtist = artist;
      fTitle = title;
      fReleaseYear = year;
      fLastPlayed = lastPlayed;
      fTrack = track;
      fGenre = genre;
      fPlayCount = playCount;
    }
    
    public Artist getArtist() {
      return fArtist;
    }
    
    public String getTitle() {
      return fTitle;
    }
    
    public int getReleaseYear() {
      return fReleaseYear;
    }
    
    public GregorianCalendar getLastPlayed() {
      return fLastPlayed;
    }
    
    public int getTrack() {
      return fTrack;
    }
    
    public String getGenre() {
      return fGenre;
    }
    
    public int getPlayCount() {
      return fPlayCount;
    }
  }

  private static final HashSet<String> sITunesKeys = new HashSet<String>();
  private static final Set<String> sNewITunesKeys = new TreeSet<String>();

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
  
  private static final Comparator<Artist> ARTIST_COMPARATOR = new Comparator<Artist>() {
    public int compare(final Artist r1, final Artist r2) {
      String n1 = r1.getSortname();
      if (n1 == null) {
        n1 = r1.getName();
      }
      String n2 = r2.getSortname();
      if (n2 == null) {
        n2 = r2.getName();
      }
          
      return com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(n1, n2);
    }
  };

  private static final Comparator<Album> ALBUM_COMPARATOR = new Comparator<Album>() {
    public int compare(final Album r1, final Album r2) {
      int result = com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(r1.getTitle(), r2.getTitle());
      if (result == 0) {
        result = ARTIST_COMPARATOR.compare(r1.getArtist(), r2.getArtist());
      }
      return result;
    }
  };

  private static final Comparator<Album> ALBUM_ORDER_COMPARATOR = new Comparator<Album>() {
    public int compare(final Album r1, final Album r2) {
      // The Integer.MAX_VALUE assures that 'unknown' album dates are after the known ones.
      int date1 = (r1.getReleaseYear() != Album.UNKNOWN_YEAR) ? r1.getReleaseYear() : Integer.MAX_VALUE;
      int date2 = (r2.getReleaseYear() != Album.UNKNOWN_YEAR) ? r2.getReleaseYear() : Integer.MAX_VALUE;
      int result = date1 - date2;
      if (result == 0) {
        result = ALBUM_COMPARATOR.compare(r1, r2);
      }
      return result;
    }
  };

  private static final Comparator<Song> SONG_ORDER_COMPARATOR = new Comparator<Song>() {
    public int compare(final Song r1, final Song r2) {
      return r1.getTrack() - r2.getTrack();
    }
  };

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
    sITunesKeys.add(TK_SORT_ALBUM_ARTITST);
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
    sITunesKeys.add(TK_GAPLESS_ALBUM);
    sITunesKeys.add(TK_MUSIC_VIDEO);
    sITunesKeys.add(TK_MATCH_ID);
    sITunesKeys.add(TK_XID);
  }

  static {
    // Create a list of all known iTunes keys. This way if a new one shows up, the program will let us know.
    Parser.createKnownKeys();
  }
  
  public List<Album> parse(final String itunesFile) throws ParserException {
    com.bolsinga.plist.data.Plist plist = null;
    try {
      plist = Util.createPlist(itunesFile);
    } catch (com.bolsinga.plist.PlistException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't parse file: ");
      sb.append(itunesFile);
      throw new ParserException(sb.toString(), e);
    }

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

      if (!sITunesKeys.contains(key)) {
        sNewITunesKeys.add(key);
      }
    }

    if (albumTitle == null) {
      albumTitle = songTitle + " - Single";
    }

    if (!isVideo && !isPodcast && (artist != null) && !albumTitle.equals("Apple Financial Results")) {
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
}
