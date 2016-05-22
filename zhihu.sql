/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : zhihu

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2016-06-01 11:28:50
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `detail`
-- ----------------------------
DROP TABLE IF EXISTS `detail`;
CREATE TABLE `detail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `href` varchar(255) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `weibo` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `bussiness` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `employment` varchar(255) DEFAULT NULL,
  `position` varchar(255) DEFAULT NULL,
  `education` varchar(255) DEFAULT NULL,
  `eduextra` varchar(255) DEFAULT NULL,
  `description` varchar(3000) DEFAULT NULL,
  `asks` int(10) unsigned zerofill DEFAULT NULL,
  `answers` int(10) unsigned zerofill DEFAULT NULL,
  `posts` int(10) unsigned zerofill DEFAULT NULL,
  `collections` int(10) unsigned zerofill DEFAULT NULL,
  `vote` int(10) unsigned zerofill DEFAULT NULL,
  `thank` int(10) unsigned zerofill DEFAULT NULL,
  `fav` int(10) unsigned zerofill DEFAULT NULL,
  `shares` int(10) unsigned zerofill DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21001 DEFAULT CHARSET=utf8;