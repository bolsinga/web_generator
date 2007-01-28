package com.bolsinga.shows.converter;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Convert {
  
  // The *? construct is a reluctant quantifier. This means it is not greedy, and matches the first it can.
  private static final Pattern sStaticPattern   = Pattern.compile("<static>(.*?)</static>", Pattern.DOTALL);
  private static final Pattern sLocationPattern = Pattern.compile("<location>(.*?)</location>", Pattern.DOTALL);
  private static final Pattern sDataPattern     = Pattern.compile("<data>(.*?)</data>", Pattern.DOTALL);

  private static final Pattern sCommentPattern = Pattern.compile("<comment>(.*?)</comment>", Pattern.DOTALL);
  private static final Pattern sDatePattern    = Pattern.compile("<date>(.*?)</date>", Pattern.DOTALL);

  public static List<Relation> relation(final String filename) throws ConvertException {
    Vector<Relation> relations = new Vector<Relation>();

    Reader reader = null;
    try {
      reader = new FileReader(filename);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }
    
    try {
      BufferedReader in = null;
      try {
        in = new BufferedReader(reader);
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
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't read relations file: ");
      sb.append(reader.toString());
      throw new ConvertException(sb.toString(), e);
    }
                
    return relations;
  }
        
  public static List<BandMap> bandsort(final String filename) throws ConvertException {
    Vector<BandMap> bandMaps = new Vector<BandMap>();

    Reader reader = null;
    try {
      reader = new FileReader(filename);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }
    
    try {
      BufferedReader in = null;
      try {
        in = new BufferedReader(reader);
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
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't read bandsort file: ");
      sb.append(reader.toString());
      throw new ConvertException(sb.toString(), e);
    }

    return bandMaps;
  }
        
  public static List<Venue> venuemap(final String filename) throws ConvertException {
    Vector<Venue> venues = new Vector<Venue>();

    Reader reader = null;
    try {
      reader = new FileReader(filename);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }

    try {
      BufferedReader in = null;
      try {
        in = new BufferedReader(reader);
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
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't read venuemap file: ");
      sb.append(reader.toString());
      throw new ConvertException(sb.toString(), e);
    }

    return venues;
  }
        
  public static List<Show> shows(final String filename) throws ConvertException {
    final String SHOW_DELIMITER = "^";
        
    Vector<Show> shows = new Vector<Show>();

    Reader reader = null;
    try {
      reader = new FileReader(filename);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }

    try {
      BufferedReader in = null;
      try {
        in = new BufferedReader(reader);
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
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't read shows file: ");
      sb.append(reader.toString());
      throw new ConvertException(sb.toString(), e);
    }
                
    return shows;
  }
        
  public static List<Statics> statics(final String filename) throws ConvertException {
    Vector<Statics> statics = new Vector<Statics>();

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(new File(filename));
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }

    try {
      try {
        FileChannel fc = null;
        try {
          fc = fis.getChannel();
          ByteBuffer bb = null;
          try {
            bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
          } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Can't map: ");
            sb.append(fc.toString());
            throw new ConvertException(sb.toString(), e);
          }
          CharBuffer cb = null;
          try {
            cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
          } catch (java.nio.charset.CharacterCodingException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Bad Encoding US-ASCII: ");
            sb.append(bb.toString());
            throw new ConvertException(sb.toString(), e);
          }
          
          Matcher staticMatcher = sStaticPattern.matcher(cb);
          if (staticMatcher.find()) {
            do {
              String entry = staticMatcher.group(1);
              Matcher locationMatcher = sLocationPattern.matcher(entry);
              if (locationMatcher.find()) {
                Matcher dataMatcher = sDataPattern.matcher(entry);
                if (dataMatcher.find()) {
                  statics.add(new Statics(locationMatcher.group(1), dataMatcher.group(1)));
                } else {
                  System.err.println("No Data: " + entry);
                }
              } else {
                System.err.println("No Location: " + entry);
              }
            } while (staticMatcher.find());
          } else {
            System.err.println("No statics: " + cb);
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
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't read statics file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }

    return statics;
  }

  public static List<Comments> comments(final String filename) throws ConvertException {
    Vector<Comments> comments = new Vector<Comments>();

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(new File(filename));
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }

    try {
      try {
        FileChannel fc = null;
        try {
          fc = fis.getChannel();
          ByteBuffer bb = null;
          try {
            bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
          } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Can't map: ");
            sb.append(fc.toString());
            throw new ConvertException(sb.toString(), e);
          }
          CharBuffer cb = null;
          try {
            cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
          } catch (java.nio.charset.CharacterCodingException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Bad Encoding US-ASCII: ");
            sb.append(bb.toString());
            throw new ConvertException(sb.toString(), e);
          }
          
          Matcher commentMatcher = sCommentPattern.matcher(cb);
          if (commentMatcher.find()) {
            do {
              String entry = commentMatcher.group(1);
              Matcher dateMatcher = sDatePattern.matcher(entry);
              if (dateMatcher.find()) {
                Matcher dataMatcher = sDataPattern.matcher(entry);
                if (dataMatcher.find()) {
                  comments.add(new Comments(dateMatcher.group(1), dataMatcher.group(1)));
                } else {
                  System.err.println("No data: " + entry);
                }
              } else {
                System.err.println("No date: " + entry);
              }
            } while (commentMatcher.find());
          } else {
            System.err.println("No comment: " + cb);
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
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't read statics file: ");
      sb.append(filename);
      throw new ConvertException(sb.toString(), e);
    }
                
    return comments;
  }
}
