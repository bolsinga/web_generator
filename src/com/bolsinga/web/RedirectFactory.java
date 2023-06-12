package com.bolsinga.web;

public interface RedirectFactory {
  public String getInternalURL(); // This is the URL to redirect to.
  public String getFilePath(); // This is the file path that will redirect to the URL above.
}
