package com.bolsinga.shows.converter;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bolsinga.diary.data.*;

public class Diary {

        public static void main(String[] args) {
                if (args.length != 3) {
                        System.out.println("Usage: Diary [comments] [statics] [output]");
                        System.exit(0);
                }
                
                Diary.convert(args[0], args[1], args[2]);
        }
        
        public static void convert(String commentsFile, String staticsFile, String outputFile) {
                List comments = null;
                List statics = null;
                
                try {
                        comments = Convert.comments(commentsFile);
                        
                        statics = Convert.statics(staticsFile);
                } catch (IOException e) {
                        System.err.println(e);
                        System.exit(1);
                }

                ObjectFactory objFactory = new ObjectFactory();
                
                try {
                        com.bolsinga.diary.data.Diary diary = objFactory.createDiary();
                        
                        createStatics(objFactory, diary, statics);
                        
                        createComments(objFactory, diary, comments);
                        
                        diary.setTitle(System.getProperty("diary.title"));
                        
                        // Write out to the output file.
                        JAXBContext jc = JAXBContext.newInstance("com.bolsinga.diary.data");
                        Marshaller m = jc.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                        
                        OutputStream os = null;
                        try {
                                os = new FileOutputStream(outputFile);
                        } catch (IOException ioe) {
                                System.err.println(ioe);
                                ioe.printStackTrace();
                                System.exit(1);
                        }
                        m.marshal(diary, os);
                        
                } catch (JAXBException e) {
                        System.err.println(e);
                        e.printStackTrace();
                        System.exit(1);
                }
        }
        
        private static void createStatics(ObjectFactory objFactory, com.bolsinga.diary.data.Diary diary, List statics) throws JAXBException {
                Statics oldStatic = null;
                com.bolsinga.diary.data.Static xStatic = null;
                
                ListIterator li = statics.listIterator();
                while (li.hasNext()) {
                        oldStatic = (Statics)li.next();
                        
                        xStatic = objFactory.createStatic();
                        
                        if (oldStatic.getLocation().equalsIgnoreCase("left")) {
                                diary.setStatic(oldStatic.getData());
                        } else if (oldStatic.getLocation().equalsIgnoreCase("header")) {
                                diary.setHeader(oldStatic.getData());
                        } else if (oldStatic.getLocation().equalsIgnoreCase("links")) {
                                diary.setFriends(oldStatic.getData());
                        }
                }
        }

        private static void createComments(ObjectFactory objFactory, com.bolsinga.diary.data.Diary diary, List comments) throws JAXBException {
                Comments oldComment = null;
                com.bolsinga.diary.data.Entry xEntry = null;
                int index = comments.size() - 1;
                
                ListIterator li = comments.listIterator();
                while (li.hasNext()) {
                        oldComment = (Comments)li.next();
                        
                        xEntry = objFactory.createEntry();
                        xEntry.setTimestamp(Diary.createTimestamp(oldComment.getDate()));
                        xEntry.setComment(oldComment.getData());
                        xEntry.setId("e" + index--);
                        
                        diary.getEntry().add(xEntry);
                }
        }
        
        private static Calendar createTimestamp(String date) {
                Calendar result = Calendar.getInstance();
                
                String monthString, dayString, yearString = null;
                int month, day, year = 0;
                
                StringTokenizer st = new StringTokenizer(date, "-");
                
                monthString = st.nextToken();
                dayString = st.nextToken();
                yearString = st.nextToken();
                
                month = Integer.parseInt(monthString);
                day = Integer.parseInt(dayString);
                year = Integer.parseInt(yearString);
                
                result.set(year, month - 1, day);
                
                return result;
        }
}
