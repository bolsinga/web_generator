package com.bolsinga.itunes;

public class ParserException extends Exception {
  static final long serialVersionUID = 382545928589503489L;
  public ParserException(String reason) {
    super(reason);
  }
  
  public ParserException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
