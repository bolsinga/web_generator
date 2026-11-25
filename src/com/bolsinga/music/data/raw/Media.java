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

    Media media = new Media(albums, songs, sortnames);
    return media;
  }

  private Media(final List<Album> albums, final List<Song> songs, final Map<String, String> sortnames) {
    fAlbums = albums;
    fSongs = songs;
    fSortnames = sortnames;
  }
}