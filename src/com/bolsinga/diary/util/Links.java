package com.bolsinga.diary.util;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Links {

	public static final String HTML_EXT = ".html";
	public static final String ARCHIVES_DIR = "archives";
	public static final String STYLES_DIR = "styles";
	public static final String RSS_DIR = "rss";
	private static final String HASH = "#";

	static DateFormat sArchivePageFormat = new SimpleDateFormat("yyyy");

	private boolean fUpOneLevel;
	
	public static Links getLinks(boolean upOneLevel) {
		return new Links(upOneLevel);
	}
	
	Links(boolean upOneLevel) {
		fUpOneLevel = upOneLevel;
	}
	
	public String getPageFileName(Entry entry) {
		return sArchivePageFormat.format(entry.getTimestamp().getTime());
	}

	public String getPagePath(Entry entry) {
		StringBuffer sb = new StringBuffer();

		sb.append(ARCHIVES_DIR);
		sb.append(File.separator);
		sb.append(getPageFileName(entry));
		sb.append(HTML_EXT);
		
		return sb.toString();
	}

	public String getLinkToPage(Entry entry) {
		StringBuffer link = new StringBuffer();
		
		if (fUpOneLevel) {
			link.append("..");
			link.append(File.separator);
		}
		
		link.append(ARCHIVES_DIR);
		link.append(File.separator);
		link.append(getPageFileName(entry));
		link.append(HTML_EXT);
		
		return link.toString();
	}
	
	public String getLinkTo(Entry entry) {
		StringBuffer link = new StringBuffer();
		
		link.append(getLinkToPage(entry));
		link.append(HASH);
		link.append(entry.getId());
		
		return link.toString();
	}
	
	public String getRSSLink() {
		StringBuffer link = new StringBuffer();
		
		link.append(System.getProperty("diary.root"));

		link.append(RSS_DIR);
		link.append(File.separator);
		link.append(System.getProperty("rss.url"));

		IMG img = new IMG(System.getProperty("rss.image.url"));
		img.setHeight(System.getProperty("rss.image.height"));
		img.setWidth(System.getProperty("rss.image.width"));
		img.setAlt(System.getProperty("rss.image.alt"));
		
		return new A(link.toString(), img.toString()).toString(); // rss feed URL
	}

	public Link getAlternateRSSLink() {
		Link result = new Link();
		result.setRel("alternate");
		result.setType("application/rss+xml");
		result.setTitle("RSS");
		
		StringBuffer sb = new StringBuffer();
		sb.append(System.getProperty("diary.root"));
		sb.append(RSS_DIR);
		sb.append(File.separator);
		sb.append(System.getProperty("rss.url"));

		result.setHref(sb.toString());
		
		return result;
	}

	public String getStyleSheetLink() {
		StringBuffer url = new StringBuffer();
		if (fUpOneLevel) {
			url.append("..");
			url.append(File.separator);
		}
		url.append(STYLES_DIR);
		url.append(File.separator);
		url.append(System.getProperty("web.layout.css"));
		return url.toString();
	}

	public Link getLinkToStyleSheet() {
		Link result = new Link();
		result.setRel("stylesheet");
		result.setType("text/css");
		result.setHref(getStyleSheetLink());
		return result;
	}
}
