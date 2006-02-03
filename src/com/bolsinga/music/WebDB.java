package com.bolsinga.music;

import java.io.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.data.*;

public class WebDB {
  private static final boolean GENERATE_XML = false;

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: WebDB [user] [password] [settings.xml] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    WebDB.generate(args[0], args[1], args[3]);
  }

  public static void generate(String user, String password, String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(user, password);
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, null);

    Web.generate(music, encoder, outputDir);

    if (WebDB.GENERATE_XML) {
      WebDB.export(music);
    }
  }

  private static void export(Music music) {
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
}
