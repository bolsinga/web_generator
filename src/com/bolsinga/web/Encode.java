package com.bolsinga.web;

import com.bolsinga.music.data.xml.impl.*;
import com.bolsinga.diary.data.xml.impl.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public abstract class Encode {

  private static final Pattern sRootURLPattern = Pattern.compile("@@ROOT_URL@@");
  private static final Pattern sTransitionalPattern = Pattern.compile("<\\W*a[^>]*target[^=>]*=[^>]*>");
  
  private static final Pattern sHTMLTagPattern = Pattern.compile("<([a-z][a-z0-9]*)[^>]*>[^<]*</\\1>", Pattern.DOTALL);

  // NOTE: With the current set of data (2006-08-02) there is a 210:1 ratio of up to std links!

  private static Encode sEncode = null;

  public synchronized static Encode getEncode(final Music music, final Diary diary) {
    if (sEncode == null) {
      sEncode = new HashEncode(music, diary);
    }
    return sEncode;
  }
  
  public static String encodeROOT_URL(final String s) {
    return Encode.encodeROOT_URL(s, Util.getSettings().getRoot());
  }
  
  public static String encodeROOT_URL(final String s, final String replacement) {
    if (s != null) {
      return sRootURLPattern.matcher(s).replaceAll(replacement);
    }
    return null;
  }
  
  public static boolean requiresTransitional(final String s) {
    if (s != null) {
      return sTransitionalPattern.matcher(s).find();
    }
    return false;
  }

  public static String encodeUntagged(final String source, final UntaggedEncoder encoder) {
    StringBuilder sb = new StringBuilder();
    Matcher html = sHTMLTagPattern.matcher(source);
    if (html.find()) {
      int offset = 0;
      do {
        sb.append(encoder.encodeUntagged(source.substring(offset, html.start())));
        sb.append(source.substring(html.start(), html.end()));
        offset = html.end();
      } while (html.find());
      sb.append(encoder.encodeUntagged(source.substring(offset, html.regionEnd())));
    } else {
      sb.append(encoder.encodeUntagged(source));
    }
    return sb.toString();
  }

  public abstract String embedLinks(Show show, boolean upOneLevel);

  public abstract String embedLinks(Entry entry, boolean upOneLevel);
}

class EncoderData {
  
  // This becomes the regex string of "()?+." It will properly escape these characters when
  //  building each regex below.
  private static final Pattern sSpecialCharsPattern = Pattern.compile("([\\(\\)\\?\\+\\.])");

  // Don't use venues with lower case names, these are 'vague' venues.
  public static final Pattern sStartsLowerCase = Pattern.compile("^\\p{Lower}+$");
  
  private static final Pattern sStartsWord = Pattern.compile("^\\w");
  private static final Pattern sEndsWord = Pattern.compile("\\w$");
  
  // See createRegex() below.
  private static final String sLinkGroup = "$1";
  
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
    
    String t = Util.createTitle("moreinfoartist", fName);
    
    fStandardLink = Util.createInternalA(standardLinks.getLinkTo(artist), EncoderData.sLinkGroup, t).toString();
    fUpLink = Util.createInternalA(upLinks.getLinkTo(artist), EncoderData.sLinkGroup, t).toString();
  }
  
  EncoderData(final Venue venue, final Links standardLinks, final Links upLinks) {
    fName = venue.getName();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    String t = Util.createTitle("moreinfovenue", fName);
    
    fStandardLink = Util.createInternalA(standardLinks.getLinkTo(venue), EncoderData.sLinkGroup, t).toString();
    fUpLink = Util.createInternalA(upLinks.getLinkTo(venue), EncoderData.sLinkGroup, t).toString();
  }
  
  EncoderData(final Album album, final Links standardLinks, final Links upLinks) {
    fName = album.getTitle();
    fPattern = Pattern.compile(EncoderData.createRegex(fName), Pattern.CASE_INSENSITIVE);
    
    String t = Util.createTitle("moreinfoalbum", fName);
    
    fStandardLink = Util.createInternalA(standardLinks.getLinkTo(album), EncoderData.sLinkGroup, t).toString();
    fUpLink = Util.createInternalA(upLinks.getLinkTo(album), EncoderData.sLinkGroup, t).toString();
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
    if (Util.getSettings().isEmbedLinks()) {
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
      // Be sure to not encode inside of HTML tags.
      result = Encode.encodeUntagged(source, new UntaggedEncoder() {
        public String encodeUntagged(final String s) {
          return EncoderData.addLinksNoHTMLMarkup(dataPattern, s, link);
        }
      });
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
    
    // \b will not count punctuation before or after a name (such as Dinosaur Jr.)
    // \W will match the surrounding whitespace, taking it out of the replacement, which 
    //   is most likely why the extra groupings were there
    // See page 240 in O'Reilly Mastering Regular Expressions 1st edition 7th printing
    
    // Only add word separator if name starts with a word
    if (sStartsWord.matcher(name).find()) {
      sb.append("\\b");
    }
    sb.append("(");
    
    Matcher m = sSpecialCharsPattern.matcher(name);
    sb.append(m.replaceAll("\\\\$1"));
    
    sb.append(")");
    // Only add word separator if name ends with a word
    if (sEndsWord.matcher(name).find()) {
      sb.append("\\b");
    }
    
    return sb.toString();
  }
}

