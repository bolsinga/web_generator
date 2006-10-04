package com.bolsinga.web;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

import com.bolsinga.music.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

class EncodeTest {

  private static final boolean ENCODE_TIMING = Boolean.getBoolean("site.times");
  private static final boolean TEST_DIARY = true;
  private static final boolean TEST_MUSIC = true;
  
  EncodeTest() {
  }
  
  void generate(final String diaryFile, final String musicFile, final String outputDir) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
    
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);

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

  static void generateDiary(final Diary diary, final Encode encoder, final String outputDir) {
    List<Entry> items = com.bolsinga.diary.Util.getEntriesCopy(diary);
    StringBuilder buffer = new StringBuilder();
    HashMap<String, Long> times = new HashMap<String, Long>(items.size());
    long start, current;

    Collections.sort(items, com.bolsinga.diary.Util.ENTRY_COMPARATOR);

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

  static void generateMusic(final Music music, final Encode encoder, final String outputDir) {
    List<Show> items = com.bolsinga.music.Util.getShowsUnmodifiable(music);
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

  private static void writeDocument(final StringBuilder buffer, final String outputDir, final String fileName) {
    try {
      File f = new File(outputDir, fileName);
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("EncodeTest cannot mkdirs: " + parent.getAbsolutePath());
        }
      }
      OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
      w.write(buffer.toString());
      w.close();
    } catch (IOException ioe) {
      System.err.println("Exception: " + ioe);
      ioe.printStackTrace();
      System.exit(1);
    }
  }
}

public abstract class Encode {

  // NOTE: With the current set of data (2006-08-02) there is a 210:1 ratio of up to std links!

  private static Encode sEncode = null;
    
  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir]>");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);

    EncodeTest test = new EncodeTest();
    test.generate(args[0], args[1], args[3]);
  }

  public synchronized static Encode getEncode(final Music music, final Diary diary) {
    if (sEncode == null) {
      sEncode = new HashEncode(music, diary);
    }
    return sEncode;
  }

  public abstract String embedLinks(Show show, boolean upOneLevel);

  public abstract String embedLinks(Entry entry, boolean upOneLevel);
}

class EncoderData {
  
  private static final Pattern sSpecialCharsPattern = Pattern.compile("([\\(\\)\\?\\+])");
  
