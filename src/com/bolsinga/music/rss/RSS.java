package com.bolsinga.music.rss;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;

import com.bolsinga.rss.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class RSS {
	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.music.rss.rss");

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: RSS [# entries in RSS file] [music.xml] [output.file]");
			System.exit(0);
		}
		
		RSS.generate(Integer.parseInt(args[0]), args[1], args[2]);
	}

	private static String getGenerator() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(sResource.getString("program"));
		
		sb.append(" (built: ");
		sb.append(sResource.getString("builddate"));
		sb.append(" running on jdk ");
		sb.append(System.getProperty("java.runtime.version"));
		sb.append(" - ");
		sb.append(System.getProperty("os.name"));
		sb.append(" ");
		sb.append(System.getProperty("os.version"));
		sb.append(")");
		
		return sb.toString();
	}

	public static void generate(int entryCount, String sourceFile, String outputFile) {
		Music music = Util.createMusic(sourceFile);
		
		generate(entryCount, music, outputFile);
	}
	
	public static void generate(int entryCount, Music music, String outputFile) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(outputFile);
		} catch (IOException ioe) {
			System.err.println(ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
		
		generate(entryCount, music, os);
	}
	
	public static void generate(int entryCount, Music music, OutputStream os) {
		com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

		try {		
			TRssChannel channel = objFactory.createTRssChannel();

			List channelElements = channel.getTitleOrLinkOrDescription();
			
			channelElements.add(objFactory.createTRssChannelTitle(System.getProperty("music.title")));
			channelElements.add(objFactory.createTRssChannelLink(System.getProperty("music.link")));
			channelElements.add(objFactory.createTRssChannelDescription(System.getProperty("music.description")));
			channelElements.add(objFactory.createTRssChannelGenerator(getGenerator()));
			channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.util.Util.getRSSDate(Calendar.getInstance().getTime())));
			channelElements.add(objFactory.createTRssChannelWebMaster(System.getProperty("music.contact")));
			
			generate(entryCount, music, objFactory, channel);

			TRss rss = objFactory.createRss();
			rss.setVersion(new java.math.BigDecimal(2.0));
			rss.setChannel(channel);
			
			// Write out to the output file.
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.rss.data");
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			m.marshal(rss, os);
			
		} catch (JAXBException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void generate(int entryCount, Music music, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
		List items = music.getShow();
		Show show = null;

		List rssItems = channel.getItem();
		TRssItem item = null;
		List itemElements = null;
		
		Collections.sort(items, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
		Collections.reverse(items);

		Links links = Links.getLinks(false);
		
		for (int i = 0; i < entryCount; i++) {
			show = (Show)items.get(i);
			
			item = objFactory.createTRssItem();
			itemElements = item.getTitleOrDescriptionOrLink();
			
			itemElements.add(objFactory.createTRssItemTitle(getTitle(show)));
			itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.util.Util.getRSSDate(com.bolsinga.music.util.Util.toCalendar(show.getDate()).getTime())));
			itemElements.add(objFactory.createTRssItemLink(System.getProperty("music.link") + links.getLinkTo(show)));
			itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.rss.util.Util.createDescription(show.getComment(), 200)));
			
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
