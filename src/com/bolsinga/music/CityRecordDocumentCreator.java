package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class CityRecordDocumentCreator extends MusicRecordDocumentCreator {

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final String outputDir) {
    CityRecordDocumentCreator creator = new CityRecordDocumentCreator(music, outputDir);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private CityRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
  }

  protected void createStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new StatsRecordFactory() {
          protected Table getTable() {
            return getStats();
          }
          
          public String getDirectory() {
            return com.bolsinga.web.Links.CITIES_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("city") };
            return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getCityNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("cities"));
              }
            };
          }
        });
      }
    });
  }
  
  private Table getStats() {
    Collection<String> items = fLookup.getCities();
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

    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    int index = 0;

    for (int value : keys) {
      List<String> k = new Vector<String>(cityCount.get(value));
      Collections.sort(k);

      for (String j : k) {
        names[index] = j;
        values[index] = value;
        index++;
      }
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("city");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("citystatsummary"));
  }
}