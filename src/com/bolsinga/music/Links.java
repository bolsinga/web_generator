package com.bolsinga.music.util;

import com.bolsinga.music.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public class Links {

    public  static final String HTML_EXT    = ".html";
        
    public  static final String ARTIST_DIR  = "bands";
    public  static final String VENUE_DIR   = "venues";
    public  static final String SHOW_DIR    = "dates";
    public  static final String CITIES_DIR  = "cities";
    public  static final String RSS_DIR     = "rss";
    public  static final String ICAL_DIR    = "ical";
    public  static final String TRACKS_DIR  = "tracks";
    public  static final String STYLES_DIR  = "styles";
        
    private static final String OTHER       = "other";
    public  static final String STATS       = "stats";
    public  static final String ALBUM_STATS = "albumstats";
    private static final String HASH        = "#";
        
    private boolean fUpOneLevel;
        
    public static Links getLinks(boolean upOneLevel) {
        return new Links(upOneLevel);
    }
        
    Links(boolean upOneLevel) {
        fUpOneLevel = upOneLevel;
    }
        
    public div addWebNavigator(Music music, String program) {
        div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_MENU);
                                                
        Object[] args = { music.getTimestamp().getTime() };
        d.addElement(new h4(MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("generated"), args)));

        ul list = new ul();

        Object[] args2 = { com.bolsinga.web.util.Util.getSettings().getContact(), program };
        com.bolsinga.web.util.Util.addListItem(list, new a(MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("mailto"), args2), com.bolsinga.web.util.Util.getResourceString("contact"))); // mailto: URL

        com.bolsinga.web.util.Util.addListItem(list, getLinkToHome());

        com.bolsinga.web.util.Util.addListItem(list, getArtistLink());

        com.bolsinga.web.util.Util.addListItem(list, getTracksLink());

        com.bolsinga.web.util.Util.addListItem(list, getShowLink());

        com.bolsinga.web.util.Util.addListItem(list, getVenueLink());

        com.bolsinga.web.util.Util.addListItem(list, getCityLink());

        d.addElement(list);
                
        return d;
    }
        
    public String getPageFileName(String name) {
        String file = Compare.simplify(name).substring(0, 1).toUpperCase();
        if (file.matches("\\W")) {
            file = OTHER;
        }
        return file;
    }
        
    public String getPageFileName(BigInteger year) {
        if (year == null) {
            return OTHER;
        } else {
            return year.toString();
        }
    }
        
    public String getPageFileName(Artist artist) {
        String name = artist.getSortname();
        if (name == null) {
            name = artist.getName();
        }
        return getPageFileName(name);
    }
        
    public String getPageFileName(Venue venue) {
        return getPageFileName(venue.getName());
    }
        
    public String getPageFileName(Show show) {
        BigInteger current = show.getDate().getYear();
        return getPageFileName(current);
    }

    public String getPageFileName(Album album) {
        return getPageFileName(album.getTitle());
    }
        
    public String getPagePath(Artist artist) {
        StringBuffer sb = new StringBuffer();
        sb.append(ARTIST_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(artist));
        sb.append(HTML_EXT);
        return sb.toString();
    }
        
    public String getPagePath(Venue venue) {
        StringBuffer sb = new StringBuffer();
        sb.append(VENUE_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(venue));
        sb.append(HTML_EXT);
        return sb.toString();
    }
        
    public String getPagePath(Show show) {
        StringBuffer sb = new StringBuffer();
        sb.append(SHOW_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(show));
        sb.append(HTML_EXT);
        return sb.toString();
    }

    public String getPagePath(Album album) {
        StringBuffer sb = new StringBuffer();
        sb.append(TRACKS_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(album));
        sb.append(HTML_EXT);
        return sb.toString();
    }

    public String getLinkToPage(Artist artist) {
        StringBuffer sb = new StringBuffer();
                
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(ARTIST_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(artist));
        sb.append(HTML_EXT);
                
        return sb.toString();
    }
        
    public String getLinkToPage(Venue venue) {
        StringBuffer sb = new StringBuffer();
                
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(VENUE_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(venue));
        sb.append(HTML_EXT);

        return sb.toString();
    }
        
    public String getLinkToPage(Show show) {
        StringBuffer sb = new StringBuffer();
                
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(SHOW_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(show));
        sb.append(HTML_EXT);

        return sb.toString();
    }
        
    public String getLinkToPage(Album album) {
        StringBuffer sb = new StringBuffer();
                
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(TRACKS_DIR);
        sb.append(File.separator);
        sb.append(getPageFileName(album));
        sb.append(HTML_EXT);

        return sb.toString();
    }

    public String getLinkTo(Artist artist) {
        StringBuffer sb = new StringBuffer();
                
        sb.append(getLinkToPage(artist));
        sb.append(HASH);
        sb.append(artist.getId());
                
        return sb.toString();
    }
        
    public String getLinkTo(Venue venue) {
        StringBuffer sb = new StringBuffer();
                
        sb.append(getLinkToPage(venue));
        sb.append(HASH);
        sb.append(venue.getId());
                
        return sb.toString();
    }
        
    public String getLinkTo(Show show) {
        StringBuffer sb = new StringBuffer();
                
        sb.append(getLinkToPage(show));
        sb.append(HASH);
        sb.append(show.getId());
                
        return sb.toString();
    }
        
    public String getLinkTo(Album album) {
        StringBuffer sb = new StringBuffer();
                
        sb.append(getLinkToPage(album));
        sb.append(HASH);
        sb.append(album.getId());
                
        return sb.toString();
    }

    public a getArtistLink() {
        StringBuffer sb = new StringBuffer();
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(ARTIST_DIR);
        sb.append(File.separator);
        sb.append(STATS);
        sb.append(HTML_EXT);
        return com.bolsinga.web.util.Util.createInternalA(sb.toString(), com.bolsinga.web.util.Util.getResourceString("bands"));
    }
                
    public a getShowLink() {
        StringBuffer sb = new StringBuffer();
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(SHOW_DIR);
        sb.append(File.separator);
        sb.append(STATS);
        sb.append(HTML_EXT);
        return com.bolsinga.web.util.Util.createInternalA(sb.toString(), com.bolsinga.web.util.Util.getResourceString("dates"));
    }
        
    public a getTracksLink() {
        StringBuffer sb = new StringBuffer();
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(TRACKS_DIR);
        sb.append(File.separator);
        sb.append(STATS);
        sb.append(HTML_EXT);
        return com.bolsinga.web.util.Util.createInternalA(sb.toString(), com.bolsinga.web.util.Util.getResourceString("tracks"));
    }

    public a getAlbumsLink() {
        StringBuffer sb = new StringBuffer();
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(TRACKS_DIR);
        sb.append(File.separator);
        sb.append(ALBUM_STATS);
        sb.append(HTML_EXT);
        return com.bolsinga.web.util.Util.createInternalA(sb.toString(), com.bolsinga.web.util.Util.getResourceString("albums"));
    }

    public a getVenueLink() {
        StringBuffer sb = new StringBuffer();
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(VENUE_DIR);
        sb.append(File.separator);
        sb.append(STATS);
        sb.append(HTML_EXT);
        return com.bolsinga.web.util.Util.createInternalA(sb.toString(), com.bolsinga.web.util.Util.getResourceString("venues"));
    }
        
    public a getCityLink() {
        StringBuffer sb = new StringBuffer();
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(CITIES_DIR);
        sb.append(File.separator);
        sb.append(STATS);
        sb.append(HTML_EXT);
        return com.bolsinga.web.util.Util.createInternalA(sb.toString(), com.bolsinga.web.util.Util.getResourceString("cities"));
    }

    public a getICalLink() {
        StringBuffer sb = new StringBuffer();
        sb.append("webcal:");
        if (fUpOneLevel) {
            sb.append("..");
            sb.append(File.separator);
        }
        sb.append(ICAL_DIR);
        sb.append(File.separator);
        sb.append(com.bolsinga.web.util.Util.getSettings().getIcalName() + ".ics");

        com.bolsinga.settings.data.Image image = com.bolsinga.web.util.Util.getSettings().getIcalImage();

        img i = new img(image.getLocation());
        i.setHeight(image.getHeight().intValue());
        i.setWidth(image.getWidth().intValue());
        i.setAlt(image.getAlt());
        i.setTitle(image.getAlt());
                
        return new a(sb.toString(), i.toString()); // ical: URL
    }
        
    public String getStyleSheetLink() {
        StringBuffer url = new StringBuffer();
        if (fUpOneLevel) {
            url.append("..");
            url.append(File.separator);
        }
        url.append(STYLES_DIR);
        url.append(File.separator);
        url.append(com.bolsinga.web.util.Util.getSettings().getCssFile());
        return url.toString();
    }

    public link getLinkToStyleSheet() {
        link result = new link();
        result.setRel("stylesheet");
        result.setType("text/css");
        result.setHref(getStyleSheetLink());
        return result;
    }

    public a getLinkToHome() {
        StringBuffer url = new StringBuffer();
        if (fUpOneLevel) {
            url.append("..");
            url.append(File.separator);
        }
        url.append("index.html");
        return com.bolsinga.web.util.Util.createInternalA(url.toString(), com.bolsinga.web.util.Util.getResourceString("home"));
    }
}
