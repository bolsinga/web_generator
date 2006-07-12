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

  private static void generate(String diaryFile, String musicFile, String outputDir) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);

    com.bolsinga.web.Encode encoder = com.bolsinga.web.Encode.getEncode(music, diary);

    generateDiary(diary, encoder, outputDir);

    generateMusic(music, encoder, outputDir);
  }

  private static void generateDiary(Diary diary, Encode encoder, String outputDir) {
    List items = diary.getEntry();
    Entry item = null;
    StringBuffer buffer = new StringBuffer();

    Collections.sort(items, com.bolsinga.diary.Util.ENTRY_COMPARATOR);

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Entry)i.next();
      
      buffer.append(encoder.embedLinks(item, true));
    }

    StringBuffer sb = new StringBuffer();
    sb.append(encoder.getClass().getName() + "_diary.txt");

    writeDocument(buffer, outputDir, sb.toString());
  }

  private static void generateMusic(Music music, Encode encoder, String outputDir) {
    List items = music.getShow();
    Show item = null;
    StringBuffer buffer = new StringBuffer();

    Collections.sort(items, com.bolsinga.music.Compare.SHOW_COMPARATOR);

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Show)i.next();

      if (item.getComment() != null) {
        buffer.append(encoder.embedLinks(item, true));
      }
    }

    StringBuffer sb = new StringBuffer();
    sb.append(encoder.getClass().getName() + "_music.txt");

    writeDocument(buffer, outputDir, sb.toString());
  }

  private static void writeDocument(StringBuffer buffer, String outputDir, String fileName) {
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

  public synchronized static Encode getEncode(Music music, Diary diary) {
    if (sEncode == null) {
      //      sEncode = new RegexEncode(music);
      //      sEncode = new NullEncode();
            sEncode = new HashEncode(music, diary);
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
  
  public static final Comparator ENCODERDATA_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
        EncoderData d1 = (EncoderData)o1;
        EncoderData d2 = (EncoderData)o2;
        
        int result = d2.getName().length() - d1.getName().length();
        if (result == 0) {
          result = d2.getName().compareTo(d1.getName());
        }
        return result;
      }
    };
  
  EncoderData(Artist artist, Links standardLinks, Links upLinks) {
    fName = artist.getName();
    fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    Object[] args = { fName };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoartist"), args);
    
    fStandardLink = com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(artist), "$2", t).toString();
    fUpLink = com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(artist), "$2", t).toString();
  }
  
  EncoderData(Venue venue, Links standardLinks, Links upLinks) {
    fName = venue.getName();
    fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    Object[] args = { fName };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfovenue"), args);
    
    fStandardLink = com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(venue), "$2", t).toString();
    fUpLink = com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(venue), "$2", t).toString();
  }
  
  EncoderData(Album album, Links standardLinks, Links upLinks) {
    fName = album.getTitle();
    fPattern = Pattern.compile(createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    Object[] args = { fName };
    String t = MessageFormat.format(com.bolsinga.web.Util.getResourceString("moreinfoalbum"), args);
    
    fStandardLink = com.bolsinga.web.Util.createInternalA(standardLinks.getLinkTo(album), "$2", t).toString();
    fUpLink = com.bolsinga.web.Util.createInternalA(upLinks.getLinkTo(album), "$2", t).toString();
  }
  
  public static void addArtistData(List items, Links standardLinks, Links upLinks, Collection encodings) {
    Artist item = null;

    Iterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Artist)i.next();
                        
      encodings.add(new EncoderData(item, standardLinks, upLinks));
    }
  }

  public static void addVenueData(List items, Links standardLinks, Links upLinks, Collection encodings) {
    Venue item = null;
                
    Iterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Venue)i.next();
                        
      if (!EncoderData.sStartsLowerCase.matcher(item.getName()).matches()) {
        encodings.add(new EncoderData(item, standardLinks, upLinks));
      }
    }
  }

  public static void addAlbumData(List items, Links standardLinks, Links upLinks, Collection encodings) {
    Album item = null;
                
    Iterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Album)i.next();
                        
      encodings.add(new EncoderData(item, standardLinks, upLinks));
    }
  }

  public static String addLinks(String source, boolean upOneLevel, Collection encodings) {
    String result = source;

    if (com.bolsinga.web.Util.getSettings().isEmbedLinks()) {
      EncoderData data = null;
      
      Iterator i = encodings.iterator();
      while (i.hasNext()) {
        data = (EncoderData)i.next();
        
        result = addLinks(data, result, upOneLevel);
      }
    }

    return result;
  }
        
  private static String addLinks(EncoderData data, String source, boolean upOneLevel) {
    String result = source;
                                                
    Matcher entryMatch = data.getPattern().matcher(source);
    if (entryMatch.find()) {                        

      StringBuffer sb = new StringBuffer();
                        
      Matcher html = sHTMLTag.matcher(source);
      if (html.find()) {
        sb.append(addLinks(data, html.group(1), upOneLevel));
        sb.append(html.group(2));
        sb.append(addLinks(data, html.group(4), upOneLevel));
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
  
  String createRegex(String name) {
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
  
  Pattern getPattern() {
    return fPattern;
  }
  
  String getLink(boolean upOneLevel) {
    StringBuffer sb = new StringBuffer();
    
    sb.append("$1");
    sb.append(upOneLevel ? fUpLink : fStandardLink);
    sb.append("$3");
    
    return sb.toString();
  }
}

class RegexEncode extends Encode {

  private final TreeSet fEncodings = new TreeSet(EncoderData.ENCODERDATA_COMPARATOR);

  public String embedLinks(Show show, boolean upOneLevel) {
    return EncoderData.addLinks(show.getComment(), upOneLevel, fEncodings);
  }

  public String embedLinks(Entry entry, boolean upOneLevel) {
    return EncoderData.addLinks(entry.getComment(), upOneLevel, fEncodings);
  }

  RegexEncode(Music music) {
    Links standardLinks = Links.getLinks(false);
    Links upLinks = Links.getLinks(true);
                
    List items = music.getArtist();
    EncoderData.addArtistData(items, standardLinks, upLinks, fEncodings);
                
    items = music.getVenue();
    EncoderData.addVenueData(items, standardLinks, upLinks, fEncodings);
                
    items = music.getAlbum();
    EncoderData.addAlbumData(items, standardLinks, upLinks, fEncodings);
  }
}

class NullEncode extends Encode {

  NullEncode() {

  }

  public String embedLinks(Show show, boolean upOneLevel) {
    return show.getComment();
  }

  public String embedLinks(Entry entry, boolean upOneLevel) {
    return entry.getComment();
  }
}

class HashEncode extends Encode {
  // Assume average of 25 words per entry.
  static final int WORDS_PER_ENTRY = 25;
  // Assume average of 3 words per name
  static final int WORDS_PER_NAME = 3;

  // The key is the Show or Entry. The value is a TreeSet containing the EncoderData
  //  that are applicable to the given key. Only these EncoderDatas will be used
  //  to encode the key, saving some time.
  HashMap fEncodables;

  HashEncode(Music music, Diary diary) {
    if (music != null) {
      List items = music.getShow();
      int numShows = (items != null) ? items.size() : 0;
      items = (diary != null) ? diary.getEntry() : null;
      int numDiary = (items != null) ? items.size() : 0;
      int numEncoded = numShows + numDiary;
      HashMap encodedMap = new HashMap(numEncoded * WORDS_PER_ENTRY);
      
      items = music.getArtist();
      int numArtist = (items != null) ? items.size() : 0;
      items = music.getVenue();
      int numVenue = (items != null) ? items.size() : 0;
      items = music.getAlbum();
      int numAlbum = (items != null) ? items.size() : 0;
      int numEncoder = numArtist + numVenue + numAlbum;

      HashMap encoderMap = new HashMap(numEncoder * WORDS_PER_NAME);
      
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
      fEncodables = new HashMap(capacity);

      Collection c;
      for (String keyWord : keyWordsSet) {
        Iterator j = ((HashMap)encodedMap.get(keyWord)).values().iterator();
        while (j.hasNext()) {
          Object encodedItem = j.next();
          
          Iterator k = ((HashMap)encoderMap.get(keyWord)).values().iterator();
          while (k.hasNext()) {
            Object encoderItem = k.next();
            
            if (fEncodables.containsKey(encodedItem)) {
              c = (Collection)fEncodables.get(encodedItem);
              c.add(encoderItem);
            } else {
              c = new TreeSet(EncoderData.ENCODERDATA_COMPARATOR);
              c.add(encoderItem);
              fEncodables.put(encodedItem, c);
            }
          }
        }
      }
    }
  }

  private void getEncodedWords(Music music, Diary diary, HashMap encodedMap) {
    getMusicWords(music, encodedMap);
    getDiaryWords(diary, encodedMap);
  }

  interface EncodeItem { 
    public Object encode(Object value);
  }

  private void addWords(String text, HashMap map, EncodeItem encoder, Object value, int capacity) {
    HashMap encodeMap = null;
    String[] words = text.split("\\W");
    for (int j = 0; j < words.length; j++) {
      String word = words[j].toLowerCase();
      if (word.length() != 0) {
        if (map.containsKey(word)) {
           encodeMap = (HashMap)map.get(word);
           if (!encodeMap.containsKey(value)) {
             encodeMap.put(value, encoder.encode(value));
           }
        } else {
          encodeMap = new HashMap(capacity);
          encodeMap.put(value, encoder.encode(value));
          map.put(word, encodeMap);
        }
      }
    }
  }

  private void getMusicWords(Music music, HashMap encodedMap) {
    List items = music.getShow();
    Show item = null;

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      item = (Show)i.next();

      if (item.getComment() != null) {
        addWords(item.getComment(), encodedMap, 
                 new EncodeItem() {
                   public Object encode(Object value) {
                     return value;
                   }
                 },
                 item, items.size());
      }
    }
  }
  
  private void getDiaryWords(Diary diary, HashMap encodedMap) {
    if (diary != null) {
      List items = diary.getEntry();
      Entry item = null;
      
      ListIterator i = items.listIterator();
      while (i.hasNext()) {
        item = (Entry)i.next();
        
        addWords(item.getComment(), encodedMap,
                 new EncodeItem() {
                   public Object encode(Object value) {
                     return value;
                   }
                 },
                 item, items.size());
      }
    }
  }

  private void getEncoderWords(Music music, HashMap encoderMap) {
    Links standardLinks = Links.getLinks(false);
    Links upLinks = Links.getLinks(true);

    getArtistWords(music, encoderMap, standardLinks, upLinks);
    getVenueWords(music, encoderMap, standardLinks, upLinks);
    getAlbumWords(music, encoderMap, standardLinks, upLinks);
  }

  private void getArtistWords(Music music, HashMap encoderMap, final Links standardLinks, final Links upLinks) {
    List items = music.getArtist();

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      final Artist item = (Artist)i.next();

      addWords(item.getName(), encoderMap,
               new EncodeItem() {
                 public Object encode(Object value) {
                   return new EncoderData(item, standardLinks, upLinks);
                 }
               },
               item, items.size());
    }
  }

  private void getVenueWords(Music music, HashMap encoderMap, final Links standardLinks, final Links upLinks) {
    List items = music.getVenue();

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      final Venue item = (Venue)i.next();

      if (!EncoderData.sStartsLowerCase.matcher(item.getName()).matches()) {
        addWords(item.getName(), encoderMap,
                 new EncodeItem() {
                   public Object encode(Object value) {
                     return new EncoderData(item, standardLinks, upLinks);
                   }
                 },
                 item, items.size());
      }
    }
  }

  private void getAlbumWords(Music music, HashMap encoderMap, final Links standardLinks, final Links upLinks) {
    List items = music.getAlbum();

    // Create a HashSet of all Artist names. If an Album has the same name as an
    //  Artist, prefer the Artist name over the Album.
    List artistList = music.getArtist();
    HashSet artists = new HashSet(artistList.size());
    ListIterator ai = artistList.listIterator();
    while (ai.hasNext()) {
      Artist item = (Artist)ai.next();
      artists.add(item.getName());
    }

    ListIterator i = items.listIterator();
    while (i.hasNext()) {
      final Album item = (Album)i.next();

      if (!artists.contains(item.getTitle())) {
        addWords(item.getTitle(), encoderMap,
                 new EncodeItem() {
                   public Object encode(Object value) {
                     return new EncoderData(item, standardLinks, upLinks);
                   }
                 },
                 item, items.size());
      }
    }
  }

  public String embedLinks(Show show, boolean upOneLevel) {
    if ((fEncodables != null) && (fEncodables.containsKey(show))) {
      return EncoderData.addLinks(show.getComment(), upOneLevel, (Collection)fEncodables.get(show));
    } else {
      return show.getComment();
    }
  }

  public String embedLinks(Entry entry, boolean upOneLevel) {
    if ((fEncodables != null) && (fEncodables.containsKey(entry))) {
      return EncoderData.addLinks(entry.getComment(), upOneLevel, (Collection)fEncodables.get(entry));
    } else {
      return entry.getComment();
    }
  }
}
