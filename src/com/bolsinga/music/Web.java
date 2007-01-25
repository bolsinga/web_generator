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

class ShowRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, com.bolsinga.web.IndexPair> fIndex;

  private final com.bolsinga.web.Encode fEncoder;
  private final boolean fUpOneLevel;

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    ShowRecordDocumentCreator creator = new ShowRecordDocumentCreator(music, outputDir, encoder, true);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
    
  private ShowRecordDocumentCreator(final Music music, final String outputDir, final com.bolsinga.web.Encode encoder, final boolean upOneLevel) {
    super(music, outputDir);
    fEncoder = encoder;
    fUpOneLevel = upOneLevel;
    fIndex = createIndex();
  }
  
  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    for (final Vector<Show> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Show first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new com.bolsinga.web.RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Vector<Show> item : getMonthlies(group)) {
                records.add(getShowMonthRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return com.bolsinga.web.Util.createPageTitle(curName, com.bolsinga.web.Util.getResourceString("dates"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public com.bolsinga.web.Navigator getNavigator() {
              return new com.bolsinga.web.Navigator(fLinks) {
                public Element getShowNavigator() {
                  return com.bolsinga.web.Util.addCurrentIndexNavigator(fIndex, curName, super.getShowNavigator());
                }
              };
            }
          });
        }
      });
    }
  }

  protected void createStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new StatsRecordFactory() {
          protected Table getTable() {
            return getStats();
          }
          
          public String getDirectory() {
            return com.bolsinga.web.Links.SHOW_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("year") };
            return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getShowNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("dates"));
              }
            };
          }
        });
      }
    });
  }
  
  private Collection<Vector<Show>> getMonthlies(final Vector<Show> items) {
    TreeMap<com.bolsinga.music.data.Date, Vector<Show>> result =
      new TreeMap<com.bolsinga.music.data.Date, Vector<Show>>(Compare.DATE_MONTH_COMPARATOR);

    for (Show item : items) {
      com.bolsinga.music.data.Date key = item.getDate();
      Vector<Show> showList;
      if (result.containsKey(key)) {
        showList = result.get(key);
        showList.add(item);
      } else {
        showList = new Vector<Show>();
        showList.add(item);
        result.put(key, showList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private java.util.Map<String, com.bolsinga.web.IndexPair> createIndex() {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Show s : Util.getShowsUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(fLinks.getLinkToPage(s), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("dates"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Show>> getGroups() {
    List<Show> shows = Util.getShowsCopy(fMusic);
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    HashMap<String, Vector<Show>> result = new HashMap<String, Vector<Show>>(shows.size());
    
    Collections.sort(shows, Compare.SHOW_COMPARATOR);
    
    for (Show show : shows) {
      String key = fLinks.getPageFileName(show);
      Vector<Show> showList;
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
  
  private Table getStats() {
    List<Show> items = Util.getShowsCopy(fMusic);
    Collections.sort(items, Compare.SHOW_COMPARATOR);

    Collection<Show> showCollection = null;
    TreeMap<Show, Collection<Show>> dates = new TreeMap<Show, Collection<Show>>(Compare.SHOW_STATS_COMPARATOR);
                
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
      String letter = fLinks.getPageFileName(item);
      com.bolsinga.web.IndexPair p = fIndex.get(letter);
      names[i] = com.bolsinga.web.Util.createInternalA(p.getLink(), letter, p.getTitle()).toString();
      values[i] = dates.get(item).size();
                        
      i++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("year");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("datestatssummary"));
  }
  
  private com.bolsinga.web.Record getShowRecord(final Show show) {
    String comment = show.getComment();
    if (comment != null) {
      comment = fEncoder.embedLinks(show, fUpOneLevel);
    }
    return com.bolsinga.web.Record.createRecordListWithComment(
      com.bolsinga.web.Util.createNamedTarget(show.getId(), Util.toString(show.getDate())), 
      Web.getShowListing(fLookup, fLinks, show),
      comment);
  }

  private com.bolsinga.web.Record getShowMonthRecordSection(final Vector<Show> shows) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();

    // Note shows here is a Collection of Shows in a single month
    String m = Util.toMonth(shows.firstElement().getDate());
    A title = com.bolsinga.web.Util.createNamedTarget(m, m);
    for (Show show : shows) {
      items.add(getShowRecord(show));
    }

    return com.bolsinga.web.Record.createRecordSection(title, items);
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

      music = MySQLCreator.createMusic(user, password);
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
    Compare.tidy(music);
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
    ArtistRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);
    
    VenueRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);

    ShowRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, encoder, outputDir);

    CityRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);

    TracksRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);
  }

  private static String createPreviewLine(final int count, final String name) {
    Object[] args = { Integer.valueOf(count), name };
    return MessageFormat.format(com.bolsinga.web.Util.getResourceString("previewformat"), args);
  }

  public static com.bolsinga.web.Navigator getMainPagePreviewNavigator(final Music music, final com.bolsinga.web.Links links) {
    return new com.bolsinga.web.Navigator(links) {
      public Element getHomeNavigator() {
        return getCurrentNavigator();
      }

      public Element getArtistNavigator() {
        return links.getArtistLink(Web.createPreviewLine( Util.getArtistsUnmodifiable(music).size(),
                                                          com.bolsinga.web.Util.getResourceString("bands")));
      }

      public Element getTrackNavigator() {
        return links.getTracksLink(Web.createPreviewLine( Util.getSongsUnmodifiable(music).size(),
                                                          com.bolsinga.web.Util.getResourceString("tracks")));
      }

      public Element getAlbumNavigator() {
        return links.getAlbumsLink(Web.createPreviewLine( Util.getAlbumsUnmodifiable(music).size(),
                                                          com.bolsinga.web.Util.getResourceString("albums")));
      }
      
      public Element getShowNavigator() {
        return links.getShowLink(Web.createPreviewLine( Util.getShowsUnmodifiable(music).size(),
                                                        com.bolsinga.web.Util.getResourceString("dates")));
      }
      
      public Element getVenueNavigator() {
        return links.getVenueLink(Web.createPreviewLine(Util.getVenuesUnmodifiable(music).size(),
                                                        com.bolsinga.web.Util.getResourceString("venues")));
      }
      
      public Element getCityNavigator() {
        return links.getCityLink(Web.createPreviewLine( Lookup.getLookup(music).getCities().size(),
                                                        com.bolsinga.web.Util.getResourceString("cities")));
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("home"));
      }
    };
  }
        
  static Vector<Element> getShowListing(final Lookup lookup, final com.bolsinga.web.Links links, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
                        
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(com.bolsinga.web.Util.createInternalA(links.getLinkTo(performer), lookup.getHTMLName(performer), t));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = (Venue)show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = com.bolsinga.web.Util.createInternalA(links.getLinkTo(venue), lookup.getHTMLName(venue), t);
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                
    return e;
  }

  // Used by the unified (diary & music) main page.
  public static Element addItem(final com.bolsinga.web.Encode encoder, final Lookup lookup, final com.bolsinga.web.Links links, final Show show, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();

    e.add(new H3().addElement(com.bolsinga.web.Util.createNamedTarget(show.getId(), Util.toString(show.getDate()))));

    e.add(com.bolsinga.web.Util.createUnorderedList(Web.getShowListing(lookup, links, show)));

    String comment = show.getComment();
    if (comment != null) {
      e.add(new StringElement(com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(show, upOneLevel))));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}
