package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.music.data.xml.*;

import com.bolsinga.web.*;

public class Music {

  private static final int UNKNOWN_YEAR = 1900;
  
  private static final HashMap<String, com.bolsinga.music.data.xml.Venue> sVenues =
    new HashMap<String, com.bolsinga.music.data.xml.Venue>();
  private static final HashMap<String, String> sBandSorts = new HashMap<String, String>();
  private static final HashMap<String, Artist> sArtists =
    new HashMap<String, Artist>();
        
  public static com.bolsinga.music.data.xml.Music createMusic(final String showsFile, final String venueFile, final String bandFile, final String relationFile, final String iTunesFile) throws ConvertException {
    ObjectFactory objFactory = new ObjectFactory();
                
    com.bolsinga.music.data.xml.Music music = Music.createMusic(objFactory, showsFile, venueFile, bandFile, relationFile, iTunesFile);

    music.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(com.bolsinga.web.Util.nowUTC()));
    
    Music.dumpSimilarArtists(music);
    
    return music;
  }

  public static void convert(final String showsFile, final String venueFile, final String bandFile, final String relationFile, final String iTunesFile, final String outputFile) throws ConvertException {
    com.bolsinga.music.data.xml.Music music = Music.createMusic(showsFile, venueFile, bandFile, relationFile, iTunesFile);
    
    OutputStream os = null;
    try {
      try {
        os = new FileOutputStream(outputFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(outputFile);
        throw new ConvertException(sb.toString(), e);
      }

      try {
        // Write out to the output file.
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data.xml");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(music, os);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't marshall: ");
        sb.append(os.toString());
        throw new ConvertException(sb.toString(), e);
      }
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(outputFile);
          throw new ConvertException(sb.toString(), e);
        }
      }
    }
  }

  private static void dumpSimilarArtists(final com.bolsinga.music.data.xml.Music music) {
    String s;
    HashSet<String> bands = new HashSet<String>();
    boolean displayed = false;
    
    List<Artist> artists = Util.getArtistsUnmodifiable(music);
    for (Artist artist : artists) {
      s = artist.getName().toLowerCase();
      if (bands.contains(s)) {
        if (!displayed) {
          System.out.println("--Similar Artist Names--");
          displayed = true;
        }
        System.out.println(s);
      } else {
        bands.add(s);
      }
    }
  }

  private static com.bolsinga.music.data.xml.Music createMusic(final ObjectFactory objFactory, final String showsFile, final String venueFile, final String bandFile, final String relationFile, final String iTunesFile) throws ConvertException {
    List<Show> shows = Convert.shows(showsFile);
    List<Venue> venues = Convert.venuemap(venueFile);
    List<BandMap> bands = Convert.bandsort(bandFile);
    List<Relation> relations = Convert.relation(relationFile);

    com.bolsinga.music.data.xml.Music music = objFactory.createMusic();
        
    createVenues(objFactory, music, venues);
                
    createBandSort(objFactory, music, bands);
        
    convert(objFactory, music, shows);

    Collections.sort(music.getArtist(), com.bolsinga.music.Compare.ARTIST_COMPARATOR); // Modification required.

    try {
      com.bolsinga.itunes.converter.ITunes.addMusic(objFactory, music, iTunesFile);
    } catch (com.bolsinga.itunes.converter.ITunesException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't convert iTunes file: ");
      sb.append(iTunesFile);
      throw new ConvertException(sb.toString(), e);
    }

    createRelations(objFactory, music, relations);
                
    return music;
  }
        
  private static void createVenues(final ObjectFactory objFactory, final com.bolsinga.music.data.xml.Music music, final List<Venue> venues) {
    // Go through each venue.
    //  Create a Venue for each             
    // Make a hash of the Venue information by name

    com.bolsinga.music.data.xml.Venue xVenue = null;
    Location xLocation = null;
    String name = null;
    int index = 0;
                
    Collections.sort(venues, Compare.VENUE_COMPARATOR);
                
    for (Venue oldVenue : venues) {
      name = oldVenue.getName();
                        
      xLocation = objFactory.createLocation();
      xLocation.setState(oldVenue.getState());
      xLocation.setWeb(oldVenue.getURL());
      xLocation.setCity(oldVenue.getCity());
      xLocation.setStreet(oldVenue.getAddress());
                        
      xVenue = objFactory.createVenue();
      xVenue.setName(name);
      xVenue.setLocation(xLocation);
      xVenue.setId("v" + index++);
                        
      music.getVenue().add(xVenue); // Modification required.
                        
      sVenues.put(name, xVenue);
    }
  }
        
  private static void createBandSort(final ObjectFactory objFactory, final com.bolsinga.music.data.xml.Music music, final List<BandMap> bands) {
    // Make a hash of the band sort names by name
    for (BandMap bandMap : bands) {
      sBandSorts.put(bandMap.getName(), bandMap.getSortName());
    }
  }
        
  private static void createRelations(final ObjectFactory objFactory, final com.bolsinga.music.data.xml.Music music, final List<Relation> relations) throws ConvertException {
    com.bolsinga.music.data.xml.Relation xRelation = null;
    String type = null, reason = null;
    int index = 0;

    for (Relation oldRelation : relations) {
      type = oldRelation.getType();
                        
      xRelation = objFactory.createRelation();
      reason = oldRelation.getReason();
      if (!reason.equals("^")) {
        xRelation.setReason(reason);
      }
      xRelation.setId("r" + index++);
                        
      if (type.equals("band")) {
        xRelation.setType(RelationType.fromValue("artist"));

        for (String member : oldRelation.getMembers()) {
          Artist mArtist = sArtists.get(member);
          if (mArtist != null) {
            xRelation.getMember().add(objFactory.createRelationMember(mArtist));
          } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown artist relation: ");
            sb.append(reason);
            sb.append(" -> ");
            sb.append(member);
            throw new ConvertException(sb.toString());
          }
        }
      } else if (type.equals("venue")) {
        xRelation.setType(RelationType.fromValue(type));

        for (String member : oldRelation.getMembers()) {
          com.bolsinga.music.data.xml.Venue mVenue = sVenues.get(member);
          if (mVenue != null) {
            xRelation.getMember().add(objFactory.createRelationMember(mVenue));
          } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown venue relation: ");
            sb.append(reason);
            sb.append(" -> ");
            sb.append(member);
            throw new ConvertException(sb.toString());
          }
        }
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown relation type: ");
        sb.append(type);
        throw new ConvertException(sb.toString());
      }
                        
      music.getRelation().add(xRelation); // Modification required.
    }
  }
        
  private static com.bolsinga.music.data.xml.Date createDate(final ObjectFactory objFactory, final String date) {
    com.bolsinga.music.data.xml.Date result = objFactory.createDate();
                
    String monthString, dayString, yearString = null;
    int month, day, year = 0;
                
    StringTokenizer st = new StringTokenizer(date, "-");
                
    monthString = st.nextToken();
    dayString = st.nextToken();
    yearString = st.nextToken();
                
    month = Integer.parseInt(monthString);
    day = Integer.parseInt(dayString);
    year = Integer.parseInt(yearString);
                
    if ((month == 0) || (day == 0) || (year == UNKNOWN_YEAR)) {
      result.setUnknown(true);
    }
                
    if (month != 0) {
      result.setMonth(new java.math.BigInteger(monthString));
    }
                
    if (day != 0) {
      result.setDay(new java.math.BigInteger(dayString));
    }
                
    if (year != UNKNOWN_YEAR) {
      result.setYear(new java.math.BigInteger(yearString));
    }
                
    return result;
  }
        
  public static Artist addArtist(final ObjectFactory objFactory, final com.bolsinga.music.data.xml.Music music, final String name, final String sortName) throws ConvertException {
    Artist result = null;
    if (!sArtists.containsKey(name)) {
      result = objFactory.createArtist();
      result.setName(name);
      result.setId("ar" + sArtists.size());

      if (sortName != null) {
        result.setSortname(sortName);
      }
      
      music.getArtist().add(result); // Modification required.
      sArtists.put(name, result);
    } else {
      result = sArtists.get(name);

      // Be sure the sort name is set as expected (it may be only known at later calls!)
      String curSortName = result.getSortname();
      if ((curSortName != null) && (sortName != null)) {
        // Ensure what we are passed matches
        if (!curSortName.equals(sortName)) {
          StringBuilder sb = new StringBuilder();
          sb.append("Different Sort Names for: ");
          sb.append(name);
          sb.append("(");
          sb.append(curSortName);
          sb.append(", ");
          sb.append(sortName);
          sb.append(")");
          throw new ConvertException(sb.toString());
        }
      } if ((curSortName == null) && (sortName != null)) {
        result.setSortname(sortName);
      }
    }

    return result;
  }
                
  private static void convert(final ObjectFactory objFactory, final com.bolsinga.music.data.xml.Music music, final List<Show> shows) throws ConvertException {
    // Go through each show.
    //  Create an Artist for each band in the set, if it doesn't already exist. Use the sort name.
    //  Create a Date.
    //  Get the Venue from the hash.
    //  Create a Comment.
    //  Create a Show with the above.
                
    com.bolsinga.music.data.xml.Show xShow = null;
    Artist xArtist = null;
    com.bolsinga.music.data.xml.Date xDate = null;

    int index = 0;
                
    Collections.sort(shows, Compare.SHOW_COMPARATOR);

    for (Show oldShow : shows) {
      xShow = objFactory.createShow();
                        
      xDate = createDate(objFactory, oldShow.getDate());
      xShow.setDate(xDate);
                   
      for (String oldBand : oldShow.getBands()) {     
        String sortName = null;
        if (sBandSorts.containsKey(oldBand)) {
          sortName = sBandSorts.get(oldBand);
        }
        xArtist = addArtist(objFactory, music, oldBand, sortName);
                                
        xShow.getArtist().add(objFactory.createShowArtist(xArtist));
      }

      xShow.setVenue(sVenues.get(oldShow.getVenue()));
      if (oldShow.getComment() != null) {
        xShow.setComment(oldShow.getComment());
      }
      xShow.setId("sh" + index++);
                        
      music.getShow().add(xShow); // Modification required.
    }
  }
}
