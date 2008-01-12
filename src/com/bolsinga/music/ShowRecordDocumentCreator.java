package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class ShowRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final Encode fEncoder;
  private final java.util.Map<String, IndexPair> fIndex;
  private final Collection<Vector<Show>> fGroups;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final Encode encoder, final String outputDir) {
    ShowRecordDocumentCreator creator = new ShowRecordDocumentCreator(music, outputDir, encoder);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
    
  private ShowRecordDocumentCreator(final Music music, final String outputDir, final Encode encoder) {
    super(music, outputDir);
    fEncoder = encoder;
    fIndex = createIndex();
    fGroups = createGroups();
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Show> group : fGroups) {
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

  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Show s : fMusic.getShows()) {
      String letter = fLinks.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(s), Util.createPageTitle(letter, Util.getResourceString("dates"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Show>> createGroups() {
    List<Show> shows = fMusic.getShowsCopy();
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    TreeMap<String, Vector<Show>> result = new TreeMap<String, Vector<Show>>();
    
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
    // fGroups has as many items as the number of years (inc unknown). Add one for the final footer total row.
    // There are 12 months in a year, Add three, one for the year, one for unknonwn month, and one for the total column.
    // 12 months + 3 (year column, other, total)
    String[][] runningTable = new String[fGroups.size() + 1][12 + 3];
    int[] monthTotals = new int[13];  // including unknown as index '12'
    String[] curRow;
    
    int row = 0;
    for (final Vector<Show> showGroup : fGroups) {
      curRow = runningTable[row];

      String year = fLinks.getPageFileName(showGroup.firstElement());
      
      IndexPair p = fIndex.get(year);
      curRow[0] = new TD(Util.createInternalA(p.getLink(), year, p.getTitle())).toString();
      
      int[] groupMonthTotals = new int[13]; // including unknown as index '12'
      for (Show show : showGroup) {
        com.bolsinga.music.data.Date date = show.getDate();
        if (!date.isUnknown()) {
          Calendar cal = Util.toCalendarLocal(date); // don't want UTC...
          groupMonthTotals[cal.get(Calendar.MONTH) - Calendar.JANUARY]++;
        } else {
          // See if the month is known.
          int month = date.getMonth();
          if (month != com.bolsinga.music.data.Date.UNKNOWN) {
            groupMonthTotals[month - 1]++;
          } else {
            groupMonthTotals[12]++;
          }
        }
      }
      
      for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER + 1; i++) {
        int val = groupMonthTotals[i - Calendar.JANUARY];
        
        String content = Integer.toString(val);
        if (val != 0) {
          content = getLinkToShowMonthYear(year, i, content).toString();
        }
        curRow[(i - Calendar.JANUARY) + 1] = new TD(content).toString();
        
        monthTotals[i - Calendar.JANUARY] += val;
      }
      
      curRow[14] = new TD(Integer.toString(showGroup.size())).toString();
      
      row++;
    }
    
    final String totalStr = Util.getResourceString("archivestotal");
    curRow = runningTable[runningTable.length - 1];
    curRow[0] = new TH(totalStr).toString();
    
    for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER + 1; i++) {
      curRow[(i - Calendar.JANUARY) + 1] = new TH(Integer.toString(monthTotals[i - Calendar.JANUARY])).toString();
    }
    int totalEntries = 0;
    for (int cnt : monthTotals) {
      totalEntries += cnt;
    }
    curRow[14] = new TH(Integer.toString(totalEntries)).toString();
    
    final String[][] table = runningTable;
    return Util.makeTable(Util.getResourceString("datestats"),
                          Util.getResourceString("datestatssummary"), 
                          new TableHandler() {
      public TR getHeaderRow() {
        TR trow = new TR().addElement(new TH());
        Calendar cal = Calendar.getInstance();
        for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
          cal.set(Calendar.DAY_OF_MONTH, 1);
          cal.set(Calendar.MONTH, i);
          trow.addElement(new TH(Util.getShortMonthName(cal)));
        }
        trow.addElement(new TH(Util.getResourceString("unknownmonthshort")));
        trow.addElement(new TH(totalStr));
        return trow;
      }
      
      public int getRowCount() {
        // Last 'row' in table is totals row.
        return table.length - 1;
      }
      
      private TR fillRow(final String[] rowData) {
        TR trow = new TR();
        for (int i = 0; i < rowData.length; i++) {
          trow.addElement(rowData[i]);
        }
        return trow;
      }
      
      public TR getRow(final int row) {
        String[] curRow = table[row];
        return fillRow(curRow);
      }
      
      public TR getFooterRow() {
        String[] curRow = table[getRowCount()];
        return fillRow(curRow);
      }
    });
  }

  private Record getShowRecord(final Show show) {
    return ShowRecordDocumentCreator.createShowRecord(show, fLinks, fLookup, fEncoder, true);
  }
  
  // This is used for the main page.
  public static Record createShowRecord(final Show show, final Links links, final Lookup lookup, final Encode encoder, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<? extends Artist> bi = show.getArtists().iterator();
    while (bi.hasNext()) {
      Artist performer = bi.next();
                        
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
    
    return Record.createRecordListWithComment(Util.createNamedTarget(show.getID(), Util.toString(show.getDate())), e, comment);
  }

  private Record getShowMonthRecordSection(final Vector<Show> shows) {
    Vector<Record> items = new Vector<Record>();

    // Note shows here is a Collection of Shows in a single month
    String name;
    com.bolsinga.music.data.Date date = shows.firstElement().getDate();
    String value = Util.toMonth(date);
    if (!date.isUnknown() || (date.getMonth() != com.bolsinga.music.data.Date.UNKNOWN)) {
      name = value;
    } else {
      name = Util.getResourceString("unknownmonthshort");
    }
    A title = Util.createNamedTarget(name, value);
    for (Show show : shows) {
      items.add(getShowRecord(show));
    }

    return Record.createRecordSection(title, items);
  }
  
  private Element getLinkToShowMonthYear(final String year, final int month, final String value) {
    String monthStr, monthStrHash;
    if (month <= Calendar.DECEMBER) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.MONTH, month);
      monthStr = Util.getMonth(cal);
      monthStrHash = monthStr;
    } else {
      monthStr = Util.getResourceString("unknownmonth");
      monthStrHash = Util.getResourceString("unknownmonthshort");
    }
    
    StringBuilder url = new StringBuilder();
    url.append(fIndex.get(year).getLink());
    url.append(Links.HASH);
    url.append(monthStrHash);
    
    StringBuilder tip = new StringBuilder();
    tip.append(monthStr);
    tip.append(", ");
    tip.append(year);
    
    Object[] args = { tip.toString() };
    String t = MessageFormat.format(Util.getResourceString("moreinfoshow"), args);
    
    return Util.createInternalA(url.toString(), value, t);
  }
}
