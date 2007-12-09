package com.bolsinga.music.data.raw;

import java.util.*;

public class Music implements com.bolsinga.music.data.Music {

  private GregorianCalendar fDate = null;
  private List<com.bolsinga.music.data.Venue> fVenues;
  private List<com.bolsinga.music.data.Artist> fArtists;
  private List<com.bolsinga.music.data.Label> fLabels;
  private List<com.bolsinga.music.data.Relation> fRelations;
  private List<com.bolsinga.music.data.Song> fSongs;
  private List<com.bolsinga.music.data.Album> fAlbums;
  private List<com.bolsinga.music.data.Show> fShows;

  public static Music create(final String showsFile, final String venueFile, final String bandFile, final String relationFile, final String iTunesFile) throws com.bolsinga.web.WebException {
    List<com.bolsinga.music.data.Venue> venues = Venue.create(venueFile);
    List<com.bolsinga.music.data.Show> shows = Show.create(showsFile);

    List<com.bolsinga.itunes.Parser.Album> itAlbums = null;
    com.bolsinga.itunes.Parser parser = new com.bolsinga.itunes.Parser();
    try {
      itAlbums = parser.parse(iTunesFile);
    } catch (com.bolsinga.itunes.ParserException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't parse iTunes file: ");
      sb.append(iTunesFile);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }

    List<com.bolsinga.music.data.Album> albums = new ArrayList<com.bolsinga.music.data.Album>();
    List<com.bolsinga.music.data.Song> songs = new ArrayList<com.bolsinga.music.data.Song>();

    for (com.bolsinga.itunes.Parser.Album itAlbum : itAlbums) {
      List<com.bolsinga.music.data.Artist> albumArtists = new ArrayList<com.bolsinga.music.data.Artist>();
      
      com.bolsinga.music.data.Artist albumArtist = null;
      com.bolsinga.itunes.Parser.Artist itArtist = itAlbum.getArtist();
      if (itArtist != null) {
        albumArtist = Artist.createOrGet(itArtist.getName());
        String sortName = itArtist.getSortname();
        if (sortName != null) {
          albumArtist.setSortname(sortName);
        }
        albumArtists.add(albumArtist);
      }
      
      com.bolsinga.music.data.Date albumReleaseYear = null;
      if (itAlbum.getReleaseYear() != com.bolsinga.itunes.Parser.Album.UNKNOWN_YEAR) {
        albumReleaseYear = Date.create(itAlbum.getReleaseYear());
      }
      
      List<com.bolsinga.itunes.Parser.Song> itAlbumSongs = itAlbum.getSongs();
      List<com.bolsinga.music.data.Song> albumSongs = new ArrayList<com.bolsinga.music.data.Song>(itAlbumSongs.size());
      
      for (com.bolsinga.itunes.Parser.Song itAlbumSong : itAlbumSongs) {
          com.bolsinga.music.data.Artist songArtist = null;
          com.bolsinga.itunes.Parser.Artist itSongArtist = itAlbumSong.getArtist();
          if (itSongArtist != null) {
            songArtist = Artist.createOrGet(itSongArtist.getName());
            String sortName = itSongArtist.getSortname();
            if (sortName != null) {
              songArtist.setSortname(sortName);
            }
            albumArtists.add(songArtist);
          }

          com.bolsinga.music.data.Date songReleaseYear = null;
          if (itAlbumSong.getReleaseYear() != com.bolsinga.itunes.Parser.Album.UNKNOWN_YEAR) {
            songReleaseYear = Date.create(itAlbumSong.getReleaseYear());
          }

          com.bolsinga.music.data.Song song = Song.create(songs.size(), itAlbumSong.getTitle(), songArtist, itAlbumSong.getLastPlayed(), itAlbumSong.getPlayCount(), itAlbumSong.getGenre(), songReleaseYear, itAlbumSong.getTrack());
          albumSongs.add(song);
          songs.add(song);
      }
      
      com.bolsinga.music.data.Album album = Album.create(albums.size(), itAlbum.getTitle(), albumArtist, albumReleaseYear, albumSongs);
      albums.add(album);
      
      for (com.bolsinga.music.data.Artist artist : albumArtists) {
        ((com.bolsinga.music.data.raw.Artist)artist).addAlbum(album);
      }
    }

    // This sets all of the artist IDs
    List<com.bolsinga.music.data.Artist> artists = Artist.getList(bandFile);

    // This needs to be read after all artists are created
    List<com.bolsinga.music.data.Relation> relations = Relation.create(relationFile);

    // Not yet used.
    List<com.bolsinga.music.data.Label> labels = new ArrayList<com.bolsinga.music.data.Label>();
    
    return new Music(venues, artists, labels, relations, songs, albums, shows);
  }

  private Music(final List<com.bolsinga.music.data.Venue> venues, final List<com.bolsinga.music.data.Artist> artists, final List<com.bolsinga.music.data.Label> labels, final List<com.bolsinga.music.data.Relation> relations, final List<com.bolsinga.music.data.Song> songs, final List<com.bolsinga.music.data.Album> albums, final List<com.bolsinga.music.data.Show> shows) {
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
  
  public List<com.bolsinga.music.data.Venue> getVenues() {
    return Collections.unmodifiableList(fVenues);
  }
  
  public List<com.bolsinga.music.data.Venue> getVenuesCopy() {
    return new ArrayList<com.bolsinga.music.data.Venue>(fVenues);
  }
  
  public List<com.bolsinga.music.data.Artist> getArtists() {
    return Collections.unmodifiableList(fArtists);
  }
  
  public List<com.bolsinga.music.data.Artist> getArtistsCopy() {
    return new ArrayList<com.bolsinga.music.data.Artist>(fArtists);
  }
  
  public List<com.bolsinga.music.data.Label> getLabels() {
    return Collections.unmodifiableList(fLabels);
  }
  
  public List<com.bolsinga.music.data.Label> getLabelsCopy() {
    return new ArrayList<com.bolsinga.music.data.Label>(fLabels);
  }
  
  public List<com.bolsinga.music.data.Relation> getRelations() {
    return Collections.unmodifiableList(fRelations);
  }
  
  public List<com.bolsinga.music.data.Relation> getRelationsCopy() {
    return new ArrayList<com.bolsinga.music.data.Relation>(fRelations);
  }
  
  public List<com.bolsinga.music.data.Song> getSongs() {
    return Collections.unmodifiableList(fSongs);
  }
  
  public List<com.bolsinga.music.data.Song> getSongsCopy() {
    return new ArrayList<com.bolsinga.music.data.Song>(fSongs);
  }
  
  public List<com.bolsinga.music.data.Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }
  
  public List<com.bolsinga.music.data.Album> getAlbumsCopy() {
    return new ArrayList<com.bolsinga.music.data.Album>(fAlbums);
  }
  
  public List<com.bolsinga.music.data.Show> getShows() {
    return Collections.unmodifiableList(fShows);
  }
  
  public List<com.bolsinga.music.data.Show> getShowsCopy() {
    return new ArrayList<com.bolsinga.music.data.Show>(fShows);
  }
}