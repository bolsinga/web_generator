package com.bolsinga.rss;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.rss.data.*;
import com.bolsinga.settings.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.*;

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

  public static void generate(final String diaryFile, final String musicFile, final String outputDir) {
    Diary diary = com.bolsinga.diary.Util.createDiary(diaryFile);
    Music music = com.bolsinga.music.Util.createMusic(musicFile);
                
    generate(diary, music, outputDir);
  }
        
  public static void generate(final Diary diary, final Music music, final String outputDir) {
    OutputStream os = null;
    try {
      File f = new File(outputDir, "rss/rss.xml");
      File parent = new File(f.getParent());
      if (!parent.mkdirs()) {
        if (!parent.exists()) {
          System.out.println("RSS cannot mkdirs: " + parent.getAbsolutePath());
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

  public static void add(final com.bolsinga.music.data.Show show, final com.bolsinga.music.Links links, final com.bolsinga.rss.data.ObjectFactory objFactory, final TRssChannel channel) throws JAXBException {
    add(getTitle(show), com.bolsinga.music.Util.toCalendarUTC(show.getDate()), links.getLinkTo(show), show.getComment(), objFactory, channel);
  }
        
  private static String getTitle(final Show show) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(com.bolsinga.music.Util.toString(show.getDate()));
    sb.append(" - ");

    Iterator<JAXBElement<Object>> i = show.getArtist().iterator();
    while (i.hasNext()) {
      Artist performer = (Artist)i.next().getValue();
                        
      sb.append(performer.getName());
                        
      if (i.hasNext()) {
        sb.append(", ");
      }
    }
                
    sb.append(" @ ");
    sb.append(((Venue)show.getVenue()).getName());
                        
    return sb.toString();
  }

  public static void add(final com.bolsinga.diary.data.Entry entry, final com.bolsinga.diary.Links links, final com.bolsinga.rss.data.ObjectFactory objFactory, final TRssChannel channel) throws JAXBException {
    add(com.bolsinga.diary.Util.getTitle(entry), entry.getTimestamp().toGregorianCalendar(), links.getLinkTo(entry), entry.getComment(), objFactory, channel);
  }

  public static void add(final String title, final GregorianCalendar cal, final String link, final String description, final com.bolsinga.rss.data.ObjectFactory objFactory, final TRssChannel channel) throws JAXBException {
    TRssItem item = objFactory.createTRssItem();
    List<Object> itemElements = item.getTitleOrDescriptionOrLink();

    StringBuilder sb = new StringBuilder();
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

  public static void generate(final Diary diary, final Music music, final OutputStream os) {
    com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

    try {           
      TRssChannel channel = objFactory.createTRssChannel();

      List<Object> channelElements = channel.getTitleOrLinkOrDescription();

      String diaryTitle = diary.getTitle();
      
      channelElements.add(objFactory.createTRssChannelTitle(diaryTitle));
      channelElements.add(objFactory.createTRssChannelLink(com.bolsinga.web.Util.getSettings().getRssRoot()));
      channelElements.add(objFactory.createTRssChannelDescription(com.bolsinga.web.Util.getSettings().getRssDescription()));
      channelElements.add(objFactory.createTRssChannelGenerator(com.bolsinga.web.Util.getGenerator()));
      channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.Util.getRSSDate(com.bolsinga.web.Util.nowUTC())));
      channelElements.add(objFactory.createTRssChannelWebMaster(com.bolsinga.web.Util.getSettings().getContact()));

      TImage logo = com.bolsinga.rss.Util.createLogo(objFactory);
      logo.setLink(com.bolsinga.web.Util.getSettings().getRssRoot());
      logo.setDescription(diaryTitle);
                        
      channelElements.add(objFactory.createTRssChannelImage(logo));
                        
      com.bolsinga.music.Links musicLinks = com.bolsinga.music.Links.getLinks(false);
      com.bolsinga.diary.Links diaryLinks = com.bolsinga.diary.Links.getLinks(false);

      int entryCount = com.bolsinga.web.Util.getSettings().getRecentCount().intValue();

      for (Object o : com.bolsinga.web.Util.getRecentItems(entryCount, music, diary)) {
        if (o instanceof com.bolsinga.music.data.Show) {
          RSS.add((Show)o, musicLinks, objFactory, channel);
        } else if (o instanceof com.bolsinga.diary.data.Entry) {
          RSS.add((Entry)o, diaryLinks, objFactory, channel);
        } else {
          System.err.println("Unknown recent item." + o.toString());
          Thread.dumpStack();
          System.exit(1);
        }
      }

      TRss rss = objFactory.createTRss();
      rss.setVersion(new java.math.BigDecimal(2.0));
      rss.setChannel(channel);

      JAXBElement<TRss> jrss = objFactory.createRss(rss);                                                
      // Write out to the output file.
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.rss.data");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      m.marshal(jrss, os);
                        
    } catch (JAXBException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
