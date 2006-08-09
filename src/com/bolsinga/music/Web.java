package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

abstract class MusicDocumentCreator extends com.bolsinga.web.MultiDocumentCreator {
  protected final GregorianCalendar fTimeStamp;
  protected final Lookup fLookup;
  protected final Links fLinks;
  private final String  fProgram;
    
  protected MusicDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program) {
    super(backgrounder, outputDir);
    fTimeStamp = timeStamp;
    fLookup = lookup;
    fLinks = links;
    fProgram = program;
  }

  protected XhtmlDocument createDocument() {
    return Web.createHTMLDocument(fLinks, getTitle());
  }

  protected div getHeaderDiv() {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MUSIC_HEADER);
    d.addElement(new h1().addElement(getTitle()));
    d.addElement(com.bolsinga.web.Util.getLogo());
    d.addElement(fLinks.addWebNavigator(fTimeStamp, fProgram));
    d.addElement(addIndexNavigator());
    return d;
  }
}

abstract class SingleSectionMusicDocumentCreator extends com.bolsinga.web.DocumentCreator {
  protected final GregorianCalendar fTimeStamp;
  protected final Lookup fLookup;
  protected final Links fLinks;
  private final String  fProgram;

  protected SingleSectionMusicDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program) {
    super(backgrounder, outputDir);
    fTimeStamp = timeStamp;
    fLookup = lookup;
    fLinks = links;
    fProgram = program;
  }

  protected XhtmlDocument createDocument() {
    return Web.createHTMLDocument(fLinks, getTitle());
  }

  protected div getHeaderDiv() {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MUSIC_HEADER);
    d.addElement(new h1().addElement(getTitle()));
    d.addElement(com.bolsinga.web.Util.getLogo());
    d.addElement(fLinks.addWebNavigator(fTimeStamp, fProgram));
    d.addElement(addIndexNavigator());
    return d;
  }
}

class ArtistDocumentCreator extends MusicDocumentCreator {
  private final List<Artist> fArtists;

  // These change during the life-cycle of this object
  private Artist fLastArtist = null;
  private Artist fCurArtist  = null;
          
  public ArtistDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final List<Artist> artists, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program) {
    super(backgrounder, lookup, links, timeStamp, outputDir, program);
    fArtists = artists;
  }

  public void add(final Artist item) {
    fCurArtist = item;
    add();
    fLastArtist = fCurArtist;
  }
        
  protected String getTitle() {
    return getTitle(com.bolsinga.web.Util.getResourceString("artists"));
  }
        
  protected boolean needNewDocument() {
    return ((fLastArtist == null) || !fLinks.getPageFileName(fLastArtist).equals(getCurrentLetter()));
  }
        
  protected boolean needNewSubsection() {
    return ((fLastArtist == null) || !fLastArtist.getName().equals(fCurArtist.getName()));
  }

  protected Element getSubsectionTitle() {
    return com.bolsinga.web.Util.createNamedTarget(fCurArtist.getId(), fCurArtist.getName());
  }

  protected String getLastPath() {
    return fLinks.getPagePath(fLastArtist);
  }
        
  protected String getCurrentLetter() {
    return fLinks.getPageFileName(fCurArtist);
  }

  protected Element getCurrentElement() {
    return Web.addItem(fLookup, fLinks, fCurArtist);
  }
    
  protected Element addIndexNavigator() {
    return Web.addArtistIndexNavigator(fArtists, fLinks, getCurrentLetter());
  }
}

class VenueDocumentCreator extends MusicDocumentCreator {
  private final List<Venue> fVenues;

  // These change during the life-cycle of this object
  private Venue fLastVenue = null;
  private Venue fCurVenue  = null;
    
