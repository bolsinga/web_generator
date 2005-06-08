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

class DiaryDocumentCreator extends com.bolsinga.web.MultiDocumentCreator {
  Diary  fDiary     = null;
  Music  fMusic     = null;
  Links  fLinks     = null;
  String fProgram   = null;
  Entry  fCurEntry  = null;
  Entry  fLastEntry = null;
        
  public DiaryDocumentCreator(Diary diary, Music music, Links links, String outputDir, String program) {
    super(outputDir);
    fDiary = diary;
    fMusic = music;
    fLinks = links;
    fProgram = program;
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
    return Web.createDocument(getTitle(), fLinks);
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
    return Web.addItem(fMusic, fCurEntry, true);
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

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    Web.generate(args[0], args[1], args[3]);
  }

  public static void generate(String sourceFile, String musicFile, String outputDir) {
    Diary diary = Util.createDiary(sourceFile);
                
    generate(diary, musicFile, outputDir);
  }
        
  public static void generate(Diary diary, String musicFile, String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
                
    generate(diary, music, outputDir);
  }
        
  public static void generate(Diary diary, Music music, String outputDir) {
    generateMainPage(music, diary, outputDir);
                
    generateArchivePages(music, diary, outputDir);
  }
        
  public static void generateMainPage(Music music, Diary diary, String outputDir) {
    Links links = Links.getLinks(false);

    XhtmlDocument doc = createDocument(diary.getTitle(), links);

    doc.getBody().addElement(generateColumn1(diary));
                
    div main = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_MAIN);
    div header = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_HEADER);
    header.addElement(com.bolsinga.web.Util.convertToUnOrderedList(diary.getHeader()));
    main.addElement(header);
    main.addElement(generateDiary(music, diary, links));
    doc.getBody().addElement(main);
                
    div mainCol2 = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_COL2);
    mainCol2.addElement(com.bolsinga.music.Web.generatePreview(music, 5));
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
    d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(diary.getStatic()));
    return d;
  }

  private static div createMainLinks(Diary diary) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_LINKS);
    d.addElement(new h4(com.bolsinga.web.Util.getSettings().getFriendsTitle()));
    d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(diary.getFriends()));
    return d;
  }

  private static div generateColumn1(Diary diary) {
    div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_COL1);
    d.addElement(Web.createMainStatics(diary));
    d.addElement(Web.createMainLinks(diary));
    return d;
  }
        
  private static String getCopyright() {
    StringBuffer cp = new StringBuffer();
                
    int year = 2003; // This is the first year of this data.
    int cur_year = Calendar.getInstance().get(Calendar.YEAR);
                
    cp.append("Contents Copyright (c) ");
    cp.append(year++);
    for ( ; year <= cur_year; ++year) {
      cp.append(", ");
      cp.append(year);
    }
                
    cp.append(" ");
    cp.append(System.getProperty("user.name"));
                
    return cp.toString();
  }
        
  public static XhtmlDocument createDocument(String title, Links links) {
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
    h.addElement(new meta().setContent(getCopyright()).setName("Copyright"));

    d.getBody().setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
                                                
    return d;
  }
        
  private static Element generateDiary(Music music, Diary diary, Links links) {
    List items = diary.getEntry();
    Entry item = null;

    div diaryDiv = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.MAIN_DIARY);
                
    Object[] args = { Calendar.getInstance().getTime() };
    diaryDiv.addElement(new h3(MessageFormat.format(com.bolsinga.web.Util.getResourceString("updated"), args)));
                
    diaryDiv.addElement(links.getRSSLink());
                                
    Collections.sort(items, Util.ENTRY_COMPARATOR);
    Collections.reverse(items);

    int mainPageEntryCount = com.bolsinga.web.Util.getSettings().getDiaryCount().intValue();
                
    for (int i = 0; i < mainPageEntryCount; i++) {
      item = (Entry)items.get(i);
                        
      diaryDiv.addElement(Web.addItem(music, item, false));
    }
                
    StringBuffer sb = new StringBuffer();
    sb.append("archives/");
    sb.append(Calendar.getInstance().get(Calendar.YEAR));
    sb.append(".html");
                
    diaryDiv.addElement(new h2().addElement(com.bolsinga.web.Util.createInternalA(sb.toString(), com.bolsinga.web.Util.getResourceString("archives"))));
                
    return diaryDiv;
  }
        
  public static void generateArchivePages(Music music, Diary diary, String outputDir) {
    List items = diary.getEntry();
    Entry item = null;
                
    Collections.sort(items, Util.ENTRY_COMPARATOR);
                
    Links links = Links.getLinks(true);
                
    DiaryDocumentCreator creator = new DiaryDocumentCreator(diary, music, links, outputDir, com.bolsinga.web.Util.getResourceString("program"));
                
    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Entry)i.next();
                        
      creator.add(item);
    }
                
    creator.close();
  }

  public static ul addItem(Music music, Entry entry, boolean upOneLevel) {
    // CSS.DIARY_ENTRY
    Vector e = new Vector();
    e.add(new h2().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(new StringElement(Web.encodedComment(music, entry, upOneLevel)));
    return com.bolsinga.web.Util.createUnorderedList(e);
  }
        
  private static String encodedComment(Music music, Entry entry, boolean upOneLevel) {
    return com.bolsinga.web.Util.convertToParagraphs(com.bolsinga.music.Web.embedLinks(music, entry.getComment(), upOneLevel));
  }
}
