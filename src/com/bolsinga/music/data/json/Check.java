package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

// Checks music JSON before it is read, for what the raw reader stops on or the pages need:
// unique IDs with their kind's prefix, unique artist and venue names, references that resolve,
// shows with artists and possible dates, venues with a city and state, and relations of one kind.
// Returns every problem found, so a file from another writer can be fixed in one pass.
class Check {
  private static final String ID = "id";
  private static final String VENUES = "venues";
  private static final String ARTISTS = "artists";
  private static final String RELATIONS = "relations";
  private static final String SHOWS = "shows";
  private static final String VENUE = "venue";
  private static final String LOCATION = "location";
  private static final String CITY = "city";
  private static final String STATE = "state";
  private static final String MEMBERS = "members";
  private static final String NAME = "name";
  private static final String SONGS = "songs";
  private static final String ALBUMS = "albums";
  private static final String DATE = "date";
  private static final String YEAR = "year";
  private static final String MONTH = "month";
  private static final String DAY = "day";
  private static final String ZIP = "zip";
  private static final String COMMENT = "comment";

  // Each kind's IDs are its prefix and a number. Songs and albums are only checked when present,
  // so a file written before they were checked still reads.
  private static final Map<String, String> PREFIXES = new HashMap<String, String>();
  static {
    PREFIXES.put(VENUES, "v");
    PREFIXES.put(ARTISTS, "ar");
    PREFIXES.put(RELATIONS, "r");
    PREFIXES.put(SHOWS, "sh");
    PREFIXES.put(ALBUMS, "a");
    PREFIXES.put(SONGS, "s");
  }

  static List<String> problems(final JSONObject json) {
    List<String> problems = new ArrayList<String>();

    Set<String> venueIDs = Check.ids(json.optJSONArray(VENUES), VENUES, problems);
    Set<String> artistIDs = Check.ids(json.optJSONArray(ARTISTS), ARTISTS, problems);
    Check.ids(json.optJSONArray(RELATIONS), RELATIONS, problems);
    Check.ids(json.optJSONArray(SHOWS), SHOWS, problems);
    if (json.has(ALBUMS)) {
      Check.ids(json.optJSONArray(ALBUMS), ALBUMS, problems);
    }
    if (json.has(SONGS)) {
      Check.ids(json.optJSONArray(SONGS), SONGS, problems);
    }

    Check.artists(json.optJSONArray(ARTISTS), problems);
    Check.venues(json.optJSONArray(VENUES), problems);
    Check.shows(json.optJSONArray(SHOWS), venueIDs, artistIDs, problems);
    Check.relations(json.optJSONArray(RELATIONS), venueIDs, artistIDs, problems);

    return problems;
  }

  // A shows file (RFC 005) has what music JSON has, minus what iTunes gives, and minus the fields
  // bolsinga/site doesn't model: the raw files never set them, and the app would drop them.
  static List<String> showsFileProblems(final JSONObject json) {
    List<String> problems = Check.problems(json);

    for (String kind : new String[] { SONGS, ALBUMS }) {
      JSONArray items = json.optJSONArray(kind);
      if (items != null && items.length() != 0) {
        problems.add(kind + " isn't empty; iTunes gives them.");
      }
    }

    JSONArray venues = json.optJSONArray(VENUES);
    for (int i = 0; venues != null && i < venues.length(); i++) {
      JSONObject venue = venues.optJSONObject(i);
      if (venue == null) {
        continue;
      }
      JSONObject location = venue.optJSONObject(LOCATION);
      if (location != null && location.has(ZIP)) {
        problems.add(Check.describe(VENUES, i, venue) + " has a zip.");
      }
      if (venue.has(COMMENT)) {
        problems.add(Check.describe(VENUES, i, venue) + " has a comment.");
      }
    }

    JSONArray artists = json.optJSONArray(ARTISTS);
    for (int i = 0; artists != null && i < artists.length(); i++) {
      JSONObject artist = artists.optJSONObject(i);
      if (artist == null) {
        continue;
      }
      if (artist.has(LOCATION)) {
        problems.add(Check.describe(ARTISTS, i, artist) + " has a location.");
      }
      if (artist.has(COMMENT)) {
        problems.add(Check.describe(ARTISTS, i, artist) + " has a comment.");
      }
    }

    return problems;
  }

  private static String describe(final String kind, final int index, final JSONObject item) {
    StringBuilder sb = new StringBuilder();
    sb.append(kind);
    sb.append("[");
    sb.append(index);
    sb.append("]");
    String id = (item != null) ? item.optString(ID, null) : null;
    if (id != null) {
      sb.append(" ");
      sb.append(id);
    }
    return sb.toString();
  }

  // The IDs of one kind; an ID seen twice, or without the kind's prefix, is a problem.
  private static Set<String> ids(final JSONArray items, final String kind, final List<String> problems) {
    Set<String> ids = new HashSet<String>();
    if (items == null) {
      problems.add("No " + kind + " array.");
      return ids;
    }
    for (int i = 0; i < items.length(); i++) {
      JSONObject item = items.optJSONObject(i);
      String id = (item != null) ? item.optString(ID, null) : null;
      if (id == null) {
        problems.add(Check.describe(kind, i, item) + " has no id.");
        continue;
      }
      if (!ids.add(id)) {
        problems.add(Check.describe(kind, i, item) + " has a duplicate id.");
      }
      String prefix = PREFIXES.get(kind);
      if (!id.matches(prefix + "[0-9]+")) {
        problems.add(Check.describe(kind, i, item) + " isn't " + prefix + " and a number.");
      }
    }
    return ids;
  }

