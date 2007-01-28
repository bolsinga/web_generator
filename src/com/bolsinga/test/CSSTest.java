package com.bolsinga.test;

import com.bolsinga.web.*;

public class CSSTest {
                                                       
  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: CSSTest [settings.xml] [layout.css] [output.dir]");
      System.exit(0);
    }

    try {
      Util.createSettings(args[0]);
    
      CSS.install(args[1], args[2]);
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
