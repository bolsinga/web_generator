package com.bolsinga.music.web;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

abstract class MusicDocumentCreator extends com.bolsinga.web.util.MultiDocumentCreator {
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
    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_HEADER);
    d.addElement(new h1().addElement(getTitle()));
    d.addElement(com.bolsinga.web.util.Util.getLogo());
    d.addElement(fLinks.addWebNavigator(fMusic, fProgram));
    d.addElement(addIndexNavigator());
    return d;
  }
}

abstract class SingleSectionMusicDocumentCreator extends com.bolsinga.web.util.DocumentCreator {
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
    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MUSIC_HEADER);
    d.addElement(new h1().addElement(getTitle()));
    d.addElement(com.bolsinga.web.util.Util.getLogo());
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
    return getTitle(com.bolsinga.web.util.Util.getResourceString("artists"));
  }
        
  protected boolean needNewDocument() {
    return ((fLastArtist == null) || !fLinks.getPageFileName(fLastArtist).equals(getCurrentLetter()));
  }
        
  protected boolean needNewSubsection() {
    return ((fLastArtist == null) || !fLastArtist.getName().equals(fCurArtist.getName()));
  }

  protected Element getSubsectionTitle() {
    return com.bolsinga.web.util.Util.createNamedTarget(fCurArtist.getId(), fCurArtist.getName());
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
    return getTitle(com.bolsinga.web.util.Util.getResourceString("venues"));
  }
        
  protected boolean needNewDocument() {
    return (fLastVenue == null) || (!fLinks.getPageFileName(fLastVenue).equals(getCurrentLetter()));
  }
        
  protected boolean needNewSubsection() {
    return (fLastVenue == null) || (!fLastVenue.getName().equals(fCurVenue.getName()));
  }

  protected Element getSubsectionTitle() {
    return com.bolsinga.web.util.Util.createNamedTarget(fCurVenue.getId(), fCurVenue.getName());
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
    
  public ShowDocumentCreator(Music music, Links links, String outputDir, String program) {
    super(music, links, outputDir, program);
  }
        
  public void add(Show item) {
    fCurShow = item;
    add();
    fLastShow = fCurShow;
  }
        
  protected String getTitle() {
    return getTitle(com.bolsinga.web.util.Util.getResourceString("dates"));
  }
    
  protected boolean needNewDocument() {
    return (fLastShow == null) || (!fLinks.getPageFileName(fLastShow).equals(getCurrentLetter()));
  }
        
  protected boolean needNewSubsection() {
    return (fLastShow == null) || (!Util.toMonth(fLastShow.getDate()).equals(Util.toMonth(fCurShow.getDate())));
  }
    
  protected Element getSubsectionTitle() {
    String m = Util.toMonth(fCurShow.getDate());
    return com.bolsinga.web.util.Util.createNamedTarget(m, m);
  }

  protected String getLastPath() {
    return fLinks.getPagePath(fLastShow);
  }
        
  protected String getCurrentLetter() {
    return fLinks.getPageFileName(fCurShow);
  }

  protected Element getCurrentElement() {
    return Web.addItem(fMusic, fLinks, fCurShow);
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
    Vector e = new Vector();
    if (fTracksStats) {
      e.add(new StringElement(com.bolsinga.web.util.Util.getResourceString("tracks")));
      e.add(fLinks.getAlbumsLink());
    } else {
      e.add(fLinks.getTracksLink());
      e.add(new StringElement(com.bolsinga.web.util.Util.getResourceString("albums")));
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.TRACKS_MENU);
    d.addElement(new h4(com.bolsinga.web.util.Util.getResourceString("view")));
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
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
    return getTitle(com.bolsinga.web.util.Util.getResourceString("tracks"));
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
    return Web.addItem(fMusic, fLinks, fCurAlbum);
  }

  protected Element addIndexNavigator() {
    return Web.addAlbumIndexNavigator(fMusic, fLinks, getCurrentLetter());
  }
}

public class Web {
        
  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Web [source.xml] [settings.xml] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.util.Util.createSettings(args[1]);
                
    Web.generate(args[0], args[2]);
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
                
