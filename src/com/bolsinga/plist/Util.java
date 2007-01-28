package com.bolsinga.plist;

import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Util {
  public static com.bolsinga.plist.data.Plist createPlist(final String sourceFile) throws PlistException {
    com.bolsinga.plist.data.Plist plist = null;
    InputStream is = null;
    try {
      is = new FileInputStream(sourceFile);
    } catch (FileNotFoundException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't find file: ");
      sb.append(sourceFile);
      throw new PlistException(sb.toString(), e);
    }
    
    try {
      JAXBContext jc = JAXBContext.newInstance("com.bolsinga.plist.data");
      Unmarshaller u = jc.createUnmarshaller();
            
      plist = (com.bolsinga.plist.data.Plist)u.unmarshal(is);
    } catch (JAXBException e) {
      StringBuilder sb = new StringBuilder();
      sb.append("Can't unmarshall: ");
      sb.append(is.toString());
      throw new PlistException(sb.toString(), e);
    }
    return plist;
  }
}
