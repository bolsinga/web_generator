package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import com.bolsinga.web.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

public class Web implements Backgroundable {

  private static final boolean GENERATE_XML = false;

  final Backgrounder fBackgrounder;
   
  public static void main(String[] args) {
    if ((args.length != 4) && (args.length != 5)) {
      Web.usage();
    }

    String type = args[0];

    String settings = null;
    String output = null;

    Music music = null;

    if (type.equals("xml")) {
      if (args.length != 4) {
        Web.usage();
      }
      
      String musicFile = args[1];
      settings = args[2];
      output = args[3];

      music = Util.createMusic(musicFile);
    } else if (type.equals("db")) {
      if (args.length != 5) {
        Web.usage();
      }

      String user = args[1];
      String password = args[2];
      settings = args[3];
      output = args[4];

      music = MySQLCreator.createMusic(user, password);
    } else {
      Web.usage();
    }

    Util.createSettings(settings);

    if (Web.GENERATE_XML) {
      Web.export(music);
      System.exit(0);
    }

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    Encode encoder = Encode.getEncode(music, null);
    Web web = new Web(backgrounder);
    web.generate(music, encoder, output);
    web.complete();
  }
  
  private Web(final Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }
  
  private static void usage() {
    System.out.println("Usage: Web xml [source.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }
        
  private static void export(final Music music) {
    Compare.tidy(music);
    try {
      File outputFile = new File("/tmp", "music_db.xml");

      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      OutputStream os = null;
      try {
        os = new FileOutputStream(outputFile);
      } catch (IOException ioe) {
        System.err.println(ioe);
        ioe.printStackTrace();
        System.exit(1);
      }
      m.marshal(music, os);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void generate(final Music music, final Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, this, music, encoder, outputDir);
  }

  public static void generate(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final Encode encoder, final String outputDir) {
    ArtistRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);
    
    VenueRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);

    ShowRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, encoder, outputDir);

    CityRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);

    TracksRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);
  }
        
  static Vector<Element> getShowListing(final Lookup lookup, final Links links, final Show show) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
                        
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(Util.createInternalA(links.getLinkTo(performer), lookup.getHTMLName(performer), t));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = (Venue)show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = Util.createInternalA(links.getLinkTo(venue), lookup.getHTMLName(venue), t);
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                
    return e;
  }

  // Used by the unified (diary & music) main page.
  public static Element addItem(final Encode encoder, final Lookup lookup, final Links links, final Show show, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();

    e.add(new H3().addElement(Util.createNamedTarget(show.getId(), Util.toString(show.getDate()))));

    e.add(Util.createUnorderedList(Web.getShowListing(lookup, links, show)));

    String comment = show.getComment();
    if (comment != null) {
      e.add(new StringElement(Util.convertToParagraphs(encoder.embedLinks(show, upOneLevel))));
    }

    Div d = Util.createDiv(CSS.ENTRY_ITEM);
    d.addElement(Util.createUnorderedList(e));
    return d;
  }
}
