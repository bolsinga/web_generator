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
    if (args.length != 5) {
      RSS.usage();
    }

    String type = args[0];

    String settings = args[3];
    String output = args[4];

    Diary diary = null;
    Music music = null;

    if (type.equals("xml")) {
      String diaryFile = args[1];
      String musicFile = args[2];

      diary = com.bolsinga.diary.Util.createDiary(diaryFile);
      music = com.bolsinga.music.Util.createMusic(musicFile);
    } else if (type.equals("db")) {
      String user = args[1];
      String password = args[2];

      music = com.bolsinga.music.Util.createMusic(user, password);
      diary = com.bolsinga.diary.Util.createDiary(user, password);
    } else {
      RSS.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);
                        
    RSS.generate(diary, music, output);
  }

  private static void usage() {
    System.out.println("Usage: RSS xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: RSS db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
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
    add(getTitle(show), com.bolsinga.music.Util.toCalendar(show.getDate()), links.getLinkTo(show), show.getComment(), objFactory, channel);
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
    add(com.bolsinga.diary.Util.getTitle(entry), entry.getTimestamp(), links.getLinkTo(entry), entry.getComment(), objFactory, channel);
  }

  public static void add(String title, java.util.Calendar cal, String link, String description, com.bolsinga.rss.data.ObjectFactory objFactory, TRssChannel channel) throws JAXBException {
    TRssItem item = objFactory.createTRssItem();
    List itemElements = item.getTitleOrDescriptionOrLink();

    StringBuffer sb = new StringBuffer();
    sb.append(com.bolsinga.web.Util.getSettings().getContact());
    sb.append(" (");
    sb.append(System.getProperty("user.name"));
    sb.append(")");

    itemElements.add(objFactory.createTRssItemTitle(title));
    itemElements.add(objFactory.createTRssItemPubDate(com.bolsinga.rss.Util.getRSSDate(cal)));
    itemElements.add(objFactory.createTRssItemAuthor(sb.toString()));
    itemElements.add(objFactory.createTRssItemLink(com.bolsinga.web.Util.getSettings().getRssRoot() + link));
    itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.web.Util.convertToParagraphs(description)));
                
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
      channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.Util.getRSSDate(Calendar.getInstance())));
      channelElements.add(objFactory.createTRssChannelWebMaster(com.bolsinga.web.Util.getSettings().getContact()));

      TRssChannel.Image logo = com.bolsinga.rss.Util.createLogo(objFactory);
      logo.setLink(com.bolsinga.web.Util.getSettings().getRssRoot());
      logo.setDescription(diary.getTitle());
                        
      channelElements.add(logo);
                        
      com.bolsinga.music.Links musicLinks = com.bolsinga.music.Links.getLinks(false);
      com.bolsinga.diary.Links diaryLinks = com.bolsinga.diary.Links.getLinks(false);

      List recentItems = com.bolsinga.web.Util.getRecentItems(music, diary);
      Iterator i = recentItems.iterator();
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
}
