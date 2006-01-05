package com.bolsinga.music;

import com.bolsinga.music.data.*;

public class ICalDB {
  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: ICalDB [user] [password] [settings.xml] [output.dir]");
      System.exit(0);
    }

    com.bolsinga.web.Util.createSettings(args[2]);
                
    ICalDB.generate(args[0], args[1], args[3]);
  }

  public static void generate(String user, String password, String outputDir) {
    Music music = com.bolsinga.music.Util.createMusic(user, password);

    ICal.generate(music, outputDir);
  }
}
