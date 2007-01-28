package com.bolsinga.diary;

import com.bolsinga.diary.data.xml.*;
import com.bolsinga.music.data.xml.*;
import com.bolsinga.settings.data.*;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Web implements Backgroundable {

  private static final boolean GENERATE_XML = false;

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 5) {
      Web.usage();
    }

    String settings = args[3];
    String output = args[4];

    Diary diary = null;
    Music music = null;

    try {
      if (args[0].equals("xml")) {
        String diaryFile = args[1];
        String musicFile = args[2];
        
        diary = Util.createDiary(diaryFile);
        music = Util.createMusic(musicFile);
      } else {
        Web.usage();
      }

      Util.createSettings(settings);
    
      if (Web.GENERATE_XML) {
        Web.export(diary);
        System.exit(0);
      }
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    Encode encoder = Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, output);
    web.complete();
  }
  
  private Web(final Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    backgrounder.addInterest(this);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }

  private static void usage() {
    System.out.println("Usage: Web xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
    System.exit(0);
  }

  private static void export(final Diary diary) throws WebException {
    File outputFile = new File("/tmp", "diary_db.xml");

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
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data.xml");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      m.marshal(diary, os);
    } catch (JAXBException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't marshall: ");
      sb.append(os.toString());
      throw new WebException(sb.toString(), e);
    }
  }

  private static void generate(final String sourceFile, final String musicFile, final String outputDir) throws WebException {
    Diary diary = Util.createDiary(sourceFile);

    generate(diary, musicFile, outputDir);
  }
        
  private static void generate(final Diary diary, final String musicFile, final String outputDir) throws WebException {
    Music music = Util.createMusic(musicFile);
    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    Encode encoder = Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, outputDir);
    web.complete();
  }
        
  private void generate(final Diary diary, final Music music, final Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, this, diary, music, encoder, outputDir);
  }

  public static void generate(final Backgrounder backgrounder, final Backgroundable backgroundable, final Diary diary, final Music music, final Encode encoder, final String outputDir) {
    MainDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder, music);

    EntryRecordDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder);

    AltDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir);
  }
}
