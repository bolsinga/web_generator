package com.bolsinga.music.web;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

abstract class DocumentCreator {
	Music fMusic = null;
	String fOutputDir = null;
	Document fDocument = null;
	
	protected DocumentCreator(Music music, String outputDir) {
		fMusic = music;
		fOutputDir = outputDir;
	}
	
	protected abstract boolean needNewDocument();
	protected abstract Document createDocument();
	protected abstract String getCurrentPath();
	protected abstract void addIndexNavigator();
	
	public void close() {
		if (fDocument != null) {
			writeDocument();
			fDocument = null;
		}
	}
	
	protected Document internalGetDocument() {
		if (needNewDocument()) {
			if (fDocument != null) {
				writeDocument();
			}
			fDocument = createDocument();
			addHeader();
			addWebNavigator();
			addIndexNavigator();
		}
		return fDocument;
	}
	
	private void writeDocument() {
		fDocument.getBody().addElement(new HR());
		addIndexNavigator();
		addWebNavigator();
		try {
			File f = new File(fOutputDir, getCurrentPath());
			File parent = new File(f.getParent());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					System.out.println("Can't: " + parent.getAbsolutePath());
				}
			}
			OutputStream os = new FileOutputStream(f);
			fDocument.output(os);
			os.close();
		} catch (IOException ioe) {
			System.err.println("Exception: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	public String getTitle(String letter, String type) {
		StringBuffer sb = new StringBuffer();
		sb.append("'");
		sb.append(letter);
		sb.append("' ");
		sb.append(type);
		return sb.toString();
	}
	
	private void addHeader() {
		IMG img = new IMG("http://homepage.mac.com/bolsinga/.Pictures/images/comp.gif");
		img.setHeight(90);
		img.setWidth(120);
		img.setAlt("[Busy computing... for you!]");
		fDocument.getBody().addElement(new Center(img));
	}
	
	protected void addWebNavigator() {
		Util.addWebNavigator(fDocument);
	}
	
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}

class ArtistDocumentCreator extends DocumentCreator {
	Artist fDocArtist = null;
	Artist fArtist = null;
	
	public ArtistDocumentCreator(Music music, String outputDir) {
		super(music, outputDir);
	}
	
	public Document getDocument(Artist artist) {
		fArtist = artist;
		return internalGetDocument();
	}
	
	protected boolean needNewDocument() {
		return (fDocArtist == null) || (!Util.getPageFileName(fDocArtist).equals(Util.getPageFileName(fArtist)));
	}
	
	protected Document createDocument() {
		fDocArtist = fArtist;
		return Web.createHTMLDocument(getTitle(Util.getPageFileName(fDocArtist), "Artists"));
	}
	
	protected String getCurrentPath() {
		return Util.getPagePath(fDocArtist);
	}
	
	protected void addIndexNavigator() {
		Web.addIndexNavigator(fMusic, fDocArtist, fDocument);
	}
}

class VenueDocumentCreator extends DocumentCreator {
	Venue fDocVenue = null;
	Venue fVenue = null;
	
	public VenueDocumentCreator(Music music, String outputDir) {
		super(music, outputDir);
	}
	
	public Document getDocument(Venue venue) {
		fVenue = venue;
		return internalGetDocument();
	}
	
	protected boolean needNewDocument() {
		return (fDocVenue == null) || (!Util.getPageFileName(fDocVenue).equals(Util.getPageFileName(fVenue)));
	}
	
	protected Document createDocument() {
		fDocVenue = fVenue;
		return Web.createHTMLDocument(getTitle(Util.getPageFileName(fDocVenue), "Venues"));
	}
	
	protected String getCurrentPath() {
		return Util.getPagePath(fDocVenue);
	}
	
	protected void addIndexNavigator() {
		Web.addIndexNavigator(fMusic, fDocVenue, fDocument);
	}
}

class ShowDocumentCreator extends DocumentCreator {
	Show fDocShow = null;
	Show fShow = null;
	com.bolsinga.music.data.Date fDate = null;
	
	public ShowDocumentCreator(Music music, String outputDir) {
		super(music, outputDir);
	}
	
