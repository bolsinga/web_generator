package com.bolsinga.site;

import java.text.Normalizer;
import java.util.*;

// Looks for artist names and album and song titles that won't match the same name typed elsewhere,
// and returns a warning for each. Artists are joined by name, so these matter even where pages look
// right. Warnings don't stop the run.
//  - Spaces at either end, or two spaces in a row.
//  - Accents stored decomposed (a letter followed by a combining mark) rather than precomposed.
// Sort names aren't checked: the raw reader makes them decomposed on purpose.
class NameCheck {
  static List<String> warnings(final com.bolsinga.music.data.Music music) {
    List<String> warnings = new ArrayList<String>();

    for (com.bolsinga.music.data.Artist artist : music.getArtists()) {
      NameCheck.check(artist.getID() + " artist", artist.getName(), warnings);
    }
    for (com.bolsinga.music.data.Album album : music.getAlbums()) {
      NameCheck.check(NameCheck.where(album.getID() + " album", album.getPerformer()), album.getTitle(), warnings);
    }
    for (com.bolsinga.music.data.Song song : music.getSongs()) {
      NameCheck.check(NameCheck.where(song.getID() + " song", song.getPerformer()), song.getTitle(), warnings);
    }

    return warnings;
  }

  // Albums and songs also name their artist, to find them in the Music app.
  private static String where(final String item, final com.bolsinga.music.data.Artist performer) {
    if (performer == null) {
      return item;
    }
    StringBuilder sb = new StringBuilder(item);
    sb.append(" by ");
    sb.append(performer.getName());
    return sb.toString();
  }

  private static void check(final String where, final String name, final List<String> warnings) {
    if (name == null) {
      return;
    }
    if (!name.equals(name.strip()) || name.contains("  ")) {
      warnings.add(NameCheck.describe(where, name, "has extra spaces"));
    }
    if (!Normalizer.isNormalized(name, Normalizer.Form.NFC)) {
      warnings.add(NameCheck.describe(where, name, "has decomposed accents"));
    }
  }

  private static String describe(final String where, final String name, final String problem) {
    StringBuilder sb = new StringBuilder();
    sb.append(where);
    sb.append(" ");
    sb.append(problem);
    sb.append(": \"");
    sb.append(name);
    sb.append("\"");
    return sb.toString();
  }
}
