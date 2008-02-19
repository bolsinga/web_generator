package com.bolsinga.music.data.json;

import java.util.*;

import org.json.*;

public class Relation implements com.bolsinga.music.data.Relation {
  private static final String ID = "id";
  private static final String REASON = "reason";
  private static final String MEMBERS = "members";

  private String id;
  private String reason;
  private List<Object> members;

  static Relation create(final com.bolsinga.music.data.Relation relation) {
    return new Relation(relation);
  }
  
  static Relation create(final JSONObject json) throws Exception {
    return new Relation(json);
  }
  
  static JSONObject createJSON(final com.bolsinga.music.data.Relation relation) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, relation.getID());
    String reason = relation.getReason();
    if (reason != null) {
      json.put(REASON, reason);
    }

    List<Object> members = relation.getMembers();
    List<String> IDs = new ArrayList<String>(members.size());
    for (final Object o : members) {
      if (o instanceof com.bolsinga.music.data.Artist) {
        IDs.add(((com.bolsinga.music.data.Artist)o).getID());
      } else if (o instanceof com.bolsinga.music.data.Venue) {
        IDs.add(((com.bolsinga.music.data.Venue)o).getID());
      } else if (o instanceof com.bolsinga.music.data.Label) {
        IDs.add(((com.bolsinga.music.data.Label)o).getID());
      } else {
        throw new Error("No Relation: " + o.toString());
      }
    }
    json.put(MEMBERS, IDs);
    
    return json;
  }
  
  private Relation() {
  
  }
  
  private Relation(final com.bolsinga.music.data.Relation relation) {
    id = relation.getID();
    reason = relation.getReason();
    
    List<Object> srcMembers = relation.getMembers();
    members = new ArrayList<Object>(srcMembers.size());
    for (Object o : srcMembers) {
      Object member = null;

      if (o instanceof com.bolsinga.music.data.Artist) {
        member = Artist.get((com.bolsinga.music.data.Artist)o);
      } else if (o instanceof com.bolsinga.music.data.Venue) {
        member = Venue.get((com.bolsinga.music.data.Venue)o);
      } else if (o instanceof com.bolsinga.music.data.Label) {
        member = Label.get((com.bolsinga.music.data.Label)o);
      } else {
        throw new Error("No Relation: " + o.toString());
      }
      members.add(member);
    }
  }
  
  private Relation(final JSONObject json) throws Exception {
    id = json.getString(ID);
    reason = json.optString(REASON, null);
    JSONArray jsonArray = json.optJSONArray(MEMBERS);
    members = new ArrayList<Object>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      String jsonID = jsonArray.getString(i);
      Artist artist = Artist.get(jsonID);
      if (artist != null) {
        members.add(artist);
        continue;
      }
      Venue venue = Venue.get(jsonID);
      if (venue != null) {
        members.add(venue);
        continue;
      }
      Label label = Label.get(jsonID);
      if (label != null) {
        members.add(label);
        continue;
      }
      StringBuilder sb = new StringBuilder();
      sb.append("Unknown Relation: ");
      sb.append(id);
      sb.append(" reason: ");
      sb.append(reason);
      sb.append(" member ID: ");
      sb.append(jsonID);
      throw new com.bolsinga.web.WebException(sb.toString());
    }
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(final String id) {
    this.id = id;
  }
  
  public String getReason() {
    return reason;
  }
  
  public void setReason(final String reason) {
    this.reason = reason;
  }
  
  public List<Object> getMembers() {
    return members;
  }
}
