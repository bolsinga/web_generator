package com.bolsinga.music.web;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

abstract class DocumentCreator {
	Music fMusic = null;
	Links fLinks = null;
	String fOutputDir = null;
	Document fDocument = null;
	String fProgram = null;
	
	protected DocumentCreator(Music music, Links links, String outputDir, String program) {
		fMusic = music;
		fLinks = links;
		fOutputDir = outputDir;
		fProgram = program;
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
		fDocument.getBody().addElement(new Center(Web.getImage()));
	}
	
	protected void addWebNavigator() {
		fLinks.addWebNavigator(fMusic, fDocument, fProgram);
	}
	
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}

class ArtistDocumentCreator extends DocumentCreator {
	Artist fDocArtist = null;
	Artist fArtist = null;
	
	public ArtistDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}
	
	public Document getDocument(Artist artist) {
		fArtist = artist;
		return internalGetDocument();
	}
	
	protected boolean needNewDocument() {
		return (fDocArtist == null) || (!fLinks.getPageFileName(fDocArtist).equals(fLinks.getPageFileName(fArtist)));
	}
	
	protected Document createDocument() {
		fDocArtist = fArtist;
		return Web.createHTMLDocument(getTitle(fLinks.getPageFileName(fDocArtist), "Artists"));
	}
	
	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocArtist);
	}
	
	protected void addIndexNavigator() {
		Web.addIndexNavigator(fMusic, fLinks, fDocArtist, fDocument);
	}
}

class VenueDocumentCreator extends DocumentCreator {
	Venue fDocVenue = null;
	Venue fVenue = null;
	
	public VenueDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}
	
	public Document getDocument(Venue venue) {
		fVenue = venue;
		return internalGetDocument();
	}
	
	protected boolean needNewDocument() {
		return (fDocVenue == null) || (!fLinks.getPageFileName(fDocVenue).equals(fLinks.getPageFileName(fVenue)));
	}
	
	protected Document createDocument() {
		fDocVenue = fVenue;
		return Web.createHTMLDocument(getTitle(fLinks.getPageFileName(fDocVenue), "Venues"));
	}
	
	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocVenue);
	}
	
	protected void addIndexNavigator() {
		Web.addIndexNavigator(fMusic, fLinks, fDocVenue, fDocument);
	}
}

class ShowDocumentCreator extends DocumentCreator {
	Show fDocShow = null;
	Show fShow = null;
	com.bolsinga.music.data.Date fDate = null;
	
	public ShowDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
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

		Web.addItem(music, fLinks, item, doc);
	}
	
	public Document getDocument(Show show) {
		fShow = show;
		return internalGetDocument();
	}
	
	protected boolean needNewDocument() {
		return (fDocShow == null) || (!fLinks.getPageFileName(fDocShow).equals(fLinks.getPageFileName(fShow)));
	}
	
	protected Document createDocument() {
		fDocShow = fShow;
		fDate = null;
		return Web.createHTMLDocument(getTitle(fLinks.getPageFileName(fDocShow), "Dates"));
	}
	
	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocShow);
	}
	
	protected void addIndexNavigator() {
		Web.addIndexNavigator(fMusic, fLinks, fDocShow, fDocument);
	}
}

class StatisticsCreator extends DocumentCreator {
	String fTitle = null;
	String fDirectory = null;
	
	public StatisticsCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
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
		sb.append(File.separator);
		sb.append(Links.STATS);
		sb.append(Links.HTML_EXT);
		return sb.toString();
	}
	
	protected void addIndexNavigator() {

	}
}

