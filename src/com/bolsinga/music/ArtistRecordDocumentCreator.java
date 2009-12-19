package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class ArtistRecordDocumentCreator extends MusicRecordDocumentCreator {
  private final String fTypeString = Util.getResourceString("artist");
  private Object fTypeArgs[] = { fTypeString };
  private final java.util.Map<String, IndexPair> fIndex;
  
  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final String outputDir) {
    ArtistRecordDocumentCreator creator = new ArtistRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private ArtistRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Artist> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Artist first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new RecordFactory() {
            public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
              Vector<Record> records = new Vector<Record>();
              
              for (Artist item : group) {
                records.add(getArtistRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return Util.createPageTitle(curName, Util.getResourceString("artists"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public Navigator getNavigator() {
              return new Navigator(fLinks) {
                public Element getArtistNavigator() {
                  return Util.addCurrentIndexNavigator(fIndex, curName, super.getArtistNavigator());
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
          public String getDirectory() {
            return Links.ARTIST_DIR;
          }

          public String getTitle() {
            return MessageFormat.format(Util.getResourceString("statistics"), fTypeArgs);
          }

          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getArtistNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("bands"));
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
            
            final List<? extends Artist> items = fMusic.getArtistsCopy();
            final ArrayList<org.json.JSONObject> values = new ArrayList<org.json.JSONObject>(items.size());

            trackStats(items, new StatsRecordFactory.StatsTracker() {
                public void track(final String name, final String link, final int value) throws com.bolsinga.web.WebException {
                    org.json.JSONObject json = new org.json.JSONObject();
                    try {
                        json.put("k", name);
                        json.put("l", link);
                        json.put("v", value);
                    } catch (org.json.JSONException e) {
                        throw new com.bolsinga.web.WebException("Can't create artist stats json", e);
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
                throw new com.bolsinga.web.WebException("Can't write artist json array", e);
            }

            sb.append(",\"");
            sb.append(Util.getResourceString("artistprefix"));
            sb.append("\"");
            
            sb.append(");");
            script.setTagText(sb.toString());
            
            records.add(Record.createRecordSimple(script));

            return records;
          }

          protected Table getTable() {
            String tableTitle = MessageFormat.format(Util.getResourceString("showsby"), fTypeArgs);
            Table table = Util.makeTable(tableTitle, Util.getResourceString("artiststatsummary"), new TableHandler() {
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
        });
      }
    });
  }

  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Artist art : fMusic.getArtists()) {
      String letter = fLinks.getPageFileName(art);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(art), Util.createPageTitle(letter, Util.getResourceString("artists"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Artist>> getGroups() {
    List<? extends Artist> artists = fMusic.getArtistsCopy();
    // Each group is per page, so they are grouped by Artist who have the same starting sort letter.
    HashMap<String, Vector<Artist>> result = new HashMap<String, Vector<Artist>>(artists.size());
    
    Collections.sort(artists, Compare.ARTIST_COMPARATOR);
    
    for (Artist artist : artists) {
      String key = fLinks.getPageFileName(artist);
      Vector<Artist> artistList;
      if (result.containsKey(key)) {
        artistList = result.get(key);
        artistList.add(artist);
      } else {
        artistList = new Vector<Artist>();
        artistList.add(artist);
        result.put(key, artistList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }

  private void trackStats(final List<? extends Artist> items, final StatsRecordFactory.StatsTracker tracker) throws com.bolsinga.web.WebException {
    Collections.sort(items, Compare.getCompare(fMusic).ARTIST_STATS_COMPARATOR);

    for (Artist item : items) {
      Collection<Show> shows = fLookup.getShows(item);
      tracker.track(fLookup.getHTMLName(item), fLinks.getLinkTo(item), (shows != null) ? shows.size() : 0);
    }
  }
  
  private Record getArtistShowRecord(final Artist artist, final Show show) {
    String dateString = Util.toString(show.getDate());
    
    return Record.createRecordList(
      Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getArtistShowListing(artist, show));
  }
  
  private Record getArtistRecordSection(final Artist artist) {
    Vector<Record> items = new Vector<Record>();

    if (artist.getAlbums().size() > 0) {
      items.add(getAlbumRelations(artist));
    }

    if (fLookup.getRelations(artist) != null) {
      items.add(getArtistRelations(artist));
    }

    Collection<Show> shows = fLookup.getShows(artist);
    if (shows != null) {
      for (Show show : shows) {
        items.add(getArtistShowRecord(artist, show));
      }
    }
    
    A title = Util.createNamedTarget(artist.getID(), fLookup.getHTMLName(artist));
    
    return Record.createRecordSection(title, items);
  }
  
  private Vector<Element> getArtistShowListing(final Artist artist, final Show show) {
    Vector<Element> e = new Vector<Element>();
    
    StringBuilder sb = new StringBuilder();
    Iterator<? extends Artist> bi = show.getArtists().iterator();
    while (bi.hasNext()) {
      Artist performer = bi.next();
      
      String htmlName = fLookup.getHTMLName(performer);
      if (artist.equals(performer)) {
        sb.append(htmlName);
      } else {
        String t = Util.createTitle("moreinfoartist", performer.getName());
        sb.append(Util.createInternalA(fLinks.getLinkTo(performer), htmlName, t));
      }
                                
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                        
    Venue venue = show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = Util.createInternalA(fLinks.getLinkTo(venue), fLookup.getHTMLName(venue), t);
    Location l = venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                        
    String comment = show.getComment();
    if (comment != null) {
      e.add(Util.createInternalA( fLinks.getLinkTo(show),
                                  Util.getResourceString("showsummary"),
                                  Util.getResourceString("showsummarytitle")));
    }
    
    return e;
  }

  private Vector<Element> getTracks(final Artist artist) {
    Vector<Element> e = new Vector<Element>();

    List<? extends Album> albums = artist.getAlbumsCopy();
    Collections.sort(albums, Compare.ALBUM_ORDER_COMPARATOR);

    for (Album album : albums) {
      StringBuilder sb = new StringBuilder();
      String t = Util.createTitle("moreinfoalbum", album.getTitle());
      sb.append(Util.createInternalA(fLinks.getLinkTo(album), fLookup.getHTMLName(album), t));
      com.bolsinga.music.data.Date albumRelease = album.getReleaseDate();
      if (albumRelease != null) {
        sb.append(" (");
        sb.append(albumRelease.getYear());
        sb.append(")");
      }
      e.add(new StringElement(sb.toString()));
    }
    
    return e;
  }
  
  private Record getAlbumRelations(final Artist artist) {
    return Record.createRecordList(
      new StringElement(Util.getResourceString("albums")), 
      getTracks(artist));
  }

  private Record getArtistRelations(final Artist artist) {
    if (Util.getSettings().isRelatedUsesPopup()) {
        Vector<String> labels = new Vector<String>();
        Vector<String> values = new Vector<String>();
        
        for (Artist art : fLookup.getRelations(artist)) {
          if (!art.equals(artist)) {
            labels.add(fLookup.getHTMLName(art));
            values.add(fLinks.getLinkTo(art));
          }
        }
        
        return Record.createRecordPopup(Util.getResourceString("seealso"), labels, values);
    } else {
        Vector<Element> e = new Vector<Element>();
        
        org.apache.ecs.Element curElement = null;
        for (Artist art : fLookup.getRelations(artist)) {
          String htmlName = fLookup.getHTMLName(art);
          if (art.equals(artist)) {
            curElement = new StringElement(htmlName);
            e.add(curElement);
          } else {
            String t = Util.createTitle("moreinfoartist", art.getName());
            e.add(Util.createInternalA(fLinks.getLinkTo(art), htmlName, t));
          }
        }

        return Record.createRecordList(
          new StringElement(Util.getResourceString("seealso")),
          e,
          curElement);
    }
  }
}
