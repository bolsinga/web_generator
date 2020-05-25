//
//  Record.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/5/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.web;

import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class Record {
  private final Element fElement;
  
  public static com.bolsinga.web.Record createRecordSection(final Element title, final Vector<com.bolsinga.web.Record> items) {
    Vector<Element> e = new Vector<Element>(items.size());
    for (com.bolsinga.web.Record item : items) {
      e.add(item.getElement());
    }
    
    Element eTitle = null;
    if (title != null) {
      eTitle = new H2().addElement(title);
    }
    
    return new com.bolsinga.web.Record(CSS.RECORD_SECTION, eTitle, Util.createUnorderedList(e), null, null);
  }
  
  public static com.bolsinga.web.Record createRecordList(final Element title, final Vector<Element> items) {
    return com.bolsinga.web.Record.createRecordList(title, items, null);
  }
  
  public static com.bolsinga.web.Record createRecordList(final Element title, final Vector<Element> items, final Element curElement) {
    return com.bolsinga.web.Record.createRecordListWithComment(title, items, curElement, null);
  }
  
  public static com.bolsinga.web.Record createRecordListWithComment(final Element title, final Vector<Element> items, final String comment) {
    return com.bolsinga.web.Record.createRecordListWithComment(title, items, null, comment);
  }
  
  private static com.bolsinga.web.Record createRecordListWithComment(final Element title, final Vector<Element> items, final Element curElement, final String comment) {
    Element eTitle = null;
    if (title != null) {
      eTitle = new H3().addElement(title);
    }

    return new com.bolsinga.web.Record(CSS.RECORD_ITEM_LIST, eTitle, Util.createUnorderedList(items, curElement), comment, null);
  }
  
  public static com.bolsinga.web.Record createRecordListOrdered(final Element title, final Vector<Element> items) {
    Element eTitle = null;
    if (title != null) {
      eTitle = new H3().addElement(title);
    }

    return new com.bolsinga.web.Record(CSS.RECORD_ITEM_LIST, eTitle, Util.createOrderedList(items), null, null);
  }
  
  public static com.bolsinga.web.Record createRecordSimple(final Element element) {
    return new com.bolsinga.web.Record(element);
  }
  
  public static com.bolsinga.web.Record createRecordPermalink(final Element title, final String comment, final A permalink) {
    Element eTitle = null;
    if (title != null) {
      eTitle = new H3().addElement(title);
    }

    return new com.bolsinga.web.Record(CSS.RECORD_ITEM_LIST, eTitle, null, comment, permalink);
  }
  
  private Record(final String divClass, final Element title, final Element items, final String comment, final A permaLink) {
    Div d = Util.createDiv(divClass);
    
    if (title != null) {
      d.addElement(title);
    }
    
    if (items != null) {
      d.addElement(items);
    }
    
    if (comment != null) {
      d.addElement(Util.convertToParagraphs(comment));
    }
    
    if (permaLink != null) {
      StringElement se = new StringElement("&nbsp;");
      se.addElement(permaLink);
      d.addElement(se);
    }
    
    fElement = d;
  }
  
  private Record(final Element element) {
    fElement = element;
  }
  
  public Element getElement() {
    return fElement;
  }
}
