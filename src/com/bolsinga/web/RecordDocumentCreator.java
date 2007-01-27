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
  
  protected void create(final RecordFactory factory) {
    Document d = populate(factory);
    try {
      writeDocument(factory, d);
    } catch (WebException e) {
      // TODO: What to do with these exceptions while running in Backgrounder!!!
      System.err.println(e);
      e.printStackTrace();
    }
  }
  
  private Document populate(final RecordFactory factory) {
    Document d = createDocument(factory);
    
    Div main = Util.createDiv(getMainDivClass());
        
    Vector<Record> records = factory.getRecords();
    for (Record record : records) {
      main.addElement(record.getElement());
    }
    
    if (Encode.requiresTransitional(main.toString())) {
      d.setDoctype(new org.apache.ecs.Doctype.Html401Transitional());
    }
    d.getBody().addElement(main);
    
    return d;
  }
  
  protected abstract String getCopyright();
  protected abstract String getMainDivClass();
  
  private Document createDocument(final RecordFactory factory) {
    Document d = new Document(ECSDefaults.getDefaultCodeset());
                
    d.getHtml().setPrettyPrint(Util.getPrettyOutput());
                
    d.setDoctype(new org.apache.ecs.Doctype.Html401Strict());
    d.appendTitle(factory.getTitle());
                
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

    d.getBody().addElement(getHeaderDiv(factory));
    
    return d;
  }
  
  private Div getHeaderDiv(final RecordFactory factory) {
    Div d = Util.createDiv(CSS.NAV_HEADER);
    d.addElement(new H1().addElement(factory.getTitle()));
    d.addElement(Util.getLogo());

    Navigator navigator = factory.getNavigator();
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
        
  private void writeDocument(final RecordFactory factory, final Document d) throws WebException {
    File f = new File(fOutputDir, factory.getFilePath());
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.err.println("RecordDocumentCreator cannot mkdirs: " + parent.getAbsolutePath());
      }
    }

    OutputStream os = null;
    try {
      os = new FileOutputStream(f);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(f.toString());
      throw new WebException(sb.toString(), e);
    }
    
    d.output(os);
    try {
      os.close();
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't close file: ");
      sb.append(os.toString());
      throw new WebException(sb.toString(), e);
    }
  }
}
