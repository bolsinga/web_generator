package com.bolsinga.music.util;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {

    private static DateFormat sMonthFormat      = new SimpleDateFormat("MMMM");
    public  static DateFormat sWebFormat        = new SimpleDateFormat("M/d/yyyy");
    private static DecimalFormat sPercentFormat = new DecimalFormat("##.##");
        
    public static Calendar toCalendar(com.bolsinga.music.data.Date date) {
        Calendar d = Calendar.getInstance();
        if (!date.isUnknown()) {
            d.clear();
            d.set(date.getYear().intValue(), date.getMonth().intValue() - 1, date.getDay().intValue());
        } else {
            System.err.println("Can't convert Unknown com.bolsinga.music.data.Date");
            System.exit(1);
        }
        return d;
    }

    public static String toString(com.bolsinga.music.data.Date date) {
        if (!date.isUnknown()) {
            return sWebFormat.format(toCalendar(date).getTime());
        } else {
            Object[] args = {   ((date.getMonth() != null) ? date.getMonth() : BigInteger.ZERO),
                                ((date.getDay() != null) ? date.getDay() : BigInteger.ZERO),
                                ((date.getYear() != null) ? date.getYear() : BigInteger.ZERO) };
            return MessageFormat.format(com.bolsinga.web.util.Util.getResourceString("unknowndate"), args);
        }
    }
        
    public static String toMonth(com.bolsinga.music.data.Date date) {
        if (!date.isUnknown()) {
            return sMonthFormat.format(toCalendar(date).getTime());
        } else {
            Calendar d = Calendar.getInstance();
            if (date.getMonth() != null) {
                d.set(Calendar.MONTH, date.getMonth().intValue() - 1);
                return sMonthFormat.format(d.getTime());
            } else {
                return com.bolsinga.web.util.Util.getResourceString("unknownmonth");
            }
        }
    }
        
    public static String toString(double value) {
        return sPercentFormat.format(value);
    }

    public static com.bolsinga.music.data.Music createMusic(String sourceFile) {
        com.bolsinga.music.data.Music music = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
            Unmarshaller u = jc.createUnmarshaller();
                        
            music = (com.bolsinga.music.data.Music)u.unmarshal(new java.io.FileInputStream(sourceFile));
        } catch (Exception ume) {
            System.err.println("Exception: " + ume);
            ume.printStackTrace();
            System.exit(1);
        }
        return music;
    }
        
    public static int trackCount(com.bolsinga.music.data.Artist artist) {
        int tracks = 0;
        List albums = artist.getAlbum();
        if (albums != null) {
            ListIterator i = albums.listIterator();
            while (i.hasNext()) {
                com.bolsinga.music.data.Album album = (com.bolsinga.music.data.Album)i.next();
                List songs = album.getSong();
                ListIterator si = songs.listIterator();
                while (si.hasNext()) {
                    com.bolsinga.music.data.Song song = (com.bolsinga.music.data.Song)si.next();
                    if (song.getPerformer().equals(artist)) {
                        tracks++;
                    }
                }
            }
        }
                
        return tracks;
    }
}
