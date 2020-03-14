package com.bolsinga.test;

import com.bolsinga.ical.*;
import com.bolsinga.music.data.*;
import com.bolsinga.settings.data.*;

import com.bolsinga.music.*;
import com.bolsinga.web.*;

import java.io.*;

/*
 * http://www.imc.org/pdi/vcal-10.txt
 */

public class ICalTest {

  public static void main(String[] args) {
    if ((args.length != 4) && (args.length != 5)) {
      ICalTest.usage();
    }

    String type = args[0];

    String settings = null;
    String output = null;

    Music music = null;

    try {
      if (type.equals("json")) {
        if (args.length != 4) {
          ICalTest.usage();
        }
        
        String musicFile = args[1];
        settings = args[2];
        output = args[3];

        music = com.bolsinga.music.data.json.Music.create(musicFile);
      } else {
        ICalTest.usage();
      }

      Util.createSettings(settings);
        
      ICal.generate(music, output);
    } catch (WebException e) {
      System.err.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void usage() {
    System.out.println("Usage: ICalTest json [source.json] [settings.xml] [output.dir]");
    System.exit(0);
  }
}
