package com.bolsinga.rss;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;
import com.bolsinga.rss.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class RSS {

	private static ResourceBundle sResource = ResourceBundle.getBundle("com.bolsinga.rss.rss");

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: RSS [# entries in RSS file] [diary.xml] [music.xml] [output.file]");
			System.exit(0);
		}
		
		RSS.generate(Integer.parseInt(args[0]), args[1], args[2], args[3]);
	}

	public static void generate(int entryCount, String diaryFile, String musicFile, String outputFile) {
		Diary diary = com.bolsinga.diary.util.Util.createDiary(diaryFile);
		Music music = com.bolsinga.music.util.Util.createMusic(musicFile);
		
		generate(entryCount, diary, music, outputFile);
	}
	
	public static void generate(int entryCount, Diary diary, Music music, String outputFile) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(outputFile);
		} catch (IOException ioe) {
			System.err.println(ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
	
		generate(entryCount, diary, music, os);
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

		sb.append(" [");
		sb.append(sResource.getString("copyright"));
		sb.append("]");

		sb.append(")");

		return sb.toString();
	}
	
	public static void generate(int entryCount, Diary diary, Music music, OutputStream os) {
		com.bolsinga.rss.data.ObjectFactory objFactory = new com.bolsinga.rss.data.ObjectFactory();

		try {		
			TRssChannel channel = objFactory.createTRssChannel();

			List channelElements = channel.getTitleOrLinkOrDescription();
			
			channelElements.add(objFactory.createTRssChannelTitle(diary.getTitle()));
			channelElements.add(objFactory.createTRssChannelLink(System.getProperty("rss.root")));
			channelElements.add(objFactory.createTRssChannelDescription(System.getProperty("rss.description")));
			channelElements.add(objFactory.createTRssChannelGenerator(getGenerator()));
			channelElements.add(objFactory.createTRssChannelPubDate(com.bolsinga.rss.util.Util.getRSSDate(Calendar.getInstance().getTime())));
			channelElements.add(objFactory.createTRssChannelWebMaster(System.getProperty("rss.contact")));

			TRssChannel.Image logo = com.bolsinga.rss.util.Util.createLogo(objFactory);
			logo.setLink(System.getProperty("rss.root"));
			logo.setDescription(diary.getTitle());
			
			channelElements.add(logo);
			
			com.bolsinga.diary.rss.RSS.generate(entryCount / 2, diary, objFactory, channel);
			com.bolsinga.music.rss.RSS.generate(entryCount / 2, music, objFactory, channel);

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
