package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

import com.bolsinga.web.*;

public abstract class DiaryRecordDocumentCreator extends RecordDocumentCreator {

  protected final Diary fDiary;
  protected final int fStartYear;
  
  DiaryRecordDocumentCreator(final Diary diary, final String outputDir, final boolean upOneLevel) {
    super(Links.getLinks(upOneLevel), outputDir);
    fDiary = diary;
    fStartYear = Util.getStartYear(fDiary);
  }
  
  protected String getCopyright() {
    return Util.getCopyright(fStartYear);
  }
}
