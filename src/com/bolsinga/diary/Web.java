package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

abstract class DiaryRecordDocumentCreator extends com.bolsinga.web.RecordDocumentCreator {

  protected final Diary fDiary;
  protected final int fStartYear;
  
  public DiaryRecordDocumentCreator(final Diary diary, final String outputDir, final boolean upOneLevel) {
    super(com.bolsinga.web.Links.getLinks(upOneLevel), outputDir);
    fDiary = diary;
    fStartYear = Util.getStartYear(fDiary);
  }
  
  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(fStartYear);
  }
  
  protected String getMainDivClass() {
    return com.bolsinga.web.CSS.DOC_2_COL_BODY;
  }
}

class EntryRecordDocumentCreator extends DiaryRecordDocumentCreator {

  private final com.bolsinga.web.Encode fEncoder;
  private final java.util.Map<String, com.bolsinga.web.IndexPair> fIndex;
  private final Collection<Vector<Entry>> fGroups;
  
  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final String outputDir, final com.bolsinga.web.Encode encoder) {
    EntryRecordDocumentCreator creator = new EntryRecordDocumentCreator(diary, outputDir, encoder);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private EntryRecordDocumentCreator(final Diary diary, final String outputDir, final com.bolsinga.web.Encode encoder) {
    super(diary, outputDir, true);
    fEncoder = encoder;
    fIndex = createIndex();
    fGroups = createGroups();
  }

  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    for (final Vector<Entry> group : fGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Entry first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new com.bolsinga.web.RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Vector<Entry> item : getMonthlies(group)) {
                records.add(getEntryMonthRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return com.bolsinga.web.Util.createPageTitle(curName, com.bolsinga.web.Util.getResourceString("archives"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public com.bolsinga.web.Navigator getNavigator() {
              return new com.bolsinga.web.Navigator(fLinks) {
                public Element getOverviewNavigator() {
                  return com.bolsinga.web.Util.addCurrentIndexNavigator(fIndex, curName, super.getOverviewNavigator());
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
      Calendar key = item.getTimestamp().toGregorianCalendar();
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

  private com.bolsinga.web.Record getEntryMonthRecordSection(final Vector<Entry> entries) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();
    
    // Note entries here is a Collection of Entrys in a single month
    String m = Util.getMonth(entries.firstElement());
    A title = com.bolsinga.web.Util.createNamedTarget(m, m);
    for (Entry entry : entries) {
      items.add(getEntryRecord(entry));
    }

    return com.bolsinga.web.Record.createRecordSection(title, items);
  }
  
  private com.bolsinga.web.Record getEntryRecord(final Entry entry) {
    return com.bolsinga.web.Record.createRecordPermalink(
      com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry)), 
      fEncoder.embedLinks(entry, true),
      com.bolsinga.web.Util.createPermaLink(fLinks.getLinkTo(entry)));
  }

  protected void createStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new com.bolsinga.web.RecordFactory() {
          public Vector<com.bolsinga.web.Record> getRecords() {
            Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
            items.add(com.bolsinga.web.Record.createRecordSimple(getStats()));
            return items;
          }

          public String getTitle() {
            return com.bolsinga.web.Util.getResourceString("archivesoverviewtitle");
          }
          
          public String getFilePath() {
            StringBuilder sb = new StringBuilder();
            sb.append(com.bolsinga.web.Links.ARCHIVES_DIR);
            sb.append(File.separator);
            sb.append("overview");
            sb.append(com.bolsinga.web.Links.HTML_EXT);
            return sb.toString();
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getOverviewNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("archivesoverviewtitle"));
              }
            };
          }
        });
      }
    });
  }

  private java.util.Map<String, com.bolsinga.web.IndexPair> createIndex() {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Entry e : Util.getEntriesUnmodifiable(fDiary)) {
      String letter = fLinks.getPageFileName(e);
      if (!m.containsKey(letter)) {
        Object[] args = { letter };
        String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoentry"), args);
        m.put(letter, new com.bolsinga.web.IndexPair(fLinks.getLinkToPage(e), t));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Entry>> createGroups() {
    List<Entry> entries = Util.getEntriesCopy(fDiary);
    
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
    final String totalStr = com.bolsinga.web.Util.getResourceString("archivestotal");

    // fGroups has as many items as the number of years. Add one for the final footer total row.
    // There are 12 months in a year, Add two, one for the year, and one for the total column.
    String[][] runningTable = new String[fGroups.size() + 1][12 + 2];
    int[] monthTotals = new int[12];
    String[] curRow;
    
    int row = 0;
    for (final Vector<Entry> entryGroup : fGroups) {
      curRow = runningTable[row];
      
      String year = Integer.toString(fStartYear + row);
      
      com.bolsinga.web.IndexPair p = fIndex.get(year);
      curRow[0] = new TD(com.bolsinga.web.Util.createInternalA(p.getLink(), year, p.getTitle())).toString();
      
      int[] groupMonthTotals = new int[12];
      for (Entry entry : entryGroup) {
        Calendar cal = entry.getTimestamp().toGregorianCalendar();
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
    
    return com.bolsinga.web.Util.makeTable( com.bolsinga.web.Util.getResourceString("archivesoverview"),
                                            com.bolsinga.web.Util.getResourceString("archivesoverviewsummary"), 
                                            new com.bolsinga.web.TableHandler() {
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
    url.append(com.bolsinga.web.Links.HASH);
    url.append(monthStr);
    
    StringBuilder tip = new StringBuilder();
    tip.append(monthStr);
    tip.append(", ");
    tip.append(year);
    
    Object[] args = { tip.toString() };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoentry"), args);
    
    return com.bolsinga.web.Util.createInternalA(url.toString(), value, t);
  }
}

class SingleRecordDocumentCreator extends DiaryRecordDocumentCreator {

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final String outputDir) {
    SingleRecordDocumentCreator creator = new SingleRecordDocumentCreator(diary, outputDir);
    creator.createAlt(backgrounder, backgroundable);
  }
  
  private SingleRecordDocumentCreator(final Diary diary, final String outputDir) {
    super(diary, outputDir, true);
  }

  protected void createAlt(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new com.bolsinga.web.RecordFactory() {
          public Vector<com.bolsinga.web.Record> getRecords() {
            Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
            items.add(com.bolsinga.web.Record.createRecordSimple(getAlt()));
            return items;
          }
          
          public String getTitle() {
            return com.bolsinga.web.Util.getResourceString("alttitle");
          }
          
          public String getFilePath() {
            StringBuilder sb = new StringBuilder();
            sb.append(com.bolsinga.web.Links.ALT_DIR);
            sb.append(File.separator);
            sb.append("index");
            sb.append(com.bolsinga.web.Links.HTML_EXT);
            return sb.toString();
          }
          
          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getColophonNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("alttitle"));
              }
            };
          }
        });
      }
    });
  }

  private Element getAlt() {
    // Add data from diary colophon
    UL list = com.bolsinga.web.Util.convertToUnOrderedList(fDiary.getColophon());
    
    Vector<Element> e = new Vector<Element>();
    
    {
    // Add email contact
    Object[] args = { com.bolsinga.web.Util.getSettings().getContact(), com.bolsinga.web.Util.getResourceString("program") };
    if (com.bolsinga.web.Util.getDebugOutput()) {
      args[1] = null;
    }
    e.addElement(new A( MessageFormat.format(com.bolsinga.web.Util.getResourceString("mailto"), args),
                        com.bolsinga.web.Util.getResourceString("contact"))); // mailto: URL
    }
    
    {
    // Add the name of the program
    Object[] args = { new Code(com.bolsinga.web.Util.getGenerator()).toString() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generatedby"), args)));
    }
    
    if (!com.bolsinga.web.Util.getDebugOutput()) {
      // Add date generated
      Object[] args = { new Code(com.bolsinga.web.Util.nowUTC().getTime().toString()).toString() };
      e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generatedon"), args)));
    }
    
    // Add the copyright
    e.addElement(new StringElement(getCopyright()));

    {
    // RSS
    Object[] args = { fLinks.getRSSLink().toString(), fLinks.getRSSAlt() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("singlespace"), args)));
    }
    
    {
    // iCal
    Object[] args = { fLinks.getICalLink().toString(), fLinks.getICalAlt() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("singlespace"), args)));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.COLOPHON);
    d.addElement(com.bolsinga.web.Util.appendToUnorderedList(list, e));
    return d;
  }
}