	public void add(Music music, Show item) {
		Document doc = getDocument(item);
		
		com.bolsinga.music.data.Date d = item.getDate();
		
		if ((fDate == null) || (!Util.toMonth(d).equals(Util.toMonth(fDate)))) {
			Body b = doc.getBody();

			b.addElement(new HR());
			b.addElement(new Center().addElement(new Big().addElement(Util.toMonth(d))));
			
			fDate = d;
		}

		Web.addItem(music, item, doc);
	}
	
	public Document getDocument(Show show) {
		fShow = show;
		return internalGetDocument();
	}
	
	protected boolean needNewDocument() {
		return (fDocShow == null) || (!Util.getPageFileName(fDocShow).equals(Util.getPageFileName(fShow)));
	}
	
	protected Document createDocument() {
		fDocShow = fShow;
		fDate = null;
		return Web.createHTMLDocument(getTitle(Util.getPageFileName(fDocShow), "Dates"));
	}
	
	protected String getCurrentPath() {
		return Util.getPagePath(fDocShow);
	}
	
	protected void addIndexNavigator() {
		Web.addIndexNavigator(fMusic, fDocShow, fDocument);
	}
}

class StatisticsCreator extends DocumentCreator {
	String fTitle = null;
	String fDirectory = null;
	
	public StatisticsCreator(Music music, String outputDir) {
		super(music, outputDir);
	}

	public Document getDocument(String title, String directory) {
		fTitle = title;
		fDirectory = directory;
		return internalGetDocument();
	}

	protected boolean needNewDocument() {
		return true;
	}
	
	protected Document createDocument() {
		return Web.createHTMLDocument(fTitle);
	}
	
	protected String getCurrentPath() {
		StringBuffer sb = new StringBuffer();
		sb.append(fDirectory);
		sb.append(Util.SEPARATOR);
		sb.append(Util.STATS);
		sb.append(Util.HTML_EXT);
		return sb.toString();
	}
	
	protected void addIndexNavigator() {

	}
}

