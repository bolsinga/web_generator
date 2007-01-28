package com.bolsinga.music;

import com.bolsinga.music.data.xml.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBElement;

public class ShowRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, IndexPair> fIndex;

  private final Encode fEncoder;
  private final boolean fUpOneLevel;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final Encode encoder, final String outputDir) {
    ShowRecordDocumentCreator creator = new ShowRecordDocumentCreator(music, outputDir, encoder, true);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
    
  private ShowRecordDocumentCreator(final Music music, final String outputDir, final Encode encoder, final boolean upOneLevel) {
    super(music, outputDir);
    fEncoder = encoder;
    fUpOneLevel = upOneLevel;
    fIndex = createIndex();
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Show> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Show first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new RecordFactory() {
            public Vector<Record> getRecords() {
              Vector<Record> records = new Vector<Record>();
              
              for (Vector<Show> item : getMonthlies(group)) {
                records.add(getShowMonthRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return Util.createPageTitle(curName, Util.getResourceString("dates"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public Navigator getNavigator() {
              return new Navigator(fLinks) {
                public Element getShowNavigator() {
                  return Util.addCurrentIndexNavigator(fIndex, curName, super.getShowNavigator());
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
            return Links.SHOW_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { Util.getResourceString("year") };
            return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
          }

          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getShowNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("dates"));
              }
            };
          }
        });
      }
    });
  }
  
  private Collection<Vector<Show>> getMonthlies(final Vector<Show> items) {
    TreeMap<com.bolsinga.music.data.xml.Date, Vector<Show>> result =
      new TreeMap<com.bolsinga.music.data.xml.Date, Vector<Show>>(Compare.DATE_MONTH_COMPARATOR);

    for (Show item : items) {
      com.bolsinga.music.data.xml.Date key = item.getDate();
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

  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Show s : Util.getShowsUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(s), Util.createPageTitle(letter, Util.getResourceString("dates"))));
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
      IndexPair p = fIndex.get(letter);
      names[i] = Util.createInternalA(p.getLink(), letter, p.getTitle()).toString();
      values[i] = dates.get(item).size();
                        
      i++;
    }
                
    String typeString = Util.getResourceString("year");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, Util.getResourceString("datestatssummary"));
  }
  
  private Record getShowRecord(final Show show) {
    return ShowRecordDocumentCreator.createShowRecord(show, fLinks, fLookup, fEncoder, fUpOneLevel);
  }
  
  // This is used for the main page.
  public static Record createShowRecord(final Show show, final Links links, final Lookup lookup, final Encode encoder, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
                        
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(Util.createInternalA(links.getLinkTo(performer), lookup.getHTMLName(performer), t));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = (Venue)show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = Util.createInternalA(links.getLinkTo(venue), lookup.getHTMLName(venue), t);
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));

    String comment = show.getComment();
    if (comment != null) {
      comment = encoder.embedLinks(show, upOneLevel);
    }
    
    return Record.createRecordListWithComment(Util.createNamedTarget(show.getId(), Util.toString(show.getDate())), e, comment);
  }

  private Record getShowMonthRecordSection(final Vector<Show> shows) {
    Vector<Record> items = new Vector<Record>();

    // Note shows here is a Collection of Shows in a single month
    String m = Util.toMonth(shows.firstElement().getDate());
    A title = Util.createNamedTarget(m, m);
    for (Show show : shows) {
      items.add(getShowRecord(show));
    }

    return Record.createRecordSection(title, items);
  }
}
