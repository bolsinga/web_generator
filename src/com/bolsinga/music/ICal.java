package com.bolsinga.music.ical;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

public class ICal {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: ICal [source.xml] [settings.xml] [output.dir]");
			System.exit(0);
		}

        com.bolsinga.web.util.Util.createSettings(args[1]);
        
		ICal.generate(args[0], args[2]);
	}

	public static void generate(String sourceFile, String outputDir) {
		Music music = Util.createMusic(sourceFile);
		
		generate(music, outputDir);
	}
	
	public static void generate(Music music, String outputDir) {
		OutputStreamWriter w = null;

        String name = com.bolsinga.web.util.Util.getSettings().getIcalName();
		
		try {
			File f = new File(outputDir, "ical/" + name + ".ics");
			File parent = new File(f.getParent());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					System.out.println("Can't: " + parent.getAbsolutePath());
				}
			}
			w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		} catch (IOException ioe) {
			System.err.println("Exception: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
		
		generate(music, name, w);
	}
	
	public static void generate(Music music, String name, Writer w) {
		List items = music.getShow();
		Show item = null;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		
		VCalendar cal = new VCalendar(name);
		
		ListIterator li = items.listIterator();
		while (li.hasNext()) {
			item = (Show)li.next();
			
			if (!item.getDate().isUnknown()) {
				addItem(item, cal);
			}
		}
		
		cal.output(w);
	}
	
	public static void addItem(Show show, VCalendar calendar) {
		com.bolsinga.music.data.Date d = show.getDate();
		Calendar date = Calendar.getInstance();
		date.set(Calendar.MONTH, d.getMonth().intValue() - 1);
		date.set(Calendar.DAY_OF_MONTH, d.getDay().intValue());
		
		StringBuffer summary = new StringBuffer();
		String url = null;
		
		ListIterator bi = show.getArtist().listIterator();
		while (bi.hasNext()) {
			Artist a = (Artist)bi.next();
			
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
