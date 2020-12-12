package com.bolsinga.diary.data;

import java.time.*;
import java.util.*;

public interface Entry {
  public String getComment();
  public void setComment(final String comment);
  public ZonedDateTime getTimestamp();
  public void setTimestamp(final ZonedDateTime timestamp);
  public String getTitle();
  public void setTitle(final String title);
  public String getID();
  public void setID(final String id);
}