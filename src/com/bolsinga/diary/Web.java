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
	TBody fTBody = null;
	Entry fEntry = null;
	
	public DiaryDocumentCreator(Diary diary, String outputDir) {
		fDiary = diary;
		fOutputDir = outputDir;
	}
	
	public TBody getTableBody(Entry entry) {
		if (needNewDocument(entry)) {
			close();
			
			fDocument = Web.createDocument(getTitle(entry));
			fEntry = entry;

			addHeader();
			addWebNavigator();
			addIndexNavigator();
			
			fTBody = new TBody();
		}
		return fTBody;
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
		fDocument.getBody().addElement(new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(10).addElement(fTBody));
		addIndexNavigator();
		addWebNavigator();
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
	
	private void addWebNavigator() {
		Center c = new Center();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Generated ");
		sb.append(Web.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append(" ");

		StringBuffer link = new StringBuffer();
		link.append("mailto:");
		link.append(System.getProperty("diary.contact"));
		link.append("?Subject=");
		link.append(System.getProperty("diary.program"));
		link.append("%20Message");
		link.append("&Body=");
		link.append(System.getProperty("diary.program"));
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
		Document doc = createDocument(System.getProperty("diary.title"));

		TBody tbody = new TBody();
		
		TD td = new TD();
		td.setVAlign("top");
		td.setWidth("20%");
		td.addElement(diary.getStatic());
		tbody.addElement(td);

		td = new TD();
		td.setVAlign("top");
		td.setWidth("60%");
		StringBuffer sb = new StringBuffer();
		sb.append("Updated ");
		sb.append(Web.sWebFormat.format(Calendar.getInstance().getTime()));
		sb.append("!");
		td.addElement(new Center(diary.getHeader()).addElement(sb.toString()));
		
		generateDiary(musicFile, diary, mainPageEntryCount, td);
		tbody.addElement(td);
		
		td = new TD();
		td.setVAlign("top");
		td.setWidth("20%");

		td.addElement(com.bolsinga.music.web.Web.generatePreview(musicFile, 5));
		tbody.addElement(td);
		
		doc.getBody().addElement(new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(10).addElement(tbody));
		
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
		
		sb.append(System.getProperty("diary.program"));
		
		sb.append(" (built: ");
		sb.append(System.getProperty("diary.builddate"));
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
		Document d = new Document();
		d.appendTitle(title);
		d.getHtml().setPrettyPrint(true);
		
		Head h = d.getHead();
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
		TBody tbody = new TBody();
		
		Collections.sort(items, ENTRY_COMPARATOR);
		Collections.reverse(items);
		
		for (int i = 0; i < mainPageEntryCount; i++) {
			item = (Entry)items.get(i);
			
			addItem(musicFile, item, tbody, false);
		}
		
		addBanner(new A("archives/2004.html", "Archives").toString(), tbody);

		td.addElement(new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(10).addElement(tbody));
	}
	
	public static void generateArchivePages(String musicFile, Diary diary, String outputDir) {
		List items = diary.getEntry();
		Entry item = null;
		
		Collections.sort(items, ENTRY_COMPARATOR);
		
		DiaryDocumentCreator creator = new DiaryDocumentCreator(diary, outputDir);
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Entry)li.next();
			
			addItem(musicFile, item, creator.getTableBody(item), true);
		}
		
		creator.close();
	}

	static DateFormat sWebFormat = new SimpleDateFormat("M/d/yyyy");

	public static void addBanner(String text, TBody tb) {
		Font f = new Font();
		f.addAttribute("color", "white");
		f.addElement(new B(text));
		
		Div div = new Div();
		div.addAttribute("align", "center");
		div.addElement(f);
		
		TR tr = new TR();
		tr.addAttribute("bgcolor", "black");
		tr.addElement(new TD(div));
		
		tb.addElement(tr);
	}
	
	public static void addItem(String musicFile, Entry entry, TBody tb, boolean upOneLevel) {
		addBanner(sWebFormat.format(entry.getTimestamp().getTime()), tb);
		
		tb.addElement(new TR().addElement(new TD(Web.encodedComment(musicFile, entry, upOneLevel))));
	}
	
	private static Pattern sCommentEncoding = Pattern.compile("\n", Pattern.DOTALL);
	private static String encodedComment(String musicFile, Entry entry, boolean upOneLevel) {
		String result = entry.getComment();
		
		// Convert new lines to <p>
		result = sCommentEncoding.matcher(result).replaceAll(" <p> ");
		
		// Automatically add artist to the comments.
		result = com.bolsinga.music.web.Web.embedLinks(musicFile, result, upOneLevel);
		
		return result;
	}
}
