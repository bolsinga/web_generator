package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

public class BandMap {
	private String fName;
	private String fSortName;
	
	public BandMap(String name, String sortName) {
		fName = name;
		fSortName = sortName;
	}
	
	public String getName() {
		return fName;
	}
	
	public void setName(String name) {
		fName = name;
	}
	
	public String getSortName() {
		return fSortName;
	}
	
	public void setSortName(String sortName) {
		fSortName = sortName;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getClass().getName().toString());
		sb.append(" ");
		sb.append(getName());
		sb.append(" -> ");
		sb.append(getSortName());
		
		return sb.toString();
	}
}
