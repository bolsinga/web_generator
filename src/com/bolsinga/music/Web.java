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

class ArtistRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, com.bolsinga.web.IndexPair> fIndex;
  
  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final String outputDir) {
    ArtistRecordDocumentCreator creator = new ArtistRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private ArtistRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    for (final Vector<Artist> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Artist first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new com.bolsinga.web.RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Artist item : group) {
                records.add(getArtistRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return com.bolsinga.web.Util.createPageTitle(curName, com.bolsinga.web.Util.getResourceString("artists"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public com.bolsinga.web.Navigator getNavigator() {
              return new com.bolsinga.web.Navigator(fLinks) {
                public Element getArtistNavigator() {
                  return com.bolsinga.web.Util.addCurrentIndexNavigator(fIndex, curName, super.getArtistNavigator());
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
            return com.bolsinga.web.Links.ARTIST_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("artist") };
            return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getArtistNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("bands"));
              }
            };
          }
        });
      }
    });
  }

  private java.util.Map<String, com.bolsinga.web.IndexPair> createIndex() {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Artist art : Util.getArtistsUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(art);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(fLinks.getLinkToPage(art), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("artists"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Artist>> getGroups() {
    List<Artist> artists = Util.getArtistsCopy(fMusic);
    // Each group is per page, so they are grouped by Artist who have the same starting sort letter.
    HashMap<String, Vector<Artist>> result = new HashMap<String, Vector<Artist>>(artists.size());
    
    Collections.sort(artists, Compare.ARTIST_COMPARATOR);
    
    for (Artist artist : artists) {
      String key = fLinks.getPageFileName(artist);
      Vector<Artist> artistList;
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

  private Table getStats() {
    List<Artist> items = Util.getArtistsCopy(fMusic);
    Collections.sort(items, Compare.getCompare(fMusic).ARTIST_STATS_COMPARATOR);

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Artist item : items) {
      String t = Util.createTitle("moreinfoartist", item.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(item), fLookup.getHTMLName(item), t).toString();
      Collection<Show> shows = fLookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    
    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("artiststatsummary"));
  }
  
  private com.bolsinga.web.Record getArtistShowRecord(final Artist artist, final Show show) {
    String dateString = Util.toString(show.getDate());
    
    return com.bolsinga.web.Record.createRecordList(
      com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getArtistShowListing(artist, show));
  }
  
  private com.bolsinga.web.Record getArtistRecordSection(final Artist artist) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();

    if (Util.getAlbumsUnmodifiable(artist).size() > 0) {
      items.add(getAlbumRelations(artist));
    }

    if (fLookup.getRelations(artist) != null) {
      items.add(getArtistRelations(artist));
    }

    Collection<Show> shows = fLookup.getShows(artist);
    if (shows != null) {
      for (Show show : shows) {
        items.add(getArtistShowRecord(artist, show));
      }
    }
    
    A title = com.bolsinga.web.Util.createNamedTarget(artist.getId(), fLookup.getHTMLName(artist));
    
    return com.bolsinga.web.Record.createRecordSection(title, items);
  }
  
  private Vector<Element> getArtistShowListing(final Artist artist, final Show show) {
    Vector<Element> e = new Vector<Element>();
    
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
      
      String htmlName = fLookup.getHTMLName(performer);
      if (artist.equals(performer)) {
        sb.append(htmlName);
      } else {
        String t = Util.createTitle("moreinfoartist", performer.getName());
        sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(performer), htmlName, t));
      }
                                
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                        
    Venue venue = (Venue)show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(venue), fLookup.getHTMLName(venue), t);
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                        
    String comment = show.getComment();
    if (comment != null) {
      e.add(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(show), com.bolsinga.web.Util.getResourceString("showsummary"), com.bolsinga.web.Util.getResourceString("showsummarytitle")));
    }
    
    return e;
  }

  private Vector<Element> getTracks(final Artist artist) {
    Vector<Element> e = new Vector<Element>();

    List<JAXBElement<Object>> albums = Util.getAlbumsCopy(artist);
    Collections.sort(albums, Compare.JAXB_ALBUM_ORDER_COMPARATOR);

    for (JAXBElement<Object> jalbum : albums) {
      Album album = (Album)jalbum.getValue();
      StringBuilder sb = new StringBuilder();
      String t = Util.createTitle("moreinfoalbum", album.getTitle());
      sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(album), fLookup.getHTMLName(album), t));
      com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
      if (albumRelease != null) {
        sb.append(" (");
        sb.append(albumRelease.getYear());
        sb.append(")");
      }
      e.add(new StringElement(sb.toString()));
    }
    
    return e;
  }
  
  private com.bolsinga.web.Record getAlbumRelations(final Artist artist) {
    return com.bolsinga.web.Record.createRecordList(
      new StringElement(com.bolsinga.web.Util.getResourceString("albums")), 
      getTracks(artist));
  }

  private com.bolsinga.web.Record getArtistRelations(final Artist artist) {
    Vector<Element> e = new Vector<Element>();
    
    org.apache.ecs.Element curElement = null;
    for (Artist art : fLookup.getRelations(artist)) {
      String htmlName = fLookup.getHTMLName(art);
      if (art.equals(artist)) {
        curElement = new StringElement(htmlName);
        e.add(curElement);
      } else {
        String t = Util.createTitle("moreinfoartist", art.getName());
        e.add(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(art), htmlName, t));
      }
    }

    return com.bolsinga.web.Record.createRecordList(
      new StringElement(com.bolsinga.web.Util.getResourceString("seealso")),
      e,
      curElement);
  }
}

class VenueRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, com.bolsinga.web.IndexPair> fIndex;

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final String outputDir) {
    VenueRecordDocumentCreator creator = new VenueRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private VenueRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    for (final Vector<Venue> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Venue first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new com.bolsinga.web.RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Venue item : group) {
                records.add(getVenueRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return com.bolsinga.web.Util.createPageTitle(curName, com.bolsinga.web.Util.getResourceString("venues"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public com.bolsinga.web.Navigator getNavigator() {
              return new com.bolsinga.web.Navigator(fLinks) {
                public Element getVenueNavigator() {
                  return com.bolsinga.web.Util.addCurrentIndexNavigator(fIndex, curName, super.getVenueNavigator());
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
            return com.bolsinga.web.Links.VENUE_DIR;
          }
          
          public String getTitle() {
            Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("venue") };
            return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getVenueNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("venues"));
              }
            };
          }
        });
      }
    });
  }
  
  private java.util.Map<String, com.bolsinga.web.IndexPair> createIndex() {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Venue v : Util.getVenuesUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(v);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(fLinks.getLinkToPage(v), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("venues"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Venue>> getGroups() {
    List<Venue> venues = Util.getVenuesCopy(fMusic);
    // Each group is per page, so they are grouped by Venue who have the same starting sort letter.
    HashMap<String, Vector<Venue>> result = new HashMap<String, Vector<Venue>>(venues.size());
    
    Collections.sort(venues, Compare.VENUE_COMPARATOR);
    
    for (Venue venue : venues) {
      String key = fLinks.getPageFileName(venue);
      Vector<Venue> venueList;
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
  
  private Table getStats() {
    List<Venue> items = Util.getVenuesCopy(fMusic);
    Collections.sort(items, Compare.getCompare(fMusic).VENUE_STATS_COMPARATOR);

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Venue item : items) {
      String t = Util.createTitle("moreinfovenue", item.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(item), fLookup.getHTMLName(item), t).toString();
      Collection<Show> shows = fLookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("venue");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("venuestatsummary"));
  }
  
  private Vector<Element> getVenueShowListing(final Venue venue, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(performer), fLookup.getHTMLName(performer), t));
      
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
    
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(fLookup.getHTMLName(venue) + ", " + l.getCity() + ", " + l.getState()));
    
    String comment = show.getComment();
    if (comment != null) {
      e.add(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(show), com.bolsinga.web.Util.getResourceString("showsummary"), com.bolsinga.web.Util.getResourceString("showsummarytitle")));
    }
    
    return e;
  }
  
  private com.bolsinga.web.Record getVenueShowRecord(final Venue venue, final Show show) {
    String dateString = Util.toString(show.getDate());
    return com.bolsinga.web.Record.createRecordList(
      com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getVenueShowListing(venue, show));
  }
  
  private com.bolsinga.web.Record getVenueRecordSection(final Venue venue) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();
    
    if (fLookup.getRelations(venue) != null) {
      items.add(getVenueRelations(venue));
    }

    Collection<Show> shows = fLookup.getShows(venue);
    if (shows != null) {
      for (Show show : shows) {
        items.add(getVenueShowRecord(venue, show));
      }
    }

    A title = com.bolsinga.web.Util.createNamedTarget(venue.getId(), fLookup.getHTMLName(venue));
    
    return com.bolsinga.web.Record.createRecordSection(title, items);
  }

  private com.bolsinga.web.Record getVenueRelations(final Venue venue) {
    Vector<Element> e = new Vector<Element>();
    
    org.apache.ecs.Element curElement = null;
    for (Venue v : fLookup.getRelations(venue)) {
      String htmlName = fLookup.getHTMLName(v);
      if (v.equals(venue)) {
        curElement = new StringElement(htmlName);
        e.add(curElement);
      } else {
        String t = Util.createTitle("moreinfovenue", v.getName());
        e.add(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(v), htmlName, t));
      }
    }

    return com.bolsinga.web.Record.createRecordList(
      new StringElement(com.bolsinga.web.Util.getResourceString("seealso")),
      e, 
      curElement);
  }
}

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
      comment = Web.getLinkedData(fEncoder, show, fUpOneLevel).toString();
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

class TracksRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, com.bolsinga.web.IndexPair> fIndex;
  
  private Vector<Album> fItems;

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final String outputDir) {
    TracksRecordDocumentCreator creator = new TracksRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private TracksRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    for (final Vector<Album> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Album first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new com.bolsinga.web.RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Album item : group) {
                records.add(getAlbumRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return com.bolsinga.web.Util.createPageTitle(curName, com.bolsinga.web.Util.getResourceString("tracks"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public com.bolsinga.web.Navigator getNavigator() {
              return new com.bolsinga.web.Navigator(fLinks) {
                public Element getTrackNavigator() {
                  return com.bolsinga.web.Util.addCurrentIndexNavigator(fIndex, curName, super.getTrackNavigator());
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
        createTracksStats();
      }
    });

    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        createAlbumsStats();
      }
    });
  }
  
  private void createTracksStats() {
    create(new StatsRecordFactory() {
      protected Table getTable() {
        return getTracksStats();
      }
      
      public String getDirectory() {
        return com.bolsinga.web.Links.TRACKS_DIR;
      }

      public String getTitle() {
        Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("track") };
        return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
      }

      public com.bolsinga.web.Navigator getNavigator() {
        return new com.bolsinga.web.Navigator(fLinks) {
          public Element getTrackNavigator() {
            return getCurrentNavigator();
          }
          
          public Element getCurrentNavigator() {
            return new StringElement(com.bolsinga.web.Util.getResourceString("tracks"));
          }
        };
      }
    });
  }
  
  private void createAlbumsStats() {
    create(new StatsRecordFactory() {
      protected Table getTable() {
        return getAlbumsStats();
      }
      
      public String getDirectory() {
        return com.bolsinga.web.Links.TRACKS_DIR;
      }
      
      public String getFilename() {
        return com.bolsinga.web.Links.ALBUM_STATS;
      }

      public String getTitle() {
        Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("album") };
        return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
      }

      public com.bolsinga.web.Navigator getNavigator() {
        return new com.bolsinga.web.Navigator(fLinks) {
          public Element getAlbumNavigator() {
            return getCurrentNavigator();
          }
          
          public Element getCurrentNavigator() {
            return new StringElement(com.bolsinga.web.Util.getResourceString("albums"));
          }
        };
      }
    });
  }

  private java.util.Map<String, com.bolsinga.web.IndexPair> createIndex() {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Album alb : Util.getAlbumsUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(alb);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(fLinks.getLinkToPage(alb), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("albums"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Album>> getGroups() {
    List<Album> albums = Util.getAlbumsCopy(fMusic);
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    HashMap<String, Vector<Album>> result = new HashMap<String, Vector<Album>>(albums.size());
    
    Collections.sort(albums, Compare.ALBUM_COMPARATOR);
    
    for (Album album : albums) {
      String key = fLinks.getPageFileName(album);
      Vector<Album> albumList;
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
  
  private Table getTracksStats() {
    List<Artist> artists = Util.getArtistsCopy(fMusic);
    Collections.sort(artists, Compare.ARTIST_TRACKS_COMPARATOR);

    int index = 0;
    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    
    for (Artist artist : artists) {
      String t = Util.createTitle("moreinfoartist", artist.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t).toString();
      values[index] = Util.trackCount(artist);
                        
      index++;
    }

    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    String tableTitle = com.bolsinga.web.Util.getResourceString("tracksby");

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("trackstatsummary"));
  }
  
  private Table getAlbumsStats() {
    List<Artist> artists = Util.getArtistsCopy(fMusic);
    Collections.sort(artists, Compare.ARTIST_ALBUMS_COMPARATOR);

    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    int index = 0;
    
    for (Artist artist : artists) {
      String t = Util.createTitle("moreinfoartist", artist.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t).toString();
      List<JAXBElement<Object>> albums = Util.getAlbumsUnmodifiable(artist);
      values[index] = (albums != null) ? albums.size() : 0;
                        
      index++;
    }

    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    String tableTitle = com.bolsinga.web.Util.getResourceString("albumsby");

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("albumstatsummary"));
  }
  
  private Vector<Element> getAlbumListing(final Album album) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = null;
    boolean isCompilation = com.bolsinga.web.Util.convert(album.isCompilation());
    com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();

    List<JAXBElement<Object>> songs = Util.getSongsUnmodifiable(album);
    for (JAXBElement<Object> jsong : songs) {
      Song song = (Song)jsong.getValue();
      sb = new StringBuilder();
      if (isCompilation) {
        Artist artist = (Artist)song.getPerformer();
        String t = Util.createTitle("moreinfoartist", artist.getName());
        sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t));
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
      e.add(new StringElement(sb.toString()));
    }
    
    return e;
  }
  
  private Element getAlbumTitle(final Album album) {
    boolean isCompilation = com.bolsinga.web.Util.convert(album.isCompilation());
                
    StringBuilder sb = new StringBuilder();
    sb.append(com.bolsinga.web.Util.createNamedTarget(album.getId(), fLookup.getHTMLName(album)));
    if (!isCompilation) {
      Artist artist = (Artist)album.getPerformer();
      sb.append(" - ");
      String t = Util.createTitle("moreinfoartist", artist.getName());
      sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t));
    }
    com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
    if (albumRelease != null) {
      sb.append(" (");
      sb.append(albumRelease.getYear());
      sb.append(")");
    }
    
    return new StringElement(sb.toString());
  }
  
  private com.bolsinga.web.Record getAlbumRecordSection(final Album album) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
    items.add(com.bolsinga.web.Record.createRecordListOrdered(null, getAlbumListing(album)));
    return com.bolsinga.web.Record.createRecordSection(getAlbumTitle(album), items);
  }
}

class CityRecordDocumentCreator extends MusicRecordDocumentCreator {

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final String outputDir) {
    CityRecordDocumentCreator creator = new CityRecordDocumentCreator(music, outputDir);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private CityRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
  }

  protected void createStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new StatsRecordFactory() {
          protected Table getTable() {
            return getStats();
          }
          
          public String getDirectory() {
            return com.bolsinga.web.Links.CITIES_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("city") };
            return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getCityNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("cities"));
              }
            };
          }
        });
      }
    });
  }
  
  private Table getStats() {
    Collection<String> items = fLookup.getCities();
    HashMap<Integer, Collection<String>> cityCount = new HashMap<Integer, Collection<String>>();
    String city = null;
    int val;
    Collection<String> stringCollection = null;

    for (String item : items) {
      val = fLookup.getShows(item).size();
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

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("citystatsummary"));
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
        
  static Element getLinkedData(final com.bolsinga.web.Encode encoder, final Show show, final boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(show, upOneLevel));
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
      e.add(getLinkedData(encoder, show, upOneLevel));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}
