package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

public class Show {     
    private String fDate;
    private List fBands;
    private String fVenue;
    private String fImages;
    private String fComment;

    public Show(String date, List bands, String venue, String images, String comment) {
        fDate = date;
        fBands = bands;
        fVenue = venue;
        fImages = images;
        fComment = comment;
    }

    public String getDate() {
        return fDate;
    }
        
    public void setDate(String date) {
        fDate = date;
    }
        
    public List getBands() {
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
        
    public String getImages() {
        return fImages;
    }
        
    public void setImages(String images) {
        fImages = images;
    }
        
    public String getComment() {
        return fComment;
    }
        
    public void setComment(String comment) {
        fComment = comment;
    }
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
                
        sb.append(getClass().getName().toString());
        sb.append(" Date: ");
        sb.append(getDate());

        sb.append(" (");
        ListIterator i = getBands().listIterator();
        while (i.hasNext()) {
            sb.append((String)(i.next()));
            sb.append(", ");
        }
        sb.append(") ");
                
        sb.append(" Venue: ");
        sb.append(getVenue());
        sb.append(" Images: ");
        sb.append(getImages());
        sb.append(" Comment: ");
        sb.append(getComment());
                
        return sb.toString();
    }
}
