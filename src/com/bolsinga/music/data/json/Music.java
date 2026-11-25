package com.bolsinga.music.data.json;

import java.io.*;
import java.time.*;
import java.util.*;

import org.json.*;

public class Music implements com.bolsinga.music.data.Music {
  private static final String TIMESTAMP = "timestamp";
  private static final String VENUES = "venues";
  private static final String ARTISTS = "artists";
  private static final String RELATIONS = "relations";
  private static final String SONGS = "songs";
  private static final String ALBUMS = "albums";
  private static final String SHOWS = "shows";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String SORTNAME = "sortname";

  private String timestamp;
  private List<Venue> venues;
  private List<Artist> artists;
  private List<Relation> relations;
  private List<Song> songs;
  private List<Album> albums;
  private List<Show> shows;

  public static Music create(final String sourceFile) throws com.bolsinga.web.WebException {
    JSONObject json = com.bolsinga.web.Util.createJSON(sourceFile);
    Music.stopOnProblems(sourceFile, Check.problems(json));
    return Music.create(json, sourceFile);
  }

  // Makes Music from a shows file and an iTunes export (RFC 005). iTunes albums and songs are joined
  // to the shows file's artists by name, then read as music JSON.
  public static Music createFromShows(final String showsFile, final String iTunesFile) throws com.bolsinga.web.WebException {
    JSONObject json = com.bolsinga.web.Util.createJSON(showsFile);
    Music.stopOnProblems(showsFile, Check.showsFileProblems(json));

    com.bolsinga.music.data.raw.Media media = com.bolsinga.music.data.raw.Media.createMedia(iTunesFile);

    Map<String, JSONObject> artistsByName = new HashMap<String, JSONObject>();
    JSONArray artists = json.getJSONArray(ARTISTS);
    for (int i = 0; i < artists.length(); i++) {
      JSONObject artist = artists.getJSONObject(i);
      artistsByName.put(artist.getString(NAME), artist);
    }

    // Every artist iTunes names, in the order it names them.
    Set<com.bolsinga.music.data.raw.Artist> iTunesArtists = new LinkedHashSet<com.bolsinga.music.data.raw.Artist>();
    for (com.bolsinga.music.data.raw.Album album : media.fAlbums) {
      if (album.getPerformer() != null) {
        iTunesArtists.add(album.getPerformer());
      }
    }
    for (com.bolsinga.music.data.raw.Song song : media.fSongs) {
      if (song.getPerformer() != null) {
        iTunesArtists.add(song.getPerformer());
      }
    }

    List<String> missing = new ArrayList<String>();
    for (com.bolsinga.music.data.raw.Artist iTunesArtist : iTunesArtists) {
      String name = iTunesArtist.getName();
      JSONObject artist = artistsByName.get(name);
      if (artist == null) {
        missing.add(name);
        continue;
      }
      iTunesArtist.setID(artist.getString(ID));
      // The shows file's sort name wins; nothing is derived from the name.
      String sortname = media.fSortnames.get(name);
      if (sortname != null && !artist.has(SORTNAME)) {
        artist.put(SORTNAME, sortname);
      }
    }
    if (!missing.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append(showsFile);
      sb.append(" is missing ");
      sb.append(missing.size());
      sb.append(" iTunes artist(s); add them, then run again:");
      for (String name : missing) {
        sb.append("\n  ");
        sb.append(name);
      }
      throw new com.bolsinga.web.WebException(sb.toString());
    }

    try {
      json.put(SONGS, Music.createSongsJSON(media.fSongs));
      json.put(ALBUMS, Music.createAlbumsJSON(media.fAlbums));
    } catch (JSONException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't add iTunes to ");
      sb.append(showsFile);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }

    return Music.create(json, showsFile);
  }

  private static void stopOnProblems(final String sourceFile, final List<String> problems) throws com.bolsinga.web.WebException {
    if (!problems.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append(sourceFile);
      sb.append(" has ");
      sb.append(problems.size());
      sb.append(" problem(s):");
      for (String problem : problems) {
        sb.append("\n  ");
        sb.append(problem);
      }
      throw new com.bolsinga.web.WebException(sb.toString());
    }
  }

  private static Music create(final JSONObject json, final String sourceFile) throws com.bolsinga.web.WebException {
    try {
      return new Music(json);
    } catch (Exception e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Cannot create Music from JSON: ");
      sb.append(sourceFile);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }
  
  public static void export(final com.bolsinga.music.data.Music music, final String outputFile, final boolean showsOnly) throws com.bolsinga.web.WebException {
    JSONObject json = null;
    try {
      json = Music.createJSON(music, showsOnly);
    } catch (JSONException e) {
      throw new com.bolsinga.web.WebException("Can't export music to json", e);
    }
    com.bolsinga.web.Util.writeJSON(json, outputFile);
  }

  public static JSONArray createVenuesJSON(final List<? extends com.bolsinga.music.data.Venue> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.music.data.Venue i : items) {
      json.put(Venue.createJSON(i));
    }
    return json;
  }

