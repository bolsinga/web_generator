package com.bolsinga.web.util;

import java.io.*;
import java.text.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public abstract class DocumentCreator {
	String fOutputDir = null;
	private XhtmlDocument fDocument = null;
	div fMain = null;
	div fSubsection = null;
	
	protected DocumentCreator(String outputDir) {
		fOutputDir = outputDir;
	}
	
	protected abstract String getTitle();

	protected abstract boolean needNewDocument();
    protected abstract XhtmlDocument createDocument();
    protected abstract div getHeaderDiv();
    protected abstract boolean needNewSubsection();
    protected abstract Element getSubsectionTitle();
    
	protected abstract String getLastPath();
	protected abstract String getCurrentLetter();
	protected abstract Element addIndexNavigator();
	
	public void close() {
		if (fDocument != null) {
			writeDocument();
			fDocument = null;
		}
	}
	
    protected div getContainer() {
        div c = getSubsection();
        if (c == null) {
            c = getMain();
        }
        return c;
    }
    
	private div getMain() {
		if ((fDocument == null) || needNewDocument()) {
            close();
            
			fDocument = createDocument();
			fDocument.getBody().addElement(getHeaderDiv());
			
			fMain = com.bolsinga.web.util.Util.createDiv(com.bolsinga.web.util.CSS.DOC_MAIN);
		}
		return fMain;
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
	
	private void writeDocument() {
		if (fSubsection != null) {
            fSubsection = null;
		}
		
		fDocument.getBody().addElement(fMain);

		try {
			File f = new File(fOutputDir, getLastPath());
			File parent = new File(f.getParent());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					System.out.println("Can't: " + parent.getAbsolutePath());
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
	
	protected String getTitle(String type) {
        Object[] args = { getCurrentLetter(), type };
        return MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("htmltitle"), args);
	}
	
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
