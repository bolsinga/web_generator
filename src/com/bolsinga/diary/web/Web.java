package com.bolsinga.diary.web;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

class DiaryDocumentCreator {
	Diary fDiary = null;
	String fOutputDir = null;
	Document fDocument = null;
	Table fTable = null;
	Entry fEntry = null;
	String fProgram = null;
	
	public DiaryDocumentCreator(Diary diary, String outputDir, String program) {
		fDiary = diary;
		fOutputDir = outputDir;
		fProgram = program;
	}
	
	public Table getTable(Entry entry) {
		if (needNewDocument(entry)) {
			close();
			
			fDocument = Web.createDocument(getTitle(entry));
			fEntry = entry;

			addHeader();
			addWebNavigator(fProgram);
			addIndexNavigator();
			
			fTable = new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(10);
		}
		return fTable;
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
		return (fEntry == null) || (!getPageFileName(fEntry).equals(getPageFileName(entry)));
	}
	
	static DateFormat sArchivePageFormat = new SimpleDateFormat("yyyy");
	
	private static String getPageFileName(Entry entry) {
		return sArchivePageFormat.format(entry.getTimestamp().getTime());
	}
	
	private static String getLinkToPage(Entry entry) {
		StringBuffer link = new StringBuffer();
		
		link.append("../");
		link.append("archives");
		link.append(File.separator);
		link.append(getPageFileName(entry));
		link.append(".html");
		
		return link.toString();
	}
	
	private String getCurrentPath(Entry entry) {
		StringBuffer sb = new StringBuffer();
		sb.append("archives");
		sb.append(File.separator);
		sb.append(getPageFileName(entry));
		sb.append(".html");
		return sb.toString();
	}
	
	private String getTitle(Entry entry) {
		StringBuffer sb = new StringBuffer();
		sb.append(getPageFileName(entry));
		sb.append(" Archives");
		return sb.toString();
	}
		
	private void writeDocument() {
		fDocument.getBody().addElement(new P());
		fDocument.getBody().addElement(fTable);
		addIndexNavigator();
		addWebNavigator(fProgram);
		try {
			File f = new File(fOutputDir, getCurrentPath(fEntry));
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
	
	private void addHeader() {
		IMG img = new IMG("http://homepage.mac.com/bolsinga/.Pictures/images/comp.gif");
		img.setHeight(90);
		img.setWidth(120);
		img.setAlt("[Busy computing... for you!]");
		fDocument.getBody().addElement(new Center(img));
	}

	private void addIndexNavigator() {
		Center c = new Center();

		java.util.Map m = new TreeMap();
		Iterator li = fDiary.getEntry().iterator();
		while (li.hasNext()) {
			Entry a = (Entry)li.next();
			String letter = getPageFileName(a);
			if (!m.containsKey(letter)) {
				m.put(letter, getLinkToPage(a));
			}
		}

		li = m.keySet().iterator();
		while (li.hasNext()) {
			String a = (String)li.next();
			if (a.equals(getPageFileName(fEntry))) {
				c.addElement(a + " ");
			} else {
				c.addElement(new A((String)m.get(a), a).toString() + " ");
			}
		}
		
		fDocument.getBody().addElement(c);
	}
	
	private void addWebNavigator(String program) {
		Center c = new Center();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Generated ");
		sb.append(Web.sWebFormat.format(Calendar.getInstance().getTime()));
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

		c.addElement(sb.toString());
		
		fDocument.getBody().addElement(c);
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
		Diary diary = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			diary = (Diary)u.unmarshal(new FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		
		generateMainPage(musicFile, mainPageEntryCount, diary, outputDir);
		
		generateArchivePages(musicFile, diary, outputDir);
	}

	public static final Comparator ENTRY_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Entry e1 = (Entry)o1;
			Entry e2 = (Entry)o2;
			
			return e1.getTimestamp().before(e2.getTimestamp()) ? -1 : 1;
		}
	};
	
	public static void generateMainPage(String musicFile, int mainPageEntryCount, Diary diary, String outputDir) {
		Document doc = createDocument(diary.getTitle());

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
		sb.append(Web.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append("!");
		td.addElement(new Center(diary.getHeader()).addElement(sb.toString()));
		
		generateDiary(musicFile, diary, mainPageEntryCount, td);
		tr.addElement(td);
		
		td = new TD();
		td.setVAlign("top");
		td.setWidth("20%");

		td.addElement(com.bolsinga.music.web.Web.generatePreview(musicFile, 5));
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
		
		cp.append("Copyright (c) ");
		cp.append(year++);
		for ( ; year <= cur_year; ++year) {
			cp.append(", ");
			cp.append(year);
		}
		
		cp.append(" Greg Bolsinga");
		
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
		sb.append(")");
		
		return sb.toString();
	}
	
	public static Document createDocument(String title) {
		Document d = new Document(ECSDefaults.getDefaultCodeset());

        d.setDoctype(new org.apache.ecs.Doctype.Html401Transitional());
		d.appendTitle(title);
		d.getHtml().setPrettyPrint(true);
		
		Head h = d.getHead();
		h.addElement(new Meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
		h.addElement(new Link().setRel("SHORTCUT ICON").setHref("http://homepage.mac.com/bolsinga/.Pictures/images/computer.ico"));
		h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new Meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new Meta().setContent(getGenerator()).setName("Generator"));
		h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));
				
		return d;
	}
	
