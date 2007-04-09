package com.bolsinga.test;

import com.bolsinga.music.data.xml.impl.*;
import com.bolsinga.diary.data.xml.impl.*;
import com.bolsinga.web.*;

import java.io.*;
import java.util.*;

public class EncodeTest {

  private static final boolean ENCODE_TIMING = Boolean.getBoolean("site.times");
  private static final boolean TEST_DIARY = true;
  private static final boolean TEST_MUSIC = true;
    
  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir]>");
      System.exit(0);
    }

    try {
      Util.createSettings(args[2]);

      EncodeTest test = new EncodeTest();
      test.generate(args[0], args[1], args[3]);
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  EncodeTest() {
  }
  
  void generate(final String diaryFile, final String musicFile, final String outputDir) throws WebException {
    Diary diary = Util.createDiary(diaryFile);
    Music music = Util.createMusic(musicFile);
    
    Encode encoder = Encode.getEncode(music, diary);

    long start, current;

    if (EncodeTest.TEST_DIARY) {
      start = System.currentTimeMillis();
      generateDiary(diary, encoder, outputDir);
      if (EncodeTest.ENCODE_TIMING) {
        current = System.currentTimeMillis() - start;
        System.err.println("e-Diary total: " + current);
      }
    }

    if (EncodeTest.TEST_MUSIC) {
      start = System.currentTimeMillis();
      generateMusic(music, encoder, outputDir);
      if (EncodeTest.ENCODE_TIMING) {
        current = System.currentTimeMillis() - start;
        System.err.println("sh-Music total: " + current);
      }
    }
  }

  private static void generateDiary(final Diary diary, final Encode encoder, final String outputDir) throws WebException {
    List<Entry> items = Util.getEntriesCopy(diary);
    StringBuilder buffer = new StringBuilder();
    HashMap<String, Long> times = new HashMap<String, Long>(items.size());
    long start, current;

    Collections.sort(items, Util.ENTRY_COMPARATOR);

    for (Entry item : items) {
      start = System.currentTimeMillis();
      buffer.append(encoder.embedLinks(item, true));
      if (EncodeTest.ENCODE_TIMING) {
        current = System.currentTimeMillis() - start;
        times.put(item.getId(), current);
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append(encoder.getClass().getName() + "_diary.txt");

    writeDocument(buffer, outputDir, sb.toString());

    if (EncodeTest.ENCODE_TIMING) {
      for (String key : times.keySet()) {
        System.err.println(key + ": " + times.get(key));
      }
    }
  }

  private static void generateMusic(final Music music, final Encode encoder, final String outputDir) throws WebException {
    List<Show> items = Util.getShowsUnmodifiable(music);
    StringBuilder buffer = new StringBuilder();
    HashMap<String, Long> times = new HashMap<String, Long>(items.size());
    long start, current;

    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);

    for (Show item : items) {
      if (item.getComment() != null) {
        start = System.currentTimeMillis();
        buffer.append(encoder.embedLinks(item, true));
        if (EncodeTest.ENCODE_TIMING) {
          current = System.currentTimeMillis() - start;
          times.put(item.getId(), current);
        }
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append(encoder.getClass().getName() + "_music.txt");

    writeDocument(buffer, outputDir, sb.toString());

    if (EncodeTest.ENCODE_TIMING) {
      for (String key : times.keySet()) {
        System.err.println(key + ": " + times.get(key));
      }
    }
  }

  private static void writeDocument(final StringBuilder buffer, final String outputDir, final String fileName) throws WebException {
    File f = new File(outputDir, fileName);
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.err.println("EncodeTest cannot mkdirs: " + parent.getAbsolutePath());
      }
    }

    Writer w = null;
    try {
      try {
        w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      } catch (UnsupportedEncodingException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't handle encoding UTF-8: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }      
      
      try {
        w.write(buffer.toString());
      } catch (IOException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't write file: ");
        sb.append(f);
        throw new WebException(sb.toString(), e);
      }
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Can't close file: ");
          sb.append(f);
          throw new WebException(sb.toString(), e);
        }
      }
    }
  }
}
