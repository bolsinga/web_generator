package com.bolsinga.diary;

import com.bolsinga.diary.data.*;

public abstract class DiaryEncoderRecordDocumentCreator extends DiaryRecordDocumentCreator {

  DiaryEncoderRecordDocumentCreator(final Diary diary, final String outputDir, final boolean upOneLevel) {
    super(diary, outputDir, upOneLevel);
  }
}
