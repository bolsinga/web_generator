package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

public class Relation {

    private String fType;
    private String fReason;
    private List fMembers;
        
    public Relation(String type, String reason) {
	fType = type;
	fReason = reason;
	fMembers = new Vector();
    }
        
    public Relation(String type, String reason, List members) {
	fType = type;
	fReason = reason;
	fMembers = members;
    }
        
    public String getType() {
	return fType;
    }
        
    public void setType(String type) {
	fType = type;
    }
        
    public String getReason() {
	return fReason;
    }
        
    public void setReason(String reason) {
	fReason = reason;
    }
        
    public void addMember(String member) {
	fMembers.add(member);
    }
        
    public List getMembers() {
	return fMembers;
    }
        
    public String toString() {
	StringBuffer sb = new StringBuffer();
                
	sb.append(getClass().getName().toString());
	sb.append(" [");
	sb.append(getType());
	sb.append(", ");
	sb.append(getReason());
	sb.append(" (");
                
	ListIterator li = getMembers().listIterator();
	while (li.hasNext()) {
	    sb.append((String)(li.next()));
	    sb.append(", ");
	}
                
	sb.append(")]");
                
	return sb.toString();
    }
}
