package com.bolsinga.rss.util;

import java.text.*;

import org.apache.ecs.html.*;

public class Util {
	private static DateFormat sRSSDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	
	public static String getRSSDate(java.util.Date date) {
		return sRSSDateFormat.format(date);
	}
	
	public static String createDescription(String data) {
		// Convert each line to <p> tags
		StringBuffer tagged = new StringBuffer();
		String[] lines = data.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			tagged.append(new P());
			tagged.append(lines[i]);
		}
		return tagged.toString();
	}
	
	public static com.bolsinga.rss.data.TRssChannel.Image createLogo(com.bolsinga.rss.data.ObjectFactory objFactory) throws javax.xml.bind.JAXBException {
		com.bolsinga.rss.data.TRssChannel.Image logo = objFactory.createTRssChannelImage();
		
		logo.setHeight(new java.math.BigInteger(System.getProperty("web.logo.height")));
		logo.setWidth(new java.math.BigInteger(System.getProperty("web.logo.width")));
		logo.setUrl(System.getProperty("web.logo.url"));
		logo.setTitle(System.getProperty("web.logo.alt"));
		
		return logo;
	}
}
