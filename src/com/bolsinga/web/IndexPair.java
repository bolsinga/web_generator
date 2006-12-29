//
//  IndexPair.java
//  web_generator
//
//  Created by Greg Bolsinga on 12/28/06.
//  Copyright 2006 Bolsinga Software. All rights reserved.
//

package com.bolsinga.web;

public class IndexPair {
  public final String fLink;
  public final String fTitle;
  
  public IndexPair(final String link, final String title) {
    fLink = link;
    fTitle = title;
  }
  
  public final String getLink() {
    return fLink;
  }
  
  public final String getTitle() {
    return fTitle;
  }
}
