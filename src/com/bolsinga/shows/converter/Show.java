package com.bolsinga.shows.converter;

import java.util.*;

public class Show {     
  private final String fDate;
  private final List<String> fBands;
  private final String fVenue;
  private final String fComment;

  public Show(String date, List<String> bands, String venue, String comment) {
    fDate = date;
    fBands = bands;
    fVenue = venue;
    fComment = comment;
  }

  public String getDate() {
    return fDate;
  }

  public List<String> getBands() {
    return fBands;
  }
        
  public void addBand(String band) {
    fBands.add(band);
  }
        
  public String getVenue() {
    return fVenue;
  }
        
  public String getComment() {
    return fComment;
  }
        
  public String toString() {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getClass().getName());
    sb.append(" Date: ");
    sb.append(getDate());

    sb.append(" (");
    for (String s : getBands()) {
      sb.append(s);
      sb.append(", ");
    }
    sb.append(") ");
                
    sb.append(" Venue: ");
    sb.append(getVenue());
    sb.append(" Comment: ");
    sb.append(getComment());
                
    return sb.toString();
  }
}
