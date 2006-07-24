package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

public class Show {     
  private String fDate;
  private List<String> fBands;
  private String fVenue;
  private String fComment;

  public Show(String date, List<String> bands, String venue, String comment) {
    fDate = date;
    fBands = bands;
    fVenue = venue;
    fComment = comment;
  }

  public String getDate() {
    return fDate;
  }
        
  public void setDate(String date) {
    fDate = date;
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
        
  public void setVenue(String venue) {
    fVenue = venue;
  }
        
  public String getComment() {
    return fComment;
  }
        
  public void setComment(String comment) {
    fComment = comment;
  }
        
  public String toString() {
    StringBuffer sb = new StringBuffer();
                
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
