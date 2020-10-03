package com.bolsinga.itunes;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

class XMLParser {
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

  private Map<String, Object> createPlist(final String sourceFile) throws ParserException {
    InputStream is = null;
    try {
      is = new FileInputStream(sourceFile);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find plist file: ");
      sb.append(sourceFile);
      throw new ParserException(sb.toString(), e);
    }

    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    SAXParser parser = null;
    try {
      parser = parserFactory.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't create SAXParser");
      throw new ParserException(sb.toString(), e);
    }

    XMLReader reader = null;
    try {
      reader = parser.getXMLReader();
    } catch (SAXException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't create XMLReader");
      throw new ParserException(sb.toString(), e);
    }

    ParserHandler handler = new ParserHandler();
    reader.setContentHandler(handler);

    InputSource source = new InputSource(is);
    try {
      reader.parse(source);
    } catch (IOException | SAXException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't parse InputSource");
      throw new ParserException(sb.toString(), e);
    }

    return handler.plist;
  }

  private Map<String, Object> createTracksDict(final String sourceFile) throws ParserException {
    Map<String, Object> plist = createPlist(sourceFile);

    for (Map.Entry<String, Object> entry : plist.entrySet()) {
      if (entry.getKey().equals("Tracks")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> dict = (Map<String, Object>)entry.getValue();
        return dict;
      }
    }
    throw new ParserException("No Tracks key in plist: " + plist.toString());
  }

  List<Track> createTracks(final String sourceFile) throws ParserException {
    Map<String, Object> tracksDict = createTracksDict(sourceFile);

    ArrayList<Track> tracks = new ArrayList<Track>();

    Set<String> newKeys = new TreeSet<String>();

    for (Object o : tracksDict.values()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> trackDict = (Map<String, Object>)o;

      Track track = new Track();

      for (Map.Entry<String, Object> entry : trackDict.entrySet()) {
        String key = entry.getKey();
        boolean knownKey = set(track, key, (String)entry.getValue());
        if (!knownKey) {
          newKeys.add(key);
        }
      }

      tracks.add(track);
    }

    if (newKeys.size() > 0) {
      System.out.println("iTunes added new keys:");
      for (String key : newKeys) {
          String varName = key.toUpperCase().replaceAll(" ", "_").replaceAll("/", "_").replaceAll("-", "_");
          System.out.println("private static final String TK_" + varName + " = \"" + key + "\";");
      }
    }

    return tracks;
  }

