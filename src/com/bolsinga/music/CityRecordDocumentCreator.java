package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class CityRecordDocumentCreator extends MusicRecordDocumentCreator {
  final String fTypeString = Util.getResourceString("city");
  final Object fTypeArgs[] = { fTypeString };
  final String fTableTitle = MessageFormat.format(Util.getResourceString("showsby"), fTypeArgs);
  
  class CityStatsRecordFactory extends StatsRecordFactory {
      public String getDirectory() {
        return Links.CITIES_DIR;
      }

      public String getTitle() {
        return MessageFormat.format(Util.getResourceString("statistics"), fTypeArgs);
      }

      public Navigator getNavigator() {
        return new Navigator(fLinks) {
          public Element getCityNavigator() {
            return getCurrentNavigator();
          }
          
          public Element getCurrentNavigator() {
            return new StringElement(Util.getResourceString("cities"));
          }
        };
      }

      public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
        Vector<Record> records = super.getRecords();

        Script script = new Script();
        script.setType("text/javascript");
        script.removeAttribute("language");

        StringBuilder sb = new StringBuilder();
        sb.append("createStats(\"");
        sb.append(CSS.TABLE_ROW_ALT);
        sb.append("\",\"");
        sb.append(CSS.TABLE_FOOTER);
        sb.append("\",");
        
        final Collection<String> items = fLookup.getCities();
        final ArrayList<org.json.JSONObject> values = new ArrayList<org.json.JSONObject>(items.size());

        trackStats(items, new StatsRecordFactory.StatsTracker() {
            public void track(String name, int value) throws com.bolsinga.web.WebException {
                org.json.JSONObject json = new org.json.JSONObject();
                try {
                    json.put("k", name);
                    json.put("v", value);
                } catch (org.json.JSONException e) {
                    throw new com.bolsinga.web.WebException("Can't create city stats json", e);
                }
                values.add(json);
            }
        });
        org.json.JSONArray jarray = new org.json.JSONArray(values);
        try {
            if (com.bolsinga.web.Util.getPrettyOutput()) {
              sb.append(jarray.toString(2));
            } else {
              sb.append(jarray.toString());
            }
        } catch (org.json.JSONException e) {
            throw new com.bolsinga.web.WebException("Can't write city stats json array", e);
        }
        
        sb.append(");");
        script.setTagText(sb.toString());
        
        records.add(Record.createRecordSimple(script));

        return records;
      }

      protected Table getTable() {
        Table table = Util.makeTable(fTableTitle, Util.getResourceString("citystatsummary"), new TableHandler() {
          public TR getHeaderRow() {
            return new TR().addElement(new TH(fTypeString)).addElement(new TH("#")).addElement(new TH("%"));
          }

          public int getRowCount() {
            return 0;
          }
          
          public TR getRow(final int row) {
            return null;
          }
          
          public TR getFooterRow() {
            return null;
          }
        });
        table.setID("stats");
        return table;
      }
  }
  
  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final String outputDir) {
    CityRecordDocumentCreator creator = new CityRecordDocumentCreator(music, outputDir);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private CityRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
  }

  private void createStats(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new CityStatsRecordFactory());
      }
    });
  }
  
  private void trackStats(final Collection<String> items, final StatsRecordFactory.StatsTracker tracker) throws com.bolsinga.web.WebException {
    HashMap<Integer, Collection<String>> cityCount = new HashMap<Integer, Collection<String>>();
    String city = null;
    int val;
    Collection<String> stringCollection = null;

    for (String item : items) {
      val = fLookup.getShows(item).size();
      if (cityCount.containsKey(val)) {
        stringCollection = cityCount.get(val);
        stringCollection.add(item);
      } else {
        stringCollection = new HashSet<String>();
        stringCollection.add(item);
        cityCount.put(val, stringCollection);
      }
    }
                
    List<Integer> keys = new Vector<Integer>(cityCount.keySet());
    Collections.sort(keys);
    Collections.reverse(keys);

    for (int value : keys) {
      List<String> k = new Vector<String>(cityCount.get(value));
      Collections.sort(k);

      for (String j : k) {
        tracker.track(j, value);
      }
    }
  }
}
