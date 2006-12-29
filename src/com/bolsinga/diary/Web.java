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

class DiaryDocumentCreator extends com.bolsinga.web.MultiDocumentCreator {
  private final com.bolsinga.web.Encode fEncoder;
  private final java.util.Map<String, com.bolsinga.web.IndexPair> fEntryIndex;
  private final int fStartYear;

  // These change during the life-cycle of this object
  private Entry  fCurEntry;
  private Entry  fLastEntry;
        
  public DiaryDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final java.util.Map<String, com.bolsinga.web.IndexPair> entryIndex, final com.bolsinga.web.Encode encoder, final com.bolsinga.web.Links links, final String outputDir, final int startYear) {
    super(backgrounder, links, outputDir);
    fEncoder = encoder;
    fEntryIndex = entryIndex;
    fStartYear = startYear;
  }

  public void add(final Entry entry) {
    fCurEntry = entry;
    add();
    fLastEntry = fCurEntry;
  }
    
  protected String getTitle() {
    return getTitle(com.bolsinga.web.Util.getResourceString("archives"));
  }
    
  protected boolean needNewDocument() {
    return ((fLastEntry == null) || !fLinks.getPageFileName(fLastEntry).equals(getCurrentLetter()));
  }

  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(fStartYear);
  }

  protected com.bolsinga.web.Navigator getNavigator() {
    return new com.bolsinga.web.Navigator(fLinks) {
      public Element getOverviewNavigator() {
        return com.bolsinga.web.Util.addCurrentIndexNavigator(fEntryIndex, getCurrentLetter(), super.getOverviewNavigator());
      }
    };
  }

  protected boolean needNewSubsection() {
    return ((fLastEntry == null) || (fLastEntry.getTimestamp().getMonth() != fCurEntry.getTimestamp().getMonth()));
  }

  protected Element getSubsectionTitle() {
    String m = Util.getMonth(fCurEntry);
    return com.bolsinga.web.Util.createNamedTarget(m, m);
  }

  protected String getLastPath() {
    return fLinks.getPagePath(fLastEntry);
  }
    
  protected String getCurrentLetter() {
    return fLinks.getPageFileName(fCurEntry);
  }

  protected Element getCurrentElement() {
    return Web.addItem(fEncoder, fCurEntry, fLinks, true);
  }
}

class DiarySingleDocumentCreator extends com.bolsinga.web.SingleElementDocumentCreator {
  private final int fStartYear;

  public DiarySingleDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Links links, final String outputDir, final String filename, final String title, final String directory, final com.bolsinga.web.Navigator navigator, final int startYear) {
    super(backgrounder, links, outputDir, filename, title, directory, navigator);
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

/*
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateMainPage(encoder, music, diary, startYear, outputDir);
      }
    });
*/

    final java.util.Map<String, com.bolsinga.web.IndexPair> entryIndex = Web.createEntryIndex(com.bolsinga.diary.Util.getEntriesUnmodifiable(diary), links);
    final Collection<Collection<Entry>> entryGroups = Web.getEntryGroups(diary, links);
    for (final Collection<Entry> entryGroup : entryGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          Web.generateArchivePages(backgrounder, entryGroup, entryIndex, encoder, links, startYear, outputDir);
        }
      });
    }

/*    
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateOverviewPage(diary, entryGroups, entryIndex, links, startYear, outputDir);
      }
    });
*/    

    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateAltPage(backgrounder, diary, links, startYear, outputDir);
      }
    });
  }

/*  
  private static void createHTMLFile(final Document doc, final String filename, final String outputDir) {
    try {
      StringBuffer sb = new StringBuffer();
      sb.append(filename);
      sb.append(com.bolsinga.web.Links.HTML_EXT);
      File f = new File(outputDir, sb.toString());
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("Web cannot mkdirs: " + parent.getAbsolutePath());
        }
      }
      OutputStream os = new FileOutputStream(f);
      doc.output(os);
      os.close();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
*/

