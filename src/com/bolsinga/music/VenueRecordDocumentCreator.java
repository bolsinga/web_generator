package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBElement;

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
            public Vector<Record> getRecords() {
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

  protected void createStats(final Backgrounder backgrounder, final Backgroundable backgroundable) {
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
    for (Venue v : Util.getVenuesUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(v);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(v), Util.createPageTitle(letter, Util.getResourceString("venues"))));
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
      names[index] = Util.createInternalA(fLinks.getLinkTo(item), fLookup.getHTMLName(item), t).toString();
      Collection<Show> shows = fLookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = Util.getResourceString("venue");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, Util.getResourceString("venuestatsummary"));
  }
  
  private Vector<Element> getVenueShowListing(final Venue venue, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(Util.createInternalA(fLinks.getLinkTo(performer), fLookup.getHTMLName(performer), t));
      
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
    
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(fLookup.getHTMLName(venue) + ", " + l.getCity() + ", " + l.getState()));
    
    String comment = show.getComment();
    if (comment != null) {
      e.add(Util.createInternalA(fLinks.getLinkTo(show), Util.getResourceString("showsummary"), Util.getResourceString("showsummarytitle")));
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
    
    if (fLookup.getRelations(venue) != null) {
      items.add(getVenueRelations(venue));
    }

    Collection<Show> shows = fLookup.getShows(venue);
    if (shows != null) {
      for (Show show : shows) {
        items.add(getVenueShowRecord(venue, show));
      }
    }

    A title = Util.createNamedTarget(venue.getId(), fLookup.getHTMLName(venue));
    
    return Record.createRecordSection(title, items);
  }

  private Record getVenueRelations(final Venue venue) {
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
