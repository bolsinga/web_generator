package com.bolsinga.web;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.*;

public class MapFileFilter {

  private static final Pattern sDelimitedPattern = Pattern.compile("@@(\\w+)@@");
  
  static void install(final File srcFile, final File dstFile, final HashMap<String, String> mapping) throws WebException {
    // Make sure the path the the dstFile exists
    File dstParent = new File(dstFile.getParent());
    if (!dstParent.mkdirs()) {
      if (!dstParent.exists()) {
        System.err.println("MapFileFilter cannot mkdirs: " + dstParent.getAbsolutePath());
      }
    }

    MapFileFilter.filterFile(srcFile, dstFile, mapping);
  }
  
  private static void filterFile(final File src, final File dst, final HashMap<String, String> mapping) throws WebException {
    // Copy source file, line by line. If a line has a "@@" delimiter, map
    //  the contents to via mapping.    
    StringBuilder sb = MapFileFilter.readFile(src, mapping);
    MapFileFilter.writeFile(dst, sb);
  }
  
  private static StringBuilder readFile(final File src, final HashMap<String, String> mapping) throws WebException {
    StringBuilder result = new StringBuilder();
    
    BufferedReader in = null;
    try {
      try {
        in = new BufferedReader(new FileReader(src));
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(src);
        throw new WebException(sb.toString());
      }
      
      String s = null;
      try {
        while ((s = in.readLine()) != null) {
          Matcher m = sDelimitedPattern.matcher(s);
          if (m.find()) {
            int offset = 0;
            do {
              result.append(s.substring(offset, m.start()));
              result.append(mapping.get(m.group(1)));
              offset = m.end();
            } while (m.find());
            result.append(s.substring(offset, m.regionEnd()));
          } else {
            result.append(s);
          }
          Util.appendPretty(result);
        }
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error reading: ");
        sb.append(src);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(src);
          throw new WebException(sb.toString(), e);
        }
      }
    }
    
    return result;
  }
  
  private static void writeFile(final File dst, final StringBuilder data) throws WebException {
    Writer out = null;
    try {
      try {
        out = new FileWriter(dst);
        out.append(data);
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error writing: ");
        sb.append(dst);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(dst);
          throw new WebException(sb.toString(), e);
        }
      }
    }
  }
}
