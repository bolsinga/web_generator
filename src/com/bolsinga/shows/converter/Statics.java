package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import com.bolsinga.shows.converter.*;

public class Statics {
    public String fLocation;
    public String fData;
        
    public Statics(String location, String data) {
        fLocation = location;
        fData = data;
    }
        
    public String getLocation() {
        return fLocation;
    }
        
    public void setLocation(String location) {
        fLocation = location;
    }
        
    public String getData() {
        return fData;
    }
        
    public void setData(String data) {
        fData = data;
    }
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
                
        sb.append(getClass().getName().toString());
        sb.append(" Location: ");
        sb.append(getLocation());
        sb.append(" Data: ");
        sb.append(getData());
                
        return sb.toString();
    }
}
