package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

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

	private List relation(String filename) throws IOException {
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
	
	private List bandsort(String filename) throws IOException {
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
	
	private List venuemap(String filename) throws IOException {
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
	
	private List shows(String filename) throws IOException {
		final String SHOW_DELIMITER = "^";
	
		Vector shows = new Vector();

		LineNumberReader in = new LineNumberReader(new FileReader(filename));
		String l = null;
		StringTokenizer st = null, bt = null;
		while ((l = in.readLine()) != null) {
			st = new StringTokenizer(l, SHOW_DELIMITER, true);

			String date = st.nextToken();			// date
			st.nextToken();							// delim
			String bandstring = st.nextToken();		// delimited bands
			st.nextToken();							// delim
			String venue = st.nextToken();			// venue
			String images = null;
			String comment = null;
			// The rest is optional
			if (st.hasMoreElements()) {
				st.nextToken();						// delim
				
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
	
	private List statics(String filename) throws IOException {
		final String STATIC_TAG = "static";
		final String LOCATION_TAG = "location";
		final String DATA_TAG = "data";
		
		Vector statics = new Vector();
		
		Tagged t = Tagged.fromFile(filename);
		
		while (t.hasMoreElements()) {
			if (t.tag().equals(STATIC_TAG)) {
				Tagged info = Tagged.fromString(t.data());
				
				String location = null;
				String data = null;
				
				while (info.hasMoreElements()) {
					if (info.tag().equals(LOCATION_TAG)) {
						location = info.data();
					} else if (info.tag().equals(DATA_TAG)) {
						data = info.data();
					} else {
						System.err.println("Unknown Statics tag: " + info.tag());
						System.exit(1);
					}
				}
				
				statics.add(new Statics(location, data));
			}
		}

		return statics;
	}

	private List comments(String filename) throws IOException {
		final String COMMENT_TAG = "comment";
		final String DATE_TAG = "date";
		final String DATA_TAG = "data";

		Vector comments = new Vector();
		
		Tagged t = Tagged.fromFile(filename);
		
		while (t.hasMoreElements()) {
			if (t.tag().equals(COMMENT_TAG)) {
				Tagged info = Tagged.fromString(t.data());
				
				String date = null;
				String data = null;
				
				while (info.hasMoreElements()) {
					if (info.tag().equals(DATE_TAG)) {
						date = info.data();
					} else if (info.tag().equals(DATA_TAG)) {
						data = info.data();
					} else {
						System.err.println("Unknown Comments tag: " + info.tag());
						System.exit(1);
					}
				}
				
				comments.add(new Comments(date, data));
			}
		}
		
		return comments;
	}
	
	private static void dump(List list) {
		ListIterator li = list.listIterator();
		while (li.hasNext()) {
			System.out.println(li.next());
		}
	}
}
