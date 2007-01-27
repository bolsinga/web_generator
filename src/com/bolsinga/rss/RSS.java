package com.bolsinga.rss;

import com.bolsinga.diary.data.xml.*;
import com.bolsinga.music.data.xml.*;
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

      diary = com.bolsinga.web.Util.createDiary(diaryFile);
      music = com.bolsinga.web.Util.createMusic(musicFile);
    } else if (type.equals("db")) {
      String user = args[1];
      String password = args[2];

      music = com.bolsinga.music.MySQLCreator.createMusic(user, password);
      diary = com.bolsinga.diary.MySQLCreator.createDiary(user, password);
    } else {
      RSS.usage();
    }

    com.bolsinga.web.Util.createSettings(settings);
    
    try {
      RSS.generate(diary, music, output);
    } catch (RSSException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void usage() {
    System.out.println("Usage: RSS xml [diary.xml] [music.xml] [settings.xml] [output.dir]");
    System.out.println("Usage: RSS db [user] [password] [settings.xml] [output.dir]");
    System.exit(0);
  }

  private static void generate(final String diaryFile, final String musicFile, final String outputDir) throws RSSException {
    Diary diary = com.bolsinga.web.Util.createDiary(diaryFile);
    Music music = com.bolsinga.web.Util.createMusic(musicFile);
                
    generate(diary, music, outputDir);
  }
        
  public static void generate(final Diary diary, final Music music, final String outputDir) throws RSSException {
    OutputStream os = null;
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(com.bolsinga.web.Links.ALT_DIR);
    File f = new File(sb.toString(), com.bolsinga.web.Util.getSettings().getRssFile());
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.out.println("RSS cannot mkdirs: " + parent.getAbsolutePath());
      }
    }
    
    try {
      os = new FileOutputStream(f);
    } catch (IOException e) {
      sb = new StringBuilder();
      sb.append("Can't create: ");
      sb.append(f.toString());
      throw new RSSException(sb.toString(), e);
    }

    generate(diary, music, os);
  }

  private static void add(final Show show, final com.bolsinga.web.Links links, final com.bolsinga.rss.data.ObjectFactory objFactory, final TRssChannel channel) {
    add(getTitle(show), com.bolsinga.web.Util.toCalendarUTC(show.getDate()), links.getLinkTo(show), show.getComment(), objFactory, channel);
  }
        
  private static String getTitle(final Show show) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(com.bolsinga.web.Util.toString(show.getDate()));
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

  private static void add(final Entry entry, final com.bolsinga.web.Links links, final com.bolsinga.rss.data.ObjectFactory objFactory, final TRssChannel channel) {
    add(com.bolsinga.web.Util.getTitle(entry), entry.getTimestamp().toGregorianCalendar(), links.getLinkTo(entry), entry.getComment(), objFactory, channel);
  }

  private static void add(final String title, final GregorianCalendar cal, final String link, final String description, final com.bolsinga.rss.data.ObjectFactory objFactory, final TRssChannel channel) {
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
    itemElements.add(objFactory.createTRssItemLink(com.bolsinga.web.Util.getSettings().getRoot() + "/" + link));
    itemElements.add(objFactory.createTRssItemDescription(com.bolsinga.web.Util.convertToParagraphs(com.bolsinga.web.Encode.encodeROOT_URL(description)).toString()));
                
    channel.getItem().add(item);
  }

  private static void generate(final Diary diary, final Music music, final OutputStream os) throws RSSException {
    com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

    TRssChannel channel = objFactory.createTRssChannel();

    List<Object> channelElements = channel.getTitleOrLinkOrDescription();

    String diaryTitle = diary.getTitle();
    
    channelElements.add(objFactory.createTRssChannelTitle(diaryTitle));
    channelElements.add(objFactory.createTRssChannelLink(com.bolsinga.web.Util.getSettings().getRoot()));
    channelElements.add(objFactory.createTRssChannelDescription(com.bolsinga.web.Util.getSettings().getRssDescription()));
    channelElements.add(objFactory.createTRssChannelGenerator(com.bolsinga.web.Util.getGenerator()));
    if (!com.bolsinga.web.Util.getDebugOutput()) {
      channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.Util.getRSSDate(com.bolsinga.web.Util.nowUTC())));
    }
    channelElements.add(objFactory.createTRssChannelWebMaster(com.bolsinga.web.Util.getSettings().getContact()));

    TImage logo = com.bolsinga.rss.Util.createLogo(objFactory);
    logo.setLink(com.bolsinga.web.Util.getSettings().getRoot());
    logo.setDescription(diaryTitle);
                      
    channelElements.add(objFactory.createTRssChannelImage(logo));
                      
    com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(false);

    int entryCount = com.bolsinga.web.Util.getSettings().getRecentCount().intValue();

    for (Object o : com.bolsinga.web.Util.getRecentItems(entryCount, music, diary)) {
      if (o instanceof Show) {
        RSS.add((Show)o, links, objFactory, channel);
      } else if (o instanceof Entry) {
        RSS.add((Entry)o, links, objFactory, channel);
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown recent item: ");
        sb.append(o.toString());
        throw new RSSException(sb.toString());
      }
    }

    TRss rss = objFactory.createTRss();
    rss.setVersion(new java.math.BigDecimal(2.0));
    rss.setChannel(channel);

    JAXBElement<TRss> jrss = objFactory.createRss(rss);                                                

    try {           
      // Write out to the output file.
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.rss.data");
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
      m.marshal(jrss, os);
    } catch (JAXBException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't create: ");
      sb.append(os.toString());
      throw new RSSException(sb.toString(), e);
    }
  }
}
