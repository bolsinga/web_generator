//
//  TableHandler.java
//  web_generator
//
//  Created by Greg Bolsinga on 12/10/06.
//  Copyright 2006 Bolsinga Software. All rights reserved.
//

package com.bolsinga.web;

import org.apache.ecs.html.*;

public interface TableHandler {
  public TR getHeaderRow();
  public int getRowCount();
  public TR getRow(final int row);
  public TR getFooterRow();
}