  public VenueDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final List<Venue> venues, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program) {
    super(backgrounder, lookup, links, timeStamp, outputDir, program);
    fVenues = venues;
  }

  public void add(final Venue item) {
    fCurVenue = item;
    add();
    fLastVenue = fCurVenue;
  }
        
  protected String getTitle() {
    return getTitle(com.bolsinga.web.Util.getResourceString("venues"));
  }
        
  protected boolean needNewDocument() {
    return (fLastVenue == null) || (!fLinks.getPageFileName(fLastVenue).equals(getCurrentLetter()));
  }
        
  protected boolean needNewSubsection() {
    return (fLastVenue == null) || (!fLastVenue.getName().equals(fCurVenue.getName()));
  }

  protected Element getSubsectionTitle() {
    return com.bolsinga.web.Util.createNamedTarget(fCurVenue.getId(), fCurVenue.getName());
  }

  protected String getLastPath() {
    return fLinks.getPagePath(fLastVenue);
  }

  protected String getCurrentLetter() {
    return fLinks.getPageFileName(fCurVenue);
  }

  protected Element getCurrentElement() {
    return Web.addItem(fLookup, fLinks, fCurVenue);
  }
        
  protected Element addIndexNavigator() {
    return Web.addVenueIndexNavigator(fVenues, fLinks, getCurrentLetter());
  }
}

class ShowDocumentCreator extends MusicDocumentCreator {
  private final com.bolsinga.web.Encode fEncoder;
  private final List<Show> fShows;

  // These change during the life-cycle of this object
  private Show fLastShow = null;
  private Show fCurShow  = null;

  public ShowDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final List<Show> shows, final Lookup lookup, final com.bolsinga.web.Encode encoder, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program) {
    super(backgrounder, lookup, links, timeStamp, outputDir, program);
    fEncoder = encoder;
    fShows = shows;
  }
        
  public void add(final Show item) {
    fCurShow = item;
    add();
    fLastShow = fCurShow;
  }
        
  protected String getTitle() {
    return getTitle(com.bolsinga.web.Util.getResourceString("dates"));
  }
    
  protected boolean needNewDocument() {
    return (fLastShow == null) || (!fLinks.getPageFileName(fLastShow).equals(getCurrentLetter()));
  }
        
  protected boolean needNewSubsection() {
    return (fLastShow == null) || (!Util.toMonth(fLastShow.getDate()).equals(Util.toMonth(fCurShow.getDate())));
  }
    
  protected Element getSubsectionTitle() {
    String m = Util.toMonth(fCurShow.getDate());
    return com.bolsinga.web.Util.createNamedTarget(m, m);
  }

  protected String getLastPath() {
    return fLinks.getPagePath(fLastShow);
  }
        
  protected String getCurrentLetter() {
    return fLinks.getPageFileName(fCurShow);
  }

  protected Element getCurrentElement() {
    return Web.addItem(fEncoder, fLinks, fCurShow);
  }

  protected Element addIndexNavigator() {
    return Web.addShowIndexNavigator(fShows, fLinks, getCurrentLetter());
  }
}

class StatisticsCreator extends SingleSectionMusicDocumentCreator {
  private final String fFileName;
  private final String fTitle;
  private final String fDirectory;

  // This changes during the life-cycle of this object
  private table  fCurTable  = null;

  public StatisticsCreator(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program, final String filename, final String title, final String directory) {
    super(backgrounder, lookup, links, timeStamp, outputDir, program);
    fFileName = filename;
    fTitle = title;
    fDirectory = directory;
  }

  public void add(final table t) {
    fCurTable = t;
    add();
  }
        
  protected String getTitle() {
    return fTitle;
  }

  protected boolean needNewDocument() {
    return true;
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
    
  protected Element getCurrentElement() {
    return fCurTable;
  }
        
  protected Element addIndexNavigator() {
    return null;
  }
}

class TracksStatisticsCreator extends StatisticsCreator {
  private final boolean fTracksStats;
        
  public static TracksStatisticsCreator createTracksStats(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program, final String title, final String directory) {
    return new TracksStatisticsCreator(backgrounder, lookup, links, timeStamp, outputDir, program, Links.STATS, true, title, directory);
  }

  public static TracksStatisticsCreator createAlbumStats(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program, final String title, final String directory) {
    return new TracksStatisticsCreator(backgrounder, lookup, links, timeStamp, outputDir, program, Links.ALBUM_STATS, false, title, directory);
  }

  private TracksStatisticsCreator(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program, final String filename, final boolean isTracksStats, final String title, final String directory) {
    super(backgrounder, lookup, links, timeStamp, outputDir, program, filename, title, directory);
    fTracksStats = isTracksStats;
  }

