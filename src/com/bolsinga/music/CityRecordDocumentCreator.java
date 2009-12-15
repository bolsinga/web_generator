package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class CityRecordDocumentCreator extends MusicRecordDocumentCreator {
  final String fTypeString = Util.getResourceString("city");
  
  interface CityStatsTracker {
    public void track(String name, int value);
  }
  
  class CityStatsRecordFactory extends StatsRecordFactory {
      protected Table getTable() {
        return getStats();
      }
      
      public String getDirectory() {
        return Links.CITIES_DIR;
      }

      public String getTitle() {
        Object typeArgs[] = { fTypeString };
        return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
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
  
  private void trackStats(final Collection<String> items, final CityStatsTracker tracker) {
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

  private Table getStats() {
    final Collection<String> items = fLookup.getCities();

    final ArrayList<String> names = new ArrayList<String>(items.size());
    final ArrayList<Integer> values = new ArrayList<Integer>(items.size());

    trackStats(items, new CityStatsTracker() {
        public void track(String name, int value) {
            names.add(name);
            values.add(value);
        }
    });
    
    Object typeArgs[] = { fTypeString };
    String tableTitle = MessageFormat.format(Util.getResourceString("showsby"), typeArgs);

    return StatsRecordFactory.makeTable(names, values, tableTitle, fTypeString, Util.getResourceString("citystatsummary"));
  }
}
