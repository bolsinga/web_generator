package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class CityRecordDocumentCreator extends MusicRecordDocumentCreator {
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
        final String typeString = Util.getResourceString("city");
        final Object typeArgs[] = { typeString };
        final Collection<String> items = fLookup.getCities();
        create(new DynamicStatsRecordFactory() {
          public String getDirectory() {
            return Links.CITIES_DIR;
          }

          public String getTitle() {
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

            protected String getTableTitle() {
                return MessageFormat.format(Util.getResourceString("showsby"), typeArgs);
            }
            
            protected String getTableSummary() {
                return Util.getResourceString("citystatsummary");
            }
            
            protected String getTableType() {
                return typeString;
            }
            
            protected int getStatsSize() {
                return items.size();
            }
            
            protected int generateStats(DynamicStatsRecordFactory.StatsTracker tracker) throws com.bolsinga.web.WebException {
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
                
                int total = 0;
                for (int value : keys) {
                    List<String> k = new Vector<String>(cityCount.get(value));
                    Collections.sort(k);
                    
                    for (String j : k) {
                        tracker.track(j, null, value);
                        total += value;
                    }
                }
                return total;
            }
            
            protected String getStatsLinkPrefix() {
                return null;
            }
        });
      }
    });
  }
}
