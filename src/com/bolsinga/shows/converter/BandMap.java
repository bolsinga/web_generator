package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

public class BandMap {
  private final String fName;
  private final String fSortName;
        
  public BandMap(final String name, final String sortName) {
    fName = name;
    fSortName = sortName;
  }
        
  public String getName() {
    return fName;
  }

  public String getSortName() {
    return fSortName;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
                
    sb.append(getClass().getName());
    sb.append(" ");
    sb.append(getName());
    sb.append(" -> ");
    sb.append(getSortName());
                
    return sb.toString();
  }
}
