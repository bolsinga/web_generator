package com.bolsinga.itunes;

import java.util.*;
import org.json.*;

public class Convert {

  public static void main(String[] args) {
    if (args.length != 1) {
      Convert.usage(args, "Wrong number of arguments");
    }

    int i = 0;
    String itunesXML = args[i++];

    List<Track> xmlTracks = null;

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
    } catch (Exception e) {
      System.err.println(e.toString());
      System.exit(1);
    }

    JSONArray jsonArray = new JSONArray(/*xmlTracks.size()*/);

    for (Track track : xmlTracks) {
      JSONObject jsonTrack = JSONParser.convert(track);
      if (jsonTrack != null) {
        jsonArray.put(jsonTrack);
      }
    }

    if (jsonArray.length() > 0) {
      String jsonString = jsonArray.toString(2);
      System.out.println(jsonString);
    }
  }

  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Convert [iTunes Music.xml]");
    System.out.println(reason);
    if (badargs != null) {
      System.out.println("Arguments:");
      for (int i = 0; i < badargs.length; i++) {
        System.out.print(badargs[i] + " ");
      }
    }
    System.out.println();
    System.exit(1);
  }
}
