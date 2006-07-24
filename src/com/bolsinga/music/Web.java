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
  Music  fMusic   = null;
  Links  fLinks   = null;
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
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MUSIC_HEADER);
    d.addElement(new h1().addElement(getTitle()));
    d.addElement(com.bolsinga.web.Util.getLogo());
    d.addElement(fLinks.addWebNavigator(fMusic, fProgram));
    d.addElement(addIndexNavigator());
    return d;
  }
}

abstract class SingleSectionMusicDocumentCreator extends com.bolsinga.web.DocumentCreator {
  Music  fMusic   = null;
  Links  fLinks   = null;
  String fProgram = null;

  protected SingleSectionMusicDocumentCreator(Music music, Links links, String outputDir, String program) {
    super(outputDir);
    fMusic = music;
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
    d.addElement(fLinks.addWebNavigator(fMusic, fProgram));
    d.addElement(addIndexNavigator());
    return d;
  }
}

class ArtistDocumentCreator extends MusicDocumentCreator {
  Artist fLastArtist = null;
  Artist fCurArtist  = null;
        
  public ArtistDocumentCreator(Music music, Links links, String outputDir, String program) {
    super(music, links, outputDir, program);
  }

  public void add(Artist item) {
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
    return Web.addItem(fMusic, fLinks, fCurArtist);
  }
    
  protected Element addIndexNavigator() {
    return Web.addArtistIndexNavigator(fMusic, fLinks, getCurrentLetter());
  }
}

class VenueDocumentCreator extends MusicDocumentCreator {
  Venue fLastVenue = null;
  Venue fCurVenue  = null;
        
  public VenueDocumentCreator(Music music, Links links, String outputDir, String program) {
    super(music, links, outputDir, program);
  }

  public void add(Venue item) {
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
    return Web.addItem(fMusic, fLinks, fCurVenue);
  }
        
  protected Element addIndexNavigator() {
    return Web.addVenueIndexNavigator(fMusic, fLinks, getCurrentLetter());
  }
}

class ShowDocumentCreator extends MusicDocumentCreator {
  Show fLastShow = null;
  Show fCurShow  = null;
  com.bolsinga.web.Encode fEncoder = null;
   
  public ShowDocumentCreator(Music music, com.bolsinga.web.Encode encoder, Links links, String outputDir, String program) {
    super(music, links, outputDir, program);
    fEncoder = encoder;
  }
        
  public void add(Show item) {
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
    return Web.addShowIndexNavigator(fMusic, fLinks, getCurrentLetter());
  }
}

class StatisticsCreator extends SingleSectionMusicDocumentCreator {
  String fFileName  = null;
  String fTitle     = null;
  String fDirectory = null;
  table  fCurTable  = null;

  public StatisticsCreator(Music music, Links links, String outputDir, String program, String filename, String title, String directory) {
    super(music, links, outputDir, program);
    fFileName = filename;
    fTitle = title;
    fDirectory = directory;
  }

  public void add(table t) {
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
  private boolean fTracksStats;
        
  public static TracksStatisticsCreator createTracksStats(Music music, Links links, String outputDir, String program, String title, String directory) {
    return new TracksStatisticsCreator(music, links, outputDir, program, Links.STATS, true, title, directory);
  }

  public static TracksStatisticsCreator createAlbumStats(Music music, Links links, String outputDir, String program, String title, String directory) {
    return new TracksStatisticsCreator(music, links, outputDir, program, Links.ALBUM_STATS, false, title, directory);
  }

  private TracksStatisticsCreator(Music music, Links links, String outputDir, String program, String filename, boolean isTracksStats, String title, String directory) {
    super(music, links, outputDir, program, filename, title, directory);
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
  Album fLastAlbum = null;
  Album fCurAlbum  = null;
        
  public TracksDocumentCreator(Music music, Links links, String outputDir, String program) {
    super(music, links, outputDir, program);
  }
        
  public void add(Album item) {
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
    return Web.addAlbumIndexNavigator(fMusic, fLinks, getCurrentLetter());
  }
}

public class Web {

  private static final boolean GENERATE_XML = false;
       
  // This is the first year of this data.
  private static int START_YEAR = 2003;
 
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

    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, null);
                
    Web.generate(music, encoder, output);
  }
  
  private static void usage() {
    System.out.println("Usage: Web xml [source.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }
        
  private static void export(Music music) {
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

  public static void generate(String sourceFile, String outputDir) {
    Music music = Util.createMusic(sourceFile);
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, null);
    generate(music, encoder, outputDir);
  }
        
  public static void generate(Music music, com.bolsinga.web.Encode encoder, String outputDir) {
    Links links = Links.getLinks(true);
                
    generateArtistPages(music, links, outputDir);
                
    generateVenuePages(music, links, outputDir);
                
    generateDatePages(music, encoder, links, outputDir);
                
    generateCityPages(music, links, outputDir);
                
    generateTracksPages(music, links, outputDir);
  }

  // NOTE: Instead of a List of ID's, JAXB returns a List of real items.
        
  public static void generateArtistPages(Music music, Links links, String outputDir) {
    List<Artist> items = music.getArtist();
    int index = 0;
                
    Collections.sort(items, com.bolsinga.music.Compare.ARTIST_COMPARATOR);
                
    ArtistDocumentCreator creator = new ArtistDocumentCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"));

    for (Artist item : items) {
      creator.add(item);
    }
    creator.close();
                
    Collections.sort(items, com.bolsinga.music.Compare.getCompare(music).ARTIST_STATS_COMPARATOR);

    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Artist item : items) {
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), item.getName()).toString();
      Collection<Show> shows = Lookup.getLookup(music).getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.ARTIST_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.close();
  }
        
