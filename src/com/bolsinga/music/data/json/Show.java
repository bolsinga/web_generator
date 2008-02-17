package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Show implements com.bolsinga.music.data.Show {
  private List<Artist> artists;
  private Date date;
  private Venue venue;
  private String comment = null;
  private String id;
  
  static Show create(final com.bolsinga.music.data.Show show) {
    return new Show(show);
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Show show) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("id", show.getID());
    json.put("date", Date.createJSON(show.getDate()));
    json.put("venue", show.getVenue().getID());
    String comment = show.getComment();
    if (comment != null) {
      json.put("comment", comment);
    }
    List<? extends com.bolsinga.music.data.Artist> artists = show.getArtists();
    List<String> artistIDs = new ArrayList<String>(artists.size());
    for (final com.bolsinga.music.data.Artist artist : artists) {
      artistIDs.add(artist.getID());
    }
    json.put("artists", artistIDs);
    
    return json;
  }
  
  private Show() {
  
  }
  
  private Show(final com.bolsinga.music.data.Show show) {
    id = show.getID();
    comment = show.getComment();
    date = Date.create(show.getDate());
    venue = Venue.createOrGet(show.getVenue());

    List<? extends com.bolsinga.music.data.Artist> srcArtists = show.getArtists();
    artists = new ArrayList<Artist>(srcArtists.size());
    for (final com.bolsinga.music.data.Artist artist : srcArtists) {
      artists.add(Artist.createOrGet(artist));
    }
  }
  
  public List<Artist> getArtists() {
    return artists;
  }
  
  public Date getDate() {
    return date;
  }
  
  public Venue getVenue() {
    return venue;
  }
  
  public String getComment() {
    return comment;
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
}
