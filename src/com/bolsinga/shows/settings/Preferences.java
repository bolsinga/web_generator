package com.bolsinga.shows.settings;

import com.bolsinga.settings.data.*;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class Preferences {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Settings [output.file]");
            System.exit(0);
        }
        
        Preferences.convert(args[0]);
    }
    
    public static void convert(String outputFile) {
        ObjectFactory objFactory = new ObjectFactory();
        
        // Read java properties and populate the Settings.
        try {
            com.bolsinga.settings.data.Settings settings = objFactory.createSettings();

            settings.setContact(System.getProperty("contact"));
            settings.setIco(System.getProperty("web.ico"));
            
            com.bolsinga.settings.data.Image image = objFactory.createImage();
            image.setLocation(System.getProperty("web.logo.url"));
            image.setWidth(new java.math.BigInteger(System.getProperty("web.logo.width")));
            image.setHeight(new java.math.BigInteger(System.getProperty("web.logo.height")));
            image.setAlt(System.getProperty("web.logo.alt"));
            settings.setLogoImage(image);

            image = objFactory.createImage();
            image.setLocation(System.getProperty("ical.image.url"));
            image.setWidth(new java.math.BigInteger(System.getProperty("ical.image.width")));
            image.setHeight(new java.math.BigInteger(System.getProperty("ical.image.height")));
            image.setAlt(System.getProperty("ical.image.alt"));
            settings.setRssImage(image);

            image = objFactory.createImage();
            image.setLocation(System.getProperty("rss.image.url"));
            image.setWidth(new java.math.BigInteger(System.getProperty("rss.image.width")));
            image.setHeight(new java.math.BigInteger(System.getProperty("rss.image.height")));
            image.setAlt(System.getProperty("rss.image.alt"));
            settings.setIcalImage(image);

            settings.setCssFile(System.getProperty("web.layout.css"));
            settings.setRssFile(System.getProperty("rss.url"));
            settings.setDiaryCount(new java.math.BigInteger(System.getProperty("diary.count")));
            settings.setRssCount(new java.math.BigInteger(System.getProperty("rss.count")));
            settings.setRssRoot(System.getProperty("rss.root"));
            settings.setRssDescription(System.getProperty("rss.description"));
            settings.setIcalName(System.getProperty("ical.name"));
            
			settings.setTimestamp(Calendar.getInstance());
            
            Preferences.export(settings, outputFile);
        } catch (JAXBException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
        }
    }
    
    public static void export(com.bolsinga.settings.data.Settings settings, String outputFile) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(outputFile);
        } catch (IOException ioe) {
            System.err.println(ioe);
            ioe.printStackTrace();
            System.exit(1);
        }

        Preferences.export(settings, os);
    }

	public static void export(com.bolsinga.settings.data.Settings settings, OutputStream os) {
        try {
			// Write out to the output file.
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.settings.data");
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			m.marshal(settings, os);
			
		} catch (JAXBException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
    }
}
