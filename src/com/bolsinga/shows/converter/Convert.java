package com.bolsinga.shows.converter;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Convert {
    private String fType;
    private String fFile;
        
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

    public Convert(String type, String file) {
        fType = type;
        fFile = file;
    }

    public void convert() throws IOException {
        List l = null;
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

    public static List relation(String filename) throws IOException {
        Vector relations = new Vector();
                
        LineNumberReader in = new LineNumberReader(new FileReader(filename));
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
                
        return relations;
    }
        
    public static List bandsort(String filename) throws IOException {
        Vector bandMaps = new Vector();
                
        LineNumberReader in = new LineNumberReader(new FileReader(filename));
        String s = null;
        StringTokenizer st = null;
        while ((s = in.readLine()) != null) {
            st = new StringTokenizer(s, "*");
                        
            bandMaps.add(new BandMap(st.nextToken(), st.nextToken()));
        }

        return bandMaps;
    }
        
    public static List venuemap(String filename) throws IOException {
        Vector venues = new Vector();
                
        LineNumberReader in = new LineNumberReader(new FileReader(filename));
        String s = null;
        StringTokenizer st = null;
        while ((s = in.readLine()) != null) {
            st = new StringTokenizer(s, "*");
                        
            Venue v = new Venue(st.nextToken(), st.nextToken(), st.nextToken());
                        
            if (st.hasMoreElements()) {
                v.setAddress(st.nextToken());
            }
                        
            if (st.hasMoreElements()) {
                v.setURL(st.nextToken());
            }
                        
            venues.add(v);
        }
                
        return venues;
    }
        
    public static List shows(String filename) throws IOException {
        final String SHOW_DELIMITER = "^";
        
        Vector shows = new Vector();

        LineNumberReader in = new LineNumberReader(new FileReader(filename));
        String l = null;
        StringTokenizer st = null, bt = null;
        while ((l = in.readLine()) != null) {
            st = new StringTokenizer(l, SHOW_DELIMITER, true);

            String date = st.nextToken();                   // date
            st.nextToken();                                                 // delim
            String bandstring = st.nextToken();             // delimited bands
            st.nextToken();                                                 // delim
            String venue = st.nextToken();                  // venue
            String images = null;
            String comment = null;
            // The rest is optional
            if (st.hasMoreElements()) {
                st.nextToken();                                         // delim
                                
                // Need to see if there are images
                if (st.hasMoreElements()) {
                    images = st.nextToken();
                    if (images.equals(SHOW_DELIMITER)) {
                        images = null;
                    } else {
                        if (st.hasMoreElements()) {
                            st.nextToken();
                        }
                    }
                    if (st.hasMoreElements()) {
                        comment = st.nextToken();
                    }
                }
            }
                        
            bt = new StringTokenizer(bandstring, "|");
            Vector bands = new Vector();
            while (bt.hasMoreElements()) {
                bands.add(bt.nextToken());
            }
                        
            shows.add(new Show(date, bands, venue, images, comment));
        }
                
        return shows;
    }
        
    private static Pattern sStaticPattern =         Pattern.compile("<static>(.*?)</static>", Pattern.DOTALL);
    private static Pattern sLocationPattern =   Pattern.compile("<location>(.*?)</location>", Pattern.DOTALL);
    private static Pattern sDataPattern =           Pattern.compile("<data>(.*?)</data>", Pattern.DOTALL);
        
    public static List statics(String filename) throws IOException {
        Vector statics = new Vector();

        FileChannel fc = new FileInputStream(new File(filename)).getChannel();
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

        return statics;
    }

    private static Pattern sCommentPattern =        Pattern.compile("<comment>(.*?)</comment>", Pattern.DOTALL);
    private static Pattern sDatePattern =           Pattern.compile("<date>(.*?)</date>", Pattern.DOTALL);

    public static List comments(String filename) throws IOException {
        Vector comments = new Vector();

        FileChannel fc = new FileInputStream(new File(filename)).getChannel();
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
                
        return comments;
    }
        
    private static void dump(List list) {
        ListIterator i = list.listIterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
}
