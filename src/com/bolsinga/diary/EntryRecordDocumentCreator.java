package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import com.bolsinga.web.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class EntryRecordDocumentCreator extends DiaryEncoderRecordDocumentCreator {

  private final java.util.Map<String, IndexPair> fIndex;
  private final Collection<Vector<Entry>> fGroups;
  
  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Diary diary, final String outputDir, final Encode encoder) {
    EntryRecordDocumentCreator creator = new EntryRecordDocumentCreator(diary, outputDir, encoder);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private EntryRecordDocumentCreator(final Diary diary, final String outputDir, final Encode encoder) {
    super(diary, outputDir, true, encoder);
    fIndex = createIndex();
    fGroups = createGroups();
  }

  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Entry> group : fGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Entry first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new RecordFactory() {
            public Vector<Record> getRecords() {
              Vector<Record> records = new Vector<Record>();
              
              for (Vector<Entry> item : getMonthlies(group)) {
                records.add(getEntryMonthRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return Util.createPageTitle(curName, Util.getResourceString("archives"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public Navigator getNavigator() {
              return new Navigator(fLinks) {
                public Element getOverviewNavigator() {
                  return Util.addCurrentIndexNavigator(fIndex, curName, super.getOverviewNavigator());
                }
              };
            }
          });
        }
      });
    }
  }

  private Collection<Vector<Entry>> getMonthlies(final Vector<Entry> items) {
    TreeMap<Calendar, Vector<Entry>> result = new TreeMap<Calendar, Vector<Entry>>(Util.MONTH_COMPARATOR);

    for (Entry item : items) {
      Calendar key = item.getTimestamp();
      Vector<Entry> list;
      if (result.containsKey(key)) {
        list = result.get(key);
        list.add(item);
      } else {
        list = new Vector<Entry>();
        list.add(item);
        result.put(key, list);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private Record getEntryMonthRecordSection(final Vector<Entry> entries) {
    Vector<Record> items = new Vector<Record>();
    
    // Note entries here is a Collection of Entrys in a single month
    String m = Util.getMonth(entries.firstElement());
    A title = Util.createNamedTarget(m, m);
    for (Entry entry : entries) {
      items.add(getEntryRecord(entry));
    }

    return Record.createRecordSection(title, items);
  }
  
  private Record getEntryRecord(final Entry entry) {
    return EntryRecordDocumentCreator.createEntryRecord(entry, fLinks, fEncoder, true);
  }
  
  // This is used for the main page and entry pages, which is why it is public static
  public static Record createEntryRecord(final Entry entry, final Links links, final Encode encoder, final boolean upOneLevel) {
    String title = entry.getTitle();
    if (title == null) {
        title = Util.getDisplayTitle(entry);
    } else {
        Object[] args = { title, Util.getTimestamp(entry) };
        title = MessageFormat.format(Util.getResourceString("entrytitle"), args);
    }
    return Record.createRecordPermalink(
      Util.createNamedTarget(entry.getID(), title), 
      encoder.embedLinks(entry, upOneLevel),
      Util.createPermaLink(links.getLinkTo(entry)));
  }

  protected void createStats(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new RecordFactory() {
          public Vector<Record> getRecords() {
            Vector<Record> items = new Vector<Record>(1);
            items.add(Record.createRecordSimple(getStats()));
            return items;
          }

          public String getTitle() {
            return Util.getResourceString("archivesoverviewtitle");
          }
          
          public String getFilePath() {
            StringBuilder sb = new StringBuilder();
            sb.append(Links.ARCHIVES_DIR);
            sb.append(File.separator);
            sb.append("overview");
            sb.append(Links.HTML_EXT);
            return sb.toString();
          }

          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getOverviewNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("archivesoverviewtitle"));
              }
            };
          }
        });
      }
    });
  }

  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Entry e : fDiary.getEntries()) {
      String letter = fLinks.getPageFileName(e);
      if (!m.containsKey(letter)) {
        Object[] args = { letter };
        String t = MessageFormat.format(Util.getResourceString("moreinfoentry"), args);
        m.put(letter, new IndexPair(fLinks.getLinkToPage(e), t));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Entry>> createGroups() {
    List<? extends Entry> entries = fDiary.getEntriesCopy();
    
    // Each group is per page, so they are grouped by Entry who have the same starting sort letter.
    // They are sorted within each group, as they are placed onto the Vector<Entry> in order.
    TreeMap<String, Vector<Entry>> result = new TreeMap<String, Vector<Entry>>();
    
    Collections.sort(entries, Util.ENTRY_COMPARATOR);
    
    for (Entry entry : entries) {
      String key = fLinks.getPageFileName(entry);
      Vector<Entry> entryList;
      if (result.containsKey(key)) {
        entryList = result.get(key);
        entryList.add(entry);
      } else {
        entryList = new Vector<Entry>();
        entryList.add(entry);
        result.put(key, entryList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }
  
  private Table getStats() {
    final String totalStr = Util.getResourceString("archivestotal");

    // fGroups has as many items as the number of years. Add one for the final footer total row.
    // There are 12 months in a year, Add two, one for the year, and one for the total column.
    String[][] runningTable = new String[fGroups.size() + 1][12 + 2];
    int[] monthTotals = new int[12];
    String[] curRow;
    
    int row = 0;
    for (final Vector<Entry> entryGroup : fGroups) {
      curRow = runningTable[row];
      
      String year = Integer.toString(fStartYear + row);
      
      IndexPair p = fIndex.get(year);
      curRow[0] = new TD(Util.createInternalA(p.getLink(), year, p.getTitle())).toString();
      
      int[] groupMonthTotals = new int[12];
      for (Entry entry : entryGroup) {
        Calendar cal = entry.getTimestamp();
        groupMonthTotals[cal.get(Calendar.MONTH) - Calendar.JANUARY]++;
      }
      
      for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
        int val = groupMonthTotals[i - Calendar.JANUARY];
        
        String content = Integer.toString(val);
        if (val != 0) {
          content = getLinkToEntryMonthYear(year, i, content).toString();
        }
        curRow[(i - Calendar.JANUARY) + 1] = new TD(content).toString();
        
        monthTotals[i - Calendar.JANUARY] += val;
      }
      
      curRow[13] = new TD(Integer.toString(entryGroup.size())).toString();
      
      row++;
    }
    
    curRow = runningTable[runningTable.length - 1];
    curRow[0] = new TH(totalStr).toString();
    
    for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
      curRow[(i - Calendar.JANUARY) + 1] = new TH(Integer.toString(monthTotals[i - Calendar.JANUARY])).toString();
    }
    int totalEntries = 0;
    for (int cnt : monthTotals) {
      totalEntries += cnt;
    }
    curRow[13] = new TH(Integer.toString(totalEntries)).toString();
    
    final String[][] table = runningTable;
    
    return Util.makeTable( Util.getResourceString("archivesoverview"),
                                            Util.getResourceString("archivesoverviewsummary"), 
                                            new TableHandler() {
      public TR getHeaderRow() {
        TR trow = new TR().addElement(new TH());
        Calendar cal = Calendar.getInstance();
        for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
          cal.set(Calendar.DAY_OF_MONTH, 1);
          cal.set(Calendar.MONTH, i);
          trow.addElement(new TH(Util.getShortMonthName(cal)));
        }
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
  
  private Element getLinkToEntryMonthYear(final String year, final int month, final String value) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.MONTH, month);
    String monthStr = Util.getMonth(cal);
    
    StringBuilder url = new StringBuilder();
    url.append(fIndex.get(year).getLink());
    url.append(Links.HASH);
    url.append(monthStr);
    
    StringBuilder tip = new StringBuilder();
    tip.append(monthStr);
    tip.append(", ");
    tip.append(year);
    
    Object[] args = { tip.toString() };
    String t = MessageFormat.format(Util.getResourceString("moreinfoentry"), args);
    
    return Util.createInternalA(url.toString(), value, t);
  }
}
