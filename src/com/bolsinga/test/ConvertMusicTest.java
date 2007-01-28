package com.bolsinga.test;

import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.data.xml.*;

import com.bolsinga.shows.converter.*;
import com.bolsinga.web.*;

public class ConvertMusicTest {
        
  public static void main(String[] args) {
    if (args.length != 6) {
      System.out.println("Usage: Music [shows] [venuemap] [bandsort] [relations] [itunes] [output]");
      System.exit(0);
    }

    try {
      com.bolsinga.shows.converter.Music.convert(args[0], args[1], args[2], args[3], args[4], args[5]);
    } catch (ConvertException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
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
