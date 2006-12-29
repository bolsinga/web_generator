//
//  SingleElementDocumentCreator.java
//  web_generator
//
//  Created by Greg Bolsinga on 12/29/06.
//  Copyright 2006 Bolsinga Software. All rights reserved.
//
package com.bolsinga.web;

import java.io.*;

import org.apache.ecs.*;

public class SingleElementDocumentCreator extends DocumentCreator {
  private final String fFileName;
  private final String fTitle;
  private final String fDirectory;
  private final Navigator fNavigator;
  
  // This changes during the life-cycle of this object
  private Element  fCurElement  = null;

  public SingleElementDocumentCreator(final Backgrounder backgrounder, final Links links, final String outputDir, final String filename, final String title, final String directory, final Navigator navigator) {
    super(backgrounder, links, outputDir);
    fFileName = filename;
    fTitle = title;
    fDirectory = directory;
    fNavigator = navigator;
  }

  public void add(final Element e) {
    fCurElement = e;
    add();
  }
        
  protected String getTitle() {
    return fTitle;
  }

  protected boolean needNewDocument() {
    return true;
  }

  protected String getLastPath() {
    StringBuilder sb = new StringBuilder();
    if (fDirectory != null) {
      sb.append(fDirectory);
      sb.append(File.separator);
    }
    sb.append(fFileName);
    sb.append(Links.HTML_EXT);
    return sb.toString();
  }

  protected String getCurrentLetter() {
    return null;
  }
    
  protected Element getCurrentElement() {
    return fCurElement;
  }

  protected Navigator getNavigator() {
    return fNavigator;
  }

  protected String getCopyright() {
    return Util.getCopyright(Util.getSettings().getCopyrightStartYear().intValue());
  }
}