/*
  public static void generateMainPage(final com.bolsinga.web.Encode encoder, final Music music, final Diary diary, final int startYear, final String outputDir) {
    com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(false);

    Document doc = createDocument(diary.getTitle(), startYear, links);

    doc.getBody().addElement(generateColumn1(diary.getStatic(), diary.getFriends()));
                
    Div main = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_MAIN);
    Div header = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_HEADER);
    String headerText = diary.getHeader();
    if (headerText != null) {
      header.addElement(com.bolsinga.web.Util.convertToUnOrderedList(headerText));
    }
    main.addElement(header);
    main.addElement(generateDiary(encoder, diary, music, links));
    doc.getBody().addElement(main);
                
    Div mainCol2 = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_COL2);
    mainCol2.addElement(com.bolsinga.music.Web.generatePreview(music));
    doc.getBody().addElement(mainCol2);
    
    Web.createHTMLFile(doc, "index", outputDir);
  }
*/

  private static Div createMainStatics(final String statics) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_OFFSITE);
    d.addElement(new H4(com.bolsinga.web.Util.getSettings().getLinksTitle()));
    if (statics != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(statics));
    }
    return d;
  }

  private static Div createMainLinks(final String friends) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_OFFSITE);
    d.addElement(new H4(com.bolsinga.web.Util.getSettings().getFriendsTitle()));
    if (friends != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(friends));
    }
    return d;
  }

  private static Div generateColumn1(final String statics, final String friends) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_HEADER);
    d.addElement(Web.createMainStatics(statics));
    d.addElement(Web.createMainLinks(friends));
    return d;
  }
        
  private static Element generateDiary(final com.bolsinga.web.Encode encoder, final Diary diary, final Music music, final com.bolsinga.web.Links links) {
    Div diaryDiv = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DOC_SUB);

    int mainPageEntryCount = com.bolsinga.web.Util.getSettings().getDiaryCount().intValue();
    boolean includeMusic = com.bolsinga.web.Util.getSettings().isMainPageHasMusic();
    
    com.bolsinga.music.Lookup lookup = com.bolsinga.music.Lookup.getLookup(music);

    List<Object> items = com.bolsinga.web.Util.getRecentItems(mainPageEntryCount, music, diary, includeMusic);
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
    
    diaryDiv.addElement(new H2().addElement(links.getOverviewLink()));
                
    return diaryDiv;
  }

  private static java.util.Map<String, com.bolsinga.web.IndexPair> createEntryIndex(final Collection<Entry> entries, final com.bolsinga.web.Links links) {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Entry e : entries) {
      String letter = links.getPageFileName(e);
      if (!m.containsKey(letter)) {
        Object[] args = { letter };
        String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoentry"), args);
        m.put(letter, new com.bolsinga.web.IndexPair(links.getLinkToPage(e), t));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private static Collection<Collection<Entry>> getEntryGroups(final Diary diary, final com.bolsinga.web.Links links) {
    List<Entry> entries = com.bolsinga.diary.Util.getEntriesCopy(diary);
    
    // Each group is per page, so they are grouped by Entry who have the same starting sort letter.
    // They are sorted within each group, as they are placed onto the Vector<Entry> in order.
    TreeMap<String, Collection<Entry>> result = new TreeMap<String, Collection<Entry>>();
    
    Collections.sort(entries, Util.ENTRY_COMPARATOR);
    
    for (Entry entry : entries) {
      String key = links.getPageFileName(entry);
      Collection<Entry> entryList;
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

  static Div getHeaderDivWithNavigator(final String title, final String program, final com.bolsinga.web.Links links, final java.util.Map<String, String> entryIndex, final String curLetter) {
    Div d = Web.getHeaderDiv(title, program, links);

    d.addElement(Web.addEntryIndexNavigator(entryIndex, curLetter, links));
    
    return d;
  }
  
  static Div getHeaderDiv(final String title, final String program, final com.bolsinga.web.Links links) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.NAV_HEADER);
    d.addElement(new H1().addElement(title));
    d.addElement(com.bolsinga.web.Util.getLogo());
    
    d.addElement(links.getLinkToHome());
    return d;
  }
  
  static Element getLinkToEntryMonthYear(final String year, final int month, final String value, final java.util.Map<String, String> entryIndex) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, month);
    String monthStr = Util.getMonth(cal);
    
    StringBuilder url = new StringBuilder();
    url.append(entryIndex.get(year));
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
  
  static Element getLinkToEntryYear(final String year, final java.util.Map<String, String> entryIndex) {
    Object[] args = { year };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoentry"), args);
    return com.bolsinga.web.Util.createInternalA(entryIndex.get(year), year, t);
  }
  
  static Element addEntryIndexNavigator(final java.util.Map<String, String> entryIndex, final String curLetter, final com.bolsinga.web.Links links) {
    if (entryIndex == null) {
      return null;
    }
    
    Vector<Element> e = new Vector<Element>();
    org.apache.ecs.Element curElement = null;
    if (curLetter == null) {
      curElement = new StringElement(com.bolsinga.web.Util.getResourceString("archivesoverviewtitle"));
      e.add(curElement);
    } else {
      e.add(links.getOverviewLink());
    }

    for (String s : entryIndex.keySet()) {
      if (s.equals(curLetter)) {
        curElement = new StringElement(s);
        e.add(curElement);
      } else {
        e.add(Web.getLinkToEntryYear(s, entryIndex));
      }
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e, curElement));
    return d;
  }
  
  public static void generateArchivePages(final com.bolsinga.web.Backgrounder backgrounder, final Collection<Entry> items, final java.util.Map<String, com.bolsinga.web.IndexPair> index, final com.bolsinga.web.Encode encoder, final com.bolsinga.web.Links links, final int startYear, final String outputDir) {
    DiaryDocumentCreator creator = new DiaryDocumentCreator(backgrounder, index, encoder, links, outputDir, startYear);
    for (Entry item : items) {
      creator.add(item);
    }
    creator.complete();
  }
  
  private static Table createOverviewTable(final Collection<Collection<Entry>> entryGroups, final java.util.Map<String, String> entryIndex, final int startYear) {
    final String totalStr = com.bolsinga.web.Util.getResourceString("archivestotal");

    // entryGroups has as many items as the number of years. Add one for the final footer total row.
    // There are 12 months in a year, Add two, one for the year, and one for the total column.
    String[][] runningTable = new String[entryGroups.size() + 1][12 + 2];
    int[] monthTotals = new int[12];
    String[] curRow;
    
    int row = 0;
    for (final Collection<Entry> entryGroup : entryGroups) {
      curRow = runningTable[row];
      
      String year = Integer.toString(startYear + row);
      
      curRow[0] = new TD(Web.getLinkToEntryYear(year, entryIndex)).toString();
      
      int[] groupMonthTotals = new int[12];
      for (Entry entry : entryGroup) {
        Calendar cal = entry.getTimestamp().toGregorianCalendar();
        groupMonthTotals[cal.get(Calendar.MONTH) - Calendar.JANUARY]++;
      }
      
      for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
        int val = groupMonthTotals[i - Calendar.JANUARY];
        
        String content = Integer.toString(val);
        if (val != 0) {
          content = Web.getLinkToEntryMonthYear(year, i, content, entryIndex).toString();
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

/*
  public static void generateOverviewPage(final Diary diary, final Collection<Collection<Entry>> entryGroups, final java.util.Map<String, String> entryIndex, final com.bolsinga.web.Links links, final int startYear, final String outputDir) {
    final String docTitle = com.bolsinga.web.Util.getResourceString("archivesoverviewtitle");

    Document doc = Web.createDocument(docTitle, startYear, links);

    Div main = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_MAIN);

    main.addElement(Web.getHeaderDivWithNavigator(docTitle, com.bolsinga.web.Util.getResourceString("program"), links, entryIndex, null));

    main.addElement(Web.createOverviewTable(entryGroups, entryIndex, startYear));

    doc.getBody().addElement(main);

    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(com.bolsinga.web.Links.ARCHIVES_DIR);
    
    Web.createHTMLFile(doc, "overview", sb.toString());
  }
*/

  private static Element generateAltContent(final Diary diary, final com.bolsinga.web.Links links, final String program, final int startYear) {
    // Add data from diary colophon
    UL list = com.bolsinga.web.Util.convertToUnOrderedList(diary.getColophon());
    
    Vector<Element> e = new Vector<Element>();
    
    {
    // Add email contact
    Object[] args = { com.bolsinga.web.Util.getSettings().getContact(), program };
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
    
    {
    // Add date generated
    Object[] args = { new Code(com.bolsinga.web.Util.nowUTC().getTime().toString()).toString() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generatedon"), args)));
    }
    
    // Add the copyright
    e.addElement(new StringElement(com.bolsinga.web.Util.getCopyright(startYear)));

    {
    // RSS
    Object[] args = { links.getRSSLink().toString(), links.getRSSAlt() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("singlespace"), args)));
    }
    
    {
    // iCal
    Object[] args = { links.getICalLink().toString(), links.getICalAlt() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("singlespace"), args)));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.COLOPHON);
    d.addElement(com.bolsinga.web.Util.appendToUnorderedList(list, e));
    return d;
  }

  public static void generateAltPage(final com.bolsinga.web.Backgrounder backgrounder, final Diary diary, final com.bolsinga.web.Links links, final int startYear, final String outputDir) {
    com.bolsinga.web.SingleElementDocumentCreator altPage = new com.bolsinga.web.SingleElementDocumentCreator(backgrounder, links, outputDir, "index", com.bolsinga.web.Util.getResourceString("alttitle"), com.bolsinga.web.Links.ALT_DIR, new com.bolsinga.web.Navigator(links) {
      public Element getColophonNavigator() {
        return getCurrentNavigator();
      }
      
      public Element getCurrentNavigator() {
        return new StringElement(com.bolsinga.web.Util.getResourceString("alttitle"));
      }
    });
    altPage.add(Web.generateAltContent(diary, links, com.bolsinga.web.Util.getResourceString("program"), startYear));
    altPage.complete();
  }

  public static Element addItem(final com.bolsinga.web.Encode encoder, final Entry entry, final com.bolsinga.web.Links links, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    e.add(new H2().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(new H4().addElement(com.bolsinga.web.Util.createPermaLink(links.getLinkTo(entry))));
    e.add(new StringElement(Web.encodedComment(encoder, entry, upOneLevel)));

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  private static String encodedComment(com.bolsinga.web.Encode encoder, Entry entry, boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(entry, upOneLevel));
  }
}
