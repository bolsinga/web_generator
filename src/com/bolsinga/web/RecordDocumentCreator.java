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
      java.net.URI uri = new java.net.URI(rootURL);
      String hostName = uri.getHost();
      String[] parts = hostName.split("\\.");
      if (parts.length >= 2) {
        StringBuilder sb = new StringBuilder(parts[parts.length - 2]);
        sb.append(".");
        sb.append(parts[parts.length - 1]);
        fSiteRoot = sb.toString();
      }
    } catch (java.net.URISyntaxException e) {
      // drop it.
    }
  }
        
  protected RecordDocumentCreator(final Links links, final String outputDir) {
    fLinks = links;
    fOutputDir = outputDir;
  }
  
  protected void create(final RecordFactory factory) {
    Document d = populate(factory);
    writeDocument(factory.getFilePath(), d);
  }
  
  protected Document populate(final RecordFactory factory) {
    Document d = createDocument(factory);
    
    Div main = Util.createDiv(getMainDivClass());
        
    Vector<com.bolsinga.web.Record> records = factory.getRecords();
    for (com.bolsinga.web.Record record : records) {
      main.addElement(record.getElement());
    }
    
    d.getBody().addElement(main);
    
    return d;
  }
  
  public void createRedirectDocument(final RedirectFactory factory) {
    Document d = new Document(ECSDefaults.getDefaultCodeset());

    d.getHtml().setPrettyPrint(true);

    d.setDoctype(new org.apache.ecs.Doctype.Html401Strict());

    Head h = d.getHead();
    h.setPrettyPrint(true);

    h.addElement(new Meta().setContent("0;url=" + factory.getInternalURL()).setHttpEquiv("refresh"));
    h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
    h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));

    writeDocument(factory.getFilePath(), d);
  }

  protected abstract String getCopyright();
  
  protected String getMainDivClass() {
    return CSS.DOC_2_COL_BODY;
  }
  
  protected String getSitePageTitle(final String factoryTitle) {
    Object[] args = { fSiteRoot, factoryTitle };
    return MessageFormat.format(Util.getResourceString("sitepagetitle"), args);
  }
  
  private Document createDocument(final RecordFactory factory) {
    Document d = new Document(ECSDefaults.getDefaultCodeset());
                
    d.getHtml().setPrettyPrint(true);
                
    d.setDoctype(new org.apache.ecs.Doctype.Html401Strict());
    d.appendTitle(getSitePageTitle(factory.getTitle()));
                
    Head h = d.getHead();
    h.setPrettyPrint(true);
    h.addElement(Util.getIconLink());
    h.addElement(Util.getWebClipIcon());
    h.addElement(fLinks.getLinkToRSS());
    h.addElement(fLinks.getLinkToStyleSheet());
                
    h.addElement(new Meta().setContent("text/html; charset=" + d.getCodeset()).setHttpEquiv("Content-Type"));
    h.addElement(new Meta().setContent(System.getProperty("user.name")).setName("Author"));
    if (!Util.getDebugOutput()) {
      h.addElement(new Meta().setContent(Util.nowUTC().getTime().toString()).setName("Date"));
    }
    h.addElement(new Meta().setContent(Util.getGenerator()).setName("Generator"));
    h.addElement(new Meta().setContent(getCopyright()).setName("Copyright"));
    
    d.getBody().setPrettyPrint(true);

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

    e.add(Util.getSearchForm());

    Div indexNavigator = Util.createDiv(CSS.ENTRY_INDEX);
    indexNavigator.addElement(Util.createUnorderedList(e, navigator.getCurrentNavigator()));
    
    d.addElement(indexNavigator);
    
    return d;
  }
        
  private void writeDocument(final String filePath, final Document d) {
    File f = new File(fOutputDir, filePath);
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.err.println("RecordDocumentCreator cannot mkdirs: " + parent.getAbsolutePath());
      }
    }

    try (OutputStream os = new FileOutputStream(f)) {
      d.output(os);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("The RecordDocument: ");
      sb.append(f);
      sb.append(" could not be written.");
      System.err.println(sb.toString());
      System.err.println(e);
      e.printStackTrace();
    }
  }
}
