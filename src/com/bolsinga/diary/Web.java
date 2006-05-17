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
  com.bolsinga.web.Encode fEncoder = null;
  Diary  fDiary     = null;
  Links  fLinks     = null;
  String fProgram   = null;
  Entry  fCurEntry  = null;
  Entry  fLastEntry = null;
  int fStartYear = -1;
        
  public DiaryDocumentCreator(Diary diary, com.bolsinga.web.Encode encoder, Links links, String outputDir, String program, int startYear) {
    super(outputDir);
    fEncoder = encoder;
    fDiary = diary;
    fLinks = links;
    fProgram = program;
    fStartYear = startYear;
  }

  public void add(Entry entry) {
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
    return ((fLastEntry == null) || (fLastEntry.getTimestamp().get(Calendar.MONTH) != fCurEntry.getTimestamp().get(Calendar.MONTH)));
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
    java.util.Map m = new TreeMap();
    Iterator i = fDiary.getEntry().iterator();
    while (i.hasNext()) {
      Entry e = (Entry)i.next();
      String letter = fLinks.getPageFileName(e);
      if (!m.containsKey(letter)) {
        m.put(letter, fLinks.getLinkToPage(e));
      }
    }

    Vector e = new Vector();
    i = m.keySet().iterator();
    while (i.hasNext()) {
      String s = (String)i.next();
      if (s.equals(getCurrentLetter())) {
        e.add(new StringElement(s));
      } else {
        e.add(com.bolsinga.web.Util.createInternalA((String)m.get(s), s));
      }
    }
    e.add(fLinks.getRSSLink());

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DIARY_INDEX);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
        
  private Element addWebNavigator(String program, Links links) {
    Vector e = new Vector();
                
    Object[] args2 = { com.bolsinga.web.Util.getSettings().getContact(), program };
    e.add(new a(MessageFormat.format(com.bolsinga.web.Util.getResourceString("mailto"), args2), com.bolsinga.web.Util.getResourceString("contact"))); // mailto: URL
    e.add(links.getLinkToHome());

    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DIARY_MENU);
    Object[] args = { Calendar.getInstance().getTime() };
    d.addElement(new h4(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generated"), args)));
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}

public class Web {

  private static final boolean GENERATE_XML = false;

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

    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    generate(diary, music, encoder, output);
  }

  private static void usage() {
    System.out.println("Usage: Web xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }

  private static void export(Diary diary) {
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

  public static void generate(String sourceFile, String musicFile, String outputDir) {
    Diary diary = Util.createDiary(sourceFile);

    generate(diary, musicFile, outputDir);
  }
        
  public static void generate(Diary diary, String musicFile, String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    generate(diary, music, encoder, outputDir);
  }
        
  public static void generate(Diary diary, Music music, com.bolsinga.web.Encode encoder, String outputDir) {
    int startYear = Util.getStartYear(diary);
    generateMainPage(encoder, music, diary, startYear, outputDir);
    generateArchivePages(diary, encoder, startYear, outputDir);
  }
        
  public static void generateMainPage(com.bolsinga.web.Encode encoder, Music music, Diary diary, int startYear, String outputDir) {
    Links links = Links.getLinks(false);

    XhtmlDocument doc = createDocument(diary.getTitle(), startYear, links);

    doc.getBody().addElement(generateColumn1(diary));
                
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
      if (!parent.exists()) {
        if (!parent.mkdirs()) {
          System.out.println("Can't: " + parent.getAbsolutePath());
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

  private static div createMainStatics(Diary diary) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_STATIC);
    d.addElement(new h4(com.bolsinga.web.Util.getSettings().getLinksTitle()));
    String statics = diary.getStatic();
    if (statics != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(statics));
    }
    return d;
  }

  private static div createMainLinks(Diary diary) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_LINKS);
    d.addElement(new h4(com.bolsinga.web.Util.getSettings().getFriendsTitle()));
    String friends = diary.getFriends();
    if (friends != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(friends));
    }
    return d;
  }

  private static div generateColumn1(Diary diary) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_COL1);
    d.addElement(Web.createMainStatics(diary));
    d.addElement(Web.createMainLinks(diary));
    return d;
  }
        
  public static XhtmlDocument createDocument(String title, int startYear, Links links) {
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
    h.addElement(new meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.getGenerator()).setName("Generator"));
    h.addElement(new meta().setContent(com.bolsinga.web.Util.getCopyright(startYear)).setName("Copyright"));

    d.getBody().setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
                                                
    return d;
  }
        
  private static Element generateDiary(com.bolsinga.web.Encode encoder, Diary diary, Music music, Links links) {
    div diaryDiv = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_DIARY);
                
    Object[] args = { Calendar.getInstance().getTime() };
    diaryDiv.addElement(new h3(MessageFormat.format(com.bolsinga.web.Util.getResourceString("updated"), args)));
                
    diaryDiv.addElement(links.getRSSLink());

    int mainPageEntryCount = com.bolsinga.web.Util.getSettings().getDiaryCount().intValue();
    boolean includeMusic = com.bolsinga.web.Util.getSettings().isMainPageHasMusic();

    List items = com.bolsinga.web.Util.getRecentItems(mainPageEntryCount, music, diary, includeMusic);
    Iterator i = items.iterator();
    while (i.hasNext()) {
      Object o = i.next();

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
                
    StringBuffer sb = new StringBuffer();
    sb.append("archives/");
    sb.append(Calendar.getInstance().get(Calendar.YEAR));
    sb.append(".html");
                
    diaryDiv.addElement(new h2().addElement(com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("archives"))));
                
    return diaryDiv;
  }
        
  public static void generateArchivePages(Diary diary, com.bolsinga.web.Encode encoder, int startYear, String outputDir) {
    List items = diary.getEntry();
    Entry item = null;
                
    Collections.sort(items, Util.ENTRY_COMPARATOR);
                
    Links links = Links.getLinks(true);
                
    DiaryDocumentCreator creator = new DiaryDocumentCreator(diary, encoder, links, outputDir, com.bolsinga.web.Util.getResourceString("program"), startYear);
                
    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Entry)i.next();
                        
      creator.add(item);
    }
                
    creator.close();
  }

  public static ul addItem(com.bolsinga.web.Encode encoder, Entry entry, Links links, boolean upOneLevel) {
    // CSS.DIARY_ENTRY
    Vector e = new Vector();
    e.add(new h2().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(new h4().addElement(com.bolsinga.web.Util.createInternalA(links.getLinkTo(entry), com.bolsinga.web.Util.getResourceString("link"), com.bolsinga.web.Util.getResourceString("linktitle"))));
    e.add(new StringElement(Web.encodedComment(encoder, entry, upOneLevel)));
    return com.bolsinga.web.Util.createUnorderedList(e);
  }
        
  private static String encodedComment(com.bolsinga.web.Encode encoder, Entry entry, boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(entry, upOneLevel));
  }
}
