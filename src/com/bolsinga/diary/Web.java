package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

class DiaryDocumentCreator extends com.bolsinga.web.MultiDocumentCreator {
  private final com.bolsinga.web.Encode fEncoder;
  private final Map<String, String>  fEntryIndex;
  private final Links  fLinks;
  private final String fProgram;
  private final int fStartYear;

  // These change during the life-cycle of this object
  private Entry  fCurEntry;
  private Entry  fLastEntry;
        
  public DiaryDocumentCreator(final com.bolsinga.web.Backgrounder backgrounder, final Map<String, String> entryIndex, final com.bolsinga.web.Encode encoder, final Links links, final String outputDir, final String program, final int startYear) {
    super(backgrounder, outputDir);
    fEncoder = encoder;
    fEntryIndex = entryIndex;
    fLinks = links;
    fProgram = program;
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

  protected XhtmlDocument createDocument() {
    return Web.createDocument(getTitle(), fStartYear, fLinks);
  }

  protected div getHeaderDiv() {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DIARY_HEADER);
    d.addElement(new h1().addElement(getTitle()));
    d.addElement(com.bolsinga.web.Util.getLogo());
    d.addElement(addWebNavigator(fProgram, fLinks));
    d.addElement(addIndexNavigator());
    return d;
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

  protected Element addIndexNavigator() {
    Vector<Element> e = new Vector<Element>();
    for (String s : fEntryIndex.keySet()) {
      if (s.equals(getCurrentLetter())) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA(fEntryIndex.get(s), s));
      }
    }
    e.add(fLinks.getRSSLink());

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DIARY_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  private Element addWebNavigator(final String program, final Links links) {
    Vector<Element> e = new Vector<Element>();
                
    Object[] args2 = { com.bolsinga.web.Util.getSettings().getContact(), program };
    e.add(new a(MessageFormat.format(com.bolsinga.web.Util.getResourceString("mailto"), args2), com.bolsinga.web.Util.getResourceString("contact"))); // mailto: URL
    e.add(links.getLinkToHome());

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DIARY_MENU);
    Object[] args = { Calendar.getInstance().getTime() }; // LocalTime OK
    d.addElement(new h4(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generated"), args)));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
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

      diary = Util.createDiary(user, password);
      music = com.bolsinga.music.Util.createMusic(user, password);
    } else {
      Web.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);
    
    if (Web.GENERATE_XML) {
      Web.export(diary);
      System.exit(0);
    }

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(backgrounder, music, diary);                
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
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(backgrounder, music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, outputDir);
    web.complete();
  }
        
