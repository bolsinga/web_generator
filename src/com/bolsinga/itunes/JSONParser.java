package com.bolsinga.itunes;

import java.io.*;
import java.util.*;
import org.json.*;

class JSONParser {
  private static final String ALBUM = "album";
  private static final String ALBUMARTIST = "albumArtist";
  private static final String ALBUMRATING = "albumRating";
  private static final String ALBUMRATINGCOMPUTED = "albumRatingComputed";
  private static final String ARTIST = "artist";
  private static final String ARTWORKCOUNT = "artworkCount";
  private static final String BITRATE = "bitRate";
  private static final String BPM = "bPM";
  private static final String COMMENTS = "comments";
  private static final String COMPILATION = "compilation";
  private static final String COMPOSER = "composer";
  private static final String CONTENTRATING = "contentRating";
  private static final String DATEADDED = "dateAdded";
  private static final String DATEMODIFIED = "dateModified";
  private static final String DISABLED = "disabled";
  private static final String DISCCOUNT = "discCount";
  private static final String DISCNUMBER = "discNumber";
  private static final String EPISODE = "episode";
  private static final String EPISODEORDER = "episodeOrder";
  private static final String EXPLICIT = "explicit";
  private static final String GENRE = "genre";
  private static final String GROUPING = "grouping";
  private static final String HASVIDEO = "hasVideo";
  private static final String HD = "hD";
  private static final String KIND = "kind";
  private static final String LOCATION = "location";
  private static final String MOVIE = "movie";
  private static final String MUSICVIDEO = "musicVideo";
  private static final String NAME = "name";
  private static final String PARTOFGAPLESSALBUM = "partOfGaplessAlbum";
  private static final String PERSISTENTID = "persistentID";
  private static final String PLAYCOUNT = "playCount";
  private static final String PLAYDATEUTC = "playDateUTC";
  private static final String PODCAST = "podcast";
  private static final String PROTECTED = "protected";
  private static final String PURCHASED = "purchased";
  private static final String RATING = "rating";
  private static final String RATINGCOMPUTED = "ratingComputed";
  private static final String RELEASEDATE = "releaseDate";
  private static final String SAMPLERATE = "sampleRate";
  private static final String SEASON = "season";
  private static final String SERIES = "series";
  private static final String SIZE = "size";
  private static final String SKIPCOUNT = "skipCount";
  private static final String SKIPDATE = "skipDate";
  private static final String SORTALBUM = "sortAlbum";
  private static final String SORTALBUMARTIST = "sortAlbumArtist";
  private static final String SORTARTIST = "sortArtist";
  private static final String SORTCOMPOSER = "sortComposer";
  private static final String SORTNAME = "sortName";
  private static final String SORTSERIES = "sortSeries";
  private static final String TOTALTIME = "totalTime";
  private static final String TRACKCOUNT = "trackCount";
  private static final String TRACKNUMBER = "trackNumber";
  private static final String TRACKTYPE = "trackType";
  private static final String TVSHOW = "tVShow";
  private static final String UNPLAYED = "unplayed";
  private static final String VIDEOHEIGHT = "videoHeight";
  private static final String VIDEOWIDTH = "videoWidth";
  private static final String YEAR = "year";

