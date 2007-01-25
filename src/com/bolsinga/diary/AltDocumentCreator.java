package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class AltDocumentCreator extends DiaryRecordDocumentCreator {

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
