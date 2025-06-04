/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:3306
 Source Schema         : intelligent_scheduling_db

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 04/06/2025 19:17:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `locked` tinyint(1) NOT NULL DEFAULT '0',
  `credentials_expired` tinyint(1) NOT NULL DEFAULT '0',
  `account_expired` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of app_user
-- ----------------------------
BEGIN;
INSERT INTO `app_user` (`id`, `username`, `password`, `email`, `full_name`, `enabled`, `locked`, `credentials_expired`, `account_expired`, `created_at`, `updated_at`) VALUES (1, 'admin', '$2a$10$sZ1TzynuZU2q9mqvvnUd9ulLwcK4ndlP.s8MZ3opcygeIMpgAWAVS', 'admin@example.com', 'System Administrator', 1, 0, 0, 0, '2025-05-29 13:17:20', '2025-05-29 13:17:20');
INSERT INTO `app_user` (`id`, `username`, `password`, `email`, `full_name`, `enabled`, `locked`, `credentials_expired`, `account_expired`, `created_at`, `updated_at`) VALUES (2, 'teacher1', '$2a$10$LyBVor/24cv.Q/I.yCjoLOjPHRN4hsOOSR.3SeF3tVUcIN3oY2umy', 'teacher1@example.com', 'Default Teacher111', 1, 0, 0, 0, '2025-05-29 13:17:20', '2025-05-31 09:22:33');
INSERT INTO `app_user` (`id`, `username`, `password`, `email`, `full_name`, `enabled`, `locked`, `credentials_expired`, `account_expired`, `created_at`, `updated_at`) VALUES (5, '123', '$2a$10$birlF47VcIAdI8nhqPPpNudu6Vlgvau9w/SurOjOUjNdHT36O/1CK', '112@qq.com', '小宝', 1, 0, 0, 0, '2025-05-31 09:23:41', '2025-05-31 09:23:41');
INSERT INTO `app_user` (`id`, `username`, `password`, `email`, `full_name`, `enabled`, `locked`, `credentials_expired`, `account_expired`, `created_at`, `updated_at`) VALUES (6, 'jiaoshi1', '$2a$10$USTGib1/Ksr127Xsu528qOB2lGD4mBHv1gprDt6FSHKbZrGxOxi0K', '1123@qq.com', '教师1', 1, 0, 0, 0, '2025-05-31 15:57:58', '2025-05-31 15:57:58');
COMMIT;

-- ----------------------------
-- Table structure for class_group_unavailabilities
-- ----------------------------
DROP TABLE IF EXISTS `class_group_unavailabilities`;
CREATE TABLE `class_group_unavailabilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_group` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教学对象标识 (对应 teaching_task.target_group)',
  `semester_id` bigint NOT NULL COMMENT '学期ID (外键关联semester.id)',
  `day_of_week` int NOT NULL COMMENT '星期几 (1=周一, ..., 7=周日)',
  `start_period` int NOT NULL COMMENT '开始节次 (从1开始)',
  `end_period` int NOT NULL COMMENT '结束节次',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '不可用的原因',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '此条不可用记录是否生效',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_class_group_semester_unavail` (`target_group`,`semester_id`),
  KEY `fk_class_group_unavail_semester` (`semester_id`),
  CONSTRAINT `fk_class_group_unavail_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级或教学对象不可用时间表';

