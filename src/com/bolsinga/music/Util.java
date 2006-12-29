package com.bolsinga.music;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.*;

import com.bolsinga.music.data.*;

public class Util {

  private static final ThreadLocal<DateFormat> sMonthFormat      = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("MMMM");
    }
  };
  private static final ThreadLocal<DateFormat> sWebFormat        = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("M/d/yyyy");
    }
  };
  private static final ThreadLocal<DecimalFormat> sPercentFormat = new ThreadLocal<DecimalFormat>() {
    public DecimalFormat initialValue() {
      return new DecimalFormat("##.##");
    }
  };
        
  public static GregorianCalendar toCalendarUTC(final com.bolsinga.music.data.Date date) {
    Calendar localTime = Calendar.getInstance(); // LocalTime OK
    boolean unknown = com.bolsinga.web.Util.convert(date.isUnknown());
    if (!unknown) {
      int showTime = com.bolsinga.web.Util.getSettings().getShowTime().intValue();
      localTime.clear();
      localTime.set(date.getYear().intValue(), date.getMonth().intValue() - 1, date.getDay().intValue(), showTime, 0);
    } else {
      System.err.println("Can't convert Unknown com.bolsinga.music.data.Date");
      System.exit(1);
    }
    // Convert to UTC
    GregorianCalendar result = com.bolsinga.web.Util.nowUTC();
    result.setTimeInMillis(localTime.getTimeInMillis());
    return result;
  }

  public static String toString(final com.bolsinga.music.data.Date date) {
    boolean unknown = com.bolsinga.web.Util.convert(date.isUnknown());
    if (!unknown) {
      return sWebFormat.get().format(Util.toCalendarUTC(date).getTime());
    } else {
      Object[] args = {   ((date.getMonth() != null) ? date.getMonth() : BigInteger.ZERO),
                          ((date.getDay() != null) ? date.getDay() : BigInteger.ZERO),
                          ((date.getYear() != null) ? date.getYear() : BigInteger.ZERO) };
      return MessageFormat.format(com.bolsinga.web.Util.getResourceString("unknowndate"), args);
    }
  }
        
  public static String toMonth(final com.bolsinga.music.data.Date date) {
    boolean unknown = com.bolsinga.web.Util.convert(date.isUnknown());
    if (!unknown) {
      return sMonthFormat.get().format(Util.toCalendarUTC(date).getTime());
    } else {
      Calendar d = Calendar.getInstance(); // UTC isn't relevant here.
      if (date.getMonth() != null) {
        d.set(Calendar.MONTH, date.getMonth().intValue() - 1);
        return sMonthFormat.get().format(d.getTime());
      } else {
        return com.bolsinga.web.Util.getResourceString("unknownmonth");
      }
    }
  }
        
  public static String toString(final double value) {
    return sPercentFormat.get().format(value);
  }
  
  public static String createTitle(final String resource, final String name) {
    Object[] args = { com.bolsinga.web.Util.toHTMLSafe(name) };
    return MessageFormat.format(com.bolsinga.web.Util.getResourceString(resource), args);
  }

  public static List<Venue> getVenuesUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getVenue());
  }

  public static List<Venue> getVenuesCopy(final Music music) {
    return new ArrayList<Venue>(music.getVenue());
  }

  public static List<Artist> getArtistsUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getArtist());
  }

  public static List<Artist> getArtistsCopy(final Music music) {
    return new ArrayList<Artist>(music.getArtist());
  }

  public static List<Label> getLabelsUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getLabel());
  }

  public static List<Label> getLabelsCopy(final Music music) {
    return new ArrayList<Label>(music.getLabel());
  }

  public static List<Relation> getRelationsUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getRelation());
  }

  public static List<Relation> getRelationsCopy(final Music music) {
    return new ArrayList<Relation>(music.getRelation());
  }

  public static List<Song> getSongsUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getSong());
  }

  public static List<Song> getSongsCopy(final Music music) {
    return new ArrayList<Song>(music.getSong());
  }

  public static List<Album> getAlbumsUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getAlbum());
  }

  public static List<Album> getAlbumsCopy(final Music music) {
    return new ArrayList<Album>(music.getAlbum());
  }

  public static List<Show> getShowsUnmodifiable(final Music music) {
    return Collections.unmodifiableList(music.getShow());
  }

  public static List<Show> getShowsCopy(final Music music) {
    return new ArrayList<Show>(music.getShow());
  }
  
  public static List<JAXBElement<Object>> getAlbumsUnmodifiable(final Artist artist) {
    return Collections.unmodifiableList(artist.getAlbum());
  }
  
  public static List<JAXBElement<Object>> getAlbumsCopy(final Artist artist) {
    return new ArrayList<JAXBElement<Object>>(artist.getAlbum());
  }

  public static List<JAXBElement<Object>> getSongsUnmodifiable(final Album album) {
    return Collections.unmodifiableList(album.getSong());
  }
  
  public static List<JAXBElement<Object>> getSongsCopy(final Album album) {
    return new ArrayList<JAXBElement<Object>>(album.getSong());
  }

  public static Music createMusic(final String sourceFile) {
    Music music = null;
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
      Unmarshaller u = jc.createUnmarshaller();
                        
      music = (Music)u.unmarshal(new java.io.FileInputStream(sourceFile));
    } catch (Exception ume) {
      System.err.println("Exception: " + ume);
      ume.printStackTrace();
      System.exit(1);
    }
    return music;
  }
        
  public static int trackCount(final Artist artist) {
    int tracks = 0;
    List<JAXBElement<Object>> albums = Util.getAlbumsUnmodifiable(artist);
    if (albums != null) {
      for (JAXBElement<Object> jalbum : albums) {
        Album album = (Album)jalbum.getValue();
        List<JAXBElement<Object>> songs = Util.getSongsUnmodifiable(album);
        for (JAXBElement<Object> jsong : songs) {
          Song song = (Song)jsong.getValue();
          if (song.getPerformer().equals(artist)) {
            tracks++;
          }
        }
      }
    }
                
    return tracks;
  }
}
