-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: gyjz
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `donation`
--

DROP TABLE IF EXISTS `donation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `donor_name` varchar(20) DEFAULT NULL COMMENT '捐赠人姓名',
  `donor_phone` varchar(11) NOT NULL COMMENT '捐赠人手机号',
  `amount` decimal(10,2) NOT NULL COMMENT '捐赠金额',
  `donation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
  `certificate_no` varchar(20) NOT NULL COMMENT '凭证号',
  `payment` varchar(10) NOT NULL COMMENT '模拟支付/微信/支付宝',
  `source` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-线上，2-线下',
  `donation_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-成功，2-已退款，3-异常',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(100) DEFAULT NULL COMMENT '退款原因',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_certificate_no` (`certificate_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_donor_phone` (`donor_phone`),
  KEY `idx_donation_status` (`donation_status`),
  CONSTRAINT `fk_donation_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='捐赠记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donation`
--

LOCK TABLES `donation` WRITE;
/*!40000 ALTER TABLE `donation` DISABLE KEYS */;
INSERT INTO `donation` VALUES (1,1,1001,'张晓明','13800000001',5000.00,'2026-01-02 10:30:00','DON2026010200001','微信',1,1,NULL,NULL,'为山区孩子尽一份力','2026-01-02 10:30:00'),(2,1,1002,'李华','13900000002',200.50,'2026-01-04 14:15:22','DON2026010400002','支付宝',1,1,NULL,NULL,'孩子们加油！','2026-01-04 14:15:22'),(3,1,1003,'王建国','13700000003',10000.00,'2026-01-05 09:00:00','DON2026010500003','模拟支付',1,1,NULL,NULL,'支持乡村教育','2026-01-05 09:00:00'),(4,1,NULL,'爱心人士','13600000004',50000.00,'2026-01-08 16:45:10','DON2026010800004','模拟支付',2,1,NULL,NULL,'匿名企业大额定向捐赠','2026-01-08 16:45:10'),(5,1,1004,'赵丽','13500000005',3320.00,'2026-01-10 11:20:30','DON2026011000005','微信',1,1,NULL,NULL,'买几本好书','2026-01-10 11:20:30'),(6,1,11,'爱心人士杨博彬','17418868901',114514.00,'2026-04-01 11:31:41','DON2026011000009','微信',1,1,NULL,NULL,'加油','2026-04-01 11:31:41'),(7,2,11,'爱心人士杨博彬','17418868901',10.00,'2026-04-02 09:35:28','GY17750937276087525','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:35:28'),(8,2,11,'爱心人士杨博彬','17418868901',10.00,'2026-04-02 09:35:33','GY177509373255034AC','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:35:33'),(9,2,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:35:53','GY177509375305276A0','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:35:53'),(10,2,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:35:57','GY1775093757213A31B','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:35:57'),(11,2,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:36:01','GY1775093760547E7AA','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:36:01'),(12,8,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:36:31','GY1775093791463DF42','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:36:31'),(13,8,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:36:35','GY17750937954367293','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:36:35'),(14,8,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:36:38','GY17750937981677663','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:36:38'),(15,2,11,'爱心人士杨博彬','17418868901',500.00,'2026-04-02 09:51:26','GY1775094685744469B','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:51:26'),(16,1,11,'爱心人士杨博彬','17418868901',10.00,'2026-04-02 09:51:38','GY17750946977822113','模拟支付',1,1,NULL,NULL,'','2026-04-02 09:51:38'),(17,9,11,'爱心人士杨博彬','17418868901',2000.00,'2026-04-02 14:22:39','GY1775110959032DC62','模拟支付',1,1,NULL,NULL,'','2026-04-02 14:22:39'),(18,9,11,'爱心人士杨博彬','17418868901',10.00,'2026-04-02 14:38:30','GY177511191031689B6','模拟支付',1,1,NULL,NULL,'','2026-04-02 14:38:30');
/*!40000 ALTER TABLE `donation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organization` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机构ID',
  `name` varchar(100) NOT NULL COMMENT '机构名称',
  `code` varchar(50) DEFAULT NULL COMMENT '机构代码/统一社会信用代码',
  `logo` varchar(200) DEFAULT NULL COMMENT '机构Logo',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '机构简介',
  `address` varchar(200) DEFAULT NULL COMMENT '机构地址',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `org_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-正常，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`org_status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公益机构表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organization`
--

LOCK TABLES `organization` WRITE;
/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
INSERT INTO `organization` VALUES (1,'希望公益基金会',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,'2026-03-28 14:21:24',NULL),(2,'爱心公益协会',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,'2026-03-28 15:18:17',NULL),(3,'江门一中',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,'2026-04-02 11:09:55',NULL);
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `title` varchar(100) NOT NULL COMMENT '项目标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '项目描述',
  `target_amount` decimal(10,2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前已筹金额',
  `project_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-进行中，2-已结束',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  `cover_image` varchar(200) DEFAULT NULL COMMENT '封面图URL',
  `category` varchar(10) DEFAULT NULL COMMENT '助学/助老/环保/医疗',
  `sort_order` int DEFAULT '0' COMMENT '排序权重',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `org_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_project_status` (`project_status`),
  KEY `idx_category` (`category`),
  KEY `idx_start_date` (`start_date`),
  KEY `project_organization_FK` (`org_id`),
  CONSTRAINT `project_organization_FK` FOREIGN KEY (`org_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (1,'山村儿童阅读计划','为偏远山区小学捐赠图书室，帮助孩子们开拓视野，目前已覆盖10所小学，受益学生超过3000人。计划筹集资金用于购买图书、书架和阅读桌椅。',100000.00,68530.50,1,'2026-01-01 00:00:00','2026-12-31 23:59:59','/images/project/reading_plan.png','教育助学',1,'2026-01-01 10:00:00',1),(2,'白血病患儿救助计划','为贫困家庭的白血病患儿提供医疗费用支持，帮助孩子们获得及时治疗。目前已成功救助12名患儿，累计支出医疗费用80余万元。',200000.00,126370.00,1,'2026-02-01 00:00:00','2026-08-31 23:59:59','/images/project/leukemia_help.png','医疗',2,'2026-02-01 09:30:00',1),(3,'社区独居老人关爱行动','为社区独居老人提供生活照料、健康监测和精神慰藉服务，包括定期上门探访、代买代购、健康讲座等。计划服务500位独居老人。',150000.00,89230.00,1,'2026-01-15 00:00:00','2026-10-15 23:59:59','/images/project/elderly_care.png','助老',1,'2026-01-15 14:20:00',1),(5,'贫困大学生助学金计划','为家庭经济困难的大学生提供助学金支持，帮助他们顺利完成学业。2025年度共资助100名大学生，发放助学金50万元。',500000.00,500000.00,2,'2025-01-01 00:00:00','2025-12-31 23:59:59','/images/project/scholarship.png','助学',0,'2025-01-01 08:00:00',2),(6,'罕见病药物研发支持','支持罕见病药物研发工作，为罕见病患者带来希望。由于项目难度较大，筹集资金未达到目标。',300000.00,125000.00,2,'2025-06-01 00:00:00','2025-12-31 23:59:59','/images/project/rare_disease.png','医疗',0,'2025-06-01 10:00:00',2),(7,'荒漠化治理植树行动','在西北荒漠化地区开展植树造林活动，计划种植10万棵梭梭树，治理荒漠化土地2000亩。',250000.00,198765.00,1,'2026-03-15 00:00:00','2026-11-30 23:59:59','/images/project/tree_planting.png','环保',1,'2026-03-15 09:00:00',2),(8,'老年大学公益课程','为老年人提供智能手机使用、书法绘画、健康养生等公益课程，丰富老年人精神文化生活。',60000.00,43850.00,1,'2026-01-10 00:00:00','2026-04-30 23:59:59','/images/project/elderly_education.png','助老',2,'2026-01-10 15:30:00',2),(9,'帮助QB回家','qb啊qb啊，qqqqq\n中山乳胶压抑大学',1000.00,2010.00,2,'2026-04-02 00:00:00','2026-05-01 23:59:59','/images/project/aa0a4b0181af4e8483fa425fc5a811cf.png','教育助学',0,'2026-04-02 11:31:22',3);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `push_record`
--

DROP TABLE IF EXISTS `push_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `push_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推送ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `title` varchar(50) NOT NULL COMMENT '标题',
  `content` varchar(100) NOT NULL COMMENT '内容',
  `type` tinyint(1) NOT NULL COMMENT '1-项目进度，2-新项目，3-捐赠成功',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `send_status` tinyint(1) NOT NULL COMMENT '1-成功，2-失败',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推送记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `push_record`
--

LOCK TABLES `push_record` WRITE;
/*!40000 ALTER TABLE `push_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `push_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `register_apply`
--

DROP TABLE IF EXISTS `register_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `register_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `institution_name` varchar(100) NOT NULL COMMENT '机构名称',
  `credit_code` varchar(18) NOT NULL COMMENT '统一社会信用代码',
  `contact_person` varchar(20) NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `qualification` varchar(200) NOT NULL COMMENT '资质文件URL',
  `apply_reason` varchar(200) DEFAULT NULL COMMENT '申请说明',
  `apply_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态：0-待审核，1-已通过，2-不通过',
  `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  PRIMARY KEY (`id`),
  KEY `idx_apply_status` (`apply_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='注册申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_apply`
--

LOCK TABLES `register_apply` WRITE;
/*!40000 ALTER TABLE `register_apply` DISABLE KEYS */;
INSERT INTO `register_apply` VALUES (2,'13912340003','123456','阳光公益基金会','91440000123456789X','王五','020-12345678','/upload/qualification/sunshine.pdf','希望为贫困山区儿童提供教育支持',2,'资质文件不清晰，请重新上传','2026-03-26 14:51:52','2026-03-26 14:52:46',1),(4,'13912340003','123456','阳光公益基金会','91440000123456789X','王五','020-12345678','/upload/qualification/sunshine.pdf','希望为贫困山区儿童提供教育支持',1,'审核通过，账号：yggyjjh_3818','2026-03-26 15:02:01','2026-03-26 15:05:29',1),(5,'13823894729','123456','华南农业大学','124400004554165634','潘浩敏','13823894729','https://gd-hbimg.huaban.com/c442a5d359c45ebaea7f07801fea2b23a4f33bfd16806-Ge02ko_fw658','华农公益',0,NULL,'2026-03-28 10:39:30',NULL,NULL),(6,'13814801578','$2a$10$n1VF5tea19VkAtJIKb6sPuEGXNwlg6aXUy0di43yTUdNXfemJy99e','江门一中','111111111111111111','夏志清','13408891478','https://www.iplaysoft.com/free-images.html','某',1,'审核通过，账号：jmyz_1604','2026-04-01 10:10:15','2026-04-02 10:41:10',1);
/*!40000 ALTER TABLE `register_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_project` (`user_id`,`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`),
  CONSTRAINT `fk_sub_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`),
  CONSTRAINT `fk_sub_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订阅表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(20) DEFAULT NULL COMMENT '登录账号',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(20) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像URL',
  `role` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-捐赠人，2-管理员，9-超级管理员',
  `org_id` bigint DEFAULT NULL COMMENT '所属机构ID',
  `user_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-正常，2-禁用',
  `push_token` varchar(200) DEFAULT NULL COMMENT '鸿蒙推送Token',
  `push_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1-开启，0-关闭',
  `last_login` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_user_status` (`user_status`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin',NULL,'123','超级管理员','https://example.com/avatar/admin.png',9,NULL,1,NULL,1,'2026-03-26 10:45:43','2026-03-26 10:45:43'),(2,'hope_2344','13812340001','123','希望公益基金会','https://example.com/avatar/hope.png',2,1,1,'push_token_admin_001',1,'2026-03-26 10:45:43','2026-03-26 10:45:43'),(3,'love_5424','13812340002','123','爱心公益协会','https://example.com/avatar/love.png',2,2,1,'push_token_admin_002',1,'2026-03-26 10:45:43','2026-03-26 10:45:43'),(4,NULL,'13912340001','123','爱心人士张三','https://example.com/avatar/zhangsan.png',1,NULL,1,'push_token_user_001',1,'2026-03-26 10:45:43','2026-03-26 10:45:43'),(5,NULL,'13912340002','123','匿名用户李四','https://example.com/avatar/lisi.png',1,NULL,1,NULL,0,'2026-03-26 10:45:43','2026-03-26 10:45:43'),(7,'yggyjjh_3818','13912340003','123456','阳光公益基金会',NULL,2,NULL,1,NULL,1,NULL,'2026-03-26 15:05:29'),(8,NULL,'13025823116','123456','qb',NULL,1,NULL,1,NULL,1,NULL,'2026-03-28 10:18:37'),(9,NULL,'13965372836','123456','xy',NULL,1,NULL,1,NULL,1,NULL,'2026-03-28 10:26:41'),(10,NULL,'13284515864','123456','cv',NULL,1,NULL,1,NULL,1,NULL,'2026-03-28 10:29:40'),(11,NULL,'17418868901','$2a$10$5CD5kUE0FuJqHg2dhRfWL.7DBBJ84FNnSiWtvD/jzaHmUhzXqAxli','爱心人士杨博彬',NULL,1,NULL,1,NULL,1,NULL,'2026-04-01 10:14:23'),(12,'jmyz_1604','13814801578','$2a$10$n1VF5tea19VkAtJIKb6sPuEGXNwlg6aXUy0di43yTUdNXfemJy99e','江门一中',NULL,2,3,1,NULL,1,NULL,'2026-04-02 10:41:10');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'gyjz'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-03 11:37:20
