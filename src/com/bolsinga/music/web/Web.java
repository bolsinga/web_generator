package com.bolsinga.music.web;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

abstract class DocumentCreator {
	Music fMusic = null;
	Links fLinks = null;
	String fOutputDir = null;
	private XhtmlDocument fDocument = null;
	String fProgram = null;
	div fMainDiv = null;
	div fSubsection = null;
	
	protected DocumentCreator(Music music, Links links, String outputDir, String program) {
		fMusic = music;
		fLinks = links;
		fOutputDir = outputDir;
		fProgram = program;
	}
	
	protected abstract boolean needNewDocument();
	protected abstract String getTitle();
	protected abstract XhtmlDocument createDocument(String title);
    
    protected boolean needNewSubsection() {
        return false;
    }
    
    protected div createSubsection() {
        return null;
    }
    
	protected abstract String getCurrentPath();
	protected abstract Element addIndexNavigator();
	
	public void close() {
		if (fDocument != null) {
			writeDocument();
			fDocument = null;
		}
	}
	
	protected div internalGetMainDiv() {
		if (needNewDocument()) {
            close();
            
			String title = getTitle();
			fDocument = createDocument(title);
			
			div headerDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_HEADER);
			headerDiv.addElement(new h1().addElement(title));
			headerDiv.addElement(com.bolsinga.web.util.Util.getLogo());
			headerDiv.addElement(addWebNavigator());
			headerDiv.addElement(addIndexNavigator());
			fDocument.getBody().addElement(headerDiv);
			
			fMainDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_MAIN);
		}
		return fMainDiv;
	}

    protected div internalGetSubsection() {
        if (needNewSubsection()) {
            if (fSubsection != null) {
                internalGetMainDiv().addElement(fSubsection);
            }

            fSubsection = createSubsection();
        }
        return fSubsection;
    }
	
	private void writeDocument() {
		if (fSubsection != null) {
			// Write out the last subsection's data if necessary
			fMainDiv.addElement(fSubsection);
		}
		
		fDocument.getBody().addElement(fMainDiv);

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
	
	protected Element addWebNavigator() {
		return fLinks.addWebNavigator(fMusic, fProgram);
	}
	
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}

class ArtistDocumentCreator extends DocumentCreator {
	Artist fDocArtist = null;
	Artist fArtist = null;
    String fArtistName = null;
	
	public ArtistDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}

	public void add(Music music, Links links, Artist item) {
		fArtist = item;
        internalGetSubsection().addElement(Web.addItem(music, links, item));
    }
	
	protected boolean needNewDocument() {
		return (fDocArtist == null) || (!fLinks.getPageFileName(fDocArtist).equals(fLinks.getPageFileName(fArtist)));
	}
	
	protected String getTitle() {
		return getTitle(fLinks.getPageFileName(fArtist), "Artists");
	}

	protected XhtmlDocument createDocument(String title) {
		fDocArtist = fArtist;
		return Web.createHTMLDocument(fLinks, title);
	}
	
    protected boolean needNewSubsection() {
        return (fArtistName == null) || (!fArtist.getName().equals(fArtistName));
    }

    protected div createSubsection() {
        fArtistName = fArtist.getName();

        a an = new a(); // named target
        an.setName(fArtist.getId());
        an.addElement("t", fArtistName);

        div subDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_SUB);
        subDiv.addElement(new h2().addElement(an));
        
        return subDiv;
    }

	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocArtist);
	}
	
	protected Element addIndexNavigator() {
		return Web.addIndexNavigator(fMusic, fLinks, fDocArtist);
	}
}

class VenueDocumentCreator extends DocumentCreator {
	Venue fDocVenue = null;
	Venue fVenue = null;
    String fVenueName = null;
	
