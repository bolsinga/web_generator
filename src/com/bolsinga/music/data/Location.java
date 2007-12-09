package com.bolsinga.music.data;

public interface Location {
  public String getStreet();
  public void setStreet(final String street);
  public String getCity();
  public void setCity(final String city);
  public String getState();
  public void setState(final String state);
  public int getZip();  // return 0 if unknown
  public void setZip(final int zip);
  public String getWeb();
  public void setWeb(final String web);
}