-- ----------------------------
-- Records of class_group_unavailabilities
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for classroom
-- ----------------------------
DROP TABLE IF EXISTS `classroom`;
CREATE TABLE `classroom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `capacity` int NOT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `building_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `floor_level` int DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '可用',
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `room_number` (`room_number`),
  CONSTRAINT `chk_capacity_positive` CHECK ((`capacity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of classroom
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for constraint_rules
-- ----------------------------
DROP TABLE IF EXISTS `constraint_rules`;
CREATE TABLE `constraint_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '约束规则的易读名称',
  `constraint_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '约束的唯一编码/类型标识',
  `constraint_type` enum('HARD','SOFT') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '约束类型：HARD硬约束, SOFT软约束',
  `target_entity_type` enum('GLOBAL','TEACHER','CLASS_GROUP','COURSE','ROOM','TEACHING_TASK') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '约束主要作用的对象类型',
  `target_entity_id` bigint DEFAULT NULL COMMENT '作用对象的具体ID (配合target_entity_type)',
  `semester_id` bigint DEFAULT NULL COMMENT '适用的学期ID (外键关联semester.id)',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用此约束',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '对约束的详细描述',
  `parameters_json` json DEFAULT NULL COMMENT '存储约束的具体参数 (结构因constraint_code而异)',
  `penalty_weight` int DEFAULT NULL COMMENT '仅用于软约束，违反的惩罚权重',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `constraint_code` (`constraint_code`),
  UNIQUE KEY `UKj88ratg3nidqeh2aea10ehct1` (`constraint_code`),
  KEY `idx_constraint_code` (`constraint_code`),
  KEY `idx_target_entity` (`target_entity_type`,`target_entity_id`),
  KEY `idx_semester_id` (`semester_id`),
  CONSTRAINT `fk_constraint_rules_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排课约束规则定义表';

-- ----------------------------
-- Records of constraint_rules
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credits` double NOT NULL,
  `hours_per_week` int DEFAULT NULL,
  `total_hours` int DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `required_room_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '激活',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `course_code` (`course_code`),
  CONSTRAINT `chk_credits_positive` CHECK ((`credits` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of course
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;
INSERT INTO `role` (`id`, `name`, `description`) VALUES (1, 'ROLE_ADMIN', 'Administrator - Full system access');
INSERT INTO `role` (`id`, `name`, `description`) VALUES (2, 'ROLE_TEACHER', 'Teacher - Access to teacher-specific functionalities');
INSERT INTO `role` (`id`, `name`, `description`) VALUES (3, 'ROLE_STUDENT', 'Student - Access to student-specific functionalities');
COMMIT;

-- ----------------------------
-- Table structure for room_unavailabilities
-- ----------------------------
DROP TABLE IF EXISTS `room_unavailabilities`;
CREATE TABLE `room_unavailabilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '教室ID (外键关联classroom.id)',
  `semester_id` bigint NOT NULL COMMENT '学期ID (外键关联semester.id)',
  `day_of_week` int NOT NULL COMMENT '星期几 (1=周一, ..., 7=周日)',
  `start_period` int NOT NULL COMMENT '开始节次 (从1开始)',
  `end_period` int NOT NULL COMMENT '结束节次',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '不可用的原因',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '此条不可用记录是否生效',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_room_semester_unavail` (`room_id`,`semester_id`),
  KEY `fk_room_unavail_semester` (`semester_id`),
  CONSTRAINT `fk_room_unavail_room` FOREIGN KEY (`room_id`) REFERENCES `classroom` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_room_unavail_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室不可用时间表';

-- ----------------------------
-- Records of room_unavailabilities
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for semester
-- ----------------------------
DROP TABLE IF EXISTS `semester`;
CREATE TABLE `semester` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `is_current` tinyint(1) DEFAULT '0',
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `total_weeks` int DEFAULT NULL COMMENT '学期总周数',
  `periods_per_day` int DEFAULT NULL COMMENT '每日总节次数',
  `days_per_week` int DEFAULT NULL COMMENT '每周上课天数 (例如 5 或 7)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semester_year_name` (`academic_year`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of semester
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `teachable_courses` text COLLATE utf8mb4_unicode_ci,
  `unavailable_slots` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '在职',
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `teacher_id_number` (`teacher_id_number`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of teacher
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for teacher_unavailabilities
-- ----------------------------
DROP TABLE IF EXISTS `teacher_unavailabilities`;
CREATE TABLE `teacher_unavailabilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL COMMENT '教师ID (外键关联teacher.id)',
  `semester_id` bigint NOT NULL COMMENT '学期ID (外键关联semester.id)',
  `day_of_week` int NOT NULL COMMENT '星期几 (1=周一, ..., 7=周日)',
  `start_period` int NOT NULL COMMENT '开始节次 (从1开始)',
  `end_period` int NOT NULL COMMENT '结束节次',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '不可用的原因',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '此条不可用记录是否生效',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_teacher_semester_unavail` (`teacher_id`,`semester_id`),
  KEY `fk_teacher_unavail_semester` (`semester_id`),
  CONSTRAINT `fk_teacher_unavail_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_teacher_unavail_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师不可用时间表';

-- ----------------------------
-- Records of teacher_unavailabilities
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for teaching_task
-- ----------------------------
DROP TABLE IF EXISTS `teaching_task`;
CREATE TABLE `teaching_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `hours_per_week` int NOT NULL,
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `required_room_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `session_length` int NOT NULL,
  `sessions_per_week` int NOT NULL,
  `target_group` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `semester_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teaching_task_semester_course_group` (`semester_id`,`course_id`,`target_group`),
  KEY `FK1ohov20ymqxb3kg3r8d59sd7b` (`course_id`),
  KEY `FKbig0y1fxf60q01gp66wu0xy8f` (`teacher_id`),
  CONSTRAINT `FK1ohov20ymqxb3kg3r8d59sd7b` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  CONSTRAINT `FKbig0y1fxf60q01gp66wu0xy8f` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`),
  CONSTRAINT `FKpcqj2m9bfet6f9ddvvhxsgjxx` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of teaching_task
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of user_role
-- ----------------------------
BEGIN;
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (2, 2);
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (5, 2);
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (6, 2);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
