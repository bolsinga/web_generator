package com.bolsinga.music.data;

import java.util.*;

public interface Relation {
  public String getID();
  public void setID(final String id);
  public String getReason();
  public void setReason(final String reason);
  public List<Object> getMembers();
}
