package com.bolsinga.music.rss;

import com.bolsinga.music.data.*;

import com.bolsinga.rss.data.*;

import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;

public class RSS {
	public static void generate(int entryCount, Music music, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
		List items = music.getShow();
		Show show = null;

		List rssItems = channel.getItem();
		TRssItem item = null;
		List itemElements = null;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		Collections.reverse(items);

		com.bolsinga.music.util.Links links = com.bolsinga.music.util.Links.getLinks(false);
		
		for (int i = 0; i < entryCount; i++) {
			show = (Show)items.get(i);
			
			item = objFactory.createTRssItem();
			itemElements = item.getTitleOrDescriptionOrLink();
			
			itemElements.add(objFactory.createTRssItemTitle(getTitle(show)));
			itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.util.Util.getRSSDate(com.bolsinga.music.util.Util.toCalendar(show.getDate()).getTime())));
			itemElements.add(objFactory.createTRssItemLink(System.getProperty("rss.root") + links.getLinkTo(show)));
			itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.rss.util.Util.createDescription(show.getComment(), Integer.MAX_VALUE)));
			
			rssItems.add(item);
		}
	}
	
	private static String getTitle(Show show) {
		StringBuffer sb = new StringBuffer();
		
		ListIterator li = show.getPerformance().listIterator();
		while (li.hasNext()) {
			Performance p = (Performance)li.next();
			Artist performer = (Artist)p.getArtist();
			
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
