package com.bolsinga.music;

import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class ArtistRecordDocumentCreator extends MusicRecordDocumentCreator {

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
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Artist item : group) {
                records.add(getArtistRecordSection(item));

                createRedirectDocument(new RedirectFactory() {
                  public String getInternalURL() {
                    return fLinks.getInternalLinkTo(item);
                  }
                  public String getFilePath() {
                    return fLinks.getIdentifierPath(item);
                  }
                  public String getTitle() {
                    boolean hasAlbums = item.getAlbums().size() > 0;
                    Object[] args = { item.getName() };
                    if (hasAlbums) {
                      return MessageFormat.format(Util.getResourceString("artistshowsandalbumsdetail"), args);
                    } else {
                      return MessageFormat.format(Util.getResourceString("artistshowsonlydetail"), args);
                    }
                  }
                  public String getDescription() {
                    return Util.createTitle("moreinfo", item.getName());
                   }
                });
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
          protected Table getTable() {
            return getStats();
          }

          public String getDirectory() {
            return Links.ARTIST_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { Util.getResourceString("artist") };
            return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
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

  private Table getStats() {
    final List<? extends Artist> items = fMusic.getArtistsCopy();
    Collections.sort(items, Compare.getCompare(fMusic).ARTIST_STATS_COMPARATOR);

    final ArrayList<String> names = new ArrayList<String>(items.size());
    final ArrayList<Integer> values = new ArrayList<Integer>(items.size());
    for (Artist item : items) {
      String t = Util.createTitle("moreinfoartist", item.getName());
      names.add(Util.createInternalA(fLinks.getLinkTo(item), fLookup.getHTMLName(item), t).toString());
      Collection<Show> shows = fLookup.getShows(item);
      values.add((shows != null) ? shows.size() : 0);
    }
                
    String typeString = Util.getResourceString("artist");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(Util.getResourceString("showsby"), typeArgs);
    
    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, Util.getResourceString("artiststatsummary"));
  }
  
  private com.bolsinga.web.Record getArtistShowRecord(final Artist artist, final Show show) {
    String dateString = Util.toString(show.getDate());
    
    return com.bolsinga.web.Record.createRecordList(
      Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getArtistShowListing(artist, show));
  }
  
  private com.bolsinga.web.Record getArtistRecordSection(final Artist artist) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();

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
    
    return com.bolsinga.web.Record.createRecordSection(title, items);
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
  
  private com.bolsinga.web.Record getAlbumRelations(final Artist artist) {
    return com.bolsinga.web.Record.createRecordList(
      new StringElement(Util.getResourceString("albums")), 
      getTracks(artist));
  }

  private com.bolsinga.web.Record getArtistRelations(final Artist artist) {
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

        return com.bolsinga.web.Record.createRecordList(
          new StringElement(Util.getResourceString("seealso")),
          e,
          curElement);
  }
}
