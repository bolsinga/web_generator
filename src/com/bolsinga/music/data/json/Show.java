package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Show implements com.bolsinga.music.data.Show {
  private static final String ARTISTS = "artists";
  private static final String DATE = "date";
  private static final String VENUE = "venue";
  private static final String COMMENT = "comment";
  private static final String ID = "id";

  private List<Artist> artists;
  private Date date;
  private Venue venue;
  private String comment = null;
  private String id;

  static Show create(final com.bolsinga.music.data.Show show) {
    return new Show(show);
  }
  
  static Show create(final JSONObject json) throws JSONException {
    return new Show(json);
  }

  static JSONObject createJSON(final com.bolsinga.music.data.Show show) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, show.getID());
    json.put(DATE, Date.createJSON(show.getDate()));
    json.put(VENUE, show.getVenue().getID());
    String comment = show.getComment();
    if (comment != null) {
      json.put(COMMENT, comment);
    }
    List<? extends com.bolsinga.music.data.Artist> artists = show.getArtists();
    List<String> artistIDs = new ArrayList<String>(artists.size());
    for (final com.bolsinga.music.data.Artist artist : artists) {
      artistIDs.add(artist.getID());
    }
    json.put(ARTISTS, artistIDs);
    
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
  
  private Show(final JSONObject json) throws JSONException {
    id = json.getString(ID);
    date = Date.create(json.getJSONObject(DATE));
    comment = json.optString(COMMENT, null);
    venue = Venue.get(json.getString(VENUE));
    assert venue != null : "Show has no venue!";
    JSONArray jsonArray = json.optJSONArray(ARTISTS);
    artists = new ArrayList<Artist>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      String jsonID = jsonArray.getString(i);
      Artist artist = Artist.get(jsonID);
      assert artist != null : "Can't get Artist: " + jsonID;
      artists.add(artist);
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
}
