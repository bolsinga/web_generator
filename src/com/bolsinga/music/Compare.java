package com.bolsinga.music;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.bind.*;

import com.bolsinga.music.data.xml.impl.*;

import com.bolsinga.web.*;

public class Compare {

  private static final String THE  = "the ";
  private static final String THEE = "thee ";
  private static final String A    = "a ";
  private static final String AN   = "an ";
    
  private static final Pattern sChomp = Pattern.compile("^\\W+");
        
  private static Compare sCompare = null;
        
  private final Music fMusic;
        
  public synchronized static Compare getCompare(final Music music) {
    if (sCompare == null) {
      sCompare = new Compare(music);
    }
    return sCompare;
  }
        
  private Compare(final Music music) {
    fMusic = music;
  }
        
  private static int convert(final com.bolsinga.music.data.xml.impl.Date d) {
    // Converts to an unusually obtained integer (I believe it assures where 'unknown' dates get sorted)
    return ((d.getYear() != null) ? d.getYear().intValue() * 10000 : 0) +
      ((d.getMonth() != null) ? d.getMonth().intValue() * 100 : 0) +
      ((d.getDay() != null) ? d.getDay().intValue() : 0);
  }
  
  private static int convertMonth(final com.bolsinga.music.data.xml.impl.Date d) {
    return ((d.getMonth() != null) ? d.getMonth().intValue() * 100 : 0);
  }
                
  public static String simplify(final String s) {
    String lower = s.toLowerCase();
    int len = s.length();
    if (lower.startsWith(THE)) {
      lower = lower.substring(THE.length(), len);
    }
    if (lower.startsWith(A)) {
      lower = lower.substring(A.length(), len);
    }
    if (lower.startsWith(AN)) {
      lower = lower.substring(AN.length(), len);
    }
    if (lower.startsWith(THEE)) {
      lower = lower.substring(THEE.length(), len);
    }
                
    Matcher m = sChomp.matcher(lower);
    String result = m.replaceFirst("");
    if ((result != null) && (result.length() > 0)) {
      return result;
    }

    return lower;
  }

  private static final Comparator<Artist> ARTIST_ID_COMPARATOR = new Comparator<Artist>() {
      public int compare(final Artist i1, final Artist i2) {
        return i1.getId().compareTo(i2.getId());
      }
    };

  private static final Comparator<Album> ALBUM_ID_COMPARATOR = new Comparator<Album>() {
      public int compare(final Album i1, final Album i2) {
        return i1.getId().compareTo(i2.getId());
      }
    };

  private static final Comparator<JAXBElement<Object>> JAXB_ALBUM_ID_COMPARATOR = new Comparator<JAXBElement<Object>>() {
      public int compare(final JAXBElement<Object> i1, final JAXBElement<Object> i2) {
        Album a1 = (Album)i1.getValue();
        Album a2 = (Album)i2.getValue();
        return ALBUM_ID_COMPARATOR.compare(a1, a2);
      }
    };

  private static final Comparator<Venue> VENUE_ID_COMPARATOR = new Comparator<Venue>() {
      public int compare(final Venue i1, final Venue i2) {
        return i1.getId().compareTo(i2.getId());
      }
    };

  private static final Comparator<Song> SONG_ID_COMPARATOR = new Comparator<Song>() {
      public int compare(final Song i1, final Song i2) {
        return i1.getId().compareTo(i2.getId());
      }
    };

  private static int convert(final int index, final String id) {
    return Integer.valueOf(id.substring(index)).intValue();
  }

  private static final Comparator<JAXBElement<Object>> RELATION_ID_COMPARATOR = new Comparator<JAXBElement<Object>>() {
      public int compare(final JAXBElement<Object> jo1, final JAXBElement<Object> jo2) {
        int id1 = -1;
        int id2 = -1;
        Object o1 = (Object)jo1.getValue();
        Object o2 = (Object)jo2.getValue();

        if (o1 instanceof Artist) {
          if (o2 instanceof Artist) {
            Artist a1 = (Artist)o1;
            Artist a2 = (Artist)o2;
            
            // 'ar'
            id1 = convert(2, a1.getId());
            id2 = convert(2, a2.getId());
          } else {
            System.err.println("Relation not Artist: " + o2);
          }
        } else if (o1 instanceof Venue) {
          if (o2 instanceof Venue) {
            Venue v1 = (Venue)o1;
            Venue v2 = (Venue)o2;

            // 'v'
            id1 = convert(1, v1.getId());
            id2 = convert(1, v2.getId());
          } else {
            System.err.println("Relation not Venue: " + o2);
          }
        } else {
          System.err.println("Unknown Relation: " + o1);
        }
        return id1 - id2;
      }
    };

