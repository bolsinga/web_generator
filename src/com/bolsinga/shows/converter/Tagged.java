package com.bolsinga.shows.converter;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Tagged {

	private Matcher fMatcher;
	
	public static Tagged fromFile(String filename) throws IOException {
		return new Tagged(new File(filename));
	}
	
	public static Tagged fromString(String s) {
		return new Tagged(s);
	}
	
	private Tagged(File f) throws IOException {
		FileChannel fc = new FileInputStream(f).getChannel();
		ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		CharBuffer cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
		
		init(cb);
	}
	
	private Tagged(String s) {
		init(s);
	}
	
	private void init(CharSequence cs) {
		Pattern p = Pattern.compile("<(\\S+?).*?>(.*?)</\\1>", Pattern.DOTALL);
		fMatcher = p.matcher(cs);
	}
	
	public boolean hasMoreElements() {
		return fMatcher.find();
	}
	
	public String tag() {
		return fMatcher.group(1);
	}
	
	public String data() {
		return fMatcher.group(2);	
	}
}
