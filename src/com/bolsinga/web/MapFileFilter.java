package com.bolsinga.web;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.*;

public class MapFileFilter {

  static void install(final String resourceID, final File dstFile) throws WebException {
    // Make sure the path the the dstFile exists
    File dstParent = new File(dstFile.getParent());
    if (!dstParent.mkdirs()) {
      if (!dstParent.exists()) {
        System.err.println("MapFileFilter cannot mkdirs: " + dstParent.getAbsolutePath());
      }
    }

    MapFileFilter.filterFile(resourceID, dstFile);
  }
  
  private static void filterFile(final String resourceID, final File dst) throws WebException {
    StringBuilder sb = MapFileFilter.readFile(resourceID);
    MapFileFilter.writeFile(dst, sb);
  }
  
  private static StringBuilder readFile(final String resourceID) throws WebException {
      StringBuilder result = new StringBuilder();

      URL data = MapFileFilter.class.getClassLoader().getResource(resourceID);
      
      try (BufferedReader in = new BufferedReader(new InputStreamReader(data.openStream()))) {
        String s = null;
        try {
          while ((s = in.readLine()) != null) {
              result.append(s);
              Util.appendPretty(result);
          }
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Error reading: ");
          sb.append(resourceID);
          throw new WebException(sb.toString(), e);
        }
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find resource: ");
      sb.append(resourceID);
      throw new WebException(sb.toString());
    }

    return result;
  }
  
  private static void writeFile(final File dst, final StringBuilder data) throws WebException {
    try (Writer out = new FileWriter(dst)) {
      out.append(data);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Error writing: ");
      sb.append(dst);
      throw new WebException(sb.toString(), e);
    }
  }
}