  public static JSONArray createArtistsJSON(final List<? extends com.bolsinga.music.data.Artist> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.music.data.Artist i : items) {
      json.put(Artist.createJSON(i));
    }
    return json;
  }

  public static JSONArray createRelationsJSON(final List<? extends com.bolsinga.music.data.Relation> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.music.data.Relation i : items) {
      json.put(Relation.createJSON(i));
    }
    return json;
  }

  public static JSONArray createSongsJSON(final List<? extends com.bolsinga.music.data.Song> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.music.data.Song i : items) {
      json.put(Song.createJSON(i));
    }
    return json;
  }

  public static JSONArray createAlbumsJSON(final List<? extends com.bolsinga.music.data.Album> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.music.data.Album i : items) {
      json.put(Album.createJSON(i));
    }
    return json;
  }

  public static JSONArray createShowsJSON(final List<? extends com.bolsinga.music.data.Show> items) throws JSONException {
    JSONArray json = new JSONArray();
    for (final com.bolsinga.music.data.Show i : items) {
      json.put(Show.createJSON(i));
    }
    return json;
  }
  
  static JSONObject createJSON(final com.bolsinga.music.data.Music music, final boolean showsOnly) throws JSONException {
    JSONObject json = new JSONObject();

    json.put(TIMESTAMP, com.bolsinga.web.Util.conformingISO8601String(music.getTimestamp()));
    
    json.put(VENUES, Music.createVenuesJSON(music.getVenues()));
    json.put(ARTISTS, Music.createArtistsJSON(music.getArtists()));
    json.put(RELATIONS, Music.createRelationsJSON(music.getRelations()));
    if (showsOnly) {
      json.put(SONGS, new JSONArray());
      json.put(ALBUMS, new JSONArray());
    } else {
      json.put(SONGS, Music.createSongsJSON(music.getSongs()));
      json.put(ALBUMS, Music.createAlbumsJSON(music.getAlbums()));
    }

    List<? extends com.bolsinga.music.data.Show> shows = music.getShowsCopy();
    Collections.sort(shows, com.bolsinga.music.Compare.SHOW_COMPARATOR);
    json.put(SHOWS, Music.createShowsJSON(shows));
    
    return json;
  }
  
  private Music() {
  
  }

  private Music(final JSONObject json) throws Exception {
    setTimestamp(ZonedDateTime.parse(json.getString(TIMESTAMP)));
    
    JSONArray jsonArray = json.getJSONArray(VENUES);
    venues = new ArrayList<Venue>(jsonArray.length());
    for (int j = 0; j < jsonArray.length(); j++) {
      JSONObject jsonItem = jsonArray.getJSONObject(j);
      venues.add(Venue.createFromJSON(jsonItem));
    }

    // Create all the Artists before Relations, Songs, Albums, Shows
    jsonArray = json.getJSONArray(ARTISTS);
    artists = new ArrayList<Artist>(jsonArray.length());
    for (int j = 0; j < jsonArray.length(); j++) {
      JSONObject jsonItem = jsonArray.getJSONObject(j);
      artists.add(Artist.createFromJSON(jsonItem));
    }

    jsonArray = json.getJSONArray(RELATIONS);
    relations = new ArrayList<Relation>(jsonArray.length());
    for (int j = 0; j < jsonArray.length(); j++) {
      JSONObject jsonItem = jsonArray.getJSONObject(j);
      relations.add(Relation.create(jsonItem));
    }

    jsonArray = json.getJSONArray(SONGS);
    songs = new ArrayList<Song>(jsonArray.length());
    for (int j = 0; j < jsonArray.length(); j++) {
      JSONObject jsonItem = jsonArray.getJSONObject(j);
      songs.add(Song.createFromJSON(jsonItem));
    }

    jsonArray = json.getJSONArray(ALBUMS);
    albums = new ArrayList<Album>(jsonArray.length());
    for (int j = 0; j < jsonArray.length(); j++) {
      JSONObject jsonItem = jsonArray.getJSONObject(j);
      albums.add(Album.createFromJSON(jsonItem));
    }
    for (Artist artist : artists) {
      artist.finishAlbums();
    }

    jsonArray = json.getJSONArray(SHOWS);
    shows = new ArrayList<Show>(jsonArray.length());
    for (int j = 0; j < jsonArray.length(); j++) {
      JSONObject jsonItem = jsonArray.getJSONObject(j);
      shows.add(Show.create(jsonItem));
    }
    java.util.Collections.sort(shows, com.bolsinga.music.Compare.SHOW_COMPARATOR);
  }
  
  public ZonedDateTime getTimestamp() {
    return ZonedDateTime.parse(this.timestamp);
  }
  
  public void setTimestamp(final ZonedDateTime timestamp) {
    this.timestamp = timestamp.toString();
  }
  
  public List<Venue> getVenues() {
    return Collections.unmodifiableList(venues);
  }
  
  public List<Venue> getVenuesCopy() {
    return new ArrayList<Venue>(venues);
  }
  
  public List<Artist> getArtists() {
    return Collections.unmodifiableList(artists);
  }
  
  public List<Artist> getArtistsCopy() {
    return new ArrayList<Artist>(artists);
  }

  public List<Relation> getRelations() {
    return Collections.unmodifiableList(relations);
  }
  
  public List<Relation> getRelationsCopy() {
    return new ArrayList<Relation>(relations);
  }
  
  public List<Song> getSongs() {
    return Collections.unmodifiableList(songs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(songs);
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(albums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(albums);
  }
  
  public List<Show> getShows() {
    return Collections.unmodifiableList(shows);
  }
  
  public List<Show> getShowsCopy() {
    return new ArrayList<Show>(shows);
  }
}
