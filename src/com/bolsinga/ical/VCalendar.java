package com.bolsinga.ical;

import java.io.*;
import java.util.*;

public class VCalendar {
	Vector fEvents = new Vector();
	String fName = null;
	
	public VCalendar(String name) {
		fName = name;
	}
	
	public void add(VEvent event) {
		fEvents.add(event);
	}
	
	public void output(Writer w) {
		try {
			w.write("BEGIN:VCALENDAR");
			w.write("\r\n");
			w.write("X-WR-CALNAME:");
			w.write(fName);
			w.write("\r\n");
			w.write("CALSCALE:GREGORIAN");
			w.write("\r\n");
			w.write("VERSION:2.0");
			w.write("\r\n");
			
			VEvent event = null;
			ListIterator li = fEvents.listIterator();
			while (li.hasNext()) {
				event = (VEvent)li.next();
				
				event.output(w);
			}
			
			w.write("END:VCALENDAR");
			w.write("\r\n");
			
			w.flush();
		} catch (IOException ioe) {
			System.err.println("Exception: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
	}
}
