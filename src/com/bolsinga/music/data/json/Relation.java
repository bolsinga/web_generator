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
  
  static JSONObject createJSON(final com.bolsinga.music.data.Relation relation) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put(ID, relation.getID());
    json.put(REASON, relation.getReason());

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
