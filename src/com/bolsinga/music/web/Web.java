package com.bolsinga.music.web;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.math.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

abstract class MusicDocumentCreator extends com.bolsinga.web.util.DocumentCreator {
	Music fMusic = null;
	Links fLinks = null;
	String fProgram = null;
    
    protected MusicDocumentCreator(Music music, Links links, String outputDir, String program) {
        super(outputDir);
		fMusic = music;
		fLinks = links;
		fProgram = program;
    }

    protected XhtmlDocument createDocument() {
        return Web.createHTMLDocument(fLinks, getTitle());
    }

    protected div getHeaderDiv() {
        div headerDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_HEADER);
        headerDiv.addElement(new h1().addElement(getTitle()));
        headerDiv.addElement(com.bolsinga.web.util.Util.getLogo());
        headerDiv.addElement(fLinks.addWebNavigator(fMusic, fProgram));
        headerDiv.addElement(addIndexNavigator());
        return headerDiv;
    }
}

class ArtistDocumentCreator extends MusicDocumentCreator {
	Artist fLastArtist = null;
	Artist fCurArtist = null;
	
	public ArtistDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}

	public void add(Music music, Links links, Artist item) {
		fCurArtist = item;
        getSubsection().addElement(Web.addItem(music, links, fCurArtist));
        fLastArtist = fCurArtist;
    }
	
	protected String getTitle() {
		return getTitle("Artists");
	}
	
	protected boolean needNewDocument() {
		return ((fLastArtist == null) || !fLinks.getPageFileName(fLastArtist).equals(getCurrentLetter()));
	}
	
    protected boolean needNewSubsection() {
        return ((fLastArtist == null) || !fLastArtist.getName().equals(fCurArtist.getName()));
    }

    protected Element getSubsectionTitle() {
        a an = new a(); // named target
        an.setName(fCurArtist.getId());
        an.addElement("t", fCurArtist.getName());
        return new h2().addElement(an);
    }

	protected String getLastPath() {
		return fLinks.getPagePath(fLastArtist);
	}
	
	protected String getCurrentLetter() {
        return fLinks.getPageFileName(fCurArtist);
    }
    
	protected Element addIndexNavigator() {
		return Web.addArtistIndexNavigator(fMusic, fLinks, getCurrentLetter());
	}
}

class VenueDocumentCreator extends MusicDocumentCreator {
	Venue fLastVenue = null;
	Venue fCurVenue = null;
	
	public VenueDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}

	public void add(Music music, Links links, Venue item) {
		fCurVenue = item;
		getSubsection().addElement(Web.addItem(music, links, fCurVenue));
		fLastVenue = fCurVenue;
    }
	
	protected String getTitle() {
		return getTitle("Venues");
	}
	
	protected boolean needNewDocument() {
		return (fLastVenue == null) || (!fLinks.getPageFileName(fLastVenue).equals(getCurrentLetter()));
	}
	
    protected boolean needNewSubsection() {
        return (fLastVenue == null) || (!fLastVenue.getName().equals(fCurVenue.getName()));
    }

    protected Element getSubsectionTitle() {
        a an = new a(); // named target
        an.setName(fCurVenue.getId());
        an.addElement("t", fCurVenue.getName());
        return new h2().addElement(an);
    }

	protected String getLastPath() {
		return fLinks.getPagePath(fLastVenue);
	}

	protected String getCurrentLetter() {
        return fLinks.getPageFileName(fCurVenue);
    }
	
	protected Element addIndexNavigator() {
		return Web.addVenueIndexNavigator(fMusic, fLinks, getCurrentLetter());
	}
}

class ShowDocumentCreator extends MusicDocumentCreator {
	Show fLastShow = null;
	Show fCurShow = null;
    
	public ShowDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}
	
	public void add(Music music, Show item) {
		fCurShow = item;
		getSubsection().addElement(Web.addItem(music, fLinks, fCurShow));
		fLastShow = fCurShow;
	}
	
	protected String getTitle() {
		return getTitle("Dates");
	}
    
	protected boolean needNewDocument() {
		return (fLastShow == null) || (!fLinks.getPageFileName(fLastShow).equals(getCurrentLetter()));
	}
	
	protected boolean needNewSubsection() {
        return (fLastShow == null) || (!Util.toMonth(fLastShow.getDate()).equals(Util.toMonth(fCurShow.getDate())));
    }
    
    protected Element getSubsectionTitle() {
        return new h2().addElement(Util.toMonth(fCurShow.getDate()));
    }

	protected String getLastPath() {
		return fLinks.getPagePath(fLastShow);
	}
	
	protected String getCurrentLetter() {
        return fLinks.getPageFileName(fCurShow);
    }

	protected Element addIndexNavigator() {
		return Web.addShowIndexNavigator(fMusic, fLinks, getCurrentLetter());
	}
}

