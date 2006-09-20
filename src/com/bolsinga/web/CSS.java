package com.bolsinga.web;

import java.io.*;

public class CSS {
  public static final String ARTIST_INDEX    = "artist_index";
  public static final String ARTIST_ITEM     = "artist_item";
  public static final String ARTIST_RELATION = "artist_relation";
  public static final String ARTIST_SHOW     = "artist_show";
  public static final String ARTIST_TRACKS   = "artist_tracks";
                                               
  public static final String DIARY_ENTRY     = "diary_entry";
  public static final String DIARY_HEADER    = "diary_header";
  public static final String DIARY_INDEX     = "diary_index";
  public static final String DIARY_MENU      = "diary_menu";
                                               
  public static final String INTERNAL        = "int";
                                               
  public static final String MAIN_COL1       = "main_col1";
  public static final String MAIN_COL2       = "main_col2";
  public static final String MAIN_DIARY      = "main_diary";
  public static final String MAIN_HEADER     = "main_header";
  public static final String MAIN_MAIN       = "main_main";
  public static final String MAIN_STATIC     = "main_static";
  public static final String MAIN_LINKS      = "main_links";
                                               
  public static final String DOC_MAIN        = "doc_main";
  public static final String DOC_SUB         = "doc_sub";
                                               
  public static final String MUSIC_HEADER    = "music_header";
  public static final String MUSIC_MENU      = "music_menu";
                                               
  public static final String PREVIEW_MAIN    = "preview_main";
  public static final String PREVIEW_MENU    = "preview_menu";
  public static final String PREVIEW_RECENT  = "preview_recent";
  public static final String PREVIEW_SHOW    = "preview_show";
                                               
  public static final String SHOW_COMMENT    = "show_comment";
  public static final String SHOW_INDEX      = "show_index";
  public static final String SHOW_ITEM       = "show_item";
                                               
  public static final String TRACKS_INDEX    = "tracks_index";
  public static final String TRACKS_ITEM     = "tracks_item";
  public static final String TRACKS_MENU     = "tracks_menu";
                                               
  public static final String VENUE_INDEX     = "venue_index";
  public static final String VENUE_ITEM      = "venue_item";
  public static final String VENUE_RELATION  = "venue_relation";
  public static final String VENUE_SHOW      = "venue_show";
                                               
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
      File f = new File(outputDir, "layout.css");
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
    writeCSSDeclaration(pw, ARTIST_INDEX);
    writeCSSDeclaration(pw, ARTIST_ITEM);
    writeCSSDeclaration(pw, ARTIST_RELATION);
    writeCSSDeclaration(pw, ARTIST_SHOW);
    writeCSSDeclaration(pw, ARTIST_TRACKS);
    writeCSSDeclaration(pw, DIARY_ENTRY);
    writeCSSDeclaration(pw, DIARY_HEADER);
    writeCSSDeclaration(pw, DIARY_INDEX);
    writeCSSDeclaration(pw, DIARY_MENU);
    writeCSSDeclaration(pw, INTERNAL);
    writeCSSDeclaration(pw, MAIN_COL1);
    writeCSSDeclaration(pw, MAIN_COL2);
    writeCSSDeclaration(pw, MAIN_DIARY);
    writeCSSDeclaration(pw, MAIN_HEADER);
    writeCSSDeclaration(pw, MAIN_MAIN);
    writeCSSDeclaration(pw, MAIN_STATIC);
    writeCSSDeclaration(pw, MAIN_LINKS);
    writeCSSDeclaration(pw, DOC_MAIN);
    writeCSSDeclaration(pw, DOC_SUB);
    writeCSSDeclaration(pw, MUSIC_HEADER);
    writeCSSDeclaration(pw, MUSIC_MENU);
    writeCSSDeclaration(pw, PREVIEW_MAIN);
    writeCSSDeclaration(pw, PREVIEW_MENU);
    writeCSSDeclaration(pw, PREVIEW_RECENT);
    writeCSSDeclaration(pw, PREVIEW_SHOW);
    writeCSSDeclaration(pw, SHOW_COMMENT);
    writeCSSDeclaration(pw, SHOW_INDEX);
    writeCSSDeclaration(pw, SHOW_ITEM);
    writeCSSDeclaration(pw, TRACKS_INDEX);
    writeCSSDeclaration(pw, TRACKS_ITEM);
    writeCSSDeclaration(pw, TRACKS_MENU);
    writeCSSDeclaration(pw, VENUE_INDEX);
    writeCSSDeclaration(pw, VENUE_ITEM);
    writeCSSDeclaration(pw, VENUE_RELATION);
    writeCSSDeclaration(pw, VENUE_SHOW);
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
