package com.bolsinga.ical;

import java.io.*;
import java.util.*;

public class VEvent {

	Calendar fDate;
	String fSummary;
	String fURL;
	
	public VEvent(Calendar date, String summary, String url) {
		fDate = date;
		fSummary = summary;
		fURL = url;
	}

	public void output(OutputStream os) {
		PrintWriter pw = new PrintWriter(os, true);
		output(pw);
		pw.close();
	}
	
	public void output(PrintWriter pw) {
		pw.println("BEGIN:VEVENT");

		writeAllDay(pw);
		
		pw.print("SUMMARY:");
		pw.println(fSummary);
		
		if (fURL != null) {
			pw.print("URL;VALUE=URI:");
			pw.println(fURL);
		}

		pw.println("RRULE:FREQ=YEARLY;INTERVAL=1");

		pw.println("END:VEVENT");
	}
	
	private void writeAllDay(PrintWriter pw) {
		Calendar end = (Calendar)fDate.clone();
		end.add(Calendar.DATE, 1);

		pw.print("DTSTART;VALUE=DATE:");
		pw.print(fDate.get(Calendar.YEAR));
		int val = fDate.get(Calendar.MONTH);
		if (val < 10) {
			pw.print("0");
		}
		pw.print(val);
		val = fDate.get(Calendar.DAY_OF_MONTH);
		if (val < 10) {
			pw.print("0");
		}
		pw.println(val);
		
		pw.print("DTEND;VALUE=DATE:");
		pw.print(end.get(Calendar.YEAR));
		val = end.get(Calendar.MONTH);
		if (val < 10) {
			pw.print("0");
		}
		pw.print(val);
		val = end.get(Calendar.DAY_OF_MONTH);
		if (val < 10) {
			pw.print("0");
		}
		pw.println(val);
	}
}
