//
//  RecordFactory.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/14/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.web;

import java.util.*;

public interface RecordFactory {
  public Vector<Record> getRecords() throws com.bolsinga.web.WebException;
  public String getTitle();
  public String getFilePath();
  public Navigator getNavigator();
}
