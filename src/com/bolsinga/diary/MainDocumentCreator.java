package com.bolsinga.diary;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public class MainDocumentCreator extends DiaryEncoderRecordDocumentCreator {

  private final Music fMusic;

  public static void createDocuments(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable, final Diary diary, final String outputDir, final com.bolsinga.web.Encode encoder, final Music music) {
    MainDocumentCreator creator = new MainDocumentCreator(diary, outputDir, encoder, music);
    creator.create(backgrounder, backgroundable);
  }
  
  private MainDocumentCreator(final Diary diary, final String outputDir, final com.bolsinga.web.Encode encoder, final Music music) {
    super(diary, outputDir, false, encoder);
    fMusic = music;
  }
  
  protected String getMainDivClass() {
    return com.bolsinga.web.CSS.DOC_3_COL_BODY;
  }
  
  protected void create(final com.bolsinga.web.Backgrounder backgrounder, final com.bolsinga.web.Backgroundable backgroundable) {
    backgrounder.execute(backgroundable, new Runnable() {
      public void run() {
        create(new com.bolsinga.web.RecordFactory() {
          public Vector<com.bolsinga.web.Record> getRecords() {
            Vector<com.bolsinga.web.Record> items = new Vector<com.bolsinga.web.Record>(1);
            items.add(com.bolsinga.web.Record.createRecordSimple(getMain()));
            return items;
          }
          
          public String getTitle() {
            return fDiary.getTitle();
          }
          
          public String getFilePath() {
            StringBuilder sb = new StringBuilder();
            sb.append("index");
            sb.append(com.bolsinga.web.Links.HTML_EXT);
            return sb.toString();
          }
          
          public com.bolsinga.web.Navigator getNavigator() {
            return com.bolsinga.music.Web.getMainPagePreviewNavigator(fMusic, fLinks);
          }
        });
      }
    });
  }
  
  private Element getMain() {
    ElementContainer ec = new ElementContainer();
    ec.addElement(getStaticHeader());
    ec.addElement(com.bolsinga.web.Util.convertToUnOrderedList(fDiary.getHeader()));
    ec.addElement(getDiary());
    return ec;
  }

  private static Div createStaticsOffsite(final String title, final String data) {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_OFFSITE);
    d.addElement(new H4(title));
    if (data != null) {
      d.addElement(com.bolsinga.web.Util.convertToUnOrderedList(data));
    }
    return d;
  }
  
  private Div getStaticHeader() {
    Div d = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.STATICS_HEADER);
    d.addElement(MainDocumentCreator.createStaticsOffsite(com.bolsinga.web.Util.getSettings().getLinksTitle(), fDiary.getStatic()));
    d.addElement(MainDocumentCreator.createStaticsOffsite(com.bolsinga.web.Util.getSettings().getFriendsTitle(), fDiary.getFriends()));
    return d;
  }

  private Element getDiary() {
    Div diaryDiv = com.bolsinga.web.Util.createDiv(com.bolsinga.web.CSS.DOC_SUB);

    int mainPageEntryCount = com.bolsinga.web.Util.getSettings().getDiaryCount().intValue();
    
    com.bolsinga.music.Lookup lookup = com.bolsinga.music.Lookup.getLookup(fMusic);

    List<Object> items = com.bolsinga.web.Util.getRecentItems(mainPageEntryCount, fMusic, fDiary);
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
