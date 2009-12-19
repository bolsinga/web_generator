package com.bolsinga.web;

import java.io.*;
import java.util.*;

public class CSS {

  private static final String LONG_ENTRY_INDEX          = "entry_index";
  private static final String LONG_ENTRY_INDEX_SUB      = "entry_index_sub";
  private static final String LONG_PERMANENT            = "permanent";
  private static final String LONG_ACTIVE               = "active";
  private static final String LONG_DOC_2_COL_BODY       = "doc_2_col_body";
  private static final String LONG_DOC_3_COL_BODY       = "doc_3_col_body";
  private static final String LONG_NAV_HEADER           = "nav_header";
  private static final String LONG_STATICS_HEADER       = "statics_header";
  private static final String LONG_STATICS_OFFSITE      = "statics_offsite";
  private static final String LONG_DOC_SUB              = "doc_sub";
  private static final String LONG_COLOPHON             = "colophon";
  private static final String LONG_TABLE_HEADER         = "table_header";
  private static final String LONG_TABLE_ROW_ALT        = "table_row_alt";
  private static final String LONG_TABLE_FOOTER         = "table_footer";
  private static final String LONG_RECORD_SECTION       = "record_section";
  private static final String LONG_RECORD_ITEM_LIST     = "record_item_list";
  private static final String LONG_RECORD_POPUP         = "record_popup";
  private static final String LONG_ENTRY_INDEX_SUB_INFO = "entry_index_sub_info";

  private static final String SHORT_ENTRY_INDEX          = "a";
  private static final String SHORT_ENTRY_INDEX_SUB      = "b";
  private static final String SHORT_PERMANENT            = "d";
  private static final String SHORT_ACTIVE               = "e";
  private static final String SHORT_DOC_2_COL_BODY       = "f";
  private static final String SHORT_DOC_3_COL_BODY       = "g";
  private static final String SHORT_NAV_HEADER           = "h";
  private static final String SHORT_STATICS_HEADER       = "i";
  private static final String SHORT_STATICS_OFFSITE      = "j";
  private static final String SHORT_DOC_SUB              = "k";
  private static final String SHORT_COLOPHON             = "l";
  private static final String SHORT_TABLE_HEADER         = "m";
  private static final String SHORT_TABLE_ROW_ALT        = "n";
  private static final String SHORT_TABLE_FOOTER         = "o";
  private static final String SHORT_RECORD_SECTION       = "p";
  private static final String SHORT_RECORD_ITEM_LIST     = "q";
  private static final String SHORT_RECORD_POPUP         = "s";
  private static final String SHORT_ENTRY_INDEX_SUB_INFO = "t";

  public static String ENTRY_INDEX;
  public static String ENTRY_INDEX_SUB;
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
  public static String RECORD_SECTION;
  public static String RECORD_ITEM_LIST;
  public static String RECORD_POPUP;
  public static String ENTRY_INDEX_SUB_INFO;
  
  private static final HashMap<String, String> sCSSMapping = new HashMap<String, String>();

