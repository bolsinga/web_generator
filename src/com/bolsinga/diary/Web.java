package com.bolsinga.diary.web;

import com.bolsinga.diary.data.*;
import com.bolsinga.diary.util.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

class DiaryDocumentCreator extends com.bolsinga.web.util.DocumentCreator {
	Diary fDiary = null;
	Links fLinks = null;
	String fProgram = null;
	Entry fCurEntry = null;
	Entry fLastEntry = null;
	
	public DiaryDocumentCreator(Diary diary, Links links, String outputDir, String program) {
        super(outputDir);
		fDiary = diary;
		fLinks = links;
		fProgram = program;
	}

    public void add(Music music, Entry entry) {
        fCurEntry = entry;
		getSubsection().addElement(Web.addItem(music, entry, true));
        fLastEntry = fCurEntry;
    }
    
    protected String getTitle() {
        return getTitle("Archives");
    }
    
	protected boolean needNewDocument() {
		return ((fLastEntry == null) || !fLinks.getPageFileName(fLastEntry).equals(getCurrentLetter()));
	}

    protected XhtmlDocument createDocument() {
        return Web.createDocument(getTitle(), fLinks);
    }

    protected div getHeaderDiv() {
        div headerDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.DIARY_HEADER);
        headerDiv.addElement(new h1().addElement(getTitle()));
        headerDiv.addElement(com.bolsinga.web.util.Util.getLogo());
        headerDiv.addElement(addWebNavigator(fProgram, fLinks));
        headerDiv.addElement(addIndexNavigator());
        return headerDiv;
    }

    protected boolean needNewSubsection() {
        return ((fLastEntry == null) || (fLastEntry.getTimestamp().get(Calendar.MONTH) != fCurEntry.getTimestamp().get(Calendar.MONTH)));
    }

    protected Element getSubsectionTitle() {
        String m = Util.getMonth(fCurEntry);
        a an = new a(); // named target
        an.setName(m);
        an.addElement("t", m);
        return new h2().addElement(an);
    }

	protected String getLastPath() {
        return fLinks.getPagePath(fLastEntry);
    }
    
	protected String getCurrentLetter() {
        return fLinks.getPageFileName(fCurEntry);
    }

	protected Element addIndexNavigator() {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.DIARY_INDEX);

		java.util.Map m = new TreeMap();
		Iterator li = fDiary.getEntry().iterator();
		while (li.hasNext()) {
			Entry e = (Entry)li.next();
			String letter = fLinks.getPageFileName(e);
			if (!m.containsKey(letter)) {
				m.put(letter, fLinks.getLinkToPage(e));
			}
		}
		
		ul list = new ul();

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String s = (String)li.next();
			if (s.equals(getCurrentLetter())) {
				list.addElement(new li(s));
			} else {
				list.addElement(new li(com.bolsinga.web.util.Util.createInternalA((String)m.get(s), s)));
			}
		}
		
		list.addElement(fLinks.getRSSLink());
		
		d.addElement(list);
		
		return d;
	}
	
	private Element addWebNavigator(String program, Links links) {
		div d = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.DIARY_MENU);
		
		StringBuffer sb = new StringBuffer();
		sb.append("Generated ");
		sb.append(Util.sWebFormat.format(Calendar.getInstance().getTime()));
		d.addElement(new h4(sb.toString()));

		ul list = new ul();
		
		sb = new StringBuffer();
		sb.append("mailto:");
		sb.append(com.bolsinga.web.util.Util.getSettings().getContact());
		sb.append("?Subject=");
		sb.append(program);
		sb.append("%20Message&amp;Body=");
		sb.append(program);
		sb.append("%20Message%0A");
		list.addElement(new li(new a(sb.toString(), "Contact"))); // mailto: URL

		list.addElement(new li(links.getLinkToHome()));

		d.addElement(list);
		
		return d;
	}
}

