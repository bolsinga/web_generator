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

class DocumentCreator {
	String fOutputDir = null;
	String fType = null;
	String fCurPath = null;
	Document fDocument = null;
	
	public DocumentCreator(String outputDir, String type) {
		fOutputDir = outputDir;
		fType = type;
	}
	
	public Document getDocument(String path) {
		if ((fCurPath == null) || (!fCurPath.equals(path))) {
			if (fDocument != null) {
				writeDocument();
			}

			fCurPath = path;
			
			String letter = fCurPath;
			fDocument = Web.createHTMLDocument(letter, fType);
		}
		
		return fDocument;
	}
	
	public void close() {
		if (fDocument != null) {
			writeDocument();
			fDocument = null;
		}
	}
	
	private void writeDocument() {
		try {
			File f = new File(fOutputDir, fCurPath);
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
	
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}

public class Web {

	public static final String HTML_EXT = ".html";
	
	public static final String ARTIST_DIR = "artists";
	public static final String VENUE_DIR = "venues";
	public static final String SHOW_DIR = "shows";
	
	public static final String OTHER = "other";
	public static final String SEPARATOR = "/";
	public static final String HASH = "#";
	
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
	}
	
	// NOTE: Instead of a List of ID's, JAXB returns a List of real items.
	
	public static void generateArtistPages(Music music, String outputDir) {
		List items = music.getArtist();
		Artist item = null;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.ARTIST_COMPARATOR);
		
		DocumentCreator creator = new DocumentCreator(outputDir, "Artists");
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Artist)li.next();
			
