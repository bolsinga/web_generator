package com.bolsinga.web.util;

import org.apache.ecs.*;
import org.apache.ecs.xhtml.*;
import org.apache.ecs.filter.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {

	private static boolean sPrettyPrint = false;
	static {
		String value = System.getProperty("web.pretty_containers");
		if (value != null) {
			sPrettyPrint = true;
		}
	}
	
	public static boolean getPrettyPrint() {
		return sPrettyPrint;
	}
	
	public static link getIconLink() {
		link result = new link();
		result.setRel("SHORTCUT ICON");
		result.setHref(System.getProperty("web.ico"));
		return result;
	}
	
	public static img getLogo() {
		img i = new img(System.getProperty("web.logo.url"));
		i.setHeight(System.getProperty("web.logo.height"));
		i.setWidth(System.getProperty("web.logo.width"));
		i.setAlt(System.getProperty("web.logo.alt"));
		return i;
	}
	
	public static ul convertToUnOrderedList(String data) {
		ul list = new ul();
		
		// Convert each line to a li tag.
		String[] lines = data.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			list.addElement(new li(lines[i]));
		}
		
		return list;
	}
	
	public static String convertToParagraphs(String data) {
		// Convert each line to <p> tags
		StringBuffer tagged = new StringBuffer();
		String[] lines = data.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			tagged.append(new p().addElement(lines[i]));
		}
		return tagged.toString();
	}
	
	public static div createDiv(String className) {
		div d = new div();
		d.setClass(className);
		d.setPrettyPrint(Util.getPrettyPrint());
		return d;
	}
	
	public static a createInternalA(String url, String value) {
		return Util.createInternalA(url, value, null);
	}
	
	public static a createInternalA(String url, String value, String title) {
		a an = new a(url, value);
		an.setClass(CSS.INTERNAL);
		if (title != null) {
			an.setTitle(title);
		}
		return an;
	}

	public static com.bolsinga.settings.data.Settings createSettings(String sourceFile) {
		com.bolsinga.settings.data.Settings settings = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.settings.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			settings = (com.bolsinga.settings.data.Settings)u.unmarshal(new java.io.FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		return settings;
	}
}
