package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

// Checks music JSON before it is read, for what the raw reader stops on or the pages need:
// unique IDs, references that resolve, shows with artists, venues with a city and state, and
// relations of one kind. Returns every problem found, so a file from another writer can be fixed
// in one pass.
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

  static List<String> problems(final JSONObject json) {
    List<String> problems = new ArrayList<String>();

    Set<String> venueIDs = Check.ids(json.optJSONArray(VENUES), VENUES, problems);
    Set<String> artistIDs = Check.ids(json.optJSONArray(ARTISTS), ARTISTS, problems);
    Check.ids(json.optJSONArray(SHOWS), SHOWS, problems);

    Check.venues(json.optJSONArray(VENUES), problems);
    Check.shows(json.optJSONArray(SHOWS), venueIDs, artistIDs, problems);
    Check.relations(json.optJSONArray(RELATIONS), venueIDs, artistIDs, problems);

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

  // The IDs of one kind; an ID seen twice is a problem.
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
      } else if (!ids.add(id)) {
        problems.add(Check.describe(kind, i, item) + " has a duplicate id.");
      }
    }
    return ids;
  }

  private static void venues(final JSONArray venues, final List<String> problems) {
    if (venues == null) {
      return;
    }
    for (int i = 0; i < venues.length(); i++) {
      JSONObject venue = venues.optJSONObject(i);
      if (venue == null) {
        continue;
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
