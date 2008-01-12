package com.bolsinga.diary.data.xml;

import java.io.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;

public class Diary implements com.bolsinga.diary.data.Diary {
  private final com.bolsinga.diary.data.xml.impl.Diary fDiary;
  private final List<com.bolsinga.diary.data.Entry> fEntries;
  
  public static Diary create(final String sourceFile) throws com.bolsinga.web.WebException {
    com.bolsinga.diary.data.xml.impl.Diary diary = null;
    
    InputStream is = null;
    try {
      try {
        is = new FileInputStream(sourceFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find diary file: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      try {
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data.xml.impl");
        Unmarshaller u = jc.createUnmarshaller();
                          
        diary = (com.bolsinga.diary.data.xml.impl.Diary)u.unmarshal(is);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't unmarsal diary file: ");
        sb.append(sourceFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close diary file: ");
          sb.append(sourceFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
    
    return new Diary(diary);
  }
  
  public static void export(final com.bolsinga.diary.data.Diary diary, final String outputFile) throws com.bolsinga.web.WebException {
    com.bolsinga.diary.data.xml.impl.Diary xmlDiary = null;
    if (diary instanceof com.bolsinga.diary.data.xml.Diary) {
      xmlDiary = ((com.bolsinga.diary.data.xml.Diary)diary).fDiary;
    } else {
      com.bolsinga.diary.data.xml.impl.ObjectFactory objFactory = new com.bolsinga.diary.data.xml.impl.ObjectFactory();
      
      xmlDiary = objFactory.createDiary();
      xmlDiary.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(diary.getTimestamp()));
      
      xmlDiary.setStatic(diary.getStatic());
      xmlDiary.setHeader(diary.getHeader());
      xmlDiary.setFriends(diary.getFriends());
      xmlDiary.setTitle(diary.getTitle());
      xmlDiary.setColophon(diary.getColophon());

      List<com.bolsinga.diary.data.xml.impl.Entry> entries = xmlDiary.getEntry(); // Modification required.
      com.bolsinga.diary.data.xml.impl.Entry xmlEntry = null;
      
      for (com.bolsinga.diary.data.Entry entry : diary.getEntries()) {
        xmlEntry = objFactory.createEntry();
        xmlEntry.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(entry.getTimestamp()));
        xmlEntry.setComment(entry.getComment());
        xmlEntry.setId(entry.getID());
                          
        entries.add(xmlEntry);
      }
    }
    
    Diary.export(xmlDiary, outputFile);
  }
  
  private static void export(final com.bolsinga.diary.data.xml.impl.Diary diary, final String outputFile) throws com.bolsinga.web.WebException {
    OutputStream os = null;
    try {
      try {
        os = new FileOutputStream(outputFile);
      } catch (FileNotFoundException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find file: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
      
      try {
        // Write out to the output file.
        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data.xml.impl");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(diary, os);
      } catch (JAXBException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't marshall: ");
        sb.append(outputFile);
        throw new com.bolsinga.web.WebException(sb.toString(), e);
      }
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          StringBuilder sb = new StringBuilder();
          sb.append("Unable to close: ");
          sb.append(outputFile);
          throw new com.bolsinga.web.WebException(sb.toString(), e);
        }
      }
    }
  }
  
  private Diary(final com.bolsinga.diary.data.xml.impl.Diary diary) {
    fDiary = diary;
    fEntries = new ArrayList<com.bolsinga.diary.data.Entry>(diary.getEntry().size());
    for (com.bolsinga.diary.data.xml.impl.Entry entry : diary.getEntry()) {
      fEntries.add(Entry.create(entry));
    }
  }
  
  public GregorianCalendar getTimestamp() {
    return fDiary.getTimestamp().toGregorianCalendar();
  }
  
  public void setTimestamp(final GregorianCalendar timestamp) {
    fDiary.setTimestamp(com.bolsinga.web.Util.toXMLGregorianCalendar(timestamp));
  }
  
  public String getTitle() {
    return fDiary.getTitle();
  }
  
  public void setTitle(final String title) {
    fDiary.setTitle(title);
  }
  
  public String getStatic() {
    return fDiary.getStatic();
  }
  
  public void setStatic(final String staticData) {
    fDiary.setStatic(staticData);
  }
  
  public String getHeader() {
    return fDiary.getHeader();
  }
  
  public void setHeader(final String header) {
    fDiary.setHeader(header);
  }
  
  public String getFriends() {
    return fDiary.getFriends();
  }
  
  public void setFriends(final String friends) {
    fDiary.setFriends(friends);
  }
  
  public String getColophon() {
    return fDiary.getColophon();
  }
  
  public void setColophon(final String colophon) {
    fDiary.setColophon(colophon);
  }
  
  public List<? extends com.bolsinga.diary.data.Entry> getEntries() {
    return Collections.unmodifiableList(fEntries);
  }

  public List<? extends com.bolsinga.diary.data.Entry> getEntriesCopy() {
    return new ArrayList<com.bolsinga.diary.data.Entry>(fEntries);
  }
}