    ArtistDocumentCreator creator = new ArtistDocumentCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"));
                
    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Artist)iterator.next();
                        
      creator.add(item);
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
                
    String typeString = com.bolsinga.web.util.Util.getResourceString("artist");
    String typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"), Links.STATS, pageTitle, Links.ARTIST_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.close();
  }
        
  public static void generateVenuePages(Music music, Links links, String outputDir) {
    List items = music.getVenue();
    Venue item = null;
    int index = 0;
                
    Collections.sort(items, com.bolsinga.music.util.Compare.VENUE_COMPARATOR);

    VenueDocumentCreator creator = new VenueDocumentCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"));
                
    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Venue)iterator.next();

      creator.add(item);
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
                
    String typeString = com.bolsinga.web.util.Util.getResourceString("venue");
    String typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"), Links.STATS, pageTitle, Links.VENUE_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
    stats.close();
  }
        
  public static void generateDatePages(Music music, Links links, String outputDir) {
    List items = music.getShow();
    Show item = null;
    Vector list = null;
    TreeMap dates = new TreeMap(com.bolsinga.music.util.Compare.SHOW_STATS_COMPARATOR);
                
    Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);

    ShowDocumentCreator creator = new ShowDocumentCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"));
                
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
                        
      creator.add(item);
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
                
    String typeString = com.bolsinga.web.util.Util.getResourceString("year");
    String typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator stats = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"), Links.STATS, pageTitle, Links.SHOW_DIR);
    stats.add(makeTable(names, values, tableTitle, typeString));
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
                
    String typeString = com.bolsinga.web.util.Util.getResourceString("city");
    String typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("statistics"), typeArgs);

    StatisticsCreator creator = new StatisticsCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"), Links.STATS, pageTitle, Links.CITIES_DIR);
    creator.add(makeTable(names, values, tableTitle, typeString));
    creator.close();
  }

  public static void generateTracksPages(Music music, Links links, String outputDir) {
    List items = music.getAlbum();

    Album item = null;
    int index = 0;
                
    Collections.sort(items, com.bolsinga.music.util.Compare.ALBUM_COMPARATOR);
                
    TracksDocumentCreator creator = new TracksDocumentCreator(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"));
                
    ListIterator iterator = items.listIterator();
    while (iterator.hasNext()) {
      item = (Album)iterator.next();
                        
      creator.add(item);
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
                
    {
      String typeString = com.bolsinga.web.util.Util.getResourceString("artist");
      String typeArgs[] = { typeString };
      String tableTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("tracksby"), typeArgs);
      String statsArgs[] = { com.bolsinga.web.util.Util.getResourceString("track") };
      String pageTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("statistics"), statsArgs);

      StatisticsCreator stats = TracksStatisticsCreator.createTracksStats(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"), pageTitle, Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString));
      stats.close();
    }

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

    {
      String typeString = com.bolsinga.web.util.Util.getResourceString("artist");
      String typeArgs[] = { typeString };
      String tableTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("albumsby"), typeArgs);
      String statsArgs[] = { com.bolsinga.web.util.Util.getResourceString("album") };
      String pageTitle = MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("statistics"), statsArgs);

      StatisticsCreator stats = TracksStatisticsCreator.createAlbumStats(music, links, outputDir, com.bolsinga.web.util.Util.getResourceString("program"), pageTitle, Links.TRACKS_DIR);
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
                
    Vector e = new Vector();
                
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

    div dm = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_MENU);
    dm.addElement(com.bolsinga.web.util.Util.getLogo());
    Object[] genArgs = { music.getTimestamp().getTime() };
    dm.addElement(new h3(MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("generated"), genArgs)));
    dm.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
    dm.addElement(links.getICalLink());
                
    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_MAIN);
    d.addElement(dm);
                
    div dr = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_RECENT);
                
    sb = new StringBuffer();
    Object[] countArgs = { new Integer(lastShowsCount) };
    dr.addElement(new h3(MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("lastshows"), countArgs)));

    List items = music.getShow();
    Show item = null;
    Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
    Collections.reverse(items);
    for (int i = 0; i < lastShowsCount; i++) {
      item = (Show)items.get(i);
                        
      div ds = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.PREVIEW_SHOW);
      ds.addElement(new h4(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(item), Util.toString(item.getDate()))));
      ds.addElement(getShowListing(links, item));
                        
      dr.addElement(ds);
    }

    d.addElement(dr);
                
    return d;
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
        
  public static Element addItem(Music music, Links links, Artist artist) {
    // CSS.ARTIST_ITEM
    Vector e = new Vector();

    if (artist.getAlbum().size() > 0) {
      e.add(Web.addTracks(music, links, artist));
    }
                
    Collection relations = Lookup.getLookup(music).getRelations(artist);
    if (relations != null) {
      e.add(Web.addRelations(music, links, artist));
    }

    List shows = Lookup.getLookup(music).getShows(artist);
    if (shows != null) {
      ListIterator iterator = shows.listIterator();
      while (iterator.hasNext()) {
        Show show = (Show)iterator.next();

        Vector se = new Vector();
        StringBuffer sb = new StringBuffer();
        ListIterator bi = show.getArtist().listIterator();
        while (bi.hasNext()) {
          Artist performer = (Artist)bi.next();
                                    
          if (artist.equals(performer)) {
            sb.append(performer.getName());
          } else {
            sb.append(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
          }
                                    
          if (bi.hasNext()) {
            sb.append(", ");
          }
        }
        se.add(new StringElement(sb.toString()));
                            
        Venue venue = (Venue)show.getVenue();
        a venueA = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(venue), venue.getName());
        Location l = (Location)venue.getLocation();
        se.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                            
        String showLink = links.getLinkTo(show);
                            
        String comment = show.getComment();
        if (comment != null) {
          se.add(com.bolsinga.web.util.Util.createInternalA(showLink, com.bolsinga.web.util.Util.getResourceString("showsummary")));
        }
                            
        div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_SHOW);
        d.addElement(new h3().addElement(com.bolsinga.web.util.Util.createInternalA(showLink, Util.toString(show.getDate()))));
        d.addElement(com.bolsinga.web.util.Util.createUnorderedList(se));
        e.add(d);
      }
    }
                
    return com.bolsinga.web.util.Util.createUnorderedList(e);
  }
        
  public static Element addItem(Music music, Links links, Venue venue) {
    // CSS.VENUE_ITEM
    Vector e = new Vector();
                
    Collection relations = Lookup.getLookup(music).getRelations(venue);
    if (relations != null) {
      e.add(Web.addRelations(music, links, venue));
    }

    List shows = Lookup.getLookup(music).getShows(venue);
    ListIterator iterator = shows.listIterator();
    while (iterator.hasNext()) {
      Show show = (Show)iterator.next();
                        
      String showLink = links.getLinkTo(show);
                        
      Vector se = new Vector();
      StringBuffer sb = new StringBuffer();
      ListIterator bi = show.getArtist().listIterator();
      while (bi.hasNext()) {
        Artist performer = (Artist)bi.next();
        sb.append(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
                                
        if (bi.hasNext()) {
          sb.append(", ");
        }
      }
      se.add(new StringElement(sb.toString()));
                        
      Location l = (Location)venue.getLocation();
      se.add(new StringElement(venue.getName() + ", " + l.getCity() + ", " + l.getState()));
                        
      String comment = show.getComment();
      if (comment != null) {
        se.add(com.bolsinga.web.util.Util.createInternalA(showLink, com.bolsinga.web.util.Util.getResourceString("showsummary")));
      }
                        
      div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_SHOW);
      d.addElement(new h3().addElement(com.bolsinga.web.util.Util.createInternalA(showLink, Util.toString(show.getDate()))));
      d.addElement(com.bolsinga.web.util.Util.createUnorderedList(se));
      e.add(d);                        
    }
                
    return com.bolsinga.web.util.Util.createUnorderedList(e);
  }
        
  private static ul getShowListing(Links links, Show show) {
    Vector e = new Vector();
    StringBuffer sb = new StringBuffer();
    ListIterator bi = show.getArtist().listIterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next();
                        
      sb.append(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(performer), performer.getName()));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = (Venue)show.getVenue();
    a venueA = com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(venue), venue.getName());
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                
    return com.bolsinga.web.util.Util.createUnorderedList(e);
  }
        
  public static Element addItem(Music music, Links links, Show show) {
    // CSS.SHOW_ITEM
    Vector e = new Vector();

    e.add(new h3().addElement(com.bolsinga.web.util.Util.createNamedTarget(show.getId(), Util.toString(show.getDate()))));

    e.add(Web.getShowListing(links, show));

    String comment = show.getComment();
    if (comment != null) {
      e.add(com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.SHOW_COMMENT).addElement(getLinkedData(music, comment, true)));
    }
                
    return com.bolsinga.web.util.Util.createUnorderedList(e);
  }

  public static Element addItem(Music music, Links links, Album album) {
    // CSS.TRACKS_ITEM
    Vector e = new Vector();
                
    StringBuffer sb;
    Artist artist = null;
    Song song;
                
    boolean isCompilation = album.isCompilation();
                
    sb = new StringBuffer();
    sb.append(com.bolsinga.web.util.Util.createNamedTarget(album.getId(), album.getTitle()));
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

    e.add(new h2().addElement(sb.toString()));

    Vector ae = new Vector();
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
      ae.add(new StringElement(sb.toString()));
    }
    e.add(com.bolsinga.web.util.Util.createOrderedList(ae));

    return com.bolsinga.web.util.Util.createUnorderedList(e);
  }
        
  public static div addRelations(Music music, Links links, Artist artist) {
    Vector e = new Vector();
    Iterator iterator = Lookup.getLookup(music).getRelations(artist).iterator();
    while (iterator.hasNext()) {
      Artist art = (Artist)iterator.next();
      if (art.equals(artist)) {
        e.add(new StringElement(art.getName()));
      } else {
        e.add(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(art), art.getName()));
      }
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_RELATION);
    d.addElement(new h3().addElement(com.bolsinga.web.util.Util.getResourceString("seealso")));
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
    return d;
  }
        
  public static div addRelations(Music music, Links links, Venue venue) {
    Vector e = new Vector();
    Iterator iterator = Lookup.getLookup(music).getRelations(venue).iterator();
    while (iterator.hasNext()) {
      Venue v = (Venue)iterator.next();
      if (v.equals(venue)) {
        e.add(new StringElement(v.getName()));
      } else {
        e.add(com.bolsinga.web.util.Util.createInternalA(links.getLinkTo(v), v.getName()));
      }
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_RELATION);
    d.addElement(new h3().addElement(com.bolsinga.web.util.Util.getResourceString("seealso")));
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
    return d;
  }

  public static div addTracks(Music music, Links links, Artist artist) {
    Vector e = new Vector();
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
      e.add(new StringElement(sb.toString()));
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_TRACKS);
    d.addElement(new h3().addElement(com.bolsinga.web.util.Util.getResourceString("albums")));
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
    return d;
  }
        
  public static Element addArtistIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map m = new TreeMap();
    Iterator iterator = music.getArtist().iterator();
    while (iterator.hasNext()) {
      Artist art = (Artist)iterator.next();
      String letter = links.getPageFileName(art);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(art));
      }
    }

    Vector e = new Vector();
    iterator = m.keySet().iterator();
    while (iterator.hasNext()) {
      String s = (String)iterator.next();
      if (s.equals(curLetter)) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s));
      }
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.ARTIST_INDEX);
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
    return d;
  }
        
  public static Element addVenueIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map m = new TreeMap();
    Iterator iterator = music.getVenue().iterator();
    while (iterator.hasNext()) {
      Venue v = (Venue)iterator.next();
      String letter = links.getPageFileName(v);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(v));
      }
    }

    Vector e = new Vector();
    iterator = m.keySet().iterator();
    while (iterator.hasNext()) {
      String v = (String)iterator.next();
      if (v.equals(curLetter)) {
        e.add(new StringElement(v));
      } else {
        e.add(com.bolsinga.web.util.Util.createInternalA((String)m.get(v), v));
      }
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.VENUE_INDEX);
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
    return d;
  }

  public static Element addAlbumIndexNavigator(Music music, Links links, String curLetter) {
    java.util.Map m = new TreeMap();
    Iterator iterator = music.getAlbum().iterator();
    while (iterator.hasNext()) {
      Album alb = (Album)iterator.next();
      String letter = links.getPageFileName(alb);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(alb));
      }
    }

    Vector e = new Vector();
    iterator = m.keySet().iterator();
    while (iterator.hasNext()) {
      String s = (String)iterator.next();
      if (s.equals(curLetter)) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s));
      }
    }

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.TRACKS_INDEX);
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
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
    java.util.Map m = new TreeMap();
    Iterator iterator = music.getShow().iterator();
    while (iterator.hasNext()) {
      Show s = (Show)iterator.next();
      String letter = links.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(s));
      }
    }

    Vector e = new Vector();
    iterator = m.keySet().iterator();
    while (iterator.hasNext()) {
      String s = (String)iterator.next();
      if (s.equals(curLetter)) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s));
      }
    }
    e.add(links.getICalLink());

    div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.SHOW_INDEX);
    d.addElement(com.bolsinga.web.util.Util.createUnorderedList(e));
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
                
    sb.append(com.bolsinga.web.util.Util.getResourceString("program"));
                
    sb.append(" (built: ");
    sb.append(com.bolsinga.web.util.Util.getResourceString("builddate"));
    sb.append(" running on jdk ");
    sb.append(System.getProperty("java.runtime.version"));
    sb.append(" - ");
    sb.append(System.getProperty("os.name"));
    sb.append(" ");
    sb.append(System.getProperty("os.version"));
                
    sb.append(" [");
    sb.append(com.bolsinga.web.util.Util.getResourceString("copyright"));
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