  // The raw files and iTunes join artists by name, so two artists can't share one.
  private static void artists(final JSONArray artists, final List<String> problems) {
    if (artists == null) {
      return;
    }
    Map<String, String> names = new HashMap<String, String>();
    for (int i = 0; i < artists.length(); i++) {
      JSONObject artist = artists.optJSONObject(i);
      if (artist == null) {
        continue;
      }
      String name = artist.optString(NAME, null);
      if (name == null) {
        problems.add(Check.describe(ARTISTS, i, artist) + " has no name.");
        continue;
      }
      String existing = names.putIfAbsent(name, artist.optString(ID, null));
      if (existing != null) {
        problems.add(Check.describe(ARTISTS, i, artist) + " has the same name as " + existing + ": " + name);
      }
    }
  }

  // Shows and relations in the raw files name venues, so two venues can't share a name either.
  private static void venues(final JSONArray venues, final List<String> problems) {
    if (venues == null) {
      return;
    }
    Map<String, String> names = new HashMap<String, String>();
    for (int i = 0; i < venues.length(); i++) {
      JSONObject venue = venues.optJSONObject(i);
      if (venue == null) {
        continue;
      }
      String name = venue.optString(NAME, null);
      if (name == null) {
        problems.add(Check.describe(VENUES, i, venue) + " has no name.");
      } else {
        String existing = names.putIfAbsent(name, venue.optString(ID, null));
        if (existing != null) {
          problems.add(Check.describe(VENUES, i, venue) + " has the same name as " + existing + ": " + name);
        }
      }
      JSONObject location = venue.optJSONObject(LOCATION);
      if (location == null) {
        problems.add(Check.describe(VENUES, i, venue) + " has no location.");
        continue;
      }
      if (location.optString(CITY, "").isEmpty()) {
        problems.add(Check.describe(VENUES, i, venue) + " has no city.");
      }
      if (location.optString(STATE, "").isEmpty()) {
        problems.add(Check.describe(VENUES, i, venue) + " has no state.");
      }
    }
  }

  private static void shows(final JSONArray shows, final Set<String> venueIDs, final Set<String> artistIDs, final List<String> problems) {
    if (shows == null) {
      return;
    }
    for (int i = 0; i < shows.length(); i++) {
      JSONObject show = shows.optJSONObject(i);
      if (show == null) {
        continue;
      }
      String name = Check.describe(SHOWS, i, show);
      Check.date(name, show.optJSONObject(DATE), problems);
      String venue = show.optString(VENUE, null);
      if (venue == null) {
        problems.add(name + " has no venue.");
      } else if (!venueIDs.contains(venue)) {
        problems.add(name + " names an unknown venue: " + venue);
      }
      JSONArray artists = show.optJSONArray(ARTISTS);
      if (artists == null || artists.length() == 0) {
        problems.add(name + " has no artists.");
        continue;
      }
      for (int j = 0; j < artists.length(); j++) {
        String artist = artists.optString(j, null);
        if (artist == null || !artistIDs.contains(artist)) {
          problems.add(name + " names an unknown artist: " + artist);
        }
      }
    }
  }

  // Unknown parts are left out. A known month is 1-12, and a known day exists in its month; February
  // has 29 days when the year is unknown or a leap year.
  private static void date(final String name, final JSONObject date, final List<String> problems) {
    if (date == null) {
      problems.add(name + " has no date.");
      return;
    }
    int year = date.optInt(YEAR, com.bolsinga.music.data.Date.UNKNOWN);
    int month = date.optInt(MONTH, com.bolsinga.music.data.Date.UNKNOWN);
    int day = date.optInt(DAY, com.bolsinga.music.data.Date.UNKNOWN);
    if (month != com.bolsinga.music.data.Date.UNKNOWN && (month < 1 || month > 12)) {
      problems.add(name + " has an impossible month: " + month);
      return;
    }
    if (day == com.bolsinga.music.data.Date.UNKNOWN) {
      return;
    }
    int max = 31;
    if (month != com.bolsinga.music.data.Date.UNKNOWN) {
      boolean leap = (year == com.bolsinga.music.data.Date.UNKNOWN) || java.time.Year.isLeap(year);
      max = java.time.Month.of(month).length(leap);
    }
    if (day < 1 || day > max) {
      problems.add(name + " has an impossible day: " + day);
    }
  }

  private static void relations(final JSONArray relations, final Set<String> venueIDs, final Set<String> artistIDs, final List<String> problems) {
    if (relations == null) {
      return;
    }
    for (int i = 0; i < relations.length(); i++) {
      JSONObject relation = relations.optJSONObject(i);
      if (relation == null) {
        continue;
      }
      String name = Check.describe(RELATIONS, i, relation);
      JSONArray members = relation.optJSONArray(MEMBERS);
      if (members == null) {
        problems.add(name + " has no members.");
        continue;
      }
      boolean hasArtist = false;
      boolean hasVenue = false;
      for (int j = 0; j < members.length(); j++) {
        String member = members.optString(j, null);
        if (member != null && artistIDs.contains(member)) {
          hasArtist = true;
        } else if (member != null && venueIDs.contains(member)) {
          hasVenue = true;
        } else {
          problems.add(name + " names an unknown member: " + member);
        }
      }
      if (hasArtist && hasVenue) {
        problems.add(name + " mixes artists and venues.");
      }
    }
  }
}
