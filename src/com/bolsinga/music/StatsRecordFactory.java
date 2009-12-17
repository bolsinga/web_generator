package com.bolsinga.music;

import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

import org.apache.ecs.html.*;

public abstract class StatsRecordFactory implements RecordFactory {

  public interface StatsTracker {
    public void track(String name, int value) throws com.bolsinga.web.WebException;
  }
  
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

  public static Table makeTable(final List<String> names, final List<Integer> values, final String caption, final String header, final String summary) {
    int runningTotal = 0;
    int i;
    for (i = 0; i < values.size(); i++) {
      runningTotal += values.get(i);
    }
    final int total = runningTotal;
    
    return Util.makeTable(caption, summary, new TableHandler() {
      public TR getHeaderRow() {
        return new TR().addElement(new TH(header)).addElement(new TH("#")).addElement(new TH("%"));
      }

      public int getRowCount() {
        return values.size();
      }
      
      public TR getRow(final int row) {
        TR trow = new TR();
        TH thh = new TH(names.get(row));
        thh.setPrettyPrint(Util.getPrettyOutput());
        trow.addElement(thh);
        int value = values.get(row);
        trow.addElement(new TD(Integer.toString(value)).setPrettyPrint(Util.getPrettyOutput()));
        trow.addElement(new TD(Util.toString((double)value / total * 100.0)).setPrettyPrint(Util.getPrettyOutput()));
        return trow;
      }
      
      public TR getFooterRow() {
        TR trow = new TR();
        trow.addElement(new TH(Integer.toString(names.size())));
        trow.addElement(new TH(Integer.toString(total)));
        trow.addElement(new TH());
        return trow;
      }
    });
  }
}
