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
            public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
              Vector<Record> records = new Vector<Record>();
              
              for (Venue item : group) {
                records.add(getVenueRecordSection(item));
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
        final String typeString = Util.getResourceString("venue");
        final Object typeArgs[] = { typeString };
        final List<? extends Venue> items = fMusic.getVenuesCopy();
        create(new DynamicStatsRecordFactory() {
          public String getDirectory() {
            return Links.VENUE_DIR;
          }
          
          public String getTitle() {
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

            protected String getTableTitle() {
                return MessageFormat.format(Util.getResourceString("showsby"), typeArgs);
            }
            
            protected String getTableSummary() {
                return Util.getResourceString("venuestatsummary");
            }
            
            protected String getTableType() {
                return typeString;
            }
            
            protected int getStatsSize() {
                return items.size();
            }

            protected int generateStats(StatsRecordFactory.StatsTracker tracker) throws com.bolsinga.web.WebException {
                Collections.sort(items, Compare.getCompare(fMusic).VENUE_STATS_COMPARATOR);
                
                int total = 0;
                for (Venue item : items) {
                    Collection<Show> shows = fLookup.getShows(item);
                    int value = (shows != null) ? shows.size() : 0;
                    tracker.track(item.getName(), fLinks.getLinkTo(item), value);
                    total += value;
                }
                return total;
            }
            
            protected String getStatsLinkPrefix() {
                return Util.getResourceString("venueprefix");
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
  
  private Record getVenueShowRecord(final Venue venue, final Show show) {
    String dateString = Util.toString(show.getDate());
    return Record.createRecordList(
      Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getVenueShowListing(venue, show));
  }
  
  private Record getVenueRecordSection(final Venue venue) {
    Vector<Record> items = new Vector<Record>();

    Location l = venue.getLocation();
    items.add(Record.createRecordSimple(Util.createExternalA(Util.getGoogleMapsURL(l), Util.getCannonicalAddress(l))));
    
    if (fLookup.getRelations(venue) != null) {
      items.add(getVenueRelations(venue));
    }

    Collection<Show> shows = fLookup.getShows(venue);
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
    
    return Record.createRecordSection(title, items);
  }

  private Record getVenueRelations(final Venue venue) {
    if (Util.getSettings().isRelatedUsesPopup()) {
        Vector<String> labels = new Vector<String>();
        Vector<String> values = new Vector<String>();
        
        for (Venue v : fLookup.getRelations(venue)) {
          if (!v.equals(venue)) {
            labels.add(fLookup.getHTMLName(v));
            values.add(fLinks.getLinkTo(v));
          }
        }
        
        return Record.createRecordPopup(Util.getResourceString("seealso"), labels, values);
    } else {
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

        return Record.createRecordList(
          new StringElement(Util.getResourceString("seealso")),
          e, 
          curElement);
    }
  }
}