  protected Element addIndexNavigator() {
    Vector<Element> e = new Vector<Element>();
    if (fTracksStats) {
      e.add(new StringElement(com.bolsinga.web.Util.getResourceString("tracks")));
      e.add(fLinks.getAlbumsLink());
    } else {
      e.add(fLinks.getTracksLink());
      e.add(new StringElement(com.bolsinga.web.Util.getResourceString("albums")));
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.TRACKS_MENU);
    d.addElement(new h4(com.bolsinga.web.Util.getResourceString("view")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}

class TracksDocumentCreator extends SingleSectionMusicDocumentCreator {
  private final List<Album> fAlbums;

  // These change during the life-cycle of this object
  private Album fLastAlbum = null;
  private Album fCurAlbum  = null;
          
  public TracksDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final List<Album> albums, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir, final String program) {
    super(backgrounder, lookup, links, timeStamp, outputDir, program);
    fAlbums = albums;
  }
        
  public void add(final Album item) {
    fCurAlbum = item;
    add();
    fLastAlbum = fCurAlbum;
  }
        
  protected String getTitle() {
    return getTitle(com.bolsinga.web.Util.getResourceString("tracks"));
  }
        
  protected boolean needNewDocument() {
    return (fLastAlbum == null) || (!fLinks.getPageFileName(fLastAlbum).equals(getCurrentLetter()));
  }

  protected String getLastPath() {
    return fLinks.getPagePath(fLastAlbum);
  }
        
  protected String getCurrentLetter() {
    return fLinks.getPageFileName(fCurAlbum);
  }

  protected Element getCurrentElement() {
    return Web.addItem(fLinks, fCurAlbum);
  }

  protected Element addIndexNavigator() {
    return Web.addAlbumIndexNavigator(fAlbums, fLinks, getCurrentLetter());
  }
}

public class Web implements com.bolsinga.web.Backgroundable {

  private static final boolean GENERATE_XML = false;
       
  // This is the first year of this data.
  private static final int START_YEAR = 2003;

  final com.bolsinga.web.Backgrounder fBackgrounder;
   
  public static void main(String[] args) {
    if ((args.length != 4) && (args.length != 5)) {
      Web.usage();
    }

    String type = args[0];

    String settings = null;
    String output = null;

    Music music = null;

    if (type.equals("xml")) {
      if (args.length != 4) {
        Web.usage();
      }
      
      String musicFile = args[1];
      settings = args[2];
      output = args[3];

      music = Util.createMusic(musicFile);
    } else if (type.equals("db")) {
      if (args.length != 5) {
        Web.usage();
      }

      String user = args[1];
      String password = args[2];
      settings = args[3];
      output = args[4];

      music = com.bolsinga.music.Util.createMusic(user, password);
    } else {
      Web.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);

    if (Web.GENERATE_XML) {
      Web.export(music);
      System.exit(0);
    }

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(backgrounder, music, null);
    Web web = new Web(backgrounder);
    web.generate(music, encoder, output);
    web.complete();
  }
  
  Web(final com.bolsinga.web.Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);
  }
  
  void complete() {
    fBackgrounder.removeInterest(this);
  }
  
  private static void usage() {
    System.out.println("Usage: Web xml [source.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }
        
  private static void export(final Music music) {
    com.bolsinga.music.Compare.tidy(music);
    try {
      File outputFile = new File("/tmp", "music_db.xml");

      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      OutputStream os = null;
      try {
        os = new FileOutputStream(outputFile);
      } catch (IOException ioe) {
        System.err.println(ioe);
        ioe.printStackTrace();
        System.exit(1);
      }
      m.marshal(music, os);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void generate(final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, music, encoder, outputDir);
  }
        
  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    Lookup lookup = Lookup.getLookup(music);
    Links links = Links.getLinks(true);
    GregorianCalendar timeStamp = music.getTimestamp().toGregorianCalendar();
                
    generateArtistPages(backgrounder, music, lookup, links, timeStamp, outputDir);
                
    generateVenuePages(backgrounder, music, lookup, links, timeStamp, outputDir);
                
    generateDatePages(backgrounder, music, encoder, lookup, links, timeStamp, outputDir);
                
    generateCityPages(backgrounder, music, lookup, links, timeStamp, outputDir);
                
    generateTracksPages(backgrounder, music, lookup, links, timeStamp, outputDir);
  }

  // NOTE: Instead of a List of ID's, JAXB returns a List of real items.
        
  public static void generateArtistPages(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir) {
    Collections.sort(music.getArtist(), com.bolsinga.music.Compare.ARTIST_COMPARATOR);
    List<Artist> items = Collections.unmodifiableList(music.getArtist());
    
    ArtistDocumentCreator creator = new ArtistDocumentCreator(backgrounder, items, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"));
    for (Artist item : items) {
      creator.add(item);
    }
    creator.complete();
                
    Collections.sort(music.getArtist(), com.bolsinga.music.Compare.getCompare(music).ARTIST_STATS_COMPARATOR);
    items = Collections.unmodifiableList(music.getArtist());

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Artist item : items) {
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), item.getName()).toString();
      Collection<Show> shows = lookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(backgrounder, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.ARTIST_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.complete();
  }
        
  public static void generateVenuePages(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir) {
    Collections.sort(music.getVenue(), com.bolsinga.music.Compare.VENUE_COMPARATOR);
    List<Venue> items = Collections.unmodifiableList(music.getVenue());

    VenueDocumentCreator creator = new VenueDocumentCreator(backgrounder, items, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"));
    for (Venue item : items) {
      creator.add(item);
    }
    creator.complete();

    Collections.sort(music.getVenue(), com.bolsinga.music.Compare.getCompare(music).VENUE_STATS_COMPARATOR);
    items = Collections.unmodifiableList(music.getVenue());

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Venue item : items) {
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), item.getName()).toString();
      Collection<Show> shows = lookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("venue");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(backgrounder, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.VENUE_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.complete();
  }
        
  public static void generateDatePages(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final com.bolsinga.web.Encode encoder, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir) {
    Collections.sort(music.getShow(), com.bolsinga.music.Compare.SHOW_COMPARATOR);
    List<Show> items = Collections.unmodifiableList(music.getShow());

    Collection<Show> showCollection = null;
    TreeMap<Show, Collection<Show>> dates = new TreeMap<Show, Collection<Show>>(com.bolsinga.music.Compare.SHOW_STATS_COMPARATOR);
                
    ShowDocumentCreator creator = new ShowDocumentCreator(backgrounder, items, lookup, encoder, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"));
    for (Show item : items) {
      if (dates.containsKey(item)) {
        showCollection = dates.get(item);
        showCollection.add(item);
      } else {
        showCollection = new Vector<Show>();
        showCollection.add(item);
        dates.put(item, showCollection);
      }
                        
      creator.add(item);
    }
    creator.complete();

    int index = 0;
    String[] names = new String[dates.size()];
    int[] values = new int[dates.size()];

    for (Show item : dates.keySet()) {
      BigInteger year = item.getDate().getYear();
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkToPage(item), (year != null) ? year.toString() : "Unknown").toString();
      values[index] = dates.get(item).size();
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("year");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(backgrounder, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.SHOW_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.complete();
  }
        
  public static void generateCityPages(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir) {
    Collection<String> items = lookup.getCities();
    HashMap<Integer, Collection<String>> cityCount = new HashMap<Integer, Collection<String>>();
    String city = null;
    int val;
    Collection<String> stringCollection = null;

    for (String item : items) {
      val = lookup.getShows(item).size();
      if (cityCount.containsKey(val)) {
        stringCollection = cityCount.get(val);
        stringCollection.add(item);
      } else {
        stringCollection = new HashSet<String>();
        stringCollection.add(item);
        cityCount.put(val, stringCollection);
      }
    }
                
    List<Integer> keys = new Vector<Integer>(cityCount.keySet());
    Collections.sort(keys);
    Collections.reverse(keys);

    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    int index = 0;

    for (int value : keys) {
      List<String> k = new Vector<String>(cityCount.get(value));
      Collections.sort(k);

      for (String j : k) {
        names[index] = j;
        values[index] = value;
        index++;
      }
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("city");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator creator = new StatisticsCreator(backgrounder, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.CITIES_DIR);
    creator.add(makeTable(names, values, tableTitle, typeString));
    creator.complete();
  }

  public static void generateTracksPages(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final Links links, final GregorianCalendar timeStamp, final String outputDir) {
    Collections.sort(music.getAlbum(), com.bolsinga.music.Compare.ALBUM_COMPARATOR);
    List<Album> items = Collections.unmodifiableList(music.getAlbum());
                
    TracksDocumentCreator creator = new TracksDocumentCreator(backgrounder, items, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"));
    for (Album item : items) {                
      creator.add(item);
    }
    creator.complete();

    List<Artist> artists = music.getArtist();
    Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_TRACKS_COMPARATOR);

    int index = 0;
    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    
    for (Artist artist : artists) {
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), artist.getName()).toString();
      values[index] = Util.trackCount(artist);
                        
      index++;
    }
                
    {
      String typeString = com.bolsinga.web.Util.getResourceString("artist");
      Object typeArgs[] = { typeString };
      String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("tracksby"), typeArgs);
      Object statsArgs[] = { com.bolsinga.web.Util.getResourceString("track") };
      String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), statsArgs);

      StatisticsCreator stats = TracksStatisticsCreator.createTracksStats(backgrounder, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"), pageTitle, Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString));
      stats.complete();
    }

    artists = music.getArtist();
    Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_ALBUMS_COMPARATOR);

    names = new String[artists.size()];
    values = new int[artists.size()];
    index = 0;
    
    for(Artist artist : artists) {
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), artist.getName()).toString();
      values[index] = (artist.getAlbum() != null) ? artist.getAlbum().size() : 0;
                        
      index++;
    }

