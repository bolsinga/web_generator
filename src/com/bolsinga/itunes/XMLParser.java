package com.bolsinga.itunes;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

class XMLParser {
  private Map<String, Object> createPlist(final String sourceFile) throws ParserException {
    InputStream is = null;
    try {
      is = new FileInputStream(sourceFile);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find plist file: ");
      sb.append(sourceFile);
      throw new ParserException(sb.toString(), e);
    }

    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    SAXParser parser = null;
    try {
      parser = parserFactory.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't create SAXParser");
      throw new ParserException(sb.toString(), e);
    }

    XMLReader reader = null;
    try {
      reader = parser.getXMLReader();
    } catch (SAXException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't create XMLReader");
      throw new ParserException(sb.toString(), e);
    }

    ParserHandler handler = new ParserHandler();
    reader.setContentHandler(handler);

    InputSource source = new InputSource(is);
    try {
      reader.parse(source);
    } catch (IOException | SAXException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't parse InputSource");
      throw new ParserException(sb.toString(), e);
    }

    return handler.plist;
  }

  private Map<String, Object> createTracksDict(final String sourceFile) throws ParserException {
    Map<String, Object> plist = createPlist(sourceFile);

    for (Map.Entry<String, Object> entry : plist.entrySet()) {
      if (entry.getKey().equals("Tracks")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> dict = (Map<String, Object>)entry.getValue();
        return dict;
      }
    }
    throw new ParserException("No Tracks key in plist: " + plist.toString());
  }

  List<Track> createTracks(final String sourceFile) throws ParserException {
    Map<String, Object> tracksDict = createTracksDict(sourceFile);

    ArrayList<Track> tracks = new ArrayList<Track>();

    for (Object o : tracksDict.values()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> trackDict = (Map<String, Object>)o;

      Track track = new Track();

      for (Map.Entry<String, Object> entry : trackDict.entrySet()) {
        track.set(entry.getKey(), (String)entry.getValue());
      }

      tracks.add(track);
    }

    return tracks;
  }
}
