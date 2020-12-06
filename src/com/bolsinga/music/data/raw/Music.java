package com.bolsinga.music.data.raw;

import java.util.*;

public class Music implements com.bolsinga.music.data.Music {

  private GregorianCalendar fDate = null;
  private List<Venue> fVenues;
  private List<Artist> fArtists;
  private List<Relation> fRelations;
  private List<Song> fSongs;
  private List<Album> fAlbums;
  private List<Show> fShows;

  public static Music create(final String showsFile, final String venueFile, final String bandFile, final String relationFile, final String iTunesFile) throws com.bolsinga.web.WebException {
    List<Venue> venues = Venue.create(venueFile);
    List<Show> shows = Show.create(showsFile);

    Media media = Media.createMedia(iTunesFile);

    // This sets all of the artist IDs
    List<Artist> artists = Artist.getList(bandFile);

    // This needs to be read after all artists are created
    List<Relation> relations = Relation.create(relationFile);

    return new Music(venues, artists, relations, media.fSongs, media.fAlbums, shows);
  }

  private Music(final List<Venue> venues, final List<Artist> artists, final List<Relation> relations, final List<Song> songs, final List<Album> albums, final List<Show> shows) {
    fDate = com.bolsinga.web.Util.nowUTC();
    fVenues = venues;
    fArtists = artists;
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