	public VenueDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}

	public void add(Music music, Links links, Venue item) {
		fVenue = item;
		internalGetSubsection().addElement(Web.addItem(music, links, item));
    }
	
	protected boolean needNewDocument() {
		return (fDocVenue == null) || (!fLinks.getPageFileName(fDocVenue).equals(fLinks.getPageFileName(fVenue)));
	}
	
	protected String getTitle() {
		return getTitle(fLinks.getPageFileName(fVenue), "Venues");
	}

	protected XhtmlDocument createDocument(String title) {
		fDocVenue = fVenue;
		return Web.createHTMLDocument(fLinks, title);
	}
	
    protected boolean needNewSubsection() {
        return (fVenueName == null) || (!fVenue.getName().equals(fVenueName));
    }

    protected div createSubsection() {
        fVenueName = fVenue.getName();

        a an = new a(); // named target
        an.setName(fVenue.getId());
        an.addElement("t", fVenueName);

        div subDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_SUB);
        subDiv.addElement(new h2().addElement(an));
        
        return subDiv;
    }

	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocVenue);
	}
	
	protected Element addIndexNavigator() {
		return Web.addIndexNavigator(fMusic, fLinks, fDocVenue);
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
		fShow = item;
		internalGetSubsection().addElement(Web.addItem(music, fLinks, fShow));
	}
    
	protected boolean needNewDocument() {
		return (fDocShow == null) || (!fLinks.getPageFileName(fDocShow).equals(fLinks.getPageFileName(fShow)));
	}
	
	protected String getTitle() {
		return getTitle(fLinks.getPageFileName(fShow), "Dates");
	}

	protected XhtmlDocument createDocument(String title) {
		fDocShow = fShow;
		fDate = null;
		fSubsection = null;
		return Web.createHTMLDocument(fLinks, title);
	}
	
	protected boolean needNewSubsection() {
        return (fDate == null) || (!Util.toMonth(fShow.getDate()).equals(Util.toMonth(fDate)));
    }
    
    protected div createSubsection() {
        fDate = fShow.getDate();

        div subDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_SUB);
        subDiv.addElement(new h2().addElement(Util.toMonth(fDate)));
        
        return subDiv;
    }

	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocShow);
	}
	
	protected Element addIndexNavigator() {
		return Web.addIndexNavigator(fMusic, fLinks, fDocShow);
	}
}

class StatisticsCreator extends DocumentCreator {
	String fTitle = null;
	String fDirectory = null;
	String fFileName = null;
	
	public StatisticsCreator(Music music, Links links, String outputDir, String program) {
		this(music, links, outputDir, program, Links.STATS);
	}

	public StatisticsCreator(Music music, Links links, String outputDir, String program, String filename) {
		super(music, links, outputDir, program);
		fFileName = filename;
	}

    public void add(table t, String title, String directory) {
		fTitle = title;
		fDirectory = directory;

		// On stats pages, this is the only div containing the table.
		internalGetMainDiv().addElement(t);
    }

	protected boolean needNewDocument() {
		return true;
	}
	
	protected String getTitle() {
		return fTitle;
	}

	protected XhtmlDocument createDocument(String title) {
		return Web.createHTMLDocument(fLinks, title);
	}

	protected String getCurrentPath() {
		StringBuffer sb = new StringBuffer();
		sb.append(fDirectory);
		sb.append(File.separator);
		sb.append(fFileName);
		sb.append(Links.HTML_EXT);
		return sb.toString();
	}
	
	protected Element addIndexNavigator() {
		return null;
	}
}

class TracksStatisticsCreator extends StatisticsCreator {
	private boolean fTracksStats;
	
	public static TracksStatisticsCreator createTracksStats(Music music, Links links, String outputDir, String program) {
		return new TracksStatisticsCreator(music, links, outputDir, program, Links.STATS, true);
	}

	public static TracksStatisticsCreator createAlbumStats(Music music, Links links, String outputDir, String program) {
		return new TracksStatisticsCreator(music, links, outputDir, program, Links.ALBUM_STATS, false);
	}

	private TracksStatisticsCreator(Music music, Links links, String outputDir, String program, String filename, boolean isTracksStats) {
		super(music, links, outputDir, program, filename);
		fTracksStats = isTracksStats;
	}