  public static void tidy(final Music music) {
    // This is run during testing only, so modificaiton is expected.
    List<Artist> artists = music.getArtist();
    Collections.sort(artists, Compare.ARTIST_ID_COMPARATOR);
    for (Artist a : artists) {
      Collections.sort(a.getAlbum(), Compare.JAXB_ALBUM_ID_COMPARATOR);
    }
    Collections.sort(music.getAlbum(), Compare.ALBUM_ID_COMPARATOR);
    Collections.sort(music.getVenue(), Compare.VENUE_ID_COMPARATOR);
    Collections.sort(music.getSong(), Compare.SONG_ID_COMPARATOR);
    for (Relation r : music.getRelation()) {
      Collections.sort(r.getMember(), Compare.RELATION_ID_COMPARATOR);
    }
  }

  public static final Comparator<String> LIBRARY_COMPARATOR = new Comparator<String>() {
      public int compare(final String s1, final String s2) {
        return simplify(s1).compareToIgnoreCase(simplify(s2));
      }
    };
        
  private static final Comparator<com.bolsinga.music.data.xml.impl.Date> DATE_COMPARATOR = new Comparator<com.bolsinga.music.data.xml.impl.Date>() {
      public int compare(final com.bolsinga.music.data.xml.impl.Date r1, final com.bolsinga.music.data.xml.impl.Date r2) {
        return convert(r1) - convert(r2);
      }
    };
    
  public static final Comparator<com.bolsinga.music.data.xml.impl.Date> DATE_MONTH_COMPARATOR = new Comparator<com.bolsinga.music.data.xml.impl.Date>() {
    public int compare(final com.bolsinga.music.data.xml.impl.Date r1, final com.bolsinga.music.data.xml.impl.Date r2) {
      return convertMonth(r1) - convertMonth(r2);
    }
  };
        
  public static final Comparator<Venue> VENUE_COMPARATOR = new Comparator<Venue>() {
      public int compare(final Venue r1, final Venue r2) {
        return LIBRARY_COMPARATOR.compare(r1.getName(), r2.getName());
      }
    };
        
