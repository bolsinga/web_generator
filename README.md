# Web Generator

## History
This java program generates [bolsinga.com](http://www.bolsinga.com/). It is a collection of code I've been working on starting sometime in 1997 or so.

It all started because I saved the tickets from shows I'd gone to. I still have them all. Eventually I also started writing down the shows I saw during college, many of which didn't have tickets to actually save. I tracked each show on lined notebook paper, as well as in my University of Illinois i-Book. This was a daily planner for students.

At some point in the mid 1990s I started entering this information into a text file. Not long thereafter, I realized I could write a program to read that file and have it tell me what band I had seen most often. It's not quite clear to me when I started the program. Its `git` history starts in 2002, which only tracks when the `cvs2svn` history was imported into `git`. However the site's [first entry](http://www.bolsinga.com/archives/1998.html#e0) is from 9/6/1998. That is when I was living in Austin, TX, working for Metrowerks on java tools. Therefore I'd guess the coding started sometime in 1997.

It was a program originally written using Metrowerks Codewarrior C++. At the time I enjoyed C++. I read  both the 2nd and 3rd editions of Bjarne Stroustrup's "The C++ Programming Language" cover to cover. Eventually due to the circumstances (read the industry insider [details](http://www.bolsinga.com/archives/2005.html#e171)), I migrated this code to use Apple's Project Builder and gcc, also using C++. I still have this source code around, but it is no longer used.

In about 2003 I started a rewrite of the program in java. It provided a way for me to code in what seemed the simplest way to get this type of program done at the time. I really wanted to use XML! I wanted to use threads. I wanted a library to create the HTML code. Java had support for these built-in (or at least downloadable), and C++ (at the time) did not. The site generated by the new java program finally debuted in May 2003.

I did much of this work while riding CalTrain, especially from 2004 to 2006 while commuting to Azul in Mountain View. At Azul, I used my personal computer for work. I really wanted to use a Mac there, so I was bringing it along with me every day. There was no tethered Internet then, so I'd code while unconnected. Otherwise it was work done in the evenings at home. I'd always had this feeling that I needed code of my own that I could show someone. That didn't become reality until March 2020 when I pushed it to github.

## Parts

### Diary
A Diary consists of Entries. Entries have a timestamp, an optional title, and a comment. It supports new lines, and no other markup.

### Shows
A Show consists of a list of artists, listed in headliner to opener order. It has a date, which may have any portion (month, day, or year) denoted as unknown. It has a venue name. It also has an optional comment about the show.

Venues have additional information, such as the URL and the address. This allows the site to be generated to calcuate how many shows have been seen in each city.

### iTunes
iTunes data is obtained from the iTunes program running on a mac. This data is used to add to the pages for artists. It will have all of their albums with release dates and song listings.

## Details

All items can contain external links or internal links. The internal links can be hand built with a `@@ROOT_URL@@` root. <del>There is also an encoder that runs across all the artist names and venue names. It will look at all the text and automatically generate links to a matching band or venue.</del>

Artists and venues can be related to each other, for any reason. Artists are sorted using iTunes sorting rules, and there is an additional data file for sorting artists not in iTunes (for artists only seen at shows).

The file formats for all of these data inputs are arbitrary and different for each file type. It basically depended upon was found to be simplest (or most interesting to try) at the time it was written.

The program is multi-threaded using java concurrency queues. It looks like I first released the [concurrent build](http://www.bolsinga.com/archives/2006.html#e246) in mid-2006. This is when I was working at Azul. At the time they built expensive computers with 384 cores to run java programs. Interestingly, we found they were mostly used to parse XML. I guess I was influenced by what I was working on then.

It has CSS to style it. It's clear my style isn't very modern. <del>At some point, I'd decided to *save some space*, and mangle the CSS names to single letters. It makes things smaller and harder to read, but my 2020 hunch is that it is a pre-mature optimization.</del>

## Saga

It never really used XML as part of its core, but depended upon it for its implementation for far too long. It would read the various files, and convert them to two separate XML files using JAXB. One was for the diary, and the other for the music. Then it would re-read these XML files and then create the HTML web site.

For awhile I was determined to use more arcane technology (JDBC and MySQL) to back the site. I'd done some work on this and then backed it all out in 2007, yet not actually removed until 2020.

In [late 2007](https://github.com/bolsinga/web_generator/commit/1da36f0005999f4be473841238b85bd7aa019a2a), I started to make the program not depend upon what created the data it was converting to HTML. This data abstraction allowed the program to be quicker, as it no longer would need to convert to and then from XML to create the site. It also allowed me to add a json export, added in early 2008, which still isn't quite used for anything in particular in 2020.

Updating JDK versions has been surprisingly frustrating. One of the things that led me to java was JAXB. This was an _automatic_ way to convert XML data to code models using a XML Schema file. It was a separate library from the regular JDK. Then it was integrated, and then removed. It was always a pain point. I'd finally [removed the JAXB requirement](https://github.com/bolsinga/web_generator/pull/11) from the program in May, 2020.

## Future

The program can output complete JSON for the diary and the music. JSON can be used as input to the program when creating the HTML. This will be useful for retiring the ancient and multiple file formats for the programs data.

JSON is the simple low friction way all sorts of languages can communicate data models. My long-term plan is to have a new Swift program manage the data, with a full editing interface. Then it can just output JSON which this java program can translate into HTML.

A clever way to accomplish this may be to have web_generator output the json from the various file formats without the iTunes data. Currently the program requires an iTunes file, so that will be the next step.
