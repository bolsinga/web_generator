package com.bolsinga.music.data.raw;

import java.util.*;

public class Music implements com.bolsinga.music.data.Music {

  private GregorianCalendar fDate = null;
  private List<Venue> fVenues;
  private List<Artist> fArtists;
  private List<Label> fLabels;
  private List<Relation> fRelations;
  private List<Song> fSongs;
  private List<Album> fAlbums;
  private List<Show> fShows;

  public static Music create(final String showsFile, final String venueFile, final String bandFile, final String relationFile, final String iTunesFile) throws com.bolsinga.web.WebException {
    List<Venue> venues = Venue.create(venueFile);
    List<Show> shows = Show.create(showsFile);

    List<com.bolsinga.itunes.Album> itAlbums = null;
    com.bolsinga.itunes.Parser parser = new com.bolsinga.itunes.Parser();
    try {
      itAlbums = parser.parse(iTunesFile);
    } catch (com.bolsinga.itunes.ParserException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't parse iTunes file: ");
      sb.append(iTunesFile);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }

    List<Album> albums = new ArrayList<Album>();
    List<Song> songs = new ArrayList<Song>();

    for (com.bolsinga.itunes.Album itAlbum : itAlbums) {
      List<Artist> albumArtists = new ArrayList<Artist>();
      
      Artist albumArtist = null;
      com.bolsinga.itunes.Artist itArtist = itAlbum.getArtist();
      if (itArtist != null) {
        albumArtist = Artist.createOrGet(itArtist.getName());
        String sortName = itArtist.getSortname();
        if (sortName != null) {
          albumArtist.setSortname(sortName);
        }
        albumArtists.add(albumArtist);
      }
      
      com.bolsinga.music.data.Date albumReleaseYear = null;
      if (itAlbum.getReleaseYear() != com.bolsinga.itunes.Album.UNKNOWN_YEAR) {
        albumReleaseYear = Date.create(itAlbum.getReleaseYear());
      }
      
      List<com.bolsinga.itunes.Song> itAlbumSongs = itAlbum.getSongs();
      List<Song> albumSongs = new ArrayList<Song>(itAlbumSongs.size());
      
      for (com.bolsinga.itunes.Song itAlbumSong : itAlbumSongs) {
          Artist songArtist = null;
          com.bolsinga.itunes.Artist itSongArtist = itAlbumSong.getArtist();
          if (itSongArtist != null) {
            songArtist = Artist.createOrGet(itSongArtist.getName());
            String sortName = itSongArtist.getSortname();
            if (sortName != null) {
              songArtist.setSortname(sortName);
            }
            albumArtists.add(songArtist);
          }

          com.bolsinga.music.data.Date songReleaseYear = null;
          if (itAlbumSong.getReleaseYear() != com.bolsinga.itunes.Album.UNKNOWN_YEAR) {
            songReleaseYear = Date.create(itAlbumSong.getReleaseYear());
          }

          Song song = Song.create(songs.size(), itAlbumSong.getTitle(), songArtist, itAlbumSong.getLastPlayed(), itAlbumSong.getPlayCount(), itAlbumSong.getGenre(), songReleaseYear, itAlbumSong.getTrack());
          albumSongs.add(song);
          songs.add(song);
      }
      
      Album album = Album.create(albums.size(), itAlbum.getTitle(), albumArtist, albumReleaseYear, albumSongs);
      albums.add(album);
      
      for (Artist artist : albumArtists) {
        artist.addAlbum(album);
      }
    }

    // This sets all of the artist IDs
    List<Artist> artists = Artist.getList(bandFile);

    // This needs to be read after all artists are created
    List<Relation> relations = Relation.create(relationFile);

    // Not yet used.
    List<Label> labels = new ArrayList<Label>();
    
    return new Music(venues, artists, labels, relations, songs, albums, shows);
  }

  private Music(final List<Venue> venues, final List<Artist> artists, final List<Label> labels, final List<Relation> relations, final List<Song> songs, final List<Album> albums, final List<Show> shows) {
    fDate = com.bolsinga.web.Util.nowUTC();
    fVenues = venues;
    fArtists = artists;
    fLabels = labels;
    fRelations = relations;
    fSongs = songs;
    fAlbums = albums;
    fShows = shows;
  }
  
  public GregorianCalendar getTimestamp() {
      return fDate;
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    fDate = timestamp;
  }
  
  public List<Venue> getVenues() {
    return Collections.unmodifiableList(fVenues);
  }
  
  public List<Venue> getVenuesCopy() {
    return new ArrayList<Venue>(fVenues);
  }
  
  public List<Artist> getArtists() {
    return Collections.unmodifiableList(fArtists);
  }
  
  public List<Artist> getArtistsCopy() {
    return new ArrayList<Artist>(fArtists);
  }
  
  public List<Label> getLabels() {
    return Collections.unmodifiableList(fLabels);
  }
  
  public List<Label> getLabelsCopy() {
    return new ArrayList<Label>(fLabels);
  }
  
  public List<Relation> getRelations() {
    return Collections.unmodifiableList(fRelations);
  }
  
  public List<Relation> getRelationsCopy() {
    return new ArrayList<Relation>(fRelations);
  }
  
  public List<Song> getSongs() {
    return Collections.unmodifiableList(fSongs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(fSongs);
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(fAlbums);
  }
  
  public List<Show> getShows() {
    return Collections.unmodifiableList(fShows);
  }
  
  public List<Show> getShowsCopy() {
    return new ArrayList<Show>(fShows);
  }
}