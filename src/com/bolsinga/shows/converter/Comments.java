package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import com.bolsinga.shows.converter.*;

public class Comments {
  private final String fDate;
  private final String fData;
        
  public Comments(final String date, final String data) {
    fDate = date;
    fData = data;
  }
        
  public String getDate() {
    return fDate;
  }

  public String getData() {
    return fData;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
                
    sb.append(getClass().getName());
    sb.append(" Date: ");
    sb.append(getDate());
    sb.append(" Data: ");
    sb.append(getData());
                
    return sb.toString();
  }
}
