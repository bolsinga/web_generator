package com.bolsinga.shows.converter;

import com.bolsinga.shows.converter.*;

import java.util.*;

public class ConvertDump {

  private final String fType;
  private final String fFile;
        
  public static void main(String args[]) {
    if (args.length != 2) {
      System.out.println("Usage: ConvertDump [type] [file]");
      System.out.println("\tcomments, shows, venuemap, bandsort, relations statics");
      System.exit(0);
    }
    
    try {
      ConvertDump c = new ConvertDump(args[0], args[1]);
      c.convert();
    } catch (ConvertException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private ConvertDump(final String type, final String file) {
    fType = type;
    fFile = file;
  }

  private void convert() throws ConvertException {
    Collection<?> l = null;
    if (fType.equals("relations")) {
      l = Convert.relation(fFile);
    } else if (fType.equals("bandsort")) {
      l = Convert.bandsort(fFile);
    } else if (fType.equals("venuemap")) {
      l = Convert.venuemap(fFile);
    } else if (fType.equals("shows")) {
      l = Convert.shows(fFile);
    } else if (fType.equals("statics")) {
      l = Convert.statics(fFile);
    } else if (fType.equals("comments")) {
      l = Convert.comments(fFile);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Unknown conversion type: ");
      sb.append(fType);
      throw new ConvertException(sb.toString());
    }
    dump(l);
  }
        
  private static void dump(final Collection<?> c) {
    for (Object t : c) {
      System.out.println(t);
    }
  }
}
