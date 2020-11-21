package com.bolsinga.itunes;

import java.util.*;
import java.util.regex.*;

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
  private final Map<String, Album> fAlbumMap = new TreeMap<String, Album>();
  
  // AlbumKey's
  private final HashSet<String> fArtistAlbumSet = new HashSet<String>();

  List<Track> parseTracks(final String itunesFile) throws ParserException {
    List<Track> tracks = null;
    if (itunesFile.endsWith("json")) {
      JSONParser parser = new JSONParser();
      tracks = parser.createTracks(itunesFile);
    } else {
      throw new ParserException("Unknown file extension: " + itunesFile);
    }
    return Collections.unmodifiableList(new ArrayList<Track>(tracks));
  }

  public List<Album> parse(final String itunesFile) throws ParserException {
    List<Track> tracks = parseTracks(itunesFile);

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

    if (track.getAlbum() == null || track.getAlbum().length() == 0) {
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
      java.time.ZonedDateTime lastPlayedZDT = (track.getPlay_Date_UTC() != null) ? java.time.ZonedDateTime.parse(track.getPlay_Date_UTC()) : null;
      GregorianCalendar lastPlayed = (lastPlayedZDT != null) ? GregorianCalendar.from(lastPlayedZDT): null;
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
}