			addItem(music, item, creator.getDocument(getPagePath(item)));
		}
		creator.close();
	}
	
	public static void generateVenuePages(Music music, String outputDir) {
		List items = music.getVenue();
		Venue item = null;

		Collections.sort(items, com.bolsinga.music.util.Compare.VENUE_COMPARATOR);

		DocumentCreator creator = new DocumentCreator(outputDir, "Venues");
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Venue)li.next();
			
			addItem(music, item, creator.getDocument(getPagePath(item)));
		}
		creator.close();
	}
	
	public static void generateDatePages(Music music, String outputDir) {
		List items = music.getShow();
		Show item = null;

		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);

		DocumentCreator creator = new DocumentCreator(outputDir, "Dates");
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Show)li.next();
			
			addItem(music, item, creator.getDocument(getPagePath(item)));
		}
		creator.close();
	}
	
	public static void addItem(Music music, Artist artist, Document doc) {
		Body b = doc.getBody();

		List shows = Lookup.getLookup(music).getShows(artist);
		
		b.addElement("----");
		b.addElement(artist.getName());
		b.addElement("----");
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			Show show = (Show)li.next();
			
			b.addElement(Util.toString(show.getDate()));
			b.addElement(((Venue)show.getVenue()).getName());
			
			ListIterator bi = show.getPerformance().listIterator();
			while (bi.hasNext()) {
				Performance p = (Performance)bi.next();
				Artist a = (Artist)p.getArtist();
				b.addElement(a.getName());
				b.addElement(" - ");
			}
			b.addElement("");
			b.addElement("----");
		}
	}
	
	public static void addItem(Music music, Venue venue, Document doc) {
		Body b = doc.getBody();

		List shows = Lookup.getLookup(music).getShows(venue);
		
		b.addElement("----");
		b.addElement(venue.getName());
		b.addElement("----");
		
		ListIterator li = shows.listIterator();
		while (li.hasNext()) {
			Show show = (Show)li.next();
			
			b.addElement(Util.toString(show.getDate()));
			b.addElement(((Venue)show.getVenue()).getName());
			
			ListIterator bi = show.getPerformance().listIterator();
			while (bi.hasNext()) {
				Performance p = (Performance)bi.next();
				Artist a = (Artist)p.getArtist();
				b.addElement(a.getName());
				b.addElement(" - ");
			}
			b.addElement("");
			b.addElement("----");
		}
	}
	
	public static void addItem(Music music, Show show, Document doc) {
		Body b = doc.getBody();

		b.addElement(getLinkTo(show));
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
		
		sb.append("My Program"); // Get this class name programmatically
		
		sb.append(" (built: ");
		sb.append("BUILD_DATE"); // Replace this at build time with ant facilities.
		sb.append(" running on jdk ");
		sb.append(System.getProperty("java.runtime.version"));
		sb.append(" - ");
		sb.append(System.getProperty("os.name"));
		sb.append(" ");
		sb.append(System.getProperty("os.version"));
		sb.append(")");
		
		return sb.toString();
	}
	
	static Document createHTMLDocument(String letter, String type) {
		Document d = new Document();
		
        d.setDoctype(new org.apache.ecs.Doctype.Html40Strict());
		StringBuffer sb = new StringBuffer();
		sb.append("'");
		sb.append(letter);
		sb.append("' ");
		sb.append(type);
		d.appendTitle(sb.toString());
		d.getHtml().setPrettyPrint(true);
		
		Head h = d.getHead();
		h.addElement(new Link().setRel("SHORTCUT ICON").setHref("http://homepage.mac.com/bolsinga/.Pictures/images/computer.ico"));
		h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new Meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new Meta().setContent(Web.getCopyright()).setName("Copyright"));
		h.addElement(new Meta().setContent(Web.getGenerator()).setName("Generator"));

		return d;
	}
	
	private static String getPageFileName(String name) {
		String file = Compare.simplify(name).substring(0, 1).toUpperCase();
		if (file.matches("\\W")) {
			file = OTHER;
		}
		return file;
	}
	
	private static String getPageFileName(BigInteger year) {
		if (year == null) {
			return OTHER;
		} else {
			return year.toString();
		}
	}
	
	public static String getPageFileName(Artist artist) {
		String name = artist.getSortname();
		if (name == null) {
			name = artist.getName();
		}
		return getPageFileName(name);
	}
	
	public static String getPageFileName(Venue venue) {
		return getPageFileName(venue.getName());
	}
	
	public static String getPageFileName(Show show) {
		BigInteger current = show.getDate().getYear();
		return getPageFileName(current);
	}
	
	public static String getPagePath(Artist artist) {
		StringBuffer sb = new StringBuffer();
		sb.append(ARTIST_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(artist));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public static String getPagePath(Venue venue) {
		StringBuffer sb = new StringBuffer();
		sb.append(VENUE_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(venue));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public static String getPagePath(Show show) {
		StringBuffer sb = new StringBuffer();
		sb.append(SHOW_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(show));
		sb.append(HTML_EXT);
		return sb.toString();
	}
	
	public static String getLinkTo(Artist artist) {
		StringBuffer link = new StringBuffer();
		
		link.append(Web.ARTIST_DIR);
		link.append(SEPARATOR);
		link.append(getPageFileName(artist));
		link.append(Web.HTML_EXT);
		link.append(HASH);
		link.append(artist.getId());
		
		return link.toString();
	}
	
	public static String getLinkTo(Venue venue) {
		StringBuffer link = new StringBuffer();
		
		link.append(Web.VENUE_DIR);
		link.append(SEPARATOR);
		link.append(getPageFileName(venue));
		link.append(Web.HTML_EXT);
		link.append(HASH);
		link.append(venue.getId());
		
		return link.toString();
	}
	
	public static String getLinkTo(Show show) {
		StringBuffer link = new StringBuffer();
		
		link.append(Web.SHOW_DIR);
		link.append(SEPARATOR);
		link.append(getPageFileName(show));
		link.append(Web.HTML_EXT);
		link.append(HASH);
		link.append(show.getId());
		
		return link.toString();
	}
	
	public static String getEntry(Artist artist) {
		return null;
	}
	
	public static String getEntry(Venue venue) {
		return null;
	}
	
	public static String getEntry(com.bolsinga.music.data.Date date) {
		return null;
	}
}
