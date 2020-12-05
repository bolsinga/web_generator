package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class MainDocumentCreator extends DiaryEncoderRecordDocumentCreator {

  private final Music fMusic;
  private final Lookup fLookup;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Diary diary, final String outputDir, final Encode encoder, final Music music) {
    MainDocumentCreator creator = new MainDocumentCreator(diary, outputDir, encoder, music);
    creator.create(backgrounder, backgroundable);
  }
  
  private MainDocumentCreator(final Diary diary, final String outputDir, final Encode encoder, final Music music) {
    super(diary, outputDir, false, encoder);
    fMusic = music;
    fLookup = Lookup.getLookup(fMusic);
  }
  
  protected String getMainDivClass() {
    return CSS.DOC_3_COL_BODY;
  }

  protected Document populate(final RecordFactory factory) {
    Document d = super.populate(factory);
    
    d.getBody().addElement(getStaticHeader());

    return d;
  }

  protected String getSitePageTitle(final String factoryTitle) {
    return factoryTitle;
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new RecordFactory() {
          public Vector<com.bolsinga.web.Record> getRecords() {
            Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
            items.add(com.bolsinga.web.Record.createRecordSimple(getMain()));
            return items;
          }
          
          public String getTitle() {
            return fDiary.getTitle();
          }
          
          public String getFilePath() {
            return MainDocumentCreator.this.getFilePath();
          }
          
          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getHomeNavigator() {
                return getCurrentNavigator();
              }

              public Element getArtistNavigator() {
                ElementContainer ec = new ElementContainer();
                ec.addElement(fLinks.getArtistLink(Util.getResourceString("bands")));

                Vector<org.apache.ecs.Element> e = new Vector<org.apache.ecs.Element>();
                
                Div d = Util.createDiv(CSS.ENTRY_INDEX_SUB_INFO);
                d.addElement(new StringElement(createPreviewLine(fMusic.getArtists().size(), Util.getResourceString("archivestotal"))));
                e.add(d);

                d = Util.createDiv(CSS.ENTRY_INDEX_SUB_INFO);
                d.addElement(new StringElement(createPreviewLine(fLookup.getLiveCount(), Util.getResourceString("live"))));
                e.add(d);

                d = Util.createDiv(CSS.ENTRY_INDEX_SUB);
                d.addElement(Util.createUnorderedList(e, null));
                
                ec.addElement(d);
                
                return ec;
              }

              public Element getTrackNavigator() {
                return fLinks.getTracksLink(createPreviewLine(fMusic.getSongs().size(),
                                                              Util.getResourceString("tracks")));
              }

              public Element getAlbumNavigator() {
                return fLinks.getAlbumsLink(createPreviewLine(fMusic.getAlbums().size(),
                                                              Util.getResourceString("albums")));
              }
              
              public Element getShowNavigator() {
                ElementContainer ec = new ElementContainer();
                ec.addElement(fLinks.getShowLink(Util.getResourceString("dates")));
                
                Vector<org.apache.ecs.Element> e = new Vector<org.apache.ecs.Element>();
                
                Div d = Util.createDiv(CSS.ENTRY_INDEX_SUB_INFO);
                d.addElement(new StringElement(createPreviewLine(fMusic.getShows().size(), Util.getResourceString("dates"))));
                e.add(d);

                d = Util.createDiv(CSS.ENTRY_INDEX_SUB_INFO);
                d.addElement(new StringElement(createPreviewLine(fLookup.getSetCount(), Util.getResourceString("sets"))));
                e.add(d);

                d = Util.createDiv(CSS.ENTRY_INDEX_SUB);
                d.addElement(Util.createUnorderedList(e, null));
                
                ec.addElement(d);
                
                return ec;
              }
              
              public Element getVenueNavigator() {
                return fLinks.getVenueLink(createPreviewLine(fMusic.getVenues().size(),
                                                            Util.getResourceString("venues")));
              }
              
              public Element getCityNavigator() {
                ElementContainer ec = new ElementContainer();
                ec.addElement(fLinks.getCityLink(Util.getResourceString("cities")));
                
                Vector<org.apache.ecs.Element> e = new Vector<org.apache.ecs.Element>();
                
                Div d = Util.createDiv(CSS.ENTRY_INDEX_SUB_INFO);
                d.addElement(new StringElement(createPreviewLine(fLookup.getCities().size(), Util.getResourceString("cities"))));
                e.add(d);

                d = Util.createDiv(CSS.ENTRY_INDEX_SUB_INFO);
                d.addElement(new StringElement(createPreviewLine(fLookup.getStateCount(), Util.getResourceString("states"))));
                e.add(d);

                d = Util.createDiv(CSS.ENTRY_INDEX_SUB);
                d.addElement(Util.createUnorderedList(e, null));
                
                ec.addElement(d);
                
                return ec;
              }
              
              public Element getCurrentNavigator() {
                return new StringElement(Util.getResourceString("home"));
              }
            };
          }
        });
      }
    });
  }
  
  private String getFilePath() {
    StringBuilder sb = new StringBuilder();
    sb.append("index");
    sb.append(Links.HTML_EXT);
    return sb.toString();
  }

  private String createPreviewLine(final int count, final String name) {
    Object[] args = { Integer.valueOf(count), name };
    return MessageFormat.format(Util.getResourceString("previewformat"), args);
  }
  
  private Element getMain() {
    ElementContainer ec = new ElementContainer();
    ec.addElement(Util.convertToUnOrderedList(fDiary.getHeader()));
    ec.addElement(getDiary());
    return ec;
  }

  private static Div createStaticsOffsite(final String title, final List<String> data) {
    Div d = Util.createDiv(CSS.STATICS_OFFSITE);
    d.addElement(new H4(title));
    if (data != null) {
      d.addElement(Util.convertToUnOrderedList(data));
    }
    return d;
  }
  
  private Div getStaticHeader() {
    Div d = Util.createDiv(CSS.STATICS_HEADER);
    d.addElement(MainDocumentCreator.createStaticsOffsite(Util.getSettings().getLinksTitle(), fDiary.getStatic()));
    d.addElement(MainDocumentCreator.createStaticsOffsite(Util.getSettings().getFriendsTitle(), fDiary.getFriends()));
    return d;
  }

  private Song getLastPlayedSong() {
    List<? extends Song> songs = fMusic.getSongsCopy();
    Collections.sort(songs, new Comparator<Song>() {
      public int compare(final Song s1, final Song s2) {
        Calendar c1 = s1.getLastPlayed();
        Calendar c2 = s2.getLastPlayed();
        if (c1 == null && c2 == null) {
          return 0;
        }
        if (c1 == null) {
          return -1;
        }
        if (c2 == null) {
          return 1;
        }
        return c1.getTime().compareTo(c2.getTime());
      }
    });
    Collections.reverse(songs);
    if (songs.size() > 0) {
      return songs.iterator().next();
    }
    return null;
  }

  private com.bolsinga.web.Record getLastPlayedRecord() {
    Song lastPlayedSong = getLastPlayedSong();
    if (lastPlayedSong != null) {
      Artist artist = lastPlayedSong.getPerformer();

      String linkToArtist = Util.createInternalA(fLinks.getLinkTo(artist), fLookup.getHTMLName(artist), Util.createTitle("moreinfoartist", artist.getName())).toString();

      Object[] args = { lastPlayedSong.getTitle(), linkToArtist, Integer.valueOf(lastPlayedSong.getPlayCount()) };

      return com.bolsinga.web.Record.createRecordPermalink(
        new StringElement(Util.getResourceString("lastplayed")),
        MessageFormat.format(Util.getResourceString("lastplayedsong"), args),
        null);
    }
    return null;
  }

  private Element getDiary() {
    Div diaryDiv = Util.createDiv(CSS.DOC_SUB);

    com.bolsinga.web.Record lastPlayedRecord = getLastPlayedRecord();
    if (lastPlayedRecord != null) {
      diaryDiv.addElement(lastPlayedRecord.getElement());
    }

    int mainPageEntryCount = Util.getSettings().getDiaryCount();
    
    List<Object> items = Util.getRecentItems(mainPageEntryCount, fMusic, fDiary);
    for (Object o : items) {
      if (o instanceof Entry) {
        // TODO: This shouldn't call getElement().
        diaryDiv.addElement(EntryRecordDocumentCreator.createEntryRecord((Entry)o, fLinks, fEncoder, false).getElement());
      } else if (o instanceof Show) {
        // This appears at the top level
        // TODO: This shouldn't call getElement().
        diaryDiv.addElement(ShowRecordDocumentCreator.createShowRecord((Show)o, fLinks, fLookup, fEncoder, true, false).getElement());
      } else {
        System.err.println("Unknown recent item: " + o.toString());
      }
    }
                
    return diaryDiv;
  }
}
