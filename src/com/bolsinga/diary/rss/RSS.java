package com.bolsinga.diary.rss;

import com.bolsinga.diary.data.*;

import com.bolsinga.rss.data.*;

import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;

public class RSS {
	public static void generate(int entryCount, Diary diary, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
		List items = diary.getEntry();
		Entry entry = null;

		List rssItems = channel.getItem();
		TRssItem item = null;
		List itemElements = null;
		
		Collections.sort(items, com.bolsinga.diary.util.Util.ENTRY_COMPARATOR);
		Collections.reverse(items);

		com.bolsinga.diary.util.Links links = com.bolsinga.diary.util.Links.getLinks(false);
		
		for (int i = 0; i < entryCount; i++) {
			entry = (Entry)items.get(i);
			
			item = objFactory.createTRssItem();
			itemElements = item.getTitleOrDescriptionOrLink();
			
			itemElements.add(objFactory.createTRssItemTitle(com.bolsinga.diary.util.Util.getTitle(entry)));
			itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.util.Util.getRSSDate(entry.getTimestamp().getTime())));
			itemElements.add(objFactory.createTRssItemLink(System.getProperty("rss.root") + links.getLinkTo(entry)));
			itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.rss.util.Util.createDescription(entry.getComment(), Integer.MAX_VALUE)));
			
			rssItems.add(item);
		}
	}
}
