package com.bolsinga.music;

import com.bolsinga.music.data.xml.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import javax.xml.bind.JAXBElement;

public class TracksRecordDocumentCreator extends MusicRecordDocumentCreator {

  private final java.util.Map<String, IndexPair> fIndex;
  
  private Vector<Album> fItems;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Music music, final String outputDir) {
    TracksRecordDocumentCreator creator = new TracksRecordDocumentCreator(music, outputDir);
    creator.create(backgrounder, backgroundable);
    creator.createStats(backgrounder, backgroundable);
  }
  
  private TracksRecordDocumentCreator(final Music music, final String outputDir) {
    super(music, outputDir);
    fIndex = createIndex();
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    for (final Vector<Album> group : getGroups()) {
      backgrounder.execute(backgroundable, new Runnable() {
        public void run() {
          final Album first = group.firstElement();
          final String curName = fLinks.getPageFileName(first);
          create(new RecordFactory() {
            public Vector<Record> getRecords() {
              Vector<Record> records = new Vector<Record>();
              
              for (Album item : group) {
                records.add(getAlbumRecordSection(item));
              }
              
              return records;
            }
            public String getTitle() {
              return Util.createPageTitle(curName, Util.getResourceString("tracks"));
            }
            
            public String getFilePath() {
              return fLinks.getPagePath(first);
            }

            public Navigator getNavigator() {
              return new Navigator(fLinks) {
                public Element getTrackNavigator() {
                  return Util.addCurrentIndexNavigator(fIndex, curName, super.getTrackNavigator());
                }
              };
            }
          });
        }
      });
    }
  }

  protected void createStats(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        createTracksStats();
      }
    });

    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        createAlbumsStats();
      }
    });
  }
  
  private void createTracksStats() {
    create(new StatsRecordFactory() {
      protected Table getTable() {
        return getTracksStats();
      }
      
      public String getDirectory() {
        return Links.TRACKS_DIR;
      }

      public String getTitle() {
        Object typeArgs[] = { Util.getResourceString("track") };
        return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
      }

      public Navigator getNavigator() {
        return new Navigator(fLinks) {
          public Element getTrackNavigator() {
            return getCurrentNavigator();
          }
          
          public Element getCurrentNavigator() {
            return new StringElement(Util.getResourceString("tracks"));
          }
        };
      }
    });
  }
  
  private void createAlbumsStats() {
    create(new StatsRecordFactory() {
      protected Table getTable() {
        return getAlbumsStats();
      }
      
      public String getDirectory() {
        return Links.TRACKS_DIR;
      }
      
      public String getFilename() {
        return Links.ALBUM_STATS;
      }

      public String getTitle() {
        Object typeArgs[] = { Util.getResourceString("album") };
        return MessageFormat.format(Util.getResourceString("statistics"), typeArgs);
      }

      public Navigator getNavigator() {
        return new Navigator(fLinks) {
          public Element getAlbumNavigator() {
            return getCurrentNavigator();
          }
          
          public Element getCurrentNavigator() {
            return new StringElement(Util.getResourceString("albums"));
          }
        };
      }
    });
  }

  private java.util.Map<String, IndexPair> createIndex() {
    java.util.Map<String, IndexPair> m = new TreeMap<String, IndexPair>();
    for (Album alb : Util.getAlbumsUnmodifiable(fMusic)) {
      String letter = fLinks.getPageFileName(alb);
      if (!m.containsKey(letter)) {
        m.put(letter, new IndexPair(fLinks.getLinkToPage(alb), Util.createPageTitle(letter, Util.getResourceString("albums"))));
      }
    }
    return Collections.unmodifiableMap(m);
  }

  private Collection<Vector<Album>> getGroups() {
    List<Album> albums = Util.getAlbumsCopy(fMusic);
    // Each group is per page, so they are grouped by Show who have the same starting sort letter.
    HashMap<String, Vector<Album>> result = new HashMap<String, Vector<Album>>(albums.size());
    
    Collections.sort(albums, Compare.ALBUM_COMPARATOR);
    
    for (Album album : albums) {
      String key = fLinks.getPageFileName(album);
      Vector<Album> albumList;
      if (result.containsKey(key)) {
        albumList = result.get(key);
        albumList.add(album);
      } else {
        albumList = new Vector<Album>();
        albumList.add(album);
        result.put(key, albumList);
      }
    }
    
    return Collections.unmodifiableCollection(result.values());
  }
  
  private Table getTracksStats() {
    List<Artist> artists = Util.getArtistsCopy(fMusic);
    Collections.sort(artists, Compare.ARTIST_TRACKS_COMPARATOR);

    int index = 0;
    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    
    for (Artist artist : artists) {
      String t = Util.createTitle("moreinfoartist", artist.getName());
      names[index] = Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t).toString();
      values[index] = Util.trackCount(artist);
                        
      index++;
    }

    String typeString = Util.getResourceString("artist");
    String tableTitle = Util.getResourceString("tracksby");

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, Util.getResourceString("trackstatsummary"));
  }
  
  private Table getAlbumsStats() {
    List<Artist> artists = Util.getArtistsCopy(fMusic);
    Collections.sort(artists, Compare.ARTIST_ALBUMS_COMPARATOR);

    String[] names = new String[artists.size()];
    int[] values = new int[artists.size()];
    int index = 0;
    
    for (Artist artist : artists) {
      String t = Util.createTitle("moreinfoartist", artist.getName());
      names[index] = Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t).toString();
      List<JAXBElement<Object>> albums = Util.getAlbumsUnmodifiable(artist);
      values[index] = (albums != null) ? albums.size() : 0;
                        
      index++;
    }

    String typeString = Util.getResourceString("artist");
    String tableTitle = Util.getResourceString("albumsby");

    return StatsRecordFactory.makeTable(names, values, tableTitle, typeString, Util.getResourceString("albumstatsummary"));
  }
  
  private Vector<Element> getAlbumListing(final Album album) {
    Vector<Element> e = new Vector<Element>();
    StringBuilder sb = null;
    boolean isCompilation = Util.convert(album.isCompilation());
    com.bolsinga.music.data.xml.Date albumRelease = album.getReleaseDate();

    List<JAXBElement<Object>> songs = Util.getSongsUnmodifiable(album);
    for (JAXBElement<Object> jsong : songs) {
      Song song = (Song)jsong.getValue();
      sb = new StringBuilder();
      if (isCompilation) {
        Artist artist = (Artist)song.getPerformer();
        String t = Util.createTitle("moreinfoartist", artist.getName());
        sb.append(Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t));
        sb.append(" - ");
      }
                        
      sb.append(Util.toHTMLSafe(song.getTitle()));
                        
      if (albumRelease == null) {
        com.bolsinga.music.data.xml.Date songRelease = song.getReleaseDate();
        if (songRelease != null) {
          sb.append(" (");
          sb.append(songRelease.getYear());
          sb.append(")");
        }
      }
      e.add(new StringElement(sb.toString()));
    }
    
    return e;
  }
  
  private Element getAlbumTitle(final Album album) {
    boolean isCompilation = Util.convert(album.isCompilation());
                
    StringBuilder sb = new StringBuilder();
    sb.append(Util.createNamedTarget(album.getId(), fLookup.getHTMLName(album)));
    if (!isCompilation) {
      Artist artist = (Artist)album.getPerformer();
      sb.append(" - ");
      String t = Util.createTitle("moreinfoartist", artist.getName());
      sb.append(Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), t));
    }
    com.bolsinga.music.data.xml.Date albumRelease = album.getReleaseDate();
    if (albumRelease != null) {
      sb.append(" (");
      sb.append(albumRelease.getYear());
      sb.append(")");
    }
    
    return new StringElement(sb.toString());
  }
  
  private Record getAlbumRecordSection(final Album album) {
    Vector<Record> items = new Vector<Record>(1);
    items.add(Record.createRecordListOrdered(null, getAlbumListing(album)));
    return Record.createRecordSection(getAlbumTitle(album), items);
  }
}
