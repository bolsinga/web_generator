package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class ShowRecordDocumentCreator extends MusicRecordDocumentCreator {

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
