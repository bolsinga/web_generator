package com.bolsinga.diary.rss;

import com.bolsinga.diary.data.*;
import com.bolsinga.diary.util.*;

import com.bolsinga.rss.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class RSS {

	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.diary.rss.rss");

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: RSS [# entries in RSS file] [diary.xml] [output.file]");
			System.exit(0);
		}
		
		RSS.generate(Integer.parseInt(args[0]), args[1], args[2]);
	}
		
	public static void generate(int entryCount, String sourceFile, String outputFile) {
		Diary diary = Util.createDiary(sourceFile);

		com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

		try {		
			TRssChannel channel = objFactory.createTRssChannel();

			List channelElements = channel.getTitleOrLinkOrDescription();
			
			channelElements.add(objFactory.createTRssChannelTitle(diary.getTitle()));
			channelElements.add(objFactory.createTRssChannelLink(System.getProperty("diary.link")));
			channelElements.add(objFactory.createTRssChannelDescription(System.getProperty("diary.description")));
			channelElements.add(objFactory.createTRssChannelGenerator(sResource.getString("program")));
			channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.util.Util.getRSSDate(Calendar.getInstance().getTime())));
			channelElements.add(objFactory.createTRssChannelWebMaster(System.getProperty("diary.contact")));
			
			generate(entryCount, diary, objFactory, channel);

			TRss rss = objFactory.createRss();
			rss.setVersion(new java.math.BigDecimal(2.0));
			rss.setChannel(channel);
			
			// Write out to the output file.
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.rss.data");
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			OutputStream os = null;
			try {
				os = new FileOutputStream(outputFile);
			} catch (IOException ioe) {
				System.err.println(ioe);
				ioe.printStackTrace();
				System.exit(1);
			}
			m.marshal(rss, os);
			
		} catch (JAXBException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void generate(int entryCount, Diary diary, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
		List items = diary.getEntry();
		Entry entry = null;

		List rssItems = channel.getItem();
		TRssItem item = null;
		List itemElements = null;
		
		Collections.sort(items, Util.ENTRY_COMPARATOR);
		Collections.reverse(items);

		Links links = Links.getLinks(false);
		
		for (int i = 0; i < entryCount; i++) {
			entry = (Entry)items.get(i);
			
			item = objFactory.createTRssItem();
			itemElements = item.getTitleOrDescriptionOrLink();
			
			itemElements.add(objFactory.createTRssItemTitle(Util.getTitle(entry)));
			itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.util.Util.getRSSDate(entry.getTimestamp().getTime())));
			itemElements.add(objFactory.createTRssItemLink(System.getProperty("diary.link") + links.getLinkTo(entry)));
			itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.rss.util.Util.createDescription(entry.getComment(), 200)));
			
			rssItems.add(item);
		}
	}
}
