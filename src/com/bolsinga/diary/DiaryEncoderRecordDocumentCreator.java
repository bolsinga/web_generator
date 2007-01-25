package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

public abstract class DiaryEncoderRecordDocumentCreator extends DiaryRecordDocumentCreator {

  protected final com.bolsinga.web.Encode fEncoder;

  DiaryEncoderRecordDocumentCreator(final Diary diary, final String outputDir, final boolean upOneLevel, final com.bolsinga.web.Encode encoder) {
    super(diary, outputDir, upOneLevel);
    fEncoder = encoder;
  }
}
