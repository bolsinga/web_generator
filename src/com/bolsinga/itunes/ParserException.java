package com.bolsinga.itunes;

public class ParserException extends Exception {
  public ParserException(String reason) {
    super(reason);
  }
  
  public ParserException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
