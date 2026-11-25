package com.bolsinga.music.data.raw;

import java.util.*;

public class Media {
  public List<Album> fAlbums;
  public List<Song> fSongs;
  // The sort names iTunes gave, by artist name, decomposed (NFD) as Artist.setSortname makes them.
  // The artists' own sort names can also be derived from their names; these can't.
  public Map<String, String> fSortnames;

  public static Media createMedia(final String iTunesFile) throws com.bolsinga.web.WebException {
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
    Map<String, String> sortnames = new HashMap<String, String>();

    for (com.bolsinga.itunes.Album itAlbum : itAlbums) {
      List<Artist> albumArtists = new ArrayList<Artist>();

      Artist albumArtist = null;
      com.bolsinga.itunes.Artist itArtist = itAlbum.getArtist();
      if (itArtist != null) {
        albumArtist = Artist.createOrGet(itArtist.getName());
        String sortName = itArtist.getSortname();
        if (sortName != null) {
          albumArtist.setSortname(sortName);
          sortnames.put(albumArtist.getName(), albumArtist.getSortname());
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
              sortnames.put(songArtist.getName(), songArtist.getSortname());
            }
            albumArtists.add(songArtist);
          }

          com.bolsinga.music.data.Date songReleaseYear = null;
          if (itAlbumSong.getReleaseYear() != com.bolsinga.itunes.Album.UNKNOWN_YEAR) {
            songReleaseYear = Date.create(itAlbumSong.getReleaseYear());
          }

          Song song = Song.create(Media.getSongID(itAlbumSong), itAlbumSong.getTitle(), songArtist, itAlbumSong.getLastPlayed(), itAlbumSong.getPlayCount(), itAlbumSong.getGenre(), songReleaseYear, itAlbumSong.getTrack());
          albumSongs.add(song);
          songs.add(song);
      }

      Album album = Album.create(Media.getAlbumID(itAlbumSongs), itAlbum.getTitle(), albumArtist, albumReleaseYear, albumSongs);
      albums.add(album);

      for (Artist artist : albumArtists) {
        artist.addAlbum(album);
      }
    }

    Media media = new Media(albums, songs, sortnames);
    return media;
  }

  // IDs come from iTunes' persistent IDs, which stay the same for a track as long as it is in the
  // library, so a new album or song doesn't renumber the others (#236). A song's ID is its track's
  // persistent ID, as a decimal number. An album's is its songs' smallest persistent ID, so it only
  // changes if that song is removed or a song with a smaller one is added to the album.
  private static String getSongID(final com.bolsinga.itunes.Song song) throws com.bolsinga.web.WebException {
    String id = song.getPersistentID();
    try {
      Long.parseUnsignedLong(id);
    } catch (NumberFormatException | NullPointerException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("iTunes track has no persistentID, or one that isn't a 64-bit number: \"");
      sb.append(song.getTitle());
      sb.append("\" ");
      sb.append(id);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
    return id;
  }

  private static String getAlbumID(final List<com.bolsinga.itunes.Song> songs) throws com.bolsinga.web.WebException {
    String result = null;
    for (com.bolsinga.itunes.Song song : songs) {
      String id = Media.getSongID(song);
      if (result == null || Long.compareUnsigned(Long.parseUnsignedLong(id), Long.parseUnsignedLong(result)) < 0) {
        result = id;
      }
    }
    return result;
  }

  private Media(final List<Album> albums, final List<Song> songs, final Map<String, String> sortnames) {
    fAlbums = albums;
    fSongs = songs;
    fSortnames = sortnames;
  }
}