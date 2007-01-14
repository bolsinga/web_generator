package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBElement;

public class VenueRecordDocumentCreator extends MusicRecordDocumentCreator {

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
