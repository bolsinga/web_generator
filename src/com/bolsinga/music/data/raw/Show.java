package com.bolsinga.music.data.raw;

import java.io.*;
import java.util.*;

public class Show implements com.bolsinga.music.data.Show {

  private static final String SHOW_DELIMITER = "^";
  
  private com.bolsinga.music.data.Date fDate;
  private List<Artist> fArtists;
  private Venue fVenue;
  private String fComment;
  private String fID;

  static List<Show> create(final String filename) throws com.bolsinga.web.WebException {
    List<Show> shows = new ArrayList<Show>();

    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
      String l = null;
      StringTokenizer st = null, bt = null;
      try {
        while ((l = in.readLine()) != null) {
          st = new StringTokenizer(l, SHOW_DELIMITER, true);
          
          String date = st.nextToken();       // date
          st.nextToken();                     // delim
          String bandstring = st.nextToken(); // delimited bands
          st.nextToken();                     // delim
          String venue = st.nextToken();      // venue
          String comment = null;
          // The rest is optional
          if (st.hasMoreElements()) {
            st.nextToken();                   // delim
            
            // Need to see if there are comments
            if (st.hasMoreElements()) {
              comment = st.nextToken();
            }
          }
          
          bt = new StringTokenizer(bandstring, "|");
          Vector<Artist> bands = new Vector<Artist>();
          while (bt.hasMoreElements()) {
            bands.add(Artist.createOrGet(bt.nextToken()));
          }
          
          shows.add(new Show(Date.create(date), bands, Venue.get(venue), comment));
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't read shows file: ");
        sb.append(filename);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
 	  } catch (UnsupportedEncodingException e)  {
      StringBuilder sb = new StringBuilder();
      sb.append("Unsupported Encoding: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new com.bolsinga.web.WebException(sb.toString(), e);
    }

    java.util.Collections.sort(shows, com.bolsinga.music.Compare.SHOW_COMPARATOR);

    int index = 0;
    for (Show show : shows) {
      show.setID("sh" + index++);
    }
               
    return shows;
  }
  
  private Show(final com.bolsinga.music.data.Date date, final List<Artist> artists, final Venue venue, final String comment) {
    fDate = date;
    fArtists = artists;
    fVenue = venue;
    fComment = comment;
  }
  
  public List<Artist> getArtists() {
    return Collections.unmodifiableList(fArtists);
  }
  
  public com.bolsinga.music.data.Date getDate() {
    return fDate;
  }
  
  public Venue getVenue() {
    return fVenue;
  }
  
  public String getComment() {
    return fComment;
  }
  
  public String getID() {
    assert fID != null : "No ID";
    return fID;
  }
  
  public void setID(final String id) {
    fID = id;
  }
}