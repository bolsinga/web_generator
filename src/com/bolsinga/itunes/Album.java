package com.bolsinga.itunes;

import java.util.*;

public class Album {
  public static final int UNKNOWN_YEAR = 0;

  private static final Comparator<Song> SONG_ORDER_COMPARATOR = new Comparator<Song>() {
    public int compare(final Song r1, final Song r2) {
      int discCompare = r1.getDiscIndex() - r2.getDiscIndex();
      if (discCompare == 0) {
        return r1.getTrack() - r2.getTrack();
      } else {
        return discCompare;
      }
    }
  };

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
    Collections.sort(fSongs, SONG_ORDER_COMPARATOR);
  }
}
