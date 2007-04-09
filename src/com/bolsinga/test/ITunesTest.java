package com.bolsinga.test;

import java.io.*;

import javax.xml.bind.*;

import com.bolsinga.itunes.converter.*;
import com.bolsinga.plist.*;
import com.bolsinga.music.data.xml.impl.*;

public class ITunesTest {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: ITunesTest [itunes] [output]");
      System.exit(0);
    }
    
    try {
      ITunesTest.convert(args[0], args[1]);
    } catch (ITunesException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
        
  private static void convert(final String itunesFile, final String outputFile) throws ITunesException {          
    Music music = ITunesTest.convert(itunesFile);

    music.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(com.bolsinga.web.Util.nowUTC()));

    OutputStream os = null;
    try {
      os = new FileOutputStream(outputFile);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(outputFile);
      throw new ITunesException(sb.toString(), e);
    }
    
    try {
      // Write out to the output file.
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data.xml.impl");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      m.marshal(music, os);
    } catch (JAXBException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't marshall: ");
      sb.append(os);
      throw new ITunesException(sb.toString(), e);
    }
  }
        
  private static Music convert(final String itunesFile) throws ITunesException {
    ObjectFactory objFactory = new ObjectFactory();
                
    Music music = objFactory.createMusic();

    ITunes.addMusic(objFactory, music, itunesFile);
                    
    return music;
  }
}
