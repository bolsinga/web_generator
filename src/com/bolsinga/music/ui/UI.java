package com.bolsinga.music.ui;

import com.bolsinga.music.data.*;
import com.bolsinga.music.util.*;

import java.awt.*;
import java.io.*;

public class UI {

  private static UI sUI = null;
        
  public static void main(String[] args) {
    String file = null;
                
    if (args.length == 1) {
      file = args[0];
    }
                
    UI.getUI().edit(file);
  }
        
  public synchronized static UI getUI() {
    if (sUI == null) {
      sUI = new UI();
    }
    return sUI;
  }

  private UI() {
    Frame f = new Frame("Edit");
    f.setVisible(true);
  }
                
  public void edit(String sourceFile) {
    Music music = Util.createMusic(sourceFile);
    edit(music);
  }
        
  public void edit(Music music) {
        
  }
}