public class Web {
		
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: Web [source.xml] [output.dir]");
			System.exit(0);
		}
		
		Web.generate(args[0], args[1]);
	}
	
	public static void generate(String sourceFile, String outputDir) {
		Music music = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			music = (Music)u.unmarshal(new FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		
		generateArtistPages(music, outputDir);
		
		generateVenuePages(music, outputDir);
		
		generateDatePages(music, outputDir);
		
		generateCityPages(music, outputDir);
	}
	
	// NOTE: Instead of a List of ID's, JAXB returns a List of real items.
	
	public static void generateArtistPages(Music music, String outputDir) {
		List items = music.getArtist();
		Artist item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.ARTIST_COMPARATOR);
		
		ArtistDocumentCreator creator = new ArtistDocumentCreator(music, outputDir);
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();
			
			addItem(music, item, creator.getDocument(item));
		}
		creator.close();
		
		Collections.sort(items, com.bolsinga.music.util.Compare.getCompare(music).ARTIST_STATS_COMPARATOR);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();

			names[index] = item.getName();
			values[index] = Lookup.getLookup(music).getShows(item).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, outputDir);
		stats.getDocument("Artist Statistics", Util.ARTIST_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by Artist", "Artist")));
		stats.close();
	}
	
	public static void generateVenuePages(Music music, String outputDir) {
		List items = music.getVenue();
		Venue item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.VENUE_COMPARATOR);

		VenueDocumentCreator creator = new VenueDocumentCreator(music, outputDir);
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Venue)li.next();

			addItem(music, item, creator.getDocument(item));
		}
		creator.close();

		Collections.sort(items, com.bolsinga.music.util.Compare.getCompare(music).VENUE_STATS_COMPARATOR);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		li = items.listIterator();
		while (li.hasNext()) {
			item = (Venue)li.next();

			names[index] = item.getName();
			values[index] = Lookup.getLookup(music).getShows(item).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, outputDir);
		stats.getDocument("Venue Statistics", Util.VENUE_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by Venue", "Venue")));
		stats.close();
	}
	
	public static void generateDatePages(Music music, String outputDir) {
		List items = music.getShow();
		Show item = null;
		Vector list = null;
		TreeMap dates = new TreeMap(com.bolsinga.music.util.Compare.SHOW_STATS_COMPARATOR);
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);

		ShowDocumentCreator creator = new ShowDocumentCreator(music, outputDir);
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Show)li.next();

			if (dates.containsKey(item)) {
				list = (Vector)dates.get(item);
				list.add(item);
			} else {
				list = new Vector();
				list.add(item);
				dates.put(item, list);
			}
			
			creator.add(music, item);
		}
		creator.close();

		String[] names = new String[dates.size()];
		int[] values = new int[dates.size()];
		int index = 0;
		Iterator i = dates.keySet().iterator();
		while (i.hasNext()) {
			item = (Show)i.next();
			
			BigInteger year = item.getDate().getYear();
			names[index] = (year != null) ? year.toString() : "Unknown";
			values[index] = ((Vector)dates.get(item)).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, outputDir);
		stats.getDocument("Show Statistics", Util.SHOW_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by Year", "Year")));
		stats.close();
	}
	
	public static void generateCityPages(Music music, String outputDir) {
		List items = music.getLocation();
		Location item = null;
		HashMap cityCount = new HashMap();
		String city = null;
		Integer val = null;
		HashSet set = null;
		HashSet cities = new HashSet();
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Location)li.next();
			
			city = item.getCity();
			cities.add(city);
			
			val = new Integer(Lookup.getLookup(music).getShows(item).size());
			if (cityCount.containsKey(val)) {
				set = (HashSet)cityCount.get(val);
				set.add(city);
			} else {
				set = new HashSet();
				set.add(city);
				cityCount.put(val, set);
			}
		}
		
		List keys = new Vector(cityCount.keySet());
		Collections.sort(keys);
		Collections.reverse(keys);
		
		String[] names = new String[cities.size()];
		int[] values = new int[cities.size()];
		int index = 0;
		
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			val = (Integer)i.next();
			
			keys = new Vector((HashSet)cityCount.get(val));
			Collections.sort(keys);
			
			Iterator j = keys.iterator();
			while (j.hasNext()) {
				names[index] = (String)j.next();
				values[index] = val.intValue();
				index++;
			}
		}
		
		StatisticsCreator creator = new StatisticsCreator(music, outputDir);
		creator.getDocument("City Statistics", Util.CITIES_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by City", "City")));
		creator.close();
	}
	
	public static void addItem(Music music, Artist artist, Document doc) {
		Body b = doc.getBody();

		List shows = Lookup.getLookup(music).getShows(artist);
		
		b.addElement(new HR());
		A a = new A();
		a.setName(artist.getId());
		a.addElement("test", artist.getName());
		b.addElement(new Center().addElement(new Big().addElement(a)));
		
		addRelations(music, artist, doc);
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			Show show = (Show)li.next();
			
			UL showListing = new UL();
			
			showListing.addElement(new LI().addElement(new A(Util.getLinkTo(show), Util.toString(show.getDate()))));
			
			UL showInfo = new UL();
			
			LI listItem = new LI();
			ListIterator bi = show.getPerformance().listIterator();
			while (bi.hasNext()) {
				Performance p = (Performance)bi.next();
				Artist performer = (Artist)p.getArtist();
				
				if (artist.equals(performer)) {
					listItem.addElement(new B(performer.getName()));
				} else {
					listItem.addElement(new A(Util.getLinkTo(performer), performer.getName()));
				}
				
				if (bi.hasNext()) {
					listItem.addElement(", ");
				}
			}
			showInfo.addElement(listItem);
			
			Venue venue = (Venue)show.getVenue();
			A venueA = new A(Util.getLinkTo(venue), venue.getName());
			Location l = (Location)venue.getLocation();
			showInfo.addElement(new LI().addElement(venueA.toString() + " " + l.getCity() + ", " + l.getState()));
			
			String comment = show.getComment();
			if (comment != null) {
				showInfo.addElement(new LI(comment));
			}
			
			showListing.addElement(showInfo);
			
			b.addElement(showListing);
		}
	}
	
	public static void addItem(Music music, Venue venue, Document doc) {
		Body b = doc.getBody();

		List shows = Lookup.getLookup(music).getShows(venue);
		
		b.addElement(new HR());
		A a = new A();
		a.setName(venue.getId());
		a.addElement("test", venue.getName());
		b.addElement(new Center().addElement(new Big().addElement(a)));
		
		addRelations(music, venue, doc);

		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			Show show = (Show)li.next();
			
			UL showListing = new UL();
			
			showListing.addElement(new LI().addElement(new A(Util.getLinkTo(show), Util.toString(show.getDate()))));
			
			UL showInfo = new UL();
			
			LI listItem = new LI();
			ListIterator bi = show.getPerformance().listIterator();
			while (bi.hasNext()) {
				Performance p = (Performance)bi.next();
				Artist performer = (Artist)p.getArtist();
				
				listItem.addElement(new A(Util.getLinkTo(performer), performer.getName()));
				
				if (bi.hasNext()) {
					listItem.addElement(", ");
				}
			}
			showInfo.addElement(listItem);
			
			B venueB = new B(venue.getName());
			Location l = (Location)venue.getLocation();
			showInfo.addElement(new LI().addElement(venueB.toString() + " " + l.getCity() + ", " + l.getState()));
			
			String comment = show.getComment();
			if (comment != null) {
				showInfo.addElement(new LI().addElement(comment));
			}
			
			showListing.addElement(showInfo);
			
			b.addElement(showListing);
		}
	}
	
	public static void addItem(Music music, Show show, Document doc) {
		Body b = doc.getBody();

		UL showListing = new UL();
		A a = new A();
		a.setName(show.getId());
		a.addElement("test", Util.toString(show.getDate()));
		showListing.addElement(new LI(new B(a)));
		
		UL showInfo = new UL();
		
		LI listItem = new LI();
		ListIterator bi = show.getPerformance().listIterator();
		while (bi.hasNext()) {
			Performance p = (Performance)bi.next();
			Artist performer = (Artist)p.getArtist();
			
			listItem.addElement(new A(Util.getLinkTo(performer), performer.getName()));
			
			if (bi.hasNext()) {
				listItem.addElement(", ");
			}
		}
		showInfo.addElement(listItem);
		
		Venue venue = (Venue)show.getVenue();
		A venueA = new A(Util.getLinkTo(venue), venue.getName());
		Location l = (Location)venue.getLocation();
		showInfo.addElement(new LI(venueA.toString() + " " + l.getCity() + ", " + l.getState()));

		String comment = show.getComment();
		if (comment != null) {
			showInfo.addElement(new LI(comment));
		}

		showListing.addElement(showInfo);
		
		b.addElement(showListing);
	}
	
	public static void addRelations(Music music, Artist artist, Document doc) {
		Collection relations = Lookup.getLookup(music).getRelations(artist);
		if (relations != null) {
			Body b = doc.getBody();
			
			UL ul = new UL();
			ul.addElement(new LI().addElement("See Also"));
			
			UL related = new UL();
			Iterator li = relations.iterator();
			while (li.hasNext()) {
				Artist a = (Artist)li.next();
				if (a.equals(artist)) {
					related.addElement(new LI().addElement(a.getName()));
				} else {
					related.addElement(new LI().addElement(new A(Util.getLinkTo(a), a.getName())));
				}
			}
			ul.addElement(related);
			
			b.addElement(ul);
		}
	}
	
	public static void addRelations(Music music, Venue venue, Document doc) {
		Collection relations = Lookup.getLookup(music).getRelations(venue);
		if (relations != null) {
			Body b = doc.getBody();
			
			UL ul = new UL();
			ul.addElement(new LI().addElement("See Also"));
			
			Iterator li = relations.iterator();
			while (li.hasNext()) {
				Venue v = (Venue)li.next();
				if (v.equals(venue)) {
					ul.addElement(new LI().addElement(v.getName()));
				} else {
					ul.addElement(new LI().addElement(new A(Util.getLinkTo(v), v.getName())));
				}
			}
			
			b.addElement(ul);
		}
	}
	
	public static void addIndexNavigator(Music music, Artist artist, Document doc) {
		Center c = new Center();
		
		java.util.Map m = new TreeMap();
		Iterator li = music.getArtist().iterator();
		while (li.hasNext()) {
			Artist a = (Artist)li.next();
			String letter = Util.getPageFileName(a);
			if (!m.containsKey(letter)) {
				m.put(letter, Util.getLinkToPage(a));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String a = (String)li.next();
			if (a.equals(Util.getPageFileName(artist))) {
				c.addElement(a + " ");
			} else {
				c.addElement(new A((String)m.get(a), a).toString() + " ");
			}
		}
		
		doc.getBody().addElement(c);
	}
	
	public static void addIndexNavigator(Music music, Venue venue, Document doc) {
		Center c = new Center();
		
		java.util.Map m = new TreeMap();
		Iterator li = music.getVenue().iterator();
		while (li.hasNext()) {
			Venue v = (Venue)li.next();
			String letter = Util.getPageFileName(v);
			if (!m.containsKey(letter)) {
				m.put(letter, Util.getLinkToPage(v));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String v = (String)li.next();
			String l = " " + v + " ";
			if (v.equals(Util.getPageFileName(venue))) {
				c.addElement(l);
			} else {
				c.addElement(new A((String)m.get(v), l));
			}
		}
		
		doc.getBody().addElement(c);
	}
	
	public static Table makeTable(String[] names, int[] values, String caption, String header) {
		Table table = new Table();
		Caption capt = new Caption();
		capt.addElement(caption);
		table.addElement(capt);
		TR tr = new TR(true).addElement(new TH(header)).addElement(new TH("#")).addElement(new TH("%"));
		tr.setAlign("center");
		table.addElement(tr);
		TD td = null;
		
		int total = 0;
		int i;
		for (i = 0; i < values.length; i++) {
			total += values[i];
		}

		for (i = 0; i < values.length; i++) {
			tr = new TR(true);
			tr.setAlign("center");
			td = new TD(names[i]);
			td.setAlign("left");
			tr.addElement(td);
			tr.addElement(new TD(Integer.toString(values[i])));
			tr.addElement(new TD(Util.toString((double)values[i] / total * 100.0)));
			
			table.addElement(tr);
		}
		
		tr = new TR(true);
		tr.setAlign("center");
		tr.addElement(new TH(Integer.toString(names.length)));
		tr.addElement(new TH(Integer.toString(total)));
		table.addElement(tr);
		
		return table;
	}
	
	public static void addIndexNavigator(Music music, Show show, Document doc) {
		Center c = new Center();
		
		java.util.Map m = new TreeMap();
		Iterator li = music.getShow().iterator();
		while (li.hasNext()) {
			Show s = (Show)li.next();
			String letter = Util.getPageFileName(s);
			if (!m.containsKey(letter)) {
				m.put(letter, Util.getLinkToPage(s));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String s = (String)li.next();
			String l = " " + s + " ";
			if (s.equals(Util.getPageFileName(show))) {
				c.addElement(l);
			} else {
				c.addElement(new A((String)m.get(s), l));
			}
		}
		
		doc.getBody().addElement(c);
	}

	private static String getCopyright() {
		StringBuffer cp = new StringBuffer();
		
		int year = 2003; // This is the first year of this data.
		int cur_year = Calendar.getInstance().get(Calendar.YEAR);
		
		cp.append("Copyright (c) ");
		cp.append(year++);
		for ( ; year <= cur_year; ++year) {
			cp.append(", ");
			cp.append(year);
		}
		
		cp.append(" Greg Bolsinga");
		
		return cp.toString();
	}
	
	private static String getGenerator() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(System.getProperty("music.program"));
		
		sb.append(" (built: ");
		sb.append(System.getProperty("music.builddate"));
		sb.append(" running on jdk ");
		sb.append(System.getProperty("java.runtime.version"));
		sb.append(" - ");
		sb.append(System.getProperty("os.name"));
		sb.append(" ");
		sb.append(System.getProperty("os.version"));
		sb.append(")");
		
		return sb.toString();
	}
	
	static Document createHTMLDocument(String title) {
		Document d = new Document();
		
        d.setDoctype(new org.apache.ecs.Doctype.Html40Strict());
		d.appendTitle(title);
		d.getHtml().setPrettyPrint(true);
		
		Head h = d.getHead();
		h.addElement(new Link().setRel("SHORTCUT ICON").setHref("http://homepage.mac.com/bolsinga/.Pictures/images/computer.ico"));
		h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new Meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new Meta().setContent(Web.getCopyright()).setName("Copyright"));
		h.addElement(new Meta().setContent(Web.getGenerator()).setName("Generator"));

		return d;
	}
}
