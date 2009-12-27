package com.bolsinga.web;

import java.io.*;
import java.util.*;

public class JavaScript {
  private static final String LONG_NA_VALUE      = "NA";

  private static final String SHORT_NA_VALUE      = "a";

  public static String NA_VALUE;

  private static final HashMap<String, String> sMapping = new HashMap<String, String>();

  static {
    boolean debug = Util.getDebugOutput();
    
    // If debug, use the long  names. If not, use the short  names
    //  to 'optimize' the file to be smaller, and each HTML file will be smaller as
    //  well, thus decreasing download times and bandwidth.

    sMapping.put(LONG_NA_VALUE,      debug ? LONG_NA_VALUE      : SHORT_NA_VALUE);

    NA_VALUE      = sMapping.get(LONG_NA_VALUE);
  }
  
  public static void install(final String srcFileName, final String outputDir) throws WebException {
    File srcFile = new File(srcFileName);
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(Links.SCRIPTS_DIR);
    File dstFile = new File(sb.toString(), Util.getSettings().getJavaScriptFile());
    
    MapFileFilter.install(srcFile, dstFile, sMapping);
  }
}
