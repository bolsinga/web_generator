package com.bolsinga.web;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Links {

  public  static final String HTML_EXT     = ".html";
        
  public  static final String ARTIST_DIR   = "bands";
  public  static final String VENUE_DIR    = "venues";
  public  static final String SHOW_DIR     = "dates";
  public  static final String CITIES_DIR   = "cities";
  public  static final String ALT_DIR      = "alt";
  public  static final String TRACKS_DIR   = "tracks";
  public  static final String STYLES_DIR   = "styles";
  public  static final String ARCHIVES_DIR = "archives";
  public  static final String SCRIPTS_DIR  = "scripts";

  private static final String OTHER        = "other";
  public  static final String STATS        = "stats";
  public  static final String ALBUM_STATS  = "albumstats";
  public  static final String HASH         = "#";

  private static final String CUR_DIR      = ".";
  private static final String PAR_DIR      = "..";
    
  private static final ThreadLocal<DateFormat> sArchivePageFormat = new ThreadLocal<DateFormat>() {
    public DateFormat initialValue() {
      return new SimpleDateFormat("yyyy");
    }
  };

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

  public String getLevelOnly() {
    StringBuilder sb = new StringBuilder();
    if (fUpOneLevel) {
      sb.append(Links.PAR_DIR);
    } else {
      sb.append(Links.CUR_DIR);
    }
    return sb.toString();
  }
  
  String getLevel() {
    StringBuilder sb = new StringBuilder();
    if (fUpOneLevel) {
      sb.append(Links.PAR_DIR);
      sb.append(File.separator);
    }
    return sb.toString();
  }
        
  public String getPageFileName(final String name) {
    String file = com.bolsinga.music.Compare.simplify(name).substring(0, 1).toUpperCase();
    if (file.matches("\\W")) {
      file = OTHER;
    }
    return file;
  }
        
  public String getPageFileName(final int year) {
    if (year == com.bolsinga.music.data.Date.UNKNOWN) {
      return OTHER;
    } else {
      return Integer.toString(year);
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
    int current = show.getDate().getYear();
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
    sb.append(artist.getID());
                
    return sb.toString();
  }
        
  public String getLinkTo(final Venue venue) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(venue));
    sb.append(HASH);
    sb.append(venue.getID());
                
    return sb.toString();
  }
        
  public String getLinkTo(final Show show) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(show));
    sb.append(HASH);
    sb.append(show.getID());
                
    return sb.toString();
  }
        
  public String getLinkTo(final Album album) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(album));
    sb.append(HASH);
    sb.append(album.getID());
                
    return sb.toString();
  }

  public A getArtistLink() {
    return getArtistLink(Util.getResourceString("bands"));
  }
  
  public A getArtistLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(ARTIST_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return Util.createInternalA(sb.toString(), t, Util.getResourceString("artiststats"));
  }
                
  public A getShowLink() {
    return getShowLink(Util.getResourceString("dates"));
  }

  public A getShowLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(SHOW_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return Util.createInternalA(sb.toString(), t, Util.getResourceString("datestats"));
  }

  public A getTracksLink() {
    return getTracksLink(Util.getResourceString("tracks"));
  }
  
  public A getTracksLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(TRACKS_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return Util.createInternalA(sb.toString(), t, Util.getResourceString("trackstats"));
  }

  public A getAlbumsLink() {
    return getAlbumsLink(Util.getResourceString("albums"));
  }
  
  public A getAlbumsLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(TRACKS_DIR);
    sb.append(File.separator);
    sb.append(ALBUM_STATS);
    sb.append(HTML_EXT);
    return Util.createInternalA(sb.toString(), t, Util.getResourceString("albumstats"));
  }

  public A getVenueLink() {
    return getVenueLink(Util.getResourceString("venues"));
  }

  public A getVenueLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(VENUE_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return Util.createInternalA(sb.toString(), t, Util.getResourceString("venuestats"));
  }
        
  public A getCityLink() {
    return getCityLink(Util.getResourceString("cities"));
  }

  public A getCityLink(final String t) {
    StringBuilder sb = new StringBuilder();
    sb.append(getLevel());
    sb.append(CITIES_DIR);
    sb.append(File.separator);
    sb.append(STATS);
    sb.append(HTML_EXT);
    return Util.createInternalA(sb.toString(), t, Util.getResourceString("citystats"));
  }

  public String getICalAlt() {
    com.bolsinga.settings.data.Image image = Util.getSettings().getIcalImage();
    return image.getAlt();
  }

  public A getICalLink() {
    StringBuilder sb = new StringBuilder();
    sb.append("webcal:");
    sb.append(getLevel());
    sb.append(ALT_DIR);
    sb.append(File.separator);
    sb.append(Util.getSettings().getIcalName() + ".ics");

    com.bolsinga.settings.data.Image image = Util.getSettings().getIcalImage();

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
    url.append(Util.getSettings().getCssFile());
    return url.toString();
  }

  public Link getLinkToStyleSheet() {
    Link result = new Link();
    result.setRel("stylesheet");
    result.setType("text/css");
    result.setHref(getStyleSheetLink());
    return result;
  }

  public String getScriptLink() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append(SCRIPTS_DIR);
    url.append(File.separator);
    url.append(Util.getSettings().getJavaScriptFile());
    return url.toString();
  }

  public Script getLinkToScript() {
    Script result = new Script();
    result.setType("text/javascript");
    result.setSrc(getScriptLink());
    result.removeAttribute("language");
    return result;
  }

  public A getLinkToHome() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append("index");
    url.append(Links.HTML_EXT);
    String h = Util.getResourceString("home");
    return Util.createInternalA(url.toString(), h, h);
  }
  
  public A getLinkToColophon() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append(Links.ALT_DIR);
    url.append(File.separator);
    url.append("index");
    url.append(Links.HTML_EXT);
    String h = Util.getResourceString("alttitle");
    return Util.createInternalA(url.toString(), h, h);
  }

  public String getPageFileName(final Entry entry) {
    return sArchivePageFormat.get().format(entry.getTimestamp().getTime());
  }

  public String getPagePath(final Entry entry) {
    StringBuilder sb = new StringBuilder();

    sb.append(ARCHIVES_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(entry));
    sb.append(HTML_EXT);
                
    return sb.toString();
  }

  public String getLinkToPage(final Entry entry) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(getLevel());
                
    sb.append(ARCHIVES_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(entry));
    sb.append(HTML_EXT);
                
    return sb.toString();
  }
        
  public String getLinkTo(final Entry entry) {
    StringBuilder sb = new StringBuilder();
                
    sb.append(getLinkToPage(entry));
    sb.append(HASH);
    sb.append(entry.getID());
                
    return sb.toString();
  }

  // Many tools automatically handle RSS links. Perhaps it is time to have a 'special' feeds
  //  page, which will provide this link as well as the iCal link. Then ATOM can go onto this
  //  page in the future as well.
  public String getRSSAlt() {
    com.bolsinga.settings.data.Image image = Util.getSettings().getRssImage();
    return image.getAlt();
  }
  
  public A getRSSLink() {
    com.bolsinga.settings.data.Image image = Util.getSettings().getRssImage();

    IMG i = new IMG(image.getLocation());
    i.setHeight(image.getHeight().intValue());
    i.setWidth(image.getWidth().intValue());
    i.setAlt(image.getAlt());
    i.setTitle(image.getAlt());
                
    return new A(getRSSURL(), i.toString()); // rss feed URL
  }
  
  public A getOverviewLink() {
    return Util.createInternalA(getOverviewURL(),
                                Util.getResourceString("archivesoverviewtitle"),
                                Util.getResourceString("archivesoverview"));
  }

  public String getRSSURL() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append(ALT_DIR);
    url.append(File.separator);
    url.append(Util.getSettings().getRssFile());
    return url.toString();
  }
  
  public String getOverviewURL() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append(ARCHIVES_DIR);
    url.append(File.separator);
    url.append("overview");
    url.append(HTML_EXT);
    return url.toString();
  }
        
  public Link getLinkToRSS() {
    Link result = new Link();
    result.setRel("alternate");
    result.setType("application/rss+xml");
    result.setTitle("RSS");
    result.setHref(getRSSURL());
    return result;
  }
}
