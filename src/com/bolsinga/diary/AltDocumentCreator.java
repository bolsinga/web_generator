package com.bolsinga.diary;

import com.bolsinga.diary.data.xml.impl.*;
import com.bolsinga.web.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class AltDocumentCreator extends DiaryRecordDocumentCreator {

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Diary diary, final String outputDir) {
    AltDocumentCreator creator = new AltDocumentCreator(diary, outputDir);
    creator.createAlt(backgrounder, backgroundable);
  }
  
  private AltDocumentCreator(final Diary diary, final String outputDir) {
    super(diary, outputDir, true);
  }

  protected void createAlt(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new RecordFactory() {
          public Vector<Record> getRecords() {
            Vector<Record> items = new Vector<Record>(1);
            items.add(Record.createRecordSimple(getAlt()));
            return items;
          }
          
          public String getTitle() {
            return Util.getResourceString("alttitle");
          }
          
          public String getFilePath() {
            return AltDocumentCreator.this.getFilePath();
          }
          
          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getColophonNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("alttitle"));
              }
            };
          }
        });
      }
    });
  }
  
  private String getFilePath() {
    StringBuilder sb = new StringBuilder();
    sb.append(Links.ALT_DIR);
    sb.append(File.separator);
    sb.append("index");
    sb.append(Links.HTML_EXT);
    return sb.toString();
  }

  private Element getAlt() {
    if (!Util.getSettings().isRedirect()) {
      return getRealAlt();
    } else {
      return Util.getRedirectMessage(getFilePath());
    }
  }
  
  private Element getRealAlt() {
    // Add data from diary colophon
    UL list = Util.convertToUnOrderedList(fDiary.getColophon());
    
    Vector<Element> e = new Vector<Element>();
    
    {
    // Add email contact
    Object[] args = { Util.getSettings().getContact(), Util.getResourceString("program") };
    if (Util.getDebugOutput()) {
      args[1] = null;
    }
    e.addElement(new A( MessageFormat.format(Util.getResourceString("mailto"), args),
                        Util.getResourceString("contact"))); // mailto: URL
    }
    
    {
    // Add the name of the program
    Object[] args = { new Code(Util.getGenerator()).toString() };
    e.addElement(new StringElement(MessageFormat.format(Util.getResourceString("generatedby"), args)));
    }
    
    if (!Util.getDebugOutput()) {
      // Add date generated
      Object[] args = { new Code(Util.nowUTC().getTime().toString()).toString() };
      e.addElement(new StringElement(MessageFormat.format(Util.getResourceString("generatedon"), args)));
    }
    
    // Add the copyright
    e.addElement(new StringElement(getCopyright()));

    {
    // RSS
    Object[] args = { fLinks.getRSSLink().toString(), fLinks.getRSSAlt() };
    e.addElement(new StringElement(MessageFormat.format(Util.getResourceString("singlespace"), args)));
    }
    
    {
    // iCal
    Object[] args = { fLinks.getICalLink().toString(), fLinks.getICalAlt() };
    e.addElement(new StringElement(MessageFormat.format(Util.getResourceString("singlespace"), args)));
    }

    Div d = Util.createDiv(CSS.COLOPHON);
    d.addElement(Util.appendToUnorderedList(list, e));
    return d;
  }
}
