package com.bolsinga.music.converter;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.data.*;

public class Music {
    public static void main(String[] args) {
                if (args.length != 6) {
                        System.out.println("Usage: Music [shows] [venuemap] [bandsort] [relations] [itunes] [output]");
                        System.exit(0);
                }

                Music.convert(args[0], args[1], args[2], args[3], args[4], args[5]);
    }
        
        public static void convert(String showsFile, String venueFile, String bandFile, String relationFile, String iTunesFile, String outputFile) {
                ObjectFactory objFactory = new ObjectFactory();
                
                try {
                        com.bolsinga.music.data.Music music = com.bolsinga.shows.converter.Music.createMusic(objFactory, showsFile, venueFile, bandFile, relationFile);

                        com.bolsinga.itunes.converter.ITunes.addMusic(objFactory, music, iTunesFile);
                
                        music.setTimestamp(Calendar.getInstance());
                        
                        // Write out to the output file.
                        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
                        Marshaller m = jc.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
                        OutputStream os = null;
                        try {
                                os = new FileOutputStream(outputFile);
                        } catch (IOException ioe) {
                                System.err.println(ioe);
                                ioe.printStackTrace();
                                System.exit(1);
                        }
                        m.marshal(music, os);
                } catch (JAXBException e) {
                        System.err.println(e);
                        e.printStackTrace();
                        System.exit(1);
                }
        }
        
        private static void dumpSimilarArtists(com.bolsinga.music.data.Music music) {
                String s;
                HashSet bands = new HashSet();
                
                ListIterator li = music.getArtist().listIterator();
                while (li.hasNext()) {
                        s = ((Artist)li.next()).getName().toLowerCase();
                        if (bands.contains(s)) {
                                System.out.println(s);
                        } else {
                                bands.add(s);
                        }
                }
                
                System.exit(0);
        }
}

