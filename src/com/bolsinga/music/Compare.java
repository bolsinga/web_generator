package com.bolsinga.music;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import com.bolsinga.music.data.*;

public class Compare {

  private static final String THE  = "the ";
  private static final String THEE = "thee ";
  private static final String A    = "a ";
  private static final String AN   = "an ";
    
  private static Pattern sChomp = Pattern.compile("\\W*");
        
  private static Compare sCompare = null;
        
  private Music fMusic = null;
        
  public synchronized static Compare getCompare(com.bolsinga.music.data.Music music) {
    if (sCompare == null) {
      sCompare = new Compare(music);
    }
    return sCompare;
  }
        
  private Compare(Music music) {
    fMusic = music;
  }
        
  private static int convert(com.bolsinga.music.data.Date d) {
    return ((d.getYear() != null) ? d.getYear().intValue() * 10000 : 0) +
      ((d.getMonth() != null) ? d.getMonth().intValue() * 100 : 0) +
      ((d.getDay() != null) ? d.getDay().intValue() : 0);
  }
                
  public static String simplify(String s) {
    String lower = s.toLowerCase();
    int len = s.length();
    if (lower.startsWith(THE)) {
      return s.substring(THE.length(), len);
    }
    if (lower.startsWith(A)) {
      return s.substring(A.length(), len);
    }
    if (lower.startsWith(AN)) {
      return s.substring(AN.length(), len);
    }
    if (lower.startsWith(THEE)) {
      return s.substring(THEE.length(), len);
    }
                
    Matcher m = sChomp.matcher(lower);
    String result = m.replaceAll("");
    if ((result != null) && (result.length() > 0)) {
      return result;
    }

    return lower;
  }

  private static final Comparator ARTIST_ID_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Artist i1 = (Artist)o1;
        Artist i2 = (Artist)o2;