  public final Comparator<Venue> VENUE_STATS_COMPARATOR = new Comparator<Venue>() {
      public int compare(final Venue r1, final Venue r2) {
        Collection<Show> shows1 = Lookup.getLookup(fMusic).getShows(r1);
        int sets1 = (shows1 != null) ? shows1.size() : 0;
        Collection<Show> shows2 = Lookup.getLookup(fMusic).getShows(r2);
        int sets2 = (shows2 != null) ? shows2.size() : 0;
                        
        int result = sets2 - sets1;
        if (result == 0) {
          result = VENUE_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public static final Comparator<Artist> ARTIST_COMPARATOR = new Comparator<Artist>() {
      public int compare(final Artist r1, final Artist r2) {
        String n1 = r1.getSortname();
        if (n1 == null) {
          n1 = r1.getName();
        }
        String n2 = r2.getSortname();
        if (n2 == null) {
          n2 = r2.getName();
        }
                        
        return LIBRARY_COMPARATOR.compare(n1, n2);
      }
    };

  public static final Comparator<Label> LABEL_COMPARATOR = new Comparator<Label>() {
      public int compare(final Label r1, final Label r2) {
        return LIBRARY_COMPARATOR.compare(r1.getName(), r2.getName());
      }
    };

  public static final Comparator<Album> ALBUM_COMPARATOR = new Comparator<Album>() {
      public int compare(final Album r1, final Album r2) {
        int result = LIBRARY_COMPARATOR.compare(r1.getTitle(), r2.getTitle());
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare((Artist)r1.getPerformer(), (Artist)r2.getPerformer());
        }
        return result;
      }
    };

  public static final Comparator<Song> SONG_ORDER_COMPARATOR = new Comparator<Song>() {
      public int compare(final Song r1, final Song r2) {
        return ((r1.getTrack() != null) ? r1.getTrack().intValue() : 0) - ((r2.getTrack() != null) ? r2.getTrack().intValue() : 0);
      }
    };

  public static final Comparator<JAXBElement<Object>> JAXB_SONG_ORDER_COMPARATOR = new Comparator<JAXBElement<Object>>() {
    public int compare(final JAXBElement<Object> o1, final JAXBElement<Object> o2) {
      Song s1 = (Song)o1.getValue();
      Song s2 = (Song)o2.getValue();
      return SONG_ORDER_COMPARATOR.compare(s1, s2);
    }
  };

  public static final Comparator<Album> ALBUM_ORDER_COMPARATOR = new Comparator<Album>() {
      public int compare(final Album r1, final Album r2) {
        // The Integer.MAX_VALUE assures that 'unknown' album dates are after the known ones.
        int date1 = (r1.getReleaseDate() != null) ? r1.getReleaseDate().getYear().intValue() : Integer.MAX_VALUE;
        int date2 = (r2.getReleaseDate() != null) ? r2.getReleaseDate().getYear().intValue() : Integer.MAX_VALUE;
        int result = date1 - date2;
        if (result == 0) {
          result = ALBUM_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };

  public static final Comparator<JAXBElement<Object>> JAXB_ALBUM_ORDER_COMPARATOR = new Comparator<JAXBElement<Object>>() {
    public int compare(final JAXBElement<Object> o1, final JAXBElement<Object> o2) {
      Album a1 = (Album)o1.getValue();
      Album a2 = (Album)o2.getValue();
      return ALBUM_ORDER_COMPARATOR.compare(a1, a2);
    }
  };

  public static final Comparator<Artist> ARTIST_TRACKS_COMPARATOR = new Comparator<Artist>() {
      public int compare(final Artist r1, final Artist r2) {
        int tracks1 = Util.trackCount(r1);
        int tracks2 = Util.trackCount(r2);
                        
        int result = tracks2 - tracks1;
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public static final Comparator<Artist> ARTIST_ALBUMS_COMPARATOR = new Comparator<Artist>() {
      public int compare(final Artist r1, final Artist r2) {
        List<JAXBElement<Object>> albums1 = Util.getAlbumsUnmodifiable(r1);
        List<JAXBElement<Object>> albums2 = Util.getAlbumsUnmodifiable(r2);
        
        int albumsCount1 = (albums1 != null) ? albums1.size() : 0;
        int albumsCount2 = (albums2 != null) ? albums2.size() : 0;
                        
        int result = albumsCount2 - albumsCount1;
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public final Comparator<Artist> ARTIST_STATS_COMPARATOR = new Comparator<Artist>() {
      public int compare(final Artist r1, final Artist r2) {
        Collection<Show> shows1 = Lookup.getLookup(fMusic).getShows(r1);
        Collection<Show> shows2 = Lookup.getLookup(fMusic).getShows(r2);
        int sets1 = (shows1 != null) ? shows1.size() : 0;
        int sets2 = (shows2 != null) ? shows2.size() : 0;
                        
        int result = sets2 - sets1;
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public static final Comparator<Show> SHOW_COMPARATOR = new Comparator<Show>() {
      public int compare(final Show r1, final Show r2) {
        int result = DATE_COMPARATOR.compare(r1.getDate(), r2.getDate());
        if (result == 0) {
          result = VENUE_COMPARATOR.compare((Venue)r1.getVenue(), (Venue)r2.getVenue());
          if (result == 0) {
            Artist a1 = (Artist)r1.getArtist().get(0).getValue();
            Artist a2 = (Artist)r2.getArtist().get(0).getValue();
            result = ARTIST_COMPARATOR.compare(a1, a2);
          }
        }
        return result;
      }
    };
        
  public static final Comparator<Show> SHOW_STATS_COMPARATOR = new Comparator<Show>() {
      public int compare(final Show r1, final Show r2) {
        com.bolsinga.music.data.xml.impl.Date d1 = r1.getDate();
        com.bolsinga.music.data.xml.impl.Date d2 = r2.getDate();

        return ((d1.getYear() != null) ? d1.getYear().intValue() : 0) - ((d2.getYear() != null) ? d2.getYear().intValue() : 0);
      }
    };
}
