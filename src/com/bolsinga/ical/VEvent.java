package com.bolsinga.ical;

import java.io.*;
import java.text.*;
import java.util.*;

public class VEvent {

	static DateFormat sFormatter = new SimpleDateFormat("yyyyMMdd");
	
	Calendar fDate;
	String fSummary;
	String fURL;
	String fUID;
	
	public VEvent(Calendar date, String summary, String url, String uid) {
		fDate = date;
		fSummary = summary;
		fURL = url;
		fUID = uid;
	}

	public void output(Writer w) {
		PrintWriter pw = new PrintWriter(w, true);
		output(pw);
		pw.close();
	}
	
	public void output(PrintWriter pw) {
		pw.println("BEGIN:VEVENT");

		writeAllDay(pw);
		
		pw.print("UID:");
		pw.println(fUID);
		
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
		pw.println(sFormatter.format(fDate.getTime()));
		
		pw.print("DTEND;VALUE=DATE:");
		pw.println(sFormatter.format(end.getTime()));
	}
}
