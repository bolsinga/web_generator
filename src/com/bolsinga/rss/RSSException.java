//
//  RSSException.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/27/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.rss;

public class RSSException extends Exception {
  static final long serialVersionUID = 995772941382329618L;
  public RSSException(String reason) {
    super(reason);
  }
  
  public RSSException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