  List<Track> createTracks(final String sourceFile) throws ParserException {
    JSONArray jsonArray;
    try {
      jsonArray = com.bolsinga.web.Util.createJSONArray(sourceFile);
    } catch (com.bolsinga.web.WebException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't create json: ");
      sb.append(sourceFile);
      throw new ParserException(sb.toString(), e);
    }

    ArrayList<Track> tracks = new ArrayList<Track>();

    Set<String> newKeys = new TreeSet<String>();

    Iterator<Object> i = jsonArray.iterator();
    while (i.hasNext()) {
      JSONObject json = (JSONObject)i.next();

      Track track = new Track();

      Iterator<String> k = json.keys();
      while (k.hasNext()) {
        String key = k.next();

        String value = json.optString(key, null);

        boolean knownKey = set(track, key, value);
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
    if (ALBUM.equals(elementName)) { track.setAlbum(value); }
    else if (ARTIST.equals(elementName)) { track.setArtist(value); }
    else if (ARTWORKCOUNT.equals(elementName)) { track.setArtwork_Count(value); }
    else if (BITRATE.equals(elementName)) { track.setBit_Rate(value); }
    else if (COMMENTS.equals(elementName)) { track.setComments(value); }
    else if (COMPILATION.equals(elementName)) { track.setCompilation(value); }
    else if (COMPOSER.equals(elementName)) { track.setComposer(value); }
    else if (DATEADDED.equals(elementName)) { track.setDate_Added(value); }
    else if (DATEMODIFIED.equals(elementName)) { track.setDate_Modified(value); }
    else if (DISCCOUNT.equals(elementName)) { track.setDisc_Count(value); }
    else if (DISCNUMBER.equals(elementName)) { track.setDisc_Number(value); }
    else if (GENRE.equals(elementName)) { track.setGenre(value); }
    else if (KIND.equals(elementName)) { track.setKind(value); }
    else if (LOCATION.equals(elementName)) { track.setLocation(value); }
    else if (NAME.equals(elementName)) { track.setName(value); }
    else if (PLAYCOUNT.equals(elementName)) { track.setPlay_Count(value); }
    else if (PLAYDATEUTC.equals(elementName)) { track.setPlay_Date_UTC(value); }
    else if (SAMPLERATE.equals(elementName)) { track.setSample_Rate(value); }
    else if (SIZE.equals(elementName)) { track.setSize(value); }
    else if (TOTALTIME.equals(elementName)) { track.setTotal_Time(value); }
    else if (TRACKCOUNT.equals(elementName)) { track.setTrack_Count(value); }
    else if (TRACKNUMBER.equals(elementName)) { track.setTrack_Number(value); }
    else if (TRACKTYPE.equals(elementName)) { track.setTrack_Type(value); }
    else if (YEAR.equals(elementName)) { track.setYear(value); }
    else if (SEASON.equals(elementName)) { track.setSeason(value); }
    else if (PERSISTENTID.equals(elementName)) { track.setPersistent_ID(value); }
    else if (SERIES.equals(elementName)) { track.setSeries(value); }
    else if (EPISODE.equals(elementName)) { track.setEpisode(value); }
    else if (EPISODEORDER.equals(elementName)) { track.setEpisode_Order(value); }
    else if (HASVIDEO.equals(elementName)) { track.setHas_Video(value); }
    else if (TVSHOW.equals(elementName)) { track.setTV_Show(value); }
    else if (PROTECTED.equals(elementName)) { track.setProtected(value); }
    else if (BPM.equals(elementName)) { track.setBPM(value); }
    else if (ALBUMARTIST.equals(elementName)) { track.setAlbum_Artist(value); }
    else if (EXPLICIT.equals(elementName)) { track.setExplicit(value); }
    else if (SKIPCOUNT.equals(elementName)) { track.setSkip_Count(value); }
    else if (SKIPDATE.equals(elementName)) { track.setSkip_Date(value); }
    else if (RELEASEDATE.equals(elementName)) { track.setRelease_Date(value); }
    else if (PODCAST.equals(elementName)) { track.setPodcast(value); }
    else if (MOVIE.equals(elementName)) { track.setMovie(value); }
    else if (UNPLAYED.equals(elementName)) { track.setUnplayed(value); }
    else if (SORTALBUM.equals(elementName)) { track.setSort_Album(value); }
    else if (SORTALBUMARTIST.equals(elementName)) { track.setSort_Album_Artist(value); }
    else if (SORTARTIST.equals(elementName)) { track.setSort_Artist(value); }
    else if (SORTCOMPOSER.equals(elementName)) { track.setSort_Composer(value); }
    else if (SORTNAME.equals(elementName)) { track.setSort_Name(value); }
    else if (CONTENTRATING.equals(elementName)) { track.setContent_Rating(value); }
    else if (DISABLED.equals(elementName)) { track.setDisabled(value); }
    else if (PURCHASED.equals(elementName)) { track.setPurchased(value); }
    else if (VIDEOHEIGHT.equals(elementName)) { track.setVideo_Height(value); }
    else if (VIDEOWIDTH.equals(elementName)) { track.setVideo_Width(value); }
    else if (HD.equals(elementName)) { track.setHD(value); }
    else if (ALBUMRATING.equals(elementName)) { track.setAlbum_Rating(value); }
    else if (ALBUMRATINGCOMPUTED.equals(elementName)) { track.setAlbum_Rating_Computed(value); }
    else if (RATING.equals(elementName)) { track.setRating(value); }
    else if (GROUPING.equals(elementName)) { track.setGrouping(value); }
    else if (PARTOFGAPLESSALBUM.equals(elementName)) { track.setPart_Of_Gapless_Album(value); }
    else if (MUSICVIDEO.equals(elementName)) { track.setMusic_Video(value); }
    else if (RATINGCOMPUTED.equals(elementName)) { track.setRating_Computed(value); }
    else if (SORTSERIES.equals(elementName)) { track.setSort_Series(value); }
    else {
      knownKey = false;
    }
    return knownKey;
  }

  static JSONObject convert(final Track track) {
    JSONObject json = new JSONObject();

    if (track.getAlbum() != null && track.getAlbum().length() > 0) { json.put(ALBUM, track.getAlbum()); }
    if (track.getArtist() != null && track.getArtist().length() > 0) { json.put(ARTIST, track.getArtist()); }
    if (track.getArtwork_Count() != null && track.getArtwork_Count().length() > 0) { json.put(ARTWORKCOUNT, track.getArtwork_Count()); }
    if (track.getBit_Rate() != null && track.getBit_Rate().length() > 0) { json.put(BITRATE, track.getBit_Rate()); }
    if (track.getComments() != null && track.getComments().length() > 0) { json.put(COMMENTS, track.getComments()); }
    if (track.getCompilation() != null && track.getCompilation().length() > 0) { json.put(COMPILATION, track.getCompilation()); }
    if (track.getComposer() != null && track.getComposer().length() > 0) { json.put(COMPOSER, track.getComposer()); }
    if (track.getDate_Added() != null && track.getDate_Added().length() > 0) { json.put(DATEADDED, track.getDate_Added()); }
    if (track.getDate_Modified() != null && track.getDate_Modified().length() > 0) { json.put(DATEMODIFIED, track.getDate_Modified()); }
    if (track.getDisc_Count() != null && track.getDisc_Count().length() > 0) { json.put(DISCCOUNT, track.getDisc_Count()); }
    if (track.getDisc_Number() != null && track.getDisc_Number().length() > 0) { json.put(DISCNUMBER, track.getDisc_Number()); }
    if (track.getGenre() != null && track.getGenre().length() > 0) { json.put(GENRE, track.getGenre()); }
    if (track.getKind() != null && track.getKind().length() > 0) { json.put(KIND, track.getKind()); }
    if (track.getLocation() != null && track.getLocation().length() > 0) { json.put(LOCATION, track.getLocation()); }
    if (track.getName() != null && track.getName().length() > 0) { json.put(NAME, track.getName()); }
    if (track.getPlay_Count() != null && track.getPlay_Count().length() > 0) { json.put(PLAYCOUNT, track.getPlay_Count()); }
    if (track.getPlay_Date_UTC() != null && track.getPlay_Date_UTC().length() > 0) { json.put(PLAYDATEUTC, track.getPlay_Date_UTC()); }
    if (track.getSample_Rate() != null && track.getSample_Rate().length() > 0) { json.put(SAMPLERATE, track.getSample_Rate()); }
    if (track.getSize() != null && track.getSize().length() > 0) { json.put(SIZE, track.getSize()); }
    if (track.getTotal_Time() != null && track.getTotal_Time().length() > 0) { json.put(TOTALTIME, track.getTotal_Time()); }
    if (track.getTrack_Count() != null && track.getTrack_Count().length() > 0) { json.put(TRACKCOUNT, track.getTrack_Count()); }
    if (track.getTrack_Number() != null && track.getTrack_Number().length() > 0) { json.put(TRACKNUMBER, track.getTrack_Number()); }
    if (track.getTrack_Type() != null && track.getTrack_Type().length() > 0) { json.put(TRACKTYPE, track.getTrack_Type()); }
    if (track.getYear() != null && track.getYear().length() > 0) { json.put(YEAR, track.getYear()); }
    if (track.getSeason() != null && track.getSeason().length() > 0) { json.put(SEASON, track.getSeason()); }
    if (track.getPersistent_ID() != null && track.getPersistent_ID().length() > 0) { json.put(PERSISTENTID, track.getPersistent_ID()); }
    if (track.getSeries() != null && track.getSeries().length() > 0) { json.put(SERIES, track.getSeries()); }
    if (track.getEpisode() != null && track.getEpisode().length() > 0) { json.put(EPISODE, track.getEpisode()); }
    if (track.getEpisode_Order() != null && track.getEpisode_Order().length() > 0) { json.put(EPISODEORDER, track.getEpisode_Order()); }
    if (track.getHas_Video() != null && track.getHas_Video().length() > 0) { json.put(HASVIDEO, track.getHas_Video()); }
    if (track.getTV_Show() != null && track.getTV_Show().length() > 0) { json.put(TVSHOW, track.getTV_Show()); }
    if (track.getProtected() != null && track.getProtected().length() > 0) { json.put(PROTECTED, track.getProtected()); }
    if (track.getBPM() != null && track.getBPM().length() > 0) { json.put(BPM, track.getBPM()); }
    if (track.getAlbum_Artist() != null && track.getAlbum_Artist().length() > 0) { json.put(ALBUMARTIST, track.getAlbum_Artist()); }
    if (track.getExplicit() != null && track.getExplicit().length() > 0) { json.put(EXPLICIT, track.getExplicit()); }
    if (track.getSkip_Count() != null && track.getSkip_Count().length() > 0) { json.put(SKIPCOUNT, track.getSkip_Count()); }
    if (track.getSkip_Date() != null && track.getSkip_Date().length() > 0) { json.put(SKIPDATE, track.getSkip_Date()); }
    if (track.getRelease_Date() != null && track.getRelease_Date().length() > 0) { json.put(RELEASEDATE, track.getRelease_Date()); }
    if (track.getPodcast() != null && track.getPodcast().length() > 0) { json.put(PODCAST, track.getPodcast()); }
    if (track.getMovie() != null && track.getMovie().length() > 0) { json.put(MOVIE, track.getMovie()); }
    if (track.getUnplayed() != null && track.getUnplayed().length() > 0) { json.put(UNPLAYED, track.getUnplayed()); }
    if (track.getSort_Album() != null && track.getSort_Album().length() > 0) { json.put(SORTALBUM, track.getSort_Album()); }
    if (track.getSort_Album_Artist() != null && track.getSort_Album_Artist().length() > 0) { json.put(SORTALBUMARTIST, track.getSort_Album_Artist()); }
    if (track.getSort_Artist() != null && track.getSort_Artist().length() > 0) { json.put(SORTARTIST, track.getSort_Artist()); }
    if (track.getSort_Composer() != null && track.getSort_Composer().length() > 0) { json.put(SORTCOMPOSER, track.getSort_Composer()); }
    if (track.getSort_Name() != null && track.getSort_Name().length() > 0) { json.put(SORTNAME, track.getSort_Name()); }
    if (track.getContent_Rating() != null && track.getContent_Rating().length() > 0) { json.put(CONTENTRATING, track.getContent_Rating()); }
    if (track.getDisabled() != null && track.getDisabled().length() > 0) { json.put(DISABLED, track.getDisabled()); }
    if (track.getPurchased() != null && track.getPurchased().length() > 0) { json.put(PURCHASED, track.getPurchased()); }
    if (track.getVideo_Height() != null && track.getVideo_Height().length() > 0) { json.put(VIDEOHEIGHT, track.getVideo_Height()); }
    if (track.getVideo_Width() != null && track.getVideo_Width().length() > 0) { json.put(VIDEOWIDTH, track.getVideo_Width()); }
    if (track.getHD() != null && track.getHD().length() > 0) { json.put(HD, track.getHD()); }
    if (track.getAlbum_Rating() != null && track.getAlbum_Rating().length() > 0) { json.put(ALBUMRATING, track.getAlbum_Rating()); }
    if (track.getAlbum_Rating_Computed() != null && track.getAlbum_Rating_Computed().length() > 0) { json.put(ALBUMRATINGCOMPUTED, track.getAlbum_Rating_Computed()); }
    if (track.getRating() != null && track.getRating().length() > 0) { json.put(RATING, track.getRating()); }
    if (track.getGrouping() != null && track.getGrouping().length() > 0) { json.put(GROUPING, track.getGrouping()); }
    if (track.getPart_Of_Gapless_Album() != null && track.getPart_Of_Gapless_Album().length() > 0) { json.put(PARTOFGAPLESSALBUM, track.getPart_Of_Gapless_Album()); }
    if (track.getMusic_Video() != null && track.getMusic_Video().length() > 0) { json.put(MUSICVIDEO, track.getMusic_Video()); }
    if (track.getRating_Computed() != null && track.getRating_Computed().length() > 0) { json.put(RATINGCOMPUTED, track.getRating_Computed()); }
    if (track.getSort_Series() != null && track.getSort_Series().length() > 0) { json.put(SORTSERIES, track.getSort_Series()); }

    return json;
  }
}
