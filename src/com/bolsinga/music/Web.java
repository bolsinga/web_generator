package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

abstract class MusicDocumentCreator extends com.bolsinga.web.MultiDocumentCreator {
  protected final Lookup fLookup;
    
  protected MusicDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    super(backgrounder, links, outputDir);
    fLookup = lookup;
  }

  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(com.bolsinga.web.Util.getSettings().getCopyrightStartYear().intValue());
  }
}

class ArtistDocumentCreator extends MusicDocumentCreator {
  private final java.util.Map<String, com.bolsinga.web.IndexPair> fArtistIndex;

  // These change during the life-cycle of this object
  private Artist fLastArtist = null;
  private Artist fCurArtist  = null;
          
  public ArtistDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final java.util.Map<String, com.bolsinga.web.IndexPair> artistIndex, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    super(backgrounder, lookup, links, outputDir);
    fArtistIndex = artistIndex;
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
    return com.bolsinga.web.Util.createNamedTarget(fCurArtist.getId(), fLookup.getHTMLName(fCurArtist));
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
  
  protected com.bolsinga.web.Navigator getNavigator() {
    return new com.bolsinga.web.Navigator(fLinks) {
      public Element getArtistNavigator() {
        return com.bolsinga.web.Util.addCurrentIndexNavigator(fArtistIndex, getCurrentLetter(), super.getArtistNavigator());
      }
    };
  }
}

class VenueDocumentCreator extends MusicDocumentCreator {
  private final java.util.Map<String, com.bolsinga.web.IndexPair> fVenueIndex;

  // These change during the life-cycle of this object
  private Venue fLastVenue = null;
  private Venue fCurVenue  = null;
    
  public VenueDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final java.util.Map<String, com.bolsinga.web.IndexPair> venueIndex, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    super(backgrounder, lookup, links, outputDir);
    fVenueIndex = venueIndex;
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
    return com.bolsinga.web.Util.createNamedTarget(fCurVenue.getId(), fLookup.getHTMLName(fCurVenue));
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

  protected com.bolsinga.web.Navigator getNavigator() {
    return new com.bolsinga.web.Navigator(fLinks) {
      public Element getVenueNavigator() {
        return com.bolsinga.web.Util.addCurrentIndexNavigator(fVenueIndex, getCurrentLetter(), super.getVenueNavigator());
      }
    };
  }
}

class ShowDocumentCreator extends MusicDocumentCreator {
  private final com.bolsinga.web.Encode fEncoder;
  private final java.util.Map<String, com.bolsinga.web.IndexPair> fShowIndex;

  // These change during the life-cycle of this object
  private Show fLastShow = null;
  private Show fCurShow  = null;

  public ShowDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final java.util.Map<String, com.bolsinga.web.IndexPair> showIndex, final Lookup lookup, final com.bolsinga.web.Encode encoder, final com.bolsinga.web.Links links, final String outputDir) {
    super(backgrounder, lookup, links, outputDir);
    fEncoder = encoder;
    fShowIndex = showIndex;
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
    return Web.addItem(fEncoder, fLookup, fLinks, fCurShow, true);
  }
  
  protected com.bolsinga.web.Navigator getNavigator() {
    return new com.bolsinga.web.Navigator(fLinks) {
      public Element getShowNavigator() {
        return com.bolsinga.web.Util.addCurrentIndexNavigator(fShowIndex, getCurrentLetter(), super.getShowNavigator());
      }
    };
  }
}

class TracksStatisticsCreator extends com.bolsinga.web.SingleElementDocumentCreator {
  private final boolean fTracksStats;
        
  public static TracksStatisticsCreator createTracksStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Links links, final String outputDir, final String title, final String directory) {
    return new TracksStatisticsCreator(backgrounder, links, outputDir, com.bolsinga.web.Links.STATS, true, title, directory);
  }

  public static TracksStatisticsCreator createAlbumStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Links links, final String outputDir, final String title, final String directory) {
    return new TracksStatisticsCreator(backgrounder, links, outputDir, com.bolsinga.web.Links.ALBUM_STATS, false, title, directory);
  }

  private TracksStatisticsCreator(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Links links, final String outputDir, final String filename, final boolean isTracksStats, final String title, final String directory) {
    super(backgrounder, links, outputDir, filename, title, directory, new com.bolsinga.web.Navigator(links) {
      public Element getTrackNavigator() {
        return getCurrentNavigator();
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("tracks"));
      }
    });
    fTracksStats = isTracksStats;
  }
}

