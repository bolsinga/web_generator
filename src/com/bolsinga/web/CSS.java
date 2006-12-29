package com.bolsinga.web;

import java.io.*;

public class CSS {

  // Have these be set for long or short names based upon DEBUGOUTPUT.
  // Short names can go into production and reduce the size of the CSS and HTML
  // reducing the bandwidth.
  
  // Also instead of having layout.css in the ~/Site repository,
  // generate it from the code so that the CSS rules are DEBUGOUTPUT or not.
  public static final String ENTRY_INDEX     = "entry_index";
  public static final String ENTRY_INDEX_SUB = "entry_index_sub";
  
  public static final String ENTRY_ITEM      = "entry_item";
  public static final String ENTRY_RELATION  = "entry_relation";
                                                 
  public static final String INTERNAL        = "int";
  public static final String PERMANENT       = "perm";
  public static final String ACTIVE          = "active";
  
  public static final String DOC_2_COL_BODY  = "doc_2_col_body";
  public static final String DOC_3_COL_BODY  = "doc_3_col_body";

  public static final String NAV_HEADER      = "nav_header";
  public static final String STATICS_HEADER  = "statics_header";
  public static final String STATICS_OFFSITE = "statics_offsite";

  public static final String DOC_SUB         = "doc_sub";

  public static final String COLOPHON        = "colophon";

  public static final String TABLE_HEADER    = "table_header";
  public static final String TABLE_ROW       = "table_row";
  public static final String TABLE_ROW_ALT   = "table_row_alt";
  public static final String TABLE_FOOTER    = "table_footer";
                                                       
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: CSS [output.dir]");
      System.exit(0);
    }
                
    CSS.generate(args[0]);
  }
        
  public static void generate(final String outputDir) {
    try {
      File f = new File(outputDir, com.bolsinga.web.Util.getSettings().getCssFile());
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("CSS cannot mkdirs: " + parent.getAbsolutePath());
        }
      }
      PrintWriter pw = new PrintWriter(new FileOutputStream(f));
      generate(pw);
      pw.close();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
        
  public static void generate(final PrintWriter pw) {
    writeCSSDeclaration(pw, ENTRY_INDEX);
    writeCSSDeclaration(pw, ENTRY_INDEX_SUB);
    writeCSSDeclaration(pw, ENTRY_ITEM);
    writeCSSDeclaration(pw, ENTRY_RELATION);
    writeCSSDeclaration(pw, INTERNAL);
    writeCSSDeclaration(pw, PERMANENT);
    writeCSSDeclaration(pw, ACTIVE);
    writeCSSDeclaration(pw, DOC_2_COL_BODY);
    writeCSSDeclaration(pw, DOC_3_COL_BODY);
    writeCSSDeclaration(pw, NAV_HEADER);
    writeCSSDeclaration(pw, STATICS_HEADER);
    writeCSSDeclaration(pw, STATICS_OFFSITE);
    writeCSSDeclaration(pw, DOC_SUB);
    writeCSSDeclaration(pw, COLOPHON);
    writeCSSDeclaration(pw, TABLE_HEADER);
    writeCSSDeclaration(pw, TABLE_ROW);
    writeCSSDeclaration(pw, TABLE_ROW_ALT);
    writeCSSDeclaration(pw, TABLE_FOOTER);
  }
        
  private static void writeCSSDeclaration(final PrintWriter pw, final String name) {
    pw.print(".");
    pw.print(name);
    pw.println(" {");
    pw.println("}");
    pw.println();
  }
}
