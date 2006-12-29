package com.bolsinga.web;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public abstract class MultiDocumentCreator extends DocumentCreator {
  // This changes during the life-cycle of this object
  private LI fSubsection = null;
    
  protected MultiDocumentCreator(final Backgrounder backgrounder, final Links links, final String outputDir) {
    super(backgrounder, links, outputDir);
  }

  protected abstract boolean needNewSubsection();
  protected abstract Element getSubsectionTitle();

  protected void add() {
    getSubsection().addElement(getCurrentElement());
  }

  private LI getSubsection() {
    if ((fSubsection == null) || needNewSubsection() || needNewDocument()) {
      Div mainDiv = getMain();
            
      UL list = new UL();
      list.setClass(com.bolsinga.web.CSS.DOC_SUB);
      list.setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput());
            
      fSubsection = new LI();
      fSubsection.setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput());
      list.addElement(fSubsection);
            
      Element e = getSubsectionTitle();
      if (e != null) {
        fSubsection.addElement(new H2().addElement(e));
      }
            
      mainDiv.addElement(list);
    }
    return fSubsection;
  }

  protected void writeDocument() {
    fSubsection = null;
    super.writeDocument();
  }
}
