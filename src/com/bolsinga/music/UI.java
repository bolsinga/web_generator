package com.bolsinga.music.ui;

import com.bolsinga.music.data.*;

import java.awt.*;
import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class UI {

	private static UI sUI = null;
	
	public static void main(String[] args) {
		String file = null;
		
		if (args.length == 1) {
			file = args[0];
		}
		
		UI.getUI().edit(file);
	}
	
	public synchronized static UI getUI() {
		if (sUI == null) {
			sUI = new UI();
		}
		return sUI;
	}

	private UI() {
		Frame f = new Frame("Edit");
		f.setVisible(true);
		
		// Location editor Panel
		// Multiple fields
		
		// Date editor Panel
		// Multiple fields
		
		// Venue editor Panel
		//  List of Venues
		//   Add Button
		//   Edit Button
		//  Name
		//  Location Panel
		
		// Artist editor Panel
		//  List of Artists
		//   Add Button
		//   Edit Button
		//  Name
		//  Sorted Name
		//  Location Panel
		
		// Show editor Panel
		//  List of Dates
		//   Add Button
		//   Edit Button
		//  Venue Name
		//   Edit Button
		//  Comment
		
		// Relation editor Panel
		// List of Types
		//  Add Button
		//  Edit Button
		// List of Members
		//  Add Button
		//  Edit Button
	}
	
	private static Music createMusic(String sourceFile) {
		Music music = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.bolsinga.music.data");
			Unmarshaller u = jc.createUnmarshaller();
			
			music = (Music)u.unmarshal(new FileInputStream(sourceFile));
		} catch (Exception ume) {
			System.err.println("Exception: " + ume);
			ume.printStackTrace();
			System.exit(1);
		}
		return music;
	}
	
	public void edit(String sourceFile) {
		Music music = createMusic(sourceFile);
		edit(music);
	}
	
	public void edit(Music music) {
	
	}
}