	protected Element addIndexNavigator() {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.TRACKS_MENU);
		
		d.addElement(new h4("View: "));
		
		ul list = new ul();
		
		if (fTracksStats) {
			list.addElement(new li("Tracks"));
			list.addElement(new li(fLinks.getAlbumsLink()));
		} else {
			list.addElement(new li(fLinks.getTracksLink()));
			list.addElement(new li("Albums"));
		}
		
		d.addElement(list);
		
		return d;
	}
}

class TracksDocumentCreator extends DocumentCreator {
	Album fDocAlbum = null;
	Album fAlbum = null;
	
	public TracksDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}
	
	public void add(Music music, Links links, Album item) {
		fAlbum = item;
        internalGetMainDiv().addElement(Web.addItem(music, links, item));
    }
	
	protected boolean needNewDocument() {
		return (fDocAlbum == null) || (!fLinks.getPageFileName(fDocAlbum).equals(fLinks.getPageFileName(fAlbum)));
	}
	
	protected String getTitle() {
		return getTitle(fLinks.getPageFileName(fAlbum), "Tracks");
	}

	protected XhtmlDocument createDocument(String title) {
		fDocAlbum = fAlbum;
		return Web.createHTMLDocument(fLinks, title);
	}

	protected String getCurrentPath() {
		return fLinks.getPagePath(fDocAlbum);
	}
	
	protected Element addIndexNavigator() {
		return Web.addIndexNavigator(fMusic, fLinks, fDocAlbum);
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
		
		generateTracksPages(music, links, outputDir);
	}

	// NOTE: Instead of a List of ID's, JAXB returns a List of real items.
	
	public static void generateArtistPages(Music music, Links links, String outputDir) {
		List items = music.getArtist();
		Artist item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.ARTIST_COMPARATOR);
		
		ArtistDocumentCreator creator = new ArtistDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
		ListIterator iterator = items.listIterator();
		while (iterator.hasNext()) {
			item = (Artist)iterator.next();
			
            creator.add(music, links, item);
		}
		creator.close();
		
		Collections.sort(items, com.bolsinga.music.util.Compare.getCompare(music).ARTIST_STATS_COMPARATOR);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		iterator = items.listIterator();
		while (iterator.hasNext()) {
			item = (Artist)iterator.next();

			names[index] = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(item), item.getName()).toString();
			List shows = Lookup.getLookup(music).getShows(item);
			values[index] = (shows != null) ? shows.size() : 0;
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		stats.add(makeTable(names, values, "Shows by Artist", "Artist"), "Artist Statistics", Links.ARTIST_DIR);
		stats.close();
	}
	
	public static void generateVenuePages(Music music, Links links, String outputDir) {
		List items = music.getVenue();
		Venue item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.VENUE_COMPARATOR);

		VenueDocumentCreator creator = new VenueDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
		ListIterator iterator = items.listIterator();
		while (iterator.hasNext()) {
			item = (Venue)iterator.next();

            creator.add(music, links, item);
		}
		creator.close();

		Collections.sort(items, com.bolsinga.music.util.Compare.getCompare(music).VENUE_STATS_COMPARATOR);

		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		iterator = items.listIterator();
		while (iterator.hasNext()) {
			item = (Venue)iterator.next();

			names[index] = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(item), item.getName()).toString();
			values[index] = Lookup.getLookup(music).getShows(item).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		stats.add(makeTable(names, values, "Shows by Venue", "Venue"), "Venue Statistics", Links.VENUE_DIR);
		stats.close();
	}
	
	public static void generateDatePages(Music music, Links links, String outputDir) {
		List items = music.getShow();
		Show item = null;
		Vector list = null;
		TreeMap dates = new TreeMap(com.bolsinga.music.util.Compare.SHOW_STATS_COMPARATOR);
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);

		ShowDocumentCreator creator = new ShowDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
		ListIterator iterator = items.listIterator();
		while (iterator.hasNext()) {
			item = (Show)iterator.next();

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
			names[index] = com.bolsinga.web.util.Util.createInternalA(links.getLinkToPage(item), (year != null) ? year.toString() : "Unknown").toString();
			values[index] = ((Vector)dates.get(item)).size();
			
			index++;
		}
		
		StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, sResource.getString("program"));
		stats.add(makeTable(names, values, "Shows by Year", "Year"), "Show Statistics", Links.SHOW_DIR);
		stats.close();
	}
	
	public static void generateCityPages(Music music, Links links, String outputDir) {
		Collection items = Lookup.getLookup(music).getCities();
		String item = null;
		HashMap cityCount = new HashMap();
		String city = null;
		Integer val = null;
		HashSet set = null;
		
		Iterator iterator = items.iterator();
		while (iterator.hasNext()) {
			item = (String)iterator.next();
			
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
		creator.add(makeTable(names, values, "Shows by City", "City"), "City Statistics", Links.CITIES_DIR);
		creator.close();
	}

	public static void generateTracksPages(Music music, Links links, String outputDir) {
		List items = music.getAlbum();

		Album item = null;
		int index = 0;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.ALBUM_COMPARATOR);
		
		TracksDocumentCreator creator = new TracksDocumentCreator(music, links, outputDir, sResource.getString("program"));
		
		ListIterator iterator = items.listIterator();
		while (iterator.hasNext()) {
			item = (Album)iterator.next();
			
            creator.add(music, links, item);
		}
		creator.close();

		items = music.getArtist();
		Collections.sort(items, com.bolsinga.music.util.Compare.ARTIST_TRACKS_COMPARATOR);

		Artist artist = null;
		String[] names = new String[items.size()];
		int[] values = new int[items.size()];
		iterator = items.listIterator();
		while (iterator.hasNext()) {
			artist = (Artist)iterator.next();

			names[index] = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(artist), artist.getName()).toString();
			values[index] = Util.trackCount(artist);
			
			index++;
		}
		
		StatisticsCreator stats = TracksStatisticsCreator.createTracksStats(music, links, outputDir, sResource.getString("program"));
		stats.add(makeTable(names, values, "Tracks by Artist", "Artist"), "Tracks Statistics", Links.TRACKS_DIR);
		stats.close();

		items = music.getArtist();
		Collections.sort(items, com.bolsinga.music.util.Compare.ARTIST_ALBUMS_COMPARATOR);

		names = new String[items.size()];
		values = new int[items.size()];
		iterator = items.listIterator();
		index = 0;
		while (iterator.hasNext()) {
			artist = (Artist)iterator.next();

			names[index] = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(artist), artist.getName()).toString();
			values[index] = (artist.getAlbum() != null) ? artist.getAlbum().size() : 0;
			
			index++;
		}

		stats = TracksStatisticsCreator.createAlbumStats(music, links, outputDir, sResource.getString("program"));
		stats.add(makeTable(names, values, "Albums by Artist", "Artist"), "Album Statistics", Links.TRACKS_DIR);
		stats.close();
	}
	
	public static Element generatePreview(String sourceFile, int lastShowsCount) {
		Music music = Util.createMusic(sourceFile);
		return generatePreview(music, lastShowsCount);
	}
	
	public static Element generatePreview(Music music, int lastShowsCount) {
		Links links = Links.getLinks(false);

		List items = music.getShow();
		Show item = null;

		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		Collections.reverse(items);
		
		div previewDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_MAIN);
		
		div previewMenu = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_MENU);
		previewMenu.addElement(com.bolsinga.web.util.Util.getLogo());
				
		StringBuffer sb = new StringBuffer();
		sb.append("Generated ");
		sb.append(Util.sWebFormat.format(music.getTimestamp().getTime()));
		previewMenu.addElement(new h3(sb.toString()));

		ul list = new ul();
		
		sb = new StringBuffer();
		sb.append(music.getArtist().size());
		sb.append(" ");
		sb.append(links.getArtistLink());
		list.addElement(new li(sb.toString()));

		sb = new StringBuffer();
		sb.append(music.getShow().size());
		sb.append(" ");
		sb.append(links.getShowLink());
		list.addElement(new li(sb.toString()));

		sb = new StringBuffer();
		sb.append(music.getVenue().size());
		sb.append(" ");
		sb.append(links.getVenueLink());
		list.addElement(new li(sb.toString()));

		sb = new StringBuffer();
		sb.append(Lookup.getLookup(music).getCities().size());
		sb.append(" ");
		sb.append(links.getCityLink());
		list.addElement(new li(sb.toString()));

		sb = new StringBuffer();
		sb.append(music.getSong().size());
		sb.append(" ");
		sb.append(links.getTracksLink());
		list.addElement(new li(sb.toString()));

		sb = new StringBuffer();
		sb.append(music.getAlbum().size());
		sb.append(" ");
		sb.append(links.getAlbumsLink());
		list.addElement(new li(sb.toString()));

		previewMenu.addElement(list);
		
		previewMenu.addElement(links.getICalLink());
		
		previewDiv.addElement(previewMenu);
		
		div previewRecent = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_RECENT);
		
		sb = new StringBuffer();
		sb.append("Last ");
		sb.append(Integer.toString(lastShowsCount));
		sb.append(" shows:");
		previewRecent.addElement(new h3(sb.toString()));
		
		for (int i = 0; i < lastShowsCount; i++) {
			item = (Show)items.get(i);
			
			div previewShow = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_SHOW);
			
			previewShow.addElement(new h4(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(item), Util.toString(item.getDate()))));
			previewShow.addElement(getShowListing(links, item));
			
			previewRecent.addElement(previewShow);
		}
		

		previewDiv.addElement(previewRecent);
		
		return previewDiv;
	}
	
	public static String embedLinks(String sourceFile, String data, boolean upOneLevel) {
		Music music = Util.createMusic(sourceFile);
		
		return embedLinks(music, data, upOneLevel);
	}
	
	public static String embedLinks(Music music, String data, boolean upOneLevel) {
		return Encode.getEncode(music).addLinks(data, upOneLevel);
	}
	
	public static String getLinkedData(Music music, String data, boolean upOneLevel) {
		return com.bolsinga.web.util.Util.convertToParagraphs(embedLinks(music, data, upOneLevel));
	}
	
	public static div addItem(Music music, Links links, Artist artist) {
		div artistDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_ITEM);
				
		if (artist.getAlbum().size() > 0) {
			artistDiv.addElement(addTracks(music, links, artist));
		}
		
		Collection relations = Lookup.getLookup(music).getRelations(artist);
		if (relations != null) {
			artistDiv.addElement(addRelations(music, links, artist));
		}

		List shows = Lookup.getLookup(music).getShows(artist);
		if (shows != null) {
		    ListIterator iterator = shows.listIterator();
		    while (iterator.hasNext()) {
			    Show show = (Show)iterator.next();

				String showLink = links.getLinkTo(show);
			    
				div showDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_SHOW);
				
			    showDiv.addElement(new h3().addElement(com.bolsinga.web.util.Util.createInternalA(showLink, Util.toString(show.getDate()))));
			    
			    ul showInfo = new ul();
			    
			    li listItem = new li();
			    ListIterator bi = show.getArtist().listIterator();
			    while (bi.hasNext()) {
				    Artist performer = (Artist)bi.next();
				    
				    if (artist.equals(performer)) {
					    listItem.addElement(performer.getName());
				    } else {
					    listItem.addElement(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
				    }
				    
				    if (bi.hasNext()) {
					    listItem.addElement(", ");
				    }
			    }
			    showInfo.addElement(listItem);
			    
			    Venue venue = (Venue)show.getVenue();
			    a venueA = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(venue), venue.getName());
			    Location l = (Location)venue.getLocation();
			    showInfo.addElement(new li().addElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
			    
			    String comment = show.getComment();
			    if (comment != null) {
				    showInfo.addElement(new li(com.bolsinga.web.util.Util.createInternalA(showLink, "Show Summary")));
			    }
			    
			    showDiv.addElement(showInfo);
			    
			    artistDiv.addElement(showDiv);
		    }
	    }
		
		return artistDiv;
	}
	
	public static div addItem(Music music, Links links, Venue venue) {
		div venueDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_ITEM);
		
		Collection relations = Lookup.getLookup(music).getRelations(venue);
		if (relations != null) {
			venueDiv.addElement(addRelations(music, links, venue));
		}

		List shows = Lookup.getLookup(music).getShows(venue);
		ListIterator iterator = shows.listIterator();
		while (iterator.hasNext()) {
			Show show = (Show)iterator.next();
			
			String showLink = links.getLinkTo(show);
			
			div showDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_SHOW);
			
			showDiv.addElement(new h3().addElement(com.bolsinga.web.util.Util.createInternalA(showLink, Util.toString(show.getDate()))));
			
			ul showInfo = new ul();
			
			li listItem = new li();
			ListIterator bi = show.getArtist().listIterator();
			while (bi.hasNext()) {
				Artist performer = (Artist)bi.next();
				
				listItem.addElement(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
				
				if (bi.hasNext()) {
					listItem.addElement(", ");
				}
			}
			showInfo.addElement(listItem);
			
			Location l = (Location)venue.getLocation();
			showInfo.addElement(new li().addElement(venue.getName() + ", " + l.getCity() + ", " + l.getState()));
			
			String comment = show.getComment();
			if (comment != null) {
				showInfo.addElement(new li(com.bolsinga.web.util.Util.createInternalA(showLink, "Show Summary")));
			}
			
			showDiv.addElement(showInfo);
			
			venueDiv.addElement(showDiv);
		}
		
		return venueDiv;
	}
	
	private static ul getShowListing(Links links, Show show) {
		ul showInfo = new ul();
		
		li listItem = new li();
		ListIterator bi = show.getArtist().listIterator();
		while (bi.hasNext()) {
			Artist performer = (Artist)bi.next();
			
			listItem.addElement(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
			
			if (bi.hasNext()) {
				listItem.addElement(", ");
			}
		}
		showInfo.addElement(listItem);
		
		Venue venue = (Venue)show.getVenue();
		a venueA = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(venue), venue.getName());
		Location l = (Location)venue.getLocation();
		showInfo.addElement(new li(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
		
		return showInfo;
	}
	
	public static div addItem(Music music, Links links, Show show) {
		div showDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.SHOW_ITEM);

        a an = new a(); // named target
        an.setName(show.getId());
        an.addElement("t", Util.toString(show.getDate()));
        showDiv.addElement(new h3().addElement(an));
		
		showDiv.addElement(getShowListing(links, show));

		String comment = show.getComment();
		if (comment != null) {
			showDiv.addElement(com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.SHOW_COMMENT).addElement(getLinkedData(music, comment, true)));
		}
		
		return showDiv;
	}

	public static div addItem(Music music, Links links, Album album) {
		div albumDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.TRACKS_ITEM);
		
		StringBuffer sb;
		Artist artist = null;
		Song song;
		
		boolean isCompilation = album.isCompilation();
		
		a an = new a(); // named target
		an.setName(album.getId());
		an.addElement("test", album.getTitle());
		
		sb = new StringBuffer();
		sb.append(an);
		if (!isCompilation) {
			artist = (Artist)album.getPerformer();
			sb.append(" - ");
			sb.append(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(artist), artist.getName()));
		}
		com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
		if (albumRelease != null) {
			sb.append(" (");
			sb.append(albumRelease.getYear());
			sb.append(")");
		}
		
		albumDiv.addElement(new h2().addElement(sb.toString()));
		
		ol albumListing = new ol();

		ListIterator iterator = album.getSong().listIterator();
		while (iterator.hasNext()) {
			song = (Song)iterator.next();
			sb = new StringBuffer();
			if (isCompilation) {
				artist = (Artist)song.getPerformer();
				sb.append(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(artist), artist.getName()));
				sb.append(" - ");
			}
			
			sb.append(song.getTitle());
			
			if (albumRelease == null) {
				com.bolsinga.music.data.Date songRelease = song.getReleaseDate();
				if (songRelease != null) {
					sb.append(" (");
					sb.append(songRelease.getYear());
					sb.append(")");
				}
			}
			
			albumListing.addElement(new li(sb.toString()));
		}
		
		albumDiv.addElement(albumListing);
		
		return albumDiv;
	}
	
	public static div addRelations(Music music, Links links, Artist artist) {
		div relDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_RELATION);
			
		relDiv.addElement(new h3().addElement("See Also"));
		
		ul related = new ul();
		Iterator iterator = Lookup.getLookup(music).getRelations(artist).iterator();
		while (iterator.hasNext()) {
			Artist art = (Artist)iterator.next();
			if (art.equals(artist)) {
				related.addElement(new li().addElement(art.getName()));
			} else {
				related.addElement(new li().addElement(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(art), art.getName())));
			}
		}
		relDiv.addElement(related);

		return relDiv;
	}
	
	public static div addRelations(Music music, Links links, Venue venue) {
		div relDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_RELATION);

		relDiv.addElement(new h3().addElement("See Also"));
		
		ul related = new ul();
		Iterator iterator = Lookup.getLookup(music).getRelations(venue).iterator();
		while (iterator.hasNext()) {
			Venue v = (Venue)iterator.next();
			if (v.equals(venue)) {
				related.addElement(new li().addElement(v.getName()));
			} else {
				related.addElement(new li().addElement(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(v), v.getName())));
			}
		}
		relDiv.addElement(related);

		return relDiv;
	}

	public static div addTracks(Music music, Links links, Artist artist) {
		div albumsDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_TRACKS);
			
		albumsDiv.addElement(new h3().addElement("Albums"));
		
		ul related = new ul();
		Iterator iterator = artist.getAlbum().iterator();
		while (iterator.hasNext()) {
			Album alb = (Album)iterator.next();
			
			StringBuffer sb = new StringBuffer();
			sb.append(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(alb), alb.getTitle()));
			com.bolsinga.music.data.Date albumRelease = alb.getReleaseDate();
			if (albumRelease != null) {
				sb.append(" (");
				sb.append(albumRelease.getYear());
				sb.append(")");
			}
			related.addElement(new li().addElement(sb.toString()));
		}
		albumsDiv.addElement(related);

		return albumsDiv;
	}
	
	public static Element addIndexNavigator(Music music, Links links, Artist artist) {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_INDEX);
				
		java.util.Map m = new TreeMap();
		Iterator iterator = music.getArtist().iterator();
		while (iterator.hasNext()) {
			Artist art = (Artist)iterator.next();
			String letter = links.getPageFileName(art);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(art));
			}
		}

		ul list = new ul();

		iterator = m.keySet().iterator();
		while (iterator.hasNext()) {
			String s = (String)iterator.next();
			if (s.equals(links.getPageFileName(artist))) {
				list.addElement(new li(s));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s)));
			}
		}
		
		d.addElement(list);
		
		return d;
	}
	
	public static Element addIndexNavigator(Music music, Links links, Venue venue) {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_INDEX);
		
		java.util.Map m = new TreeMap();
		Iterator iterator = music.getVenue().iterator();
		while (iterator.hasNext()) {
			Venue v = (Venue)iterator.next();
			String letter = links.getPageFileName(v);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(v));
			}
		}

		ul list = new ul();
		
		iterator = m.keySet().iterator();
		while (iterator.hasNext()) {
			String v = (String)iterator.next();
			if (v.equals(links.getPageFileName(venue))) {
				list.addElement(new li(v));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(v), v)));
			}
		}
		
		d.addElement(list);
		
		return d;
	}

	public static Element addIndexNavigator(Music music, Links links, Album album) {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.TRACKS_INDEX);
		
		java.util.Map m = new TreeMap();
		Iterator iterator = music.getAlbum().iterator();
		while (iterator.hasNext()) {
			Album alb = (Album)iterator.next();
			String letter = links.getPageFileName(alb);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(alb));
			}
		}

		ul list = new ul();
		
		iterator = m.keySet().iterator();
		while (iterator.hasNext()) {
			String s = (String)iterator.next();
			if (s.equals(links.getPageFileName(album))) {
				list.addElement(new li(s));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s)));
			}
		}
		
		d.addElement(list);
		
		return d;
	}
	
	public static table makeTable(String[] names, int[] values, String caption, String header) {
		table t = new table();
		t.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		caption capt = new caption();
		capt.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		capt.addElement(caption);
		t.addElement(capt);
		tr trow = new tr().addElement(new th(header)).addElement(new th("#")).addElement(new th("%"));
        trow.setClass(com.bolsinga.web.util.CSS.TABLE_HEADER);
		trow.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		t.addElement(trow);
		th thh = null;
		
		int total = 0;
		int i;
		for (i = 0; i < values.length; i++) {
			total += values[i];
		}

		for (i = 0; i < values.length; i++) {
			trow = new tr();
			trow.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
            trow.setClass((((i + 1) % 2) == 1) ? com.bolsinga.web.util.CSS.TABLE_ROW : com.bolsinga.web.util.CSS.TABLE_ROW_ALT);
			thh = new th(names[i]);
			thh.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
			trow.addElement(thh);
			trow.addElement(new td(Integer.toString(values[i])).setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint()));
			trow.addElement(new td(Util.toString((double)values[i] / total * 100.0)).setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint()));
			
			t.addElement(trow);
		}
		
		trow = new tr();
		trow.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
        trow.setClass(com.bolsinga.web.util.CSS.TABLE_FOOTER);
		trow.addElement(new th(Integer.toString(names.length)));
		trow.addElement(new th(Integer.toString(total)));
		trow.addElement(new th());
		t.addElement(trow);
		
		return t;
	}
	
	public static Element addIndexNavigator(Music music, Links links, Show show) {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.SHOW_INDEX);
		
		java.util.Map m = new TreeMap();
		Iterator iterator = music.getShow().iterator();
		while (iterator.hasNext()) {
			Show s = (Show)iterator.next();
			String letter = links.getPageFileName(s);
			if (!m.containsKey(letter)) {
				m.put(letter, links.getLinkToPage(s));
			}
		}

		ul list = new ul();
		
		iterator = m.keySet().iterator();
		while (iterator.hasNext()) {
			String s = (String)iterator.next();
			if (s.equals(links.getPageFileName(show))) {
				list.addElement(new li(s));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s)));
			}
		}
		
		list.addElement(new li(links.getICalLink()));
		
		d.addElement(list);
		
		return d;
	}

	private static String getCopyright() {
		StringBuffer cp = new StringBuffer();
		
		int year = 2003; // This is the first year of this data.
		int cur_year = Calendar.getInstance().get(Calendar.YEAR);
		
		cp.append("Contents Copyright (c) ");
		cp.append(year++);
		for ( ; year <= cur_year; ++year) {
			cp.append(", ");
			cp.append(year);
		}
		
		cp.append(" ");
		cp.append(System.getProperty("user.name"));
		
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
		
		sb.append(" [");
		sb.append(sResource.getString("copyright"));
		sb.append("]");
		
		sb.append(")");
		
		return sb.toString();
	}
	
	static XhtmlDocument createHTMLDocument(Links links, String title) {
		XhtmlDocument d = new XhtmlDocument(ECSDefaults.getDefaultCodeset());

		d.getHtml().setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		
        d.setDoctype(new org.apache.ecs.Doctype.XHtml10Strict());
		d.appendTitle(title);
		
		head h = d.getHead();
		h.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		h.addElement(com.bolsinga.web.util.Util.getIconLink());
		h.addElement(links.getLinkToStyleSheet());

		h.addElement(new meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
		h.addElement(new meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new meta().setContent(Web.getGenerator()).setName("Generator"));
		h.addElement(new meta().setContent(Web.getCopyright()).setName("Copyright"));

		d.getBody().setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());

		return d;
	}
}
