package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public class Links {

  public  static final String HTML_EXT     = ".html";
  public  static final String ARCHIVES_DIR = "archives";
  public  static final String STYLES_DIR   = "styles";
  public  static final String RSS_DIR      = "rss";
  private static final String HASH         = "#";
    
  static DateFormat sArchivePageFormat = new SimpleDateFormat("yyyy");

  private boolean fUpOneLevel;
        
  public static Links getLinks(boolean upOneLevel) {
    return new Links(upOneLevel);
  }
        
  Links(boolean upOneLevel) {
    fUpOneLevel = upOneLevel;
  }
        
  public String getPageFileName(Entry entry) {
    return sArchivePageFormat.format(entry.getTimestamp().getTime());
  }

  public String getPagePath(Entry entry) {
    StringBuffer sb = new StringBuffer();

    sb.append(ARCHIVES_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(entry));
    sb.append(HTML_EXT);
                
    return sb.toString();
  }

  public String getLinkToPage(Entry entry) {
    StringBuffer sb = new StringBuffer();
                
    if (fUpOneLevel) {
      sb.append("..");
      sb.append(File.separator);
    }
                
    sb.append(ARCHIVES_DIR);
    sb.append(File.separator);
    sb.append(getPageFileName(entry));
    sb.append(HTML_EXT);
                
    return sb.toString();
  }
        
  public String getLinkTo(Entry entry) {
    StringBuffer sb = new StringBuffer();
                
    sb.append(getLinkToPage(entry));
    sb.append(HASH);
    sb.append(entry.getId());
                
    return sb.toString();
  }
        
  public a getRSSLink() {
    com.bolsinga.settings.data.Image image = com.bolsinga.web.Util.getSettings().getRssImage();

    img i = new img(image.getLocation());
    i.setHeight(image.getHeight().intValue());
    i.setWidth(image.getWidth().intValue());
    i.setAlt(image.getAlt());
    i.setTitle(image.getAlt());
                
    return new a(getRSSURL(), i.toString()); // rss feed URL
  }

  public String getRSSURL() {
    StringBuffer url = new StringBuffer();
    if (fUpOneLevel) {
      url.append("..");
      url.append(File.separator);
    }
    url.append(RSS_DIR);
    url.append(File.separator);
    url.append(com.bolsinga.web.Util.getSettings().getRssFile());
    return url.toString();
  }
        
  public link getLinkToRSS() {
    link result = new link();
    result.setRel("alternate");
    result.setType("application/rss+xml");
    result.setTitle("RSS");
    result.setHref(getRSSURL());
    return result;
  }

  public String getStyleSheetLink() {
    StringBuffer url = new StringBuffer();
    if (fUpOneLevel) {
      url.append("..");
      url.append(File.separator);
    }
    url.append(STYLES_DIR);
    url.append(File.separator);
    url.append(com.bolsinga.web.Util.getSettings().getCssFile());
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
    return com.bolsinga.web.Util.createInternalA(url.toString(), com.bolsinga.web.Util.getResourceString("home"));
  }
}
