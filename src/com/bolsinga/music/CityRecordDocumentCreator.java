package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class CityRecordDocumentCreator extends MusicRecordDocumentCreator {
  private final String fTypeString = Util.getResourceString("city");
  private final Object fTypeArgs[] = { fTypeString };
  private int fTotal;
    
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
        final Collection<String> items = fLookup.getCities();
        create(new DynamicStatsRecordFactory() {
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

            protected String getTableTitle() {
                return MessageFormat.format(Util.getResourceString("showsby"), fTypeArgs);
            }
            
            protected String getTableSummary() {
                return Util.getResourceString("citystatsummary");
            }
            
            protected String getTableType() {
                return fTypeString;
            }
            
            protected int getStatsSize() {
                return items.size();
            }
            
            protected void generateStats(StatsRecordFactory.StatsTracker tracker) throws com.bolsinga.web.WebException {
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
                
                fTotal = 0;
                for (int value : keys) {
                    List<String> k = new Vector<String>(cityCount.get(value));
                    Collections.sort(k);
                    
                    for (String j : k) {
                        tracker.track(j, null, value);
                        fTotal += value;
                    }
                }
            }
            
            protected int getStatsTotal() {
                return fTotal;
            }
            
            protected String getStatsLinkPrefix() {
                return null;
            }
        });
      }
    });
  }
}
