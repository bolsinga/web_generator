package com.bolsinga.test;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import com.bolsinga.diary.*;
import com.bolsinga.web.*;

import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class DiaryTest implements Backgroundable {

  private static final boolean GENERATE_XML = false;

  private final Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 5) {
      DiaryTest.usage();
    }

    String settings = args[3];
    String output = args[4];

    Diary diary = null;
    Music music = null;

    try {
      if (args[0].equals("xml")) {
        String diaryFile = args[1];
        String musicFile = args[2];
        
        diary = com.bolsinga.diary.data.xml.Diary.create(diaryFile);
        music = com.bolsinga.music.data.xml.Music.create(musicFile);
      } else {
        DiaryTest.usage();
      }

      Util.createSettings(settings);
    
      if (DiaryTest.GENERATE_XML) {
        DiaryTest.export(diary);
        System.exit(0);
      }
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }

    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    Encode encoder = Encode.getEncode(music, diary);                
    DiaryTest web = new DiaryTest(backgrounder);
    web.generate(diary, music, encoder, output);
    web.complete();
  }
  
  private DiaryTest(final Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    backgrounder.addInterest(this);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }

  private static void usage() {
    System.out.println("Usage: DiaryTest xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
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
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data.xml.impl");
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
    Diary diary = com.bolsinga.diary.data.xml.Diary.create(sourceFile);

    generate(diary, musicFile, outputDir);
  }
        
  private static void generate(final Diary diary, final String musicFile, final String outputDir) throws WebException {
    Music music = com.bolsinga.music.data.xml.Music.create(musicFile);
    Backgrounder backgrounder = Backgrounder.getBackgrounder();
    Encode encoder = Encode.getEncode(music, diary);                
    DiaryTest web = new DiaryTest(backgrounder);
    web.generate(diary, music, encoder, outputDir);
    web.complete();
  }
        
  private void generate(final Diary diary, final Music music, final Encode encoder, final String outputDir) {
    DiaryTest.generate(fBackgrounder, this, diary, music, encoder, outputDir);
  }

  public static void generate(final Backgrounder backgrounder, final Backgroundable backgroundable, final Diary diary, final Music music, final Encode encoder, final String outputDir) {
    MainDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder, music);

    EntryRecordDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder);

    AltDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir);
  }
}
