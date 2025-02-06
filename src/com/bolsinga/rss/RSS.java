package com.bolsinga.rss;

import com.bolsinga.diary.data.*;
import com.bolsinga.music.data.*;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class RSS {
  public static void generate(final Diary diary, final Music music, final String outputDir) throws RSSException {
    StringBuilder sb = new StringBuilder();
    sb.append(outputDir);
    sb.append(File.separator);
    sb.append(com.bolsinga.web.Links.ALT_DIR);
    File f = new File(sb.toString(), com.bolsinga.web.Util.getSettings().getRssFile());
    File parent = new File(f.getParent());
    if (!parent.mkdirs()) {
      if (!parent.exists()) {
        System.err.println("RSS cannot mkdirs: " + parent.getAbsolutePath());
      }
    }

    try (OutputStream os = new FileOutputStream(f)) {
      generate(diary, music, os);
    } catch (IOException e) {
      sb = new StringBuilder();
      sb.append("Can't create rss file: ");
      sb.append(f.toString());
      throw new RSSException(sb.toString(), e);
    }
  }

  private static void add(final Show show, final com.bolsinga.web.Links links, final Document document, final Node channel) {
    add(getTitle(show), com.bolsinga.web.Util.toCalendarUTC(show.getDate()), links.getLinkTo(show), show.getComment(), document, channel);
  }

  private static String getTitle(final Show show) {
    StringBuilder sb = new StringBuilder();

    sb.append(com.bolsinga.web.Util.toString(show.getDate()));
    sb.append(" - ");

    Iterator<? extends Artist> i = show.getArtists().iterator();
    while (i.hasNext()) {
      Artist performer = i.next();

      sb.append(performer.getName());

      if (i.hasNext()) {
        sb.append(", ");
      }
    }

    sb.append(" @ ");
    sb.append(show.getVenue().getName());

    return sb.toString();
  }

  private static void add(final Entry entry, final com.bolsinga.web.Links links, final Document document, final Node channel) {
    add(com.bolsinga.web.Util.getDisplayTitle(entry), GregorianCalendar.from(entry.getTimestamp()), links.getIdentifierPath(entry), entry.getComment(), document, channel);
  }

  private static void add(final String title, final Calendar cal, final String link, final String description, final Document document, final Node channel) {
		Element item = document.createElement("item");

    StringBuilder sb = new StringBuilder();
    sb.append(com.bolsinga.web.Util.getSettings().getContact());
    sb.append(" (");
    sb.append(System.getProperty("user.name"));
    sb.append(")");

		Element node = document.createElement("title");
		node.appendChild(document.createTextNode(title));
		item.appendChild(node);

		node = document.createElement("pubDate");
		node.appendChild(document.createTextNode(com.bolsinga.rss.Util.getRSSDate(cal)));
		item.appendChild(node);

		node = document.createElement("author");
		node.appendChild(document.createTextNode(sb.toString()));
		item.appendChild(node);

		node = document.createElement("link");
		node.appendChild(document.createTextNode(com.bolsinga.web.Util.getSettings().getRoot() + "/" + link));
		item.appendChild(node);

		node = document.createElement("description");
		node.appendChild(document.createTextNode(com.bolsinga.web.Util.convertToParagraphs(com.bolsinga.web.Encode.encodeROOT_URL(description)).toString()));
		item.appendChild(node);

    channel.appendChild(item);
  }

  private static void generate(final Diary diary, final Music music, final OutputStream os) throws RSSException {
    String diaryTitle = diary.getTitle();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Can't create XML Document Builder");
			throw new RSSException(sb.toString(), e);
		}

		Document document = builder.newDocument();
		document.setXmlStandalone(true);

		Element rss = document.createElement("rss");
		rss.setAttribute("version", "2.0");

		Element channel = document.createElement("channel");

		Element node = document.createElement("title");
		node.appendChild(document.createTextNode(diaryTitle));
		channel.appendChild(node);

		node = document.createElement("link");
		node.appendChild(document.createTextNode(com.bolsinga.web.Util.getSettings().getRoot()));
		channel.appendChild(node);

		node = document.createElement("description");
		node.appendChild(document.createTextNode(com.bolsinga.web.Util.getSettings().getRssDescription()));
		channel.appendChild(node);

		node = document.createElement("generator");
		node.appendChild(document.createTextNode(com.bolsinga.web.Util.getGenerator()));
		channel.appendChild(node);

    if (!com.bolsinga.web.Util.getDebugOutput()) {
			node = document.createElement("pubDate");
			node.appendChild(document.createTextNode(com.bolsinga.rss.Util.getRSSDate(com.bolsinga.web.Util.nowUTC())));
			channel.appendChild(node);
    }

		node = document.createElement("webMaster");
		node.appendChild(document.createTextNode(com.bolsinga.web.Util.getSettings().getContact()));
		channel.appendChild(node);

		com.bolsinga.web.Settings.Image image = com.bolsinga.web.Util.getSettings().getLogoImage();

		node = document.createElement("image");
		Node imageNode = document.createElement("url");
		imageNode.appendChild(document.createTextNode(image.getLocation()));
		node.appendChild(imageNode);

		imageNode = document.createElement("title");
		imageNode.appendChild(document.createTextNode(image.getAlt()));
		node.appendChild(imageNode);

		imageNode = document.createElement("link");
		imageNode.appendChild(document.createTextNode(com.bolsinga.web.Util.getSettings().getRoot()));
		node.appendChild(imageNode);

		imageNode = document.createElement("width");
		imageNode.appendChild(document.createTextNode(String.valueOf(image.getWidth())));
		node.appendChild(imageNode);

		imageNode = document.createElement("height");
		imageNode.appendChild(document.createTextNode(String.valueOf(image.getHeight())));
		node.appendChild(imageNode);

		imageNode = document.createElement("description");
		imageNode.appendChild(document.createTextNode(diaryTitle));
		node.appendChild(imageNode);

		channel.appendChild(node);

    com.bolsinga.web.Links links = com.bolsinga.web.Links.getLinks(false);

    int entryCount = com.bolsinga.web.Util.getSettings().getRecentCount();

    for (Object o : com.bolsinga.web.Util.getRecentItems(entryCount, music, diary)) {
      if (o instanceof Show) {
        RSS.add((Show)o, links, document, channel);
      } else if (o instanceof Entry) {
        RSS.add((Entry)o, links, document, channel);
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown recent item: ");
        sb.append(o.toString());
        throw new RSSException(sb.toString());
      }
    }

		rss.appendChild(channel);
		document.appendChild(rss);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Can't configure Transformer");
			throw new RSSException(sb.toString(), e);
		}
    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
    DOMSource source = new DOMSource(document);
    StreamResult streamResult = new StreamResult(os);
		try {
		  transformer.transform(source, streamResult);
		} catch (TransformerException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Can't Transform");
			throw new RSSException(sb.toString(), e);
		}
  }
}