  public void generate(final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, this, diary, music, encoder, outputDir);
  }

  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    final int startYear = Util.getStartYear(diary);
    final Links links = Links.getLinks(true);

    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        Web.generateMainPage(encoder, music, diary, startYear, outputDir);
      }
    });

    final Map<String, String> entryIndex = Web.createEntryIndex(diary.getEntry(), links);
    Collection<Collection<Entry>> entryGroups = Web.getEntryGroups(diary, links);
    for (final Collection<Entry> entryGroup : entryGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          Web.generateArchivePages(backgrounder, entryGroup, entryIndex, encoder, links, startYear, outputDir);
        }
      });
    }
  }

  public static void generateMainPage(final com.bolsinga.web.Encode encoder, final Music music, final Diary diary, final int startYear, final String outputDir) {
    Links links = Links.getLinks(false);

    XhtmlDocument doc = createDocument(diary.getTitle(), startYear, links);

    doc.getBody().addElement(generateColumn1(diary.getStatic(), diary.getFriends()));
                
    div main = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_MAIN);
    div header = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_HEADER);
    String headerText = diary.getHeader();
    if (headerText != null) {
      header.addElement(com.bolsinga.web.Util.convertToUnOrderedList(headerText));
    }
    main.addElement(header);
    main.addElement(generateDiary(encoder, diary, music, links));
    doc.getBody().addElement(main);
                
    div mainCol2 = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_COL2);
    int previewCount = com.bolsinga.web.Util.getSettings().getPreviewCount().intValue();
    mainCol2.addElement(com.bolsinga.music.Web.generatePreview(music, previewCount));
    doc.getBody().addElement(mainCol2);
                
    try {
      File f = new File(outputDir, "index.html");
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

  private static div createMainStatics(final String statics) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_STATIC);
    d.addElement(new h4(com.bolsinga.web.Util.getSettings().getLinksTitle()));
    if (statics != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(statics));
    }
    return d;
  }

  private static div createMainLinks(final String friends) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_LINKS);
    d.addElement(new h4(com.bolsinga.web.Util.getSettings().getFriendsTitle()));
    if (friends != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(friends));
    }
    return d;
  }

  private static div generateColumn1(final String statics, final String friends) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_COL1);
    d.addElement(Web.createMainStatics(statics));
    d.addElement(Web.createMainLinks(friends));
    return d;
  }
        
  public static XhtmlDocument createDocument(final String title, final int startYear, final Links links) {
    XhtmlDocument d = new XhtmlDocument(ECSDefaults.getDefaultCodeset());
                
    d.getHtml().setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
                
    d.setDoctype(new org.apache.ecs.Doctype.XHtml10Strict());
    d.appendTitle(title);
                
    head h = d.getHead();
    h.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
    h.addElement(com.bolsinga.web.Util.getIconLink());
    h.addElement(links.getLinkToRSS());
    h.addElement(links.getLinkToStyleSheet());
                
    h.addElement(new meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
    h.addElement(new meta().setContent(System.getProperty("user.name")).setName("Author"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.nowUTC().getTime().toString()).setName("Date"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.getGenerator()).setName("Generator"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.getCopyright(startYear)).setName("Copyright"));

    d.getBody().setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
                                                
    return d;
  }
        
  private static Element generateDiary(final com.bolsinga.web.Encode encoder, final Diary diary, final Music music, final Links links) {
    div diaryDiv = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_DIARY);
                
    Object[] args = { Calendar.getInstance().getTime() }; // LocalTime OK
    diaryDiv.addElement(new h3(MessageFormat.format(com.bolsinga.web.Util.getResourceString("updated"), args)));
                
    diaryDiv.addElement(links.getRSSLink());

    int mainPageEntryCount = com.bolsinga.web.Util.getSettings().getDiaryCount().intValue();
    boolean includeMusic = com.bolsinga.web.Util.getSettings().isMainPageHasMusic();

    List<Object> items = com.bolsinga.web.Util.getRecentItems(mainPageEntryCount, music, diary, includeMusic);
    for (Object o : items) {
      if (o instanceof com.bolsinga.diary.data.Entry) {
        diaryDiv.addElement(Web.addItem(encoder, (com.bolsinga.diary.data.Entry)o, links, false));
      } else if (o instanceof com.bolsinga.music.data.Show) {
        diaryDiv.addElement(com.bolsinga.music.Web.addItem(encoder, (com.bolsinga.music.data.Show)o));
      } else {
        System.err.println("Unknown recent item." + o.toString());
        Thread.dumpStack();
        System.exit(1);
      }
    }
                
    StringBuilder sb = new StringBuilder();
    sb.append("archives/");
    sb.append(Calendar.getInstance().get(Calendar.YEAR)); // LocalTime OK
    sb.append(".html");
                
    diaryDiv.addElement(new h2().addElement(com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("archives"))));
                
    return diaryDiv;
  }

  private static Map<String, String> createEntryIndex(final Collection<Entry> entries, final Links links) {
    Map<String, String> m = new TreeMap<String, String>();
    for (Entry e : entries) {
      String letter = links.getPageFileName(e);
      if (!m.containsKey(letter)) {
        m.put(letter, links.getLinkToPage(e));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private static Collection<Collection<Entry>> getEntryGroups(final Diary diary, final Links links) {
    // Each group is per page, so they are grouped by Entry who have the same starting sort letter.
    HashMap<String, Collection<Entry>> result = new HashMap<String, Collection<Entry>>(diary.getEntry().size());
    
    for (Entry entry : diary.getEntry()) {
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

  public static void generateArchivePages(final com.bolsinga.web.Backgrounder backgrounder, final Diary diary, final com.bolsinga.web.Encode encoder, final int startYear, final String outputDir) {
    Collections.sort(diary.getEntry(), Util.ENTRY_COMPARATOR);
    List<Entry> items = Collections.unmodifiableList(diary.getEntry());
    Links links = Links.getLinks(true);
    Map<String, String> index = Web.createEntryIndex(items, links);
    
    Web.generateArchivePages(backgrounder, items, index, encoder, links, startYear, outputDir);
  }
                
  public static void generateArchivePages(final com.bolsinga.web.Backgrounder backgrounder, final Collection<Entry> items, final Map<String, String> index, final com.bolsinga.web.Encode encoder, final Links links, final int startYear, final String outputDir) {
    DiaryDocumentCreator creator = new DiaryDocumentCreator(backgrounder, index, encoder, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), startYear);
    for (Entry item : items) {
      creator.add(item);
    }
    creator.complete();
  }

  public static ul addItem(final com.bolsinga.web.Encode encoder, final Entry entry, final Links links, final boolean upOneLevel) {
    // CSS.DIARY_ENTRY
    Vector<Element> e = new Vector<Element>();
    e.add(new h2().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(new h4().addElement(com.bolsinga.web.Util.createInternalA(links.getLinkTo(entry), com.bolsinga.web.Util.getResourceString("link"), com.bolsinga.web.Util.getResourceString("linktitle"))));
    e.add(new StringElement(Web.encodedComment(encoder, entry, upOneLevel)));
    return com.bolsinga.web.Util.createUnorderedList(e);
  }
        
  private static String encodedComment(com.bolsinga.web.Encode encoder, Entry entry, boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(entry, upOneLevel));
  }
}
