package com.bolsinga.plist.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {
  public static com.bolsinga.plist.data.Plist createPlist(String sourceFile) {
    com.bolsinga.plist.data.Plist plist = null;
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.plist.data");
      Unmarshaller u = jc.createUnmarshaller();
            
      plist = (com.bolsinga.plist.data.Plist)u.unmarshal(new java.io.FileInputStream(sourceFile));
    } catch (Exception ume) {
      System.err.println("Exception: " + ume);
      ume.printStackTrace();
      System.exit(1);
    }
    return plist;
  }
}
