package com.bolsinga.rss;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.rss.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class RSS {

    public static void main(String[] args) {
	if (args.length != 4) {
	    System.out.println("Usage: RSS [diary.xml] [music.xml] [settings.xml] [output.dir]");
	    System.exit(0);
	}

        com.bolsinga.web.util.Util.createSettings(args[2]);
                        
	RSS.generate(args[0], args[1], args[3]);
    }

    public static void generate(String diaryFile, String musicFile, String outputDir) {
	Diary diary = com.bolsinga.diary.util.Util.createDiary(diaryFile);
	Music music = com.bolsinga.music.util.Util.createMusic(musicFile);
                
	generate(diary, music, outputDir);
    }
        
    public static void generate(Diary diary, Music music, String outputDir) {
	OutputStream os = null;
	try {
	    File f = new File(outputDir, "rss/rss.xml");
	    File parent = new File(f.getParent());
	    if (!parent.exists()) {
		if (!parent.mkdirs()) {
		    System.out.println("Can't: " + parent.getAbsolutePath());
		}
	    }
	    os = new FileOutputStream(f);
	} catch (IOException ioe) {
	    System.err.println(ioe);
	    ioe.printStackTrace();
	    System.exit(1);
	}
        
	generate(diary, music, os);
    }

    private static String getGenerator() {
	StringBuffer sb = new StringBuffer();

	sb.append(com.bolsinga.web.util.Util.getResourceString("program"));

	sb.append(" (built: ");
	sb.append(com.bolsinga.web.util.Util.getResourceString("builddate"));
	sb.append(" running on jdk ");
	sb.append(System.getProperty("java.runtime.version"));
	sb.append(" - ");
	sb.append(System.getProperty("os.name"));
	sb.append(" ");
	sb.append(System.getProperty("os.version"));

	sb.append(" [");
	sb.append(com.bolsinga.web.util.Util.getResourceString("copyright"));
	sb.append("]");

	sb.append(")");

	return sb.toString();
    }
        
    public static void generate(Diary diary, Music music, OutputStream os) {
	com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

	try {           
	    TRssChannel channel = objFactory.createTRssChannel();

	    List channelElements = channel.getTitleOrLinkOrDescription();
                        
	    channelElements.add(objFactory.createTRssChannelTitle(diary.getTitle()));
	    channelElements.add(objFactory.createTRssChannelLink(com.bolsinga.web.util.Util.getSettings().getRssRoot()));
	    channelElements.add(objFactory.createTRssChannelDescription(com.bolsinga.web.util.Util.getSettings().getRssDescription()));
	    channelElements.add(objFactory.createTRssChannelGenerator(getGenerator()));
	    channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.util.Util.getRSSDate(Calendar.getInstance().getTime())));
	    channelElements.add(objFactory.createTRssChannelWebMaster(com.bolsinga.web.util.Util.getSettings().getContact()));

	    TRssChannel.Image logo = com.bolsinga.rss.util.Util.createLogo(objFactory);
	    logo.setLink(com.bolsinga.web.util.Util.getSettings().getRssRoot());
	    logo.setDescription(diary.getTitle());
                        
	    channelElements.add(logo);

	    List shows = music.getShow();
	    Collections.sort(shows, com.bolsinga.music.util.Compare.SHOW_COMPARATOR);
	    Collections.reverse(shows);

	    List entries = diary.getEntry();
	    Collections.sort(entries, com.bolsinga.diary.util.Util.ENTRY_COMPARATOR);
	    Collections.reverse(entries);
                        
	    com.bolsinga.music.util.Links musicLinks = com.bolsinga.music.util.Links.getLinks(false);
	    com.bolsinga.diary.util.Links diaryLinks = com.bolsinga.diary.util.Links.getLinks(false);

            int entryCount = com.bolsinga.web.util.Util.getSettings().getRssCount().intValue();

	    Vector items = new Vector(entryCount * 2);
	    items.addAll(shows.subList(0, entryCount));
	    items.addAll(entries.subList(0, entryCount));

	    Collections.sort(items, CHANNEL_ITEM_COMPARATOR);
	    Collections.reverse(items);
            
	    Iterator i = items.subList(0, entryCount).iterator();
	    while (i.hasNext()) {
		Object o = i.next();
                                
		if (o instanceof com.bolsinga.music.data.Show) {
		    com.bolsinga.music.rss.RSS.add((Show)o, musicLinks, objFactory, channel);
		} else if (o instanceof com.bolsinga.diary.data.Entry) {
		    com.bolsinga.diary.rss.RSS.add((Entry)o, diaryLinks, objFactory, channel);
		}
	    }

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

    public static final Comparator CHANNEL_ITEM_COMPARATOR = new Comparator() {
	    public int compare(Object o1, Object o2) {
		Calendar c1 = null;
		Calendar c2 = null;
                        
		if (o1 instanceof com.bolsinga.music.data.Show) {
		    c1 = com.bolsinga.music.util.Util.toCalendar(((Show)o1).getDate());
		} else if (o1 instanceof com.bolsinga.diary.data.Entry) {
		    c1 = ((Entry)o1).getTimestamp();
		}

		if (o2 instanceof com.bolsinga.music.data.Show) {
		    c2 = com.bolsinga.music.util.Util.toCalendar(((Show)o2).getDate());
		} else if (o2 instanceof com.bolsinga.diary.data.Entry) {
		    c2 = ((Entry)o2).getTimestamp();
		}
                        
		return c1.getTime().compareTo(c2.getTime());
	    }
        };
}
