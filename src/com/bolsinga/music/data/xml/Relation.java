package com.bolsinga.music.data.xml;

import java.util.*;

import javax.xml.bind.JAXBElement;

public class Relation implements com.bolsinga.music.data.Relation {
  private static final HashMap<String, Relation> sMap = new HashMap<String, Relation>();

  private final com.bolsinga.music.data.xml.impl.Relation fRelation;
  private final List<Object> fMembers;
  
  public static Relation get(final com.bolsinga.music.data.xml.impl.Relation item) {
    synchronized (sMap) {
      Relation result = sMap.get(item.getId());
      if (result == null) {
        result = new Relation(item);
        sMap.put(item.getId(), result);
      }
      return result;
    }
  }
  
  private Relation(final com.bolsinga.music.data.xml.impl.Relation relation) {
    fRelation = relation;
    
    fMembers = new ArrayList<Object>(fRelation.getMember().size());
    for (JAXBElement<Object> jmember : fRelation.getMember()) {
      Object member = null;
      Object xmlMember = jmember.getValue();
      if (xmlMember instanceof com.bolsinga.music.data.xml.impl.Artist) {
        member = Artist.get((com.bolsinga.music.data.xml.impl.Artist)xmlMember);
      } else if (xmlMember instanceof com.bolsinga.music.data.xml.impl.Venue) {
        member = Venue.get((com.bolsinga.music.data.xml.impl.Venue)xmlMember);
      } else if (xmlMember instanceof com.bolsinga.music.data.xml.impl.Label) {
        member = Label.get((com.bolsinga.music.data.xml.impl.Label)xmlMember);
      } else {
        throw new Error("No Relation: " + xmlMember.toString());
      }
      fMembers.add(member);
    }
  }
  
  public String getID() {
    return fRelation.getId();
  }
  
  public void setID(final String id) {
    fRelation.setId(id);
  }

  public String getReason() {
    return fRelation.getReason();
  }
  
  public void setReason(final String reason) {
    fRelation.setReason(reason);
  }

  public List<Object> getMembers() {
    return Collections.unmodifiableList(fMembers);
  }
}
