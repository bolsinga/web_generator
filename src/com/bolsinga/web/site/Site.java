package com.bolsinga.web.site;

import com.bolsinga.music.data.*;
import com.bolsinga.diary.data.*;

public class Site {
    public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println("Usage: Web [diary.xml] [music.xml] [settings.xml] [output.dir] <all|music|diary>");
			System.exit(0);
		}

        com.bolsinga.web.util.Util.createSettings(args[2]);
		
		Site.generate(args[0], args[1], args[3], args[4]);
    }
    
    public static void generate(String diaryFile, String musicFile, String outputDir, String variant) {
        Diary diary = com.bolsinga.diary.util.Util.createDiary(diaryFile);
        Music music = com.bolsinga.music.util.Util.createMusic(musicFile);
        
        if (!variant.equals("music")) {
            com.bolsinga.diary.web.Web.generate(diary, music, outputDir);
        }
        
        if (!variant.equals("diary")) {
            com.bolsinga.music.web.Web.generate(music, outputDir);
            com.bolsinga.music.ical.ICal.generate(music, outputDir);
        }
        
        com.bolsinga.rss.RSS.generate(diary, music, outputDir);
    }
}
