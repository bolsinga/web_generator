package com.bolsinga.diary.web;

import com.bolsinga.diary.data.*;
import com.bolsinga.diary.util.*;
import com.bolsinga.music.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

class DiaryDocumentCreator {
	Diary fDiary = null;
	Links fLinks = null;
	String fOutputDir = null;
	Document fDocument = null;
	Div fMainDiv = null;
	Entry fEntry = null;
	String fProgram = null;
	
	public DiaryDocumentCreator(Diary diary, Links links, String outputDir, String program) {
		fDiary = diary;
		fLinks = links;
		fOutputDir = outputDir;
		fProgram = program;
	}
	
	public Div getMainDiv(Entry entry) {
		if (needNewDocument(entry)) {
			close();
			
			String title = getTitle(entry);
			fDocument = Web.createDocument(title, fLinks);
			fEntry = entry;

			Div headerDiv = com.bolsinga.web.util.Util.createDiv();
			headerDiv.addElement(new H1().addElement(title));
			headerDiv.addElement(com.bolsinga.web.util.Util.getLogo());
			headerDiv.addElement(addWebNavigator(fProgram));
			headerDiv.addElement(addIndexNavigator());
			fDocument.getBody().addElement(headerDiv);
			
			fMainDiv = com.bolsinga.web.util.Util.createDiv();
		}
		return fMainDiv;
	}
	
	public void close() {
		if (fDocument != null) {
			writeDocument();
			fDocument = null;
		}
	}

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	private boolean needNewDocument(Entry entry) {
		return (fEntry == null) || (!fLinks.getPageFileName(fEntry).equals(fLinks.getPageFileName(entry)));
	}
	
	private String getTitle(Entry entry) {
		StringBuffer sb = new StringBuffer();
		sb.append(fLinks.getPageFileName(entry));
		sb.append(" Archives");
		return sb.toString();
	}
		
