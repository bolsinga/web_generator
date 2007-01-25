package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

public abstract class MusicRecordDocumentCreator extends com.bolsinga.web.RecordDocumentCreator {

  protected final Music fMusic;
  protected final Lookup fLookup;
  
  MusicRecordDocumentCreator(final Music music, final String outputDir) {
    super(com.bolsinga.web.Links.getLinks(true), outputDir);
    fMusic = music;
    fLookup = Lookup.getLookup(fMusic);
  }
  
  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(com.bolsinga.web.Util.getSettings().getCopyrightStartYear().intValue());
  }
  
  protected String getMainDivClass() {
    return com.bolsinga.web.CSS.DOC_2_COL_BODY;
  }
}
