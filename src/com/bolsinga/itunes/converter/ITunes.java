package com.bolsinga.itunes.converter;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.plist.util.*;
import com.bolsinga.music.data.*;

public class ITunes {

	private static final String TK_ALBUM = "Album";
	private static final String TK_ARTIST = "Artist";
	private static final String TK_ARTWORK_COUNT = "Artwork Count";
	private static final String TK_BIT_RATE = "Bit Rate";
	private static final String TK_COMMENTS = "Comments";
	private static final String TK_COMPILATION = "Compilation";
	private static final String TK_COMPOSER = "Composer";
	private static final String TK_DATE_ADDED = "Date Added";
	private static final String TK_DATE_MODIFIED = "Date Modified";
	private static final String TK_DISC_COUNT = "Disc Count";
	private static final String TK_DISC_NUMBER = "Disc Number";
	private static final String TK_FILE_CREATOR = "File Creator";
	private static final String TK_FILE_FOLDER_COUNT = "File Folder Count";
	private static final String TK_FILE_TYPE = "File Type";
	private static final String TK_GENRE = "Genre";
	private static final String TK_KIND = "Kind";
	private static final String TK_LIBRARY_FOLDER_COUNT = "Library Folder Count";
	private static final String TK_LOCATION = "Location";
	private static final String TK_NAME = "Name";
	private static final String TK_PLAY_COUNT = "Play Count";
	private static final String TK_PLAY_DATE = "Play Date";
	private static final String TK_PLAY_DATE_UTC = "Play Date UTC";
	private static final String TK_SAMPLE_RATE = "Sample Rate";
	private static final String TK_SIZE = "Size";
	private static final String TK_TOTAL_TIME = "Total Time";
	private static final String TK_TRACK_COUNT = "Track Count";
	private static final String TK_TRACK_ID = "Track ID";
	private static final String TK_TRACK_NUMBER = "Track Number";
	private static final String TK_TRACK_TYPE = "Track Type";
	private static final String TK_YEAR = "Year";

	private static final String FORMAT_12_INCH_LP = "12 Inch LP";
	private static final String FORMAT_12_INCH_EP = "12 Inch EP";
	private static final String FORMAT_12_INCH_SINGLE = "12 Inch Single";
	private static final String FORMAT_10_INCH_EP = "10 Inch EP";
	private static final String FORMAT_7_INCH_SINGLE = "7 Inch Single";
	private static final String FORMAT_CASSETTE = "Cassette";
	private static final String FORMAT_CD = "CD";
	private static final String FORMAT_DIGITAL_FILE = "Digital File";

	private static HashMap sArtists = new HashMap();
	private static HashMap sAlbums = new HashMap();

	private static HashSet sITunesKeys = new HashSet();
	
	public static void main(String[] args) {
	    if (args.length != 2) {
		System.out.println("Usage: ITunes [itunes] [output]");
		System.exit(0);
	    }
		
	    ITunes.convert(args[0], args[1]);
	}
	
