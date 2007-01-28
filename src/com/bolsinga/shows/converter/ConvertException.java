//
//  ConvertException.java
//  web_generator
//
//  Created by Greg Bolsinga on 1/27/07.
//  Copyright 2007 Bolsinga Software. All rights reserved.
//
package com.bolsinga.shows.converter;

public class ConvertException extends Exception {
  public ConvertException(String reason) {
    super(reason);
  }
  
  public ConvertException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
