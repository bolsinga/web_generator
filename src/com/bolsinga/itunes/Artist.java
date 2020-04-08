package com.bolsinga.itunes;

import java.util.*;

public class Artist {

  static final Comparator<Album> ALBUM_ORDER_COMPARATOR = new Comparator<Album>() {
    public int compare(final Album r1, final Album r2) {
      // The Integer.MAX_VALUE assures that 'unknown' album dates are after the known ones.
      int date1 = (r1.getReleaseYear() != Album.UNKNOWN_YEAR) ? r1.getReleaseYear() : Integer.MAX_VALUE;
      int date2 = (r2.getReleaseYear() != Album.UNKNOWN_YEAR) ? r2.getReleaseYear() : Integer.MAX_VALUE;
      int result = date1 - date2;
      if (result == 0) {
        result = ALBUM_COMPARATOR.compare(r1, r2);
      }
      return result;
    }
  };

  private static final Comparator<Artist> ARTIST_COMPARATOR = new Comparator<Artist>() {
    public int compare(final Artist r1, final Artist r2) {
      String n1 = r1.getSortname();
      if (n1 == null) {
        n1 = r1.getName();
      }
      String n2 = r2.getSortname();
      if (n2 == null) {
        n2 = r2.getName();
      }

      return com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(n1, n2);
    }
  };

  private static final Comparator<Album> ALBUM_COMPARATOR = new Comparator<Album>() {
    public int compare(final Album r1, final Album r2) {
      int result = com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(r1.getTitle(), r2.getTitle());
      if (result == 0) {
        result = ARTIST_COMPARATOR.compare(r1.getArtist(), r2.getArtist());
      }
      return result;
    }
  };

  private final String fName;
  private final String fSortName;
  private final List<Album> fAlbums;

  Artist(final String name, final String sortName) {
    fName = name;
    fSortName = sortName;
    fAlbums = new ArrayList<Album>();
  }

  public String getName() {
    return fName;
  }

  public String getSortname() {
    return fSortName;
  }

  public List<Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }

  void addAlbum(Album album) {
    fAlbums.add(album);
  }

  void sortAlbums() {
    Collections.sort(fAlbums, ALBUM_ORDER_COMPARATOR);
  }
}