  private static final Pattern sHTMLTagPattern = Pattern.compile("(.*)(<([a-z][a-z0-9]*)[^>]*>[^<]*</\\3>)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  // Don't use venues with lower case names, these are 'vague' venues.
  public static final Pattern sStartsLowerCase = Pattern.compile("\\p{Lower}.*");
  
  private final String fName;
  private final Pattern fPattern;
  private final String fStandardLink;
  private final String fUpLink;
  
  public static final Comparator<EncoderData> ENCODERDATA_COMPARATOR = new Comparator<EncoderData>() {
      public int compare(final EncoderData d1, final EncoderData d2) {
        int result = d2.getName().length() - d1.getName().length();
        if (result == 0) {
          result = d2.getName().compareTo(d1.getName());
        }
        return result;
      }
    };
  
  EncoderData(final Artist artist, final Links standardLinks, final Links upLinks) {
    fName = artist.getName();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    String t = com.bolsinga.music.Util.createTitle("moreinfoartist", fName);
    
    fStandardLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(artist), "$2", t).toString());
    fUpLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(artist), "$2", t).toString());
  }
  
  EncoderData(final Venue venue, final Links standardLinks, final Links upLinks) {
    fName = venue.getName();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    String t = com.bolsinga.music.Util.createTitle("moreinfovenue", fName);
    
    fStandardLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(venue), "$2", t).toString());
    fUpLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(venue), "$2", t).toString());
  }
  
  EncoderData(final Album album, final Links standardLinks, final Links upLinks) {
    fName = album.getTitle();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    String t = com.bolsinga.music.Util.createTitle("moreinfoalbum", fName);
    
    fStandardLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(album), "$2", t).toString());
    fUpLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(album), "$2", t).toString());
  }

  public static String addLinks(final String source, final boolean upOneLevel, final Collection<EncoderData> encodings) {
    String result = source;

    // The general idea is to replace text not within HTML tags with links to each EncoderData pattern.
    
    // It may be best to have one Matcher that goes through one pattern for eache EncoderData.
    // The EncoderData pattern will be to find the 'name' but not within HTML tags
    // After each EncoderData is complete, the Matcher will be reset to the next EncoderData pattern and the freshly encoded source.
    // This will also remove recursion from the algorithm.

    // This looks at the modified source for each EncoderData. This means it continually searches
    // the modified source for HTML tags for each EncoderData.
    if (com.bolsinga.web.Util.getSettings().isEmbedLinks()) {
      for (EncoderData data : encodings) {
        result = EncoderData.addLinks(data.getPattern(), result, data.getLink(upOneLevel));
      }
    }

    return result;
  }

  private static String addLinks(final Pattern dataPattern, final String source, final String link) {
    String result = source;

    // Find the EncoderData pattern in the source
    Matcher entryMatch = dataPattern.matcher(source);
    if (entryMatch.find()) {                        

      StringBuilder sb = new StringBuilder();
     
      // Be sure to not encode inside of HTML tags.
      Matcher html = sHTMLTagPattern.matcher(source);
      if (html.find()) {
        // Group 1 may have HTML markup
        sb.append(EncoderData.addLinks(dataPattern, html.group(1), link));
        // Group 2 is the HTML markup found
        sb.append(html.group(2));
        // Group 4 has no HTML markup
        sb.append(EncoderData.addLinksNoHTMLMarkup(dataPattern, html.group(4), link));
      } else {
        sb.append(entryMatch.replaceAll(link));
      }
                        
      result = sb.toString();
    }
                
    return result;
  }

  private static String addLinksNoHTMLMarkup(final Pattern dataPattern, final String source, final String link) {
    // Find the EncoderData pattern in the source
    Matcher entryMatch = dataPattern.matcher(source);
    return entryMatch.replaceAll(link);
  }

  String getName() {
    // This is only used for sorting.
    return fName;
  }
  
  Pattern getPattern() {
    return fPattern;
  }
  
  private String getLink(final boolean upOneLevel) {
    return (upOneLevel ? fUpLink : fStandardLink);
  }

  private static String createRegex(final String name) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("(^|\\W)(");
    
    Matcher m = sSpecialCharsPattern.matcher(name);
    sb.append(m.replaceAll("\\\\$1"));
    
    sb.append(")(\\W)");
    
    return sb.toString();
  }
  
  private static String getLink(final String link) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("$1");
    sb.append(link);
    sb.append("$3");
    
    return sb.toString();
  }
}

class HashEncode extends Encode {

  // Assume average of 25 words per entry.
  private static final int WORDS_PER_ENTRY = 25;
  // Assume average of 3 words per name
  private static final int WORDS_PER_NAME = 3;

  private static final Pattern sRootURLPattern = Pattern.compile("(@@ROOT_URL@@)");
  
  // The key is the Show or Entry. The value is a TreeSet containing the EncoderData
  //  that are applicable to the given key. Only these EncoderDatas will be used
  //  to encode the key, saving some time. This is created in the constructor,
  //  and only read from thereafter.
  private final Map<Object, Collection<EncoderData>> fEncodables;
  