  public static void generateVenuePages(Music music, Links links, String outputDir) {
    List<Venue> items = music.getVenue();
    int index = 0;
                
    Collections.sort(items, com.bolsinga.music.Compare.VENUE_COMPARATOR);

    VenueDocumentCreator creator = new VenueDocumentCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"));

    for (Venue item : items) {
      creator.add(item);
    }
    creator.close();

    Collections.sort(items, com.bolsinga.music.Compare.getCompare(music).VENUE_STATS_COMPARATOR);

    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Venue item : items) {
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), item.getName()).toString();
      Collection<Show> shows = Lookup.getLookup(music).getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("venue");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.VENUE_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.close();
  }
        
  public static void generateDatePages(Music music, com.bolsinga.web.Encode encoder, Links links, String outputDir) {
    List<Show> items = music.getShow();
    Collection<Show> showCollection = null;
    TreeMap<Show, Collection<Show>> dates = new TreeMap<Show, Collection<Show>>(com.bolsinga.music.Compare.SHOW_STATS_COMPARATOR);
                
    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);

    ShowDocumentCreator creator = new ShowDocumentCreator(music, encoder, links, outputDir, com.bolsinga.web.Util.getResourceString("program"));

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
    creator.close();

    String[] names = new String[dates.size()];
    int[] values = new int[dates.size()];
    int index = 0;

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

    StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.SHOW_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.close();
  }
        
  public static void generateCityPages(Music music, Links links, String outputDir) {
    Collection<String> items = Lookup.getLookup(music).getCities();
    HashMap<Integer, Collection<String>> cityCount = new HashMap<Integer, Collection<String>>();
    String city = null;
    int val;
    Collection<String> stringCollection = null;

    for (String item : items) {
      val = Lookup.getLookup(music).getShows(item).size();
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

    StatisticsCreator creator = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), Links.STATS, pageTitle, Links.CITIES_DIR);
    creator.add(makeTable(names, values, tableTitle, typeString));
    creator.close();
  }

  public static void generateTracksPages(Music music, Links links, String outputDir) {
    List<Album> items = music.getAlbum();
    int index = 0;
                
    Collections.sort(items, com.bolsinga.music.Compare.ALBUM_COMPARATOR);
                
    TracksDocumentCreator creator = new TracksDocumentCreator(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"));

    for (Album item : items) {                
      creator.add(item);
    }
    creator.close();

    List<Artist> artists = music.getArtist();
    Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_TRACKS_COMPARATOR);

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

      StatisticsCreator stats = TracksStatisticsCreator.createTracksStats(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), pageTitle, Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString));
      stats.close();
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

      StatisticsCreator stats = TracksStatisticsCreator.createAlbumStats(music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), pageTitle, Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString));
      stats.close();
    }
  }
        
  public static Element generatePreview(String sourceFile, int lastShowsCount) {
    Music music = Util.createMusic(sourceFile);
    return generatePreview(music, lastShowsCount);
  }
        
  public static Element generatePreview(Music music, int lastShowsCount) {
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

    List<Show> items = music.getShow();
    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);
    Collections.reverse(items);
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
        
  public static String getLinkedData(com.bolsinga.web.Encode encoder, Show show, boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(show, upOneLevel));
  }
        
  public static Element addItem(Music music, Links links, Artist artist) {
    // CSS.ARTIST_ITEM
    Vector<Element> e = new Vector<Element>();

    if (artist.getAlbum().size() > 0) {
      e.add(Web.addTracks(links, artist));
    }
                
    Collection relations = Lookup.getLookup(music).getRelations(artist);
    if (relations != null) {
      e.add(Web.addRelations(music, links, artist));
    }

    Collection<Show> shows = Lookup.getLookup(music).getShows(artist);
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
        
  public static Element addItem(Music music, Links links, Venue venue) {
    // CSS.VENUE_ITEM
    Vector<Element> e = new Vector<Element>();
                
    Collection relations = Lookup.getLookup(music).getRelations(venue);
    if (relations != null) {
      e.add(Web.addRelations(music, links, venue));
    }

    Collection<Show> shows = Lookup.getLookup(music).getShows(venue);
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
        
  private static ul getShowListing(Links links, Show show) {
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

  public static Element addItem(com.bolsinga.web.Encode encoder, Show show) {
    Links links = Links.getLinks(false);

    return Web.addItem(encoder, links, show);
  }

  public static Element addItem(com.bolsinga.web.Encode encoder, Links links, Show show) {
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

  public static Element addItem(Links links, Album album) {
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
        
  public static div addRelations(Music music, Links links, Artist artist) {
    Vector<Element> e = new Vector<Element>();
    Iterator iterator = Lookup.getLookup(music).getRelations(artist).iterator();
    while (iterator.hasNext()) {
      Artist art = (Artist)iterator.next();
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
        
  public static div addRelations(Music music, Links links, Venue venue) {
    Vector<Element> e = new Vector<Element>();
    Iterator iterator = Lookup.getLookup(music).getRelations(venue).iterator();
    while (iterator.hasNext()) {
      Venue v = (Venue)iterator.next();
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

  public static div addTracks(Links links, Artist artist) {
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
        
  public static Element addArtistIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();

    for (Artist art : music.getArtist()) {
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
        
  public static Element addVenueIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();
    for (Venue v : music.getVenue()) {
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

  public static Element addAlbumIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();
    for (Album alb : music.getAlbum()) {
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
        
  public static table makeTable(String[] names, int[] values, String caption, String header) {
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
        
  public static Element addShowIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map<String, String> m = new TreeMap<String, String>();
    for (Show s : music.getShow()) {
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
        
  static XhtmlDocument createHTMLDocument(Links links, String title) {
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