public class Web {
	
	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.music.web.web");
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: Web [source.xml] [output.dir]");
			System.exit(0);
		}
		
		Web.generate(args[0], args[1]);
	}
	
	public static void generate(String sourceFile, String outputDir) {
		Music music = Util.createMusic(sourceFile);
		generate(music, outputDir);
	}
	
	public static void generate(Music music, String outputDir) {
		Links links = Links.getLinks(true);
		
		generateArtistPages(music, links, outputDir);
		
		generateVenuePages(music, links, outputDir);
		
		generateDatePages(music, links, outputDir);
		
		generateCityPages(music, links, outputDir);
	}

	// NOTE: Instead of a List of ID's, JAXB returns a List of real items.
	
	public static void generateArtistPages(Music music, Links links, String outputDir) {
		List items = music.getArtist();
		Artist item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.ARTIST_COMPARATOR);
		
		ArtistDocumentCreator creator = new ArtistDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();
			
			addItem(music, links, item, creator.getDocument(item));
		}
		creator.close();
		
		Collections.sort(items, com.bolsinga.music.util.Compare.getCompare(music).ARTIST_STATS_COMPARATOR);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();

			names[index] = new A(links.getLinkTo(item), item.getName()).toString();
			values[index] = Lookup.getLookup(music).getShows(item).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		stats.getDocument("Artist Statistics", Links.ARTIST_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by Artist", "Artist")));
		stats.close();
	}
	
	public static void generateVenuePages(Music music, Links links, String outputDir) {
		List items = music.getVenue();
		Venue item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.VENUE_COMPARATOR);

		VenueDocumentCreator creator = new VenueDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Venue)li.next();

			addItem(music, links, item, creator.getDocument(item));
		}
		creator.close();

		Collections.sort(items, com.bolsinga.music.util.Compare.getCompare(music).VENUE_STATS_COMPARATOR);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		li = items.listIterator();
		while (li.hasNext()) {
			item = (Venue)li.next();

			names[index] = new A(links.getLinkTo(item), item.getName()).toString();
			values[index] = Lookup.getLookup(music).getShows(item).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		stats.getDocument("Venue Statistics", Links.VENUE_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by Venue", "Venue")));
		stats.close();
	}
	
	public static void generateDatePages(Music music, Links links, String outputDir) {
		List items = music.getShow();
		Show item = null;
		Vector list = null;
		TreeMap dates = new TreeMap(com.bolsinga.music.util.Compare.SHOW_STATS_COMPARATOR);
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);

		ShowDocumentCreator creator = new ShowDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
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
			names[index] = new A(links.getLinkToPage(item), (year != null) ? year.toString() : "Unknown").toString();
			values[index] = ((Vector)dates.get(item)).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		stats.getDocument("Show Statistics", Links.SHOW_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by Year", "Year")));
		stats.close();
	}
	
	public static void generateCityPages(Music music, Links links, String outputDir) {
		Collection items = Lookup.getLookup(music).getCities();
		String item = null;
		HashMap cityCount = new HashMap();
		String city = null;
		Integer val = null;
		HashSet set = null;
		
		Iterator li = items.iterator();
		while (li.hasNext()) {
			item = (String)li.next();
			
			val = new Integer(Lookup.getLookup(music).getShows(item).size());
			if (cityCount.containsKey(val)) {
				set = (HashSet)cityCount.get(val);
				set.add(item);
			} else {
				set = new HashSet();
				set.add(item);
				cityCount.put(val, set);
			}
		}
		
		List keys = new Vector(cityCount.keySet());
		Collections.sort(keys);
		Collections.reverse(keys);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		int index = 0;
		
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			val = (Integer)i.next();
			
			List k = new Vector((HashSet)cityCount.get(val));
			Collections.sort(k);
			
			Iterator j = k.iterator();
			while (j.hasNext()) {
				names[index] = (String)j.next();
				values[index] = val.intValue();
				index++;
			}
		}
		
		StatisticsCreator creator = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		creator.getDocument("City Statistics", Links.CITIES_DIR).getBody().addElement(new Center().addElement(makeTable(names, values, "Shows by City", "City")));
		creator.close();
	}
	
	public static String generatePreview(String sourceFile, int lastShowsCount) {
		Music music = Util.createMusic(sourceFile);
		return generatePreview(music, lastShowsCount);
	}
	
	public static String generatePreview(Music music, int lastShowsCount) {
		Links links = Links.getLinks(false);

		List items = music.getShow();
		Show item = null;

		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		Collections.reverse(items);
		
		TBody tbody = null;
		TR tr = null;
		StringBuffer sb = null;
		
		Table navigation = null;
		Table recent = null;
		
		tbody = new TBody();
		tr = new TR().setAlign("right");
		tr.addElement(new TD(Web.getImage()));
		tbody.addElement(tr);
		
		sb = new StringBuffer();
		sb.append("Generated ");
		sb.append(Util.sWebFormat.format(music.getTimestamp().getTime()));
		tr = new TR().setAlign("right");
		tr.addElement(new TD(sb.toString()));
		tbody.addElement(tr);
		
		tr = new TR().setAlign("right");
		sb = new StringBuffer();
		sb.append(music.getArtist().size());
		sb.append(" ");
		sb.append(links.getArtistLink());
		tr.addElement(new TD(sb.toString()));
		tbody.addElement(tr);

		tr = new TR().setAlign("right");
		sb = new StringBuffer();
		sb.append(music.getShow().size());
		sb.append(" ");
		sb.append(links.getShowLink());
		tr.addElement(new TD(sb.toString()));
		tbody.addElement(tr);
		
		tr = new TR().setAlign("right");
		sb = new StringBuffer();
		sb.append(music.getVenue().size());
		sb.append(" ");
		sb.append(links.getVenueLink());
		tr.addElement(new TD(sb.toString()));
		tbody.addElement(tr);
		
		tr = new TR().setAlign("right");
		sb = new StringBuffer();
		sb.append(Lookup.getLookup(music).getCities().size());
		sb.append(" ");
		sb.append(links.getCityLink());
		tr.addElement(new TD(sb.toString()));
		tbody.addElement(tr);
		
		navigation = new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(0).addElement(tbody);

		tbody = new TBody();
		sb = new StringBuffer();
		sb.append("Last ");
		sb.append(Integer.toString(lastShowsCount));
		sb.append(" shows:");
		tr = new TR().setAlign("center");
		tr.addElement(new TD(sb.toString()));
		tbody.addElement(tr);
		
		TD td = new TD();
		for (int i = 0; i < lastShowsCount; i++) {
			item = (Show)items.get(i);
			
			UL showListing = new UL();
			showListing.addElement(new LI().addElement(new A(links.getLinkTo(item), Util.toString(item.getDate()))));
			
			UL showInfo = new UL();
			
			LI listItem = new LI();
			ListIterator li = item.getPerformance().listIterator();
			while (li.hasNext()) {
				Performance p = (Performance)li.next();
				Artist performer = (Artist)p.getArtist();
				
				listItem.addElement(new A(links.getLinkTo(performer), performer.getName()));
				
				if (li.hasNext()) {
					listItem.addElement(", ");
				}
			}
			showInfo.addElement(listItem);
			
			Venue venue = (Venue)item.getVenue();
			A venueA = new A(links.getLinkTo(venue), venue.getName());
			Location l = (Location)venue.getLocation();
			showInfo.addElement(new LI().addElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
			
			showListing.addElement(showInfo);
			
			td.addElement(showListing);
			
			td.addElement(new P());
		}
		
		tbody.addElement(new TR().addElement(td));
		recent = new Table().setBorder(0).setWidth("100%").setCellSpacing(5).setCellPadding(0).addElement(tbody);
		
		sb = new StringBuffer();
		sb.append(navigation.toString());
		sb.append(new HR().toString());
		sb.append(recent.toString());
		
		return sb.toString();
	}
	
	public static String embedLinks(String sourceFile, String data, boolean upOneLevel) {
		Music music = Util.createMusic(sourceFile);
		
		return embedLinks(music, data, upOneLevel);
	}
	
	public static String embedLinks(Music music, String data, boolean upOneLevel) {
		return Encode.getEncode(music).addLinks(data, upOneLevel);
	}
	
	private static HashMap sLinkedData = new HashMap();
	
	public static synchronized String getLinkedData(Music music, String data, boolean upOneLevel) {
		if (!sLinkedData.containsKey(data)) {
			sLinkedData.put(data, embedLinks(music, data, upOneLevel));
		}
		
		return (String)sLinkedData.get(data);
	}
	
	public static void addItem(Music music, Links links, Artist artist, Document doc) {
		Body b = doc.getBody();

		List shows = Lookup.getLookup(music).getShows(artist);
		
		b.addElement(new HR());
		A a = new A();
		a.setName(artist.getId());
		a.addElement("test", artist.getName());
		b.addElement(new Center().addElement(new Big().addElement(a)));
		
		addRelations(music, links, artist, doc);
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			Show show = (Show)li.next();
			
			UL showListing = new UL();
			
			showListing.addElement(new LI().addElement(new A(links.getLinkTo(show), Util.toString(show.getDate()))));
			
			UL showInfo = new UL();
			
			LI listItem = new LI();
			ListIterator bi = show.getPerformance().listIterator();
			while (bi.hasNext()) {
				Performance p = (Performance)bi.next();
				Artist performer = (Artist)p.getArtist();
				
				if (artist.equals(performer)) {
					listItem.addElement(new B(performer.getName()));
				} else {
					listItem.addElement(new A(links.getLinkTo(performer), performer.getName()));
				}
				
				if (bi.hasNext()) {
					listItem.addElement(", ");
				}
			}
			showInfo.addElement(listItem);
			
			Venue venue = (Venue)show.getVenue();
			A venueA = new A(links.getLinkTo(venue), venue.getName());
			Location l = (Location)venue.getLocation();
			showInfo.addElement(new LI().addElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
			
			String comment = show.getComment();
			if (comment != null) {
				showInfo.addElement(new LI(getLinkedData(music, comment, true)));
			}
			
			showListing.addElement(showInfo);
			
			b.addElement(showListing);
		}
	}
	
	public static void addItem(Music music, Links links, Venue venue, Document doc) {
		Body b = doc.getBody();

		List shows = Lookup.getLookup(music).getShows(venue);
		
		b.addElement(new HR());
		A a = new A();
		a.setName(venue.getId());
		a.addElement("test", venue.getName());
		b.addElement(new Center().addElement(new Big().addElement(a)));
		
		addRelations(music, links, venue, doc);

		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			Show show = (Show)li.next();
			
			UL showListing = new UL();
			
			showListing.addElement(new LI().addElement(new A(links.getLinkTo(show), Util.toString(show.getDate()))));
			
			UL showInfo = new UL();
			
			LI listItem = new LI();
			ListIterator bi = show.getPerformance().listIterator();
			while (bi.hasNext()) {
				Performance p = (Performance)bi.next();
				Artist performer = (Artist)p.getArtist();
				
				listItem.addElement(new A(links.getLinkTo(performer), performer.getName()));
				
				if (bi.hasNext()) {
					listItem.addElement(", ");
				}
			}
			showInfo.addElement(listItem);
			
			B venueB = new B(venue.getName());
			Location l = (Location)venue.getLocation();
			showInfo.addElement(new LI().addElement(venueB.toString() + ", " + l.getCity() + ", " + l.getState()));
			
			String comment = show.getComment();
			if (comment != null) {
				showInfo.addElement(new LI(getLinkedData(music, comment, true)));
			}
			
			showListing.addElement(showInfo);
			
			b.addElement(showListing);
		}
	}
	
	public static void addItem(Music music, Links links, Show show, Document doc) {
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
			
			listItem.addElement(new A(links.getLinkTo(performer), performer.getName()));
			
			if (bi.hasNext()) {
				listItem.addElement(", ");
			}
		}
		showInfo.addElement(listItem);
		
		Venue venue = (Venue)show.getVenue();
		A venueA = new A(links.getLinkTo(venue), venue.getName());
		Location l = (Location)venue.getLocation();
		showInfo.addElement(new LI(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));

		String comment = show.getComment();
		if (comment != null) {
			showInfo.addElement(new LI(getLinkedData(music, comment, true)));
		}

		showListing.addElement(showInfo);
		
		b.addElement(showListing);
	}
	
	public static void addRelations(Music music, Links links, Artist artist, Document doc) {
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
					related.addElement(new LI().addElement(new A(links.getLinkTo(a), a.getName())));
				}
			}
			ul.addElement(related);
			
			b.addElement(ul);
		}
	}
	
	public static void addRelations(Music music, Links links, Venue venue, Document doc) {
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
					ul.addElement(new LI().addElement(new A(links.getLinkTo(v), v.getName())));
				}
			}
			
			b.addElement(ul);
		}
	}
	
	public static void addIndexNavigator(Music music, Links links, Artist artist, Document doc) {
		Center c = new Center();
		
		java.util.Map m = new TreeMap();
		Iterator li = music.getArtist().iterator();
		while (li.hasNext()) {
			Artist a = (Artist)li.next();
			String letter = links.getPageFileName(a);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(a));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String a = (String)li.next();
			if (a.equals(links.getPageFileName(artist))) {
				c.addElement(a + " ");
			} else {
				c.addElement(new A((String)m.get(a), a).toString() + " ");
			}
		}
		
		doc.getBody().addElement(c);
	}
	
	public static void addIndexNavigator(Music music, Links links, Venue venue, Document doc) {
		Center c = new Center();
		
		java.util.Map m = new TreeMap();
		Iterator li = music.getVenue().iterator();
		while (li.hasNext()) {
			Venue v = (Venue)li.next();
			String letter = links.getPageFileName(v);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(v));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String v = (String)li.next();
			String l = " " + v + " ";
			if (v.equals(links.getPageFileName(venue))) {
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
	
	public static void addIndexNavigator(Music music, Links links, Show show, Document doc) {
		Center c = new Center();
		
		java.util.Map m = new TreeMap();
		Iterator li = music.getShow().iterator();
		while (li.hasNext()) {
			Show s = (Show)li.next();
			String letter = links.getPageFileName(s);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(s));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String s = (String)li.next();
			String l = " " + s + " ";
			if (s.equals(links.getPageFileName(show))) {
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
		
		sb.append(sResource.getString("program"));
		
		sb.append(" (built: ");
		sb.append(sResource.getString("builddate"));
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
		Document d = new Document(ECSDefaults.getDefaultCodeset());
		
        d.setDoctype(new org.apache.ecs.Doctype.Html401Transitional());
		d.appendTitle(title);
		d.getHtml().setPrettyPrint(true);
		
		Head h = d.getHead();
		h.addElement(new Meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
		h.addElement(new Link().setRel("SHORTCUT ICON").setHref("http://homepage.mac.com/bolsinga/.Pictures/images/computer.ico"));
		h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new Meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new Meta().setContent(Web.getGenerator()).setName("Generator"));
		h.addElement(new Meta().setContent(Web.getCopyright()).setName("Copyright"));

		return d;
	}
	
	static IMG getImage() {
		IMG img = new IMG("http://homepage.mac.com/bolsinga/.Pictures/images/comp.gif");
		img.setHeight(90);
		img.setWidth(120);
		img.setAlt("[Busy computing... for you!]");
		return img;
	}
}
