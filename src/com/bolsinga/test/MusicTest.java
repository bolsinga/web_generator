package com.bolsinga.test;

import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class MusicTest implements Backgroundable {

  private static final boolean GENERATE_XML = false;

  final Backgrounder fBackgrounder;
   
  public static void main(String[] args) {
    if ((args.length != 4) && (args.length != 5)) {
      MusicTest.usage();
    }

    String type = args[0];

    String settings = null;
    String output = null;

    Music music = null;

    try {
      if (type.equals("xml")) {
        if (args.length != 4) {
          MusicTest.usage();
        }
        
        String musicFile = args[1];
        settings = args[2];
        output = args[3];

        music = com.bolsinga.music.data.xml.Music.create(musicFile);
      } else {
        MusicTest.usage();
      }
  
      Util.createSettings(settings);

      if (MusicTest.GENERATE_XML) {
        MusicTest.export(music);
        System.exit(0);
      }
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    Encode encoder = Encode.getEncode(music, null);
    MusicTest web = new MusicTest(backgrounder);
    web.generate(music, encoder, output);
    web.complete();
  }
  
  private MusicTest(final Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    fBackgrounder.addInterest(this);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }
  
  private static void usage() {
    System.out.println("Usage: MusicTest xml [source.xml] [settings.xml] [output.dir]");
    System.exit(0);
  }
        
  private static void export(final Music music) throws WebException {
    Compare.tidy(music);
    File outputFile = new File("/tmp", "music_db.xml");

    OutputStream os = null;
    try {
      os = new FileOutputStream(outputFile);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(outputFile);
      throw new WebException(sb.toString(), e);
    }

    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data.xml.impl");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      m.marshal(music, os);
    } catch (JAXBException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't marshall: ");
      sb.append(os.toString());
      throw new WebException(sb.toString(), e);
    }
  }

  private void generate(final Music music, final Encode encoder, final String outputDir) {
    MusicTest.generate(fBackgrounder, this, music, encoder, outputDir);
  }

  public static void generate(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final Encode encoder, final String outputDir) {
    ArtistRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);
    
    VenueRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);

    ShowRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, encoder, outputDir);

    CityRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);

    TracksRecordDocumentCreator.createDocuments(backgrounder, backgroundable, music, outputDir);
  }
}
