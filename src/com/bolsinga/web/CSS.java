package com.bolsinga.web;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.*;

public class CSS {

  private static final String LONG_ENTRY_INDEX      = "entry_index";
  private static final String LONG_ENTRY_INDEX_SUB  = "entry_index_sub";
  private static final String LONG_ENTRY_ITEM       = "entry_item";
  private static final String LONG_ENTRY_RELATION   = "entry_relation";
  private static final String LONG_INTERNAL         = "internal";
  private static final String LONG_PERMANENT        = "permanent";
  private static final String LONG_ACTIVE           = "active";
  private static final String LONG_DOC_2_COL_BODY   = "doc_2_col_body";
  private static final String LONG_DOC_3_COL_BODY   = "doc_3_col_body";
  private static final String LONG_NAV_HEADER       = "nav_header";
  private static final String LONG_STATICS_HEADER   = "statics_header";
  private static final String LONG_STATICS_OFFSITE  = "statics_offsite";
  private static final String LONG_DOC_SUB          = "doc_sub";
  private static final String LONG_COLOPHON         = "colophon";
  private static final String LONG_TABLE_HEADER     = "table_header";
  private static final String LONG_TABLE_ROW_ALT    = "table_row_alt";
  private static final String LONG_TABLE_FOOTER     = "table_footer";

  private static final String SHORT_ENTRY_INDEX     = "a";
  private static final String SHORT_ENTRY_INDEX_SUB = "b";
  private static final String SHORT_ENTRY_ITEM      = "d";
  private static final String SHORT_ENTRY_RELATION  = "d";
  private static final String SHORT_INTERNAL        = "e";
  private static final String SHORT_PERMANENT       = "f";
  private static final String SHORT_ACTIVE          = "g";
  private static final String SHORT_DOC_2_COL_BODY  = "h";
  private static final String SHORT_DOC_3_COL_BODY  = "i";
  private static final String SHORT_NAV_HEADER      = "j";
  private static final String SHORT_STATICS_HEADER  = "k";
  private static final String SHORT_STATICS_OFFSITE = "l";
  private static final String SHORT_DOC_SUB         = "m";
  private static final String SHORT_COLOPHON        = "n";
  private static final String SHORT_TABLE_HEADER    = "o";
  private static final String SHORT_TABLE_ROW_ALT   = "p";
  private static final String SHORT_TABLE_FOOTER    = "q";

  public static String ENTRY_INDEX;
  public static String ENTRY_INDEX_SUB;
  public static String ENTRY_ITEM;
  public static String ENTRY_RELATION;
  public static String INTERNAL;
  public static String PERMANENT;
  public static String ACTIVE;
  public static String DOC_2_COL_BODY;
  public static String DOC_3_COL_BODY;
  public static String NAV_HEADER;
  public static String STATICS_HEADER;
  public static String STATICS_OFFSITE;
  public static String DOC_SUB;
  public static String COLOPHON;
  public static String TABLE_HEADER;
  public static String TABLE_ROW_ALT;
  public static String TABLE_FOOTER;
  
  private static final HashMap<String, String> sCSSMapping = new HashMap<String, String>();

  private static final Pattern sDelimitedPattern = Pattern.compile("@@(\\w+)@@");
  
