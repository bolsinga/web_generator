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
        try {
            w.write("BEGIN:VEVENT");
            w.write("\r\n");

            writeAllDay(w);
                        
            w.write("UID:");
            w.write(fUID);
            w.write("\r\n");
                        
            w.write("SUMMARY:");
            w.write(fSummary);
            w.write("\r\n");
                        
            if (fURL != null) {
                w.write("URL;VALUE=URI:");
                w.write(fURL);
                w.write("\r\n");
            }

            w.write("RRULE:FREQ=YEARLY;INTERVAL=1");
            w.write("\r\n");

            w.write("END:VEVENT");
            w.write("\r\n");

            w.flush();
        } catch (IOException ioe) {
            System.err.println("Exception: " + ioe);
            ioe.printStackTrace();
            System.exit(1);
        }
    }
        
    private void writeAllDay(Writer w) throws IOException {
        Calendar end = (Calendar)fDate.clone();
        end.add(Calendar.DATE, 1);

        w.write("DTSTART;VALUE=DATE:");
        w.write(sFormatter.format(fDate.getTime()));
        w.write("\r\n");
                
        w.write("DTEND;VALUE=DATE:");
        w.write(sFormatter.format(end.getTime()));
        w.write("\r\n");
    }
}
