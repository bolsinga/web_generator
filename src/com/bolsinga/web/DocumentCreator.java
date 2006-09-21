package com.bolsinga.web;

import java.io.*;
import java.text.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

class DocWriter implements Runnable {
  private final Document fDoc;
  private final String fOutputDir;
  private final String fLastPath;
  
  public DocWriter(final Document doc, final String outputDir, final String lastPath) {
    fDoc = doc;
    fOutputDir = outputDir;
    fLastPath = lastPath;
  }
  
  public void run() {
    DocWriter.writeFile(fDoc, fOutputDir, fLastPath);
  }
  
  public static void writeFile(final Document doc, final String outputDir, final String lastPath) {
    try {
      File f = new File(outputDir, lastPath);
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("DocumentCreator cannot mkdirs: " + parent.getAbsolutePath());
        }
      }
      OutputStream os = new FileOutputStream(f);
      doc.output(os);
      os.close();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
}

public abstract class DocumentCreator implements Backgroundable {

  private static final boolean sUseAsynchronousIO = Boolean.getBoolean("web.asynchronousio");

  private final Backgrounder fBackgrounder;
  private final String fOutputDir;

  // These change during the life-cycle of this object
  private Document fDocument = null;
  private Div fMain = null;
        
  protected DocumentCreator(final Backgrounder backgrounder, final String outputDir) {
    fBackgrounder = backgrounder;
    fOutputDir = outputDir;
    fBackgrounder.addInterest(this);
  }
        
  protected abstract String getTitle();

  protected abstract boolean needNewDocument();
  protected abstract Document createDocument();
  protected abstract Div getHeaderDiv();
    
  protected abstract String getLastPath();
  protected abstract String getCurrentLetter();
  protected abstract Element getCurrentElement();
  protected abstract Element addIndexNavigator();
  
  public void complete() {
    close();
    fBackgrounder.removeInterest(this);
  }
  
  void close() {
    if (fDocument != null) {
      writeDocument();
      fDocument = null;
    }
  }
        
  protected void add() {
    getMain().addElement(getCurrentElement());
  }
    
  protected Div getMain() {
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

    if (DocumentCreator.sUseAsynchronousIO) {
      fBackgrounder.execute(this, new DocWriter(fDocument, fOutputDir, getLastPath()));
    } else {
      DocWriter.writeFile(fDocument, fOutputDir, getLastPath());
    }
  }
        
  protected String getTitle(final String type) {
    Object[] args = { getCurrentLetter(), type };
    return MessageFormat.format(com.bolsinga.web.Util.getResourceString("htmltitle"), args);
  }
        
  protected void finalize() throws Throwable {
    complete();
    super.finalize();
  }
}
