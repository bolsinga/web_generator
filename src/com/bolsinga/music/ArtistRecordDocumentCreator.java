package com.bolsinga.music;

import com.bolsinga.music.data.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBElement;

public class ArtistRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, com.bolsinga.web.IndexPair> fIndex;
  
  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Music music, final String outputDir) {
    ArtistRecordDocumentCreator creator = new ArtistRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private ArtistRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    for (final Vector<Artist> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Artist first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new com.bolsinga.web.RecordFactory() {
            public Vector<com.bolsinga.web.Record> getRecords() {
              Vector<com.bolsinga.web.Record> records = new Vector<com.bolsinga.web.Record>();
              
              for (Artist item : group) {
                records.add(getArtistRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return com.bolsinga.web.Util.createPageTitle(curName, com.bolsinga.web.Util.getResourceString("artists"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public com.bolsinga.web.Navigator getNavigator() {
              return new com.bolsinga.web.Navigator(fLinks) {
                public Element getArtistNavigator() {
                  return com.bolsinga.web.Util.addCurrentIndexNavigator(fIndex, curName, super.getArtistNavigator());
                }
              };
            }
          });
        }
      });
    }
  }
  
  protected void createStats(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new StatsRecordFactory() {
          protected Table getTable() {
            return getStats();
          }

          public String getDirectory() {
            return com.bolsinga.web.Links.ARTIST_DIR;
          }

          public String getTitle() {
            Object typeArgs[] = { com.bolsinga.web.Util.getResourceString("artist") };
            return MessageFormat.format(com.bolsinga.web.Util.getResourceString("statistics"), typeArgs);
          }

          public com.bolsinga.web.Navigator getNavigator() {
            return new com.bolsinga.web.Navigator(fLinks) {
              public Element getArtistNavigator() {
                return getCurrentNavigator();
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(com.bolsinga.web.Util.getResourceString("bands"));
              }
            };
          }
        });
      }
    });
  }

  private java.util.Map<String, com.bolsinga.web.IndexPair> createIndex() {
    java.util.Map<String, com.bolsinga.web.IndexPair> m = new TreeMap<String, com.bolsinga.web.IndexPair>();
    for (Artist art : Util.getArtistsUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(art);
      if (!m.containsKey(letter)) {
        m.put(letter, new com.bolsinga.web.IndexPair(fLinks.getLinkToPage(art), com.bolsinga.web.Util.createPageTitle(letter, com.bolsinga.web.Util.getResourceString("artists"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Artist>> getGroups() {
    List<Artist> artists = Util.getArtistsCopy(fMusic);
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
    List<Artist> items = Util.getArtistsCopy(fMusic);
    Collections.sort(items, Compare.getCompare(fMusic).ARTIST_STATS_COMPARATOR);

    int index = 0;
    String[] names = new String[items.size()];
    int[] values = new int[items.size()];
    for (Artist item : items) {
      String t = Util.createTitle("moreinfoartist", item.getName());
      names[index] = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(item), fLookup.getHTMLName(item), t).toString();
      Collection<Show> shows = fLookup.getShows(item);
      values[index] = (shows != null) ? shows.size() : 0;
                        
      index++;
    }
                
    String typeString = com.bolsinga.web.Util.getResourceString("artist");
    Object typeArgs[] = { typeString };
    String tableTitle = MessageFormat.format(com.bolsinga.web.Util.getResourceString("showsby"), typeArgs);
    
    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, com.bolsinga.web.Util.getResourceString("artiststatsummary"));
  }
  
  private com.bolsinga.web.Record getArtistShowRecord(final Artist artist, final Show show) {
    String dateString = Util.toString(show.getDate());
    
    return com.bolsinga.web.Record.createRecordList(
      com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(show), dateString, dateString), 
      getArtistShowListing(artist, show));
  }
  
  private com.bolsinga.web.Record getArtistRecordSection(final Artist artist) {
    Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>();

    if (Util.getAlbumsUnmodifiable(artist).size() > 0) {
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
    
    A title = com.bolsinga.web.Util.createNamedTarget(artist.getId(), fLookup.getHTMLName(artist));
    
    return com.bolsinga.web.Record.createRecordSection(title, items);
  }
  
  private Vector<Element> getArtistShowListing(final Artist artist, final Show show) {
    Vector<Element> e = new Vector<Element>();
    
    StringBuilder sb = new StringBuilder();
    Iterator<JAXBElement<Object>> bi = show.getArtist().iterator();
    while (bi.hasNext()) {
      Artist performer = (Artist)bi.next().getValue();
      
      String htmlName = fLookup.getHTMLName(performer);
      if (artist.equals(performer)) {
        sb.append(htmlName);
      } else {
        String t = Util.createTitle("moreinfoartist", performer.getName());
        sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(performer), htmlName, t));
      }
                                
      if (bi.hasNext()) {
        sb.append(", ");
      }
    }
    e.add(new StringElement(sb.toString()));
                        
    Venue venue = (Venue)show.getVenue();
    String t = Util.createTitle("moreinfovenue", venue.getName());
    A venueA = com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(venue), fLookup.getHTMLName(venue), t);
    Location l = (Location)venue.getLocation();
    e.add(new StringElement(venueA.toString() + ", " + l.getCity() + ", " + l.getState()));
                        
    String comment = show.getComment();
    if (comment != null) {
      e.add(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(show), com.bolsinga.web.Util.getResourceString("showsummary"), com.bolsinga.web.Util.getResourceString("showsummarytitle")));
    }
    
    return e;
  }

  private Vector<Element> getTracks(final Artist artist) {
    Vector<Element> e = new Vector<Element>();

    List<JAXBElement<Object>> albums = Util.getAlbumsCopy(artist);
    Collections.sort(albums, Compare.JAXB_ALBUM_ORDER_COMPARATOR);

    for (JAXBElement<Object> jalbum : albums) {
      Album album = (Album)jalbum.getValue();
      StringBuilder sb = new StringBuilder();
      String t = Util.createTitle("moreinfoalbum", album.getTitle());
      sb.append(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(album), fLookup.getHTMLName(album), t));
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
      new StringElement(com.bolsinga.web.Util.getResourceString("albums")), 
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
        e.add(com.bolsinga.web.Util.createInternalA(fLinks.getLinkTo(art), htmlName, t));
      }
    }

    return com.bolsinga.web.Record.createRecordList(
      new StringElement(com.bolsinga.web.Util.getResourceString("seealso")),
      e,
      curElement);
  }
}
