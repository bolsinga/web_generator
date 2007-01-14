package com.bolsinga.music;

import java.io.*;
import java.util.*;

import org.apache.ecs.html.*;

public abstract class StatsRecordFactory implements com.bolsinga.web.RecordFactory {

  public Vector<com.bolsinga.web.Record> getRecords() {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
    items.add(com.bolsinga.web.Record.createRecordSimple(getTable()));
    return items;
  }

  public String getFilePath() {
    StringBuilder sb = new StringBuilder();
    sb.append(getDirectory());
    sb.append(File.separator);
    sb.append(getFilename());
    sb.append(com.bolsinga.web.Links.HTML_EXT);
    return sb.toString();
  }
          
  public String getFilename() {
    return com.bolsinga.web.Links.STATS;
  }
  
  protected abstract String getDirectory();
  protected abstract Table getTable();

  public static Table makeTable(final String[] names, final int[] values, final String caption, final String header, final String summary) {
    int runningTotal = 0;
    int i;
    for (i = 0; i < values.length; i++) {
      runningTotal += values[i];
    }
    final int total = runningTotal;
    
    return com.bolsinga.web.Util.makeTable(caption, summary, new com.bolsinga.web.TableHandler() {
      public TR getHeaderRow() {
        return new TR().addElement(new TH(header)).addElement(new TH("#")).addElement(new TH("%"));
      }

      public int getRowCount() {
        return values.length;
      }
      
      public TR getRow(final int row) {
        TR trow = new TR();
        TH thh = new TH(names[row]);
        thh.setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput());
        trow.addElement(thh);
        trow.addElement(new TD(Integer.toString(values[row])).setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput()));
        trow.addElement(new TD(Util.toString((double)values[row] / total * 100.0)).setPrettyPrint(com.bolsinga.web.Util.getPrettyOutput()));
        return trow;
      }
      
      public TR getFooterRow() {
        TR trow = new TR();
        trow.addElement(new TH(Integer.toString(names.length)));
        trow.addElement(new TH(Integer.toString(total)));
        trow.addElement(new TH());
        return trow;
      }
    });
  }
}