public class Web {

	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.diary.web.web");

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir]");
			System.exit(0);
		}

        Settings settings = com.bolsinga.web.util.Util.createSettings(args[2]);
		
        Web.initializeSettings(settings);
        
        int mainPageEntryCount = settings.getDiaryCount().intValue();
        
		Web.generate(mainPageEntryCount, args[0], args[1], args[3]);
	}
    
    private static void initializeSettings(Settings settings) {
        com.bolsinga.settings.data.Image image = settings.getLogoImage();
		System.setProperty("web.logo.url", image.getLocation());
		System.setProperty("web.logo.width", image.getWidth().toString());
		System.setProperty("web.logo.height", image.getHeight().toString());
		System.setProperty("web.logo.alt", image.getAlt());
		System.setProperty("web.layout.css", settings.getCssFile());
		System.setProperty("rss.url", settings.getRssFile());
        image = settings.getRssImage();
		System.setProperty("rss.image.url", image.getLocation());
		System.setProperty("rss.image.width", image.getWidth().toString());
		System.setProperty("rss.image.height", image.getHeight().toString());
		System.setProperty("rss.image.alt", image.getAlt());
		System.setProperty("music.ical.url", settings.getIcalName() + ".ics");
        image = settings.getIcalImage();
		System.setProperty("ical.image.url", image.getLocation());
		System.setProperty("ical.image.width", image.getWidth().toString());
		System.setProperty("ical.image.height", image.getHeight().toString());
		System.setProperty("ical.image.alt", image.getAlt());
    }

	public static void generate(int mainPageEntryCount, String sourceFile, String musicFile, String outputDir) {
		Diary diary = Util.createDiary(sourceFile);
		
		generate(mainPageEntryCount, diary, musicFile, outputDir);
	}
	
	public static void generate(int mainPageEntryCount, Diary diary, String musicFile, String outputDir) {
		Music music = com.bolsinga.music.util.Util.createMusic(musicFile);
		
		generate(mainPageEntryCount, diary, music, outputDir);
	}
	
	public static void generate(int mainPageEntryCount, Diary diary, Music music, String outputDir) {
		generateMainPage(music, mainPageEntryCount, diary, outputDir);
		
		generateArchivePages(music, diary, outputDir);
	}
	
	public static void generateMainPage(Music music, int mainPageEntryCount, Diary diary, String outputDir) {
		Links links = Links.getLinks(false);

		XhtmlDocument doc = createDocument(diary.getTitle(), links);

		doc.getBody().addElement(generateColumn1(diary));
		
		div main = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_MAIN);
		div header = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_HEADER);
		header.addElement(com.bolsinga.web.util.Util.convertToUnOrderedList(diary.getHeader()));
		main.addElement(header);
		main.addElement(generateDiary(music, diary, links, mainPageEntryCount));
		doc.getBody().addElement(main);
		
		div mainCol2 = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_COL2);
		mainCol2.addElement(com.bolsinga.music.web.Web.generatePreview(music, 5));
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

	private static div generateColumn1(Diary diary) {
		div mainCol1 = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_COL1);
		
		div staticDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_STATIC);
		// +++gdb Add a h2 element.
		staticDiv.addElement(com.bolsinga.web.util.Util.convertToUnOrderedList(diary.getStatic()));
		mainCol1.addElement(staticDiv);

		div linksDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_LINKS);
		// +++gdb Add a h2 element.
		linksDiv.addElement(com.bolsinga.web.util.Util.convertToUnOrderedList(diary.getFriends()));
		mainCol1.addElement(linksDiv);
		
		return mainCol1;
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
	
	private static String getGenerator() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(sResource.getString("program"));
		
		sb.append(" (built: ");
		sb.append(sResource.getString("builddate"));
		sb.append(" running on jdk ");
		sb.append(System.getProperty("java.runtime.version"));
		sb.append(" - ");
		sb.append(System.getProperty("os.name"));
		sb.append(" ");
		sb.append(System.getProperty("os.version"));
		
		sb.append(" [");
		sb.append(sResource.getString("copyright"));
		sb.append("]");
		
		sb.append(")");
		
		return sb.toString();
	}
		
	public static XhtmlDocument createDocument(String title, Links links) {
		XhtmlDocument d = new XhtmlDocument(ECSDefaults.getDefaultCodeset());
		
		d.getHtml().setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		
        d.setDoctype(new org.apache.ecs.Doctype.XHtml10Strict());
		d.appendTitle(title);
		
		head h = d.getHead();
		h.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		h.addElement(com.bolsinga.web.util.Util.getIconLink());
		h.addElement(links.getLinkToRSS());
		h.addElement(links.getLinkToStyleSheet());
		
		h.addElement(new meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
		h.addElement(new meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new meta().setContent(getGenerator()).setName("Generator"));
		h.addElement(new meta().setContent(getCopyright()).setName("Copyright"));

		d.getBody().setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
						
		return d;
	}
	
	private static Element generateDiary(Music music, Diary diary, Links links, int mainPageEntryCount) {
		List items = diary.getEntry();
		Entry item = null;

		div diaryDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.MAIN_DIARY);
		
		StringBuffer sb = new StringBuffer();
		sb.append("Updated ");
		sb.append(Util.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append("!");
		diaryDiv.addElement(new h3(sb.toString()));
		
		diaryDiv.addElement(links.getRSSLink());
				
		Collections.sort(items, Util.ENTRY_COMPARATOR);
		Collections.reverse(items);
		
		for (int i = 0; i < mainPageEntryCount; i++) {
			item = (Entry)items.get(i);
			
			diaryDiv.addElement(Web.addItem(music, item, false));
		}
		
		sb = new StringBuffer();
		sb.append("archives/");
		sb.append(Calendar.getInstance().get(Calendar.YEAR));
		sb.append(".html");
		
		diaryDiv.addElement(new h2().addElement(com.bolsinga.web.util.Util.createInternalA(sb.toString(), "Archives")));
		
		return diaryDiv;
	}
	
	public static void generateArchivePages(Music music, Diary diary, String outputDir) {
		List items = diary.getEntry();
		Entry item = null;
		
		Collections.sort(items, Util.ENTRY_COMPARATOR);
		
		Links links = Links.getLinks(true);
		
		DiaryDocumentCreator creator = new DiaryDocumentCreator(diary, links, outputDir, sResource.getString("program"));
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Entry)li.next();
			
            creator.add(music, item);
		}
		
		creator.close();
	}

	public static div addItem(Music music, Entry entry, boolean upOneLevel) {
		div diaryDiv = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.DIARY_ENTRY);
		
		a an = new a(); // named target
		an.setName(entry.getId());
		an.addElement("test", Util.getTitle(entry));
		
		diaryDiv.addElement(new h2().addElement(an));
		diaryDiv.addElement(Web.encodedComment(music, entry, upOneLevel));
		
        return diaryDiv;
	}
	
	private static synchronized String encodedComment(Music music, Entry entry, boolean upOneLevel) {
		return com.bolsinga.web.util.Util.convertToParagraphs(com.bolsinga.music.web.Web.embedLinks(music, entry.getComment(), upOneLevel));
	}
}
