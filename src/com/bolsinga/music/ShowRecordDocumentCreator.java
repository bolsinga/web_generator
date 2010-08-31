package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.math.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class ShowRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final Encode fEncoder;
  private final java.util.Map<String, IndexPair> fIndex;
  private final Collection<Vector<Show>> fGroups;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final Encode encoder, final String outputDir) {
    ShowRecordDocumentCreator creator = new ShowRecordDocumentCreator(music, outputDir, encoder);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
    
  private ShowRecordDocumentCreator(final Music music, final String outputDir, final Encode encoder) {
    super(music, outputDir);
    fEncoder = encoder;
    fIndex = createIndex();
    fGroups = createGroups();
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Show> group : fGroups) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Show first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new RecordFactory() {
            public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
              Vector<Record> records = new Vector<Record>();
              
              for (Vector<Show> item : getMonthlies(group)) {
                records.add(getShowMonthRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return Util.createPageTitle(curName, Util.getResourceString("dates"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public Navigator getNavigator() {
              return new Navigator(fLinks) {
                public Element getShowNavigator() {
                  return Util.addCurrentIndexNavigator(fIndex, curName, super.getShowNavigator());
                }
              };
            }
          });
        }
      });
    }
  }

  private void createStats(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new StatsRecordFactory() {
          protected Table getTable() {
              Table table = Util.makeTable(Util.getResourceString("datestats"),
                                    Util.getResourceString("datestatssummary"), 
                                    new TableHandler() {
                  public TR getHeaderRow() {
                      TR trow = new TR().addElement(new TH());
                      Calendar cal = Calendar.getInstance();
                      for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
                          cal.set(Calendar.DAY_OF_MONTH, 1);
                          cal.set(Calendar.MONTH, i);
                          trow.addElement(new TH(Util.getShortMonthName(cal)));
                      }
                      trow.addElement(new TH(Util.getResourceString("unknownmonthshort")));
                      trow.addElement(new TH(Util.getResourceString("archivestotal")));
                      return trow;
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
          
            public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
                Vector<Record> records = super.getRecords();

                Script script = new Script();
                script.setType("text/javascript");
                script.removeAttribute("language");
                
                StringBuilder sb = new StringBuilder();
                sb.append("window.addEventListener(\"load\",function(){");
                sb.append("createShowStats(\"");
                sb.append(CSS.TABLE_ROW_ALT);
                sb.append("\",\"");
                sb.append(CSS.TABLE_FOOTER);
                sb.append("\",");

                org.json.JSONObject data = new org.json.JSONObject();
                
                try {
                    // ../dates directory
                    data.put("directory", fLinks.getDirectoryPath(Links.SHOW_DIR));
                    
                    // Year prefix: "'y' Dates"
                    data.put("title", Util.getResourceString("dates"));
                    
                    // prefix: "Dates from "
                    data.put("prefix", Util.getResourceString("moreinfoshow"));
                    
                    // Month names (including unknown) in an array
                    final ArrayList<org.json.JSONObject> months = new ArrayList<org.json.JSONObject>(Calendar.DECEMBER - Calendar.JANUARY + 1 + 1);
                    for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER + 1; month++) {
                        org.json.JSONObject json = new org.json.JSONObject();
                        if (month <= Calendar.DECEMBER) {
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            cal.set(Calendar.MONTH, month);
                            String monthStr = Util.getMonth(cal);
                            json.put("m", monthStr);
                            json.put("ms", monthStr);
                        } else {
                            json.put("m", Util.getResourceString("unknownmonth"));
                            json.put("ms", Util.getResourceString("unknownmonthshort"));
                        }
                        months.add(json);
                    }
                    data.put("months", new org.json.JSONArray(months));
                    
                    // {
                    // y: year (may be "other")
                    // s: array of show counts (including unknown month)
                    // }
                    final ArrayList<org.json.JSONObject> shows = new ArrayList<org.json.JSONObject>(Calendar.DECEMBER - Calendar.JANUARY + 1 + 1);
                    int[] monthTotals = new int[Calendar.DECEMBER - Calendar.JANUARY + 1 + 1];  // including unknown as index '12'
                    
                    // fGroups has as many items as the number of years (inc unknown).
                    for (final Vector<Show> showGroup : fGroups) {
                        org.json.JSONObject json = new org.json.JSONObject();
                        
                        String year = fLinks.getPageFileName(showGroup.firstElement());
                        json.put("y", year);
                        
                        IndexPair p = fIndex.get(year);
                        
                        int[] groupMonthTotals = new int[Calendar.DECEMBER - Calendar.JANUARY + 1 + 1]; // including unknown as index '12'
                        for (Show show : showGroup) {
                            com.bolsinga.music.data.Date date = show.getDate();
                            if (!date.isUnknown()) {
                                Calendar cal = Util.toCalendarLocal(date); // don't want UTC...
                                groupMonthTotals[cal.get(Calendar.MONTH) - Calendar.JANUARY]++;
                            } else {
                                // See if the month is known.
                                int month = date.getMonth();
                                if (month != com.bolsinga.music.data.Date.UNKNOWN) {
                                    groupMonthTotals[month - 1]++;
                                } else {
                                    groupMonthTotals[12]++;
                                }
                            }
                        }
                        
                        int yearTotal = 0;
                        final org.json.JSONArray ms = new org.json.JSONArray();
                        for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER + 1; i++) {
                            int val = groupMonthTotals[i - Calendar.JANUARY];
                            monthTotals[i - Calendar.JANUARY] += val;
                            ms.put(val);
                            yearTotal += val;
                        }
                        ms.put(yearTotal);
                        json.put("s", ms);
                        
                        shows.add(json);
                    }
                    data.put("shows", new org.json.JSONArray(shows));
                    
                    // array of all totals
                    org.json.JSONArray totalArray = new org.json.JSONArray();
                    totalArray.put(Util.getResourceString("archivestotal"));
                    int grandTotal = 0;
                    for (int t : monthTotals) {
                        totalArray.put(t);
                        grandTotal += t;
                    }
                    totalArray.put(grandTotal);
                    data.put("totals", totalArray);
                } catch (org.json.JSONException e) {
                    throw new com.bolsinga.web.WebException("Can't create show stats json", e);
                }
                
                try {
                    if (com.bolsinga.web.Util.getPrettyOutput()) {
                        sb.append(data.toString(2));
                    } else {
                        sb.append(data.toString());
                    }
                } catch (org.json.JSONException e) {
                    throw new com.bolsinga.web.WebException("Can't write show stats json array", e);
                }
                
                sb.append(");");
                
                sb.append("},false);");
                script.setTagText(sb.toString());
                
                records.add(Record.createRecordSimple(script));
                
                return records;
            }
            
          public String getDirectory() {
            return Links.SHOW_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { Util.getResourceString("year") };
            return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
          }

          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getShowNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("dates"));
              }
            };
          }
        });
      }
    });
  }
  
  private Collection<Vector<Show>> getMonthlies(final Vector<Show> items) {
    TreeMap<com.bolsinga.music.data.Date, Vector<Show>> result =
      new TreeMap<com.bolsinga.music.data.Date, Vector<Show>>(Compare.DATE_MONTH_COMPARATOR);

    for (Show item : items) {
      com.bolsinga.music.data.Date key = item.getDate();
      Vector<Show> showList;
      if (result.containsKey(key)) {
        showList = result.get(key);
        showList.add(item);
      } else {
        showList = new Vector<Show>();
        showList.add(item);
        result.put(key, showList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Show s : fMusic.getShows()) {
      String letter = fLinks.getPageFileName(s);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(s), Util.createPageTitle(letter, Util.getResourceString("dates"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Show>> createGroups() {
    List<? extends Show> shows = fMusic.getShowsCopy();
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    TreeMap<String, Vector<Show>> result = new TreeMap<String, Vector<Show>>();
    
    for (Show show : shows) {
      String key = fLinks.getPageFileName(show);
      Vector<Show> showList;
      if (result.containsKey(key)) {
        showList = result.get(key);
        showList.add(show);
      } else {
        showList = new Vector<Show>();
        showList.add(show);
        result.put(key, showList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private Record getShowRecord(final Show show) {
    return ShowRecordDocumentCreator.createShowRecord(show, fLinks, fLookup, fEncoder, false, true);
  }
  
  // This is used for the main page.
  public static Record createShowRecord(final Show show, final Links links, final Lookup lookup, final Encode encoder, final boolean titleIsLink, final boolean upOneLevel) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = new StringBuilder();
    Iterator<? extends Artist> bi = show.getArtists().iterator();
    while (bi.hasNext()) {
      Artist performer = bi.next();
                        
      String t = Util.createTitle("moreinfoartist", performer.getName());
      sb.append(Util.createInternalA(links.getLinkTo(performer), lookup.getHTMLName(performer), t));
                        
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                
    Venue venue = show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = Util.createInternalA(links.getLinkTo(venue), lookup.getHTMLName(venue), t);
    Location l = venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));

    String comment = show.getComment();
    if (comment != null) {
      comment = encoder.embedLinks(show, upOneLevel);
    }
    
    String dateString = Util.toString(show.getDate());
    Element title = null;
    if (titleIsLink) {
        title = Util.createInternalA(links.getLinkTo(show), dateString, dateString);
    } else {
        title = Util.createNamedTarget(show.getID(), dateString);
    }
    return Record.createRecordListWithComment(title, e, comment);
  }

  private Record getShowMonthRecordSection(final Vector<Show> shows) {
    Vector<Record> items = new Vector<Record>();

    // Note shows here is a Collection of Shows in a single month
    String name;
    com.bolsinga.music.data.Date date = shows.firstElement().getDate();
    String value = Util.toMonth(date);
    if (!date.isUnknown() || (date.getMonth() != com.bolsinga.music.data.Date.UNKNOWN)) {
      name = value;
    } else {
      name = Util.getResourceString("unknownmonthshort");
    }
    A title = Util.createNamedTarget(name, value);
    for (Show show : shows) {
      items.add(getShowRecord(show));
    }

    return Record.createRecordSection(title, items);
  }
}
