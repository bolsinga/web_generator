package com.bolsinga.diary.util;

import com.bolsinga.diary.data.*;

import java.io.*;
import java.text.*;
import java.util.*;

public class Links {

	public static final String HTML_EXT = ".html";
	public static final String ARCHIVES_DIR = "archives";
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
}
