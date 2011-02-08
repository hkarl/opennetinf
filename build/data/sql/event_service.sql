-- phpMyAdmin SQL Dump
-- version 3.1.2deb1ubuntu0.2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 09, 2009 at 07:46 PM
-- Server version: 5.0.75
-- PHP Version: 5.2.6-3ubuntu4.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


-- first create the users
GRANT USAGE ON *.* TO 'augnetuser'@'%'; -- workaround since DROP USER IF EXISTS is not supported by mysql
DROP USER 'augnetuser'@'%';
CREATE USER 'augnetuser'@'%' IDENTIFIED BY 'augnetpass';
GRANT ALL ON *.* TO 'augnetuser'@'%';

--
-- Database: `esf`
--
DROP DATABASE IF EXISTS `esf`;
CREATE DATABASE `esf` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `esf`;

-- --------------------------------------------------------

--
-- Table structure for table `ec_po_mapping`
--

DROP TABLE IF EXISTS `ec_po_mapping`;
CREATE TABLE IF NOT EXISTS `ec_po_mapping` (
  `event_container_id` varchar(255) character set utf8 NOT NULL,
  `person_object_id` varchar(255) character set utf8 NOT NULL,
  PRIMARY KEY  (`event_container_id`),
  FULLTEXT KEY `event_container_id` (`event_container_id`),
  FULLTEXT KEY `event_container_id_2` (`event_container_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ec_po_mapping`
--


-- --------------------------------------------------------

--
-- Table structure for table `ec_sub_mapping`
--

DROP TABLE IF EXISTS `ec_sub_mapping`;
CREATE TABLE IF NOT EXISTS `ec_sub_mapping` (
  `event_container_id` varchar(255) character set utf8 NOT NULL,
  `sparql_subscription` varchar(510) character set utf8 NOT NULL,
  `subscription_identification` varchar(255) character set utf8 NOT NULL,
  `expiration_date` bigint(20) NOT NULL,
  KEY `event_container_id` (`event_container_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ec_sub_mapping`
--

--
-- Database: `esf_testing`
--
DROP DATABASE IF EXISTS `esf_testing`;
CREATE DATABASE `esf_testing` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `esf_testing`;

-- --------------------------------------------------------

--
-- Table structure for table `ec_po_mapping`
--

DROP TABLE IF EXISTS `ec_po_mapping`;
CREATE TABLE IF NOT EXISTS `ec_po_mapping` (
  `event_container_id` varchar(255) character set utf8 NOT NULL,
  `person_object_id` varchar(255) character set utf8 NOT NULL,
  PRIMARY KEY  (`event_container_id`),
  FULLTEXT KEY `event_container_id` (`event_container_id`),
  FULLTEXT KEY `event_container_id_2` (`event_container_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ec_po_mapping`
--


-- --------------------------------------------------------

--
-- Table structure for table `ec_sub_mapping`
--

DROP TABLE IF EXISTS `ec_sub_mapping`;
CREATE TABLE IF NOT EXISTS `ec_sub_mapping` (
  `event_container_id` varchar(255) character set utf8 NOT NULL,
  `sparql_subscription` varchar(510) character set utf8 NOT NULL,
  `subscription_identification` varchar(255) character set utf8 NOT NULL,
  `expiration_date` bigint(20) NOT NULL,
  KEY `event_container_id` (`event_container_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ec_sub_mapping`
--


