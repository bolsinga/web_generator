-- MySQL dump 10.9
--
-- Host: localhost    Database: music
-- ------------------------------------------------------
-- Server version	4.1.18-standard

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `music`
--

/*!40000 DROP DATABASE IF EXISTS `music`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `music` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `music`;

--
-- Table structure for table `album`
--

DROP TABLE IF EXISTS `album`;
CREATE TABLE `album` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `title` text NOT NULL,
  `label_id` int(10) unsigned default NULL,
  `comment` text,
  `compilation` tinyint(1) default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `artist`
--

DROP TABLE IF EXISTS `artist`;
CREATE TABLE `artist` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `sortname` varchar(255) default '',
  `location_id` int(10) unsigned default '0',
  `comment` text,
  `active` tinyint(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `label`
--

DROP TABLE IF EXISTS `label`;
CREATE TABLE `label` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `location_id` int(10) unsigned default '0',
  `comment` text,
  `active` tinyint(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `street` varchar(255) default '',
  `city` varchar(255) default '',
  `state` varchar(2) default NULL,
  `zip` smallint(5) unsigned default '0',
  `url` text,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `performance`
--

DROP TABLE IF EXISTS `performance`;
CREATE TABLE `performance` (
  `id` int(10) unsigned NOT NULL default '0',
  `artist_id` int(10) unsigned NOT NULL default '0',
  `playorder` tinyint(3) unsigned NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `relation`
--

DROP TABLE IF EXISTS `relation`;
CREATE TABLE `relation` (
  `id` int(10) unsigned NOT NULL default '0',
  `related_id` int(10) unsigned NOT NULL default '0',
  `type` enum('artist','venue') NOT NULL default 'artist',
  `reason` varchar(255) default ''
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `shows`
--

DROP TABLE IF EXISTS `shows`;
CREATE TABLE `shows` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `date` date NOT NULL default '0000-00-00',
  `venue_id` int(10) unsigned NOT NULL default '0',
  `comment` text,
  `performance_id` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `song`
--

DROP TABLE IF EXISTS `song`;
CREATE TABLE `song` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `title` text NOT NULL,
  `performer_id` int(10) unsigned NOT NULL default '0',
  `composer_id` int(10) unsigned default '0',
  `producer_id` int(10) unsigned default '0',
  `release` date default '0000-00-00',
  `purchase` date default '0000-00-00',
  `genre` varchar(255) default '',
  `track` tinyint(3) unsigned default '0',
  `last_played` datetime default NULL,
  `live` tinyint(1) default '0',
  `album_id` int(10) unsigned default '0',
  `format` set('12 Inch LP','12 Inch EP','12 Inch Single','10 Inch EP','7 Inch Single','Cassette','CD','Digital File') NOT NULL default 'Digital File',
  `playcount` int(10) unsigned default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `venue`
--

DROP TABLE IF EXISTS `venue`;
CREATE TABLE `venue` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `location_id` int(10) unsigned default '0',
  `comment` text,
  `active` tinyint(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