	private static void generateDiary(String musicFile, Diary diary, int mainPageEntryCount, TD td) {
		List items = diary.getEntry();
		Entry item = null;
		Table table = new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(10);
		
		Collections.sort(items, ENTRY_COMPARATOR);
		Collections.reverse(items);
		
		for (int i = 0; i < mainPageEntryCount; i++) {
			item = (Entry)items.get(i);
			
			addItem(musicFile, item, table, false);
		}
		
		StringBuffer archivesLink = new StringBuffer();
		archivesLink.append("archives/");
		archivesLink.append(Calendar.getInstance().get(Calendar.YEAR));
		archivesLink.append(".html");
		
		addBanner(new A(archivesLink.toString(), "Archives").toString(), table);

		td.addElement(table);
	}
	
	public static void generateArchivePages(String musicFile, Diary diary, String outputDir) {
		List items = diary.getEntry();
		Entry item = null;
		
		Collections.sort(items, ENTRY_COMPARATOR);
		
		DiaryDocumentCreator creator = new DiaryDocumentCreator(diary, outputDir, sResource.getString("program"));
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Entry)li.next();
			
			addItem(musicFile, item, creator.getTable(item), true);
		}
		
		creator.close();
	}

	static DateFormat sWebFormat = new SimpleDateFormat("M/d/yyyy");

	public static void addBanner(String text, Table table) {
		Font f = new Font();
		f.addAttribute("color", "white");
		f.addElement(new B(text));
		
		Div div = new Div();
		div.addAttribute("align", "center");
		div.addElement(f);
		
		TR tr = new TR();
		tr.addAttribute("bgcolor", "black");
		tr.addElement(new TD(div));
		
		table.addElement(tr);
	}
	
	public static void addItem(String musicFile, Entry entry, Table table, boolean upOneLevel) {
		addBanner(sWebFormat.format(entry.getTimestamp().getTime()), table);
		
		table.addElement(new TR().addElement(new TD(Web.encodedComment(musicFile, entry, upOneLevel))));
	}
	
	private static Pattern sCommentEncoding = Pattern.compile("\n", Pattern.DOTALL);
	
	private static HashMap sLinkedData = new HashMap();
	
	private static synchronized String encodedComment(String musicFile, Entry entry, boolean upOneLevel) {
		String comment = entry.getComment();
		
		if (!sLinkedData.containsKey(comment)) {
			// Automatically add music links to the comments.
			String result = com.bolsinga.music.web.Web.embedLinks(musicFile, comment, upOneLevel);
			
			// Convert new lines to <p>
			result = sCommentEncoding.matcher(result).replaceAll("<p>");
			
			sLinkedData.put(comment, result);
		}
		
		return (String)sLinkedData.get(comment);
	}
}