  HashEncode(final Music music, final Diary diary) {
    if (music != null) {
      List<Show> shows = com.bolsinga.music.Util.getShowsUnmodifiable(music);
      int numShows = (shows != null) ? shows.size() : 0;
      List<Entry> entries = (diary != null) ? com.bolsinga.diary.Util.getEntriesUnmodifiable(diary) : null;
      int numDiary = (entries != null) ? entries.size() : 0;
      int numEncoded = numShows + numDiary;
      HashMap<String, HashSet<Object>> encodedMap = new HashMap<String, HashSet<Object>>(numEncoded * WORDS_PER_ENTRY);
      
      List<Artist> artists = com.bolsinga.music.Util.getArtistsUnmodifiable(music);
      int numArtist = (artists != null) ? artists.size() : 0;
      List<Venue> venues = com.bolsinga.music.Util.getVenuesUnmodifiable(music);
      int numVenue = (venues != null) ? venues.size() : 0;
      List<Album> albums= com.bolsinga.music.Util.getAlbumsUnmodifiable(music);
      int numAlbum = (albums != null) ? albums.size() : 0;
      int numEncoder = numArtist + numVenue + numAlbum;

      HashMap<String, HashMap<Object, EncoderData>> encoderMap = new HashMap<String, HashMap<Object, EncoderData>>(numEncoder * WORDS_PER_NAME);
      
      // The the words for each; the key is the unique word
      // For what will be encoded, the value is a HashSet of the Show and Entries that
      //  contain the key
      getEncodedWords(music, diary, encodedMap);
      // For what will be encoding, the value is a HashSet of the Artist, Venue, Album that
      //  contain the key.
      getEncoderWords(music, encoderMap);
      
      // get the intersection of the words between the encoded and the encoders.
      //  These words serve as the base line to determine what work will need to be done.
      HashSet<String> keyWordsSet = new HashSet<String>(encoderMap.keySet());
      keyWordsSet.retainAll(encodedMap.keySet());
      
      int capacity = keyWordsSet.size() / WORDS_PER_ENTRY;
      HashMap<Object, Collection<EncoderData>> encodables = new HashMap<Object, Collection<EncoderData>>(capacity);

      Collection<EncoderData> c;
      for (String keyWord : keyWordsSet) {
        for (Object encodedItem : encodedMap.get(keyWord)) {
          for (EncoderData encoderItem : encoderMap.get(keyWord).values()) {
           if (encodables.containsKey(encodedItem)) {
              c = encodables.get(encodedItem);
              c.add(encoderItem);
            } else {
              c = new TreeSet<EncoderData>(EncoderData.ENCODERDATA_COMPARATOR);
              c.add(encoderItem);
              encodables.put(encodedItem, c);
            }
          }
        }
      }
      fEncodables = Collections.unmodifiableMap(encodables);
    } else {
      fEncodables = null;
    }
  }

  private void getEncodedWords(final Music music, final Diary diary, final HashMap<String, HashSet<Object>> encodedMap) {
    getMusicWords(music, encodedMap);
    getDiaryWords(diary, encodedMap);
  }

  interface EncodeItem { 
    public EncoderData encode(Object value);
  }

  private void addWords(final String text, final HashMap<String, HashMap<Object, EncoderData>> map, final EncodeItem encoder, final Object value, final int capacity) {
    HashMap<Object, EncoderData> encodeMap = null;
    String[] words = text.split("\\W");
    for (int j = 0; j < words.length; j++) {
      String word = words[j].toLowerCase();
      if (word.length() != 0) {
        if (map.containsKey(word)) {
           encodeMap = map.get(word);
           if (!encodeMap.containsKey(value)) {
             encodeMap.put(value, encoder.encode(value));
           }
        } else {
          encodeMap = new HashMap<Object, EncoderData>(capacity);
          encodeMap.put(value, encoder.encode(value));
          map.put(word, encodeMap);
        }
      }
    }
  }

  private void addEncodedWords(final String text, final HashMap<String, HashSet<Object>> map, final Object value, final int capacity) {
    HashSet<Object> encodeSet = null;
    String[] words = text.split("\\W");
    for (int j = 0; j < words.length; j++) {
      String word = words[j].toLowerCase();
      if (word.length() != 0) {
        if (map.containsKey(word)) {
           encodeSet = map.get(word);
           if (!encodeSet.contains(value)) {
             encodeSet.add(value);
           }
        } else {
          encodeSet = new HashSet<Object>(capacity);
          encodeSet.add(value);
          map.put(word, encodeSet);
        }
      }
    }
  }

