package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class Web implements com.bolsinga.web.Backgroundable {

  private static final boolean GENERATE_XML = false;

  private final com.bolsinga.web.Backgrounder fBackgrounder;
  
  public static void main(String[] args) {
    if (args.length != 5) {
      Web.usage();
    }

    String settings = args[3];
    String output = args[4];

    Diary diary = null;
    Music music = null;

    if (args[0].equals("xml")) {
      String diaryFile = args[1];
      String musicFile = args[2];
      
      diary = Util.createDiary(diaryFile);
      music = com.bolsinga.music.Util.createMusic(musicFile);
    } else if (args[0].equals("db")) {
      String user = args[1];
      String password = args[2];

      diary = MySQLCreator.createDiary(user, password);
      music = com.bolsinga.music.MySQLCreator.createMusic(user, password);
    } else {
      Web.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);
    
    if (Web.GENERATE_XML) {
      Web.export(diary);
      System.exit(0);
    }

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, output);
    web.complete();
  }
  
  private Web(final com.bolsinga.web.Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    backgrounder.addInterest(this);
  }
  
  private void complete() {
    fBackgrounder.removeInterest(this);
  }

  private static void usage() {
    System.out.println("Usage: Web xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: Web db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }

  private static void export(final Diary diary) {
    try {
      File outputFile = new File("/tmp", "diary_db.xml");

      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data");
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
      m.marshal(diary, os);
    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void generate(final String sourceFile, final String musicFile, final String outputDir) {
    Diary diary = Util.createDiary(sourceFile);

    generate(diary, musicFile, outputDir);
  }
        
  private static void generate(final Diary diary, final String musicFile, final String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, outputDir);
    web.complete();
  }
        
  private void generate(final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, this, diary, music, encoder, outputDir);
  }

  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    MainDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder, music);

    EntryRecordDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder);

    AltDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir);
  }
        
  static Element addItem(final com.bolsinga.web.Encode encoder, final Entry entry, final com.bolsinga.web.Links links, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    e.add(new H3().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(com.bolsinga.web.Util.createPermaLink(links.getLinkTo(entry)));
    e.add(new StringElement(com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(entry, upOneLevel))));

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}
