package com.bolsinga.test;

import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.data.xml.*;

import com.bolsinga.music.*;
import com.bolsinga.web.*;

public class ConvertMusicTest {
        
  public static void main(String[] args) {
    if (args.length != 6) {
      System.out.println("Usage: Music [shows] [venuemap] [bandsort] [relations] [itunes] [output]");
      System.exit(0);
    }
  
    com.bolsinga.music.data.xml.Music music = null;
    try {
      music = com.bolsinga.shows.converter.Music.createMusic(args[0], args[1], args[2], args[3], args[4]);
    } catch (com.bolsinga.shows.converter.ConvertException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
    
    System.out.println("--SORT CHECK--");
    ConvertMusicTest.displayNoSorts(music);
    
    System.out.println();
    System.out.println("--NAME CHECK--");
    ConvertMusicTest.dumpSimilarArtists(music);
  }
  
  public static void displayNoSorts(final com.bolsinga.music.data.xml.Music music) {
    List<Artist> artists = music.getArtist();
    Collections.sort(artists, Compare.ARTIST_COMPARATOR);
    for (Artist a : artists) {
      if (a.getSortname() == null) {
        String name = a.getName();
        if (name.contains(" ")) {
          System.out.println(name);
        }
      }
    }
  }
        
  private static void dumpSimilarArtists(final com.bolsinga.music.data.xml.Music music) {
    String s;
    HashSet<String> bands = new HashSet<String>();
    
    List<Artist> artists = Util.getArtistsUnmodifiable(music);
    for (Artist artist : artists) {
      s = artist.getName().toLowerCase();
      if (bands.contains(s)) {
        System.out.println(s);
      } else {
        bands.add(s);
      }
    }
  }

  private static void dump(final com.bolsinga.music.data.xml.Music music) {
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data.xml");
      Marshaller m = jc.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      m.marshal( music, System.out );
    } catch (JAXBException e) {
      System.err.println("Can't dump!");
      e.printStackTrace();
    }
  }
}