        return i1.getId().compareTo(i2.getId());
      }
    };

  private static final Comparator ALBUM_ID_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Album i1 = (Album)o1;
        Album i2 = (Album)o2;

        return i1.getId().compareTo(i2.getId());
      }
    };

  private static final Comparator VENUE_ID_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Venue i1 = (Venue)o1;
        Venue i2 = (Venue)o2;

        return i1.getId().compareTo(i2.getId());
      }
    };

  private static final Comparator SONG_ID_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Song i1 = (Song)o1;
        Song i2 = (Song)o2;

        return i1.getId().compareTo(i2.getId());
      }
    };

  private static int convert(int index, String id) {
    return Integer.valueOf(id.substring(index)).intValue();
  }

  private static final Comparator RELATION_ID_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        int id1 = -1;
        int id2 = -1;

        if (o1 instanceof Artist) {
          if (o2 instanceof Artist) {
            Artist a1 = (Artist)o1;
            Artist a2 = (Artist)o2;
            
            // 'ar'
            id1 = convert(2, a1.getId());
            id2 = convert(2, a2.getId());
          } else {
            System.err.println("Relation not Artist: " + o2);
            System.exit(1);
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
            System.exit(1);
          }
        } else {
          System.err.println("Unknown Relation: " + o1);
          System.exit(1);
        }
        return id1 - id2;
      }
    };

  public static void tidy(Music music) {
    Collections.sort(music.getArtist(), Compare.ARTIST_ID_COMPARATOR);
    Iterator i = music.getArtist().iterator();
    while (i.hasNext()) {
      Artist a = (Artist)i.next();
      Collections.sort(a.getAlbum(), Compare.ALBUM_ID_COMPARATOR);
    }
    Collections.sort(music.getAlbum(), Compare.ALBUM_ID_COMPARATOR);
    Collections.sort(music.getVenue(), Compare.VENUE_ID_COMPARATOR);
    Collections.sort(music.getSong(), Compare.SONG_ID_COMPARATOR);
    i = music.getRelation().iterator();
    while (i.hasNext()) {
      Relation r = (Relation)i.next();
      Collections.sort(r.getMember(), Compare.RELATION_ID_COMPARATOR);
    }
  }
        
  public static final Comparator LIBRARY_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        String s1 = (String)o1;
        String s2 = (String)o2;
        
        return simplify(s1).compareToIgnoreCase(simplify(s2));
      }
    };
        
  public static final Comparator DATE_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        com.bolsinga.music.data.Date r1 = (com.bolsinga.music.data.Date)o1;
        com.bolsinga.music.data.Date r2 = (com.bolsinga.music.data.Date)o2;

        return convert(r1) - convert(r2);
      }
    };
        
  public static final Comparator VENUE_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Venue r1 = (Venue)o1;
        Venue r2 = (Venue)o2;
                        
        return LIBRARY_COMPARATOR.compare(r1.getName(), r2.getName());
      }
    };
        
  public final Comparator VENUE_STATS_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Venue r1 = (Venue)o1;
        Venue r2 = (Venue)o2;

        List shows1 = Lookup.getLookup(fMusic).getShows(r1);
        int sets1 = (shows1 != null) ? shows1.size() : 0;
        List shows2 = Lookup.getLookup(fMusic).getShows(r2);
        int sets2 = (shows2 != null) ? shows2.size() : 0;
                        
        int result = sets2 - sets1;
        if (result == 0) {
          result = VENUE_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public static final Comparator ARTIST_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Artist r1 = (Artist)o1;
        Artist r2 = (Artist)o2;
                        
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

  public static final Comparator ALBUM_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Album r1 = (Album)o1;
        Album r2 = (Album)o2;
                        
        int result = LIBRARY_COMPARATOR.compare(r1.getTitle(), r2.getTitle());
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1.getPerformer(), r2.getPerformer());
        }
        return result;
      }
    };

  public static final Comparator SONG_ORDER_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Song r1 = (Song)o1;
        Song r2 = (Song)o2;
                        
        return ((r1.getTrack() != null) ? r1.getTrack().intValue() : 0) - ((r2.getTrack() != null) ? r2.getTrack().intValue() : 0);
      }
    };

  public static final Comparator ALBUM_ORDER_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Album r1 = (Album)o1;
        Album r2 = (Album)o2;

        // The 3000 assures that 'unknown' album dates are after the known ones.
        return ((r1.getReleaseDate() != null) ? r1.getReleaseDate().getYear().intValue() : 3000) - ((r2.getReleaseDate() != null) ? r2.getReleaseDate().getYear().intValue() : 3000);
      }
    };

  public static final Comparator ARTIST_TRACKS_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Artist r1 = (Artist)o1;
        Artist r2 = (Artist)o2;
                        
        int tracks1 = Util.trackCount(r1);
        int tracks2 = Util.trackCount(r2);
                        
        int result = tracks2 - tracks1;
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public static final Comparator ARTIST_ALBUMS_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Artist r1 = (Artist)o1;
        Artist r2 = (Artist)o2;
                        
        int albums1 = (r1.getAlbum() != null) ? r1.getAlbum().size() : 0;
        int albums2 = (r2.getAlbum() != null) ? r2.getAlbum().size() : 0;
                        
        int result = albums2 - albums1;
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public final Comparator ARTIST_STATS_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Artist r1 = (Artist)o1;
        Artist r2 = (Artist)o2;
                        
        List shows1 = Lookup.getLookup(fMusic).getShows(r1);
        List shows2 = Lookup.getLookup(fMusic).getShows(r2);
        int sets1 = (shows1 != null) ? shows1.size() : 0;
        int sets2 = (shows2 != null) ? shows2.size() : 0;
                        
        int result = sets2 - sets1;
        if (result == 0) {
          result = ARTIST_COMPARATOR.compare(r1, r2);
        }
        return result;
      }
    };
        
  public static final Comparator SHOW_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Show r1 = (Show)o1;
        Show r2 = (Show)o2;
                        
        int result = DATE_COMPARATOR.compare(r1.getDate(), r2.getDate());
        if (result == 0) {
          result = VENUE_COMPARATOR.compare(r1.getVenue(), r2.getVenue());
          if (result == 0) {
            result = ARTIST_COMPARATOR.compare(r1.getArtist().get(0), r2.getArtist().get(0));
          }
        }
        return result;
      }
    };
        
  public static final Comparator SHOW_STATS_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        Show r1 = (Show)o1;
        Show r2 = (Show)o2;
                        
        com.bolsinga.music.data.Date d1 = r1.getDate();
        com.bolsinga.music.data.Date d2 = r2.getDate();

        return ((d1.getYear() != null) ? d1.getYear().intValue() : 0) - ((d2.getYear() != null) ? d2.getYear().intValue() : 0);
      }
    };
}
