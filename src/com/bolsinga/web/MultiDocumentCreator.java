package com.bolsinga.web.util;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public abstract class MultiDocumentCreator extends DocumentCreator {
	div fSubsection = null;
    
    protected MultiDocumentCreator(String outputDir) {
        super(outputDir);
    }

    protected abstract boolean needNewSubsection();
    protected abstract Element getSubsectionTitle();

    protected div getContainer() {
        div c = getSubsection();
        if (c == null) {
            c = getMain();
        }
        return c;
    }

    private div getSubsection() {
        if ((fSubsection == null) || needNewSubsection()) {
            div mainDiv = getMain();
            fSubsection = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.DOC_SUB);
            Element e = getSubsectionTitle();
            if (e != null) {
                fSubsection.addElement(new h2().addElement(e));
            }
            mainDiv.addElement(fSubsection);
        }
        return fSubsection;
    }

	protected void writeDocument() {
        fSubsection = null;
        super.writeDocument();
    }
}
