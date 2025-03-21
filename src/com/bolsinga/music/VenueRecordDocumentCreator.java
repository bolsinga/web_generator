package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class VenueRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, IndexPair> fIndex;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final String outputDir) {
    VenueRecordDocumentCreator creator = new VenueRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private VenueRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Venue> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Venue first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Venue item : group) {
                records.add(getVenueRecordSection(item));

                createRedirectDocument(new RedirectFactory() {
                  public String getInternalURL() {
                    return fLinks.getInternalLinkTo(item);
                  }
                  public String getFilePath() {
                    return fLinks.getIdentifierPath(item);
                  }
                  public String getTitle() {
                    Object[] args = { item.getName() };
                    return MessageFormat.format(Util.getResourceString("venuedetail"), args);
                  }
                  public String getDescription() {
                    return Util.createTitle("moreinfo", item.getName());
                   }
                });
              }
              
              return records;
            }
            public String getTitle() {
              return Util.createPageTitle(curName, Util.getResourceString("venues"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public Navigator getNavigator() {
              return new Navigator(fLinks) {
                public Element getVenueNavigator() {
                  return Util.addCurrentIndexNavigator(fIndex, curName, super.getVenueNavigator());
                }
              };
            }
          });
        }
      });
    }
  }

  private void createStats(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new StatsRecordFactory() {
          protected Table getTable() {
            return getStats();
          }

          public String getDirectory() {
            return Links.VENUE_DIR;
          }
          
          public String getTitle() {
            Object typeArgs[] = { Util.getResourceString("venue") };
            return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
          }

          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getVenueNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("venues"));
              }
            };
          }
        });
      }
    });
  }
  
  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Venue v : fMusic.getVenues()) {
      String letter = fLinks.getPageFileName(v);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(v), Util.createPageTitle(letter, Util.getResourceString("venues"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Venue>> getGroups() {
    List<? extends Venue> venues = fMusic.getVenuesCopy();
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
    List<? extends Venue> items = fMusic.getVenuesCopy();
    Collections.sort(items, Compare.getCompare(fMusic).VENUE_STATS_COMPARATOR);

    ArrayList<String> names = new ArrayList<String>(items.size());
    ArrayList<Integer> values = new ArrayList<Integer>(items.size());
    for (Venue item : items) {
      String t = Util.createTitle("moreinfovenue", item.getName());
      names.add(Util.createInternalA(fLinks.getLinkTo(item), fLookup.getHTMLName(item), t).toString());
      Collection<Show> shows = fLookup.getShows(item);
      values.add((shows != null) ? shows.size() : 0);
    }
                
    String typeString = Util.getResourceString("venue");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, Util.getResourceString("venuestatsummary"));
  }
  
  private Vector<Element> getVenueShowListing(final Venue venue, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<? extends Artist> bi = show.getArtists().iterator();
    while (bi.hasNext()) {
      Artist performer = bi.next();
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(Util.createInternalA(fLinks.getLinkTo(performer), fLookup.getHTMLName(performer), t));
      
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
        
    String comment = show.getComment();
    if (comment != null) {
      e.add(Util.createInternalA( fLinks.getLinkTo(show),
                                  Util.getResourceString("showsummary"),
                                  Util.getResourceString("showsummarytitle")));
    }
    
    return e;
  }
  
  private com.bolsinga.web.Record getVenueShowRecord(final Venue venue, final Show show) {
    String dateString = Util.toString(show.getDate());
    return com.bolsinga.web.Record.createRecordList(
      Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getVenueShowListing(venue, show));
  }
  
  private com.bolsinga.web.Record getVenueRecordSection(final Venue venue) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();

    Location l = venue.getLocation();
    items.add(com.bolsinga.web.Record.createRecordSimple(Util.createExternalA(Util.getMapsURL(l), Util.getCannonicalAddress(l))));
    
    if (fLookup.getRelations(venue) != null) {
      items.add(getVenueRelations(venue));
    }

    List<Show> shows = new ArrayList<Show>(fLookup.getShows(venue));
    Collections.sort(shows, Compare.SHOW_COMPARATOR);

    if (shows != null) {
      for (Show show : shows) {
        items.add(getVenueShowRecord(venue, show));
      }
    }

    A title;
    String url = l.getWeb();
    if (url != null) {
      title = Util.createExternalAWithName(url, fLookup.getHTMLName(venue), venue.getID());
    } else {
      title = Util.createNamedTarget(venue.getID(), fLookup.getHTMLName(venue));
    }
    
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
            e.add(Util.createInternalA(fLinks.getLinkTo(v), htmlName, t));
          }
        }

        return com.bolsinga.web.Record.createRecordList(
          new StringElement(Util.getResourceString("seealso")),
          e, 
          curElement);
  }
}
