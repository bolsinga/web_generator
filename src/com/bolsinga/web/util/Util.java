package com.bolsinga.web.util;

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import org.apache.ecs.filter.*;

public class Util {
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
}
