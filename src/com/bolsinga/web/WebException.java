//
//  WebException.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/27/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.web;

public class WebException extends Exception {
  public WebException(String reason) {
    super(reason);
  }
  
  public WebException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
