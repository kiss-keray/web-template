/*
Navicat MySQL Data Transfer

Source Server         : mysql
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : ticketsystem

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-05-18 20:09:29
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for member
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键（自增）',
  `createDate` datetime NOT NULL COMMENT '创建日期',
  `updateDate` datetime NOT NULL COMMENT '更新日期',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(32) NOT NULL COMMENT '密码',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `age` int(11) DEFAULT '0' COMMENT '年龄',
  `sex` bit(1) DEFAULT b'0' COMMENT '性别',
  `balance` decimal(10,0) DEFAULT '0' COMMENT '余额',
  `img` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `phone` varchar(11) DEFAULT NULL COMMENT '电话',
  `role` int(11) DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_index` (`username`) USING HASH,
  KEY `fk_user_role` (`role`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role`) REFERENCES `role` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键（自增）',
  `createDate` datetime NOT NULL COMMENT '创建日期',
  `updateDate` datetime NOT NULL COMMENT '更新日期',
  `name` varchar(255) NOT NULL COMMENT '角色名',
  `value` varchar(255) DEFAULT NULL COMMENT '角色值',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name` (`name`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for role_roleinterface
-- ----------------------------
DROP TABLE IF EXISTS `role_roleinterface`;
CREATE TABLE `role_roleinterface` (
  `role` int(11) NOT NULL COMMENT '角色id',
  `interface` int(11) NOT NULL COMMENT '接口id',
  PRIMARY KEY (`role`,`interface`),
  KEY `fk_interface` (`interface`),
  CONSTRAINT `fk_interface` FOREIGN KEY (`interface`) REFERENCES `roleinterface` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role` FOREIGN KEY (`role`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for roleinterface
-- ----------------------------
DROP TABLE IF EXISTS `roleinterface`;
CREATE TABLE `roleinterface` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键（自增）',
  `createDate` datetime NOT NULL COMMENT '创建日期',
  `updateDate` datetime NOT NULL COMMENT '更新日期',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `group` varchar(100) DEFAULT NULL COMMENT '分组',
  `url` varchar(255) NOT NULL COMMENT 'url',
  `name` varchar(255) DEFAULT NULL COMMENT '接口名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `url_index` (`url`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