class TracksDocumentCreator extends com.bolsinga.web.DocumentCreator {
  private final java.util.Map<String, com.bolsinga.web.IndexPair> fAlbumIndex;
  protected final Lookup fLookup;

  // These change during the life-cycle of this object
  private Album fLastAlbum = null;
  private Album fCurAlbum  = null;
          
  public TracksDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final java.util.Map<String, com.bolsinga.web.IndexPair> albumIndex, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    super(backgrounder, links, outputDir);
    fAlbumIndex = albumIndex;
    fLookup = lookup;
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
    return Web.addItem(fLookup, fLinks, fCurAlbum);
  }

  protected com.bolsinga.web.Navigator getNavigator() {
    return new com.bolsinga.web.Navigator(fLinks) {
      public Element getTrackNavigator() {
        return com.bolsinga.web.Util.addCurrentIndexNavigator(fAlbumIndex, getCurrentLetter(), super.getTrackNavigator());
      }
    };
  }

  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(com.bolsinga.web.Util.getSettings().getCopyrightStartYear().intValue());
  }
}

public class Web implements com.bolsinga.web.Backgroundable {

  private static final boolean GENERATE_XML = false;

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

      music = com.bolsinga.music.MySQLCreator.createMusic(user, password);
    } else {
      Web.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);

    if (Web.GENERATE_XML) {
      Web.export(music);
      System.exit(0);
    }

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, null);
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
    Web.generate(fBackgrounder, this, music, encoder, outputDir);
  }

  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    final Lookup lookup = Lookup.getLookup(music);
    final com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(true);

    final java.util.Map<String, com.bolsinga.web.IndexPair> artistIndex = Web.createArtistIndex(Util.getArtistsUnmodifiable(music), links);
    Collection<Collection<Artist>> artistGroups = Web.getArtistGroups(music, links);
    for (final Collection<Artist> artistGroup : artistGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          Web.generateArtistPages(backgrounder, artistGroup, artistIndex, lookup, links, outputDir);
        }
      });
    }
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateArtistStats(backgrounder, music, lookup, links, outputDir);
      }
    });
    
    final java.util.Map<String, com.bolsinga.web.IndexPair> venueIndex = Web.createVenueIndex(Util.getVenuesUnmodifiable(music), links);
    Collection<Collection<Venue>> venueGroups = Web.getVenueGroups(music, links);
    for (final Collection<Venue> venueGroup : venueGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          Web.generateVenuePages(backgrounder, venueGroup, venueIndex, lookup, links, outputDir);
        }
      });
    }
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateVenueStats(backgrounder, music, lookup, links, outputDir);
      }
    });

    final java.util.Map<String, com.bolsinga.web.IndexPair> showIndex = Web.createShowIndex(Util.getShowsUnmodifiable(music), links);
    Collection<Collection<Show>> showGroups = Web.getShowGroups(music, links);
    for (final Collection<Show> showGroup : showGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          Web.generateDatePages(backgrounder, showGroup, showIndex, encoder, lookup, links, outputDir);
        }
      });
    }
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateDateStats(backgrounder, music, showIndex, encoder, lookup, links, outputDir);
      }
    });

    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateCityPages(backgrounder, music, lookup, links, outputDir);
      }
    });

    final java.util.Map<String, com.bolsinga.web.IndexPair> albumIndex = Web.createAlbumIndex(Util.getAlbumsUnmodifiable(music), links);
    Collection<Collection<Album>> albumGroups = Web.getAlbumGroups(music, links);
    for (final Collection<Album> albumGroup : albumGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          Web.generateTracksPages(backgrounder, albumGroup, albumIndex, lookup, links, outputDir);
        }
      });
    }
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateTracksStats(backgrounder, music, lookup, links, outputDir);
      }
    });
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateAlbumsStats(backgrounder, music, lookup, links, outputDir);
      }
    });
  }

  private static Collection<Collection<Artist>> getArtistGroups(final Music music, final com.bolsinga.web.Links links) {
    List<Artist> artists = Util.getArtistsCopy(music);
    // Each group is per page, so they are grouped by Artist who have the same starting sort letter.
    HashMap<String, Collection<Artist>> result = new HashMap<String, Collection<Artist>>(artists.size());
    
    Collections.sort(artists, Compare.ARTIST_COMPARATOR);
    
    for (Artist artist : artists) {
      String key = links.getPageFileName(artist);
      Collection<Artist> artistList;
      if (result.containsKey(key)) {
        artistList = result.get(key);
        artistList.add(artist);
      } else {
        artistList = new Vector<Artist>();
        artistList.add(artist);
        result.put(key, artistList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private static Collection<Collection<Venue>> getVenueGroups(final Music music, final com.bolsinga.web.Links links) {
    List<Venue> venues = Util.getVenuesCopy(music);
    // Each group is per page, so they are grouped by Venue who have the same starting sort letter.
    HashMap<String, Collection<Venue>> result = new HashMap<String, Collection<Venue>>(venues.size());
    
    Collections.sort(venues, Compare.VENUE_COMPARATOR);
    
    for (Venue venue : venues) {
      String key = links.getPageFileName(venue);
      Collection<Venue> venueList;
      if (result.containsKey(key)) {
        venueList = result.get(key);
        venueList.add(venue);
      } else {
        venueList = new Vector<Venue>();
        venueList.add(venue);
        result.put(key, venueList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private static Collection<Collection<Show>> getShowGroups(final Music music, final com.bolsinga.web.Links links) {
    List<Show> shows = Util.getShowsCopy(music);
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    HashMap<String, Collection<Show>> result = new HashMap<String, Collection<Show>>(shows.size());
    
    Collections.sort(shows, Compare.SHOW_COMPARATOR);
    
    for (Show show : shows) {
      String key = links.getPageFileName(show);
      Collection<Show> showList;
      if (result.containsKey(key)) {
        showList = result.get(key);
        showList.add(show);
      } else {
        showList = new Vector<Show>();
        showList.add(show);
        result.put(key, showList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private static Collection<Collection<Album>> getAlbumGroups(final Music music, final com.bolsinga.web.Links links) {
    List<Album> albums = Util.getAlbumsCopy(music);
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    HashMap<String, Collection<Album>> result = new HashMap<String, Collection<Album>>(albums.size());
    
    Collections.sort(albums, Compare.ALBUM_COMPARATOR);
    
    for (Album album : albums) {
      String key = links.getPageFileName(album);
      Collection<Album> albumList;
      if (result.containsKey(key)) {
        albumList = result.get(key);
        albumList.add(album);
      } else {
        albumList = new Vector<Album>();
        albumList.add(album);
        result.put(key, albumList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  // NOTE: Instead of a List of ID's, JAXB returns a List of real items.
    
  private static void generateArtistPages(final com.bolsinga.web.Backgrounder backgrounder, final Collection<Artist> items, final java.util.Map<String, com.bolsinga.web.IndexPair> index, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    ArtistDocumentCreator creator = new ArtistDocumentCreator(backgrounder, index, lookup, links, outputDir);
    for (Artist item : items) {
      creator.add(item);
    }
    creator.complete();
  }
  
  private static void generateArtistStats(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    List<Artist> items = Util.getArtistsCopy(music);
    Collections.sort(items, com.bolsinga.music.Compare.getCompare(music).ARTIST_STATS_COMPARATOR);

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Artist item : items) {
      String t = com.bolsinga.music.Util.createTitle("moreinfoartist", item.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), lookup.getHTMLName(item), t).toString();
      Collection<Show> shows = lookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    com.bolsinga.web.SingleElementDocumentCreator stats = new com.bolsinga.web.SingleElementDocumentCreator(backgrounder, links, outputDir, com.bolsinga.web.Links.STATS, pageTitle, com.bolsinga.web.Links.ARTIST_DIR, new com.bolsinga.web.Navigator(links) {
      public Element getArtistNavigator() {
        return getCurrentNavigator();
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("bands"));
      }
    });
    stats.add(makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("artiststatsummary")));
    stats.complete();
  }

  public static void generateVenuePages(final com.bolsinga.web.Backgrounder backgrounder, final Collection<Venue> items, final java.util.Map<String, com.bolsinga.web.IndexPair> index, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    VenueDocumentCreator creator = new VenueDocumentCreator(backgrounder, index, lookup, links, outputDir);
    for (Venue item : items) {
      creator.add(item);
    }
    creator.complete();
  }
  
  private static void generateVenueStats(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    List<Venue> items = Util.getVenuesCopy(music);
    Collections.sort(items, com.bolsinga.music.Compare.getCompare(music).VENUE_STATS_COMPARATOR);

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Venue item : items) {
      String t = com.bolsinga.music.Util.createTitle("moreinfovenue", item.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(item), lookup.getHTMLName(item), t).toString();
      Collection<Show> shows = lookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("venue");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    com.bolsinga.web.SingleElementDocumentCreator stats = new com.bolsinga.web.SingleElementDocumentCreator(backgrounder, links, outputDir, com.bolsinga.web.Links.STATS, pageTitle, com.bolsinga.web.Links.VENUE_DIR, new com.bolsinga.web.Navigator(links) {
      public Element getVenueNavigator() {
        return getCurrentNavigator();
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("venues"));
      }
    });
    stats.add(makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("venuestatsummary")));
    stats.complete();
  }

  public static void generateDatePages(final com.bolsinga.web.Backgrounder backgrounder, final Collection<Show> items, final java.util.Map<String, com.bolsinga.web.IndexPair> index, final com.bolsinga.web.Encode encoder, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    ShowDocumentCreator creator = new ShowDocumentCreator(backgrounder, index, lookup, encoder, links, outputDir);
    for (Show item : items) {
      creator.add(item);
    }
    creator.complete();
  }
  
  public static void generateDateStats(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final java.util.Map<String, com.bolsinga.web.IndexPair> index, final com.bolsinga.web.Encode encoder, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    List<Show> items = Util.getShowsCopy(music);
    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);

    Collection<Show> showCollection = null;
    TreeMap<Show, Collection<Show>> dates = new TreeMap<Show, Collection<Show>>(com.bolsinga.music.Compare.SHOW_STATS_COMPARATOR);
                
    for (Show item : items) {
      if (dates.containsKey(item)) {
        showCollection = dates.get(item);
        showCollection.add(item);
      } else {
        showCollection = new Vector<Show>();
        showCollection.add(item);
        dates.put(item, showCollection);
      }
    }

    int i = 0;
    String[] names = new String[dates.size()];
    int[] values = new int[dates.size()];

    for (Show item : dates.keySet()) {
      String letter = links.getPageFileName(item);
      com.bolsinga.web.IndexPair p = index.get(letter);
      names[i] = com.bolsinga.web.Util.createInternalA(p.getLink(), letter, p.getTitle()).toString();
      values[i] = dates.get(item).size();
                        
      i++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("year");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);

    com.bolsinga.web.SingleElementDocumentCreator stats = new com.bolsinga.web.SingleElementDocumentCreator(backgrounder, links, outputDir, com.bolsinga.web.Links.STATS, pageTitle, com.bolsinga.web.Links.SHOW_DIR, new com.bolsinga.web.Navigator(links) {
      public Element getShowNavigator() {
        return getCurrentNavigator();
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("dates"));
      }
    });
    stats.add(makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("datestatssummary")));
    stats.complete();
  }
        
  public static void generateCityPages(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
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

    com.bolsinga.web.SingleElementDocumentCreator creator = new com.bolsinga.web.SingleElementDocumentCreator(backgrounder, links, outputDir, com.bolsinga.web.Links.STATS, pageTitle, com.bolsinga.web.Links.CITIES_DIR, new com.bolsinga.web.Navigator(links) {
      public Element getCityNavigator() {
        return getCurrentNavigator();
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("cities"));
      }
    });
    creator.add(makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("citystatsummary")));
    creator.complete();
  }

  public static void generateTracksPages(final com.bolsinga.web.Backgrounder backgrounder, final Collection<Album> items, final java.util.Map<String, com.bolsinga.web.IndexPair> index, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    TracksDocumentCreator creator = new TracksDocumentCreator(backgrounder, index, lookup, links, outputDir);
    for (Album item : items) {                
      creator.add(item);
    }
    creator.complete();
  }
  
  private static void generateTracksStats(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    List<Artist> artists = Util.getArtistsCopy(music);
    Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_TRACKS_COMPARATOR);

    int index = 0;
    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    
    for (Artist artist : artists) {
      String t = com.bolsinga.music.Util.createTitle("moreinfoartist", artist.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), lookup.getHTMLName(artist), t).toString();
      values[index] = Util.trackCount(artist);
                        
      index++;
    }

    {
      String typeString = com.bolsinga.web.Util.getResourceString("artist");
      String tracksTableTitle = com.bolsinga.web.Util.getResourceString("tracksby");
      String albumsTableTitle = links.getAlbumsLink(com.bolsinga.web.Util.getResourceString("albumsby")).toString();
      Object statsArgs[] = { com.bolsinga.web.Util.getResourceString("track") };
      String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), statsArgs);
      Object titleArgs[] = { tracksTableTitle, albumsTableTitle };
      String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("albumtrackstitle"), titleArgs);
      
      com.bolsinga.web.SingleElementDocumentCreator stats = TracksStatisticsCreator.createTracksStats(backgrounder, links, outputDir, pageTitle, com.bolsinga.web.Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("trackstatsummary")));
      stats.complete();
    }
  }

  private static void generateAlbumsStats(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Lookup lookup, final com.bolsinga.web.Links links, final String outputDir) {
    List<Artist> artists = Util.getArtistsCopy(music);
    Collections.sort(artists, com.bolsinga.music.Compare.ARTIST_ALBUMS_COMPARATOR);

    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    int index = 0;
    
    for (Artist artist : artists) {
      String t = com.bolsinga.music.Util.createTitle("moreinfoartist", artist.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), lookup.getHTMLName(artist), t).toString();
      List<JAXBElement<Object>> albums = Util.getAlbumsUnmodifiable(artist);
      values[index] = (albums != null) ? albums.size() : 0;
                        
      index++;
    }

    {
      String typeString = com.bolsinga.web.Util.getResourceString("artist");
      String tracksTableTitle = links.getTracksLink(com.bolsinga.web.Util.getResourceString("tracksby")).toString();
      String albumsTableTitle = com.bolsinga.web.Util.getResourceString("albumsby");
      Object statsArgs[] = { com.bolsinga.web.Util.getResourceString("album") };
      String pageTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), statsArgs);
      Object titleArgs[] = { tracksTableTitle, albumsTableTitle };
      String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("albumtrackstitle"), titleArgs);

      com.bolsinga.web.SingleElementDocumentCreator stats = TracksStatisticsCreator.createAlbumStats(backgrounder, links, outputDir, pageTitle, com.bolsinga.web.Links.TRACKS_DIR);
      stats.add(makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("albumstatsummary")));
      stats.complete();
    }
  }

  private static String createPreviewLine(final int count, final String name) {
      Object[] args = { Integer.valueOf(count), name };
      return MessageFormat.format(com.bolsinga.web.Util.getResourceString("previewformat"), args);
  }
        
  public static Element generatePreview(final Music music) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.NAV_HEADER);
    d.addElement(com.bolsinga.web.Util.getLogo());
                
    com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(false);

    Vector<Element> e = new Vector<Element>();
    e.add(links.getArtistLink(Web.createPreviewLine(  Util.getArtistsUnmodifiable(music).size(),
                                                      com.bolsinga.web.Util.getResourceString("bands"))));

    e.add(links.getShowLink(Web.createPreviewLine(  Util.getShowsUnmodifiable(music).size(),
                                                    com.bolsinga.web.Util.getResourceString("dates"))));

    e.add(links.getVenueLink(Web.createPreviewLine( Util.getVenuesUnmodifiable(music).size(),
                                                    com.bolsinga.web.Util.getResourceString("venues"))));

    e.add(links.getCityLink(Web.createPreviewLine(  Lookup.getLookup(music).getCities().size(),
                                                    com.bolsinga.web.Util.getResourceString("cities"))));

    e.add(links.getTracksLink(Web.createPreviewLine(  Util.getSongsUnmodifiable(music).size(),
                                                      com.bolsinga.web.Util.getResourceString("tracks"))));

    e.add(links.getAlbumsLink(Web.createPreviewLine(  Util.getAlbumsUnmodifiable(music).size(),
                                                      com.bolsinga.web.Util.getResourceString("albums"))));
                
    Div indexNavigator = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_INDEX);
    indexNavigator.addElement(com.bolsinga.web.Util.createUnorderedList(e));

    d.addElement(indexNavigator);
    
    return d;
  }
        
  public static String getLinkedData(final com.bolsinga.web.Encode encoder, final Show show, final boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(show, upOneLevel));
  }
        
  public static Element addItem(final Lookup lookup, final com.bolsinga.web.Links links, final Artist artist) {
    Vector<Element> e = new Vector<Element>();

    if (Util.getAlbumsUnmodifiable(artist).size() > 0) {
      e.add(Web.addTracks(lookup, links, artist));
    }
                
    if (lookup.getRelations(artist) != null) {
      e.add(Web.addRelations(lookup, links, artist));
    }

    Collection<Show> shows = lookup.getShows(artist);
    if (shows != null) {
      for (Show show : shows) {
        Vector<Element> se = new Vector<Element>();
        StringBuilder sb = new StringBuilder();
        Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
        while (bi.hasNext()) {
          Artist performer = (Artist)bi.next().getValue();
          
          String htmlName = lookup.getHTMLName(performer);
          if (artist.equals(performer)) {
            sb.append(htmlName);
          } else {
            String t = com.bolsinga.music.Util.createTitle("moreinfoartist", performer.getName());
            sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), htmlName, t));
          }
                                    
          if (bi.hasNext()) {
            sb.append(", ");
          }
        }
        se.add(new StringElement(sb.toString()));
                            
        Venue venue = (Venue)show.getVenue();
        String t = com.bolsinga.music.Util.createTitle("moreinfovenue", venue.getName());
        A venueA = com.bolsinga.web.Util.createInternalA(links.getLinkTo(venue), lookup.getHTMLName(venue), t);
        Location l = (Location)venue.getLocation();
        se.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                            
        String showLink = links.getLinkTo(show);
                            
        String comment = show.getComment();
        if (comment != null) {
          se.add(com.bolsinga.web.Util.createInternalA(showLink, com.bolsinga.web.Util.getResourceString("showsummary"), com.bolsinga.web.Util.getResourceString("showsummarytitle")));
        }
                            
        Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ARTIST_SHOW);
        String dateString = Util.toString(show.getDate());
        d.addElement(new H3().addElement(com.bolsinga.web.Util.createInternalA(showLink, dateString, dateString)));
        d.addElement(com.bolsinga.web.Util.createUnorderedList(se));
        e.add(d);
      }
    }
    
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  public static Element addItem(final Lookup lookup, final com.bolsinga.web.Links links, final Venue venue) {
    Vector<Element> e = new Vector<Element>();
                
    if (lookup.getRelations(venue) != null) {
      e.add(Web.addRelations(lookup, links, venue));
    }

    Collection<Show> shows = lookup.getShows(venue);
    if (shows != null) {
      for (Show show : shows) {
        String showLink = links.getLinkTo(show);
        
        Vector<Element> se = new Vector<Element>();
        StringBuilder sb = new StringBuilder();
        Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
        while (bi.hasNext()) {
          Artist performer = (Artist)bi.next().getValue();
          String t = com.bolsinga.music.Util.createTitle("moreinfoartist", performer.getName());
          sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), lookup.getHTMLName(performer), t));
          
          if (bi.hasNext()) {
            sb.append(", ");
          }
        }
        se.add(new StringElement(sb.toString()));
        
        Location l = (Location)venue.getLocation();
        se.add(new StringElement(lookup.getHTMLName(venue) + ", " + l.getCity() + ", " + l.getState()));
        
        String comment = show.getComment();
        if (comment != null) {
          se.add(com.bolsinga.web.Util.createInternalA(showLink, com.bolsinga.web.Util.getResourceString("showsummary"), com.bolsinga.web.Util.getResourceString("showsummarytitle")));
        }
        
        Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.VENUE_SHOW);
        String dateString = Util.toString(show.getDate());
        d.addElement(new H3().addElement(com.bolsinga.web.Util.createInternalA(showLink, dateString, dateString)));
        d.addElement(com.bolsinga.web.Util.createUnorderedList(se));
        e.add(d);                        
      }
    }
                
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  private static UL getShowListing(final Lookup lookup, final com.bolsinga.web.Links links, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
                        
      String t = com.bolsinga.music.Util.createTitle("moreinfoartist", performer.getName());
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), lookup.getHTMLName(performer), t));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = (Venue)show.getVenue();
    String t = com.bolsinga.music.Util.createTitle("moreinfovenue", venue.getName());
    A venueA = com.bolsinga.web.Util.createInternalA(links.getLinkTo(venue), lookup.getHTMLName(venue), t);
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                
    return com.bolsinga.web.Util.createUnorderedList(e);
  }

  public static Element addItem(final com.bolsinga.web.Encode encoder, final Lookup lookup, final com.bolsinga.web.Links links, final Show show, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();

    e.add(new H3().addElement(com.bolsinga.web.Util.createNamedTarget(show.getId(), Util.toString(show.getDate()))));

    e.add(Web.getShowListing(lookup, links, show));

    String comment = show.getComment();
    if (comment != null) {
      e.add(com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.SHOW_COMMENT).addElement(getLinkedData(encoder, show, upOneLevel)));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }

  public static Element addItem(final Lookup lookup, final com.bolsinga.web.Links links, final Album album) {
    Vector<Element> e = new Vector<Element>();
                
    StringBuilder sb;
    Artist artist = null;
    Song song;
                
    boolean isCompilation = com.bolsinga.web.Util.convert(album.isCompilation());
                
    sb = new StringBuilder();
    sb.append(com.bolsinga.web.Util.createNamedTarget(album.getId(), lookup.getHTMLName(album)));
    if (!isCompilation) {
      artist = (Artist)album.getPerformer();
      sb.append(" - ");
      String t = com.bolsinga.music.Util.createTitle("moreinfoartist", artist.getName());
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), lookup.getHTMLName(artist), t));
    }
    com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
    if (albumRelease != null) {
      sb.append(" (");
      sb.append(albumRelease.getYear());
      sb.append(")");
    }

    e.add(new H2().addElement(sb.toString()));

    Vector<Element> ae = new Vector<Element>();
    List<JAXBElement<Object>> songs = Util.getSongsUnmodifiable(album);
    for (JAXBElement<Object> jsong : songs) {
      song = (Song)jsong.getValue();
      sb = new StringBuilder();
      if (isCompilation) {
        artist = (Artist)song.getPerformer();
        String t = com.bolsinga.music.Util.createTitle("moreinfoartist", artist.getName());
        sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(artist), lookup.getHTMLName(artist), t));
        sb.append(" - ");
      }
                        
      sb.append(com.bolsinga.web.Util.toHTMLSafe(song.getTitle()));
                        
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

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  public static Div addRelations(final Lookup lookup, final com.bolsinga.web.Links links, final Artist artist) {
    Vector<Element> e = new Vector<Element>();
    org.apache.ecs.Element curElement = null;
    for (Artist art : lookup.getRelations(artist)) {
      String htmlName = lookup.getHTMLName(art);
      if (art.equals(artist)) {
        curElement = new StringElement(htmlName);
        e.add(curElement);
      } else {
        String t = com.bolsinga.music.Util.createTitle("moreinfoartist", art.getName());
        e.add(com.bolsinga.web.Util.createInternalA(links.getLinkTo(art), htmlName, t));
      }
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_RELATION);
    d.addElement(new H3().addElement(com.bolsinga.web.Util.getResourceString("seealso")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e, curElement));
    return d;
  }
        
  public static Div addRelations(final Lookup lookup, final com.bolsinga.web.Links links, final Venue venue) {
    Vector<Element> e = new Vector<Element>();
    org.apache.ecs.Element curElement = null;
    for (Venue v : lookup.getRelations(venue)) {
      String htmlName = lookup.getHTMLName(v);
      if (v.equals(venue)) {
        curElement = new StringElement(htmlName);
        e.add(curElement);
      } else {
        String t = com.bolsinga.music.Util.createTitle("moreinfovenue", v.getName());
        e.add(com.bolsinga.web.Util.createInternalA(links.getLinkTo(v), htmlName, t));
      }
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_RELATION);
    d.addElement(new H3().addElement(com.bolsinga.web.Util.getResourceString("seealso")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e, curElement));
    return d;
  }

  public static Div addTracks(final Lookup lookup, final com.bolsinga.web.Links links, final Artist artist) {
    Vector<Element> e = new Vector<Element>();

    List<JAXBElement<Object>> albums = Util.getAlbumsCopy(artist);
    Collections.sort(albums, com.bolsinga.music.Compare.JAXB_ALBUM_ORDER_COMPARATOR);

    for (JAXBElement<Object> jalbum : albums) {
      Album album = (Album)jalbum.getValue();
      StringBuilder sb = new StringBuilder();
      String t = com.bolsinga.music.Util.createTitle("moreinfoalbum", album.getTitle());
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(album), lookup.getHTMLName(album), t));
      com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
      if (albumRelease != null) {
        sb.append(" (");
        sb.append(albumRelease.getYear());
        sb.append(")");
      }
      e.add(new StringElement(sb.toString()));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ARTIST_TRACKS);
    d.addElement(new H3().addElement(com.bolsinga.web.Util.getResourceString("albums")));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
  
  private static java.util.Map<String, com.bolsinga.web.IndexPair> createArtistIndex(final Collection<Artist> artists, final com.bolsinga.web.Links links) {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Artist art : artists) {
      String letter = links.getPageFileName(art);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(links.getLinkToPage(art), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("artists"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }
  
  private static java.util.Map<String, com.bolsinga.web.IndexPair> createVenueIndex(final Collection<Venue> venues, final com.bolsinga.web.Links links) {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Venue v : venues) {
      String letter = links.getPageFileName(v);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(links.getLinkToPage(v), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("venues"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private static java.util.Map<String, com.bolsinga.web.IndexPair> createAlbumIndex(final Collection<Album> items, final com.bolsinga.web.Links links) {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Album alb : items) {
      String letter = links.getPageFileName(alb);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(links.getLinkToPage(alb), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("albums"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }
    
  public static Table makeTable(final String[] names, final int[] values, final String caption, final String header, final String summary) {
    int runningTotal = 0;
    int i;
    for (i = 0; i < values.length; i++) {
      runningTotal += values[i];
    }
    final int total = runningTotal;
    
    return com.bolsinga.web.Util.makeTable(caption, summary, new com.bolsinga.web.TableHandler() {
      public TR getHeaderRow() {
        return new TR().addElement(new TH(header)).addElement(new TH("#")).addElement(new TH("%"));
      }

      public int getRowCount() {
        return values.length;
      }
      
      public TR getRow(final int row) {
        TR trow = new TR();
        TH thh = new TH(names[row]);
        thh.setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput());
        trow.addElement(thh);
        trow.addElement(new TD(Integer.toString(values[row])).setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput()));
        trow.addElement(new TD(Util.toString((double)values[row] / total * 100.0)).setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput()));
        return trow;
      }
      
      public TR getFooterRow() {
        TR trow = new TR();
        trow.addElement(new TH(Integer.toString(names.length)));
        trow.addElement(new TH(Integer.toString(total)));
        trow.addElement(new TH());
        return trow;
      }
    });
  }
        
  private static java.util.Map<String, com.bolsinga.web.IndexPair> createShowIndex(final Collection<Show> shows, final com.bolsinga.web.Links links) {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Show s : shows) {
      String letter = links.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(links.getLinkToPage(s), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("dates"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }
}
