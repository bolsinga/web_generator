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
	
	public void output(OutputStream os) {
		PrintWriter pw = new PrintWriter(os, true);
		output(pw);
		pw.close();
	}
	
	public void output(PrintWriter pw) {
		pw.println("BEGIN:VCALENDAR");
		pw.print("X-WR-CALNAME:");
		pw.println(fName);
		pw.println("CALSCALE:GREGORIAN");
		pw.println("VERSION:2.0");
		
		VEvent event = null;
		ListIterator li = fEvents.listIterator();
		while (li.hasNext()) {
			event = (VEvent)li.next();
			
			event.output(pw);
		}
		
		pw.println("END:VCALENDAR");
	}
}
