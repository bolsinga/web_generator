package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Links {

  public static final String HTML_EXT     = ".html";
  public static final String ARCHIVES_DIR = "archives";
  public static final String STYLES_DIR   = "styles";
  public static final String RSS_DIR      = "rss";
  public static final String HASH         = "#";
    
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
  
  public String getLevel() {
    StringBuilder sb = new StringBuilder();
    if (fUpOneLevel) {
      sb.append("..");
      sb.append(File.separator);
    }
    return sb.toString();
  }
        
  public String getPageFileName(final Entry entry) {
    return sArchivePageFormat.get().format(entry.getTimestamp().toGregorianCalendar().getTime());
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
    sb.append(entry.getId());
                
    return sb.toString();
  }
        
  public A getRSSLink() {
    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getRssImage();

    IMG i = new IMG(image.getLocation());
    i.setHeight(image.getHeight().intValue());
    i.setWidth(image.getWidth().intValue());
    i.setAlt(image.getAlt());
    i.setTitle(image.getAlt());
                
    return new A(getRSSURL(), i.toString()); // rss feed URL
  }
  
  public A getOverviewLink() {
    return com.bolsinga.web.Util.createInternalA( getOverviewURL(),
                                                  com.bolsinga.web.Util.getResourceString("archivesoverviewtitle"),
                                                  com.bolsinga.web.Util.getResourceString("archivesoverview"));
  }

  public String getRSSURL() {
    StringBuilder url = new StringBuilder();
    url.append(getLevel());
    url.append(RSS_DIR);
    url.append(File.separator);
    url.append(com.bolsinga.web.Util.getSettings().getRssFile());
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
    url.append("index");
    url.append(HTML_EXT);
    String h = com.bolsinga.web.Util.getResourceString("home");
    return com.bolsinga.web.Util.createInternalA(url.toString(), h, h);
  }
}
