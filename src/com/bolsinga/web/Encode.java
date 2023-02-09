package com.bolsinga.web;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public abstract class Encode {

  private static final Pattern sRootURLPattern = Pattern.compile("@@ROOT_URL@@");
  
  private static final Pattern sHTMLTagPattern = Pattern.compile("<(pre|code|a)[^>]*>[^<]*</\\1>", Pattern.DOTALL);

  // NOTE: With the current set of data (2006-08-02) there is a 210:1 ratio of up to std links!

  private static Encode sEncode = null;

  public synchronized static Encode getEncode() {
    if (sEncode == null) {
      sEncode = new HashEncode();
    }
    return sEncode;
  }
  
  public static String encodeROOT_URL(final String s) {
    return Encode.encodeROOT_URL(s, Util.getSettings().getRoot());
  }
  
  static String encodeROOT_URL(final String s, final String replacement) {
    if (s != null) {
      return sRootURLPattern.matcher(s).replaceAll(replacement);
    }
    return null;
  }

  static String encodeUntagged(final String source, final UntaggedEncoder encoder) {
    StringBuilder sb = new StringBuilder();
    Matcher html = sHTMLTagPattern.matcher(source);
    if (html.find()) {
      int offset = 0;
      do {
        sb.append(encoder.encodeUntagged(source.substring(offset, html.start())));
        sb.append(source.substring(html.start(), html.end()));
        offset = html.end();
      } while (html.find());
      sb.append(encoder.encodeUntagged(source.substring(offset, html.regionEnd())));
    } else {
      sb.append(encoder.encodeUntagged(source));
    }
    return sb.toString();
  }

  public abstract String embedLinks(Show show);

  public abstract String embedLinks(Entry entry, boolean upOneLevel);
}

class HashEncode extends Encode {

  public String embedLinks(final Show show) {
    return Util.toHTMLSafe(show.getComment());
  }

  public String embedLinks(final Entry entry, final boolean upOneLevel) {
    String result = Util.toHTMLSafe(entry.getComment());
    return Encode.encodeROOT_URL(result, Links.getLinks(upOneLevel).getLevelOnly());
  }
}
