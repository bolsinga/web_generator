package com.bolsinga.web.util;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

class CSS {
	public static final String INTERNAL	= "internal";
}

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
	
	public static Link getIconLink() {
		Link result = new Link();
		result.setRel("SHORTCUT ICON");
		result.setHref(System.getProperty("web.ico"));
		return result;
	}
	
	public static IMG getLogo() {
		IMG img = new IMG(System.getProperty("web.logo.url"));
		img.setHeight(System.getProperty("web.logo.height"));
		img.setWidth(System.getProperty("web.logo.width"));
		img.setAlt(System.getProperty("web.logo.alt"));
		return img;
	}
	
	public static String convertToParagraphs(String data) {
		// Convert each line to <p> tags
		StringBuffer tagged = new StringBuffer();
		String[] lines = data.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			tagged.append(new P());
			tagged.append(lines[i]);
		}
		return tagged.toString();
	}
	
	public static Div createDiv(String className) {
		Div div = new Div();
		div.setClass(className);
		div.setPrettyPrint(Util.getPrettyPrint());
		return div;
	}
	
	public static A createInternalA(String url, String value) {
		A a = new A(url, value);
		a.setClass(CSS.INTERNAL);
		return a;
	}
}
