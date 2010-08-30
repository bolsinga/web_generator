package com.bolsinga.music;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import org.apache.ecs.html.*;

public abstract class StatsRecordFactory implements RecordFactory { 
  public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
    Vector<Record> items = new Vector<Record>(1);
    items.add(Record.createRecordSimple(getTable()));
    return items;
  }

  public String getFilePath() {
    StringBuilder sb = new StringBuilder();
    sb.append(getDirectory());
    sb.append(File.separator);
    sb.append(getFilename());
    sb.append(Links.HTML_EXT);
    return sb.toString();
  }
          
  public String getFilename() {
    return Links.STATS;
  }
  
  protected abstract String getDirectory();
  protected abstract Table getTable() throws com.bolsinga.web.WebException;
}