    {
      String typeString = com.bolsinga.web.Util.getResourceString("artist");
      Object typeArgs[] = { typeString };
      String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("albumsby"), typeArgs);
      Object statsArgs[] = { com.bolsinga.web.Util.getResourceString("album") };
      String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), statsArgs);

      StatisticsCreator stats = TracksStatisticsCreator.createAlbumStats(backgrounder, lookup, links, timeStamp, outputDir, com.bolsinga.web.Util.getResourceString("program"), pageTitle, Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString));
      stats.complete();
    }
  }
        
  public static Element generatePreview(final String sourceFile, final int lastShowsCount) {
    Music music = Util.createMusic(sourceFile);
    return generatePreview(music, lastShowsCount);
  }
        
  public static Element generatePreview(final Music music, final int lastShowsCount) {
    Links links = Links.getLinks(false);
                
    Vector<Element> e = new Vector<Element>();
                
    StringBuffer sb = new StringBuffer();
    sb.append(music.getArtist().size());
    sb.append(" ");
    sb.append(links.getArtistLink());
    e.add(new StringElement(sb.toString()));

    sb = new StringBuffer();
    sb.append(music.getShow().size());
    sb.append(" ");
    sb.append(links.getShowLink());
    e.add(new StringElement(sb.toString()));

    sb = new StringBuffer();
    sb.append(music.getVenue().size());
    sb.append(" ");
    sb.append(links.getVenueLink());
    e.add(new StringElement(sb.toString()));

    sb = new StringBuffer();
    sb.append(Lookup.getLookup(music).getCities().size());
    sb.append(" ");
    sb.append(links.getCityLink());
    e.add(new StringElement(sb.toString()));

    sb = new StringBuffer();
    sb.append(music.getSong().size());
    sb.append(" ");
    sb.append(links.getTracksLink());
    e.add(new StringElement(sb.toString()));

    sb = new StringBuffer();
    sb.append(music.getAlbum().size());
    sb.append(" ");
    sb.append(links.getAlbumsLink());
    e.add(new StringElement(sb.toString()));

    div dm = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.PREVIEW_MENU);
    dm.addElement(com.bolsinga.web.Util.getLogo());
    Object[] genArgs = { music.getTimestamp().toGregorianCalendar().getTime() };
    dm.addElement(new h3(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generated"), genArgs)));
    dm.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    dm.addElement(links.getICalLink());
                
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.PREVIEW_MAIN);
    d.addElement(dm);
                
    div dr = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.PREVIEW_RECENT);
                
    Object[] countArgs = { new Integer(lastShowsCount) };
    dr.addElement(new h3(MessageFormat.format(com.bolsinga.web.Util.getResourceString("lastshows"), countArgs)));

    Collections.sort(music.getShow(), com.bolsinga.music.Compare.SHOW_COMPARATOR);
    Collections.reverse(music.getShow());

    List<Show> items = Collections.unmodifiableList(music.getShow());
    
    for (int i = 0; i < lastShowsCount; i++) {
      Show item = items.get(i);

      div ds = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.PREVIEW_SHOW);
      ds.addElement(new h4(com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), Util.toString(item.getDate()))));
      ds.addElement(getShowListing(links, item));
                        
      dr.addElement(ds);
    }

    d.addElement(dr);
                
    return d;
  }
        
  public static String getLinkedData(final com.bolsinga.web.Encode encoder, final Show show, final boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(show, upOneLevel));
  }
        
  public static Element addItem(final Lookup lookup, final Links links, final Artist artist) {
    // CSS.ARTIST_ITEM
    Vector<Element> e = new Vector<Element>();

    if (artist.getAlbum().size() > 0) {
      e.add(Web.addTracks(links, artist));
    }
                
    if (lookup.getRelations(artist) != null) {
      e.add(Web.addRelations(lookup, links, artist));
    }

    Collection<Show> shows = lookup.getShows(artist);
    if (shows != null) {
      for (Show show : shows) {
        Vector<Element> se = new Vector<Element>();
        StringBuffer sb = new StringBuffer();
        Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
        while (bi.hasNext()) {
          Artist performer = (Artist)bi.next().getValue();
                                    
          if (artist.equals(performer)) {
            sb.append(performer.getName());
          } else {
            sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
          }
                                    
          if (bi.hasNext()) {
            sb.append(", ");
          }
        }
        se.add(new StringElement(sb.toString()));
                            
        Venue venue = (Venue)show.getVenue();
        a venueA = com.bolsinga.web.Util.createInternalA(links.getLinkTo(venue), venue.getName());
        Location l = (Location)venue.getLocation();
        se.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                            
        String showLink = links.getLinkTo(show);
                            
        String comment = show.getComment();
        if (comment != null) {
          se.add(com.bolsinga.web.Util.createInternalA(showLink, com.bolsinga.web.Util.getResourceString("showsummary")));
        }
                            
        div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ARTIST_SHOW);
        d.addElement(new h3().addElement(com.bolsinga.web.Util.createInternalA(showLink, Util.toString(show.getDate()))));
        d.addElement(com.bolsinga.web.Util.createUnorderedList(se));
        e.add(d);
      }
    }
                
    return com.bolsinga.web.Util.createUnorderedList(e);
  }
        
  public static Element addItem(final Lookup lookup, final Links links, final Venue venue) {
    // CSS.VENUE_ITEM
    Vector<Element> e = new Vector<Element>();
                
    if (lookup.getRelations(venue) != null) {
      e.add(Web.addRelations(lookup, links, venue));
    }

    Collection<Show> shows = lookup.getShows(venue);
    if (shows != null) {
      for (Show show : shows) {
        String showLink = links.getLinkTo(show);
        
        Vector<Element> se = new Vector<Element>();
        StringBuffer sb = new StringBuffer();
        Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
        while (bi.hasNext()) {
          Artist performer = (Artist)bi.next().getValue();
          sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
          
          if (bi.hasNext()) {
            sb.append(", ");
          }
        }
        se.add(new StringElement(sb.toString()));
        
        Location l = (Location)venue.getLocation();
        se.add(new StringElement(venue.getName() + ", " + l.getCity() + ", " + l.getState()));
        
        String comment = show.getComment();
        if (comment != null) {
          se.add(com.bolsinga.web.Util.createInternalA(showLink, com.bolsinga.web.Util.getResourceString("showsummary")));
        }
        
        div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.VENUE_SHOW);
        d.addElement(new h3().addElement(com.bolsinga.web.Util.createInternalA(showLink, Util.toString(show.getDate()))));
        d.addElement(com.bolsinga.web.Util.createUnorderedList(se));
        e.add(d);                        
      }
    }
                
    return com.bolsinga.web.Util.createUnorderedList(e);
  }
        
  private static ul getShowListing(final Links links, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuffer sb = new StringBuffer();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
                        
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = (Venue)show.getVenue();
    a venueA = com.bolsinga.web.Util.createInternalA(links.getLinkTo(venue), venue.getName());
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                
    return com.bolsinga.web.Util.createUnorderedList(e);
  }

  public static Element addItem(final com.bolsinga.web.Encode encoder, final Show show) {
    Links links = Links.getLinks(false);

    return Web.addItem(encoder, links, show);
  }

  public static Element addItem(final com.bolsinga.web.Encode encoder, final Links links, final Show show) {
    // CSS.SHOW_ITEM
    Vector<Element> e = new Vector<Element>();

    e.add(new h3().addElement(com.bolsinga.web.Util.createNamedTarget(show.getId(), Util.toString(show.getDate()))));

    e.add(Web.getShowListing(links, show));

    String comment = show.getComment();
    if (comment != null) {
      e.add(com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.SHOW_COMMENT).addElement(getLinkedData(encoder, show, true)));
    }
                
    return com.bolsinga.web.Util.createUnorderedList(e);
  }

  public static Element addItem(final Links links, final Album album) {
    // CSS.TRACKS_ITEM
    Vector<Element> e = new Vector<Element>();
                
    StringBuffer sb;
    Artist artist = null;
    Song song;
                
    boolean isCompilation = com.bolsinga.web.Util.convert(album.isCompilation());
                
    sb = new StringBuffer();
    sb.append(com.bolsinga.web.Util.createNamedTarget(album.getId(), album.getTitle()));
    if (!isCompilation) {
      artist = (Artist)album.getPerformer();
      sb.append(" - ");
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), artist.getName()));
    }
    com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
    if (albumRelease != null) {
      sb.append(" (");
      sb.append(albumRelease.getYear());
      sb.append(")");
    }

    e.add(new h2().addElement(sb.toString()));

    Vector<Element> ae = new Vector<Element>();
    for (JAXBElement<Object> jsong : album.getSong()) {
      song = (Song)jsong.getValue();
      sb = new StringBuffer();
      if (isCompilation) {
        artist = (Artist)song.getPerformer();
        sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), artist.getName()));
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
      ae.add(new StringElement(sb.toString()));
    }
    e.add(com.bolsinga.web.Util.createOrderedList(ae));

    return com.bolsinga.web.Util.createUnorderedList(e);
  }
        
  public static div addRelations(final Lookup lookup, final Links links, final Artist artist) {
    Vector<Element> e = new Vector<Element>();
    for (Artist art : lookup.getRelations(artist)) {
      if (art.equals(artist)) {
        e.add(new StringElement(art.getName()));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(links.getLinkTo(art), art.getName()));
      }
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ARTIST_RELATION);
    d.addElement(new h3().addElement(com.bolsinga.web.Util.getResourceString("seealso")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  public static div addRelations(final Lookup lookup, final Links links, final Venue venue) {
    Vector<Element> e = new Vector<Element>();
    for (Venue v : lookup.getRelations(venue)) {
      if (v.equals(venue)) {
        e.add(new StringElement(v.getName()));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(links.getLinkTo(v), v.getName()));
      }
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.VENUE_RELATION);
    d.addElement(new h3().addElement(com.bolsinga.web.Util.getResourceString("seealso")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }

  public static div addTracks(final Links links, final Artist artist) {
    Vector<Element> e = new Vector<Element>();

    List<JAXBElement<Object>> albums = artist.getAlbum();
    Collections.sort(albums, com.bolsinga.music.Compare.JAXB_ALBUM_ORDER_COMPARATOR);

    for (JAXBElement<Object> jalbum : albums) {
      Album album = (Album)jalbum.getValue();
      StringBuffer sb = new StringBuffer();
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(album), album.getTitle()));
      com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
      if (albumRelease != null) {
        sb.append(" (");
        sb.append(albumRelease.getYear());
        sb.append(")");
      }
      e.add(new StringElement(sb.toString()));
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ARTIST_TRACKS);
    d.addElement(new h3().addElement(com.bolsinga.web.Util.getResourceString("albums")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  public static Element addArtistIndexNavigator(final List<Artist> artists, final Links links, final String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();

    for (Artist art : artists) {
      String letter = links.getPageFileName(art);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(art));
      }
    }

    Vector<Element> e = new Vector<Element>();
    for (String s : m.keySet()) {
      if (s.equals(curLetter)) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(m.get(s), s));
      }
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ARTIST_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  public static Element addVenueIndexNavigator(final List<Venue> venues, final Links links, final String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();
    for (Venue v : venues) {
      String letter = links.getPageFileName(v);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(v));
      }
    }

    Vector<Element> e = new Vector<Element>();
    for (String v : m.keySet()) {
      if (v.equals(curLetter)) {
        e.add(new StringElement(v));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(m.get(v), v));
      }
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.VENUE_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }

  public static Element addAlbumIndexNavigator(final List<Album> items, final Links links, final String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();
    for (Album alb : items) {
      String letter = links.getPageFileName(alb);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(alb));
      }
    }

    Vector<Element> e = new Vector<Element>();
    for (String s : m.keySet()) {
      if (s.equals(curLetter)) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(m.get(s), s));
      }
    }

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.TRACKS_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  public static table makeTable(final String[] names, final int[] values, final String caption, final String header) {
    table t = new table();
    t.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
    caption capt = new caption();
    capt.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
    capt.addElement(caption);
    t.addElement(capt);
    tr trow = new tr().addElement(new th(header)).addElement(new th("#")).addElement(new th("%"));
    trow.setClass(com.bolsinga.web.CSS.TABLE_HEADER);
    trow.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
    t.addElement(trow);
    th thh = null;
                
    int total = 0;
    int i;
    for (i = 0; i < values.length; i++) {
      total += values[i];
    }

    for (i = 0; i < values.length; i++) {
      trow = new tr();
      trow.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
      trow.setClass((((i + 1) % 2) != 0) ? com.bolsinga.web.CSS.TABLE_ROW : com.bolsinga.web.CSS.TABLE_ROW_ALT);
      thh = new th(names[i]);
      thh.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
      trow.addElement(thh);
      trow.addElement(new td(Integer.toString(values[i])).setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint()));
      trow.addElement(new td(Util.toString((double)values[i] / total * 100.0)).setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint()));
                        
      t.addElement(trow);
    }
                
    trow = new tr();
    trow.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
    trow.setClass(com.bolsinga.web.CSS.TABLE_FOOTER);
    trow.addElement(new th(Integer.toString(names.length)));
    trow.addElement(new th(Integer.toString(total)));
    trow.addElement(new th());
    t.addElement(trow);
                
    return t;
  }
        
  public static Element addShowIndexNavigator(final List<Show> shows, final Links links, final String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();
    for (Show s : shows) {
      String letter = links.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(s));
      }
    }

    Vector<Element> e = new Vector<Element>();
    for (String s : m.keySet()) {
      if (s.equals(curLetter)) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(m.get(s), s));
      }
    }
    e.add(links.getICalLink());

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.SHOW_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  static XhtmlDocument createHTMLDocument(final Links links, final String title) {
    XhtmlDocument d = new XhtmlDocument(ECSDefaults.getDefaultCodeset());

    d.getHtml().setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
                
    d.setDoctype(new org.apache.ecs.Doctype.XHtml10Strict());
    d.appendTitle(title);
                
    head h = d.getHead();
    h.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
    h.addElement(com.bolsinga.web.Util.getIconLink());
    h.addElement(links.getLinkToStyleSheet());

    h.addElement(new meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
    h.addElement(new meta().setContent(System.getProperty("user.name")).setName("Author"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.nowUTC().getTime().toString()).setName("Date"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.getGenerator()).setName("Generator"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.getCopyright(START_YEAR)).setName("Copyright"));

    d.getBody().setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());

    return d;
  }
}