  private boolean set(Track track, String elementName, String value) {
    boolean knownKey = true;
    if (TK_ALBUM.equals(elementName)) { track.setAlbum(value); }
    else if (TK_ARTIST.equals(elementName)) { track.setArtist(value); }
    else if (TK_ARTWORK_COUNT.equals(elementName)) { track.setArtwork_Count(value); }
    else if (TK_BIT_RATE.equals(elementName)) { track.setBit_Rate(value); }
    else if (TK_COMMENTS.equals(elementName)) { track.setComments(value); }
    else if (TK_COMPILATION.equals(elementName)) { track.setCompilation(value); }
    else if (TK_COMPOSER.equals(elementName)) { track.setComposer(value); }
    else if (TK_DATE_ADDED.equals(elementName)) { track.setDate_Added(value); }
    else if (TK_DATE_MODIFIED.equals(elementName)) { track.setDate_Modified(value); }
    else if (TK_DISC_COUNT.equals(elementName)) { track.setDisc_Count(value); }
    else if (TK_DISC_NUMBER.equals(elementName)) { track.setDisc_Number(value); }
    else if (TK_FILE_CREATOR.equals(elementName)) { track.setFile_Creator(value); }
    else if (TK_FILE_FOLDER_COUNT.equals(elementName)) { track.setFile_Folder_Count(value); }
    else if (TK_FILE_TYPE.equals(elementName)) { track.setFile_Type(value); }
    else if (TK_GENRE.equals(elementName)) { track.setGenre(value); }
    else if (TK_KIND.equals(elementName)) { track.setKind(value); }
    else if (TK_LIBRARY_FOLDER_COUNT.equals(elementName)) { track.setLibrary_Folder_Count(value); }
    else if (TK_LOCATION.equals(elementName)) { track.setLocation(value); }
    else if (TK_NAME.equals(elementName)) { track.setName(value); }
    else if (TK_PLAY_COUNT.equals(elementName)) { track.setPlay_Count(value); }
    else if (TK_PLAY_DATE.equals(elementName)) { track.setPlay_Date(value); }
    else if (TK_PLAY_DATE_UTC.equals(elementName)) { track.setPlay_Date_UTC(value); }
    else if (TK_SAMPLE_RATE.equals(elementName)) { track.setSample_Rate(value); }
    else if (TK_SIZE.equals(elementName)) { track.setSize(value); }
    else if (TK_TOTAL_TIME.equals(elementName)) { track.setTotal_Time(value); }
    else if (TK_TRACK_COUNT.equals(elementName)) { track.setTrack_Count(value); }
    else if (TK_TRACK_ID.equals(elementName)) { track.setTrack_ID(value); }
    else if (TK_TRACK_NUMBER.equals(elementName)) { track.setTrack_Number(value); }
    else if (TK_TRACK_TYPE.equals(elementName)) { track.setTrack_Type(value); }
    else if (TK_YEAR.equals(elementName)) { track.setYear(value); }
    else if (TK_SEASON.equals(elementName)) { track.setSeason(value); }
    else if (TK_PERSISTENT_ID.equals(elementName)) { track.setPersistent_ID(value); }
    else if (TK_SERIES.equals(elementName)) { track.setSeries(value); }
    else if (TK_EPISODE.equals(elementName)) { track.setEpisode(value); }
    else if (TK_EPISODE_ORDER.equals(elementName)) { track.setEpisode_Order(value); }
    else if (TK_HAS_VIDEO.equals(elementName)) { track.setHas_Video(value); }
    else if (TK_TV_SHOW.equals(elementName)) { track.setTV_Show(value); }
    else if (TK_PROTECTED.equals(elementName)) { track.setProtected(value); }
    else if (TK_BPM.equals(elementName)) { track.setBPM(value); }
    else if (TK_ALBUM_ARTIST.equals(elementName)) { track.setAlbum_Artist(value); }
    else if (TK_EXPLICIT.equals(elementName)) { track.setExplicit(value); }
    else if (TK_SKIP_COUNT.equals(elementName)) { track.setSkip_Count(value); }
    else if (TK_SKIP_DATE.equals(elementName)) { track.setSkip_Date(value); }
    else if (TK_RELEASE_DATE.equals(elementName)) { track.setRelease_Date(value); }
    else if (TK_PODCAST.equals(elementName)) { track.setPodcast(value); }
    else if (TK_MOVIE.equals(elementName)) { track.setMovie(value); }
    else if (TK_UNPLAYED.equals(elementName)) { track.setUnplayed(value); }
    else if (TK_SORT_ALBUM.equals(elementName)) { track.setSort_Album(value); }
    else if (TK_SORT_ALBUM_ARTIST.equals(elementName)) { track.setSort_Album_Artist(value); }
    else if (TK_SORT_ARTIST.equals(elementName)) { track.setSort_Artist(value); }
    else if (TK_SORT_COMPOSER.equals(elementName)) { track.setSort_Composer(value); }
    else if (TK_SORT_NAME.equals(elementName)) { track.setSort_Name(value); }
    else if (TK_CONTENT_RATING.equals(elementName)) { track.setContent_Rating(value); }
    else if (TK_DISABLED.equals(elementName)) { track.setDisabled(value); }
    else if (TK_PURCHASED.equals(elementName)) { track.setPurchased(value); }
    else if (TK_VIDEO_HEIGHT.equals(elementName)) { track.setVideo_Height(value); }
    else if (TK_VIDEO_WIDTH.equals(elementName)) { track.setVideo_Width(value); }
    else if (TK_EID.equals(elementName)) { track.setEID(value); }
    else if (TK_HD.equals(elementName)) { track.setHD(value); }
    else if (TK_ALBUM_RATING.equals(elementName)) { track.setAlbum_Rating(value); }
    else if (TK_ALBUM_RATING_COMPUTED.equals(elementName)) { track.setAlbum_Rating_Computed(value); }
    else if (TK_RATING.equals(elementName)) { track.setRating(value); }
    else if (TK_GROUPING.equals(elementName)) { track.setGrouping(value); }
    else if (TK_PART_OF_GAPLESS_ALBUM.equals(elementName)) { track.setPart_Of_Gapless_Album(value); }
    else if (TK_MUSIC_VIDEO.equals(elementName)) { track.setMusic_Video(value); }
    else if (TK_MATCH_ID.equals(elementName)) { track.setMatchID(value); }
    else if (TK_XID.equals(elementName)) { track.setXID(value); }
    else if (TK_RATING_COMPUTED.equals(elementName)) { track.setRating_Computed(value); }
    else if (TK_SORT_SERIES.equals(elementName)) { track.setSort_Series(value); }
    else {
      knownKey = false;
    }
    return knownKey;
  }
}
