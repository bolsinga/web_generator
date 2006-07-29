package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import com.bolsinga.shows.converter.*;

public class Comments {
  private String fDate;
  private String fData;
        
  public Comments(String date, String data) {
    fDate = date;
    fData = data;
  }
        
  public String getDate() {
    return fDate;
  }
        
  public void setDate(String date) {
    fDate = date;
  }
        
  public String getData() {
    return fData;
  }
        
  public void setData(String data) {
    fData = data;
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
