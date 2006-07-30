package com.bolsinga.shows.converter;

import java.util.*;

public class Compare {
  private static int convert(final String d) {
    String monthString, dayString, yearString = null;
    int month, day, year = 0;
                
    StringTokenizer st = new StringTokenizer(d, "-");
                
    monthString = st.nextToken();
    dayString = st.nextToken();
    yearString = st.nextToken();
                
    month = Integer.parseInt(monthString);
    day = Integer.parseInt(dayString);
    year = Integer.parseInt(yearString);

    return (year * 10000) + (month * 100) + day;
  }

  public static final Comparator<Venue> VENUE_COMPARATOR = new Comparator<Venue>() {
      public int compare(final Venue r1, final Venue r2) {
        return com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(r1.getName(), r2.getName());
      }
    };
        
  public static final Comparator<Show> SHOW_COMPARATOR = new Comparator<Show>() {
      public int compare(final Show r1, final Show r2) {
        int result = convert(r1.getDate()) - convert(r2.getDate());
        if (result == 0) {
          result = com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(r1.getVenue(), r2.getVenue());
          if (result == 0) {
            result = ((String)r1.getBands().get(0)).compareToIgnoreCase(((String)r2.getBands().get(0)));
          }
        }
        return result;
      }
    };
        
  public static final Comparator<Comments> COMMENT_COMPARATOR = new Comparator<Comments>() {
      public int compare(final Comments r1, final Comments r2) {
        return convert(r1.getDate()) - convert(r2.getDate());
      }
    };
}
