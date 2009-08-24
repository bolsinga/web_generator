package com.bolsinga.music.data.xml;

import java.io.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class Music implements com.bolsinga.music.data.Music {
  private final com.bolsinga.music.data.xml.impl.Music fMusic;
  private final List<Venue> fVenues;
  private final List<Artist> fArtists;
  private final List<Label> fLabels;
  private final List<Relation> fRelations;
  private final List<Song> fSongs;
  private final List<Album> fAlbums;
  private final List<Show> fShows;

  private static final Comparator<com.bolsinga.music.data.xml.impl.Artist> XML_ARTIST_COMPARATOR = new Comparator<com.bolsinga.music.data.xml.impl.Artist>() {
    public int compare(final com.bolsinga.music.data.xml.impl.Artist r1, final com.bolsinga.music.data.xml.impl.Artist r2) {
      String n1 = r1.getSortname();
      if (n1 == null) {
        n1 = r1.getName();
      }
      String n2 = r2.getSortname();
      if (n2 == null) {
        n2 = r2.getName();
      }
          
      return com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(n1, n2);
    }
  };

  private static final Comparator<com.bolsinga.music.data.xml.impl.Album> XML_ALBUM_COMPARATOR = new Comparator<com.bolsinga.music.data.xml.impl.Album>() {
    public int compare(final com.bolsinga.music.data.xml.impl.Album r1, final com.bolsinga.music.data.xml.impl.Album r2) {
      int result = com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(r1.getTitle(), r2.getTitle());
      if (result == 0) {
        result = XML_ARTIST_COMPARATOR.compare((com.bolsinga.music.data.xml.impl.Artist)r1.getPerformer(), (com.bolsinga.music.data.xml.impl.Artist)r2.getPerformer());
      }
      return result;
    }
  };

  private static final Comparator<com.bolsinga.music.data.xml.impl.Album> XML_ALBUM_ORDER_COMPARATOR = new Comparator<com.bolsinga.music.data.xml.impl.Album>() {
    public int compare(final com.bolsinga.music.data.xml.impl.Album r1, final com.bolsinga.music.data.xml.impl.Album r2) {
      // The Integer.MAX_VALUE assures that 'unknown' album dates are after the known ones.
      int date1 = (r1.getReleaseDate() != null) ? r1.getReleaseDate().getYear().intValue() : Integer.MAX_VALUE;
      int date2 = (r2.getReleaseDate() != null) ? r2.getReleaseDate().getYear().intValue() : Integer.MAX_VALUE;
      int result = date1 - date2;
      if (result == 0) {
        result = XML_ALBUM_COMPARATOR.compare(r1, r2);
      }
      return result;
    }
  };

  private static final Comparator<JAXBElement<Object>> JAXB_ALBUM_ORDER_COMPARATOR = new Comparator<JAXBElement<Object>>() {
    public int compare(final JAXBElement<Object> o1, final JAXBElement<Object> o2) {
      com.bolsinga.music.data.xml.impl.Album a1 = (com.bolsinga.music.data.xml.impl.Album)o1.getValue();
      com.bolsinga.music.data.xml.impl.Album a2 = (com.bolsinga.music.data.xml.impl.Album)o2.getValue();
      return XML_ALBUM_ORDER_COMPARATOR.compare(a1, a2);
    }
  };

  private static final Comparator<com.bolsinga.music.data.xml.impl.Song> XML_ALL_SONGS_ORDER_COMPARATOR = new Comparator<com.bolsinga.music.data.xml.impl.Song>() {
    public int compare(final com.bolsinga.music.data.xml.impl.Song s1, final com.bolsinga.music.data.xml.impl.Song s2) {
      com.bolsinga.music.data.xml.impl.Artist a1 = (com.bolsinga.music.data.xml.impl.Artist)s1.getPerformer();
      com.bolsinga.music.data.xml.impl.Artist a2 = (com.bolsinga.music.data.xml.impl.Artist)s2.getPerformer();
      int result = XML_ARTIST_COMPARATOR.compare(a1, a2);
      if (result == 0) {
        // The Integer.MAX_VALUE assures that 'unknown' dates are after the known ones.
        int date1 = (s1.getReleaseDate() != null) ? s1.getReleaseDate().getYear().intValue() : Integer.MAX_VALUE;
        int date2 = (s2.getReleaseDate() != null) ? s2.getReleaseDate().getYear().intValue() : Integer.MAX_VALUE;
        result = date1 - date2;
        if (result == 0) {
          result = com.bolsinga.music.Compare.LIBRARY_COMPARATOR.compare(s1.getTitle(), s2.getTitle());
          if (result == 0) {
            GregorianCalendar lp1 = (s1.getLastPlayed() != null) ? s1.getLastPlayed().toGregorianCalendar() : null;
            GregorianCalendar lp2 = (s2.getLastPlayed() != null) ? s2.getLastPlayed().toGregorianCalendar() : null;
            if ((lp1 != null) && (lp2 != null)) {
              result = lp1.compareTo(lp2);
            } else {
              result = (lp1 == null) ? 1 : 0;
            }
          }
        }
      }
      return result;
    }
  };
  
  public static Music create(final String sourceFile) throws com.bolsinga.web.WebException {
    com.bolsinga.music.data.xml.impl.Music music = null;
    
    InputStream is = null;
    try {
      try {
        is = new FileInputStream(sourceFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find music file: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      try {
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data.xml.impl");
        Unmarshaller u = jc.createUnmarshaller();
                          
        music = (com.bolsinga.music.data.xml.impl.Music)u.unmarshal(is);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't unmarsal music file: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close music file: ");
          sb.append(sourceFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
    
    return new Music(music);
  }
  
  private static com.bolsinga.music.data.xml.impl.Date convert(final com.bolsinga.music.data.xml.impl.ObjectFactory objFactory, final com.bolsinga.music.data.Date date) {
    com.bolsinga.music.data.xml.impl.Date xmlDate = objFactory.createDate();
    
    if (date.isUnknown()) {
      xmlDate.setUnknown(true);
    }
    
    int item;
    item = date.getMonth();
    if (item != com.bolsinga.music.data.Date.UNKNOWN) {
      xmlDate.setMonth(Integer.valueOf(item));
    }
    
    item = date.getDay();
    if (item != com.bolsinga.music.data.Date.UNKNOWN) {
      xmlDate.setDay(Integer.valueOf(item));
    }
    
    item = date.getYear();
    if (item != com.bolsinga.music.data.Date.UNKNOWN) {
      xmlDate.setYear(java.math.BigInteger.valueOf(item));
    }
    
    return xmlDate;
  }
  
  private static com.bolsinga.music.data.xml.impl.Location convert(final com.bolsinga.music.data.xml.impl.ObjectFactory objFactory, final com.bolsinga.music.data.Location location) {
    com.bolsinga.music.data.xml.impl.Location xmlLocation = objFactory.createLocation();

    xmlLocation.setState(location.getState());
    xmlLocation.setWeb(location.getWeb());
    xmlLocation.setCity(location.getCity());
    xmlLocation.setStreet(location.getStreet());
    int zip = location.getZip();
    if (zip != 0) {
      xmlLocation.setZip(java.math.BigInteger.valueOf(zip));
    }

    return xmlLocation;
  }

  public static void export(final com.bolsinga.music.data.Music music, final String outputFile) throws com.bolsinga.web.WebException {
    com.bolsinga.music.data.xml.impl.Music xmlMusic = null;
    if (music instanceof com.bolsinga.music.data.xml.impl.Music) {
      xmlMusic = (com.bolsinga.music.data.xml.impl.Music)xmlMusic;
    } else {
      com.bolsinga.music.data.xml.impl.ObjectFactory objFactory = new com.bolsinga.music.data.xml.impl.ObjectFactory();
      
      xmlMusic = objFactory.createMusic();
      xmlMusic.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(music.getTimestamp()));

      // Venue ID -> XML Venue
      HashMap<String, com.bolsinga.music.data.xml.impl.Venue> venueMap = new HashMap<String, com.bolsinga.music.data.xml.impl.Venue>();
      for (com.bolsinga.music.data.Venue item : music.getVenues()) {
        com.bolsinga.music.data.xml.impl.Venue xmlVenue = objFactory.createVenue();
        
        xmlVenue.setName(item.getName());
        xmlVenue.setLocation(Music.convert(objFactory, item.getLocation()));
        xmlVenue.setId(item.getID());
        
        venueMap.put(xmlVenue.getId(), xmlVenue);
        
        xmlMusic.getVenue().add(xmlVenue);
      }
      
      // Artist ID -> XML Artist
      HashMap<String, com.bolsinga.music.data.xml.impl.Artist> artistMap = new HashMap<String, com.bolsinga.music.data.xml.impl.Artist>();
      for (com.bolsinga.music.data.Artist item : music.getArtists()) {
        com.bolsinga.music.data.xml.impl.Artist xmlArtist = objFactory.createArtist();
        
        xmlArtist.setName(item.getName());
        String s = item.getSortname();
        if (s != null) {
          xmlArtist.setSortname(s);
        }
        xmlArtist.setId(item.getID());
        com.bolsinga.music.data.Location location = item.getLocation();
        if (location != null) {
          xmlArtist.setLocation(Music.convert(objFactory, location));
        }
        s = item.getComment();
        if (s != null) {
          xmlArtist.setComment(s);
        }
        
        artistMap.put(xmlArtist.getId(), xmlArtist);
        
        xmlMusic.getArtist().add(xmlArtist);
      }
      
      for (com.bolsinga.music.data.Label item : music.getLabels()) {
        assert false : "Labels not supported";
      }
      
      for (com.bolsinga.music.data.Relation item : music.getRelations()) {
        com.bolsinga.music.data.xml.impl.Relation xmlRelation = objFactory.createRelation();
        
        xmlRelation.setId(item.getID());
        String reason = item.getReason();
        if (reason != null) {
          xmlRelation.setReason(reason);
        }
        
        boolean typeSet = false;
        for (Object member : item.getMembers()) {
          if (member instanceof com.bolsinga.music.data.Artist) {
            com.bolsinga.music.data.Artist artist = (com.bolsinga.music.data.Artist)member;
            if (!typeSet) {
              xmlRelation.setType(com.bolsinga.music.data.xml.impl.RelationType.fromValue("artist"));
              typeSet = true;
            }
            xmlRelation.getMember().add(objFactory.createRelationMember(artistMap.get(artist.getID())));
          } else if (member instanceof com.bolsinga.music.data.Venue) {
            com.bolsinga.music.data.Venue venue = (com.bolsinga.music.data.Venue)member;
            if (!typeSet) {
              xmlRelation.setType(com.bolsinga.music.data.xml.impl.RelationType.fromValue("venue"));
              typeSet = true;
            }
            xmlRelation.getMember().add(objFactory.createRelationMember(venueMap.get(venue.getID())));
          } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown relation: ");
            sb.append(member);
            throw new com.bolsinga.web.WebException(sb.toString());
          }
        }
        
        xmlMusic.getRelation().add(xmlRelation);
      }
      
      // Song ID -> XML Song
      HashMap<String, com.bolsinga.music.data.xml.impl.Song> songMap = new HashMap<String, com.bolsinga.music.data.xml.impl.Song>();
      for (com.bolsinga.music.data.Song item : music.getSongs()) {
        com.bolsinga.music.data.xml.impl.Song xmlSong = objFactory.createSong();
        
        xmlSong.setId(item.getID());
        xmlSong.setTitle(item.getTitle());
        xmlSong.setPerformer(artistMap.get(item.getPerformer().getID()));
        xmlSong.setPlayCount(java.math.BigInteger.valueOf(item.getPlayCount()));
        xmlSong.setGenre(item.getGenre());
        GregorianCalendar lastPlayed = item.getLastPlayed();
        if (lastPlayed != null) {
          xmlSong.setLastPlayed(com.bolsinga.web.Util.toXMLGregorianCalendar(lastPlayed));
        }
        
        com.bolsinga.music.data.Date releaseDate = item.getReleaseDate();
        if (releaseDate != null) {
          xmlSong.setReleaseDate(Music.convert(objFactory, releaseDate));
        }
        
        int track = item.getTrack();
        if (track != 0) {
          xmlSong.setTrack(java.math.BigInteger.valueOf(track));
        }
        
        boolean isDigitized = item.isDigitized();
        if (isDigitized) {
          xmlSong.setDigitized(isDigitized);
        }

        songMap.put(xmlSong.getId(), xmlSong);
        
        xmlMusic.getSong().add(xmlSong);
      }
      
      // Album ID -> Set Artist IDs
      HashMap<String, HashSet<String>> albumArtistMap = new HashMap<String, HashSet<String>>();
      for (com.bolsinga.music.data.Album item : music.getAlbums()) {
        com.bolsinga.music.data.xml.impl.Album xmlAlbum = objFactory.createAlbum();
        
        xmlAlbum.setId(item.getID());
        xmlAlbum.setTitle(item.getTitle());
        
        com.bolsinga.music.data.Artist artist = item.getPerformer();
        if (artist != null) {
          com.bolsinga.music.data.xml.impl.Artist xmlArtist = artistMap.get(artist.getID());
          xmlAlbum.setPerformer(xmlArtist);
          
          HashSet<String> albumArtists = albumArtistMap.get(xmlAlbum.getId());
          if (albumArtists == null) {
            albumArtists = new HashSet<String>();
            albumArtistMap.put(xmlAlbum.getId(), albumArtists);
          }
          if (!albumArtists.contains(xmlArtist.getId())) {
            JAXBElement<Object> jalbum = objFactory.createArtistAlbum(xmlAlbum);
            xmlArtist.getAlbum().add(jalbum); // Modification required.
            
            albumArtists.add(xmlArtist.getId());
          }
        } else {
          xmlAlbum.setCompilation(true);
        }
        
        for (String format : item.getFormats()) {
          xmlAlbum.getFormat().add(format);
        }
        
        for (com.bolsinga.music.data.Song song : item.getSongs()) {
          com.bolsinga.music.data.xml.impl.Song xmlSong = songMap.get(song.getID());
          
          xmlAlbum.getSong().add(objFactory.createAlbumSong(xmlSong));
          
          if (artist == null) {
            HashSet<String> albumArtists = albumArtistMap.get(xmlAlbum.getId());
            if (albumArtists == null) {
              albumArtists = new HashSet<String>();
              albumArtistMap.put(xmlAlbum.getId(), albumArtists);
            }
            com.bolsinga.music.data.xml.impl.Artist xmlArtist = artistMap.get(song.getPerformer().getID());
            if (!albumArtists.contains(xmlArtist.getId())) {
              JAXBElement<Object> jalbum = objFactory.createArtistAlbum(xmlAlbum);
              xmlArtist.getAlbum().add(jalbum); // Modification required.
              
              albumArtists.add(xmlArtist.getId());
            }
          }
        }
        
        com.bolsinga.music.data.Date releaseDate = item.getReleaseDate();
        if (releaseDate != null) {
          xmlAlbum.setReleaseDate(Music.convert(objFactory, releaseDate));
        }
        
        xmlMusic.getAlbum().add(xmlAlbum);
      }
      
      for (com.bolsinga.music.data.Show item : music.getShows()) {
        com.bolsinga.music.data.xml.impl.Show xmlShow = objFactory.createShow();

        xmlShow.setDate(Music.convert(objFactory, item.getDate()));

        for (com.bolsinga.music.data.Artist artist : item.getArtists()) {
          xmlShow.getArtist().add(objFactory.createShowArtist(artistMap.get(artist.getID())));
        }
        xmlShow.setVenue(venueMap.get(item.getVenue().getID()));

        String s = item.getComment();
        if (s != null) {
          xmlShow.setComment(s);
        }
        xmlShow.setId(item.getID());
        
        xmlMusic.getShow().add(xmlShow);
      }
    }

    for (com.bolsinga.music.data.xml.impl.Artist a : xmlMusic.getArtist()) {
      List<JAXBElement<Object>> albums = a.getAlbum(); // Modification required.
      Collections.sort(albums, JAXB_ALBUM_ORDER_COMPARATOR);
    }

    Collections.sort(xmlMusic.getAlbum(), XML_ALBUM_ORDER_COMPARATOR);
    
    Collections.sort(xmlMusic.getSong(), XML_ALL_SONGS_ORDER_COMPARATOR);
    
    Music.export(xmlMusic, outputFile);
  }

  private static void export(final com.bolsinga.music.data.xml.impl.Music music, final String outputFile) throws com.bolsinga.web.WebException {
    OutputStream os = null;
    try {
      try {
        os = new FileOutputStream(outputFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }

      try {
        // Write out to the output file.
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data.xml.impl");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(music, os);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't marshall: ");
        sb.append(os.toString());
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(outputFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
  }
  
  private Music(final com.bolsinga.music.data.xml.impl.Music music) {
    fMusic = music;
    
    fVenues = new ArrayList<Venue>(fMusic.getVenue().size());
    for (com.bolsinga.music.data.xml.impl.Venue venue : fMusic.getVenue()) {
      fVenues.add(Venue.get(venue));
    }

    fArtists = new ArrayList<Artist>(fMusic.getArtist().size());
    for (com.bolsinga.music.data.xml.impl.Artist artist : fMusic.getArtist()) {
      fArtists.add(Artist.get(artist));
    }

    fLabels = new ArrayList<Label>(fMusic.getLabel().size());
    for (com.bolsinga.music.data.xml.impl.Label label : fMusic.getLabel()) {
      fLabels.add(Label.get(label));
    }

    fRelations = new ArrayList<Relation>(fMusic.getRelation().size());
    for (com.bolsinga.music.data.xml.impl.Relation relation : fMusic.getRelation()) {
      fRelations.add(Relation.get(relation));
    }

    fSongs = new ArrayList<Song>(fMusic.getSong().size());
    for (com.bolsinga.music.data.xml.impl.Song song : fMusic.getSong()) {
      fSongs.add(Song.get(song));
    }

    fAlbums = new ArrayList<Album>(fMusic.getAlbum().size());
    for (com.bolsinga.music.data.xml.impl.Album album : fMusic.getAlbum()) {
      fAlbums.add(Album.get(album));
    }

    fShows = new ArrayList<Show>(fMusic.getShow().size());
    for (com.bolsinga.music.data.xml.impl.Show show : fMusic.getShow()) {
      fShows.add(Show.get(show));
    }
  }
  
  public GregorianCalendar getTimestamp() {
    return fMusic.getTimestamp().toGregorianCalendar();
  }

  public void setTimestamp(final GregorianCalendar timestamp) {
    fMusic.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(timestamp));
  }
  
  public List<Venue> getVenues() {
    return Collections.unmodifiableList(fVenues);
  }
  
  public List<Venue> getVenuesCopy() {
    return new ArrayList<Venue>(fVenues);
  }
  
  public List<Artist> getArtists() {
    return Collections.unmodifiableList(fArtists);
  }
  
  public List<Artist> getArtistsCopy() {
    return new ArrayList<Artist>(fArtists);
  }
  
  public List<Label> getLabels() {
    return Collections.unmodifiableList(fLabels);
  }
  
  public List<Label> getLabelsCopy() {
    return new ArrayList<Label>(fLabels);
  }
  
  public List<Relation> getRelations() {
    return Collections.unmodifiableList(fRelations);
  }
  
  public List<Relation> getRelationsCopy() {
    return new ArrayList<Relation>(fRelations);
  }
  
  public List<Song> getSongs() {
    return Collections.unmodifiableList(fSongs);
  }
  
  public List<Song> getSongsCopy() {
    return new ArrayList<Song>(fSongs);
  }
  
  public List<Album> getAlbums() {
    return Collections.unmodifiableList(fAlbums);
  }
  
  public List<Album> getAlbumsCopy() {
    return new ArrayList<Album>(fAlbums);
  }
  
  public List<Show> getShows() {
    return Collections.unmodifiableList(fShows);
  }
  
  public List<Show> getShowsCopy() {
    return new ArrayList<Show>(fShows);
  }
}
