package com.bolsinga.web;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

import com.bolsinga.music.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

public abstract class Encode {

  private static Encode sEncode = null;
  
  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir]>");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    Encode.generate(args[0], args[1], args[3]);
  }

  private static void generate(final String diaryFile, final String musicFile, final String outputDir) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);

    com.bolsinga.web.Backgrounder backgrounder = com.bolsinga.web.Backgrounder.getBackgrounder();
    
    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(backgrounder, music, diary);

    generateDiary(diary, encoder, outputDir);

    generateMusic(music, encoder, outputDir);
    
    // +++gdb This isn't proper!
  }

  private static void generateDiary(final Diary diary, final Encode encoder, final String outputDir) {
    List<Entry> items = diary.getEntry();
    StringBuffer buffer = new StringBuffer();

    Collections.sort(items, com.bolsinga.diary.Util.ENTRY_COMPARATOR);

    for (Entry item : items) {
      buffer.append(encoder.embedLinks(item, true));
    }

    StringBuffer sb = new StringBuffer();
    sb.append(encoder.getClass().getName() + "_diary.txt");

    writeDocument(buffer, outputDir, sb.toString());
  }

  private static void generateMusic(final Music music, final Encode encoder, final String outputDir) {
    List<Show> items = music.getShow();
    StringBuffer buffer = new StringBuffer();

    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);

    for (Show item : items) {
      if (item.getComment() != null) {
        buffer.append(encoder.embedLinks(item, true));
      }
    }

    StringBuffer sb = new StringBuffer();
    sb.append(encoder.getClass().getName() + "_music.txt");

    writeDocument(buffer, outputDir, sb.toString());
  }

  private static void writeDocument(final StringBuffer buffer, final String outputDir, final String fileName) {
    try {
      File f = new File(outputDir, fileName);
      File parent = new File(f.getParent());
      if (!parent.exists()) {
        if (!parent.mkdirs()) {
          System.out.println("Can't: " + parent.getAbsolutePath());
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

  public synchronized static Encode getEncode(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Diary diary) {
    if (sEncode == null) {
      sEncode = new HashEncode(backgrounder, music, diary);
    }
    return sEncode;
  }

  public abstract String embedLinks(Show show, boolean upOneLevel);

  public abstract String embedLinks(Entry entry, boolean upOneLevel);
}

class EncoderData {
  
  private static final Pattern sSpecialChars = Pattern.compile("([\\(\\)\\?])");
  
  private static final Pattern sHTMLTag = Pattern.compile("(.*)(<([a-z][a-z0-9]*)[^>]*>[^<]*</\\3>)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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
    
    Object[] args = { fName };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoartist"), args);
    
    fStandardLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(artist), "$2", t).toString());
    fUpLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(artist), "$2", t).toString());
  }
  
  EncoderData(final Venue venue, final Links standardLinks, final Links upLinks) {
    fName = venue.getName();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    Object[] args = { fName };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfovenue"), args);
    
    fStandardLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(venue), "$2", t).toString());
    fUpLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(venue), "$2", t).toString());
  }
  
  EncoderData(final Album album, final Links standardLinks, final Links upLinks) {
    fName = album.getTitle();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    Object[] args = { fName };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoalbum"), args);
    
    fStandardLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(album), "$2", t).toString());
    fUpLink = EncoderData.getLink(com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(album), "$2", t).toString());
  }

  public static String addLinks(final String source, final boolean upOneLevel, final Collection<EncoderData> encodings) {
    String result = source;

    if (com.bolsinga.web.Util.getSettings().isEmbedLinks()) {
      for (EncoderData data : encodings) {
        result = EncoderData.addLinks(data, result, upOneLevel);
      }
    }

    return result;
  }
        
  private static String addLinks(final EncoderData data, final String source, final boolean upOneLevel) {
    String result = source;
                                                
    Matcher entryMatch = data.getPattern().matcher(source);
    if (entryMatch.find()) {                        

      StringBuffer sb = new StringBuffer();
                        
      Matcher html = sHTMLTag.matcher(source);
      if (html.find()) {
        sb.append(EncoderData.addLinks(data, html.group(1), upOneLevel));
        sb.append(html.group(2));
        sb.append(EncoderData.addLinks(data, html.group(4), upOneLevel));
      } else {
        do {
          entryMatch.appendReplacement(sb, data.getLink(upOneLevel));
        } while (entryMatch.find());
        entryMatch.appendTail(sb);
      }
                        
      result = sb.toString();
    }
                
    return result;
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
    StringBuffer sb = new StringBuffer();
    
    sb.append("(^|\\W)(");
    
    Matcher m = sSpecialChars.matcher(name);
    while (m.find()) {
      m.appendReplacement(sb, "\\\\$1");
    }
    m.appendTail(sb);
    
    sb.append(")(\\W)");
    
    return sb.toString();
  }
  
  private static String getLink(final String link) {
    StringBuffer sb = new StringBuffer();
    
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

  private final com.bolsinga.web.Backgrounder fBackgrounder;
  
  // The key is the Show or Entry. The value is a TreeSet containing the EncoderData
  //  that are applicable to the given key. Only these EncoderDatas will be used
  //  to encode the key, saving some time. This is created in the constructor,
  //  and only read from thereafter.
  private final Map<Object, Collection<EncoderData>> fEncodables;

  HashEncode(final com.bolsinga.web.Backgrounder backgrounder, final Music music, final Diary diary) {
    fBackgrounder = backgrounder;
    
    if (music != null) {
      List<Show> shows = music.getShow();
      int numShows = (shows != null) ? shows.size() : 0;
      List<Entry> entries = (diary != null) ? diary.getEntry() : null;
      int numDiary = (entries != null) ? entries.size() : 0;
      int numEncoded = numShows + numDiary;
      HashMap<String, HashSet<Object>> encodedMap = new HashMap<String, HashSet<Object>>(numEncoded * WORDS_PER_ENTRY);
      
      List<Artist> artists = music.getArtist();
      int numArtist = (artists != null) ? artists.size() : 0;
      List<Venue> venues = music.getVenue();
      int numVenue = (venues != null) ? venues.size() : 0;
      List<Album> albums= music.getAlbum();
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
    List<Show> items = music.getShow();
    
    for (Show item : items) {
      if (item.getComment() != null) {
        addEncodedWords(item.getComment(), encodedMap, item, items.size());
      }
    }
  }
  
  private void getDiaryWords(final Diary diary, final HashMap<String, HashSet<Object>> encodedMap) {
    if (diary != null) {
      List<Entry> items = diary.getEntry();

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
    List<Artist> items = music.getArtist();

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
    List<Venue> items = music.getVenue();

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
    List<Album> items = music.getAlbum();

    // Create a HashSet of all Artist names. If an Album has the same name as an
    //  Artist, prefer the Artist name over the Album.
    List<Artist> artistList = music.getArtist();
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
    return embedLinks(entry, entry.getComment(), upOneLevel);
  }
  
  private String embedLinks(final Object obj, final String source, final boolean upOneLevel) {
    if ((fEncodables != null) && (fEncodables.containsKey(obj))) {
      return EncoderData.addLinks(source, upOneLevel, fEncodables.get(obj));
    } else {
      return source;
    }
  }
}
