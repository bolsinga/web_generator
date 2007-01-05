package com.bolsinga.web;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public abstract class DocumentCreator implements Backgroundable {

  protected final Links fLinks;
  private final String fOutputDir;

  // These change during the life-cycle of this object
  private Document fDocument = null;
  private Div fMain = null;
        
  protected DocumentCreator(final Links links, final String outputDir) {
    fLinks = links;
    fOutputDir = outputDir;
  }
        
  protected abstract String getTitle();

  protected abstract boolean needNewDocument();
  
  protected abstract String getCopyright();
  
  private Document createDocument() {
    Document d = new Document(ECSDefaults.getDefaultCodeset());
                
    d.getHtml().setPrettyPrint(Util.getPrettyOutput());
                
    d.setDoctype(new org.apache.ecs.Doctype.Html401Strict());
    d.appendTitle(getTitle());
                
    Head h = d.getHead();
    h.setPrettyPrint(Util.getPrettyOutput());
    h.addElement(Util.getIconLink());
    h.addElement(fLinks.getLinkToRSS());
    h.addElement(fLinks.getLinkToStyleSheet());
                
    h.addElement(new Meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
    h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
    if (!Util.getDebugOutput()) {
      h.addElement(new Meta().setContent(Util.nowUTC().getTime().toString()).setName("Date"));
    }
    h.addElement(new Meta().setContent(Util.getGenerator()).setName("Generator"));
    h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));

    d.getBody().setPrettyPrint(Util.getPrettyOutput());
                                                
    return d;
  }

  protected abstract Navigator getNavigator();
  
  private Div getHeaderDiv() {
    Div d = Util.createDiv(CSS.NAV_HEADER);
    d.addElement(new H1().addElement(getTitle()));
    d.addElement(Util.getLogo());

    Navigator navigator = getNavigator();
    Vector<Element> e = new Vector<Element>();
    e.add(navigator.getHomeNavigator());
    e.add(navigator.getOverviewNavigator());
    e.add(navigator.getArtistNavigator());
    e.add(navigator.getShowNavigator());
    e.add(navigator.getVenueNavigator());
    e.add(navigator.getCityNavigator());
    e.add(navigator.getTrackNavigator());
    e.add(navigator.getAlbumNavigator());
    e.add(navigator.getColophonNavigator());
    
    Div indexNavigator = Util.createDiv(CSS.ENTRY_INDEX);
    indexNavigator.addElement(Util.createUnorderedList(e, navigator.getCurrentNavigator()));
    
    d.addElement(indexNavigator);
    
    return d;
  }
    
  protected abstract String getLastPath();
  protected abstract String getCurrentLetter();
  protected abstract Element getCurrentElement();
  
  public void complete() {
    close();
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
  
  protected boolean isTwoColumn() {
    return true;
  }
    
  protected Div getMain() {
    if ((fDocument == null) || needNewDocument()) {
      close();
            
      fDocument = createDocument();
      fDocument.getBody().addElement(getHeaderDiv());
      
      String divType = isTwoColumn() ? CSS.DOC_2_COL_BODY : CSS.DOC_3_COL_BODY;
                        
      fMain = Util.createDiv(divType);
    }
    return fMain;
  }
        
  protected void writeDocument() {
    if (Encode.requiresTransitional(fMain.toString())) {
      fDocument.setDoctype(new org.apache.ecs.Doctype.Html401Transitional());
    }
    fDocument.getBody().addElement(fMain);

    try {
      File f = new File(fOutputDir, getLastPath());
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("DocumentCreator cannot mkdirs: " + parent.getAbsolutePath());
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
    return Util.createPageTitle(getCurrentLetter(), type);
  }
        
  protected void finalize() throws Throwable {
    complete();
    super.finalize();
  }
}
