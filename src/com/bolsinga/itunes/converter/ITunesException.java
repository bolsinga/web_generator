//
//  ITunesException.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/27/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.itunes.converter;

public class ITunesException extends Exception {
  public ITunesException(String reason) {
    super(reason);
  }
  
  public ITunesException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