  private void getMusicWords(final Music music, final HashMap<String, HashSet<Object>> encodedMap) {
    List<Show> items = com.bolsinga.music.Util.getShowsUnmodifiable(music);
    
    for (Show item : items) {
      if (item.getComment() != null) {
        addEncodedWords(item.getComment(), encodedMap, item, items.size());
      }
    }
  }
  
  private void getDiaryWords(final Diary diary, final HashMap<String, HashSet<Object>> encodedMap) {
    if (diary != null) {
      Collection<Entry> items = com.bolsinga.diary.Util.getEntriesUnmodifiable(diary);

      for (Entry item : items) {
        addEncodedWords(item.getComment(), encodedMap, item, items.size());
      }
    }
  }

  private void getEncoderWords(final Music music, final HashMap<String, HashMap<Object, EncoderData>> encoderMap) {
    Links standardLinks = Links.getLinks(false);
    Links upLinks = Links.getLinks(true);

    getArtistWords(music, encoderMap, standardLinks, upLinks);
    getVenueWords(music, encoderMap, standardLinks, upLinks);
    getAlbumWords(music, encoderMap, standardLinks, upLinks);
  }

  private void getArtistWords(final Music music, final HashMap<String, HashMap<Object, EncoderData>> encoderMap, final Links standardLinks, final Links upLinks) {
    List<Artist> items = com.bolsinga.music.Util.getArtistsUnmodifiable(music);

    for (final Artist item : items) {
      addWords(item.getName(), encoderMap,
               new EncodeItem() {
                 public EncoderData encode(Object value) {
                   return new EncoderData(item, standardLinks, upLinks);
                 }
               },
               item, items.size());
    }
  }

  private void getVenueWords(final Music music, final HashMap<String, HashMap<Object, EncoderData>> encoderMap, final Links standardLinks, final Links upLinks) {
    List<Venue> items = com.bolsinga.music.Util.getVenuesUnmodifiable(music);

    for (final Venue item : items) {
      if (!EncoderData.sStartsLowerCase.matcher(item.getName()).matches()) {
        addWords(item.getName(), encoderMap,
                 new EncodeItem() {
                   public EncoderData encode(Object value) {
                     return new EncoderData(item, standardLinks, upLinks);
                   }
                 },
                 item, items.size());
      }
    }
  }

  private void getAlbumWords(final Music music, final HashMap<String, HashMap<Object, EncoderData>> encoderMap, final Links standardLinks, final Links upLinks) {
    List<Album> items = com.bolsinga.music.Util.getAlbumsUnmodifiable(music);

    // Create a HashSet of all Artist names. If an Album has the same name as an
    //  Artist, prefer the Artist name over the Album.
    List<Artist> artistList = com.bolsinga.music.Util.getArtistsUnmodifiable(music);
    HashSet<String> artists = new HashSet<String>(artistList.size());
    
    for (Artist artist : artistList) {
      artists.add(artist.getName());
    }

    for (final Album item : items) {
      if (!artists.contains(item.getTitle())) {
        addWords(item.getTitle(), encoderMap,
                 new EncodeItem() {
                   public EncoderData encode(Object value) {
                     return new EncoderData(item, standardLinks, upLinks);
                   }
                 },
                 item, items.size());
      }
    }
  }

  public String embedLinks(final Show show, final boolean upOneLevel) {
    return embedLinks(show, show.getComment(), upOneLevel);
  }

  public String embedLinks(final Entry entry, final boolean upOneLevel) {
    String result = embedLinks(entry, entry.getComment(), upOneLevel);
    return sRootURLPattern.matcher(result).replaceAll(com.bolsinga.web.Util.getSettings().getRoot());
  }
  
  private String embedLinks(final Object obj, final String source, final boolean upOneLevel) {
    String result = null;
    if ((fEncodables != null) && (fEncodables.containsKey(obj))) {
      result = EncoderData.addLinks(source, upOneLevel, fEncodables.get(obj));
    } else {
      result = source;
    }
    return com.bolsinga.web.Util.toHTMLSafe(result);
  }
}
