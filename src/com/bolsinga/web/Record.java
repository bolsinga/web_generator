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
  
  public static Record createRecordSection(final Element title, final Vector<Record> items) {
    Vector<Element> e = new Vector<Element>(items.size());
    for (Record item : items) {
      e.add(item.getElement());
    }
    
    Element eTitle = null;
    if (title != null) {
      eTitle = new H2().addElement(title);
    }
    
    return new Record(CSS.RECORD_SECTION, eTitle, Util.createUnorderedList(e), null, null);
  }
  
  public static Record createRecordList(final Element title, final Vector<Element> items) {
    return Record.createRecordList(title, items, null);
  }
  
  public static Record createRecordList(final Element title, final Vector<Element> items, final Element curElement) {
    return Record.createRecordListWithComment(title, items, curElement, null);
  }
  
  public static Record createRecordListWithComment(final Element title, final Vector<Element> items, final String comment) {
    return Record.createRecordListWithComment(title, items, null, comment);
  }
  
  private static Record createRecordListWithComment(final Element title, final Vector<Element> items, final Element curElement, final String comment) {
    Element eTitle = null;
    if (title != null) {
      eTitle = new H3().addElement(title);
    }

    return new Record(CSS.RECORD_ITEM_LIST, eTitle, Util.createUnorderedList(items, curElement), comment, null);
  }
  
  public static Record createRecordListOrdered(final Element title, final Vector<Element> items) {
    Element eTitle = null;
    if (title != null) {
      eTitle = new H3().addElement(title);
    }

    return new Record(CSS.RECORD_ITEM_LIST, eTitle, Util.createOrderedList(items), null, null);
  }
  
  public static Record createRecordSimple(final Element element) {
    return new Record(element);
  }
  
  public static Record createRecordPermalink(final Element title, final String comment, final A permalink) {
    Element eTitle = null;
    if (title != null) {
      eTitle = new H3().addElement(title);
    }

    return new Record(CSS.RECORD_ITEM_LIST, eTitle, null, comment, permalink);
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
      d.addElement(permaLink);
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
