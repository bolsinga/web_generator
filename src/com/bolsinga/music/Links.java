package com.bolsinga.music;

import com.bolsinga.music.*;
import com.bolsinga.music.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Links {

  public  static final String HTML_EXT    = ".html";
        
  public  static final String ARTIST_DIR  = "bands";
  public  static final String VENUE_DIR   = "venues";
  public  static final String SHOW_DIR    = "dates";
  public  static final String CITIES_DIR  = "cities";
  public  static final String RSS_DIR     = "rss";
  public  static final String ICAL_DIR    = "alt";
  public  static final String TRACKS_DIR  = "tracks";
  public  static final String STYLES_DIR  = "styles";
        
  private static final String OTHER       = "other";
  public  static final String STATS       = "stats";
  public  static final String ALBUM_STATS = "albumstats";
  private static final String HASH        = "#";
        
  private final boolean fUpOneLevel;
  
  private static Links sStdLinks = null;
  private static Links sUpLinks = null;
        
  public static synchronized Links getLinks(final boolean upOneLevel) {
    if (upOneLevel) {
      if (sUpLinks == null) {
        sUpLinks = new Links(upOneLevel);
      }
      return sUpLinks;
    } else {
      if (sStdLinks == null) {
        sStdLinks = new Links(upOneLevel);
      }
      return sStdLinks;
    }
  }
        
  Links(final boolean upOneLevel) {
    fUpOneLevel = upOneLevel;
  }

  public String getLevel() {
    StringBuilder sb = new StringBuilder();
    if (fUpOneLevel) {
      sb.append("..");
      sb.append(File.separator);
    }
    return sb.toString();
  }
        
  public String getPageFileName(final String name) {
    String file = Compare.simplify(name).substring(0, 1).toUpperCase();
    if (file.matches("\\W")) {
      file = OTHER;
    }
    return file;
  }
        
  public String getPageFileName(final BigInteger year) {
    if (year == null) {
      return OTHER;
    } else {
      return year.toString();
    }
  }
        
  public String getPageFileName(final Artist artist) {
    String name = artist.getSortname();
    if (name == null) {
      name = artist.getName();
    }
    return getPageFileName(name);
  }
        
  public String getPageFileName(final Venue venue) {
    return getPageFileName(venue.getName());
  }
        
  public String getPageFileName(final Show show) {
    BigInteger current = show.getDate().getYear();
    return getPageFileName(current);
  }

  public String getPageFileName(final Album album) {
    return getPageFileName(album.getTitle());
  }
        
  public String getPagePath(final Artist artist) {
    StringBuilder sb = new StringBuilder();
    sb.append(ARTIST_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(artist));
    sb.append(HTML_EXT);
    return sb.toString();
  }
        
  public String getPagePath(final Venue venue) {
    StringBuilder sb = new StringBuilder();
    sb.append(VENUE_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(venue));
    sb.append(HTML_EXT);
    return sb.toString();
  }
        
  public String getPagePath(final Show show) {
    StringBuilder sb = new StringBuilder();
    sb.append(SHOW_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(show));
    sb.append(HTML_EXT);
    return sb.toString();
  }

  public String getPagePath(final Album album) {
    StringBuilder sb = new StringBuilder();
    sb.append(TRACKS_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(album));
    sb.append(HTML_EXT);
    return sb.toString();
  }

  public String getLinkToPage(final Artist artist) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(ARTIST_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(artist));
    sb.append(HTML_EXT);
                
    return sb.toString();
  }
        
  public String getLinkToPage(final Venue venue) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLevel());
    sb.append(VENUE_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(venue));
    sb.append(HTML_EXT);

    return sb.toString();
  }
        
  public String getLinkToPage(final Show show) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLevel());
    sb.append(SHOW_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(show));
    sb.append(HTML_EXT);

    return sb.toString();
  }
        
  public String getLinkToPage(final Album album) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLevel());
    sb.append(TRACKS_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(album));
    sb.append(HTML_EXT);

    return sb.toString();
  }

  public String getLinkTo(final Artist artist) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(artist));
    sb.append(HASH);
    sb.append(artist.getId());
                
    return sb.toString();
  }
        
  public String getLinkTo(final Venue venue) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(venue));
    sb.append(HASH);
    sb.append(venue.getId());
                
    return sb.toString();
  }
        
  public String getLinkTo(final Show show) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(show));
    sb.append(HASH);
    sb.append(show.getId());
                
    return sb.toString();
  }
        
  public String getLinkTo(final Album album) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(album));
    sb.append(HASH);
    sb.append(album.getId());
                
    return sb.toString();
  }

  public A getArtistLink() {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(ARTIST_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("bands"), com.bolsinga.web.Util.getResourceString("artiststats"));
  }
                
  public A getShowLink() {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(SHOW_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("dates"), com.bolsinga.web.Util.getResourceString("datestats"));
  }

  public A getTracksLink() {
    return getTracksLink(com.bolsinga.web.Util.getResourceString("tracks"));
  }
  
  public A getTracksLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(TRACKS_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return com.bolsinga.web.Util.createInternalA(sb.toString(), t, com.bolsinga.web.Util.getResourceString("trackstats"));
  }

  public A getAlbumsLink() {
    return getAlbumsLink(com.bolsinga.web.Util.getResourceString("albums"));
  }
  
  public A getAlbumsLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(TRACKS_DIR);
    sb.append(File.separator);
    sb.append(ALBUM_STATS);
    sb.append(HTML_EXT);
    return com.bolsinga.web.Util.createInternalA(sb.toString(), t, com.bolsinga.web.Util.getResourceString("albumstats"));
  }

  public A getVenueLink() {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(VENUE_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("venues"), com.bolsinga.web.Util.getResourceString("venuestats"));
  }
        
  public A getCityLink() {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(CITIES_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("cities"), com.bolsinga.web.Util.getResourceString("citystats"));
  }

  public String getICalAlt() {
    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getIcalImage();
    return image.getAlt();
  }

  public A getICalLink() {
    StringBuilder sb = new StringBuilder();
    sb.append("webcal:");
    sb.append(getLevel());
    sb.append(ICAL_DIR);
    sb.append(File.separator);
    sb.append(com.bolsinga.web.Util.getSettings().getIcalName() + ".ics");

    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getIcalImage();

    IMG i = new IMG(image.getLocation());
    i.setHeight(image.getHeight().intValue());
    i.setWidth(image.getWidth().intValue());
    i.setAlt(image.getAlt());
    i.setTitle(image.getAlt());
                
    return new A(sb.toString(), i.toString()); // ical: URL
  }
        
  public String getStyleSheetLink() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append(STYLES_DIR);
    url.append(File.separator);
    url.append(com.bolsinga.web.Util.getSettings().getCssFile());
    return url.toString();
  }

  public Link getLinkToStyleSheet() {
    Link result = new Link();
    result.setRel("stylesheet");
    result.setType("text/css");
    result.setHref(getStyleSheetLink());
    return result;
  }

  public A getLinkToHome() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append("index.html");
    String h = com.bolsinga.web.Util.getResourceString("home");
    return com.bolsinga.web.Util.createInternalA(url.toString(), h, h);
  }
}
