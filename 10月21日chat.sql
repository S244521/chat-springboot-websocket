/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : chat

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 21/10/2025 20:36:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for conversation
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '聊天名称',
  `type` tinyint(1) NOT NULL COMMENT '会话类型（0-私聊，1-群聊等）',
  `conversation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标准化的参与者字符串',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of conversation
-- ----------------------------
INSERT INTO `conversation` VALUES ('13f695a9f2bc4687838cda95351efd47', NULL, 0, '9');
INSERT INTO `conversation` VALUES ('1be2d03e94874adfacc162d1c8d4428c', '深夜树洞', 1, '1');
INSERT INTO `conversation` VALUES ('3029bbc5b00e4a37b5cb8be3fcbbc331', '天地一家大爱盟', 1, '');
INSERT INTO `conversation` VALUES ('43f2b4b766114a51b21aacf87dbe3033', '智哥搞事情', 1, '1,9');
INSERT INTO `conversation` VALUES ('80f8c269c946438186a25097752ea4d0', NULL, 0, '7,9');
INSERT INTO `conversation` VALUES ('823e2259c4054927b4835f7ec270af89', NULL, 0, '9,14');
INSERT INTO `conversation` VALUES ('975041d8220d49c48516c61732b4fceb', '智哥悄悄话', 1, '1');

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文件表id',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `fileurl` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文件地址',
  `uploadtime` datetime NULL DEFAULT NULL COMMENT '上传时间',
  `num` int NULL DEFAULT 0 COMMENT '下载次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件资源表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file
-- ----------------------------
INSERT INTO `file` VALUES (1, 'chat.sql', 'http://localhost:9999/file/download/6ab7d10a-d0d8-474f-96cb-9a88c2b52ffe.sql', '2025-10-21 13:39:27', 0);
INSERT INTO `file` VALUES (2, 'java后端陶怀智简历.pdf', 'http://localhost:9999/file/download/45916505-85cf-4061-8fd0-8ef60d0b8059.pdf', '2025-10-21 13:39:46', 0);
INSERT INTO `file` VALUES (3, 'java面试.md', 'http://localhost:9999/file/download/d58c71d8-c820-44ec-9644-1fed96780585.md', '2025-10-21 13:39:55', 0);
INSERT INTO `file` VALUES (4, 'spring面试题.md', 'http://localhost:9999/file/download/f3ec86be-98ea-4e50-aa0c-a5eb03bddf31.md', '2025-10-21 13:40:02', 0);

-- ----------------------------
-- Table structure for history
-- ----------------------------
DROP TABLE IF EXISTS `history`;
CREATE TABLE `history`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '聊天记录id',
  `userid` int NULL DEFAULT NULL COMMENT '用户id',
  `conversationid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话id',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '聊天记录',
  `createtime` datetime NULL DEFAULT NULL COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of history
-- ----------------------------

-- ----------------------------
-- Table structure for root
-- ----------------------------
DROP TABLE IF EXISTS `root`;
CREATE TABLE `root`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '后台账号id',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台账号表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of root
-- ----------------------------
INSERT INTO `root` VALUES (1, 'root', 'root');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `sex` tinyint(1) NULL DEFAULT 2 COMMENT '性别(0-女,1-男,2-未知)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '244521', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '陶哥', 0);
INSERT INTO `user` VALUES (2, '123456', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '洛天依', 1);
INSERT INTO `user` VALUES (3, 'jiege', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '杰哥', 2);
INSERT INTO `user` VALUES (4, 'laiyila', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '莱伊拉', 2);
INSERT INTO `user` VALUES (5, 'naxida', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '纳西达', 1);
INSERT INTO `user` VALUES (6, 'keqing', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '刻晴', 2);
INSERT INTO `user` VALUES (7, 'wangchuqing', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '万晴初', 1);
INSERT INTO `user` VALUES (8, 'guyuefangyuan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '古月方源', 0);
INSERT INTO `user` VALUES (9, 'wusuai', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '吴帅', 0);
INSERT INTO `user` VALUES (10, 'wuyong', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '武庸', 2);
INSERT INTO `user` VALUES (11, 'fengjinhuang', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '凤金煌', 1);
INSERT INTO `user` VALUES (12, 'fengjiuge', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '凤九歌', 2);
INSERT INTO `user` VALUES (13, 'youhun', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '幽魂', 2);
INSERT INTO `user` VALUES (14, 'wangxiaomei', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王小美', 0);
INSERT INTO `user` VALUES (15, 'xitelali', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '茜特菈莉', 0);
INSERT INTO `user` VALUES (16, 'falusan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '珐露珊', 0);
INSERT INTO `user` VALUES (17, 'yanfei', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '烟菲', 1);
INSERT INTO `user` VALUES (18, 'qihailaozu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '气海老祖', 2);
INSERT INTO `user` VALUES (19, 'xingxiu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '星宿', 0);
INSERT INTO `user` VALUES (20, 'wuji', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '无极', 1);
INSERT INTO `user` VALUES (21, 'honglian', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '红莲', 2);
INSERT INTO `user` VALUES (22, 'kuangman', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '狂蛮', 0);
INSERT INTO `user` VALUES (23, 'yuanshi', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '原始', 1);
INSERT INTO `user` VALUES (24, 'yuanlian', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '元莲', 2);
INSERT INTO `user` VALUES (25, 'daotian', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '盗天', 0);

-- ----------------------------
-- Triggers structure for table conversation
-- ----------------------------
DROP TRIGGER IF EXISTS `check_conversation_unique_before_insert`;
delimiter ;;
CREATE TRIGGER `check_conversation_unique_before_insert` BEFORE INSERT ON `conversation` FOR EACH ROW BEGIN
  IF NEW.`type` = 0 THEN
    -- 检查是否已有 type=0 且 conversation 相同的记录
    IF EXISTS (
      SELECT 1 FROM `conversation` 
      WHERE `type` = 0 
        AND SUBSTRING(`conversation`, 1, 300) = SUBSTRING(NEW.`conversation`, 1, 300)
    ) THEN
      SIGNAL SQLSTATE '45000' 
      SET MESSAGE_TEXT = 'Duplicate conversation for type=0';
    END IF;
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table conversation
-- ----------------------------
DROP TRIGGER IF EXISTS `check_conversation_unique_before_update`;
delimiter ;;
CREATE TRIGGER `check_conversation_unique_before_update` BEFORE UPDATE ON `conversation` FOR EACH ROW BEGIN
  IF NEW.`type` = 0 THEN
    IF EXISTS (
      SELECT 1 FROM `conversation` 
      WHERE `type` = 0 
        AND SUBSTRING(`conversation`, 1, 300) = SUBSTRING(NEW.`conversation`, 1, 300)
        AND `id` != NEW.`id`  -- 排除自身记录
    ) THEN
      SIGNAL SQLSTATE '45000' 
      SET MESSAGE_TEXT = 'Duplicate conversation for type=0';
    END IF;
  END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
