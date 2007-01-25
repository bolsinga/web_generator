package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

class AltDocumentCreator extends DiaryRecordDocumentCreator {

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final String outputDir) {
    AltDocumentCreator creator = new AltDocumentCreator(diary, outputDir);
    creator.createAlt(backgrounder, backgroundable);
  }
  
  private AltDocumentCreator(final Diary diary, final String outputDir) {
    super(diary, outputDir, true);
  }

  protected void createAlt(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new com.bolsinga.web.RecordFactory() {
          public Vector<com.bolsinga.web.Record> getRecords() {
            Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
            items.add(com.bolsinga.web.Record.createRecordSimple(getAlt()));
            return items;
          }
          
          public String getTitle() {
            return com.bolsinga.web.Util.getResourceString("alttitle");
          }
          
          public String getFilePath() {
            StringBuilder sb = new StringBuilder();
            sb.append(com.bolsinga.web.Links.ALT_DIR);
            sb.append(File.separator);
            sb.append("index");
            sb.append(com.bolsinga.web.Links.HTML_EXT);
            return sb.toString();
          }
          
          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getColophonNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("alttitle"));
              }
            };
          }
        });
      }
    });
  }

  private Element getAlt() {
    // Add data from diary colophon
    UL list = com.bolsinga.web.Util.convertToUnOrderedList(fDiary.getColophon());
    
    Vector<Element> e = new Vector<Element>();
    
    {
    // Add email contact
    Object[] args = { com.bolsinga.web.Util.getSettings().getContact(), com.bolsinga.web.Util.getResourceString("program") };
    if (com.bolsinga.web.Util.getDebugOutput()) {
      args[1] = null;
    }
    e.addElement(new A( MessageFormat.format(com.bolsinga.web.Util.getResourceString("mailto"), args),
                        com.bolsinga.web.Util.getResourceString("contact"))); // mailto: URL
    }
    
    {
    // Add the name of the program
    Object[] args = { new Code(com.bolsinga.web.Util.getGenerator()).toString() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generatedby"), args)));
    }
    
    if (!com.bolsinga.web.Util.getDebugOutput()) {
      // Add date generated
      Object[] args = { new Code(com.bolsinga.web.Util.nowUTC().getTime().toString()).toString() };
      e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("generatedon"), args)));
    }
    
    // Add the copyright
    e.addElement(new StringElement(getCopyright()));

    {
    // RSS
    Object[] args = { fLinks.getRSSLink().toString(), fLinks.getRSSAlt() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("singlespace"), args)));
    }
    
    {
    // iCal
    Object[] args = { fLinks.getICalLink().toString(), fLinks.getICalAlt() };
    e.addElement(new StringElement(MessageFormat.format(com.bolsinga.web.Util.getResourceString("singlespace"), args)));
    }

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.COLOPHON);
    d.addElement(com.bolsinga.web.Util.appendToUnorderedList(list, e));
    return d;
  }
}

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
  
  Web(final com.bolsinga.web.Backgrounder backgrounder) {
    fBackgrounder = backgrounder;
    backgrounder.addInterest(this);
  }
  
  void complete() {
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

  public static void generate(final String sourceFile, final String musicFile, final String outputDir) {
    Diary diary = Util.createDiary(sourceFile);

    generate(diary, musicFile, outputDir);
  }
        
  public static void generate(final Diary diary, final String musicFile, final String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);                
    Web web = new Web(backgrounder);
    web.generate(diary, music, encoder, outputDir);
    web.complete();
  }
        
  public void generate(final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    Web.generate(fBackgrounder, this, diary, music, encoder, outputDir);
  }

  public static void generate(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final Music music, final com.bolsinga.web.Encode encoder, final String outputDir) {
    MainDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder, music);

    EntryRecordDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir, encoder);

    AltDocumentCreator.createDocuments(backgrounder, backgroundable, diary, outputDir);
  }
        
  public static Element addItem(final com.bolsinga.web.Encode encoder, final Entry entry, final com.bolsinga.web.Links links, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    e.add(new H3().addElement(com.bolsinga.web.Util.createNamedTarget(entry.getId(), Util.getTitle(entry))));
    e.add(com.bolsinga.web.Util.createPermaLink(links.getLinkTo(entry)));
    e.add(new StringElement(com.bolsinga.web.Util.convertToParagraphs(encoder.embedLinks(entry, upOneLevel))));

    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.ENTRY_ITEM);
    d.addElement(com.bolsinga.web.Util.createUnorderedList(e));
    return d;
  }
}
