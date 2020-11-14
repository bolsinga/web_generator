package com.bolsinga.music.data.raw;

import java.util.*;

public class Main {

  public static void main(String[] args) {
    if (args.length != 2) {
      Main.usage(args, "Wrong number of arguments");
    }

    int i = 0;
    String itunesXML = args[i++];
    String itunesJSON = args[i++];

    Media xmlMedia = null;
    Media jsonMedia = null;

    try {
      try {
        xmlMedia = Media.createMedia(itunesXML);
      } catch (com.bolsinga.web.WebException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't create Media for iTunes XML file: ");
        sb.append(itunesXML);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }

      try {
        jsonMedia = Media.createMedia(itunesJSON);
      } catch (com.bolsinga.web.WebException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't create Media for iTunes json file: ");
        sb.append(itunesJSON);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } catch (Exception e) {
      System.err.println(e.toString());
      System.exit(1);
    }

    System.err.println("XML albums : " + xmlMedia.fAlbums.size() + " JSON albums: " + jsonMedia.fAlbums.size());

    HashMap<String, Album> xmlAlbumIDs = new HashMap<String, Album>(xmlMedia.fAlbums.size());
    for (Album album : xmlMedia.fAlbums) {
      String metaKey = album.getID() + album.getTitle();
      if (xmlAlbumIDs.containsKey(metaKey)) {
        System.err.println("album already in xml map: " + albumDescription(album));
      } else {
        xmlAlbumIDs.put(metaKey, album);
      }
    }

    HashMap<String, Album> jsonAlbumIDs = new HashMap<String, Album>(jsonMedia.fAlbums.size());
    for (Album album : jsonMedia.fAlbums) {
      String metaKey = album.getID() + album.getTitle();
      if (jsonAlbumIDs.containsKey(metaKey)) {
        System.err.println("album already in json map: " + albumDescription(album));
      } else {
        jsonAlbumIDs.put(metaKey, album);
      }
    }

    System.out.println("Items in XML not in JSON");
    for (String key : xmlAlbumIDs.keySet()) {
      if (jsonAlbumIDs.containsKey(key)) {
        jsonAlbumIDs.remove(key);
      } else {
        Album album = xmlAlbumIDs.get(key);
        System.out.println("Album: " + albumDescription(album));
      }
    }

    System.out.println("Items in JSON not in XML");
    for (String key : jsonAlbumIDs.keySet()) {
      Album album = jsonAlbumIDs.get(key);
      System.out.println("Album: " + albumDescription(album));
    }
  }

  private static String albumDescription(final Album album) {
    StringBuilder sb = new StringBuilder();
    sb.append(album.getID());
    sb.append(": ");
    sb.append(album.getTitle());
    sb.append(": ");
    sb.append(album.getPerformer().getName());
    return sb.toString();
  }


  private static void usage(final String[] badargs, final String reason) {
    System.out.println("Usage: Main [iTunes Music.xml] [itunes.json]");
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
