package com.bolsinga.itunes;

import java.util.*;

public class Main {

  public static void main(String[] args) {
    if (args.length != 3) {
      Main.usage(args, "Wrong number of arguments");
    }

    int i = 0;
    String itunesXML = args[i++];
    String itunesJSON = args[i++];
    String output = args[i++];

    List<Track> xmlTracks = null;
    List<Track> jsonTracks = null;

    try {
      Parser xmlParser = new Parser();
      try {
        xmlTracks = xmlParser.parseTracks(itunesXML);
      } catch (ParserException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't parse iTunes XML file: ");
        sb.append(itunesXML);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }

      Parser jsonParser = new Parser();
      try {
        jsonTracks = jsonParser.parseTracks(itunesJSON);
      } catch (ParserException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't parse iTunes json file: ");
        sb.append(itunesJSON);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } catch (Exception e) {
      System.err.println(e.toString());
      System.exit(1);
    }

    System.err.println("XML tracks : " + xmlTracks.size() + " JSON tracks: " + jsonTracks.size());

    HashMap<String, Track> xmlPersistentIDs = new HashMap<String, Track>(xmlTracks.size());
    for (Track track : xmlTracks) {
      if (xmlPersistentIDs.containsKey(track.getPersistent_ID())) {
        System.err.println("track already in xml map: " + track.toString());
      } else {
        xmlPersistentIDs.put(track.getPersistent_ID(), track);
      }
    }

    HashMap<String, Track> jsonPersistentIDs = new HashMap<String, Track>(jsonTracks.size());
    for (Track track : jsonTracks) {
      if (jsonPersistentIDs.containsKey(track.getPersistent_ID())) {
        System.err.println("track already in json map: " + track.toString());
      } else {
        jsonPersistentIDs.put(track.getPersistent_ID(), track);
      }
    }

    System.out.println("Items in XML not in JSON");
    for (String key : xmlPersistentIDs.keySet()) {
      if (jsonPersistentIDs.containsKey(key)) {
        jsonPersistentIDs.remove(key);
      } else {
        System.out.println("Track: " + xmlPersistentIDs.get(key).toString());
      }
    }

    System.out.println("Items in JSON not in XML");
    for (String key : jsonPersistentIDs.keySet()) {
      Track track = jsonPersistentIDs.get(key);
      System.out.println("Track: " + track.toString());
    }
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [itunes.json] [output.dir]");
    System.out.println(reason);
    if (badargs != null) {
      System.out.println("Arguments:");
      for (int i = 0; i < badargs.length; i++) {
        System.out.print(badargs[i] + " ");
      }
    }
    System.out.println();
    System.exit(0);
  }
}
