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
    
    return new Record(CSS.RECORD_SECTION, new H2().addElement(title), Util.createUnorderedList(e), null, null);
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
    return new Record(CSS.RECORD_ITEM_LIST, new H3().addElement(title), Util.createUnorderedList(items, curElement), comment, null);
  }
  
  public static Record createRecordListOrdered(final Element title, final Vector<Element> items) {
    return new Record(CSS.RECORD_ITEM_LIST, new H3().addElement(title), Util.createOrderedList(items), null, null);
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
  
  public Element getElement() {
    return fElement;
  }
}
