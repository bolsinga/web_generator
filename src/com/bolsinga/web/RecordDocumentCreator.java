package com.bolsinga.web;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public abstract class RecordDocumentCreator implements Backgroundable {

  protected final Links fLinks;
  private final String fOutputDir;
  private static String fSiteRoot;
  
  static {
    String rootURL = Util.getSettings().getRoot();
    fSiteRoot = rootURL;
    try {
      String[] parts = new java.net.URL(rootURL).getHost().split("\\.");
      if (parts.length >= 2) {
        StringBuilder sb = new StringBuilder(parts[parts.length - 2]);
        sb.append(".");
        sb.append(parts[parts.length - 1]);
        fSiteRoot = sb.toString();
      }
    } catch (java.net.MalformedURLException e) {
      // drop it.
    }
  }
        
  protected RecordDocumentCreator(final Links links, final String outputDir) {
    fLinks = links;
    fOutputDir = outputDir;
  }
  
  protected void create(final RecordFactory factory) {
    try {
        Document d = populate(factory);
        writeDocument(factory, d);
    } catch (Exception e) {
        // Catch this here, since this is typically called by Runnable.run().
        System.err.println(e);
        e.printStackTrace();
        System.exit(1);
    }
  }
  
  protected Document populate(final RecordFactory factory) throws com.bolsinga.web.WebException {
    Document d = createDocument(factory);
    
    Div main = Util.createDiv(getMainDivClass());
        
    Vector<Record> records = factory.getRecords();
    for (Record record : records) {
      main.addElement(record.getElement());
    }
    
    if (Encode.requiresTransitional(main.toString())) {
      System.out.println("Requires HTML410Transitional: " + getSitePageTitle(factory.getTitle()));
      d.setDoctype(new org.apache.ecs.Doctype.Html401Transitional());
    }
    d.getBody().addElement(main);
    
    return d;
  }
  
  protected abstract String getCopyright();
  
  protected String getMainDivClass() {
    return CSS.DOC_2_COL_BODY;
  }
  
  protected Meta getAdditionalMeta() {
    return null;
  }
  
  protected String getSitePageTitle(final String factoryTitle) {
    Object[] args = { fSiteRoot, factoryTitle };
    return MessageFormat.format(Util.getResourceString("sitepagetitle"), args);
  }
  
  private Document createDocument(final RecordFactory factory) {
    Document d = new Document(ECSDefaults.getDefaultCodeset());
                
    d.getHtml().setPrettyPrint(Util.getPrettyOutput());
                
    d.setDoctype(new org.apache.ecs.Doctype.Html401Strict());
    d.appendTitle(getSitePageTitle(factory.getTitle()));
                
    Head h = d.getHead();
    h.setPrettyPrint(Util.getPrettyOutput());
    h.addElement(Util.getIconLink());
    h.addElement(Util.getWebClipIcon());
    h.addElement(fLinks.getLinkToRSS());
    h.addElement(fLinks.getLinkToStyleSheet());
    h.addElement(fLinks.getLinkToScript());
                
    h.addElement(new Meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
    h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
    if (!Util.getDebugOutput()) {
      h.addElement(new Meta().setContent(Util.nowUTC().getTime().toString()).setName("Date"));
    }
    h.addElement(new Meta().setContent(Util.getGenerator()).setName("Generator"));
    h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));
    
    Meta additional = getAdditionalMeta();
    if (additional != null) {
      h.addElement(additional);
    }
    
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
        
  private void writeDocument(final RecordFactory factory, final Document d) throws com.bolsinga.web.WebException {
    d.getBody().addElement(Util.getSettings().getPageFooter());
    
    File f = new File(fOutputDir, factory.getFilePath());
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.err.println("RecordDocumentCreator cannot mkdirs: " + parent.getAbsolutePath());
      }
    }

    try {
      OutputStream os = null;
      try {
        os = new FileOutputStream(f);
        d.output(os);
      } finally {
        if (os != null) {
          os.close();
        }
      }
    } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't write RecordDocument file: ");
        sb.append(f);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
    }
  }
}