class StatisticsCreator extends MusicDocumentCreator {
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
		getMainDiv().addElement(t);
    }
	
	protected String getTitle() {
		return fTitle;
	}

	protected boolean needNewDocument() {
		return true;
	}

    protected boolean needNewSubsection() {
        return false;
    }

    protected Element getSubsectionTitle() {
        return null;
    }

	protected String getLastPath() {
		StringBuffer sb = new StringBuffer();
		sb.append(fDirectory);
		sb.append(File.separator);
		sb.append(fFileName);
		sb.append(Links.HTML_EXT);
		return sb.toString();
	}

	protected String getCurrentLetter() {
        return null;
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

class TracksDocumentCreator extends MusicDocumentCreator {
	Album fLastAlbum = null;
	Album fCurAlbum = null;
	
	public TracksDocumentCreator(Music music, Links links, String outputDir, String program) {
		super(music, links, outputDir, program);
	}
	
	public void add(Music music, Links links, Album item) {
		fCurAlbum = item;
        getMainDiv().addElement(Web.addItem(music, links, fCurAlbum));
        fLastAlbum = fCurAlbum;
    }
	
	protected String getTitle() {
		return getTitle("Tracks");
	}
	
	protected boolean needNewDocument() {
		return (fLastAlbum == null) || (!fLinks.getPageFileName(fLastAlbum).equals(getCurrentLetter()));
	}

    protected boolean needNewSubsection() {
        return false;
    }

    protected Element getSubsectionTitle() {
        a an = new a(); // named target
        an.setName(fCurAlbum.getId());
        an.addElement("t", fCurAlbum.getTitle());
        return an;
    }

	protected String getLastPath() {
		return fLinks.getPagePath(fLastAlbum);
	}
	
	protected String getCurrentLetter() {
        return fLinks.getPageFileName(fCurAlbum);
    }

	protected Element addIndexNavigator() {
		return Web.addAlbumIndexNavigator(fMusic, fLinks, getCurrentLetter());
	}
}

public class Web {
	
	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.music.web.web");
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: Web [source.xml] [settings.xml] [output.dir]");
			System.exit(0);
		}
        
        Web.initializeSettings(args[1]);
		
		Web.generate(args[0], args[2]);
	}
    
    private static void initializeSettings(String settingsFile) {
        Settings settings = com.bolsinga.web.util.Util.createSettings(settingsFile);

		System.setProperty("web.ico", settings.getIco());
        com.bolsinga.settings.data.Image image = settings.getLogoImage();
		System.setProperty("web.logo.url", image.getLocation());
		System.setProperty("web.logo.width", image.getWidth().toString());
		System.setProperty("web.logo.height", image.getHeight().toString());
		System.setProperty("web.logo.alt", image.getAlt());
		System.setProperty("web.layout.css", settings.getCssFile());
		System.setProperty("music.contact", settings.getContact());
		System.setProperty("rss.url", settings.getRssFile());
		System.setProperty("music.ical.url", settings.getIcalName() + ".ics");
        image = settings.getIcalImage();
		System.setProperty("ical.image.url", image.getLocation());
		System.setProperty("ical.image.width", image.getWidth().toString());
		System.setProperty("ical.image.height", image.getHeight().toString());
		System.setProperty("ical.image.alt", image.getAlt());
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
	
	public static Element addArtistIndexNavigator(Music music, Links links, String curLetter) {
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
			if (s.equals(curLetter)) {
				list.addElement(new li(s));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s)));
			}
		}
		
		d.addElement(list);
		
		return d;
	}
	
	public static Element addVenueIndexNavigator(Music music, Links links, String curLetter) {
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
			if (v.equals(curLetter)) {
				list.addElement(new li(v));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(v), v)));
			}
		}
		
		d.addElement(list);
		
		return d;
	}

	public static Element addAlbumIndexNavigator(Music music, Links links, String curLetter) {
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
			if (s.equals(curLetter)) {
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
	
	public static Element addShowIndexNavigator(Music music, Links links, String curLetter) {
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
			if (s.equals(curLetter)) {
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
