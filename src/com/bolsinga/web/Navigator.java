//
//  Navigator.java
//  web_generator
//
//  Created by Greg Bolsinga on 12/28/06.
//  Copyright 2006 Bolsinga Software. All rights reserved.
//

package com.bolsinga.web;

import org.apache.ecs.*;

public class Navigator {
  private final com.bolsinga.music.Links fMusicLinks;
  
  public Navigator(final com.bolsinga.music.Links musicLinks) {
    fMusicLinks = musicLinks;
  }
  
  public Element getArtistNavigator() {
    return fMusicLinks.getArtistLink();
  }
  
  public Element getTrackNavigator() {
    return fMusicLinks.getTracksLink();
  }
  
  public Element getShowNavigator() {
    return fMusicLinks.getShowLink();
  }
  
  public Element getVenueNavigator() {
    return fMusicLinks.getVenueLink();
  }
  
  public Element getCityNavigator() {
    return fMusicLinks.getCityLink();
  }
  
  public Element getCurrentNavigator() {
    return null;
  }
}
