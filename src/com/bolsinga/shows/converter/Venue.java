package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

public class Venue {
	private String fName;
	private String fCity;
	private String fState;
	private String fAddress;
	private String fURL;
	
	public Venue(String name, String city, String state, String address, String url) {
		fName = name;
		fCity = city;
		fState = state;
		fAddress = address;
		fURL = url;
	}
	
	public Venue(String name, String city, String state) {
		fName = name;
		fCity = city;
		fState = state;
	}
	
	public String getName() {
		return fName;
	}
	
	public void setName(String name) {
		fName = name;
	}
	
	public String getCity() {
		return fCity;
	}
	
	public void setCity(String city) {
		fCity = city;
	}
	
	public String getState() {
		return fState;
	}
	
	public void setState(String state) {
		fState = state;
	}
	
	public String getAddress() {
		return fAddress;
	}
	
	public void setAddress(String address) {
		fAddress = address;
	}
	
	public String getURL() {
		return fURL;
	}
	
	public void setURL(String url) {
		fURL = url;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getClass().getName().toString());
		sb.append(" Name: ");
		sb.append(getName());
		sb.append(" City: ");
		sb.append(getCity());
		sb.append(" State: ");
		sb.append(getState());
		sb.append(" Address: ");
		sb.append(getAddress());
		sb.append(" URL: ");
		sb.append(getURL());
		
		return sb.toString();
	}
}
