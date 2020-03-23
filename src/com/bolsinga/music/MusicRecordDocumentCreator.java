package com.bolsinga.music;

import com.bolsinga.music.data.*;
import com.bolsinga.web.*;

public abstract class MusicRecordDocumentCreator extends RecordDocumentCreator {

  protected final Music fMusic;
  protected final Lookup fLookup;
  
  MusicRecordDocumentCreator(final Music music, final String outputDir) {
    super(Links.getLinks(true), outputDir);
    fMusic = music;
    fLookup = Lookup.getLookup(fMusic);
  }
  
  protected String getCopyright() {
    return Util.getCopyright(Util.getSettings().getCopyrightStartYear());
  }
}
