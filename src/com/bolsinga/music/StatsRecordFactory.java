package com.bolsinga.music;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import org.apache.ecs.html.*;

public abstract class StatsRecordFactory implements RecordFactory {

  public Vector<Record> getRecords() {
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
  protected abstract Table getTable();

  public static Table makeTable(final String[] names, final int[] values, final String caption, final String header, final String summary) {
    int runningTotal = 0;
    int i;
    for (i = 0; i < values.length; i++) {
      runningTotal += values[i];
    }
    final int total = runningTotal;
    
    return Util.makeTable(caption, summary, new TableHandler() {
      public TR getHeaderRow() {
        return new TR().addElement(new TH(header)).addElement(new TH("#")).addElement(new TH("%"));
      }

      public int getRowCount() {
        return values.length;
      }
      
      public TR getRow(final int row) {
        TR trow = new TR();
        TH thh = new TH(names[row]);
        thh.setPrettyPrint(Util.getPrettyOutput());
        trow.addElement(thh);
        trow.addElement(new TD(Integer.toString(values[row])).setPrettyPrint(Util.getPrettyOutput()));
        trow.addElement(new TD(Util.toString((double)values[row] / total * 100.0)).setPrettyPrint(Util.getPrettyOutput()));
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
