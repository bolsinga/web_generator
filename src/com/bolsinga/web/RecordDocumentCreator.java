package com.bolsinga.web;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public abstract class RecordDocumentCreator implements Backgroundable {

  protected final Links fLinks;
  private final String fOutputDir;
        
  protected RecordDocumentCreator(final Links links, final String outputDir) {
    fLinks = links;
    fOutputDir = outputDir;
  }
  
  protected void create() {
    Document d = populate();
    writeDocument(d);
  }
  
  private Document populate() {
    Document d = createDocument();
    
    Div main = Util.createDiv(getMainDivClass());
        
    Vector<Record> records = getRecords();
    for (Record record : records) {
      main.addElement(record.getElement());
    }
    
    if (Encode.requiresTransitional(main.toString())) {
      d.setDoctype(new org.apache.ecs.Doctype.Html401Transitional());
    }
    d.getBody().addElement(main);
    
    return d;
  }
  
  protected abstract String getTitle();
  protected abstract String getCopyright();
  protected abstract Navigator getNavigator();
  protected abstract String getMainDivClass();
  protected abstract String getFilePath();
  protected abstract Vector<Record> getRecords();
  
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

    d.getBody().addElement(getHeaderDiv());
    
    return d;
  }
  
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
        
  private void writeDocument(Document d) {
    try {
      File f = new File(fOutputDir, getFilePath());
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("RecordDocumentCreator cannot mkdirs: " + parent.getAbsolutePath());
        }
      }
      OutputStream os = new FileOutputStream(f);
      d.output(os);
      os.close();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
}
