package com.bolsinga.web;

import java.io.*;
import java.text.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public abstract class DocumentCreator {
  private final String fOutputDir;

  // These change during the life-cycle of this object
  private XhtmlDocument fDocument = null;
  private div fMain = null;
        
  protected DocumentCreator(final String outputDir) {
    fOutputDir = outputDir;
  }
        
  protected abstract String getTitle();

  protected abstract boolean needNewDocument();
  protected abstract XhtmlDocument createDocument();
  protected abstract div getHeaderDiv();
    
  protected abstract String getLastPath();
  protected abstract String getCurrentLetter();
  protected abstract Element getCurrentElement();
  protected abstract Element addIndexNavigator();
        
  public void close() {
    if (fDocument != null) {
      writeDocument();
      fDocument = null;
    }
  }
        
  protected void add() {
    getMain().addElement(getCurrentElement());
  }
    
  protected div getMain() {
    if ((fDocument == null) || needNewDocument()) {
      close();
            
      fDocument = createDocument();
      fDocument.getBody().addElement(getHeaderDiv());
                        
      fMain = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DOC_MAIN);
    }
    return fMain;
  }
        
  protected void writeDocument() {
    fDocument.getBody().addElement(fMain);

    try {
      File f = new File(fOutputDir, getLastPath());
      File parent = new File(f.getParent());
      if (!parent.exists()) {
        if (!parent.mkdirs()) {
          System.out.println("Can't: " + parent.getAbsolutePath());
        }
      }
      OutputStream os = new FileOutputStream(f);
      fDocument.output(os);
      os.close();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
        
  protected String getTitle(final String type) {
    Object[] args = { getCurrentLetter(), type };
    return MessageFormat.format(com.bolsinga.web.Util.getResourceString("htmltitle"), args);
  }
        
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }
}
