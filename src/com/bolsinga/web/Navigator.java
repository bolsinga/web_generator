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
  private final Links fLinks;
  
  public Navigator(final Links links) {
    fLinks = links;
  }
  
  public Element getHomeNavigator() {
    return fLinks.getLinkToHome();
  }
  
  public Element getColophonNavigator() {
    return fLinks.getLinkToColophon();
  }
  
  public Element getOverviewNavigator() {
    return fLinks.getOverviewLink();
  }
  
  public Element getArtistNavigator() {
    return fLinks.getArtistLink();
  }
  
  public Element getTrackNavigator() {
    return fLinks.getTracksLink();
  }

  public Element getAlbumNavigator() {
    return fLinks.getAlbumsLink();
  }
  
  public Element getShowNavigator() {
    return fLinks.getShowLink();
  }
  
  public Element getVenueNavigator() {
    return fLinks.getVenueLink();
  }
  
  public Element getCityNavigator() {
    return fLinks.getCityLink();
  }
  
  public Element getCurrentNavigator() {
    return null;
  }
}