	private void writeDocument() {
		fDocument.getBody().addElement(fMainDiv);
		
		Div footerDiv = com.bolsinga.web.util.Util.createDiv();
		footerDiv.addElement(addIndexNavigator());
		footerDiv.addElement(addWebNavigator(fProgram));
		fDocument.getBody().addElement(footerDiv);
		
		try {
			File f = new File(fOutputDir, fLinks.getPagePath(fEntry));
			File parent = new File(f.getParent());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					System.out.println("Can't: " + parent.getAbsolutePath());
				}
			}
			OutputStream os = new FileOutputStream(f);
			fDocument.output(os);
			os.close();
		} catch (IOException ioe) {
			System.err.println("Exception: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	private Element addIndexNavigator() {
		Div div = com.bolsinga.web.util.Util.createDiv();

		java.util.Map m = new TreeMap();
		Iterator li = fDiary.getEntry().iterator();
		while (li.hasNext()) {
			Entry a = (Entry)li.next();
			String letter = fLinks.getPageFileName(a);
			if (!m.containsKey(letter)) {
				m.put(letter, fLinks.getLinkToPage(a));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String a = (String)li.next();
			if (a.equals(fLinks.getPageFileName(fEntry))) {
				div.addElement(a + " ");
			} else {
				div.addElement(new A((String)m.get(a), a).toString() + " ");
			}
		}
		
		div.addElement(fLinks.getRSSLink());
		
		return div;
	}
	
	private Element addWebNavigator(String program) {
		Div div = com.bolsinga.web.util.Util.createDiv();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Generated ");
		sb.append(Util.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append(" ");

		StringBuffer link = new StringBuffer();
		link.append("mailto:");
		link.append(System.getProperty("diary.contact"));
		link.append("?Subject=");
		link.append(program);
		link.append("%20Message");
		link.append("&amp;Body=");
		link.append(program);
		link.append("%20Message%0A");
		A a = new A(link.toString(), "Contact");
		sb.append(a.toString());
		sb.append(" ");

		a = new A(System.getProperty("diary.root"), "Home");
		sb.append(a.toString());
		sb.append(" ");

		div.addElement(sb.toString());
		
		return div;
	}
}

public class Web {

	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.diary.web.web");

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: Web [# entries on main page] [diary.xml] [music.xml] [output.dir]");
			System.exit(0);
		}
		
		Web.generate(Integer.parseInt(args[0]), args[1], args[2], args[3]);
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

		Document doc = createDocument(diary.getTitle(), links);

		Table table = new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(10);
		
		TR tr = new TR(true);
		
		TD td = new TD();
		td.setVAlign("top");
		td.setWidth("20%");
		td.addElement(diary.getStatic());
		tr.addElement(td);

		td = new TD();
		td.setVAlign("top");
		td.setWidth("60%");
		StringBuffer sb = new StringBuffer();
		sb.append("Updated ");
		sb.append(Util.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append("!");
		td.addElement(com.bolsinga.web.util.Util.createDiv().addElement(diary.getHeader()).addElement(sb.toString()));
		td.addElement(com.bolsinga.web.util.Util.createDiv().addElement(links.getRSSLink()));
		
		generateDiary(music, diary, links, mainPageEntryCount, td);
		tr.addElement(td);
		
		td = new TD();
		td.setVAlign("top");
		td.setWidth("20%");

		td.addElement(com.bolsinga.music.web.Web.generatePreview(music, 5));
		tr.addElement(td);
		
		doc.getBody().addElement(table.addElement(tr));
		
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
		
	public static Document createDocument(String title, Links links) {
		Document d = new Document(ECSDefaults.getDefaultCodeset());
		
		d.getHtml().setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		
        d.setDoctype(new org.apache.ecs.Doctype.Html401Strict());
		d.appendTitle(title);
		
		Head h = d.getHead();
		h.setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
		h.addElement(com.bolsinga.web.util.Util.getIconLink());
		
		h.addElement(links.getAlternateRSSLink());
		
		h.addElement(new Meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
		h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new Meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new Meta().setContent(getGenerator()).setName("Generator"));
		h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));

		d.getBody().setPrettyPrint(com.bolsinga.web.util.Util.getPrettyPrint());
						
		return d;
	}
	
	private static void generateDiary(Music music, Diary diary, Links links, int mainPageEntryCount, TD td) {
		List items = diary.getEntry();
		Entry item = null;
		
		Div mainDiv = com.bolsinga.web.util.Util.createDiv();
		
		Collections.sort(items, Util.ENTRY_COMPARATOR);
		Collections.reverse(items);
		
		for (int i = 0; i < mainPageEntryCount; i++) {
			item = (Entry)items.get(i);
			
			addItem(music, item, mainDiv, false, true);
		}
		
		StringBuffer archivesLink = new StringBuffer();
		archivesLink.append("archives/");
		archivesLink.append(Calendar.getInstance().get(Calendar.YEAR));
		archivesLink.append(".html");
		
		mainDiv.addElement(new H2().addElement(new A(archivesLink.toString(), "Archives")));
		
		td.addElement(mainDiv);
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
			
			addItem(music, item, creator.getMainDiv(item), true, false);
		}
		
		creator.close();
	}

	public static void addItem(Music music, Entry entry, Div mainDiv, boolean upOneLevel, boolean cacheEncoding) {
		Div diaryDiv = com.bolsinga.web.util.Util.createDiv();
		
		A a = new A();
		a.setName(entry.getId());
		a.addElement("test", Util.getTitle(entry));
		
		diaryDiv.addElement(new H2().addElement(a));
		Div commentDiv = com.bolsinga.web.util.Util.createDiv();
		diaryDiv.addElement(commentDiv.addElement(Web.encodedComment(music, entry, upOneLevel, cacheEncoding)));
		
		mainDiv.addElement(diaryDiv);
	}
	
	private static HashMap sLinkedData = new HashMap();
	
	private static synchronized String encodedComment(Music music, Entry entry, boolean upOneLevel, boolean cacheEncoding) {
		String encoded = null;
		
		if (!cacheEncoding && sLinkedData.containsKey(entry.getId())) {
			encoded = (String)sLinkedData.get(entry.getId());
		} else {
			encoded = com.bolsinga.web.util.Util.convertToParagraphs(com.bolsinga.music.web.Web.embedLinks(music, entry.getComment(), upOneLevel));
			if (cacheEncoding) {
				sLinkedData.put(entry.getId(), encoded);
			}
		}
		
		return encoded;
	}
}