	public static void convert(String itunesFile, String outputFile) {
	    com.bolsinga.plist.data.Plist plist = Util.createPlist(itunesFile);

	    com.bolsinga.music.data.Music music = ITunes.convert(plist);
	    
	    try {
		music.setTimestamp(Calendar.getInstance());
		    
		// Write out to the output file.
		JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		OutputStream os = null;
		try {
		    os = new FileOutputStream(outputFile);
		} catch (IOException ioe) {
		    System.err.println(ioe);
		    ioe.printStackTrace();
		    System.exit(1);
		}
		m.marshal(music, os);
	    } catch (JAXBException e) {
		System.err.println(e);
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	public static com.bolsinga.music.data.Music convert(com.bolsinga.plist.data.Plist plist) {
	    ObjectFactory objFactory = new ObjectFactory();
	    com.bolsinga.music.data.Music music = null;
	    
	    try {
		music = objFactory.createMusic();
	    } catch (JAXBException e) {
		System.err.println(e);
		e.printStackTrace();
		System.exit(1);
	    }
	    
	    ITunes.add(objFactory, music, plist);
	    
	    return music;
	}
	
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
	}
	
	public static void add(ObjectFactory objFactory, com.bolsinga.music.data.Music music, com.bolsinga.plist.data.Plist plist) {
	
	    // Cache data from the given com.bolsinga.music.data.Music object here.
	    
	    // Create a list of all known iTunes keys. This way if a new one shows up, the program will let us know.
	    createKnownKeys();
	    
	    ListIterator li = (plist.getDict().getKeyAndArrayOrData()).listIterator();
	    while (li.hasNext()) {
		com.bolsinga.plist.data.Key key = (com.bolsinga.plist.data.Key)li.next();
		if (key.getValue().equals("Tracks")) {
		    com.bolsinga.plist.data.Dict dict = (com.bolsinga.plist.data.Dict)li.next();
		    
		    List tracks = dict.getKeyAndArrayOrData();
		    ITunes.add(objFactory, music, tracks);
		} else {
		    Object o = li.next();
		}
	    }
	}
	
	public static void add(ObjectFactory objFactory, com.bolsinga.music.data.Music music, java.util.List tracks) {
	    ListIterator li = tracks.listIterator();
	    while (li.hasNext()) {
		com.bolsinga.plist.data.Key key = (com.bolsinga.plist.data.Key)li.next();

		com.bolsinga.plist.data.Dict track = (com.bolsinga.plist.data.Dict)li.next();
		ITunes.add(objFactory, music, track);
	    }
	}
	
	public static void add(ObjectFactory objFactory, com.bolsinga.music.data.Music music, com.bolsinga.plist.data.Dict track) {
	    ListIterator li = track.getKeyAndArrayOrData().listIterator();
	    
	    String songTitle = null;
	    String artist = null;
	    Calendar lastPlayed = null;
	    String genre = null;
	    String albumTitle = null;
	    int index = -1, total = -1, year = -1;
	    boolean compilation = false;
	    
	    while (li.hasNext()) {
		String key = ((com.bolsinga.plist.data.Key)li.next()).getValue();
				
		if (key.equals(TK_NAME)) {
		    songTitle = ((com.bolsinga.plist.data.String)li.next()).getValue();
		    continue;
		}
		if (key.equals(TK_ARTIST)) {
		    artist = ((com.bolsinga.plist.data.String)li.next()).getValue();
		    continue;
		}
		if (key.equals(TK_ALBUM)) {
		    albumTitle = ((com.bolsinga.plist.data.String)li.next()).getValue();
		    continue;
		}
		if (key.equals(TK_GENRE)) {
		    genre = ((com.bolsinga.plist.data.String)li.next()).getValue();
		    continue;
		}
		if (key.equals(TK_TRACK_NUMBER)) {
		    index = ((com.bolsinga.plist.data.Integer)li.next()).getValue().intValue();
		    continue;
		}
		if (key.equals(TK_TRACK_COUNT)) {
		    total = ((com.bolsinga.plist.data.Integer)li.next()).getValue().intValue();
		    continue;
		}
		if (key.equals(TK_YEAR)) {
		    year = ((com.bolsinga.plist.data.Integer)li.next()).getValue().intValue();
		    continue;
		}
		if (key.equals(TK_PLAY_DATE_UTC)) {
		    lastPlayed = ((com.bolsinga.plist.data.Date)li.next()).getValue();
		    continue;
		}
		if (key.equals(TK_COMPILATION)) {
		    // Ignore the value, but it needs to be pulled.
		    compilation = (li.next() != null);
		    continue;
		}

		// This key isn't used, so pass over its value.
		Object o = li.next();

		if (!sITunesKeys.contains(key)) {
		    System.out.println("iTunes added a new key: " + key);
		}
	    }
	    
	    ITunes.addTrack(objFactory, music, artist, songTitle, albumTitle, year, index, total, genre, lastPlayed, compilation);
	}
	
	public static void addTrack(ObjectFactory objFactory, com.bolsinga.music.data.Music music, String artistName, String songTitle, String albumTitle, int year, int index, int total, String genre, Calendar lastPlayed, boolean compilation) {
	    try {
		// Get or create the artist
		Artist artist = ITunes.addArtist(objFactory, music, artistName);
		
		// Get or create the album.
		Album album = ITunes.addAlbum(objFactory, music, albumTitle, compilation ? null : artist);
		
		// The song is always the new item. The artist and album may already be known.
		ITunes.addAlbumTrack(objFactory, music, artist, album, songTitle, year, index, total, genre, lastPlayed);
	    } catch (JAXBException e) {
		System.err.println(e);
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	public static Artist addArtist(ObjectFactory objFactory, com.bolsinga.music.data.Music music, String name) throws JAXBException {
	    Artist result = null;
	    if (!sArtists.containsKey(name)) {
		result = objFactory.createArtist();
		
		result.setName(name);
		result.setId("ar" + sArtists.size());
		
		music.getArtist().add(result);
		sArtists.put(name, result);
	    } else {
		result = (Artist)sArtists.get(name);
	    }
	    return result;
	}
	
	public static Album addAlbum(ObjectFactory objFactory, com.bolsinga.music.data.Music music, String name, Artist artist) throws JAXBException {
	    Album result = null;
	    if (name == null) {
		name = "Unknown";
	    }
	    if (!sAlbums.containsKey(name)) {
		result = objFactory.createAlbum();
		
		result.setTitle(name);
		if (artist != null) {
		    result.getPerformer().add(artist);
		} else {
		    result.setCompilation(true);
		}
		result.getFormat().add(FORMAT_DIGITAL_FILE);
		result.setId("a" + sAlbums.size());
		
		music.getAlbum().add(result);
		sAlbums.put(name, result);
	    } else {
		result = (Album)sAlbums.get(name);
	    }
	    return result;
	}
	
	public static void addAlbumTrack(ObjectFactory objFactory, com.bolsinga.music.data.Music music, Artist artist, Album album, String songTitle, int year, int index, int total, String genre, Calendar lastPlayed) throws JAXBException {
	    // Create the song, add it to the album, and add the album to the artist if it isn't there already.
	    Song song = ITunes.createSong(objFactory, music, artist, songTitle, year, genre, lastPlayed);
	    
//	    ITunes.addSongToAlbum(album, song, index, total);
	    
	    ITunes.addAlbumToArtist(artist, album);
	}
	
	static Song createSong(ObjectFactory objFactory, com.bolsinga.music.data.Music music, Artist artist, String songTitle, int year, String genre, Calendar lastPlayed) throws JAXBException {
	    List songs = music.getSong();
	    
	    Song result = null;
	    
	    result = objFactory.createSong();
	    result.setTitle(songTitle);
	    result.getPerformer().add(artist);
	    result.setLastPlayed(lastPlayed);
	    result.setGenre(genre);
	    
	    com.bolsinga.music.data.Date release = objFactory.createDate();
	    release.setUnknown(true);
	    release.setYear(java.math.BigInteger.valueOf(year));
	    result.setReleaseDate(release);
	    	    
	    result.setId("s" + songs.size());
	    result.setDigitized(true);
	    
	    songs.add(result);
	    
	    return result;
	}
	
	static void addSongToAlbum(Album album, Song song, int index, int total) {
	    List songs = album.getSong();
	    
	    if (songs.get(index - 1) == null) {
		songs.set(index - 1, song);
	    } else {
		System.err.println("Song " + song.getTitle() + " already in album " + album.getTitle());
	    }
	}
	
	static void addAlbumToArtist(Artist artist, Album album) {
	    List albums = artist.getAlbum();
	    
	    if (!albums.contains(album)) {
		albums.add(album);
	    }
	}
}
