//
//  PlistException.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/27/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.plist;

public class PlistException extends Exception {
  public PlistException(String reason) {
    super(reason);
  }
  
  public PlistException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