class DiarySingleDocumentCreator extends com.bolsinga.web.SingleElementDocumentCreator {
  private final int fStartYear;

  public DiarySingleDocumentCreator(final com.bolsinga.web.Links links, final String outputDir, final String filename, final String title, final String directory, final com.bolsinga.web.Navigator navigator, final int startYear) {
    super(links, outputDir, filename, title, directory, navigator);
    fStartYear = startYear;
  }
  
  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(fStartYear);
  }
}

public class Web implements com.bolsinga.web.Backgroundable {

  private static final boolean GENERATE_XML = false;

  private final com.bolsinga.web.Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 5) {
      Web.usage();
    }

    String settings = args[3];
    String output = args[4];

    Diary diary = null;
    Music music = null;

    if (args[0].equals("xml")) {
      String diaryFile = args[1];
      String musicFile = args[2];
      
      diary = Util.createDiary(diaryFile);
      music = com.bolsinga.music.Util.createMusic(musicFile);
    } else if (args[0].equals("db")) {
      String user = args[1];
      String password = args[2];

      diary = MySQLCreator.createDiary(user, password);
      music = com.bolsinga.music.MySQLCreator.createMusic(user, password);
    } else {
      Web.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);
    
    if (Web.GENERATE_XML) {
      Web.export(diary);
      System.exit(0);
    }

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, output);
    web.complete();
  }
  
  Web(final com.bolsinga.web.Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    backgrounder.addInterest(this);
  }
  
  void complete() {
    fBackgrounder.removeInterest(this);
  }

  private static void usage() {
    System.out.println("Usage: Web xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }

  private static void export(final Diary diary) {
    try {
      File outputFile = new File("/tmp", "diary_db.xml");

      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data");
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
      m.marshal(diary, os);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void generate(final String sourceFile, final String musicFile, final String outputDir) {
    Diary diary = Util.createDiary(sourceFile);

    generate(diary, musicFile, outputDir);
  }
        
  public static void generate(final Diary diary, final String musicFile, final String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, outputDir);
    web.complete();
  }
        
  public void generate(final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, this, diary, music, encoder, outputDir);
  }

  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    final int startYear = Util.getStartYear(diary);
    final com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(true);

    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateMainPage(encoder, music, diary, startYear, outputDir);
      }
    });

    EntryRecordDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder);

    SingleRecordDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir);
  }

  private static Element generateMainContent(final Diary diary, final Music music, final com.bolsinga.web.Links links, final com.bolsinga.web.Encode encoder) {
    ElementContainer ec = new ElementContainer();
    ec.addElement(Web.generateStaticHeader(diary));
    ec.addElement(com.bolsinga.web.Util.convertToUnOrderedList(diary.getHeader()));
    ec.addElement(Web.generateDiary(encoder, diary, music, links));
    return ec;
  }
  
  public static void generateMainPage(final com.bolsinga.web.Encode encoder, final Music music, final Diary diary, final int startYear, final String outputDir) {
    com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(false);

    com.bolsinga.web.SingleElementDocumentCreator page = new DiarySingleDocumentCreator(links, outputDir, "index", diary.getTitle(), null, com.bolsinga.music.Web.getMainPagePreviewNavigator(music, links), startYear) {
      protected boolean isTwoColumn() {
        return false;
      }
    };
    page.add(Web.generateMainContent(diary, music, links, encoder));
    page.complete();
  }

  private static Div createStaticsOffsite(final String title, final String data) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_OFFSITE);
    d.addElement(new H4(title));
    if (data != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(data));
    }
    return d;
  }
  
  private static Div generateStaticHeader(final Diary diary) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_HEADER);
    d.addElement(Web.createStaticsOffsite(com.bolsinga.web.Util.getSettings().getLinksTitle(), diary.getStatic()));
    d.addElement(Web.createStaticsOffsite(com.bolsinga.web.Util.getSettings().getFriendsTitle(), diary.getFriends()));
    return d;
  }
        
  private static Element generateDiary(final com.bolsinga.web.Encode encoder, final Diary diary, final Music music, final com.bolsinga.web.Links links) {
    Div diaryDiv = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DOC_SUB);

    int mainPageEntryCount = com.bolsinga.web.Util.getSettings().getDiaryCount().intValue();
    
    com.bolsinga.music.Lookup lookup = com.bolsinga.music.Lookup.getLookup(music);

    List<Object> items = com.bolsinga.web.Util.getRecentItems(mainPageEntryCount, music, diary);
    for (Object o : items) {
      if (o instanceof com.bolsinga.diary.data.Entry) {
        diaryDiv.addElement(Web.addItem(encoder, (com.bolsinga.diary.data.Entry)o, links, false));
      } else if (o instanceof com.bolsinga.music.data.Show) {
        // This appears at the top level
        diaryDiv.addElement(com.bolsinga.music.Web.addItem(encoder, lookup, links, (com.bolsinga.music.data.Show)o, false));
      } else {
        System.err.println("Unknown recent item." + o.toString());
        Thread.dumpStack();
        System.exit(1);
      }
    }
                
    return diaryDiv;
  }

  public static Element addItem(final com.bolsinga.web.Encode encoder, final Entry entry, final com.bolsinga.web.Links links, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    e.add(new H3().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(com.bolsinga.web.Util.createPermaLink(links.getLinkTo(entry)));
    e.add(new StringElement(com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(entry, upOneLevel))));

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}
