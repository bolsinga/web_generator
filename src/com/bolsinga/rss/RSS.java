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

    com.bolsinga.web.Util.createSettings(args[2]);
                        
    RSS.generate(args[0], args[1], args[3]);
  }

  public static void generate(String diaryFile, String musicFile, String outputDir) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
                
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

  public static void add(com.bolsinga.music.data.Show show, com.bolsinga.music.Links links, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
    TRssItem item = objFactory.createTRssItem();
    List itemElements = item.getTitleOrDescriptionOrLink();
                
    itemElements.add(objFactory.createTRssItemTitle(getTitle(show)));
    itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.Util.getRSSDate(com.bolsinga.music.Util.toCalendar(show.getDate()).getTime())));
    itemElements.add(objFactory.createTRssItemLink(com.bolsinga.web.Util.getSettings().getRssRoot() + links.getLinkTo(show)));
    itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.web.Util.convertToParagraphs(show.getComment())));
                
    channel.getItem().add(item);
  }
        
  private static String getTitle(Show show) {
    StringBuffer sb = new StringBuffer();
    
    sb.append(com.bolsinga.music.Util.toString(show.getDate()));
    sb.append(" - ");
                
    ListIterator i = show.getArtist().listIterator();
    while (i.hasNext()) {
      Artist performer = (Artist)i.next();
                        
      sb.append(performer.getName());
                        
      if (i.hasNext()) {
        sb.append(", ");
      }
    }
                
    sb.append(" @ ");
    sb.append(((Venue)show.getVenue()).getName());
                        
    return sb.toString();
  }

  public static void add(com.bolsinga.diary.data.Entry entry, com.bolsinga.diary.Links links, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
    TRssItem item = objFactory.createTRssItem();
    List itemElements = item.getTitleOrDescriptionOrLink();
                
    itemElements.add(objFactory.createTRssItemTitle(com.bolsinga.diary.Util.getTitle(entry)));
    itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.Util.getRSSDate(entry.getTimestamp().getTime())));
    itemElements.add(objFactory.createTRssItemLink(com.bolsinga.web.Util.getSettings().getRssRoot() + links.getLinkTo(entry)));
    itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.web.Util.convertToParagraphs(entry.getComment())));
                
    channel.getItem().add(item);
  }

  public static void generate(Diary diary, Music music, OutputStream os) {
    com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

    try {           
      TRssChannel channel = objFactory.createTRssChannel();

      List channelElements = channel.getTitleOrLinkOrDescription();
                        
      channelElements.add(objFactory.createTRssChannelTitle(diary.getTitle()));
      channelElements.add(objFactory.createTRssChannelLink(com.bolsinga.web.Util.getSettings().getRssRoot()));
      channelElements.add(objFactory.createTRssChannelDescription(com.bolsinga.web.Util.getSettings().getRssDescription()));
      channelElements.add(objFactory.createTRssChannelGenerator(com.bolsinga.web.Util.getGenerator()));
      channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.Util.getRSSDate(Calendar.getInstance().getTime())));
      channelElements.add(objFactory.createTRssChannelWebMaster(com.bolsinga.web.Util.getSettings().getContact()));

      TRssChannel.Image logo = com.bolsinga.rss.Util.createLogo(objFactory);
      logo.setLink(com.bolsinga.web.Util.getSettings().getRssRoot());
      logo.setDescription(diary.getTitle());
                        
      channelElements.add(logo);

      List shows = music.getShow();
      Collections.sort(shows, com.bolsinga.music.Compare.SHOW_COMPARATOR);
      Collections.reverse(shows);

      List entries = diary.getEntry();
      Collections.sort(entries, com.bolsinga.diary.Util.ENTRY_COMPARATOR);
      Collections.reverse(entries);
                        
      com.bolsinga.music.Links musicLinks = com.bolsinga.music.Links.getLinks(false);
      com.bolsinga.diary.Links diaryLinks = com.bolsinga.diary.Links.getLinks(false);

      int entryCount = com.bolsinga.web.Util.getSettings().getRssCount().intValue();

      Vector items = new Vector(entryCount * 2);
      items.addAll(shows.subList(0, entryCount));
      items.addAll(entries.subList(0, entryCount));

      Collections.sort(items, CHANNEL_ITEM_COMPARATOR);
      Collections.reverse(items);
            
      Iterator i = items.subList(0, entryCount).iterator();
      while (i.hasNext()) {
        Object o = i.next();
                                
        if (o instanceof com.bolsinga.music.data.Show) {
          RSS.add((Show)o, musicLinks, objFactory, channel);
        } else if (o instanceof com.bolsinga.diary.data.Entry) {
          RSS.add((Entry)o, diaryLinks, objFactory, channel);
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
          c1 = com.bolsinga.music.Util.toCalendar(((Show)o1).getDate());
        } else if (o1 instanceof com.bolsinga.diary.data.Entry) {
          c1 = ((Entry)o1).getTimestamp();
        }

        if (o2 instanceof com.bolsinga.music.data.Show) {
          c2 = com.bolsinga.music.Util.toCalendar(((Show)o2).getDate());
        } else if (o2 instanceof com.bolsinga.diary.data.Entry) {
          c2 = ((Entry)o2).getTimestamp();
        }
                        
        return c1.getTime().compareTo(c2.getTime());
      }
    };
}
