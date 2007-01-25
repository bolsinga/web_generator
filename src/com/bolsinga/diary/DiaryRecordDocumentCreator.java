package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

public abstract class DiaryRecordDocumentCreator extends com.bolsinga.web.RecordDocumentCreator {

  protected final Diary fDiary;
  protected final int fStartYear;
  
  DiaryRecordDocumentCreator(final Diary diary, final String outputDir, final boolean upOneLevel) {
    super(com.bolsinga.web.Links.getLinks(upOneLevel), outputDir);
    fDiary = diary;
    fStartYear = Util.getStartYear(fDiary);
  }
  
  protected String getCopyright() {
    return com.bolsinga.web.Util.getCopyright(fStartYear);
  }
  
  protected String getMainDivClass() {
    return com.bolsinga.web.CSS.DOC_2_COL_BODY;
  }
}
