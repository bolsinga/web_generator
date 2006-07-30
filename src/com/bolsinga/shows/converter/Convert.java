package com.bolsinga.shows.converter;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Convert {
        
  private static final Pattern sStaticPattern   = Pattern.compile("<static>(.*?)</static>", Pattern.DOTALL);
  private static final Pattern sLocationPattern = Pattern.compile("<location>(.*?)</location>", Pattern.DOTALL);
  private static final Pattern sDataPattern     = Pattern.compile("<data>(.*?)</data>", Pattern.DOTALL);

  private static final Pattern sCommentPattern = Pattern.compile("<comment>(.*?)</comment>", Pattern.DOTALL);
  private static final Pattern sDatePattern    = Pattern.compile("<date>(.*?)</date>", Pattern.DOTALL);

  private final String fType;
  private final String fFile;
        
  public static void main(String args[]) {
    if (args.length != 2) {
      System.out.println("Usage: Convert [type] [file]");
      System.out.println("\tcomments, shows, venuemap, bandsort, relations statics");
      System.exit(0);
    }
                
    try {
      Convert c = new Convert(args[0], args[1]);
      c.convert();
    } catch (IOException ioexception) {
      System.err.println(ioexception);
      System.exit(1);
    }
                
    System.exit(0);
  }

  public Convert(final String type, final String file) {
    fType = type;
    fFile = file;
  }

  public void convert() throws IOException {
    Collection<?> l = null;
    if (fType.equals("relations")) {
      l = relation(fFile);
    } else if (fType.equals("bandsort")) {
      l = bandsort(fFile);
    } else if (fType.equals("venuemap")) {
      l = venuemap(fFile);
    } else if (fType.equals("shows")) {
      l = shows(fFile);
    } else if (fType.equals("statics")) {
      l = statics(fFile);
    } else if (fType.equals("comments")) {
      l = comments(fFile);
    } else {
      System.err.println("Unknown type: " + fType);
      System.exit(1);
    }
    dump(l);
  }

  public static List<Relation> relation(final String filename) throws IOException {
    Vector<Relation> relations = new Vector<Relation>();

    LineNumberReader in = null;
    try {
      in = new LineNumberReader(new FileReader(filename));
      String s = null;
      StringTokenizer st = null;
      while ((s = in.readLine()) != null) {
        st = new StringTokenizer(s, "|");
        
        Relation r = new Relation(st.nextToken(), st.nextToken());
        
        while (st.hasMoreElements()) {
          r.addMember(st.nextToken());
        }
        
        relations.add(r);
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
                
    return relations;
  }
        
  public static List<BandMap> bandsort(final String filename) throws IOException {
    Vector<BandMap> bandMaps = new Vector<BandMap>();
                
    LineNumberReader in = null;
    try {
      in = new LineNumberReader(new FileReader(filename));
      String s = null;
      StringTokenizer st = null;
      while ((s = in.readLine()) != null) {
        st = new StringTokenizer(s, "*");
        
        bandMaps.add(new BandMap(st.nextToken(), st.nextToken()));
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }

    return bandMaps;
  }
        
  public static List<Venue> venuemap(final String filename) throws IOException {
    Vector<Venue> venues = new Vector<Venue>();
                
    LineNumberReader in = null;
    try {
      in = new LineNumberReader(new FileReader(filename));
      String s = null;
      StringTokenizer st = null;

      String name, city, state, address, url;

      while ((s = in.readLine()) != null) {
        st = new StringTokenizer(s, "*");

        name = st.nextToken();
        city = st.nextToken();
        state = st.nextToken();
        
        if (st.hasMoreElements()) {
          address = st.nextToken();
        } else {
          address = null;
        }
        
        if (st.hasMoreElements()) {
          url = st.nextToken();
        } else {
          url = null;
        }
        
        Venue v = new Venue(name, city, state, address, url);
        
        venues.add(v);
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }

    return venues;
  }
        
  public static List<Show> shows(final String filename) throws IOException {
    final String SHOW_DELIMITER = "^";
        
    Vector<Show> shows = new Vector<Show>();

    LineNumberReader in = null;
    try {
      in = new LineNumberReader(new FileReader(filename));
      String l = null;
      StringTokenizer st = null, bt = null;
      while ((l = in.readLine()) != null) {
        st = new StringTokenizer(l, SHOW_DELIMITER, true);
        
        String date = st.nextToken();       // date
        st.nextToken();                     // delim
        String bandstring = st.nextToken(); // delimited bands
        st.nextToken();                     // delim
        String venue = st.nextToken();      // venue
        String comment = null;
        // The rest is optional
        if (st.hasMoreElements()) {
          st.nextToken();                                         // delim
          
          // Need to see if there are comments
          if (st.hasMoreElements()) {
            comment = st.nextToken();
          }
        }
        
        bt = new StringTokenizer(bandstring, "|");
        Vector<String> bands = new Vector<String>();
        while (bt.hasMoreElements()) {
          bands.add(bt.nextToken());
        }
        
        shows.add(new Show(date, bands, venue, comment));
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
                
    return shows;
  }
        
  public static List<Statics> statics(final String filename) throws IOException {
    Vector<Statics> statics = new Vector<Statics>();

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(new File(filename));
      FileChannel fc = null;
      try {
        fc = fis.getChannel();
        ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        CharBuffer cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
        
        Matcher staticMatcher = sStaticPattern.matcher(cb);
        while (staticMatcher.find()) {
          String entry = staticMatcher.group(1);
          Matcher locationMatcher = sLocationPattern.matcher(entry);
          if (locationMatcher.find()) {
            Matcher dataMatcher = sDataPattern.matcher(entry);
          if (dataMatcher.find()) {
            statics.add(new Statics(locationMatcher.group(1), dataMatcher.group(1)));
          }
          }
        }
      } finally {
        if (fc != null) {
          fc.close();
        }
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
    }

    return statics;
  }

  public static List<Comments> comments(final String filename) throws IOException {
    Vector<Comments> comments = new Vector<Comments>();

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(new File(filename));
      FileChannel fc = null;
      try {
        fc = fis.getChannel();
        ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        CharBuffer cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
        
        Matcher commentMatcher = sCommentPattern.matcher(cb);
        while (commentMatcher.find()) {
          String entry = commentMatcher.group(1);
          Matcher dateMatcher = sDatePattern.matcher(entry);
          if (dateMatcher.find()) {
            Matcher dataMatcher = sDataPattern.matcher(entry);
            if (dataMatcher.find()) {
              comments.add(new Comments(dateMatcher.group(1), dataMatcher.group(1)));
            }
          }
        }
      } finally {
        if (fc != null) {
          fc.close();
        }
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
                
    return comments;
  }
        
  private static void dump(final Collection<?> c) {
    for (Object t : c) {
      System.out.println(t);
    }
  }
}