  static {
    boolean debug = Util.getDebugOutput();
    
    // If debug, use the long CSS class names. If not, use the short CSS class names
    //  to 'optimize' the file to be smaller, and each HTML file will be smaller as
    //  well, thus decreasing download times and bandwidth.

    sCSSMapping.put(LONG_ENTRY_INDEX,     debug ? LONG_ENTRY_INDEX     : SHORT_ENTRY_INDEX);
    sCSSMapping.put(LONG_ENTRY_INDEX_SUB, debug ? LONG_ENTRY_INDEX_SUB : SHORT_ENTRY_INDEX_SUB);
    sCSSMapping.put(LONG_ENTRY_ITEM,      debug ? LONG_ENTRY_ITEM      : SHORT_ENTRY_ITEM);
    sCSSMapping.put(LONG_ENTRY_RELATION,  debug ? LONG_ENTRY_RELATION  : SHORT_ENTRY_RELATION);
    sCSSMapping.put(LONG_INTERNAL,        debug ? LONG_INTERNAL        : SHORT_INTERNAL);
    sCSSMapping.put(LONG_PERMANENT,       debug ? LONG_PERMANENT       : SHORT_PERMANENT);
    sCSSMapping.put(LONG_ACTIVE,          debug ? LONG_ACTIVE          : SHORT_ACTIVE);
    sCSSMapping.put(LONG_DOC_2_COL_BODY,  debug ? LONG_DOC_2_COL_BODY  : SHORT_DOC_2_COL_BODY);
    sCSSMapping.put(LONG_DOC_3_COL_BODY,  debug ? LONG_DOC_3_COL_BODY  : SHORT_DOC_3_COL_BODY);
    sCSSMapping.put(LONG_NAV_HEADER,      debug ? LONG_NAV_HEADER      : SHORT_NAV_HEADER);
    sCSSMapping.put(LONG_STATICS_HEADER,  debug ? LONG_STATICS_HEADER  : SHORT_STATICS_HEADER);
    sCSSMapping.put(LONG_STATICS_OFFSITE, debug ? LONG_STATICS_OFFSITE : SHORT_STATICS_OFFSITE);
    sCSSMapping.put(LONG_DOC_SUB,         debug ? LONG_DOC_SUB         : SHORT_DOC_SUB);
    sCSSMapping.put(LONG_COLOPHON,        debug ? LONG_COLOPHON        : SHORT_COLOPHON);
    sCSSMapping.put(LONG_TABLE_HEADER,    debug ? LONG_TABLE_HEADER    : SHORT_TABLE_HEADER);
    sCSSMapping.put(LONG_TABLE_ROW_ALT,   debug ? LONG_TABLE_ROW_ALT   : SHORT_TABLE_ROW_ALT);
    sCSSMapping.put(LONG_TABLE_FOOTER,    debug ? LONG_TABLE_FOOTER    : SHORT_TABLE_FOOTER);

    ENTRY_INDEX     = sCSSMapping.get(LONG_ENTRY_INDEX);
    ENTRY_INDEX_SUB = sCSSMapping.get(LONG_ENTRY_INDEX_SUB);
    ENTRY_ITEM      = sCSSMapping.get(LONG_ENTRY_ITEM);
    ENTRY_RELATION  = sCSSMapping.get(LONG_ENTRY_RELATION);
    INTERNAL        = sCSSMapping.get(LONG_INTERNAL);
    PERMANENT       = sCSSMapping.get(LONG_PERMANENT);
    ACTIVE          = sCSSMapping.get(LONG_ACTIVE);
    DOC_2_COL_BODY  = sCSSMapping.get(LONG_DOC_2_COL_BODY);
    DOC_3_COL_BODY  = sCSSMapping.get(LONG_DOC_3_COL_BODY);
    NAV_HEADER      = sCSSMapping.get(LONG_NAV_HEADER);
    STATICS_HEADER  = sCSSMapping.get(LONG_STATICS_HEADER);
    STATICS_OFFSITE = sCSSMapping.get(LONG_STATICS_OFFSITE);
    DOC_SUB         = sCSSMapping.get(LONG_DOC_SUB);
    COLOPHON        = sCSSMapping.get(LONG_COLOPHON);
    TABLE_HEADER    = sCSSMapping.get(LONG_TABLE_HEADER);
    TABLE_ROW_ALT   = sCSSMapping.get(LONG_TABLE_ROW_ALT);
    TABLE_FOOTER    = sCSSMapping.get(LONG_TABLE_FOOTER);
  }
                                                       
  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: CSS [settings.xml] [layout.css] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[0]);
    
    CSS.install(args[1], args[2]);
  }
  
  public static void install(final String srcFileName, final String outputDir) {
    File srcFile = new File(srcFileName);
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(Links.STYLES_DIR);
    File dstFile = new File(sb.toString(), Util.getSettings().getCssFile());
    
    try {
      CSS.install(srcFile, dstFile);
    } catch (IOException e) {
      System.err.println("Exception trying to install CSS file: " + e);
      e.printStackTrace();
      System.exit(1);
    }
  }
    
  public static void install(final File srcFile, final File dstFile) throws IOException {
    // Make sure the path the the dstFile exists
    File dstParent = new File(dstFile.getParent());
    if (!dstParent.mkdirs()) {
      if (!dstParent.exists()) {
        System.out.println("CSS cannot mkdirs: " + dstParent.getAbsolutePath());
      }
    }

    CSS.filterFile(srcFile, dstFile);
  }
  
  private static void filterFile(final File src, final File dst) throws IOException {
    // Copy source file, line by line. If a line has a "@@" delimiter, map
    //  the contents to the proper CSS class name with sCSSMapping.
    StringBuilder sb = new StringBuilder();
    
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(src));
      String s = null;
      
      while ((s = in.readLine()) != null) {
        Matcher m = sDelimitedPattern.matcher(s);
        if (m.find()) {
          int offset = 0;
          do {
            sb.append(s.substring(offset, m.start()));
            sb.append(sCSSMapping.get(m.group(1)));
            offset = m.end();
          } while (m.find());
          sb.append(s.substring(offset, m.regionEnd()));
        } else {
          sb.append(s);
        }
        Util.appendPretty(sb);
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
    
    Writer out = null;
    try {
      out = new FileWriter(dst);
      out.append(sb);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
}
