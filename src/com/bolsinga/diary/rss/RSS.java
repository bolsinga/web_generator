package com.bolsinga.diary.rss;

import com.bolsinga.diary.data.*;

import com.bolsinga.rss.data.*;

import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;

public class RSS {
	public static void add(com.bolsinga.diary.data.Entry entry, com.bolsinga.diary.util.Links links, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
		TRssItem item = objFactory.createTRssItem();
		List itemElements = item.getTitleOrDescriptionOrLink();
		
		itemElements.add(objFactory.createTRssItemTitle(com.bolsinga.diary.util.Util.getTitle(entry)));
		itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.util.Util.getRSSDate(entry.getTimestamp().getTime())));
		itemElements.add(objFactory.createTRssItemLink(com.bolsinga.web.util.Util.getSettings().getRssRoot() + links.getLinkTo(entry)));
		itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.web.util.Util.convertToParagraphs(entry.getComment())));
		
		channel.getItem().add(item);
	}
}