  static {
    boolean debug = Util.getDebugOutput();
    
    // If debug, use the long CSS class names. If not, use the short CSS class names
    //  to 'optimize' the file to be smaller, and each HTML file will be smaller as
    //  well, thus decreasing download times and bandwidth.

    sCSSMapping.put(LONG_ENTRY_INDEX,          debug ? LONG_ENTRY_INDEX          : SHORT_ENTRY_INDEX);
    sCSSMapping.put(LONG_ENTRY_INDEX_SUB,      debug ? LONG_ENTRY_INDEX_SUB      : SHORT_ENTRY_INDEX_SUB);
    sCSSMapping.put(LONG_PERMANENT,            debug ? LONG_PERMANENT            : SHORT_PERMANENT);
    sCSSMapping.put(LONG_ACTIVE,               debug ? LONG_ACTIVE               : SHORT_ACTIVE);
    sCSSMapping.put(LONG_DOC_2_COL_BODY,       debug ? LONG_DOC_2_COL_BODY       : SHORT_DOC_2_COL_BODY);
    sCSSMapping.put(LONG_DOC_3_COL_BODY,       debug ? LONG_DOC_3_COL_BODY       : SHORT_DOC_3_COL_BODY);
    sCSSMapping.put(LONG_NAV_HEADER,           debug ? LONG_NAV_HEADER           : SHORT_NAV_HEADER);
    sCSSMapping.put(LONG_STATICS_HEADER,       debug ? LONG_STATICS_HEADER       : SHORT_STATICS_HEADER);
    sCSSMapping.put(LONG_STATICS_OFFSITE,      debug ? LONG_STATICS_OFFSITE      : SHORT_STATICS_OFFSITE);
    sCSSMapping.put(LONG_DOC_SUB,              debug ? LONG_DOC_SUB              : SHORT_DOC_SUB);
    sCSSMapping.put(LONG_COLOPHON,             debug ? LONG_COLOPHON             : SHORT_COLOPHON);
    sCSSMapping.put(LONG_TABLE_HEADER,         debug ? LONG_TABLE_HEADER         : SHORT_TABLE_HEADER);
    sCSSMapping.put(LONG_TABLE_ROW_ALT,        debug ? LONG_TABLE_ROW_ALT        : SHORT_TABLE_ROW_ALT);
    sCSSMapping.put(LONG_TABLE_FOOTER,         debug ? LONG_TABLE_FOOTER         : SHORT_TABLE_FOOTER);
    sCSSMapping.put(LONG_RECORD_SECTION,       debug ? LONG_RECORD_SECTION       : SHORT_RECORD_SECTION);
    sCSSMapping.put(LONG_RECORD_ITEM_LIST,     debug ? LONG_RECORD_ITEM_LIST     : SHORT_RECORD_ITEM_LIST);
    sCSSMapping.put(LONG_RECORD_POPUP,         debug ? LONG_RECORD_POPUP         : SHORT_RECORD_POPUP);
    sCSSMapping.put(LONG_ENTRY_INDEX_SUB_INFO, debug ? LONG_ENTRY_INDEX_SUB_INFO : SHORT_ENTRY_INDEX_SUB_INFO);

    ENTRY_INDEX          = sCSSMapping.get(LONG_ENTRY_INDEX);
    ENTRY_INDEX_SUB      = sCSSMapping.get(LONG_ENTRY_INDEX_SUB);
    PERMANENT            = sCSSMapping.get(LONG_PERMANENT);
    ACTIVE               = sCSSMapping.get(LONG_ACTIVE);
    DOC_2_COL_BODY       = sCSSMapping.get(LONG_DOC_2_COL_BODY);
    DOC_3_COL_BODY       = sCSSMapping.get(LONG_DOC_3_COL_BODY);
    NAV_HEADER           = sCSSMapping.get(LONG_NAV_HEADER);
    STATICS_HEADER       = sCSSMapping.get(LONG_STATICS_HEADER);
    STATICS_OFFSITE      = sCSSMapping.get(LONG_STATICS_OFFSITE);
    DOC_SUB              = sCSSMapping.get(LONG_DOC_SUB);
    COLOPHON             = sCSSMapping.get(LONG_COLOPHON);
    TABLE_HEADER         = sCSSMapping.get(LONG_TABLE_HEADER);
    TABLE_ROW_ALT        = sCSSMapping.get(LONG_TABLE_ROW_ALT);
    TABLE_FOOTER         = sCSSMapping.get(LONG_TABLE_FOOTER);
    RECORD_SECTION       = sCSSMapping.get(LONG_RECORD_SECTION);
    RECORD_ITEM_LIST     = sCSSMapping.get(LONG_RECORD_ITEM_LIST);
    RECORD_POPUP         = sCSSMapping.get(LONG_RECORD_POPUP);
    ENTRY_INDEX_SUB_INFO = sCSSMapping.get(LONG_ENTRY_INDEX_SUB_INFO);
  }
  
  public static void install(final String srcFileName, final String outputDir) throws WebException {
    File srcFile = new File(srcFileName);
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(Links.STYLES_DIR);
    File dstFile = new File(sb.toString(), Util.getSettings().getCssFile());
    
    MapFileFilter.install(srcFile, dstFile, sCSSMapping);
  }
}
