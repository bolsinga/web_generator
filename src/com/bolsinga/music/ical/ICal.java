package com.bolsinga.music.ical;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ICal {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: ICal [source.xml] [output.dir]");
			System.exit(0);
		}
		
		ICal.generate(args[0], args[1]);
	}

	public static void generate(String sourceFile, String outputDir) {
		Music music = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			music = (Music)u.unmarshal(new FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		
		generateICal(music, outputDir);
	}
	
	public static void generateICal(Music music, String outputDir) {
		List items = music.getShow();
		Show item = null;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		
		VCalendar cal = new VCalendar("Shows");
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Show)li.next();
			
			if (!item.getDate().isUnknown()) {
				addItem(item, cal);
			}
		}
		
		try {
			File f = new File(outputDir, "Shows.ics");
			File parent = new File(f.getParent());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					System.out.println("Can't: " + parent.getAbsolutePath());
				}
			}
			OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
			cal.output(os);
			os.close();
		} catch (IOException ioe) {
			System.err.println("Exception: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void addItem(Show show, VCalendar calendar) {
		com.bolsinga.music.data.Date d = show.getDate();
		Calendar date = Calendar.getInstance();
		date.set(Calendar.MONTH, d.getMonth().intValue() - 1);
		date.set(Calendar.DAY_OF_MONTH, d.getDay().intValue());
		
		StringBuffer summary = new StringBuffer();
		String url = null;
		
		ListIterator bi = show.getPerformance().listIterator();
		while (bi.hasNext()) {
			Performance p = (Performance)bi.next();
			Artist a = (Artist)p.getArtist();
			
			summary.append(a.getName());
			
			if (bi.hasNext()) {
				summary.append(", ");
			}
		}
		
		summary.append(" @ ");
		
		summary.append(((Venue)show.getVenue()).getName());
		
		summary.append(" (");
		summary.append(d.getYear());
		summary.append(")");
		
		calendar.add(new VEvent(date, summary.toString(), url, show.getId()));
	}
}
