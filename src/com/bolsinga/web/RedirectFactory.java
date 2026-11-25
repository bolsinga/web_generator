package com.bolsinga.web;

public interface RedirectFactory {
  public String getInternalURL(); // This is the URL to redirect to.
  public String getFilePath(); // This is the file path that will redirect to the URL above.
  public String getTitle(); // This is the og:title for this URL.
  public String getDescription(); // This is the og:description for this URL.
  // This is the og:type for this URL: an Open Graph type, website unless the page is something more.
  public default String getType() {
    return RecordDocumentCreator.OG_TYPE_WEBSITE;
  }
}
