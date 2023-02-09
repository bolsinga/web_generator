package com.bolsinga.web;

import java.io.*;
import java.util.*;

public class CSS {

  public static final String ENTRY_INDEX          = "entry_index";
  public static final String ENTRY_INDEX_SUB      = "entry_index_sub";
  public static final String PERMANENT            = "permanent";
  public static final String ACTIVE               = "active";
  public static final String DOC_2_COL_BODY       = "doc_2_col_body";
  public static final String DOC_3_COL_BODY       = "doc_3_col_body";
  public static final String NAV_HEADER           = "nav_header";
  public static final String STATICS_HEADER       = "statics_header";
  public static final String STATICS_OFFSITE      = "statics_offsite";
  public static final String DOC_SUB              = "doc_sub";
  public static final String COLOPHON             = "colophon";
  public static final String TABLE_HEADER         = "table_header";
  public static final String TABLE_ROW_ALT        = "table_row_alt";
  public static final String TABLE_FOOTER         = "table_footer";
  public static final String RECORD_SECTION       = "record_section";
  public static final String RECORD_ITEM_LIST     = "record_item_list";
  public static final String ENTRY_INDEX_SUB_INFO = "entry_index_sub_info";

  public static void install(final String outputDir) throws WebException {
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(Links.STYLES_DIR);
    File dstFile = new File(sb.toString(), Util.getSettings().getCssFile());
    
    MapFileFilter.install("com/bolsinga/web/layout.css", dstFile);
  }
}
