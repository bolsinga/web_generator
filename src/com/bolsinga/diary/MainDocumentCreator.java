package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

import com.bolsinga.web.*;

import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class MainDocumentCreator extends DiaryEncoderRecordDocumentCreator {

  private final Music fMusic;

  public static void createDocuments(final Backgrounder backgrounder, final Backgroundable backgroundable, final Diary diary, final String outputDir, final Encode encoder, final Music music) {
    MainDocumentCreator creator = new MainDocumentCreator(diary, outputDir, encoder, music);
    creator.create(backgrounder, backgroundable);
  }
  
  private MainDocumentCreator(final Diary diary, final String outputDir, final Encode encoder, final Music music) {
    super(diary, outputDir, false, encoder);
    fMusic = music;
  }
  
  protected String getMainDivClass() {
    return CSS.DOC_3_COL_BODY;
  }
  
  protected void create(final Backgrounder backgrounder, final Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new RecordFactory() {
          public Vector<Record> getRecords() {
            Vector<Record> items = new Vector<Record>(1);
            items.add(Record.createRecordSimple(getMain()));
            return items;
          }
          
          public String getTitle() {
            return fDiary.getTitle();
          }
          
          public String getFilePath() {
            StringBuilder sb = new StringBuilder();
            sb.append("index");
            sb.append(Links.HTML_EXT);
            return sb.toString();
          }
          
          public Navigator getNavigator() {
            return new Navigator(fLinks) {
              public Element getHomeNavigator() {
                return getCurrentNavigator();
              }

              public Element getArtistNavigator() {
                return fLinks.getArtistLink(createPreviewLine(Util.getArtistsUnmodifiable(fMusic).size(),
                                                                  Util.getResourceString("bands")));
              }

              public Element getTrackNavigator() {
                return fLinks.getTracksLink(createPreviewLine(Util.getSongsUnmodifiable(fMusic).size(),
                                                                  Util.getResourceString("tracks")));
              }

              public Element getAlbumNavigator() {
                return fLinks.getAlbumsLink(createPreviewLine(Util.getAlbumsUnmodifiable(fMusic).size(),
                                                                  Util.getResourceString("albums")));
              }
              
              public Element getShowNavigator() {
                return fLinks.getShowLink(createPreviewLine(Util.getShowsUnmodifiable(fMusic).size(),
                                                                Util.getResourceString("dates")));
              }
              
              public Element getVenueNavigator() {
                return fLinks.getVenueLink(createPreviewLine(Util.getVenuesUnmodifiable(fMusic).size(),
                                                                Util.getResourceString("venues")));
              }
              
              public Element getCityNavigator() {
                return fLinks.getCityLink(createPreviewLine(com.bolsinga.music.Lookup.getLookup(fMusic).getCities().size(),
                                                                Util.getResourceString("cities")));
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

  private String createPreviewLine(final int count, final String name) {
    Object[] args = { Integer.valueOf(count), name };
    return MessageFormat.format(Util.getResourceString("previewformat"), args);
  }
  
  private Element getMain() {
    ElementContainer ec = new ElementContainer();
    ec.addElement(getStaticHeader());
    ec.addElement(Util.convertToUnOrderedList(fDiary.getHeader()));
    ec.addElement(getDiary());
    return ec;
  }

  private static Div createStaticsOffsite(final String title, final String data) {
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

  private Element getDiary() {
    Div diaryDiv = Util.createDiv(CSS.DOC_SUB);

    int mainPageEntryCount = Util.getSettings().getDiaryCount().intValue();
    
    com.bolsinga.music.Lookup lookup = com.bolsinga.music.Lookup.getLookup(fMusic);

    List<Object> items = Util.getRecentItems(mainPageEntryCount, fMusic, fDiary);
    for (Object o : items) {
      if (o instanceof com.bolsinga.diary.data.Entry) {
        diaryDiv.addElement(Web.addItem(fEncoder, (com.bolsinga.diary.data.Entry)o, fLinks, false));
      } else if (o instanceof com.bolsinga.music.data.Show) {
        // This appears at the top level
        diaryDiv.addElement(com.bolsinga.music.Web.addItem(fEncoder, lookup, fLinks, (com.bolsinga.music.data.Show)o, false));
      } else {
        System.err.println("Unknown recent item." + o.toString());
        Thread.dumpStack();
        System.exit(1);
      }
    }
                
    return diaryDiv;
  }
}
