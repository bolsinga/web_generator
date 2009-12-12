package com.bolsinga.test;

import com.bolsinga.web.*;

import java.io.*;

import javax.xml.bind.*;

public class WriteSettings {
                                                       
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: WriteSettings [settings.xml] [output.dir]");
      System.exit(0);
    }

    try {
        Util.createSettings(args[0]);

        com.bolsinga.settings.data.Settings settings = Util.getSettings();
        settings.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(Util.nowUTC()));
        settings.setPageFooter("<script type=\"text/javascript\">var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));</script><script type=\"text/javascript\">try{var pageTracker = _gat._getTracker(\"UA-1597360-1\");pageTracker._trackPageview();} catch(err) {}</script>");
        
        String outputFile = args[1];
        
        OutputStream os = null;
        try {
            try {
                os = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Can't find file: ");
                sb.append(outputFile);
                throw new com.bolsinga.web.WebException(sb.toString(), e);
            }

            try {
                // Write out to the output file.
                JAXBContext jc = JAXBContext.newInstance("com.bolsinga.settings.data");
                Marshaller m = jc.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

                m.marshal(settings, os);
            } catch (JAXBException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Can't marshall: ");
                sb.append(outputFile);
                throw new com.bolsinga.web.WebException(sb.toString(), e);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unable to close: ");
                    sb.append(outputFile);
                    throw new com.bolsinga.web.WebException(sb.toString(), e);
                }
            }
        }
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
