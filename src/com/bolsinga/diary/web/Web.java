package com.bolsinga.diary.web;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

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
			
			fDocument = createDocument(entry);
			// headers
			fEntry = entry;
			
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

	private boolean needNewDocument(Entry entry) {
		return (fEntry == null) || (!getPageFileName(fEntry).equals(getPageFileName(entry)));
	}
	
	static DateFormat sArchivePageFormat = new SimpleDateFormat("yyyy");
	
	private String getPageFileName(Entry entry) {
		return sArchivePageFormat.format(entry.getTimestamp().getTime());
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
	
	private Document createDocument(Entry entry) {
		Document d = new Document();
		d.appendTitle(getTitle(entry));
		d.getHtml().setPrettyPrint(true);
		
		Head h = d.getHead();
		h.addElement(new Link().setRel("SHORTCUT ICON").setHref("http://homepage.mac.com/bolsinga/.Pictures/images/computer.ico"));
		h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
		h.addElement(new Meta().setContent(Calendar.getInstance().getTime().toString()).setName("Date"));
		h.addElement(new Meta().setContent(getGenerator()).setName("Generator"));
		h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));
		
		return d;
	}
	
	private void writeDocument() {
		fDocument.getBody().addElement(new Table().setBorder(0).setWidth("100%").setCellSpacing(0).setCellPadding(0).addElement(fTBody));
		fDocument.getBody().addElement(new HR());
		// footer here
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
}

public class Web {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: Web [# entries on main page] [source.xml] [output.dir]");
			System.exit(0);
		}
		
		Web.generate(Integer.parseInt(args[0]), args[1], args[2]);
	}

	public static void generate(int mainPageEntryCount, String sourceFile, String outputDir) {
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
		
		generateMainPage(mainPageEntryCount, diary, outputDir);
		
		generateArchivePages(diary, outputDir);
	}

	public static final Comparator ENTRY_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Entry e1 = (Entry)o1;
			Entry e2 = (Entry)o2;
			
			return e1.getTimestamp().before(e2.getTimestamp()) ? -1 : 1;
		}
	};
	
	public static void generateMainPage(int mainPageEntryCount, Diary diary, String outputDir) {
	
	}
	
	public static void generateArchivePages(Diary diary, String outputDir) {
		List items = diary.getEntry();
		Entry item = null;
		
		Collections.sort(items, ENTRY_COMPARATOR);
		
		DiaryDocumentCreator creator = new DiaryDocumentCreator(diary, outputDir);
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Entry)li.next();
			
			addItem(diary, item, creator.getTableBody(item));
		}
		
		creator.close();
	}

	private static DateFormat sWebFormat = new SimpleDateFormat("M/d/yyyy");

	public static void addItem(Diary diary, Entry entry, TBody tb) {
		Font f = new Font();
		f.addAttribute("color", "white");
		f.addElement(new B(sWebFormat.format(entry.getTimestamp().getTime())));
		
		Div div = new Div();
		div.addAttribute("align", "center");
		div.addElement(f);
		
		TR tr = new TR();
		tr.addAttribute("bgcolor", "black");
		tr.addElement(new TD(div));
		
		tb.addElement(tr);
		
		tb.addElement(new TR().addElement(new TD(entry.getComment())));
	}
}