class HashEncode extends Encode {

  // Assume average of 25 words per entry.
  private static final int WORDS_PER_ENTRY = 25;
  // Assume average of 3 words per name
  private static final int WORDS_PER_NAME = 3;
  
  // The key is the Show or Entry. The value is a TreeSet containing the EncoderData
  //  that are applicable to the given key. Only these EncoderDatas will be used
  //  to encode the key, saving some time. This is created in the constructor,
  //  and only read from thereafter.
  private final Map<Object, Collection<EncoderData>> fEncodables;
  
  HashEncode(final Music music, final Diary diary) {
    if (music != null) {
      List<Show> shows = Util.getShowsUnmodifiable(music);
      int numShows = (shows != null) ? shows.size() : 0;
      List<Entry> entries = (diary != null) ? Util.getEntriesUnmodifiable(diary) : null;
      int numDiary = (entries != null) ? entries.size() : 0;
      int numEncoded = numShows + numDiary;
      HashMap<String, HashSet<Object>> encodedMap = new HashMap<String, HashSet<Object>>(numEncoded * WORDS_PER_ENTRY);
      
      List<Artist> artists = Util.getArtistsUnmodifiable(music);
      int numArtist = (artists != null) ? artists.size() : 0;
      List<Venue> venues = Util.getVenuesUnmodifiable(music);
      int numVenue = (venues != null) ? venues.size() : 0;
      List<Album> albums= Util.getAlbumsUnmodifiable(music);
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
    List<Show> items = Util.getShowsUnmodifiable(music);
    
    for (Show item : items) {
      if (item.getComment() != null) {
        addEncodedWords(item.getComment(), encodedMap, item, items.size());
      }
    }
  }
  
  private void getDiaryWords(final Diary diary, final HashMap<String, HashSet<Object>> encodedMap) {
    if (diary != null) {
      Collection<Entry> items = Util.getEntriesUnmodifiable(diary);

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
    List<Artist> items = Util.getArtistsUnmodifiable(music);

    for (final Artist item : items) {
      EncodeItem encodeItem = new EncodeItem() {
        private EncoderData fEncoderData = new EncoderData(item, standardLinks, upLinks);
        public EncoderData encode(Object value) {
          return fEncoderData;
        }
      };
      addWords(item.getName(), encoderMap, encodeItem, item, items.size());
    }
  }

  private void getVenueWords(final Music music, final HashMap<String, HashMap<Object, EncoderData>> encoderMap, final Links standardLinks, final Links upLinks) {
    List<Venue> items = Util.getVenuesUnmodifiable(music);

    for (final Venue item : items) {
      if (!EncoderData.sStartsLowerCase.matcher(item.getName()).matches()) {
        EncodeItem encodeItem = new EncodeItem() {
          private EncoderData fEncoderData = new EncoderData(item, standardLinks, upLinks);
          public EncoderData encode(Object value) {
            return fEncoderData;
          }
        };
        addWords(item.getName(), encoderMap, encodeItem, item, items.size());
      }
    }
  }

  private void getAlbumWords(final Music music, final HashMap<String, HashMap<Object, EncoderData>> encoderMap, final Links standardLinks, final Links upLinks) {
    List<Album> items = Util.getAlbumsUnmodifiable(music);

    // Create a HashSet of all Artist names. If an Album has the same name as an
    //  Artist, prefer the Artist name over the Album.
    List<Artist> artistList = Util.getArtistsUnmodifiable(music);
    HashSet<String> artists = new HashSet<String>(artistList.size());
    
    for (Artist artist : artistList) {
      artists.add(artist.getName());
    }

    for (final Album item : items) {
      if (!artists.contains(item.getTitle())) {
        EncodeItem encodeItem = new EncodeItem() {
          private EncoderData fEncoderData = new EncoderData(item, standardLinks, upLinks);
          public EncoderData encode(Object value) {
            return fEncoderData;
          }
        };
        addWords(item.getTitle(), encoderMap, encodeItem, item, items.size());
      }
    }
  }

  public String embedLinks(final Show show, final boolean upOneLevel) {
    return embedLinks(show, show.getComment(), upOneLevel);
  }

  public String embedLinks(final Entry entry, final boolean upOneLevel) {
    String result = embedLinks(entry, entry.getComment(), upOneLevel);
    return Encode.encodeROOT_URL(result, Links.getLinks(upOneLevel).getLevelOnly());
  }
  
  private String embedLinks(final Object obj, final String source, final boolean upOneLevel) {
    String result = null;
    if ((fEncodables != null) && (fEncodables.containsKey(obj))) {
      result = EncoderData.addLinks(source, upOneLevel, fEncodables.get(obj));
    } else {
      result = source;
    }
    return Util.toHTMLSafe(result);
  }
}
