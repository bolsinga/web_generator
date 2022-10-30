package com.bolsinga.itunes;

import java.time.*;
import java.util.*;

public class Song {
  static final int UNKNOWN_TRACK = 0;

  private final Artist fArtist;
  private final String fTitle;
  private final int fReleaseYear;
  private final ZonedDateTime fLastPlayed;
  private final int fTrack;
  private final String fGenre;
  private final int fPlayCount;
  private final int fDiscIndex;

  Song(final Artist artist, final String title, final int year, final ZonedDateTime lastPlayed, final int track, final String genre, final int playCount, final int discIndex) {
    fArtist = artist;
    fTitle = title;
    fReleaseYear = year;
    fLastPlayed = lastPlayed;
    fTrack = track;
    fGenre = genre;
    fPlayCount = playCount;
    fDiscIndex = discIndex;
  }

  public Artist getArtist() {
    return fArtist;
  }

  public String getTitle() {
    return fTitle;
  }

  public int getReleaseYear() {
    return fReleaseYear;
  }

  public ZonedDateTime getLastPlayed() {
    return fLastPlayed;
  }

  public int getTrack() {
    return fTrack;
  }

  public int getDiscIndex() {
    return fDiscIndex;
  }

  public String getGenre() {
    return fGenre;
  }

  public int getPlayCount() {
    return fPlayCount;
  }
}
