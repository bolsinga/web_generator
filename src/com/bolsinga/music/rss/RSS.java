package com.bolsinga.music.rss;

import com.bolsinga.music.data.*;

import com.bolsinga.rss.data.*;

import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;

public class RSS {
    public static void add(com.bolsinga.music.data.Show show, com.bolsinga.music.util.Links links, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
	TRssItem item = objFactory.createTRssItem();
	List itemElements = item.getTitleOrDescriptionOrLink();
                
	itemElements.add(objFactory.createTRssItemTitle(getTitle(show)));
	itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.util.Util.getRSSDate(com.bolsinga.music.util.Util.toCalendar(show.getDate()).getTime())));
	itemElements.add(objFactory.createTRssItemLink(com.bolsinga.web.util.Util.getSettings().getRssRoot() + links.getLinkTo(show)));
	itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.web.util.Util.convertToParagraphs(show.getComment())));
                
	channel.getItem().add(item);
    }
        
    private static String getTitle(Show show) {
	StringBuffer sb = new StringBuffer();
    
        sb.append(com.bolsinga.music.util.Util.toString(show.getDate()));
        sb.append(" - ");
                
	ListIterator li = show.getArtist().listIterator();
	while (li.hasNext()) {
	    Artist performer = (Artist)li.next();
                        
	    sb.append(performer.getName());
                        
	    if (li.hasNext()) {
		sb.append(", ");
	    }
	}
                
	sb.append(" @ ");
	sb.append(((Venue)show.getVenue()).getName());
                        
	return sb.toString();
    }
}
