package com.bolsinga.web;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public abstract class MultiDocumentCreator extends DocumentCreator {
  private li fSubsection = null;
    
  protected MultiDocumentCreator(String outputDir) {
    super(outputDir);
  }

  protected abstract boolean needNewSubsection();
  protected abstract Element getSubsectionTitle();

  protected void add() {
    getSubsection().addElement(getCurrentElement());
  }

  private li getSubsection() {
    if ((fSubsection == null) || needNewSubsection() || needNewDocument()) {
      div mainDiv = getMain();
            
      ul list = new ul();
      list.setClass(com.bolsinga.web.CSS.DOC_SUB);
      list.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
            
      fSubsection = new li();
      fSubsection.setPrettyPrint(com.bolsinga.web.Util.getPrettyPrint());
      list.addElement(fSubsection);
            
      Element e = getSubsectionTitle();
      if (e != null) {
        fSubsection.addElement(new h2().addElement(e));
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
